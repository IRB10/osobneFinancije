package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Korisnik
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.AfterEach
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class KorisnikRepozitorijIntegrationTest {
    @Autowired
    private val entityManager: TestEntityManager? = null

    @Autowired
    private val korisnikRepozitorij: KorisnikRepozitorij? = null

    @Before
    fun init() {
    }

    @AfterEach
    fun clear() {
        entityManager!!.remove(korisnikRepozitorij!!.findByKorisnickoIme("alex"))
        entityManager.flush()
    }

    @Test
    fun pronadiPoKorisnickomImenuTest() {
        val alex = Korisnik()
        alex.korisnickoIme = "alex"
        alex.stanjeRacuna = 0.0
        alex.ime = "Testing"
        alex.prezime = "Integration"
        alex.email = "alex.alex@gmail.com"
        alex.aktivan = true
        entityManager!!.persist<Any>(alex)

        val found = korisnikRepozitorij!!.findByKorisnickoIme(alex.korisnickoIme)
        assertEquals(found!!.korisnickoIme, alex.korisnickoIme)
        assertNotNull(found.korisnickoIme)
    }

    @Test
    fun pronadiPoEmailuTest() {
        val alexMail = Korisnik()
        alexMail.korisnickoIme = "alex"
        alexMail.stanjeRacuna = 0.0
        alexMail.ime = "Testing"
        alexMail.prezime = "Integration"
        alexMail.email = "alex.alex@gmail.com"
        alexMail.aktivan = true
        entityManager!!.persist<Any>(alexMail)

        val emailFound = korisnikRepozitorij!!.findByEmail(alexMail.email)
        assertEquals(emailFound!!.email, alexMail.email)
        assertNotNull(emailFound.email)
    }

}