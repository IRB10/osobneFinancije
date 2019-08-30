package com.diplomski.osobneFinancije.kontrolori

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest


@Controller
class ErrorKontrolor : ErrorController {

    @RequestMapping("/error")
    fun handleError(model: ModelMap, httpServletRequest: HttpServletRequest): String {
        var status: Any? = httpServletRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)

        if (status != null) {
            val statusCode: Int? = Integer.valueOf(status.toString())
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("statusCode", statusCode)
                return "error"
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("statusCode", statusCode)
                return "error"
            }
        }
        model.addAttribute("statusCode", RequestDispatcher.ERROR_STATUS_CODE)
        return "error"
    }

    override fun getErrorPath(): String {
        return "/error"
    }
}