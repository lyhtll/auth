package com.example.auth.domain.auth.controller

import com.example.auth.domain.auth.docs.AuthDocs
import com.example.auth.domain.auth.dto.request.LoginRequest
import com.example.auth.domain.auth.dto.request.ReissueRequest
import com.example.auth.domain.auth.dto.request.SignUpRequest
import com.example.auth.domain.auth.service.AuthService
import com.example.auth.global.common.BaseResponse
import com.example.auth.global.config.properties.SecurityProperties
import com.example.auth.global.error.CustomException
import com.example.auth.global.security.jwt.response.TokenResponse
import com.example.auth.global.security.jwt.util.CookieUtil
import com.example.let_v2.domain.auth.error.AuthError
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val cookieUtil: CookieUtil,
    private val securityProperties: SecurityProperties
) : AuthDocs {

    @PostMapping("/signup")
    override fun signup(
        @Valid @RequestBody signUpRequest: SignUpRequest
    ): ResponseEntity<BaseResponse.Empty> {
        authService.signup(signUpRequest)
        return BaseResponse.success(HttpStatus.CREATED.value())
    }

    @PostMapping("/logout")
    override fun logout(response: HttpServletResponse): ResponseEntity<BaseResponse.Empty> {
        authService.logout()
        cookieUtil.deleteTokenCookies(response)
        return BaseResponse.success(HttpStatus.OK.value())
    }

    @PostMapping("/login")
    override fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        @RequestHeader("X-App-Secret", required = false) appSecretHeader: String?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        validateClientAuthentication(request, appSecretHeader)
        val tokenResponse = authService.login(loginRequest)
        return handleTokenResponse(tokenResponse, appSecretHeader, response)
    }

    @PostMapping("/reissue")
    override fun reissue(
        @RequestHeader("X-App-Secret", required = false) appSecretHeader: String?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        validateClientAuthentication(request, appSecretHeader)
        val refreshToken = extractRefreshToken(request)
        val tokenResponse = authService.reissue(ReissueRequest(refreshToken))
        return handleTokenResponse(tokenResponse, appSecretHeader, response)
    }

    private fun validateClientAuthentication(request: HttpServletRequest, appSecretHeader: String?) {
        if (appSecretHeader != null) {
            // 앱: Secret 검증
            if (appSecretHeader != securityProperties.appSecret) {
                throw CustomException(AuthError.INVALID_APP_SECRET)
            }
        }
    }

    private fun handleTokenResponse(
        tokenResponse: TokenResponse,
        appSecretHeader: String?,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        return if (appSecretHeader != null) {
            BaseResponse.of(data = tokenResponse, status = HttpStatus.OK.value())
        } else {
            cookieUtil.addTokenCookies(response, tokenResponse.accessToken, tokenResponse.refreshToken)
            BaseResponse.success(HttpStatus.OK.value())
        }
    }

    private fun extractRefreshToken(
        request: HttpServletRequest,
    ): String {
        val tokenFromCookie = cookieUtil.getRefreshTokenFromCookie(request)
        val authHeader = request.getHeader("Authorization")
        val tokenFromHeader = authHeader?.removePrefix("Bearer ")?.takeIf { it.isNotBlank() }

        return tokenFromCookie ?: tokenFromHeader ?: run {
            throw CustomException(AuthError.REFRESH_TOKEN_NOT_FOUND)
        }
    }
}