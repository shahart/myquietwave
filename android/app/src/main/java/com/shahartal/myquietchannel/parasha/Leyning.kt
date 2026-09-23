package com.shahartal.myquietchannel.parasha

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Leyning(
    @SerializedName("haftarah") val haftarah: String = "",
    @SerializedName("haftarah_sephardic") val haftarah_sephardic: String? = null
)
