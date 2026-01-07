package de.tabmates.server.user.api.mappers

import de.tabmates.server.user.api.dto.AuthenticatedUserDto
import de.tabmates.server.user.api.dto.UserDto
import de.tabmates.server.user.domain.model.AuthenticatedUser
import de.tabmates.server.user.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.toUserDto(),
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        email = email ?: "",
        username = username,
        hasVerifiedEmail = hasVerifiedEmail,
    )
}
