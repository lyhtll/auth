package com.example.auth.global.interceptor

import com.example.auth.global.ratelimit.RateLimitConfig
import com.example.auth.global.error.ErrorResponse
import com.example.auth.global.ratelimit.RateLimitError
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.bucket4j.distributed.proxy.ProxyManager
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RateLimitInterceptor(
    private val proxyManager: ProxyManager<String>,
    private val objectMapper: ObjectMapper
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val key = getClientKey(request)
        val endpoint = request.requestURI

        val bucket = proxyManager.builder()
            .build(key) { RateLimitConfig.createBucketConfiguration(endpoint) }

        val probe = bucket.tryConsumeAndReturnRemaining(1)

        val (capacity, _) = RateLimitConfig.getRateLimitForEndpoint(endpoint)
        response.addHeader("X-Rate-Limit-Limit", capacity.toString())
        response.addHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())

        if (probe.isConsumed) {
            return true
        }

        // Rate limit 초과 시
        val retryAfter = RateLimitConfig.getRetryAfterSeconds()
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = "application/json; charset=UTF-8"
        response.addHeader("Retry-After", retryAfter.toString())

        val errorResponse = ErrorResponse(
            status = RateLimitError.RATE_LIMIT_EXCEEDED.status,
            message = RateLimitError.RATE_LIMIT_EXCEEDED.message
        )
        response.writer.write(objectMapper.writeValueAsString(errorResponse))

        return false
    }

    private fun getClientKey(request: HttpServletRequest): String {
        val ip = request.getHeader("X-Forwarded-For")
            ?: request.remoteAddr
        val path = request.requestURI
        return "rate_limit:$ip:$path"
    }
}



