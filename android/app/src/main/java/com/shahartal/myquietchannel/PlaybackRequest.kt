package com.shahartal.myquietchannel

import android.content.Intent

internal data class PlaybackIntentValues(
    val station: String?,
    val newsDurationMinutes: Int,
    val scheduleText: String?,
    val radioOnly: Boolean,
) {
    companion object {
        fun fromIntent(intent: Intent?): PlaybackIntentValues = PlaybackIntentValues(
            station = intent?.getStringExtra(VolumeCycleService.EXTRA_STATION)
                ?: Station.GLGLZ.displayName,
            newsDurationMinutes = intent?.getIntExtra(
                VolumeCycleService.EXTRA_NEWS_DURATION,
                AppSettings.DEFAULT_NEWS_DURATION_MINUTES,
            ) ?: AppSettings.DEFAULT_NEWS_DURATION_MINUTES,
            scheduleText = intent?.getStringExtra(VolumeCycleService.EXTRA_NEXT_HOURS),
            radioOnly = intent?.getBooleanExtra(VolumeCycleService.EXTRA_RADIO_PLAYER, false) ?: false,
        )
    }
}

internal data class PlaybackStartRequest(
    val station: Station,
    val newsDurationMinutes: Int,
    val scheduleText: String,
    val todo: String,
    val location: String,
    val radioOnly: Boolean,
) {
    companion object {
        fun fromUiValues(
            station: String?,
            newsDuration: String?,
            schedule: String?,
            todo: String?,
            location: String?,
            radioOnly: Boolean,
        ): PlaybackStartRequest = PlaybackStartRequest(
            station = Station.fromPersistedValue(station),
            newsDurationMinutes = PlaybackPolicy.normalizeDuration(
                newsDuration?.toIntOrNull() ?: AppSettings.DEFAULT_NEWS_DURATION_MINUTES,
                isNearShabbat = false,
            ),
            scheduleText = schedule.orEmpty().ifBlank { NewsSchedule.DEFAULT_TEXT },
            todo = todo.orEmpty(),
            location = location.orEmpty(),
            radioOnly = radioOnly,
        )
    }
}
