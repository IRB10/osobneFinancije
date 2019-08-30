package com.diplomski.osobneFinancije.validatori

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass


@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UsernameValidator::class])
@MustBeDocumented
annotation class ValidUsername(
    val message: String = "Korisničko ime već postoji",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)