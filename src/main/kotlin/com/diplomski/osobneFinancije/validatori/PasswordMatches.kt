package com.diplomski.osobneFinancije.validatori

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordMatchesValidator::class])
@MustBeDocumented
annotation class PasswordMatches(
    val message: String = "{label.password.dont.match}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)