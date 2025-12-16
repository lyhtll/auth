package com.example.auth.global.security.util

import com.example.auth.domain.user.domain.User
import com.example.auth.domain.user.error.UserError
import com.example.auth.domain.user.repository.UserRepository
import com.example.auth.domain.user.repository.findByNameOrThrow
import com.example.auth.global.error.CustomException
import com.example.auth.global.security.jwt.error.JwtError
import com.example.auth.global.security.jwt.util.CookieUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class SecurityUtil(
    private val userRepository: UserRepository,
    private val cookieUtil: CookieUtil
) {
    fun getCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        val username = when (val principal = authentication.principal) {
            is Jwt -> principal.subject
            is String -> principal
            else -> authentication.name
        } ?: throw CustomException(UserError.USER_NOT_FOUND)

        return userRepository.findByNameOrThrow(username)
    }

    fun getCurrentAccessToken(): String {
        val request = getCurrentRequest()

        // 1. 쿠키에서 토큰 추출 시도
        cookieUtil.getAccessTokenFromCookie(request)?.let { return it }

        // 2. Authorization 헤더에서 추출
        val authHeader = request.getHeader("Authorization")
            ?: throw CustomException(JwtError.TOKEN_NOT_FOUND)

        if (!authHeader.startsWith("Bearer ")) {
            throw CustomException(JwtError.TOKEN_NOT_FOUND)
        }

        return authHeader.substring(7)
    }

    private fun getCurrentRequest(): HttpServletRequest {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw CustomException(JwtError.TOKEN_NOT_FOUND)
        return requestAttributes.request
    }
}