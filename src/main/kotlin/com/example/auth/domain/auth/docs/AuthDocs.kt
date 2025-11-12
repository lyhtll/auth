package com.example.auth.domain.auth.docs

import com.example.auth.domain.auth.dto.request.LoginRequest
import com.example.auth.domain.auth.dto.request.ReissueRequest
import com.example.auth.domain.auth.dto.request.SignUpRequest
import com.example.auth.global.common.BaseResponse
import com.example.auth.global.security.jwt.response.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "Auth", description = "인증 API")
interface AuthDocs {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    fun signup(signUpRequest: SignUpRequest): ResponseEntity<BaseResponse.Empty>

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다")
    fun login(loginRequest: LoginRequest): ResponseEntity<BaseResponse.Success<TokenResponse>>

    @Operation(
        summary = "토큰 재발급",
        description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun reissue(reissueRequest: ReissueRequest): ResponseEntity<BaseResponse.Success<TokenResponse>>

    @Operation(
        summary = "로그아웃",
        description = "사용자를 로그아웃 처리합니다"
    )
    fun logout(): ResponseEntity<BaseResponse.Empty>
}