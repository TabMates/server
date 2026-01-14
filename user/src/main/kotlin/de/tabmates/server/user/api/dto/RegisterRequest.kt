package de.tabmates.server.user.api.dto

import de.tabmates.server.user.api.util.Password
import de.tabmates.server.user.api.util.ValidEmail
import org.hibernate.validator.constraints.Length

data class RegisterRequest(
    @field:ValidEmail
    val email: String,
    @field:Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    val username: String,
    @field:Password
    val password: String,
)
