package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {
    @Test
    fun missingValuesLoadDocumentedDefaults() {
        assertEquals(AppSettings(), SettingsRepository(FakeSettingsDataSource()).load())
    }

    @Test
    fun legacyRadioStringIsReadAndReplacedOnSave() {
        val dataSource = FakeSettingsDataSource(
            strings = mutableMapOf(SettingsRepository.KEY_LEGACY_RADIO_ONLY to "true")
        )
        val repository = SettingsRepository(dataSource)

        assertTrue(repository.load().radioOnly)
        repository.save(repository.load())

        assertTrue(dataSource.booleans.getValue(SettingsRepository.KEY_RADIO_ONLY))
        assertFalse(dataSource.strings.containsKey(SettingsRepository.KEY_LEGACY_RADIO_ONLY))
    }

    @Test
    fun saveNormalizesDurationAndBlankSchedule() {
        val dataSource = FakeSettingsDataSource()
        val repository = SettingsRepository(dataSource)

        repository.save(AppSettings(newsDurationMinutes = 500, scheduleText = ""))

        assertEquals(PlaybackPolicy.MAX_NEWS_DURATION_MINUTES, repository.load().newsDurationMinutes)
        assertEquals(NewsSchedule.DEFAULT_TEXT, repository.load().scheduleText)
    }

    @Test
    fun settingsRoundTrip() {
        val repository = SettingsRepository(FakeSettingsDataSource())
        val expected = AppSettings(
            todo = "test",
            location = "US-New York-NY",
            station = Station.KAN_88,
            radioOnly = true,
            newsDurationMinutes = 8,
            scheduleText = "8, 12:30",
        )

        repository.save(expected)

        assertEquals(expected, repository.load())
    }
}

private class FakeSettingsDataSource(
    val strings: MutableMap<String, String> = mutableMapOf(),
    val ints: MutableMap<String, Int> = mutableMapOf(),
    val booleans: MutableMap<String, Boolean> = mutableMapOf(),
) : SettingsDataSource {
    override fun contains(key: String): Boolean =
        key in strings || key in ints || key in booleans

    override fun getString(key: String, default: String): String = strings[key] ?: default
    override fun getInt(key: String, default: Int): Int = ints[key] ?: default
    override fun getBoolean(key: String, default: Boolean): Boolean = booleans[key] ?: default

    override fun save(settings: AppSettings) {
        strings[SettingsRepository.KEY_TODO] = settings.todo
        strings[SettingsRepository.KEY_LOCATION] = settings.location
        strings[SettingsRepository.KEY_STATION] = settings.station.displayName
        strings[SettingsRepository.KEY_NEXT_HOURS] = settings.scheduleText
        strings.remove(SettingsRepository.KEY_LEGACY_RADIO_ONLY)
        ints[SettingsRepository.KEY_NEWS_DURATION] = settings.newsDurationMinutes
        booleans[SettingsRepository.KEY_RADIO_ONLY] = settings.radioOnly
    }
}
