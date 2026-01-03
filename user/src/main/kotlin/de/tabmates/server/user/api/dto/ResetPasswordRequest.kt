package de.tabmates.server.user.api.dto

import de.tabmates.server.user.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest(
    @field:NotBlank
    val token: String,
    @field:Password
    val newPassword: String,
)
