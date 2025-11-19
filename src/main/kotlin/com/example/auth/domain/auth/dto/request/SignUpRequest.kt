package com.example.auth.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 25, message = "이름은 25자를 초과할 수 없습니다")
    val name: String,
    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 32, message = "비밀번호는 8~32자여야 합니다")
    val password: String
)