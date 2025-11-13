package com.example.auth.domain.user.repository

import com.example.auth.domain.user.domain.User
import com.example.auth.domain.user.error.UserError
import com.example.auth.global.error.CustomException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun existsByName(name: String): Boolean
    fun findByName(name: String): User?
}

fun UserRepository.findByNameOrThrow(username: String): User {
    return findByName(username) ?: throw CustomException(UserError.USER_NOT_FOUND)
}