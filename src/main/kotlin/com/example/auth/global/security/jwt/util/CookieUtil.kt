package com.example.auth.global.security.jwt.util

import com.example.auth.global.config.environment.EnvironmentConfig
import com.example.auth.global.security.jwt.properties.CookieProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

@Component
class CookieUtil(
    private val cookieProperties: CookieProperties,
    private val environmentConfig: EnvironmentConfig
) {
    companion object {
        const val ACCESS_TOKEN_COOKIE_NAME = "accessToken"
        const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
    }

    /**
     * ResponseCookie 생성 (HTTP-Only, SameSite 지원)
     */
    private fun createResponseCookie(name: String, value: String, maxAge: Long): String {
        return ResponseCookie.from(name, value)
            .httpOnly(true) // JavaScript 접근 차단
            .secure(environmentConfig.getCookieSecureFlag()) // 로컬: false, 프로덕션: true
            .path("/")
            .maxAge(maxAge)
            .sameSite(if (environmentConfig.isProduction()) "Strict" else "Lax")
            .build()
            .toString()
    }

    /**
     * Access Token 쿠키 생성
     */
    fun createAccessTokenCookie(token: String): String {
        return createResponseCookie(ACCESS_TOKEN_COOKIE_NAME, token, cookieProperties.accessTokenMaxAge)
    }

    /**
     * Refresh Token 쿠키 생성
     */
    fun createRefreshTokenCookie(token: String): String {
        return createResponseCookie(REFRESH_TOKEN_COOKIE_NAME, token, cookieProperties.refreshTokenMaxAge)
    }

    /**
     * 쿠키 삭제 (만료 시간 0으로 설정)
     */
    fun deleteCookie(name: String): String {
        return createResponseCookie(name, "", 0)
    }

    /**
     * 쿠키에서 토큰 추출
     */
    fun getTokenFromCookie(request: HttpServletRequest, cookieName: String): String? {
        return request.cookies?.firstOrNull { it.name == cookieName }?.value
    }

    /**
     * Access Token을 쿠키에서 추출
     */
    fun getAccessTokenFromCookie(request: HttpServletRequest): String? {
        return getTokenFromCookie(request, ACCESS_TOKEN_COOKIE_NAME)
    }

    /**
     * Refresh Token을 쿠키에서 추출
     */
    fun getRefreshTokenFromCookie(request: HttpServletRequest): String? {
        return getTokenFromCookie(request, REFRESH_TOKEN_COOKIE_NAME)
    }

    /**
     * 응답에 토큰 쿠키 추가 (Set-Cookie 헤더 사용)
     */
    fun addTokenCookies(response: HttpServletResponse, accessToken: String, refreshToken: String) {
        val accessCookie = createAccessTokenCookie(accessToken)
        val refreshCookie = createRefreshTokenCookie(refreshToken)

        response.addHeader("Set-Cookie", accessCookie)
        response.addHeader("Set-Cookie", refreshCookie)

    }

    /**
     * 응답에서 토큰 쿠키 삭제
     */
    fun deleteTokenCookies(response: HttpServletResponse) {
        response.addHeader("Set-Cookie", deleteCookie(ACCESS_TOKEN_COOKIE_NAME))
        response.addHeader("Set-Cookie", deleteCookie(REFRESH_TOKEN_COOKIE_NAME))
    }
}

