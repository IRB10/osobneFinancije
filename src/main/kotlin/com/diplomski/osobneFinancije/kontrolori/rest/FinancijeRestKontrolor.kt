package com.diplomski.osobneFinancije.kontrolori.rest

import com.diplomski.osobneFinancije.entiteti.Transakcija
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.apiFinances
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.apiObligations
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

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
    fun addNewObligation(@RequestBody entry: Transakcija) {
        userService.spremiTransakciju(entry)
    }

    companion object {
        private const val jsonResponseType = "application/json"
    }
}