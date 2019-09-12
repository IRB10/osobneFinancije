package com.diplomski.osobneFinancije.servisi

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.repozitoriji.*
import com.diplomski.osobneFinancije.servisi.impl.KorisnikServisImpl
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.scheduler.Scheduler

@RunWith(SpringRunner::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ComponentScan(basePackages = ["com.diplomski.osobneFinancije.servisi"])
class KorisnikServisIntegrationTest {
    @Autowired
    internal var entityManager: TestEntityManager? = null

    @TestConfiguration
    internal class UserServiceImplTestContextConfiguration {
        @Autowired
        var obavijestRepozitorij: ObavijestRepozitorij? = null

        @Autowired
        var korisnikRepozitorij: KorisnikRepozitorij? = null
        @Autowired
        var ulogaRepozitorij: UlogaRepozitorij? = null
        @Autowired
        var transakcijaRepozitorij: TransakcijaRepozitorij? = null
        @Autowired
        var tokenRepozitorij: TokenRepozitorij? = null
        @Autowired
        var verifikacijskiTokenRepozitorij: VerifikacijskiTokenRepozitorij? = null
        @Autowired
        var racunRepozitorij: RacunRepozitorij? = null
        @Autowired
        var prijenosRepozitorij: PrijenosRepozitorij? = null
        @Autowired
        lateinit var obavijestiServis: ObavijestiServis
        @Autowired
        var kategorijaRepozitorij: KategorijaRepozitorij? = null
        @Autowired
        @Qualifier("messageSource")
        var poruke: MessageSource? = null
        @Autowired
        var jdbcScheduler: Scheduler? = null
        @Autowired
        var transactionTemplate: TransactionTemplate? = null

        @Bean
        fun korisnikServis(): KorisnikServis {
            return KorisnikServisImpl(
                this.korisnikRepozitorij!!,
                this.transakcijaRepozitorij!!,
                this.verifikacijskiTokenRepozitorij!!,
                this.tokenRepozitorij!!,
                this.ulogaRepozitorij!!,
                this.racunRepozitorij!!,
                this.prijenosRepozitorij!!,
                this.obavijestiServis,
                this.kategorijaRepozitorij!!,
                this.poruke!!,
                this.transactionTemplate!!,
                this.jdbcScheduler!!
            )
        }
    }

    @Autowired
    private val korisnikServis: KorisnikServis? = null

    @Autowired
    internal var userRepository: KorisnikRepozitorij? = null

    @Before
    fun init() {
    }

    @After
    fun clear() {
        entityManager!!.remove(userRepository!!.findByKorisnickoIme("alex"))
        entityManager!!.flush()
    }

    @Test
    fun promjeniLozinkuKorisnikuTest() {
        val alex = Korisnik()
        alex.korisnickoIme = "alex"
        alex.stanjeRacuna = 0.0
        alex.lozinka = BCryptPasswordEncoder().encode("easy")
        val tempPassword = BCryptPasswordEncoder().encode("easy")
        alex.ime = "Testing"
        alex.prezime = "Integration"
        alex.email = "alex.alex@gmail.com"
        alex.aktivan = true
        entityManager!!.persist<Any>(alex)

        val found = userRepository!!.findByKorisnickoIme(alex.korisnickoIme)
        korisnikServis!!.promijeniLozinkuKorisniku(found!!, "test1")
        assertNotEquals(found.lozinka, tempPassword)
        assertEquals(found.lozinka, alex.lozinka)
        assertNotNull(found.lozinka)
    }

}