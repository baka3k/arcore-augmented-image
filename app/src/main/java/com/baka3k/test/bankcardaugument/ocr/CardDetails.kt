package com.baka3k.test.bankcardaugument.ocr

data class CardDetails(
    val owner: String?,
    val number: String?,
    val expirationMonth: String?,
    val expirationYear: String?
)