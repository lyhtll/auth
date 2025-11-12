package com.example.auth.domain.user.domain

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long ?= null,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val password: String,
    @Enumerated(EnumType.STRING)
    val role: UserRole,
    val studentId: Int,
    @Column(nullable = false)
    val realName: String
    )