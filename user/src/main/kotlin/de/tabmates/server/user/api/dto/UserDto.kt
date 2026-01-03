package de.tabmates.server.user.api.dto

import de.tabmates.server.common.domain.type.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean,
)
