package com.diplomski.osobneFinancije.kontrolori.rest

import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.apiFinances
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder

@RunWith(SpringRunner::class)
@SpringBootTest
@ContextConfiguration
@WebAppConfiguration
@WithMockUser
class FinancijeRestKontrolorIntegrationTest {
    @Autowired
    private val context: WebApplicationContext? = null

    private var mockMvc: MockMvc? = null

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mockMvc = MockMvcBuilders.webAppContextSetup(context!!).apply<DefaultMockMvcBuilder>(springSecurity()).build()
    }

    @Test
    @Throws(Exception::class)
    fun restFinancijeTest() {
        mockMvc!!.perform(get(apiFinances)).andExpect(status().isOk)
    }
}