package com.example.auth.global.security.jwt.config

import com.example.auth.global.config.properties.CorsProperties
import com.example.auth.global.security.jwt.properties.CookieProperties
import com.example.auth.global.security.jwt.properties.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class, CookieProperties::class, CorsProperties::class)
class JwtConfig