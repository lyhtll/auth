package com.example.auth.domain.auth.docs

import com.example.auth.domain.auth.dto.request.LoginRequest
import com.example.auth.domain.auth.dto.request.SignUpRequest
import com.example.auth.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity

@Tag(name = "Auth", description = "인증 API")
interface AuthDocs {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    fun signup(signUpRequest: SignUpRequest): ResponseEntity<BaseResponse.Empty>

    @Operation(
        summary = "로그인",
        description = """
            웹/앱에 따라 다른 방식으로 토큰 반환:
            - 웹(브라우저): Origin 헤더로 자동 인식, HTTP-Only 쿠키로 토큰 반환
            - 앱(네이티브): X-App-Secret 헤더 필수, JSON으로 토큰 반환
        """
    )
    fun login(
        loginRequest: LoginRequest,
        appSecretHeader: String?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<*>

    @Operation(
        summary = "토큰 재발급",
        description = """
            리프레시 토큰으로 새로운 액세스 토큰 발급:
            - 웹: 쿠키에서 자동 추출
            - 앱: Authorization 헤더 또는 쿠키에서 추출
        """,
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun reissue(
        appSecretHeader: String?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<*>

    @Operation(
        summary = "로그아웃",
        description = "로그아웃 처리 및 쿠키 삭제"
    )
    fun logout(response: HttpServletResponse): ResponseEntity<BaseResponse.Empty>
}