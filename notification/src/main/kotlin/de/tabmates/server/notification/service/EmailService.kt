package de.tabmates.server.notification.service

import de.tabmates.server.common.domain.type.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val templateService: EmailTemplateService,
    @param:Value("\${tabmates.email.from}")
    private val emailFrom: String,
    @param:Value("\${tabmates.email.url}")
    private val baseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendVerificationEmail(
        email: String,
        username: String,
        userId: UserId,
        token: String,
    ) {
        logger.info("Sending verification email to user $userId")

        val verificationUrl =
            UriComponentsBuilder
                .fromUriString("$baseUrl/api/auth/verify")
                .queryParam("token", token)
                .build()
                .toUriString()

        val htmlContent =
            templateService.processTemplate(
                templateName = "emails/account-verification",
                variables =
                    mapOf(
                        "username" to username,
                        "verificationUrl" to verificationUrl,
                    ),
            )

        sendHtmlEmail(
            to = email,
            subject = "Verify your account",
            htmlContent = htmlContent,
        )
    }

    fun sendPasswordResetEmail(
        email: String,
        username: String,
        userId: UserId,
        token: String,
        expiresIn: Duration,
    ) {
        logger.info("Sending password reset email for user $userId")

        val resetPasswordUrl =
            UriComponentsBuilder
                .fromUriString("$baseUrl/api/auth/reset-password")
                .queryParam("token", token)
                .build()
                .toUriString()

        val htmlContent =
            templateService.processTemplate(
                templateName = "emails/reset-password",
                variables =
                    mapOf(
                        "username" to username,
                        "resetPasswordUrl" to resetPasswordUrl,
                        "expiresInMinutes" to expiresIn.toMinutes(),
                    ),
            )

        sendHtmlEmail(
            to = email,
            subject = "Reset your password",
            htmlContent = htmlContent,
        )
    }

    private fun sendHtmlEmail(
        to: String,
        subject: String,
        htmlContent: String,
    ) {
        val message = javaMailSender.createMimeMessage()
        MimeMessageHelper(message, true, "UTF-8").apply {
            setFrom(emailFrom)
            setTo(to)
            setSubject(subject)
            setText(htmlContent, true)
        }
        try {
            javaMailSender.send(message)
        } catch (e: MailException) {
            logger.error("Failed to send email", e)
        }
    }
}
