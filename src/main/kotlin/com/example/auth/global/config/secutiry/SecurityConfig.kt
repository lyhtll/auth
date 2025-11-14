package com.example.auth.global.config.secutiry

import com.example.auth.global.security.jwt.filter.JwtAuthenticationFilter
import com.example.auth.global.security.jwt.filter.JwtExceptionFilter
import com.example.auth.global.security.jwt.handler.JwtAccessDeniedHandler
import com.example.auth.global.security.jwt.handler.JwtAuthenticationEntryPoint
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtExceptionFilter: JwtExceptionFilter,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
) {
    @Value("\${security.dummy-password-hash}")
    private lateinit var dummyPasswordHash: String

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun dummyPasswordHash(): String = dummyPasswordHash

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors {}
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            }
            .authorizeHttpRequests {
                it.requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtExceptionFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .headers { headers ->
                headers.frameOptions { it.deny() }
            }

        return http.build()
    }
}