package com.example.auth.domain.user.docs

import com.example.auth.domain.user.dto.response.GetMeResponse
import com.example.auth.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "User", description = "사용자 API")
interface UserDocs {
    @Operation(summary = "사용자 정보 조회", description = "사용자의 정보를 조회합니다.")
    fun getMe(): ResponseEntity<BaseResponse.Success<GetMeResponse>>
}