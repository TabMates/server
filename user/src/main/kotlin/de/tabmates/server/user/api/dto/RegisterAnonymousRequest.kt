package de.tabmates.server.user.api.dto

import de.tabmates.server.user.api.util.Password
import org.hibernate.validator.constraints.Length

data class RegisterAnonymousRequest(
    @field:Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    val username: String,
    @field:Password
    val password: String,
)
