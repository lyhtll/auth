package com.example.auth.domain.auth.controller

import com.example.auth.domain.auth.docs.AuthDocs
import com.example.auth.domain.auth.dto.request.LoginRequest
import com.example.auth.domain.auth.dto.request.ReissueRequest
import com.example.auth.domain.auth.dto.request.SignUpRequest
import com.example.auth.domain.auth.service.AuthService
import com.example.auth.global.common.BaseResponse
import com.example.auth.global.security.jwt.util.CookieUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val cookieUtil: CookieUtil
) : AuthDocs {
    @PostMapping("/signup")
    override fun signup(
        @Valid @RequestBody signUpRequest: SignUpRequest
    ) : ResponseEntity<BaseResponse.Empty> {
        authService.signup(signUpRequest)
        return BaseResponse.success(HttpStatus.CREATED.value())
    }

    @PostMapping("/login")
    override fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<BaseResponse.Empty> {
        val tokenResponse = authService.login(loginRequest)

        // HTTP-Only 쿠키에 토큰 저장
        cookieUtil.addTokenCookies(response, tokenResponse.accessToken, tokenResponse.refreshToken)

        return BaseResponse.success(HttpStatus.OK.value())
    }

    @PostMapping("/reissue")
    override fun reissue(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<BaseResponse.Empty> {
        // 쿠키에서 Refresh Token 추출
        val refreshToken = cookieUtil.getRefreshTokenFromCookie(request)
            ?: throw IllegalArgumentException("Refresh token not found in cookie")

        val tokenResponse = authService.reissue(ReissueRequest(refreshToken))

        // 새로운 토큰을 쿠키에 저장
        cookieUtil.addTokenCookies(response, tokenResponse.accessToken, tokenResponse.refreshToken)

        return BaseResponse.success(HttpStatus.OK.value())
    }

    @PostMapping("/logout")
    override fun logout(response: HttpServletResponse): ResponseEntity<BaseResponse.Empty> {
        authService.logout()
        // 쿠키에서 토큰 삭제
        cookieUtil.deleteTokenCookies(response)
        return BaseResponse.success(HttpStatus.OK.value())
    }
}