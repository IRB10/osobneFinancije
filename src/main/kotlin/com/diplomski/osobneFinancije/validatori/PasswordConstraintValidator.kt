package com.diplomski.osobneFinancije.validatori

import com.google.common.base.Joiner
import org.passay.LengthRule
import org.passay.PasswordData
import org.passay.PasswordValidator
import java.util.*
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class PasswordConstraintValidator : ConstraintValidator<ValidPassword, String> {

    override fun initialize(arg0: ValidPassword?) {

    }

    override fun isValid(password: String, context: ConstraintValidatorContext): Boolean {
        val validator = PasswordValidator(Arrays.asList(LengthRule(4, 30)))
        val result = validator.validate(PasswordData(password))
        if (result.isValid) {
            return true
        }
        context.disableDefaultConstraintViolation()
        context.buildConstraintViolationWithTemplate(Joiner.on(",").join(validator.getMessages(result)))
            .addConstraintViolation()
        return false
    }

}