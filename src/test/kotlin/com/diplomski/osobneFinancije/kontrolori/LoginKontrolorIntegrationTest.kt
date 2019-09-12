package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.login
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import javax.servlet.Filter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginKontrolorIntegrationTest {
    private var mockMvcBuilder: MockMvc? = null

    @Autowired
    private val context: WebApplicationContext? = null

    @Autowired
    private val springSecurityFilterChain: Filter? = null

    @BeforeEach
    fun init() {
        MockitoAnnotations.initMocks(this)
        mockMvcBuilder = MockMvcBuilders.webAppContextSetup(this.context!!)
            .addFilters<DefaultMockMvcBuilder>(this.springSecurityFilterChain!!).build()
    }

    @Test
    @Throws(Exception::class)
    fun loginTest() {
        mockMvcBuilder!!.perform(get(login)).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun prijavaKoristeciCsrfTest() {
        mockMvcBuilder!!.perform(get("/login").with(csrf().asHeader())).andExpect(status().isOk)

    }

    @Test
    @Throws(Exception::class)
    fun prijavaSPogresnimPodacimaTest() {
        mockMvcBuilder!!.perform(post("/login").param("username", "test").param("password", "easy"))
            .andExpect(status().isFound)
            .andExpect(header().string("Location", "/login?error"))
    }

    @Test
    @Throws(Exception::class)
    fun odjavaKorisnikaTest() {
        mockMvcBuilder!!.perform(logout()).andExpect(status().isFound).andExpect(header().string("Location", "/login?logout"))
    }
}

