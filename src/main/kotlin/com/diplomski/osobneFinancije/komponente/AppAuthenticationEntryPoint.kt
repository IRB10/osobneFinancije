package com.diplomski.osobneFinancije.komponente

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AppAuthenticationEntryPoint : BasicAuthenticationEntryPoint() {
    @Throws(IOException::class, ServletException::class)
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.addHeader("WWW-Authenticate", "Basic realm=\"$realmName\"")
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.message)
    }

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        realmName = "PERSONAL FINANCE"
    }
}