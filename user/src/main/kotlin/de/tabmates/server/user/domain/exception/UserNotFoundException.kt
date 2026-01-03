package de.tabmates.server.user.domain.exception

import java.lang.RuntimeException

class UserNotFoundException : RuntimeException("User not found")
