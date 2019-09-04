package com.diplomski.osobneFinancije

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaRepositories("com.diplomski.osobneFinancije.repozitoriji")
@EnableScheduling
class ApplicationOsobneFinancije {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<ApplicationOsobneFinancije>(*args)
        }
    }
}
