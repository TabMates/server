package de.tabmates.server.user.api.controllers

import de.tabmates.server.common.api.util.requestedUserId
import de.tabmates.server.user.api.config.IpRateLimit
import de.tabmates.server.user.api.dto.AuthenticatedUserDto
import de.tabmates.server.user.api.dto.ChangePasswordRequest
import de.tabmates.server.user.api.dto.EmailRequest
import de.tabmates.server.user.api.dto.LoginAnonymousRequest
import de.tabmates.server.user.api.dto.LoginRequest
import de.tabmates.server.user.api.dto.RefreshRequest
import de.tabmates.server.user.api.dto.RegisterAnonymousRequest
import de.tabmates.server.user.api.dto.RegisterRequest
import de.tabmates.server.user.api.dto.ResetPasswordRequest
import de.tabmates.server.user.api.dto.UserDto
import de.tabmates.server.user.api.mappers.toAuthenticatedUserDto
import de.tabmates.server.user.api.mappers.toUserDto
import de.tabmates.server.user.infra.ratelimiting.EmailRateLimiter
import de.tabmates.server.user.service.AuthService
import de.tabmates.server.user.service.EmailVerificationService
import de.tabmates.server.user.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter,
) {
    @PostMapping("/register")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
    fun register(
        @Valid @RequestBody body: RegisterRequest,
    ): UserDto {
        return authService
            .register(
                email = body.email,
                username = body.username,
                password = body.password,
            ).toUserDto()
    }

    @PostMapping("/register-anonymous")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
    fun registerAnonymous(
        @Valid @RequestBody body: RegisterAnonymousRequest,
    ): UserDto {
        return authService
            .registerAnonymous(
                username = body.username,
                password = body.password,
            ).toUserDto()
    }

    @PostMapping("/login")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
    fun login(
        @RequestBody body: LoginRequest,
    ): AuthenticatedUserDto {
        return authService
            .login(
                email = body.email,
                password = body.password,
            ).toAuthenticatedUserDto()
    }

    @PostMapping("/login-anonymous")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
    fun loginAnonymous(
        @RequestBody body: LoginAnonymousRequest,
    ): AuthenticatedUserDto {
        return authService
            .loginAnonymous(
                userId = body.userId,
                password = body.password,
            ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
    fun refreshToken(
        @RequestBody body: RefreshRequest,
    ): AuthenticatedUserDto {
        return authService
            .refresh(refreshToken = body.refreshToken)
            .toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    fun logout(
        @RequestBody body: RefreshRequest,
    ) {
        authService.logout(refreshToken = body.refreshToken)
    }

    @PostMapping("/resend-verification")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
    fun resendVerification(
        @Valid @RequestBody body: EmailRequest,
    ) {
        emailRateLimiter.withRateLimit(
            email = body.email,
        ) {
            emailVerificationService.resendVerificationEmail(email = body.email)
        }
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String,
    ) {
        emailVerificationService.verifyEmail(token)
    }

    @PostMapping("/forgot-password")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
    fun forgotPassword(
        @Valid @RequestBody body: EmailRequest,
    ) {
        passwordResetService.requestPasswordReset(
            email = body.email,
        )
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest,
    ) {
        passwordResetService.resetPassword(
            token = body.token,
            newPassword = body.newPassword,
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest,
    ) {
        passwordResetService.changePassword(
            userId = requestedUserId,
            oldPassword = body.oldPassword,
            newPassword = body.newPassword,
        )
    }
}
