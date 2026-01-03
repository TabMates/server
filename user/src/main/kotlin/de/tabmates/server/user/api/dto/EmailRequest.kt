package de.tabmates.server.user.api.dto

import jakarta.validation.constraints.Email

data class EmailRequest(
    @field:Email
    val email: String,
)
