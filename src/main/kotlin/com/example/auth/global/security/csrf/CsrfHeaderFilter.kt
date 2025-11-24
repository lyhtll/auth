package com.example.auth.global.security.csrf

import com.example.auth.domain.auth.constant.ClientType
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CsrfHeaderFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val origin = request.getHeader("Origin")

        // 앱 클라이언트(Origin 없음)는 CSRF 토큰 불필요
        if (ClientType.isApp(origin)) {
            request.setAttribute("_csrf", null)
        } else {
            // 웹(Origin 있음): CSRF 토큰을 응답 헤더로 전송
            val csrfToken = request.getAttribute(CsrfToken::class.java.name) as? CsrfToken
            csrfToken?.let {
                response.setHeader(it.headerName, it.token)
            }
        }
        filterChain.doFilter(request, response)
    }
}