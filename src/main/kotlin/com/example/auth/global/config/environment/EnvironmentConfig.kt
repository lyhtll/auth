package com.example.auth.global.config.environment

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EnvironmentConfig(
    @Value("\${spring.profiles.active:local}") private val profile: String
) {
    fun isLocal(): Boolean = profile == "local"

    fun isProduction(): Boolean = profile == "prod" || profile == "production"

    fun getCookieSecureFlag(): Boolean = !isLocal()

    fun getProfile(): String = profile
}