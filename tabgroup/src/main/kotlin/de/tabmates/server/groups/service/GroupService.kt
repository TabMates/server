package de.tabmates.server.groups.service

import de.tabmates.server.common.domain.exception.ForbiddenException
import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.api.dto.TabEntryDto
import de.tabmates.server.groups.api.mappers.toTabEntryDto
import de.tabmates.server.groups.domain.event.GroupCreatedEvent
import de.tabmates.server.groups.domain.event.GroupDeletedEvent
import de.tabmates.server.groups.domain.event.GroupParticipantJoinedEvent
import de.tabmates.server.groups.domain.event.GroupParticipantLeftEvent
import de.tabmates.server.groups.domain.exception.GroupNotFoundException
import de.tabmates.server.groups.domain.exception.GroupParticipantNotFoundException
import de.tabmates.server.groups.domain.model.Group
import de.tabmates.server.groups.domain.model.TabEntry
import de.tabmates.server.groups.infra.database.entities.GroupEntity
import de.tabmates.server.groups.infra.database.entities.GroupParticipantEntity
import de.tabmates.server.groups.infra.database.entities.UserTypeDatabase
import de.tabmates.server.groups.infra.database.mappers.toGroup
import de.tabmates.server.groups.infra.database.mappers.toTabEntry
import de.tabmates.server.groups.infra.database.repositories.GroupParticipantRepository
import de.tabmates.server.groups.infra.database.repositories.GroupRepository
import de.tabmates.server.groups.infra.database.repositories.TabEntryRepository
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupParticipantRepository: GroupParticipantRepository,
    private val tabEntryRepository: TabEntryRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun getGroupById(
        groupId: GroupId,
        requestedUserId: UserId,
    ): Group? {
        return groupRepository
            .findGroupById(groupId, requestedUserId)
            ?.toGroup(lastTabEntryForGroup(groupId))
    }

    fun findGroupsByUser(userId: UserId): List<Group> {
        val groupEntities = groupRepository.findAllByUserId(userId)
        val groupIds = groupEntities.mapNotNull { it.id }
        val latestTabEntries =
            tabEntryRepository
                .findLatestTabEntryByGroupIds(groupIds.toSet())
                .associateBy { it.groupId }

        return groupEntities
            .map { it.toGroup(lastTabEntry = latestTabEntries[it.id]?.toTabEntry()) }
            .sortedByDescending { it.lastActivityAt }
    }

    @Transactional
    fun createGroup(
        creatorId: UserId,
        participantsUserIds: Set<UserId>,
    ): Group {
        val otherParticipants = groupParticipantRepository.findByUserIdIn(participantsUserIds)

        val creator =
            groupParticipantRepository.findByIdOrNull(creatorId)
                ?: throw GroupParticipantNotFoundException(creatorId)

        return groupRepository
            .save(
                GroupEntity(
                    creator = creator,
                    participants = setOf(creator) + otherParticipants,
                ),
            ).toGroup(lastTabEntry = null)
            .also { entity ->
                applicationEventPublisher.publishEvent(
                    GroupCreatedEvent(
                        groupId = entity.id,
                        participantIds = entity.participants.map { it.userId }.toSet(),
                    ),
                )
            }
    }

    @Transactional
    fun addParticipantsToGroup(
        requestedUserId: UserId,
        groupId: GroupId,
        userIds: Set<UserId>,
    ): Group {
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw GroupNotFoundException()

        val isRequestingUserParticipantInGroup = group.participants.any { it.userId == requestedUserId }
        if (!isRequestingUserParticipantInGroup) {
            throw ForbiddenException()
        }

        val users =
            userIds.map { userId ->
                groupParticipantRepository.findByIdOrNull(userId)
                    ?: throw GroupParticipantNotFoundException(userId)
            }

        val lastTabEntry = lastTabEntryForGroup(groupId)
        val updatedGroup =
            groupRepository
                .save(
                    group.apply {
                        this.participants = group.participants + users
                    },
                ).toGroup(lastTabEntry)

        applicationEventPublisher.publishEvent(
            GroupParticipantJoinedEvent(
                groupId = groupId,
                userIds = userIds,
            ),
        )

        return updatedGroup
    }

    @Transactional
    fun addNewParticipantsToGroup(
        requestedUserId: UserId,
        groupId: GroupId,
        usernames: Set<String>,
    ): Group {
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw GroupNotFoundException()

        val isRequestingUserParticipantInGroup = group.participants.any { it.userId == requestedUserId }
        if (!isRequestingUserParticipantInGroup) {
            throw ForbiddenException()
        }

        val newUsers =
            usernames.map { username ->
                GroupParticipantEntity(
                    userId = UUID.randomUUID(),
                    username = username,
                    email = null,
                    userType = UserTypeDatabase.PLACEHOLDER,
                )
            }
        groupParticipantRepository.saveAll(newUsers)

        return addParticipantsToGroup(
            requestedUserId = requestedUserId,
            groupId = groupId,
            userIds = newUsers.map { it.userId }.toSet(),
        )
    }

    @Transactional
    fun removeParticipantFromGroup(
        groupId: GroupId,
        userId: UserId,
    ) {
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw GroupNotFoundException()

        val participantToRemove =
            group.participants.firstOrNull { it.userId == userId }
                ?: throw GroupParticipantNotFoundException(userId)

        val newParticipantsSize = group.participants.size - 1
        if (newParticipantsSize == 0) {
            groupRepository.deleteById(groupId)
            applicationEventPublisher.publishEvent(
                GroupDeletedEvent(
                    groupId = groupId,
                ),
            )
            return
        }

        groupRepository.save(
            group.apply {
                this.participants = group.participants - participantToRemove
            },
        )

        applicationEventPublisher.publishEvent(
            GroupParticipantLeftEvent(
                groupId = groupId,
                userId = userId,
            ),
        )
    }

    @Cacheable(
        value = ["tabEntries"],
        key = "#groupId",
        condition = "#before == null && #pageSize <= 50",
        sync = true,
    )
    fun getTabEntries(
        groupId: GroupId,
        before: Instant?,
        pageSize: Int,
    ): List<TabEntryDto> {
        return tabEntryRepository
            .findByTabEntryIdBefore(
                groupId = groupId,
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize),
            ).content
            .map { it.toTabEntry().toTabEntryDto() }
    }

    private fun lastTabEntryForGroup(groupId: GroupId): TabEntry? {
        return tabEntryRepository
            .findLatestTabEntryByGroupIds(setOf(groupId))
            .firstOrNull()
            ?.toTabEntry()
    }
}
