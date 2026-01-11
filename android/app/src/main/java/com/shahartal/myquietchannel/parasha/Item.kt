package com.shahartal.myquietchannel.parasha

data class Item(
    val category: String,
    val hebrew: String,
    val leyning: Leyning,
    val memo: String,
    val date: String,
    // val title_orig: String,
    val link: String // https://www.sefaria.org/Zevachim.36a?lang=bi&utm_source=hebcal.com&utm_medium=api
)
