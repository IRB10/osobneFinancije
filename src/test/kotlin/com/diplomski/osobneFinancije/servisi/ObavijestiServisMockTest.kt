package com.diplomski.osobneFinancije.servisi


import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Obavijest
import com.diplomski.osobneFinancije.repozitoriji.ObavijestRepozitorij
import com.diplomski.osobneFinancije.servisi.impl.ObavijestiServisImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class ObavijestiServisMockTest {
    @Mock
    internal var obavijestRepozitorij: ObavijestRepozitorij? = null
    @InjectMocks
    internal var obavijestiServis: ObavijestiServisImpl? = null

    @Test
    fun pronadiSveProcitaneZaKorisnikaTest() {
        val notification = Obavijest("False", Date(), Korisnik(1), false)
        val notification1 = Obavijest("False", Date(), Korisnik(1), false)
        val notification2 = Obavijest("True", Date(), Korisnik(1), true)
        notification2.procitano = true
        val notificationList = ArrayList<Obavijest>()
        notificationList.add(notification)
        notificationList.add(notification1)
        notificationList.add(notification2)

        `when`(obavijestRepozitorij!!.findAll()).thenReturn(notificationList)
        val notifications = ArrayList<Obavijest>()
        notifications.add(notification2)
        assertEquals(notifications, obavijestiServis!!.pronadiSveProcitanePoKorisniku(Korisnik(1)))
        assertNotNull(obavijestiServis!!.pronadiSveProcitanePoKorisniku(Korisnik(1)))
        assertNotNull(obavijestiServis!!.pronadiSveProcitanePoKorisniku(Korisnik(0)))
        assertNotNull(obavijestiServis!!.pronadiSveZaKorisnika(Korisnik(1)))
        assertNotNull(obavijestiServis!!.pronadiSveZaKorisnika(Korisnik(0)))
        notificationList.remove(notification2)
        assertEquals(notificationList, obavijestiServis!!.pronadiSveZaKorisnika(Korisnik(1)))

    }

}