package com.diplomski.osobneFinancije.validatori

import com.diplomski.osobneFinancije.forme.PasswordDto
import com.diplomski.osobneFinancije.forme.RegisterForm
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext


class PasswordMatchesValidator : ConstraintValidator<PasswordMatches, Any> {
    override fun initialize(constraintAnnotation: PasswordMatches?) {
        //
    }

    override fun isValid(obj: Any, context: ConstraintValidatorContext): Boolean {
        return if (obj is RegisterForm) {
            obj.lozinka == obj.getPonovljenaLozinka()
        } else {
            val user = obj as PasswordDto
            user.lozinka == user.getPonovljenaLozinka()
        }
    }

}