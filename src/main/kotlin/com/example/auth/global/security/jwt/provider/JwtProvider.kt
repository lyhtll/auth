package com.example.auth.global.security.jwt.provider

import com.example.auth.domain.auth.domain.RefreshToken
import com.example.auth.domain.auth.repository.BlacklistTokenRepository
import com.example.auth.domain.auth.repository.RefreshTokenRepository
import com.example.auth.domain.user.domain.UserRole
import com.example.auth.global.config.properties.JwtKeyProperties
import com.example.auth.global.error.CustomException
import com.example.auth.global.security.jwt.error.JwtError
import com.example.auth.global.security.jwt.response.TokenResponse
import com.example.auth.global.security.jwt.type.TokenType
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime

@Component
class JwtProvider(
    private val jwtProperties: JwtKeyProperties,
    private val tokenRepository: RefreshTokenRepository,
    private val blacklistTokenRepository: BlacklistTokenRepository,
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder
) {

    fun generateAndSaveTokens(username: String, role: UserRole): TokenResponse {
        val accessToken = generateAccessToken(username, role)
        val refreshToken = generateRefreshToken(username)

        val refreshTokenEntity = RefreshToken(
            username = username,
            refreshToken = refreshToken,
            expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpiration / 1000)
        )

        tokenRepository.save(refreshTokenEntity)

        return TokenResponse(accessToken, refreshToken)
    }

    fun generateAccessToken(username: String, role: UserRole): String {
        val now = Instant.now()
        val validity = now.plusMillis(jwtProperties.accessTokenExpiration)

        val claims = JwtClaimsSet.builder()
            .subject(username)
            .claim("role", role.name)
            .claim("type", TokenType.ACCESS.name)
            .issuedAt(now)
            .expiresAt(validity)
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }

    fun generateRefreshToken(username: String): String {
        val now = Instant.now()
        val validity = now.plusMillis(jwtProperties.refreshTokenExpiration)

        val claims = JwtClaimsSet.builder()
            .subject(username)
            .claim("type", TokenType.REFRESH.name)
            .issuedAt(now)
            .expiresAt(validity)
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }

    fun extractUsername(token: String): String {
        return getClaims(token).subject
    }

    fun extractTokenType(token: String): TokenType {
        val typeString = getClaims(token).getClaim<String>("type")
            ?: throw CustomException(JwtError.INVALID_TOKEN)

        return try {
            TokenType.valueOf(typeString)
        } catch (_: IllegalArgumentException) {
            throw CustomException(JwtError.INVALID_TOKEN)
        }
    }

    fun extractExpiration(token: String): Instant {
        return getClaims(token).expiresAt
            ?: throw CustomException(JwtError.INVALID_TOKEN)
    }

    fun validateTokenType(token: String, expectedType: TokenType) {
        val actualType = extractTokenType(token)
        if (actualType != expectedType) {
            throw CustomException(JwtError.INVALID_TOKEN_TYPE)
        }
    }

    fun isBlacklisted(token: String): Boolean {
        return blacklistTokenRepository.existsById(token)
    }

    fun getClaims(token: String): Jwt {
        try {
            return jwtDecoder.decode(token)
        } catch (e: JwtException) {
            when (e) {
                is JwtValidationException -> {
                    if (e.errors.any { it.errorCode == "invalid_token" }) {
                        throw CustomException(JwtError.EXPIRED_TOKEN)
                    }
                }
            }
            throw CustomException(JwtError.INVALID_TOKEN)
        } catch (_: Exception) {
            throw CustomException(JwtError.INVALID_TOKEN)
        }
    }
}