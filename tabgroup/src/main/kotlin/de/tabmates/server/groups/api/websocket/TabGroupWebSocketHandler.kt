package de.tabmates.server.groups.api.websocket

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.common.service.JwtService
import de.tabmates.server.groups.api.dto.ws.DeleteTabEntryDto
import de.tabmates.server.groups.api.dto.ws.ErrorDto
import de.tabmates.server.groups.api.dto.ws.GroupParticipantsChangedDto
import de.tabmates.server.groups.api.dto.ws.IncomingWebSocketMessage
import de.tabmates.server.groups.api.dto.ws.IncomingWebSocketMessageType
import de.tabmates.server.groups.api.dto.ws.NewTabEntryDto
import de.tabmates.server.groups.api.dto.ws.OutgoingWebSocketMessage
import de.tabmates.server.groups.api.dto.ws.OutgoingWebSocketMessageType
import de.tabmates.server.groups.api.mappers.toTabEntry
import de.tabmates.server.groups.api.mappers.toTabEntryDto
import de.tabmates.server.groups.domain.event.GroupCreatedEvent
import de.tabmates.server.groups.domain.event.GroupParticipantJoinedEvent
import de.tabmates.server.groups.domain.event.GroupParticipantLeftEvent
import de.tabmates.server.groups.domain.event.TabEntryDeletedEvent
import de.tabmates.server.groups.service.GroupService
import de.tabmates.server.groups.service.TabEntryService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class TabGroupWebSocketHandler(
    private val tabEntryService: TabEntryService,
    private val objectMapper: ObjectMapper,
    private val groupService: GroupService,
    private val jwtService: JwtService,
) : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val connectionLock = ReentrantReadWriteLock()

    private val sessions = ConcurrentHashMap<String, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()
    private val userGroupIds = ConcurrentHashMap<UserId, MutableSet<GroupId>>()
    private val groupToSessions = ConcurrentHashMap<GroupId, MutableSet<String>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader =
            session
                .handshakeHeaders
                .getFirst(HttpHeaders.AUTHORIZATION)
                ?: run {
                    logger.warn("Session ${session.id} was closed due to missing Authorization header")
                    session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"))
                    return
                }

        val userId = jwtService.getUserIdFromToken(authHeader)

        val userSession =
            UserSession(
                userId = userId,
                session = session,
            )

        connectionLock.write {
            sessions[session.id] = userSession

            userToSessions.compute(userId) { _, existingSessions ->
                (existingSessions ?: mutableSetOf()).apply {
                    add(session.id)
                }
            }

            val groupIds =
                userGroupIds.computeIfAbsent(userId) {
                    val chatIds = groupService.findGroupsByUser(userId).map { it.id }
                    ConcurrentHashMap.newKeySet<GroupId>().apply {
                        addAll(chatIds)
                    }
                }

            groupIds.forEach { groupId ->
                groupToSessions.compute(groupId) { _, existingSessions ->
                    (existingSessions ?: mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }
        }

        logger.info("WebSocket connection established for user $userId with session ${session.id}")
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        connectionLock.write {
            sessions.remove(session.id)?.let { userSession ->
                userToSessions.compute(userSession.userId) { _, existingSessions ->
                    existingSessions
                        ?.apply { remove(session.id) }
                        ?.takeIf { it.isNotEmpty() }
                }

                userGroupIds[userSession.userId]?.forEach { groupId ->
                    groupToSessions.compute(groupId) { _, existingSessions ->
                        existingSessions
                            ?.apply { remove(session.id) }
                            ?.takeIf { it.isNotEmpty() }
                    }
                }

                logger.info(
                    "WebSocket connection closed for user ${userSession.userId} with session ${session.id}",
                )
            }
        }
    }

    override fun handleTransportError(
        session: WebSocketSession,
        exception: Throwable,
    ) {
        logger.error("Transport error in session ${session.id}", exception)
        try {
            session.close(CloseStatus.SERVER_ERROR.withReason("Transport error"))
        } catch (e: Exception) {
            logger.error("Couldn't close session for session ${session.id}", e)
        }
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        logger.debug("Received message: ${message.payload} from session: ${session.id}")

        val userSession =
            connectionLock.read {
                sessions[session.id] ?: return
            }

        try {
            val webSocketMessage =
                objectMapper.readValue(
                    message.payload,
                    IncomingWebSocketMessage::class.java,
                )
            when (webSocketMessage.type) {
                IncomingWebSocketMessageType.NEW_TAB_ENTRY -> {
                    val newTabEntryDto =
                        objectMapper.readValue(
                            webSocketMessage.payload,
                            NewTabEntryDto::class.java,
                        )
                    handleNewTabEntry(
                        dto = newTabEntryDto,
                        senderId = userSession.userId,
                    )
                }

                IncomingWebSocketMessageType.UPDATED_TAB_ENTRY -> {
                    val newTabEntryDto =
                        objectMapper.readValue(
                            webSocketMessage.payload,
                            NewTabEntryDto::class.java,
                        )
                    handleUpdatedTabEntry(
                        dto = newTabEntryDto,
                        session = userSession.session,
                        senderId = userSession.userId,
                    )
                }
            }
        } catch (e: JacksonException) {
            logger.warn("Could not parse message ${message.payload}", e)
            sendError(
                session = userSession.session,
                error =
                    ErrorDto(
                        code = "INVALID_JSON",
                        message = "Incoming JSON or UUID is invalid",
                    ),
            )
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onDeleteTabEntry(event: TabEntryDeletedEvent) {
        broadcastToGroup(
            groupId = event.groupId,
            message =
                OutgoingWebSocketMessage(
                    type = OutgoingWebSocketMessageType.TAB_ENTRY_DELETED,
                    payload =
                        objectMapper.writeValueAsString(
                            DeleteTabEntryDto(
                                groupId = event.groupId,
                                tabEntryId = event.tabEntryId,
                            ),
                        ),
                ),
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onGroupCreated(event: GroupCreatedEvent) {
        updateGroupForUsers(
            groupId = event.groupId,
            userIds = event.participantIds,
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onJoinGroup(event: GroupParticipantJoinedEvent) {
        updateGroupForUsers(
            groupId = event.groupId,
            userIds = event.userIds,
        )

        broadcastToGroup(
            groupId = event.groupId,
            message =
                OutgoingWebSocketMessage(
                    type = OutgoingWebSocketMessageType.GROUP_PARTICIPANTS_CHANGED,
                    payload =
                        objectMapper.writeValueAsString(
                            GroupParticipantsChangedDto(
                                groupId = event.groupId,
                            ),
                        ),
                ),
        )
    }

    private fun updateGroupForUsers(
        groupId: GroupId,
        userIds: Set<UserId>,
    ) {
        connectionLock.write {
            userIds.forEach { userId ->
                userGroupIds.compute(userId) { _, existingChatIds ->
                    (existingChatIds ?: mutableSetOf()).apply {
                        add(groupId)
                    }
                }
                userToSessions[userId]?.forEach { sessionId ->
                    groupToSessions.compute(groupId) { _, existingSessions ->
                        (existingSessions ?: mutableSetOf()).apply {
                            add(sessionId)
                        }
                    }
                }
            }
        }
    }

    override fun handlePongMessage(
        session: WebSocketSession,
        message: PongMessage,
    ) {
        connectionLock.write {
            sessions.compute(session.id) { _, userSession ->
                userSession?.copy(
                    lastPongTimestamp = System.currentTimeMillis(),
                )
            }
        }
        logger.debug("Received pong from session {}", session.id)
    }

    @Scheduled(fixedDelay = PING_INTERVAL_MS)
    fun pingClients() {
        val currentTime = System.currentTimeMillis()
        val sessionsToClose = mutableListOf<String>()
        val sessionsSnapshot = connectionLock.read { sessions.toMap() }

        sessionsSnapshot.forEach { (sessionId, userSession) ->
            try {
                if (userSession.session.isOpen) {
                    val lastPong = userSession.lastPongTimestamp
                    if (currentTime - lastPong > PONG_TIMEOUT_MS) {
                        logger.warn("Session $sessionId timed out due to missing pong")
                        sessionsToClose.add(sessionId)
                    } else {
                        userSession.session.sendMessage(PingMessage())
                        logger.debug("Sent ping to {}", userSession.userId)
                    }
                } else {
                    sessionsToClose.add(sessionId)
                }
            } catch (e: Exception) {
                logger.error("Could not ping session $sessionId", e)
                sessionsToClose.add(sessionId)
            }
        }

        sessionsToClose.forEach { sessionId ->
            connectionLock.read {
                sessions[sessionId]?.session?.let { session ->
                    try {
                        session.close(CloseStatus.GOING_AWAY.withReason("Ping timeout"))
                    } catch (e: Exception) {
                        logger.error("Couldn't close session for session ${session.id}", e)
                    }
                }
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onLeftGroup(event: GroupParticipantLeftEvent) {
        connectionLock.write {
            userGroupIds.compute(event.userId) { _, existingGroupIds ->
                existingGroupIds
                    ?.apply { remove(event.groupId) }
                    ?.takeIf { it.isNotEmpty() }
            }
            userToSessions[event.userId]?.forEach { sessionId ->
                groupToSessions.compute(event.groupId) { _, existingSessions ->
                    existingSessions
                        ?.apply { remove(sessionId) }
                        ?.takeIf { it.isNotEmpty() }
                }
            }
        }

        broadcastToGroup(
            groupId = event.groupId,
            message =
                OutgoingWebSocketMessage(
                    type = OutgoingWebSocketMessageType.GROUP_PARTICIPANTS_CHANGED,
                    payload =
                        objectMapper.writeValueAsString(
                            GroupParticipantsChangedDto(
                                groupId = event.groupId,
                            ),
                        ),
                ),
        )
    }

    private fun sendError(
        session: WebSocketSession,
        error: ErrorDto,
    ) {
        val webSocketMessage =
            objectMapper.writeValueAsString(
                OutgoingWebSocketMessage(
                    type = OutgoingWebSocketMessageType.ERROR,
                    payload = objectMapper.writeValueAsString(error),
                ),
            )

        try {
            session.sendMessage(TextMessage(webSocketMessage))
        } catch (e: Exception) {
            logger.error("Error while sending error message to session ${session.id}", e)
        }
    }

    private fun broadcastToGroup(
        groupId: GroupId,
        message: OutgoingWebSocketMessage,
    ) {
        val tabGroupSessions =
            connectionLock.read {
                groupToSessions[groupId] ?: emptySet()
            }
        tabGroupSessions.forEach { sessionId ->
            val userSession =
                connectionLock.read {
                    sessions[sessionId] ?: return@forEach
                }
            sendToUser(
                userId = userSession.userId,
                message = message,
            )
        }
    }

    private fun handleNewTabEntry(
        dto: NewTabEntryDto,
        senderId: UserId,
    ) {
        val userGroupIds = connectionLock.read { this.userGroupIds[senderId] } ?: return

        if (dto.groupId !in userGroupIds) {
            return
        }

        val savedTabEntry =
            tabEntryService.addTabEntry(
                groupId = dto.groupId,
                creatorId = senderId,
                paidByUserId = dto.paidByUserId,
                title = dto.title,
                description = dto.description,
                amount = dto.amount,
                currency = dto.currency,
                splits = dto.splits.map { it.toTabEntry() },
                tabEntryId = dto.id,
            )

        broadcastToGroup(
            groupId = dto.groupId,
            message =
                OutgoingWebSocketMessage(
                    type = OutgoingWebSocketMessageType.NEW_TAB_ENTRY,
                    payload = objectMapper.writeValueAsString(savedTabEntry.toTabEntryDto()),
                ),
        )
    }

    private fun handleUpdatedTabEntry(
        dto: NewTabEntryDto,
        session: WebSocketSession,
        senderId: UserId,
    ) {
        val userGroupIds = connectionLock.read { this.userGroupIds[senderId] } ?: return

        if (dto.groupId !in userGroupIds) {
            return
        }

        if (dto.id == null) {
            logger.warn("Received update for tab entry with null id from user $senderId")
            sendError(
                session = session,
                error =
                    ErrorDto(
                        code = "INVALID_JSON",
                        message = "Incoming JSON or UUID is invalid",
                    ),
            )
            return
        }

        val savedTabEntry =
            tabEntryService.updateTabEntry(
                tabEntryId = dto.id,
                modifiedByUserId = senderId,
                paidByUserId = dto.paidByUserId,
                title = dto.title,
                description = dto.description,
                amount = dto.amount,
                currency = dto.currency,
                splits = dto.splits.map { it.toTabEntry() },
            )

        broadcastToGroup(
            groupId = dto.groupId,
            message =
                OutgoingWebSocketMessage(
                    type = OutgoingWebSocketMessageType.UPDATED_TAB_ENTRY,
                    payload = objectMapper.writeValueAsString(savedTabEntry.toTabEntryDto()),
                ),
        )
    }

    private fun sendToUser(
        userId: UserId,
        message: OutgoingWebSocketMessage,
    ) {
        val userSessions =
            connectionLock.read {
                userToSessions[userId] ?: emptySet()
            }
        userSessions.forEach { sessionId ->
            val userSession =
                connectionLock.read {
                    sessions[sessionId] ?: return@forEach
                }
            if (userSession.session.isOpen) {
                try {
                    val messageJson = objectMapper.writeValueAsString(message)
                    userSession.session.sendMessage(TextMessage(messageJson))
                    logger.debug("Sent message to user {}: {}", userId, messageJson)
                } catch (e: Exception) {
                    logger.error("Error while sending a message to user $userId", e)
                }
            }
        }
    }

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession,
        val lastPongTimestamp: Long = System.currentTimeMillis(),
    )

    companion object {
        private const val PING_INTERVAL_MS = 30_000L
        private const val PONG_TIMEOUT_MS = 60_000L
    }
}
