package com.diplomski.osobneFinancije.forme

import com.diplomski.osobneFinancije.validatori.PasswordMatches
import com.diplomski.osobneFinancije.validatori.ValidEmail
import com.diplomski.osobneFinancije.validatori.ValidUsername
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@PasswordMatches
class RegistracijaForma {
    @ValidUsername
    @NotEmpty
    var korisnickoIme: String? = null

    @NotEmpty
    var ime: String? = null

    @NotEmpty
    var prezime: String? = null

    @ValidEmail
    var email: String? = null

    @NotEmpty
    var lozinka: String? = null

    @NotEmpty
    private var ponovljenaLozinka: String? = null

    fun getPonovljenaLozinka(): String? {
        return ponovljenaLozinka
    }

    fun setPonovljenaLozinka(rPassword: String) {
        this.ponovljenaLozinka = rPassword
    }

}