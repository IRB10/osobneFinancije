package com.diplomski.osobneFinancije.validatori

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass


@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordConstraintValidator::class])
@MustBeDocumented
annotation class ValidPassword(
    val message: String = "Lozinka nepravilna",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)