package com.diplomski.osobneFinancije.konfiguracija

import com.diplomski.osobneFinancije.ekstenzije.configure
import com.diplomski.osobneFinancije.komponente.AppAuthenticationEntryPoint
import com.diplomski.osobneFinancije.servisi.impl.KorisnikServisImpl
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.badUser
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.basePath
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.cssAll
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.errorPutanja
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.imgAll
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.jsAll
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.registerConfirm
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.updatePassword
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.webjarsAll
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.forgotPassword
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.login
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.login_error
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.registration
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.resetPassword
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.firewall.HttpFirewall
import org.springframework.security.web.firewall.StrictHttpFirewall


@Configuration
class WebSecurityKonfiguracija(
    private val appAuthenticationEntryPoint: AppAuthenticationEntryPoint,
    private val korisnikServis: KorisnikServisImpl
) : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) = configure(auth) {
        auth.userDetailsService(korisnikServis).passwordEncoder(BCryptPasswordEncoder())
    }

    @Throws(Exception::class)
    override fun configure(web: WebSecurity) {
        web
            .ignoring()
            .antMatchers("/").antMatchers("/resources/**").antMatchers("/static/**")
    }

    override fun configure(http: HttpSecurity) = configure(http) {
        http
            .authorizeRequests()
            .antMatchers(
                login,
                login_error,
                basePath,
                registration,
                imgAll,
                cssAll,
                webjarsAll,
                jsAll,
                forgotPassword,
                updatePassword,
                registerConfirm,
                badUser,
                errorPutanja,
                resetPassword,
                updatePassword
            )
            .permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin()
            .loginPage("/login").defaultSuccessUrl("/user/home")
            .and()
            .logout()
            .logoutUrl("/logout")
            .and()
            .csrf().disable()

        http.csrf().disable().authorizeRequests().antMatchers("/api/**").hasAnyRole("ROLE_ADMIN", "ROLE_USER").and()
            .httpBasic()
            .realmName("PERSONAL FINANCE").authenticationEntryPoint(appAuthenticationEntryPoint)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun allowUrlEncodedSlashHttpFirewall(): HttpFirewall {
        val firewall = StrictHttpFirewall()
        firewall.setAllowUrlEncodedSlash(true)
        firewall.setAllowSemicolon(true)
        return firewall
    }

    @Bean("authenticationManager")
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

}