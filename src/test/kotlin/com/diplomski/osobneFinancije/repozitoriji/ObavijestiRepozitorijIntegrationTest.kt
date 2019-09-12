package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Obavijest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ObavijestiRepozitorijIntegrationTest {
    @Autowired
    private val entityManager: TestEntityManager? = null

    @Autowired
    private val obavijestRepozitorij: ObavijestRepozitorij? = null

    @Autowired
    private val korisnikRepozitorij: KorisnikRepozitorij? = null

    @Before
    fun init() {
        var korisnik = Korisnik()
        korisnik.korisnickoIme = "alex"
        korisnik.email = "alex@alex.com"
        korisnikRepozitorij?.save(korisnik)
    }

    @After
    fun clear() {
        val user = korisnikRepozitorij!!.findByKorisnickoIme("alex")
        entityManager!!.remove(obavijestRepozitorij!!.findByKorisnik(user!!))
        entityManager.flush()
    }

    @Test
    fun pronadiPoKorisnickomImenuTest() {
        val userTest = korisnikRepozitorij!!.findByKorisnickoIme("alex")

        val notification = Obavijest()
        notification.procitano = true
        notification.korisnik = userTest!!
        notification.poruka = "Integration testing message"
        notification.kreirano =Date()
        entityManager!!.persist<Any>(notification)

        val foundNotification = obavijestRepozitorij!!.findByKorisnik(userTest)
        if (foundNotification != null) {
            assertEquals(foundNotification.korisnik, userTest)
        }
        assertNotNull(foundNotification)
    }
}