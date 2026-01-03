package de.tabmates.server.user.service

import de.tabmates.server.common.domain.events.user.UserEvent
import de.tabmates.server.common.domain.exception.InvalidTokenException
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.common.infra.messagequeue.EventPublisher
import de.tabmates.server.user.domain.exception.InvalidCredentialsException
import de.tabmates.server.user.domain.exception.SamePasswordException
import de.tabmates.server.user.domain.exception.UserNotFoundException
import de.tabmates.server.user.infra.database.entities.PasswordResetTokenEntity
import de.tabmates.server.user.infra.database.repositories.PasswordResetTokenRepository
import de.tabmates.server.user.infra.database.repositories.RefreshTokenRepository
import de.tabmates.server.user.infra.database.repositories.RegisteredUserRepository
import de.tabmates.server.user.infra.security.PasswordEncoder
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val registeredUserRepository: RegisteredUserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: EventPublisher,
    @param:Value("\${tabmates.email.reset-password.expiry-minutes}") private val expiryMinutes: Long,
) {
    @Transactional
    fun requestPasswordReset(email: String) {
        val user = registeredUserRepository.findByEmail(email.trim()) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val token =
            PasswordResetTokenEntity(
                user = user,
                expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES),
            )
        passwordResetTokenRepository.save(token)

        eventPublisher.publish(
            event =
                UserEvent.RequestResetPassword(
                    userId = user.id!!,
                    email = user.email,
                    username = user.username,
                    passwordResetToken = token.token,
                    expiresInMinutes = expiryMinutes,
                ),
        )
    }

    @Transactional
    fun resetPassword(
        token: String,
        newPassword: String,
    ) {
        val resetToken =
            passwordResetTokenRepository.findByToken(token)
                ?: throw InvalidTokenException("Invalid password reset token")

        if (resetToken.isUsed) {
            throw InvalidTokenException("Password reset token has already been used")
        }

        if (resetToken.isExpired) {
            throw InvalidTokenException("Password reset token has expired")
        }

        val user = resetToken.user

        if (passwordEncoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }

        val hashedNewPassword = passwordEncoder.encode(newPassword)
        registeredUserRepository.save(
            user.apply {
                this.hashedPassword = hashedNewPassword
            },
        )
        passwordResetTokenRepository.save(
            resetToken.apply {
                usedAt = Instant.now()
            },
        )

        refreshTokenRepository.deleteByUserId(user.id!!)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String,
    ) {
        val user = registeredUserRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)

        val newHashedPassword = passwordEncoder.encode(newPassword)
        registeredUserRepository.save(
            user.apply {
                this.hashedPassword = newHashedPassword
            },
        )
    }

    @Scheduled(cron = "0 0 3 * * *") // Every day at 3 AM
    fun cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(Instant.now())
    }
}
