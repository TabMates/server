package de.tabmates.server.user.domain.exception

class InvalidCredentialsException : RuntimeException(
    "The entered credentials are invalid.",
)
