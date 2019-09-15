package com.diplomski.osobneFinancije.kontrolori.rest

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@RunWith(SpringRunner::class)
@SpringBootTest
@ContextConfiguration
@WebAppConfiguration
@WithMockUser
class KorisniciRestKontrolorIntegrationTest {
    @Autowired
    private val context: WebApplicationContext? = null

    private var mockMvc: MockMvc? = null

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mockMvc = MockMvcBuilders.webAppContextSetup(context!!).apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity()).build()
    }

    @Test
    @Throws(Exception::class)
    fun restKorisniciTest() {
        mockMvc!!.perform(MockMvcRequestBuilders.get("/admin/users")).andExpect(
            MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun restDohvatiKorisnikeTest(){
        val rezultat = mockMvc!!.perform(
            MockMvcRequestBuilders.get("/admin/users")
                .with(user("irb10"))
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val rezultatAdmin = rezultat.response.contentAsString
        assertNotNull(rezultatAdmin)

        val rezultat1 = mockMvc!!.perform(
            MockMvcRequestBuilders.get("/admin/users")
                .with(user("antevu"))
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val rezultatNeAdmin = rezultat1.response.contentAsString
        assertNotNull(rezultatNeAdmin)
        assertEquals("[]", rezultatNeAdmin)
    }
}