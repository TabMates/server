package de.tabmates.server.user.domain.exception

import java.lang.RuntimeException

class EmailNotVerifiedException : RuntimeException(
    "Email is not verified",
)
