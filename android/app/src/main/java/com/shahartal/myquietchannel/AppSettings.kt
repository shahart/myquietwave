package com.shahartal.myquietchannel

import android.content.SharedPreferences

internal data class AppSettings(
    val todo: String = DEFAULT_TODO,
    val location: String = DEFAULT_LOCATION,
    val station: Station = Station.GLGLZ,
    val radioOnly: Boolean = false,
    val newsDurationMinutes: Int = DEFAULT_NEWS_DURATION_MINUTES,
    val scheduleText: String = NewsSchedule.DEFAULT_TEXT,
) {
    companion object {
        const val DEFAULT_LOCATION = "IL-Jerusalem"
        const val DEFAULT_NEWS_DURATION_MINUTES = 4
        const val DEFAULT_TODO = "פלטה, מיחם, שעון שבת, מנורה קטנה במסדרון, מזגן"
    }
}

internal class SettingsRepository(
    private val dataSource: SettingsDataSource,
) {
    constructor(preferences: SharedPreferences) : this(SharedPreferencesSettingsDataSource(preferences))

    fun load(): AppSettings {
        val persistedStation = dataSource.getString(KEY_STATION, Station.GLGLZ.displayName)
        return AppSettings(
            todo = dataSource.getString(KEY_TODO, AppSettings.DEFAULT_TODO) ?: AppSettings.DEFAULT_TODO,
            location = dataSource.getString(KEY_LOCATION, AppSettings.DEFAULT_LOCATION)
                ?: AppSettings.DEFAULT_LOCATION,
            station = Station.fromPersistedValue(persistedStation),
            radioOnly = readRadioOnly(),
            newsDurationMinutes = PlaybackPolicy.normalizeDuration(
                dataSource.getInt(KEY_NEWS_DURATION, AppSettings.DEFAULT_NEWS_DURATION_MINUTES),
                isNearShabbat = false,
            ),
            scheduleText = dataSource.getString(KEY_NEXT_HOURS, NewsSchedule.DEFAULT_TEXT)
                ?.takeIf { it.isNotBlank() }
                ?: NewsSchedule.DEFAULT_TEXT,
        )
    }

    fun save(settings: AppSettings) {
        dataSource.save(
            settings.copy(
                newsDurationMinutes = PlaybackPolicy.normalizeDuration(
                    settings.newsDurationMinutes,
                    isNearShabbat = false,
                ),
                scheduleText = settings.scheduleText.ifBlank { NewsSchedule.DEFAULT_TEXT },
            )
        )
    }

    private fun readRadioOnly(): Boolean = when {
        dataSource.contains(KEY_RADIO_ONLY) -> dataSource.getBoolean(KEY_RADIO_ONLY, false)
        else -> dataSource.getString(KEY_LEGACY_RADIO_ONLY, "false")?.toBooleanStrictOrNull() ?: false
    }

    companion object {
        const val PREFERENCES_NAME = "UserPreferences"
        internal const val KEY_TODO = "todoList"
        internal const val KEY_LOCATION = "location"
        internal const val KEY_STATION = "station"
        internal const val KEY_RADIO_ONLY = "radioOnly"
        internal const val KEY_LEGACY_RADIO_ONLY = "justRadio"
        internal const val KEY_NEWS_DURATION = "newsDuration"
        internal const val KEY_NEXT_HOURS = "nextHours"
    }
}

internal interface SettingsDataSource {
    fun contains(key: String): Boolean
    fun getString(key: String, default: String): String?
    fun getInt(key: String, default: Int): Int
    fun getBoolean(key: String, default: Boolean): Boolean
    fun save(settings: AppSettings)
}

private class SharedPreferencesSettingsDataSource(
    private val preferences: SharedPreferences,
) : SettingsDataSource {
    override fun contains(key: String): Boolean = preferences.contains(key)
    override fun getString(key: String, default: String): String? = preferences.getString(key, default)
    override fun getInt(key: String, default: Int): Int = preferences.getInt(key, default)
    override fun getBoolean(key: String, default: Boolean): Boolean = preferences.getBoolean(key, default)

    override fun save(settings: AppSettings) {
        preferences.edit()
            .putString(SettingsRepository.KEY_TODO, settings.todo)
            .putString(SettingsRepository.KEY_LOCATION, settings.location)
            .putString(SettingsRepository.KEY_STATION, settings.station.displayName)
            .putBoolean(SettingsRepository.KEY_RADIO_ONLY, settings.radioOnly)
            .remove(SettingsRepository.KEY_LEGACY_RADIO_ONLY)
            .putInt(SettingsRepository.KEY_NEWS_DURATION, settings.newsDurationMinutes)
            .putString(SettingsRepository.KEY_NEXT_HOURS, settings.scheduleText)
            .apply()
    }
}
