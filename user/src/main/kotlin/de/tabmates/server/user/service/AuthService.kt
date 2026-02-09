package de.tabmates.server.user.service

import de.tabmates.server.common.domain.events.user.UserEvent
import de.tabmates.server.common.domain.exception.InvalidTokenException
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.common.infra.messagequeue.EventPublisher
import de.tabmates.server.common.service.JwtService
import de.tabmates.server.user.domain.exception.EmailNotVerifiedException
import de.tabmates.server.user.domain.exception.InvalidCredentialsException
import de.tabmates.server.user.domain.exception.UserAlreadyExistsException
import de.tabmates.server.user.domain.exception.UserNotFoundException
import de.tabmates.server.user.domain.model.AuthenticatedUser
import de.tabmates.server.user.domain.model.User
import de.tabmates.server.user.infra.database.entities.AnonymousUserEntity
import de.tabmates.server.user.infra.database.entities.RefreshTokenEntity
import de.tabmates.server.user.infra.database.entities.RegisteredUserEntity
import de.tabmates.server.user.infra.database.mappers.toUser
import de.tabmates.server.user.infra.database.repositories.AnonymousUserRepository
import de.tabmates.server.user.infra.database.repositories.RefreshTokenRepository
import de.tabmates.server.user.infra.database.repositories.RegisteredUserRepository
import de.tabmates.server.user.infra.security.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val registeredUserRepository: RegisteredUserRepository,
    private val anonymousUserRepository: AnonymousUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService,
    private val eventPublisher: EventPublisher,
) {
    @Transactional
    fun register(
        email: String,
        username: String,
        password: String,
    ): User {
        val trimmedEmail = email.trim()
        val user =
            registeredUserRepository.findByEmailOrUsername(
                email = trimmedEmail,
                username = username.trim(),
            )
        if (user != null) {
            throw UserAlreadyExistsException()
        }

        val savedUser =
            registeredUserRepository
                .saveAndFlush(
                    RegisteredUserEntity(
                        email = trimmedEmail,
                        username = username.trim(),
                        hashedPassword = passwordEncoder.encode(password),
                    ),
                ).toUser()

        val emailToken = emailVerificationService.createVerificationToken(trimmedEmail)

        eventPublisher.publish(
            event =
                UserEvent.Created(
                    userId = savedUser.id,
                    email = savedUser.email!!,
                    username = savedUser.username,
                    verificationToken = emailToken.token,
                ),
        )

        return savedUser
    }

    @Transactional
    fun registerAnonymous(
        username: String,
        password: String,
    ): User {
        val savedUser =
            anonymousUserRepository
                .saveAndFlush(
                    AnonymousUserEntity(
                        username = username.trim(),
                        hashedPassword = passwordEncoder.encode(password),
                    ),
                ).toUser()

        eventPublisher.publish(
            event =
                UserEvent.AnonymousUserCreated(
                    userId = savedUser.id,
                    username = savedUser.username,
                ),
        )

        return savedUser
    }

    fun login(
        email: String,
        password: String,
    ): AuthenticatedUser {
        val user =
            registeredUserRepository.findByEmail(email.trim())
                ?: throw InvalidCredentialsException()

        val passwordMatches =
            passwordEncoder.matches(
                rawPassword = password,
                hashedPassword = user.hashedPassword,
            )
        if (!passwordMatches) {
            throw InvalidCredentialsException()
        }

        if (!user.hasVerifiedEmail) {
            throw EmailNotVerifiedException()
        }

        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, refreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        } ?: throw UserNotFoundException()
    }

    fun loginAnonymous(
        userId: UserId,
        password: String,
    ): AuthenticatedUser {
        val user =
            anonymousUserRepository.findByIdOrNull(userId)
                ?: throw InvalidCredentialsException()

        val passwordMatches =
            passwordEncoder.matches(
                rawPassword = password,
                hashedPassword = user.hashedPassword,
            )
        if (!passwordMatches) {
            throw InvalidCredentialsException()
        }

        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, refreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException(message = "Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user =
            registeredUserRepository.findByIdOrNull(userId)?.toUser()
                ?: anonymousUserRepository.findByIdOrNull(userId)?.toUser()
                ?: throw UserNotFoundException()
        val hashed = hashToken(refreshToken)

        refreshTokenRepository.findByUserIdAndHashedToken(
            userId = userId,
            hashedToken = hashed,
        ) ?: throw InvalidTokenException(message = "Invalid refresh token")

        refreshTokenRepository.deleteByUserIdAndHashedToken(
            userId = userId,
            hashedToken = hashed,
        )

        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)

        storeRefreshToken(userId, newRefreshToken)

        return AuthenticatedUser(
            user = user,
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
        )
    }

    @Transactional
    fun logout(refreshToken: String) {
        val userId = jwtService.getUserIdFromToken(refreshToken)
        val hashed = hashToken(refreshToken)

        refreshTokenRepository.deleteByUserIdAndHashedToken(
            userId = userId,
            hashedToken = hashed,
        )
    }

    private fun storeRefreshToken(
        userId: UserId,
        token: String,
    ) {
        val hashedToken = hashToken(token)
        val expiresAt = Instant.now().plusMillis(jwtService.refreshTokenValidity.inWholeMilliseconds)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashedToken,
            ),
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
