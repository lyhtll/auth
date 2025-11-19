package com.example.auth.global.ratelimit

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class RateLimitConfig(
    private val redisClient: RedisClient
) {
    @Bean
    fun proxyManager(): ProxyManager<String> {
        val connection: StatefulRedisConnection<String, ByteArray> =
            redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))

        return LettuceBasedProxyManager.builderFor(connection)
            .withExpirationStrategy(
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                    Duration.ofMinutes(2)
                )
            )
            .build()
    }

    companion object {
        private const val REFILL_DURATION_SECONDS = 60L

        fun createBucketConfiguration(endpoint: String): BucketConfiguration {
            val (capacity, refillTokens) = getRateLimitForEndpoint(endpoint)

            return BucketConfiguration.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(refillTokens, Duration.ofMinutes(1))
                        .build()
                )
                .build()
        }

        fun getRateLimitForEndpoint(endpoint: String): Pair<Long, Long> {
            return when {
                // 로그인/회원가입: 1분당 5개
                endpoint.startsWith("/auth/login") ||
                endpoint.startsWith("/auth/signup") -> 5L to 5L

                // 기타 인증 API: 1분당 10개
                endpoint.startsWith("/auth/") -> 10L to 10L

                // 일반 API: 1분당 100개
                else -> 100L to 100L
            }
        }

        fun getRetryAfterSeconds(): Long = REFILL_DURATION_SECONDS
    }
}