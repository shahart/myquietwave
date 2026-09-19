package com.shahartal.myquietchannel

internal enum class Station(
    val displayName: String,
    val streamUrl: String,
    val songFeedName: String? = null,
) {
    GLGLZ("גלגלצ", "https://glzwizzlv.bynetcdn.com/glglz_mp3", "glglz"),
    GLZ("גלי צהל", "https://glzwizzlv.bynetcdn.com/glz_mp3"),
    BET("רשת ב", "https://playerservices.streamtheworld.com/api/livestream-redirect/KAN_BET.mp3"),
    GIMMEL("רשת ג", "https://playerservices.streamtheworld.com/api/livestream-redirect/KAN_GIMMEL.mp3"),
    FM102("FM102", "https://cdn88.mediacast.co.il/102fm-tlv/102fm_mp3/icecast.audio"),
    GALEY_ISRAEL("גלי ישראל", "https://cdn.cybercdn.live/Galei_Israel/Live/icecast.audio"),
    KAN_88("כאן 88", "https://27863.live.streamtheworld.com/KAN_88.mp3"),
    KOL_CHAI("קול חי", "https://live.kcm.fm/live-new"),
    KOL_CHAI_MUSIC("קול חי מיוזיק", "https://live.kcm.fm/livemusic"),
    KOL_BARAMA("קול ברמה", "https://cdn.cybercdn.live/Kol_Barama/Live_Audio/icecast.audio"),
    MORESHET("כאן מורשת", "https://playerservices.streamtheworld.com/api/livestream-redirect/KAN_MORESHET.mp3"),
    ;

    companion object {
        fun fromPersistedValue(value: String?): Station {
            val normalized = value?.trim()
            return entries.firstOrNull { it.displayName == normalized || it.name == normalized } ?: GLGLZ
        }

        fun fromStreamUrl(value: String?): Station? = entries.firstOrNull { it.streamUrl == value }
    }
}
