package com.diplomski.osobneFinancije.servisi


import com.diplomski.osobneFinancije.entiteti.Kategorija
import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Token
import com.diplomski.osobneFinancije.entiteti.Transakcija
import com.diplomski.osobneFinancije.forme.ProfilForma
import com.diplomski.osobneFinancije.repozitoriji.*
import com.diplomski.osobneFinancije.servisi.impl.KorisnikServisImpl
import com.diplomski.osobneFinancije.utils.Konstante
import com.google.common.collect.ImmutableList
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.context.MessageSource
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.scheduler.Scheduler
import java.time.LocalDateTime
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class KorisnikServisMockTest {
    @Mock
    internal var korisnikRepozitorij: KorisnikRepozitorij? = null
    @Mock
    internal var transakcijaRepozitorij: TransakcijaRepozitorij? = null
    @Mock
    internal var tokenRepozitorij: TokenRepozitorij? = null
    @Mock
    internal var poruke: MessageSource? = null
    @Mock
    internal var jdbcScheduler: Scheduler? = null
    @Mock
    internal var transactionTemplate: TransactionTemplate? = null
    @Mock
    internal var obavijestiServis: ObavijestiServis? = null
    @Mock
    internal var kategorijaRepozitorij: KategorijaRepozitorij? = null
    @Mock
    internal var ulogaRepozitorij: UlogaRepozitorij? = null
    @Mock
    internal var prijenosRepozitorij: PrijenosRepozitorij? = null
    @Mock
    internal var racunRepozitorij: RacunRepozitorij? = null
    @Mock
    internal var verifikacijskiTokenRepozitorij: VerifikacijskiTokenRepozitorij? = null
    @InjectMocks
    internal var korisnikServisImpl: KorisnikServisImpl? = null

    @Before
    fun init() {
        val authentication = Mockito.mock(Authentication::class.java)
        val securityContext = Mockito.mock(SecurityContext::class.java)
        Mockito.`when`(securityContext.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(securityContext)
        `when`(securityContext.authentication.name).thenReturn("antevu")
    }

    @Test
    fun azurirajDetaljeKorisnikTest() {
        val profileForm = ProfilForma()
        profileForm.ime = "testName"
        profileForm.prezime = "testSurname"
        profileForm.email = "testEmail@gmail.com"

        val createdUserAccount = korisnikServisImpl!!.azurirajDetaljeKorisnika(Korisnik(), profileForm)
        assertEquals("testEmail@gmail.com", createdUserAccount.email)
        assertEquals("testName", createdUserAccount.ime)
        assertEquals("testSurname", createdUserAccount.prezime)
        assertEquals(Korisnik::class.java, createdUserAccount.javaClass)
        assertNull(createdUserAccount.lozinka)
        assertEquals(0.0, createdUserAccount.stanjeRacuna, 0.0)
    }

    @Test
    fun dohvatiTransakcijeTest() {
        `when`(transakcijaRepozitorij!!.findAll()).thenReturn(
            ImmutableList.of<Transakcija>(
                Transakcija(
                    2.0,
                    LocalDateTime.parse("2019-09-12T15:00:05.045"),
                    Kategorija(1),
                    Korisnik(1),
                    LocalDateTime.parse("2019-09-12T15:00:05.045"), "irb10", "antevu"
                ),
                Transakcija(3.0, LocalDateTime.now(), Kategorija(1), Korisnik(1), LocalDateTime.MAX, "antevu", "irb10")
            )
        )
        val entryList = ArrayList<Transakcija>()
        entryList.add(
            Transakcija(
                2.0,
                LocalDateTime.parse("2019-09-12T15:00:05.045"),
                Kategorija(1),
                Korisnik(1),
                LocalDateTime.parse("2019-09-12T15:00:05.045"), "irb10", "antevu"
            )
        )
        entryList.add(
            Transakcija(3.0, LocalDateTime.now(), Kategorija(1), Korisnik(1), LocalDateTime.MAX, "antevu", "irb10")
        )
        assertEquals(entryList.size, korisnikServisImpl!!.dohvatiSveTransakcije().size)
    }

    @Test
    fun dohvatiTransakcijeZaMjesecTest() {
        `when`(transakcijaRepozitorij!!.findAll()).thenReturn(
            ImmutableList.of<Transakcija>(
                Transakcija(
                    2.0,
                    LocalDateTime.now(),
                    Kategorija(1),
                    Korisnik(1),
                    LocalDateTime.parse("2019-09-12T15:00:05.045"), "antevu", "irb10"
                ),
                Transakcija(3.0, LocalDateTime.now(), Kategorija(1), Korisnik(1), LocalDateTime.MAX, "antevu", "irb10"),
                Transakcija(3.0, LocalDateTime.now(), Kategorija(1), Korisnik(1), LocalDateTime.MIN, "antevu", "irb10")
            )
        )
        val entryList = ArrayList<Transakcija>()
        entryList
            .add(
                Transakcija(
                    2.0,
                    LocalDateTime.now(),
                    Kategorija(1),
                    Korisnik(1),
                    LocalDateTime.parse("2019-09-12T15:00:05.045"), "antevu", "irb10"
                )
            )

        assertEquals(entryList.size, korisnikServisImpl!!.dohvatiTransakcijeZaMjesec(9).size)
    }

    @Test
    fun dohvatiTransakcijeZaGodinuTest() {
        `when`(transakcijaRepozitorij!!.findAll()).thenReturn(
            ImmutableList.of<Transakcija>(
                Transakcija(
                    2.0,
                    LocalDateTime.now(),
                    Kategorija(1),
                    Korisnik(1),
                    LocalDateTime.parse("2019-09-12T15:00:05.045"), "antevu", "irb10"
                ),
                Transakcija(3.0, LocalDateTime.now(), Kategorija(1), Korisnik(1), LocalDateTime.MAX, "antevu", "irb10"),
                Transakcija(3.0, LocalDateTime.now(), Kategorija(1), Korisnik(1), LocalDateTime.MIN, "antevu", "irb10")
            )
        )
        val entryList = ArrayList<Transakcija>()
        entryList
            .add(
                Transakcija(
                    2.0,
                    LocalDateTime.now(),
                    Kategorija(1),
                    Korisnik(1),
                    LocalDateTime.parse("2019-09-12T15:00:05.045"), "antevu", "irb10"
                )
            )

        assertEquals(entryList.size, korisnikServisImpl!!.dohvatiTransakcijeZaGodinu(2019).size)
    }

    @Test
    fun dohvatiTransakcijeZaKorisnikaTest() {
        `when`(transakcijaRepozitorij!!.findAll()).thenReturn(
            ImmutableList.of<Transakcija>(
                Transakcija(
                    2.0,
                    LocalDateTime.now(),
                    Kategorija(1),
                    Korisnik(1),
                    LocalDateTime.parse("2019-09-12T15:00:05.045"), "antevu", "irb10"
                ),
                Transakcija(3.0, LocalDateTime.now(), Kategorija(1), Korisnik(1), LocalDateTime.MAX, "antevu", "irb10"),
                Transakcija(3.0, LocalDateTime.now(), Kategorija(1), Korisnik(1), LocalDateTime.MIN, "antevu", "irb10")
            )
        )
        val entryList = ArrayList<Transakcija>()
        entryList
            .add(
                Transakcija(
                    2.0,
                    LocalDateTime.now(),
                    Kategorija(1),
                    Korisnik(1),
                    LocalDateTime.parse("2019-09-12T15:00:05.045"), "antevu", "irb10"
                )
            )
        entryList.add(
            Transakcija(
                3.0,
                LocalDateTime.now(),
                Kategorija(1),
                Korisnik(1),
                LocalDateTime.MAX,
                "antevu",
                "irb10"
            )
        )
        entryList.add(
            Transakcija(
                3.0,
                LocalDateTime.now(),
                Kategorija(1),
                Korisnik(1),
                LocalDateTime.MIN,
                "antevu",
                "irb10"
            )
        )
        assertEquals(entryList.size, korisnikServisImpl!!.dohvatiTransakcijeZaKorisnika().size)
    }

    @Test
    fun provjeriAkoPostojiTest() {
        val entryList = ImmutableList.of<Transakcija>(
            Transakcija(
                2.0,
                LocalDateTime.now(),
                Kategorija(1),
                Korisnik(1),
                LocalDateTime.parse("2019-09-12T15:00:05.045"), "antevu", "irb10"
            ),
            Transakcija(3.0, LocalDateTime.now(), Kategorija(1), Korisnik(1), LocalDateTime.MAX, "antevu", "irb10"),
            Transakcija(3.0, LocalDateTime.now(), Kategorija(1), Korisnik(1), LocalDateTime.MIN, "antevu", "irb10")
        )
        val entry = Transakcija(
            2.0,
            LocalDateTime.now(),
            Kategorija(1),
            Korisnik(1),
            LocalDateTime.parse("2019-09-12T15:00:05.045"), "antevu", "irb10"
        )
        assertTrue(korisnikServisImpl!!.checkIfAlreadyExists(entry, entryList))

    }

    @Test
    fun validirajTokenZaResetLozinkeTest() {
        val curTimeInMs = Date().time
        val myToken = Token("c659343e-8528-4ec3-8bdf-054502e461b1", Korisnik())
        myToken.datumIsteka = Date(curTimeInMs + 5 * Konstante.TimeManagament.ONE_MINUTE_IN_MILLIS)
        val myTokenExpired = Token("c659343e-8528-4ec3-8bdf-054502e461b2", Korisnik())
        myTokenExpired.datumIsteka = Date(curTimeInMs - 5 * Konstante.TimeManagament.ONE_MINUTE_IN_MILLIS)
        `when`(tokenRepozitorij!!.findByToken("c659343e-8528-4ec3-8bdf-054502e461b1")).thenReturn(myToken)
        `when`(tokenRepozitorij!!.findByToken("c659343e-8528-4ec3-8bdf-054502e461b2")).thenReturn(
            myTokenExpired
        )
        assertEquals("invalidToken", korisnikServisImpl!!.validirajTokenZaLozinku(1, ""))
        assertEquals(
            "valid",
            korisnikServisImpl!!.validirajTokenZaLozinku(0, "c659343e-8528-4ec3-8bdf-054502e461b1")
        )
        assertEquals(
            "expired",
            korisnikServisImpl!!.validirajTokenZaLozinku(0, "c659343e-8528-4ec3-8bdf-054502e461b2")
        )
    }
}