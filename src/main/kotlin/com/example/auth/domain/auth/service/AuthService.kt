package com.example.auth.domain.auth.service

import com.example.auth.domain.auth.domain.BlacklistToken
import com.example.auth.domain.auth.dto.request.LoginRequest
import com.example.auth.domain.auth.dto.request.ReissueRequest
import com.example.auth.domain.auth.dto.request.SignUpRequest
import com.example.auth.domain.auth.repository.BlacklistTokenRepository
import com.example.auth.domain.auth.repository.RefreshTokenRepository
import com.example.auth.domain.user.domain.User
import com.example.auth.domain.user.domain.UserRole
import com.example.auth.domain.user.error.UserError
import com.example.auth.domain.user.repository.UserRepository
import com.example.auth.domain.user.repository.findByNameOrThrow
import com.example.auth.global.error.CustomException
import com.example.auth.global.security.jwt.error.JwtError
import com.example.auth.global.security.jwt.provider.JwtProvider
import com.example.auth.global.security.jwt.response.TokenResponse
import com.example.auth.global.security.jwt.type.TokenType
import com.example.auth.global.security.util.SecurityUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val tokenRepository: RefreshTokenRepository,
    private val securityUtil: SecurityUtil,
    private val blacklistTokenRepository: BlacklistTokenRepository,
    private val dummyPasswordHash: String
) {


    @Transactional
     fun signup(request: SignUpRequest) : Unit {
        validateUsernameNotExists(request.name)

        val user = User(
            name = request.name,
            password = passwordEncoder.encode(request.password),
            role = UserRole.USER
        )
        userRepository.save(user)
    }

    @Transactional
     fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByName(request.name)

        if (user == null) {
            passwordEncoder.matches(request.password, dummyPasswordHash)
            throw CustomException(UserError.INVALID_CREDENTIALS)
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw CustomException(UserError.INVALID_CREDENTIALS)
        }

        return jwtProvider.generateAndSaveTokens(user.name, user.role)
    }

    @Transactional
     fun reissue(request: ReissueRequest): TokenResponse {
        val refreshToken = request.refreshToken

        jwtProvider.validateTokenType(refreshToken, TokenType.REFRESH)

        val username = jwtProvider.extractUsername(refreshToken)

        validateRefreshToken(username, refreshToken)

        val user = userRepository.findByNameOrThrow(username)
        val tokens = jwtProvider.generateAndSaveTokens(user.name, user.role)

        return tokens
    }

    @Transactional
     fun logout(): Unit {
        val username = securityUtil.getCurrentUser().name
        tokenRepository.deleteById(username)

        val accessToken = securityUtil.getCurrentAccessToken()

        val expiredAt = jwtProvider.extractExpiration(accessToken)
        blacklistTokenRepository.save(
            BlacklistToken(accessToken, expiredAt.time)
        )
        if (tokenRepository.existsById(username)) {
            throw CustomException(JwtError.TOKEN_DELETE_FAILED)
        }
    }


    private fun validateRefreshToken(username: String, refreshToken: String) {
        val savedToken = tokenRepository.findByUsername(username)
            ?: throw CustomException(JwtError.INVALID_REFRESH_TOKEN)

        // Timing Attack 방지
        if (!MessageDigest.isEqual(
                savedToken.refreshToken.toByteArray(),
                refreshToken.toByteArray()
            )) {
            throw CustomException(JwtError.INVALID_REFRESH_TOKEN)
        }
    }

    private fun validateUsernameNotExists(name: String) {
        if (userRepository.existsByName(name)) {
            throw CustomException(UserError.USERNAME_DUPLICATION)
        }
    }
}