package com.diplomski.osobneFinancije.kontrolori

import org.springframework.ui.ModelMap
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
class StandardniKontrolor {
    @GetMapping("/")
    fun defaultPage(model: ModelMap): ModelAndView {
        return ModelAndView("index")
    }

    @GetMapping("/home")
    fun pocetnaLogiranKorisnik(): ModelAndView {
        return ModelAndView("home")
    }
}