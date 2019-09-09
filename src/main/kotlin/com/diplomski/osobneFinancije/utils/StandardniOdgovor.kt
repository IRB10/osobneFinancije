package com.diplomski.osobneFinancije.utils

import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError


class StandardniOdgovor {
    private var poruka: String = ""
    private var error: String? = null

    constructor(message: String) : super() {
        this.poruka = message
    }

    constructor(message: String, error: String) : super() {
        this.poruka = message
        this.error = error
    }

    constructor(allErrors: List<ObjectError>, error: String) {
        this.error = error
        var temp = ""
        for (err in allErrors) {
            temp += if (err is FieldError) {
                "{\"field\":\"" + err.field + "\",\"defaultMessage\":\"" + err.defaultMessage + "\"},"
            } else {
                "{\"object\":\"" + err.objectName + "\",\"defaultMessage\":\"" + err.defaultMessage + "\"},"
            }
        }
        this.poruka = "[${temp.substring(0, temp.length - 1)}]"
    }
}