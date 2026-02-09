package de.tabmates.server.groups.api.exceptionhandling

import de.tabmates.server.groups.domain.exception.GroupNotFoundException
import de.tabmates.server.groups.domain.exception.GroupParticipantNotFoundException
import de.tabmates.server.groups.domain.exception.TabEntryNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class TabGroupExceptionHandler {
    @ExceptionHandler(
        GroupNotFoundException::class,
        GroupParticipantNotFoundException::class,
        TabEntryNotFoundException::class,
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onNotFound(e: Exception) =
        mapOf(
            "code" to "NOT_FOUND",
            "message" to e.message,
        )
}
