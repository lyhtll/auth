package com.example.auth.global.security.jwt.properties

import com.example.auth.global.config.properties.CorsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class, CookieProperties::class, CorsProperties::class)
class JwtConfig
