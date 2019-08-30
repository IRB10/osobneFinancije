package com.diplomski.osobneFinancije.utils

interface Konstante {
    interface EntryDetails {
        companion object {
            val entryDetailsIncome = "New entry record for korisnik: "
        }
    }

    interface TimeManagament {
        companion object {
            val ONE_MINUTE_IN_MILLIS: Long = 60000
        }
    }

    interface Redirect {
        companion object {
            val redirectLogin = "redirect:/personal-finance/loginPage"
        }
    }

    interface Uloge {
        companion object {
            val adminRole = "ROLE_ADMIN"
            val userRole = "ROLE_USER"
        }
    }

    interface Putanje {
        interface OsnovnePutanje {
            companion object {
                val loginPage = "loginPage"
                val register = "registration"
                const val forgotPassword = "/forgotPassword"
                val login = "/login"
                val logout = "/logout"
                val login_error = "/login-error"
                val home = "/home"
                const val registration = "/registration"
                const val resetPassword = "/resetPassword"
                val forgotPasswordPage = "forgotPassword"
            }
        }

        interface OsiguranePutanje {
            companion object {
                val errors = "error"
                val basePath = "/"
                val errorPutanja = "/error"
                val webjarsAll = "/webjars/**"
                val cssAll = "/css/**"
                val jsAll = "/js/**"
                val imgAll = "/img/**"
                val anyPath = "/**"
                val userAll = "/user/**"
                val adminAll = "/admin/**"
                val userWellcome = "/hello"
                const val updatePassword = "/updatePassword"
                val changePassword = "/changePassword"
                const val savePassword = "/user/savePassword"
                const val registerConfirm = "/registrationConfirm*"
                val badUser = "/badUser"
                const val userProfile = "/user/profile"
                const val profileDetails = "/user/ProfileDetails"
                val profile = "profile"
                val displayProfile = "displayProfile"
                const val userFinances = "/user/Finances"
                val finances = "finances"
                const val accountRoute = "/user/Accounts"
                const val createAccountRoute = "/user/createAccount"
                val createAccount = "createAccount"
                val account = "account"
                const val addIncome = "/user/addIncome"
                const val addExpense = "/user/addExpense"
                const val pathObligations = "/user/Obligations"
                val displayObligations = "obligations"
                const val addObligations = "/user/addObligation"
                const val oldNotifications = "/user/oldNotifications"
                const val newNotifications = "/user/newNotifications"
                const val homepage = "/user/home"
                val overview = "overview"
                const val monthReport = "/user/monthReport"
                val displayMonthReport = "monthReport"
                const val yearReport = "/user/yearReport"
                val displayYearReport = "yearReport"
                const val pdfReport = "/user/pdfReport"
                val displayPdfReport = "pdfReport"
                const val csvData = "/user/csvData"
                const val csvDataImport = "/user/csvDataImport"
                const val apiFinances = "/api/finances"
                const val apiObligations = "/api/addObligation"
                const val displayEntries = "/api/search"
            }
        }
    }
}