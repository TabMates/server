package de.tabmates.server.user.api.dto

import de.tabmates.server.user.api.util.ValidEmail

data class EmailRequest(
    @field:ValidEmail
    val email: String,
)
