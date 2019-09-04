package com.diplomski.osobneFinancije.servisi

import com.amazonaws.services.lambda.invoke.LambdaFunction
import com.diplomski.osobneFinancije.entiteti.LambdaInput
import com.diplomski.osobneFinancije.entiteti.LambdaOutput
import com.diplomski.osobneFinancije.entiteti.ReportInput
import com.diplomski.osobneFinancije.entiteti.ReportOutput

interface FinancijeServis {

    @LambdaFunction(functionName = "aws-lambda-kotlin-number-reverse")
    fun lambdaOutput(input: LambdaInput): LambdaOutput

    @LambdaFunction(functionName = "aws-dnevni-izvjestaj")
    fun lambdaIzvjestaj(input: ReportInput): ReportOutput
}