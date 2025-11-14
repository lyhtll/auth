package com.example.auth.global.config.web

import com.example.auth.global.config.properties.CorsProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    private val corsProperties: CorsProperties
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(*corsProperties.allowedOrigins.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true) // 쿠키 전송 허용 (중요!)
            .maxAge(3600)
    }
}

