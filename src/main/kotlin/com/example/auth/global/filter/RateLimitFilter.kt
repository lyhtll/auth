package com.example.auth.global.filter

import com.example.auth.global.config.properties.SecurityProperties
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitFilter(
    private val securityProperties: SecurityProperties
) : Filter {

    private val bucketCache = ConcurrentHashMap<String, Bucket>()

    private enum class RateLimitEndpoint(val path: String, val method: String) {
        LOGIN("/auth/login", "POST"),
        SIGNUP("/auth/signup", "POST"),
        REISSUE("/auth/reissue", "POST")
    }

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        val endpoint = getRateLimitEndpoint(httpRequest)

        if (endpoint != null) {
            val clientIdentifier = "${endpoint.name}:${getClientIdentifier(httpRequest)}"
            val bucket = bucketCache.computeIfAbsent(clientIdentifier) { createBucket(endpoint) }

            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response)
            } else {
                sendRateLimitResponse(httpResponse, endpoint)
            }
        } else {
            chain.doFilter(request, response)
        }
    }

    private fun getRateLimitEndpoint(request: HttpServletRequest): RateLimitEndpoint? {
        return RateLimitEndpoint.entries.firstOrNull { endpoint ->
            request.requestURI.endsWith(endpoint.path) && request.method == endpoint.method
        }
    }

    private fun createBucket(endpoint: RateLimitEndpoint): Bucket {
        val limit = when (endpoint) {
            RateLimitEndpoint.LOGIN -> securityProperties.rateLimit.login
            RateLimitEndpoint.SIGNUP -> securityProperties.rateLimit.signup
            RateLimitEndpoint.REISSUE -> securityProperties.rateLimit.reissue
        }

        val bandwidth = Bandwidth.builder()
            .capacity(limit.capacity)
            .refillIntervally(limit.refillTokens, Duration.ofSeconds(limit.refillPeriod))
            .build()

        return Bucket.builder()
            .addLimit(bandwidth)
            .build()
    }

    private fun getClientIdentifier(request: HttpServletRequest): String {
        // 1. X-Forwarded-For (프록시/로드밸런서 뒤)
        request.getHeader("X-Forwarded-For")?.let {
            if (it.isNotBlank()) {
                return it.split(",")[0].trim()
            }
        }

        // 2. X-Real-IP
        request.getHeader("X-Real-IP")?.let {
            if (it.isNotBlank()) {
                return it
            }
        }

        // 3. RemoteAddr (기본)
        return request.remoteAddr
    }

    private fun sendRateLimitResponse(response: HttpServletResponse, endpoint: RateLimitEndpoint) {
        val message = when (endpoint) {
            RateLimitEndpoint.LOGIN -> "로그인 시도 횟수를 초과했습니다. 1분 후 다시 시도해주세요."
            RateLimitEndpoint.SIGNUP -> "회원가입 시도 횟수를 초과했습니다. 5분 후 다시 시도해주세요."
            RateLimitEndpoint.REISSUE -> "토큰 갱신 시도 횟수를 초과했습니다. 1분 후 다시 시도해주세요."
        }

        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(
            """
            {
                "status": 429,
                "error": "Too Many Requests",
                "message": "$message"
            }
            """.trimIndent()
        )
    }
}

