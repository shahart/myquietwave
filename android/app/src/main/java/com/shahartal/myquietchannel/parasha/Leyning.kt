package com.shahartal.myquietchannel.parasha

data class Leyning(
    val haftarah: String,
    var haftarah_sephardic: String // detekt: Constructor parameter names should match the pattern: [a-z][A-Za-z0-9]* [ConstructorParameterNaming]
)
