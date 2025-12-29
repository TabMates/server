package de.tabmates.server.common.api.exceptionhandling

import de.tabmates.server.common.domain.exception.ForbiddenException
import de.tabmates.server.common.domain.exception.InvalidTokenException
import de.tabmates.server.common.domain.exception.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CommonExceptionHandler {
    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun onForbidden(e: ForbiddenException) =
        mapOf(
            "code" to "FORBIDDEN",
            "message" to e.message,
        )

    @ExceptionHandler(InvalidTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onInvalidToken(e: InvalidTokenException) =
        mapOf(
            "code" to "INVALID_TOKEN",
            "message" to e.message,
        )

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onForbidden(e: UnauthorizedException) =
        mapOf(
            "code" to "UNAUTHORIZED",
            "message" to e.message,
        )
}
