package com.diplomski.osobneFinancije.kontrolori

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.ui.ModelMap
import javax.servlet.http.HttpServletRequest

@RunWith(MockitoJUnitRunner::class)
class ErrorKontrolorMockTest {
    @InjectMocks
    private val errorKontrolor = ErrorKontrolor()

    @Before
    fun init() {
        Mockito.mock(HttpServletRequest::class.java)
    }

    @Test
    fun handleErrorTest() {
        val model = Mockito.mock(HttpServletRequest::class.java)
        assertEquals("error", errorKontrolor.handleError(Mockito.mock<ModelMap>(ModelMap::class.java), model))
    }

}