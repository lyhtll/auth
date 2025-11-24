package com.example.auth.domain.auth.constant

object ClientType {
    /**
     * Origin 헤더가 있으면 웹(브라우저)
     * Origin 헤더가 없으면 앱(네이티브)
     */
    fun isWeb(origin: String?): Boolean {
        return !origin.isNullOrBlank()
    }

    fun isApp(origin: String?): Boolean {
        return origin.isNullOrBlank()
    }
}