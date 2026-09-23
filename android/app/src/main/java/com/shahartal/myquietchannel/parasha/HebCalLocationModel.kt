package com.shahartal.myquietchannel.parasha

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class HebCalLocationModel(
    @SerializedName("title") val title: String = ""
)
