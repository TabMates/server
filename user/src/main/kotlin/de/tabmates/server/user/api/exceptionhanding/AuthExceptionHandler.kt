package de.tabmates.server.user.api.exceptionhanding

import de.tabmates.server.user.domain.exception.EmailNotVerifiedException
import de.tabmates.server.user.domain.exception.InvalidCredentialsException
import de.tabmates.server.user.domain.exception.RateLimitException
import de.tabmates.server.user.domain.exception.SamePasswordException
import de.tabmates.server.user.domain.exception.UserAlreadyExistsException
import de.tabmates.server.user.domain.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onUserAlreadyExists(e: UserAlreadyExistsException) =
        mapOf(
            "code" to "USER_ALREADY_EXISTS",
            "message" to e.message,
        )

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onUserNotFound(e: UserNotFoundException) =
        mapOf(
            "code" to "USER_NOT_FOUND",
            "message" to e.message,
        )

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onInvalidCredentials(e: InvalidCredentialsException) =
        mapOf(
            "code" to "INVALID_CREDENTIALS",
            "message" to e.message,
        )

    @ExceptionHandler(EmailNotVerifiedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun onEmailNotVerified(e: EmailNotVerifiedException) =
        mapOf(
            "code" to "EMAIL_NOT_VERIFIED",
            "message" to e.message,
        )

    @ExceptionHandler(SamePasswordException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onSamePassword(e: SamePasswordException) =
        mapOf(
            "code" to "SAME_PASSWORD",
            "message" to e.message,
        )

    @ExceptionHandler(RateLimitException::class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    fun onRateLimitExceeded(e: RateLimitException) =
        mapOf(
            "code" to "RATE_LIMIT_EXCEEDED",
            "message" to e.message,
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors =
            e.bindingResult.allErrors.map {
                it.defaultMessage ?: "Invalid value"
            }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    "code" to "VALIDATION_FAILED",
                    "errors" to errors,
                ),
            )
    }
}
