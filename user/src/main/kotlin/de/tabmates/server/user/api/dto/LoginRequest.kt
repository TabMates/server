package de.tabmates.server.user.api.dto

data class LoginRequest(
    val email: String,
    val password: String,
)
