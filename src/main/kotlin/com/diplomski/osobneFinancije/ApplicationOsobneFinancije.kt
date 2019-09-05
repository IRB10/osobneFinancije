package com.diplomski.osobneFinancije

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.transaction.PlatformTransactionManager
import java.util.concurrent.Executors
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers

@SpringBootApplication
@EnableJpaRepositories("com.diplomski.osobneFinancije.repozitoriji")
@EnableScheduling
class ApplicationOsobneFinancije {

    @Value("\${spring.datasource.maximum-pool-size}")
    private val connectionPoolSize: Int = 0

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<ApplicationOsobneFinancije>(*args)
        }
    }

    @Bean
    fun jdbcScheduler(): Scheduler {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize))
    }

    @Bean
    fun transactionTemplate(transactionManager: PlatformTransactionManager): TransactionTemplate {
        return TransactionTemplate(transactionManager)
    }
}
