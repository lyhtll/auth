package com.example.auth.domain.auth.service

import com.example.auth.domain.auth.dto.request.LoginRequest
import com.example.auth.domain.auth.dto.request.ReissueRequest
import com.example.auth.domain.auth.dto.request.SignUpRequest
import com.example.auth.global.security.jwt.response.TokenResponse

interface AuthService {
    fun signup(request: SignUpRequest): Unit
    fun login(request: LoginRequest): TokenResponse
    fun reissue(request: ReissueRequest): TokenResponse
    fun logout(): Unit
}