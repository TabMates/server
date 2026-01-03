package de.tabmates.server.user.infra.security

import java.security.SecureRandom
import java.util.Base64

object TokenGenerator {
    fun generateSecureToken(): String {
        val bytes = ByteArray(32) { 0 }
        SecureRandom().nextBytes(bytes)
        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }
}
