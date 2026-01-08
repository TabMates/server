package de.tabmates.server.user.api.dto

import de.tabmates.server.common.domain.type.UserId

data class LoginAnonymousRequest(
    val userId: UserId,
    val password: String,
)
