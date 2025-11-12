package com.example.auth.domain.auth.repository

import com.example.auth.domain.auth.domain.RefreshToken
import org.springframework.data.repository.CrudRepository

interface RefreshTokenRepository : CrudRepository<RefreshToken, String> {
    fun findByUsername(username: String): RefreshToken?
    fun deleteByUsername(username: String)
}