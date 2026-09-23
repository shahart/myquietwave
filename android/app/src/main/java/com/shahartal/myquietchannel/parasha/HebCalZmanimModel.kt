package com.shahartal.myquietchannel.parasha

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class HebCalZmanimModel(
    @SerializedName("location") val location: HebCalLocationModel? = null,
    @SerializedName("times") val times: HebCalZmanimTimesModel? = null
)
