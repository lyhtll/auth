package com.example.auth.global.common

import org.springframework.http.ResponseEntity

sealed class BaseResponse(
    open val status: Int,
    open val message: String
) {
    data class Success<T>(
        override val status: Int,
        override val message: String,
        val data: T
    ) : BaseResponse(status, message)

    data class Empty(
        override val status: Int,
        override val message: String
    ) : BaseResponse(status, message)

    companion object {
        fun <T> of(
            data: T,
            status: Int = 200,
            message: String = "Success"
        ): ResponseEntity<Success<T>> {
            return ResponseEntity
                .status(status)
                .body(Success(status, message, data))
        }

        fun success(
            status: Int = 200,
            message: String = "Success"
        ): ResponseEntity<Empty> {
            return ResponseEntity
                .status(status)
                .body(Empty(status, message))
        }
    }
}
