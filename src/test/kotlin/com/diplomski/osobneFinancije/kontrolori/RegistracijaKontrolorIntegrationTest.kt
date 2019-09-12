package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.repozitoriji.KorisnikRepozitorij
import com.diplomski.osobneFinancije.repozitoriji.VerifikacijskiTokenRepozitorij
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayProfile
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.loginPage
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.register
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.registration
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.ObjectUtils
import org.springframework.web.context.WebApplicationContext

@RunWith(SpringRunner::class)
@SpringBootTest
@WebAppConfiguration
class RegisterControllerTest {
    private var mockMvcBuilder: MockMvc? = null

    @Autowired
    internal var korisnikRepozitorij: KorisnikRepozitorij? = null

    @Autowired
    internal var verifikacijskiTokenRepozitorij: VerifikacijskiTokenRepozitorij? = null

    @Autowired
    private val context: WebApplicationContext? = null

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mockMvcBuilder = MockMvcBuilders.webAppContextSetup(this.context!!).build()
        val authentication = Mockito.mock(Authentication::class.java)
        val securityContext = Mockito.mock(SecurityContext::class.java)
        Mockito.`when`(securityContext.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(securityContext)
        `when`(securityContext.authentication.name).thenReturn("irb10")
    }

    @After
    fun clear() {
        if (!ObjectUtils.isEmpty(korisnikRepozitorij!!.findByKorisnickoIme("irb100"))) {
            verifikacijskiTokenRepozitorij!!.delete(verifikacijskiTokenRepozitorij!!.findByKorisnik(korisnikRepozitorij!!.findByKorisnickoIme("irb100")!!))
            korisnikRepozitorij!!.delete(korisnikRepozitorij!!.findByKorisnickoIme("irb100")!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun registrirajTest() {
        mockMvcBuilder!!.perform(
            post("/registration").param("korisnickoIme", "irb100").param("email", "ivanbebek@gmail.com").param(
                "ime",
                "Test"
            )
                .param("prezime", "User").param("lozinka", "easy").param("ponovljenaLozinka", "easy")
        )
            .andExpect(view().name(register))
        mockMvcBuilder!!.perform(
            post("/registration").param("korisnickoIme", "irb10").param("email", "ivanbebek@gmail.com").param(
                "ime",
                "Test"
            )
                .param("prezime", "User").param("lozinka", "easy").param("ponovljenaLozinka", "easy")
        ).andExpect(view().name(register))

    }

    @Test
    @Throws(Exception::class)
    fun azurirajProfilTest() {
        mockMvcBuilder!!
            .perform(
                post("/user/profile").param("ime", "Testo").param(
                    "email",
                    "ivanbebek33@gmail.com"
                ).param("prezime", "User").param("stanjeRacuna", "0.0").with(csrf())
            )
            .andExpect(status().`is`(200)).andExpect(model().errorCount<Any>(0)).andExpect(model().hasNoErrors<Any>())
            .andExpect(view().name(displayProfile))
    }

}