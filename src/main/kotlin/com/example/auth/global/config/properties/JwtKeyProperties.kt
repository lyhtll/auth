package com.example.auth.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtKeyProperties(
    val publicKey: String,
    val privateKey: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long
)

