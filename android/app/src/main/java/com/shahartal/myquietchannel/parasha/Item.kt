package com.shahartal.myquietchannel.parasha

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Item(
    @SerializedName("category") val category: String = "",
    @SerializedName("hebrew") val hebrew: String = "",
    @SerializedName("leyning") val leyning: Leyning? = null,
    @SerializedName("memo") val memo: String? = null,
    @SerializedName("date") val date: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("subcat") val subcat: String? = null,
    @SerializedName("link") val link: String? = null,
    @SerializedName("yomtov") val yomtov: Boolean = false
)
