package com.example.auth.global.error

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException) =
        ErrorResponse.from(ex.error)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorMessage = ex.bindingResult.fieldErrors
            .firstOrNull()?.defaultMessage ?: "입력값이 올바르지 않습니다"

        return ResponseEntity
            .status(400)
            .body(ErrorResponse(400, errorMessage))
    }

    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception) =
        ResponseEntity.status(500).body(ErrorResponse(500, ex.message ?: "Unknown error"))
}