package com.example.auth.global.security.jwt.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cookie")
data class CookieProperties(
    val accessTokenMaxAge: Long,
    val refreshTokenMaxAge: Long
)