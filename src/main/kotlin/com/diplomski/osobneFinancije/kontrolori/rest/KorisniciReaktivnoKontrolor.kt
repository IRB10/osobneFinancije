package com.diplomski.osobneFinancije.kontrolori.rest

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/api")
class KorisniciReaktivnoKontrolor(private val korisnikServis: KorisnikServis) {

    @RequestMapping(
        value = ["/users/one"],
        method = [RequestMethod.POST],
        consumes = [APPLICATION_JSON_VALUE],
        produces = [APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun searchOne(httpEntity: HttpEntity<String>): Mono<Korisnik> {
        return this.korisnikServis.dohvatiKorisnikaReaktivno(JSONObject(httpEntity.body!!).getString("korisnickoIme"))
    }

    @GetMapping("/users/all")
    @ResponseBody
    fun list(): Flux<Korisnik> {
        return this.korisnikServis.dohvatiSveKorisnikeReaktivno()
    }

    @PostMapping(value = ["/users/add"])
    @ApiOperation(
        value = "Add or update entry",
        notes = "Add or update entry.",
        response = Korisnik::class,
        produces = KorisniciReaktivnoKontrolor.jsonResponseType
    )
    @ApiResponses(value = [ApiResponse(code = 404, message = "Entries cannot be added or updated")])
    fun add(@RequestBody korisnik: Korisnik): Mono<Long> {
        return this.korisnikServis.dodajKorisnikaReaktivno(korisnik).map { korisnik1 -> korisnik1!!.id }
    }

    companion object {
        private const val jsonResponseType = "application/json"
    }
}