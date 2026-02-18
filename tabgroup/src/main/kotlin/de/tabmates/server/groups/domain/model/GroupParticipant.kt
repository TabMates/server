package de.tabmates.server.groups.domain.model

import de.tabmates.server.common.domain.type.UserId

enum class UserType {
    REGISTERED,
    ANONYMOUS,
    PLACEHOLDER,
}

data class GroupParticipant(
    val userId: UserId,
    val username: String,
    val email: String?,
    val userType: UserType,
)
