package com.example.auth.global.security.util

import com.example.auth.domain.user.domain.User
import com.example.auth.domain.user.error.UserError
import com.example.auth.domain.user.repository.UserRepository
import com.example.auth.global.error.CustomException
import com.example.auth.global.security.jwt.error.JwtError
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class SecurityUtil(
    private val userRepository: UserRepository
) {
    fun getCurrentUser(): User{
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        val username = authentication.name
        return userRepository.findByName(username)
            ?: throw CustomException(UserError.USER_NOT_FOUND)
    }

    fun getCurrentAccessToken(): String {
        val request = getCurrentRequest()
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