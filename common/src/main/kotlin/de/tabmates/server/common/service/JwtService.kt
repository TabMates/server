package de.tabmates.server.common.service

import de.tabmates.server.common.domain.exception.InvalidTokenException
import de.tabmates.server.common.domain.type.UserId
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import kotlin.io.encoding.Base64
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Service
class JwtService(
    @param:Value("\${jwt.secret}") private val secretBase64: String,
    @param:Value("\${jwt.expiration-minutes}") private val expirationMinutes: Int,
) {
    private val secretKey =
        Keys.hmacShaKeyFor(
            Base64.Default.decode(secretBase64),
        )
    private val accessTokenValidity = expirationMinutes.minutes
    val refreshTokenValidity = 30.days

    fun generateAccessToken(userId: UserId): String {
        return generateToken(
            userId = userId,
            type = "access",
            expiry = accessTokenValidity.inWholeMilliseconds,
        )
    }

    fun generateRefreshToken(userId: UserId): String {
        return generateToken(
            userId = userId,
            type = "refresh",
            expiry = refreshTokenValidity.inWholeMilliseconds,
        )
    }

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        val expiration = claims.expiration ?: return false
        val now = Date()
        return tokenType == "access" && expiration.after(now)
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        val expiration = claims.expiration ?: return false
        val now = Date()
        return tokenType == "refresh" && expiration.after(now)
    }

    fun getUserIdFromToken(token: String): UserId {
        val claims =
            parseAllClaims(token)
                ?: throw InvalidTokenException(message = "The attached JWT token is invalid")
        val subject = claims.subject
        return try {
            UserId.fromString(subject)
        } catch (_: Exception) {
            throw InvalidTokenException(message = "The attached JWT token is invalid")
        }
    }

    private fun generateToken(
        userId: UserId,
        type: String,
        expiry: Long,
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts
            .builder()
            .subject(userId.toString())
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken =
            if (token.startsWith("Bearer ")) {
                token.removePrefix("Bearer ").trim()
            } else {
                token.trim()
            }

        return try {
            Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (_: Exception) {
            null
        }
    }
}
