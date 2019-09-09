package com.diplomski.osobneFinancije.validatori

import com.diplomski.osobneFinancije.repozitoriji.KorisnikRepozitorij
import javax.validation.ConstraintValidatorContext
import org.springframework.beans.factory.annotation.Autowired
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.validation.ConstraintValidator


class EmailValidator : ConstraintValidator<ValidEmail, String> {
    @Autowired
    private val korisnikRepozitorij: KorisnikRepozitorij? = null

    private var pattern: Pattern? = null
    private var matcher: Matcher? = null

    override fun initialize(constraintAnnotation: ValidEmail?) {}

    override fun isValid(email: String, context: ConstraintValidatorContext): Boolean {
        return validateEmail(email)
    }

    private fun validateEmail(email: String): Boolean {
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern!!.matcher(email)
        return if (emailExist(email)) {
            false
        } else {
            matcher!!.matches()
        }
    }

    private fun emailExist(email: String): Boolean {
        val user = korisnikRepozitorij!!.findByEmail(email)
        return user != null
    }

    companion object {
        private const val EMAIL_PATTERN =
            "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$"
    }

}