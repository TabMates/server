package de.tabmates.server.user.service

import de.tabmates.server.common.domain.events.user.UserEvent
import de.tabmates.server.common.domain.exception.InvalidTokenException
import de.tabmates.server.common.infra.messagequeue.EventPublisher
import de.tabmates.server.user.domain.exception.UserNotFoundException
import de.tabmates.server.user.domain.model.EmailVerificationToken
import de.tabmates.server.user.infra.database.entities.EmailVerificationTokenEntity
import de.tabmates.server.user.infra.database.mappers.toEmailVerificationToken
import de.tabmates.server.user.infra.database.repositories.EmailVerificationTokenRepository
import de.tabmates.server.user.infra.database.repositories.RegisteredUserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val registeredUserRepository: RegisteredUserRepository,
    private val eventPublisher: EventPublisher,
    @param:Value("\${tabmates.email.verification.expiry-hours}") private val expiryHours: Long,
) {
    @Transactional
    fun resendVerificationEmail(email: String) {
        val token = createVerificationToken(email)

        if (token.user.hasVerifiedEmail) {
            return
        }

        eventPublisher.publish(
            event =
                UserEvent.RequestResendVerification(
                    userId = token.user.id,
                    email = token.user.email!!,
                    username = token.user.username!!,
                    verificationToken = token.token,
                ),
        )
    }

    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity =
            registeredUserRepository.findByEmail(email)
                ?: throw UserNotFoundException()

        emailVerificationTokenRepository.invalidateActiveTokensForUser(userEntity)

        val token =
            EmailVerificationTokenEntity(
                expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
                user = userEntity,
            )

        return emailVerificationTokenRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken =
            emailVerificationTokenRepository.findByToken(token)
                ?: throw InvalidTokenException("Email verification token is invalid")

        if (verificationToken.isUsed) {
            throw InvalidTokenException("Email verification token has already been used")
        }

        if (verificationToken.isExpired) {
            throw InvalidTokenException("Email verification token has expired")
        }

        emailVerificationTokenRepository.save(
            verificationToken.apply {
                usedAt = Instant.now()
            },
        )

        registeredUserRepository.save(
            verificationToken.user.apply {
                this.hasVerifiedEmail = true
            },
        )

        eventPublisher.publish(
            event =
                UserEvent.Verified(
                    userId = verificationToken.user.id!!,
                    email = verificationToken.user.email,
                    username = verificationToken.user.username,
                ),
        )
    }

    @Scheduled(cron = "0 0 3 * * *") // Every day at 3 AM
    fun cleanupExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(Instant.now())
    }
}
