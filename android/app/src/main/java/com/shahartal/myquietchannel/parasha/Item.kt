package com.shahartal.myquietchannel.parasha

data class Item(
    val category: String,
    val hebrew: String,
    val memo: String,
    val date: String,
    val link: String // https://www.sefaria.org/Zevachim.36a?lang=bi&utm_source=hebcal.com&utm_medium=api
)
