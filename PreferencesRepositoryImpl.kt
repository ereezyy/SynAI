package com.example.synapseai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.synapseai.data.model.UserPreferences
import com.example.synapseai.util.ErrorHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the PreferencesRepository interface using DataStore.
 */
@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    // Create a DataStore instance using the extension property
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "synapse_preferences")

    // Define preference keys
    private object PreferenceKeys {
        // Notification preferences
        val ENABLE_MEETING_REMINDERS = booleanPreferencesKey("enable_meeting_reminders")
        val ENABLE_TRANSCRIPTION_NOTIFICATIONS = booleanPreferencesKey("enable_transcription_notifications")
        val ENABLE_SUMMARIZATION_NOTIFICATIONS = booleanPreferencesKey("enable_summarization_notifications")

        // Default reminder times (stored as a comma-separated string)
        val DEFAULT_REMINDER_TIMES = stringPreferencesKey("default_reminder_times")

        // Theme preferences
        val USE_DARK_MODE = booleanPreferencesKey("use_dark_mode")
        val USE_SYSTEM_THEME = booleanPreferencesKey("use_system_theme")
        val PRIMARY_COLOR = stringPreferencesKey("primary_color")

        // Other preferences
        val AUTO_START_RECORDING = booleanPreferencesKey("auto_start_recording")
        val KEEP_AUDIO_AFTER_TRANSCRIPTION = booleanPreferencesKey("keep_audio_after_transcription")
    }

    override fun getUserPreferences(): Flow<UserPreferences> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    ErrorHandler.logError(exception, "Error reading preferences")
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                mapPreferencesToUserPreferences(preferences)
            }
    }

    override suspend fun updateNotificationPreferences(
        enableMeetingReminders: Boolean?,
        enableTranscriptionNotifications: Boolean?,
        enableSummarizationNotifications: Boolean?
    ) {
        context.dataStore.edit { preferences ->
            enableMeetingReminders?.let { preferences[PreferenceKeys.ENABLE_MEETING_REMINDERS] = it }
            enableTranscriptionNotifications?.let { preferences[PreferenceKeys.ENABLE_TRANSCRIPTION_NOTIFICATIONS] = it }
            enableSummarizationNotifications?.let { preferences[PreferenceKeys.ENABLE_SUMMARIZATION_NOTIFICATIONS] = it }
        }
    }

    override suspend fun updateDefaultReminderTimes(reminderTimes: List<Int>) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DEFAULT_REMINDER_TIMES] = reminderTimes.joinToString(",")
        }
    }

    override suspend fun updateThemePreferences(
        useDarkMode: Boolean?,
        useSystemTheme: Boolean?,
        primaryColor: String?
    ) {
        context.dataStore.edit { preferences ->
            useDarkMode?.let { preferences[PreferenceKeys.USE_DARK_MODE] = it }
            useSystemTheme?.let { preferences[PreferenceKeys.USE_SYSTEM_THEME] = it }
            primaryColor?.let { preferences[PreferenceKeys.PRIMARY_COLOR] = it }
        }
    }

    override suspend fun updateOtherPreferences(
        autoStartRecording: Boolean?,
        keepAudioAfterTranscription: Boolean?
    ) {
        context.dataStore.edit { preferences ->
            autoStartRecording?.let { preferences[PreferenceKeys.AUTO_START_RECORDING] = it }
            keepAudioAfterTranscription?.let { preferences[PreferenceKeys.KEEP_AUDIO_AFTER_TRANSCRIPTION] = it }
        }
    }

    override suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Map the raw Preferences to a UserPreferences object.
     */
    private fun mapPreferencesToUserPreferences(preferences: Preferences): UserPreferences {
        // Create a default UserPreferences object
        val defaultPreferences = UserPreferences()

        // Get reminder times
        val reminderTimesString = preferences[PreferenceKeys.DEFAULT_REMINDER_TIMES]
        val reminderTimes = if (reminderTimesString != null) {
            reminderTimesString.split(",").mapNotNull { it.trim().toIntOrNull() }
        } else {
            defaultPreferences.defaultReminderTimes
        }

        // Return a UserPreferences object with values from preferences or defaults
        return UserPreferences(
            enableMeetingReminders = preferences[PreferenceKeys.ENABLE_MEETING_REMINDERS]
                ?: defaultPreferences.enableMeetingReminders,
            enableTranscriptionNotifications = preferences[PreferenceKeys.ENABLE_TRANSCRIPTION_NOTIFICATIONS]
                ?: defaultPreferences.enableTranscriptionNotifications,
            enableSummarizationNotifications = preferences[PreferenceKeys.ENABLE_SUMMARIZATION_NOTIFICATIONS]
                ?: defaultPreferences.enableSummarizationNotifications,
            defaultReminderTimes = reminderTimes,
            useDarkMode = preferences[PreferenceKeys.USE_DARK_MODE]
                ?: defaultPreferences.useDarkMode,
            useSystemTheme = preferences[PreferenceKeys.USE_SYSTEM_THEME]
                ?: defaultPreferences.useSystemTheme,
            primaryColor = preferences[PreferenceKeys.PRIMARY_COLOR]
                ?: defaultPreferences.primaryColor,
            autoStartRecording = preferences[PreferenceKeys.AUTO_START_RECORDING]
                ?: defaultPreferences.autoStartRecording,
            keepAudioAfterTranscription = preferences[PreferenceKeys.KEEP_AUDIO_AFTER_TRANSCRIPTION]
                ?: defaultPreferences.keepAudioAfterTranscription
        )
    }
}
