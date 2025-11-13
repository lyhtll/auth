package com.example.auth.domain.auth.domain

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash(value = "blacklist", timeToLive = 1800)
data class BlacklistToken(
    @Id
    val token: String,
    val expiredAt: Long
)

