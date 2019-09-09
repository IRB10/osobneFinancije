package com.diplomski.osobneFinancije.dogadaji

import com.diplomski.osobneFinancije.entiteti.Korisnik
import org.springframework.context.ApplicationEvent
import java.util.*


class ZavrsenaRegistracijaDogadaj(var korisnik: Korisnik, var locale: Locale, var appUrl: String) :
    ApplicationEvent(korisnik)