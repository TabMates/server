package de.tabmates.server.user.api.util

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Pattern
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
@Pattern(
    regexp = "^(?=.*[\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(.{8,40})$",
    message =
        "Password must be at least 8 characters and contain at least one digit or special character. " +
            "The maximum length is 40 characters.",
)
annotation class Password(
    val message: String =
        "Password must be at least 8 characters and contain at least one digit or special character. " +
            "The maximum length is 40 characters.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
