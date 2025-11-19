package com.example.auth.global.ratelimit

import com.example.auth.global.error.CustomError
import org.springframework.http.HttpStatus

enum class RateLimitError(
    override val message: String,
    override val status: Int
) : CustomError {
    RATE_LIMIT_EXCEEDED("Rate limit exceeded. Please try again later.", HttpStatus.TOO_MANY_REQUESTS.value())
}