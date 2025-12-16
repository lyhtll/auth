package com.example.auth.global.config.redis

import com.example.auth.global.config.properties.RedisProperties
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableConfigurationProperties(RedisProperties::class)
class RedisConfig(
    private val redisProperties: RedisProperties
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisProperties.host, redisProperties.port)
        if (redisProperties.password.isNotBlank()) {
            config.setPassword(redisProperties.password)
        }
        return LettuceConnectionFactory(config)
    }

    @Bean
    fun redisClient(): RedisClient {
        val redisURI = RedisURI.builder()
            .withHost(redisProperties.host)
            .withPort(redisProperties.port)
            .apply {
                if (redisProperties.password.isNotBlank()) {
                    withPassword(redisProperties.password.toCharArray())
                }
            }
            .build()
        return RedisClient.create(redisURI)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory()
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
        }
    }
}