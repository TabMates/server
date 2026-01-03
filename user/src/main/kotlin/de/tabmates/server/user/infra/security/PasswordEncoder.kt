package de.tabmates.server.user.infra.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordEncoder {
    private val bcrypt = BCryptPasswordEncoder()

    fun encode(rawPassword: String): String {
        return bcrypt.encode(rawPassword) ?: throw IllegalStateException("Failed to hash password")
    }

    fun matches(
        rawPassword: String,
        hashedPassword: String,
    ): Boolean {
        return bcrypt.matches(rawPassword, hashedPassword)
    }
}
