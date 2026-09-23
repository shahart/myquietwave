package com.shahartal.myquietchannel.parasha

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class HebCal(
    @SerializedName("items") val items: List<Item> = emptyList()
)
