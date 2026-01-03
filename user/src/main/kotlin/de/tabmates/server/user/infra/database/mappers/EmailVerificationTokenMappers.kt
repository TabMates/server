package de.tabmates.server.user.infra.database.mappers

import de.tabmates.server.user.domain.model.EmailVerificationToken
import de.tabmates.server.user.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken {
    return EmailVerificationToken(
        id = id,
        token = token,
        user = user.toUser(),
    )
}
