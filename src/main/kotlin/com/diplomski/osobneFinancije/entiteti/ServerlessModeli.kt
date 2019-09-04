package com.diplomski.osobneFinancije.entiteti

data class LambdaInput(
    val inputFirst: Double?,
    val inputSecond: Double?,
    val inputOperation: String?
)

data class LambdaOutput(
    val result: Double = 0.0
)

data class ReportInput(
    val rezultatInput: Map<String, Transakcija>
)

data class ReportOutput(
    var rezultatOutput: String? = ""
)