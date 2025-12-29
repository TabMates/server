package de.tabmates.server.common.domain.exception

class InvalidTokenException(override val message: String?) : RuntimeException(message ?: "Invalid token")
