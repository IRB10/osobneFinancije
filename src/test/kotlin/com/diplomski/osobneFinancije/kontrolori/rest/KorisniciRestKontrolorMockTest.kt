package com.diplomski.osobneFinancije.kontrolori.rest

import com.diplomski.osobneFinancije.servisi.KorisnikServis
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.security.core.Authentication
import javax.servlet.http.HttpServletRequest


@RunWith(MockitoJUnitRunner.Silent::class)
class KorisniciRestKontrolorMockTest {
    @Mock
    internal var userService: KorisnikServis? = Mockito.mock(KorisnikServis::class.java)

    @Mock
    internal var authentication: Authentication? = null

    @InjectMocks
    private val korisniciRestKontrolor = KorisniciRestKontrolor(this.userService!!)

    @Before
    fun init() {
        authentication = Mockito.mock(Authentication::class.java)
        Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(authentication!!.name).thenReturn("irb10")
    }

    @Test
    fun handleErrorTest() {
        assertNotNull(korisniciRestKontrolor.dohvatiSveKorisnike(this.authentication!!))
    }
}