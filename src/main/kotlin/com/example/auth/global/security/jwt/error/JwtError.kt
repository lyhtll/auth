package com.example.auth.global.security.jwt.error

import com.example.auth.global.error.CustomError
import org.springframework.http.HttpStatus

enum class JwtError(
    override val status: Int,
    override val message: String
): CustomError {
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED.value(), "Expired JWT token"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value(), "Invalid JWT token"),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED.value(), "Unsupported JWT token"),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED.value(), "Malformed JWT token"),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED.value(), "Invalid token type"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED.value(), "Invalid refresh token"),
    TOKEN_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete token"),
    BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED.value(), "Blacklisted token"),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED.value(), "Token not found")
}