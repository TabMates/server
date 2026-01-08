package de.tabmates.server.user.infra.database.mappers

import de.tabmates.server.user.domain.model.User
import de.tabmates.server.user.domain.model.UserType
import de.tabmates.server.user.infra.database.entities.AnonymousUserEntity
import de.tabmates.server.user.infra.database.entities.RegisteredUserEntity

fun RegisteredUserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = email,
        hasVerifiedEmail = hasVerifiedEmail,
        userType = UserType.REGISTERED,
    )
}

fun AnonymousUserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = null,
        hasVerifiedEmail = false,
        userType = UserType.ANONYMOUS,
    )
}
