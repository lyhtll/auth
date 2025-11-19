package com.example.auth.domain.user.dto.response

import com.example.auth.domain.user.domain.User
import com.example.auth.domain.user.domain.UserRole

data class GetMeResponse(
    val id: Long,
    val name: String,
    val role: UserRole
){
    companion object {
        fun of(user: User): GetMeResponse {
            return GetMeResponse(
                id = user.id!!,
                name = user.name,
                role = user.role
            )
        }
    }
}
