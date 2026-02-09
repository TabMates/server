package de.tabmates.server.common.domain.events.user

object UserEventConstants {
    const val USER_EXCHANGE = "user.events"

    const val USER_CREATED_KEY = "user.created"
    const val USER_VERIFIED = "user.verified"
    const val USER_REQUEST_RESEND_VERIFICATION = "user.request_resend_verification"
    const val USER_REQUEST_RESET_PASSWORD = "user.request_reset_password"
    const val USER_ANONYMOUS_CREATED = "user.anonymous_created"
}
