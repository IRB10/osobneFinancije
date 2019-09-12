package com.diplomski.osobneFinancije.kontrolori.rest

import com.diplomski.osobneFinancije.entiteti.Transakcija
import com.diplomski.osobneFinancije.forme.FinancijeForma
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.apiFinances
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.apiObligations
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
class FinancijeRestKontrolor(
    private val userService: KorisnikServis
) {

    @GetMapping(value = [apiFinances])
    @ApiOperation(
        value = "Get entries",
        notes = "Fetch user data.",
        response = Transakcija::class,
        produces = jsonResponseType
    )
    @ApiResponses(value = [ApiResponse(code = 404, message = "Entries not found")])
    fun retriveEntriesForUser(authentication: Authentication): List<Transakcija> {
        return userService.dohvatiTransakcijeZaKorisnika(authentication.name)
    }

    @PostMapping(value = [apiObligations])
    @ApiOperation(
        value = "Add or update entry",
        notes = "Add or update entry.",
        response = Transakcija::class,
        produces = jsonResponseType
    )
    @ApiResponses(value = [ApiResponse(code = 404, message = "Entries cannot be added or updated")])
    fun addNewObligation(@RequestBody financijeForma: FinancijeForma) {
        userService.spremiTransakcijuAJAX(financijeForma)
    }

    @GetMapping("/api/transactions/all")
    @ResponseBody
    fun list(): Flux<Transakcija> {
        return this.userService.dohvatiSveTransakcijeReaktivno()
    }

    companion object {
        private const val jsonResponseType = "application/json"
    }
}