package de.tabmates.server.common.api.util

import de.tabmates.server.common.domain.exception.UnauthorizedException
import de.tabmates.server.common.domain.type.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestedUserId: UserId
    get() =
        SecurityContextHolder.getContext().authentication?.principal as? UserId
            ?: throw UnauthorizedException()
