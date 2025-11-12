package com.example.auth.domain.auth.controller

import com.example.auth.domain.auth.docs.AuthDocs
import com.example.auth.domain.auth.dto.request.LoginRequest
import com.example.auth.domain.auth.dto.request.ReissueRequest
import com.example.auth.domain.auth.dto.request.SignUpRequest
import com.example.auth.domain.auth.service.AuthService
import com.example.auth.global.common.BaseResponse
import com.example.auth.global.security.jwt.response.TokenResponse
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
) : AuthDocs {
    @PostMapping("/signup")
    override fun signup(
        @Valid @RequestBody signUpRequest: SignUpRequest
    ) : ResponseEntity<BaseResponse<Unit>>{
        authService.signup(signUpRequest)
        return BaseResponse.of(null,HttpStatus.CREATED.value())
    }

    @PostMapping("/login")
    override fun login(
        @Valid @RequestBody loginRequest: LoginRequest
    ): ResponseEntity<BaseResponse<TokenResponse>>{
        return BaseResponse.of(authService.login(loginRequest), HttpStatus.OK.value())
    }

    @PostMapping("/reissue")
    override fun reissue(
        @Valid @RequestBody reissueRequest: ReissueRequest
    ): ResponseEntity<BaseResponse<TokenResponse>>{
        return BaseResponse.of(authService.reissue(reissueRequest), HttpStatus.OK.value())
    }

    @PostMapping("/logout")
    override fun logout(): ResponseEntity<BaseResponse<Unit>>{
        authService.logout()
        return BaseResponse.of(null, HttpStatus.OK.value())
    }
}