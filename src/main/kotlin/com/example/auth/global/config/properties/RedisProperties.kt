package com.example.auth.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisProperties(
    val host: String,
    val port: Int,
    val password: String
)

