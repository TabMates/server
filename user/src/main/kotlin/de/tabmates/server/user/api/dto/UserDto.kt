package de.tabmates.server.user.api.dto

import de.tabmates.server.common.domain.type.UserId

enum class UserTypeDto {
    REGISTERED,
    ANONYMOUS,
}

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean,
    val userType: UserTypeDto,
)
