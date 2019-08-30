package com.diplomski.osobneFinancije.validatori

import com.diplomski.osobneFinancije.repozitoriji.KorisnikRepozitorij
import javax.validation.ConstraintValidatorContext
import org.springframework.beans.factory.annotation.Autowired
import javax.validation.ConstraintValidator


class UsernameValidator : ConstraintValidator<ValidUsername, String> {
    @Autowired
    private val korisnikRepozitorij: KorisnikRepozitorij? = null

    override fun initialize(constraintAnnotation: ValidUsername?) {}

    override fun isValid(s: String, constraintValidatorContext: ConstraintValidatorContext): Boolean {
        return validirajKorisnickoIme(s)
    }

    private fun validirajKorisnickoIme(korisnickoIme: String): Boolean {
        return !korisnikPostoji(korisnickoIme)
    }

    private fun korisnikPostoji(korisnickoIme: String): Boolean {
        val user = korisnikRepozitorij!!.findByKorisnickoIme(korisnickoIme)
        return user != null
    }
}