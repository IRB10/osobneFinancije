package com.diplomski.osobneFinancije.kontrolori.rest

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class KorisniciRestKontrolor(
    private val userService: KorisnikServis
) {
    @GetMapping(value = ["/admin/users"])
    @ApiOperation(
        value = "Get users",
        notes = "Get users data.",
        response = Korisnik::class,
        produces = jsonResponseType
    )
    @ApiResponses(value = [ApiResponse(code = 404, message = "No users found")])
    fun dohvatiSveKorisnike(authentication: Authentication): List<Korisnik> {
        val korisnik = userService.pronadiPoKorisnickomImenu(authentication.name)
        return if (korisnik != null && korisnik.uloga_id.naziv == "ROLE_ADMIN") {
            userService.dohvatiSveKorisnike()
        } else {
            Collections.emptyList()
        }
    }

    companion object {
        private const val jsonResponseType = "application/json"
    }
}