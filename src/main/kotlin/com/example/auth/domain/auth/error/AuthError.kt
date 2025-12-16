package com.example.auth.domain.auth.error

import com.example.auth.global.error.CustomError
import org.springframework.http.HttpStatus

enum class AuthError(
    override val message: String,
    override val status: Int
) : CustomError {
    INVALID_APP_SECRET("Invalid app secret. This endpoint requires valid app authentication.", HttpStatus.UNAUTHORIZED.value()),
    REFRESH_TOKEN_NOT_FOUND("Refresh token not found. Please provide token in cookie (web) or Authorization header (app)", HttpStatus.UNAUTHORIZED.value()),
    PASSWORD_ENCODER_ERROR("Password encoding failed. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR.value())
}


