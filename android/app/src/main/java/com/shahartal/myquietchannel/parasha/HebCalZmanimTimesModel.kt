package com.shahartal.myquietchannel.parasha

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class HebCalZmanimTimesModel(
    @SerializedName("sunrise") val sunrise: String = "",
    @SerializedName("sunset") val sunset: String = "",
    @SerializedName("beinHaShmashos") val beinHaShmashos: String = "",
    @SerializedName("dusk") val dusk: String = "",
    @SerializedName("tzeit7083deg") val tzeit7083deg: String = "",
    @SerializedName("tzeit72min") val tzeit72min: String = "",
    @SerializedName("dawn") val dawn: String = "",
    @SerializedName("chatzot") val chatzot: String = "",
    @SerializedName("chatzotNight") val chatzotNight: String = "",
    @SerializedName("alotHaShachar") val alotHaShachar: String = "",
    @SerializedName("minchaGedola") val minchaGedola: String = "",
    @SerializedName("plagHaMincha") val plagHaMincha: String = "",
    @SerializedName("minchaKetana") val minchaKetana: String = "",
    @SerializedName("sofZmanShma") val sofZmanShma: String = "",
    @SerializedName("sofZmanTfilla") val sofZmanTfilla: String = "",
    @SerializedName("sofZmanShmaMGA") val sofZmanShmaMGA: String = "",
    @SerializedName("sofZmanTfillaMGA") val sofZmanTfillaMGA: String = ""
)
