package com.example.auth.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security")
data class SecurityProperties(
    val appSecret: String,
    val dummyPasswordHash: String,
    val rateLimit: RateLimit = RateLimit()
) {
    data class RateLimit(
        val login: Limit = Limit(capacity = 5, refillTokens = 5, refillPeriod = 60),
        val signup: Limit = Limit(capacity = 3, refillTokens = 3, refillPeriod = 300),
        val reissue: Limit = Limit(capacity = 10, refillTokens = 10, refillPeriod = 60)
    )

    data class Limit(
        val capacity: Long = 5,
        val refillTokens: Long = 5,
        val refillPeriod: Long = 60
    )
}

