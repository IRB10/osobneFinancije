package com.diplomski.osobneFinancije.validatori

import com.diplomski.osobneFinancije.forme.LozinkaDto
import com.diplomski.osobneFinancije.forme.RegistracijaForma
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext


class PasswordMatchesValidator : ConstraintValidator<PasswordMatches, Any> {
    override fun initialize(constraintAnnotation: PasswordMatches?) {
        //
    }

    override fun isValid(obj: Any, context: ConstraintValidatorContext): Boolean {
        return if (obj is RegistracijaForma) {
            obj.lozinka == obj.getPonovljenaLozinka()
        } else {
            val user = obj as LozinkaDto
            user.lozinka == user.getPonovljenaLozinka()
        }
    }

}