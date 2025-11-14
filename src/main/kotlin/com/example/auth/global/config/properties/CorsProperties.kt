package com.example.auth.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    val allowedOrigins: List<String>
)