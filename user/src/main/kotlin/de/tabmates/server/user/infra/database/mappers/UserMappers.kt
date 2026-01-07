package de.tabmates.server.user.infra.database.mappers

import de.tabmates.server.user.domain.model.User
import de.tabmates.server.user.infra.database.entities.RegisteredUserEntity
import de.tabmates.server.user.infra.database.entities.TemporaryUserEntity

fun RegisteredUserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = email,
        hasVerifiedEmail = hasVerifiedEmail,
        registeredUser = true,
    )
}

fun TemporaryUserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = null,
        hasVerifiedEmail = false,
        registeredUser = false,
    )
}
