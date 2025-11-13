package com.example.auth.domain.auth.repository

import com.example.auth.domain.auth.domain.BlacklistToken
import org.springframework.data.repository.CrudRepository

interface BlacklistTokenRepository : CrudRepository<BlacklistToken, String>

