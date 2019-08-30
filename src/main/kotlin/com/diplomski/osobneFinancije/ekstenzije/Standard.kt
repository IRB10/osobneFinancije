package com.diplomski.osobneFinancije.ekstenzije

/**
 * Poziva specifičnu funkciju [block] s danim [receiver] kao prijamnikom i vraća Unit.
 */
inline fun <T> configure(receiver: T, block: T.() -> Unit) = receiver.block()