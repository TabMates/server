package de.tabmates.server.user.domain.model

import de.tabmates.server.common.domain.type.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String?,
    val hasVerifiedEmail: Boolean,
    val registeredUser: Boolean,
)
