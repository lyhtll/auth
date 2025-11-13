package com.example.auth.global.security.jwt.provider

import com.example.auth.domain.auth.domain.RefreshToken
import com.example.auth.domain.auth.repository.BlacklistTokenRepository
import com.example.auth.domain.auth.repository.RefreshTokenRepository
import com.example.auth.domain.user.domain.UserRole
import com.example.auth.domain.user.error.UserError
import com.example.auth.domain.user.repository.UserCacheRepository
import com.example.auth.domain.user.repository.UserRepository
import com.example.auth.global.error.CustomException
import com.example.auth.global.security.jwt.error.JwtError
import com.example.auth.global.security.jwt.properties.JwtProperties
import com.example.auth.global.security.jwt.response.TokenResponse
import com.example.auth.global.security.jwt.type.TokenType
import com.example.auth.global.security.user.CustomUserDetails
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.Date
import org.springframework.security.core.Authentication

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
    private val userCacheRepository: UserCacheRepository,
    private val tokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val blacklistTokenRepository: BlacklistTokenRepository
) {
    private val secretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray())
    }

    private val jwtParser by lazy {
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
    }

    fun generateAndSaveTokens(username: String, role: UserRole): TokenResponse {
        val accessToken = generateAccessToken(username, role)
        val refreshToken = generateRefreshToken(username)

        // RefreshToken 엔티티로 저장
        val refreshTokenEntity = RefreshToken(
            username = username,
            refreshToken = refreshToken,
            expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpiration / 1000)
        )

        tokenRepository.save(refreshTokenEntity)

        return TokenResponse(
            accessToken,
            refreshToken
        )
    }

    fun generateAccessToken(username: String, role: UserRole): String {
        val now = Date()
        val validity = Date(now.time + jwtProperties.accessTokenExpiration)

        return Jwts.builder()
            .subject(username)
            .claim("role", role.name)
            .claim("type", TokenType.ACCESS.name)
            .issuedAt(now)
            .expiration(validity)
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(username: String): String {
        val now = Date()
        val validity = Date(now.time + jwtProperties.refreshTokenExpiration)

        return Jwts.builder()
            .subject(username)
            .claim("type", TokenType.REFRESH.name)
            .issuedAt(now)
            .expiration(validity)
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String {
        return getClaims(token).subject
    }

    fun extractRole(token: String): UserRole {
        val roleString = getClaims(token)["role"] as? String
            ?: throw CustomException(JwtError.INVALID_TOKEN)

        return try {
            UserRole.valueOf(roleString)
        } catch (e: IllegalArgumentException) {
            throw CustomException(JwtError.INVALID_TOKEN)
        }
    }

    fun extractTokenType(token: String): TokenType {
        val typeString = getClaims(token)["type"] as? String
            ?: throw CustomException(JwtError.INVALID_TOKEN)

        return try {
            TokenType.valueOf(typeString)
        } catch (e: IllegalArgumentException) {
            throw CustomException(JwtError.INVALID_TOKEN)
        }
    }

    fun extractExpiration(token: String): Date {
        return getClaims(token).expiration
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

    fun getClaims(token: String): Claims {
        try {
            return jwtParser.parseSignedClaims(token).payload
        } catch (e: ExpiredJwtException) {
            throw CustomException(JwtError.EXPIRED_TOKEN)
        } catch (e: UnsupportedJwtException) {
            throw CustomException(JwtError.UNSUPPORTED_TOKEN)
        } catch (e: MalformedJwtException) {
            throw CustomException(JwtError.MALFORMED_TOKEN)
        } catch (e: Exception) {
            throw CustomException(JwtError.INVALID_TOKEN)
        }
    }

    fun getAuthentication(token: String): Authentication{
        validateTokenType(token, TokenType.ACCESS)

        if (isBlacklisted(token)) {
            throw CustomException(JwtError.BLACKLISTED_TOKEN)
        }

        val username = extractUsername(token)
        val role = extractRole(token)

        val user = userCacheRepository.findByUsername(username)
            ?: userRepository.findByName(username)
                ?.also { userCacheRepository.save(it) }
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        val details = CustomUserDetails(user)
        return UsernamePasswordAuthenticationToken(details,null ,details.authorities)
    }

    fun extractToken(request: HttpServletRequest): String? {
        val bearToken = request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION)
        return if (bearToken?.startsWith("Bearer ") == true) {
            bearToken.substring(7)
        }else null
    }

}