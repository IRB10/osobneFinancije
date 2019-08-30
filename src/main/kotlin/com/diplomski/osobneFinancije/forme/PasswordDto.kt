package com.diplomski.osobneFinancije.forme

import com.diplomski.osobneFinancije.validatori.PasswordMatches
import com.diplomski.osobneFinancije.validatori.ValidPassword

@PasswordMatches
class PasswordDto {

    var staraLozinka: String? = null

    @ValidPassword
    var lozinka: String? = null

    private var ponovljenaLozinka: String? = null

    fun getPonovljenaLozinka(): String? {
        return ponovljenaLozinka
    }

    fun setPonovljenaLozinka(ponovljenaLozinka: String) {
        this.ponovljenaLozinka = ponovljenaLozinka
    }
}