package com.shahartal.myquietchannel

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
