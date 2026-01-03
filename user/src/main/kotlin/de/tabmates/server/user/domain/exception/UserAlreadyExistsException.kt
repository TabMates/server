package de.tabmates.server.user.domain.exception

import java.lang.RuntimeException

class UserAlreadyExistsException : RuntimeException(
    "A user with the given email already exists.",
)
