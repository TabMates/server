package de.tabmates.server.user.domain.model

import de.tabmates.server.common.domain.type.UserId

enum class UserType {
    REGISTERED,
    ANONYMOUS,
}

data class User(
    val id: UserId,
    val username: String,
    val email: String?,
    val hasVerifiedEmail: Boolean,
    val userType: UserType,
)
