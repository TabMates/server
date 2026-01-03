package de.tabmates.server.user.domain.exception

import java.lang.RuntimeException

class SamePasswordException : RuntimeException(
    "The new password cannot be the same as the old password.",
)
