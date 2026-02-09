package de.tabmates.server.groups.api.dto.ws

enum class IncomingWebSocketMessageType {
    NEW_TAB_ENTRY,
    UPDATED_TAB_ENTRY,
}

enum class OutgoingWebSocketMessageType {
    NEW_TAB_ENTRY,
    UPDATED_TAB_ENTRY,
    TAB_ENTRY_DELETED,
    GROUP_PARTICIPANTS_CHANGED,
    ERROR,
}

data class IncomingWebSocketMessage(
    val type: IncomingWebSocketMessageType,
    val payload: String,
)

data class OutgoingWebSocketMessage(
    val type: OutgoingWebSocketMessageType,
    val payload: String,
)
