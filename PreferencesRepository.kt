package com.example.synapseai.data.repository

import com.example.synapseai.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user preferences.
 */
interface PreferencesRepository {
    /**
     * Get the current user preferences as a Flow.
     * @return A flow that emits the current user preferences whenever they change.
     */
    fun getUserPreferences(): Flow<UserPreferences>

    /**
     * Update the notification preferences.
     * @param enableMeetingReminders Whether to enable meeting reminder notifications.
     * @param enableTranscriptionNotifications Whether to enable transcription completion notifications.
     * @param enableSummarizationNotifications Whether to enable summarization completion notifications.
     */
    suspend fun updateNotificationPreferences(
        enableMeetingReminders: Boolean? = null,
        enableTranscriptionNotifications: Boolean? = null,
        enableSummarizationNotifications: Boolean? = null
    )

    /**
     * Update the default reminder times.
     * @param reminderTimes List of reminder times in minutes before the meeting.
     */
    suspend fun updateDefaultReminderTimes(reminderTimes: List<Int>)

    /**
     * Update theme preferences.
     * @param useDarkMode Whether to use dark mode.
     * @param useSystemTheme Whether to use the system theme.
     * @param primaryColor Primary color for the app theme (as a hex string).
     */
    suspend fun updateThemePreferences(
        useDarkMode: Boolean? = null,
        useSystemTheme: Boolean? = null,
        primaryColor: String? = null
    )

    /**
     * Update various other preferences.
     * @param autoStartRecording Whether to automatically start recording when joining a meeting.
     * @param keepAudioAfterTranscription Whether to keep audio files after transcription.
     */
    suspend fun updateOtherPreferences(
        autoStartRecording: Boolean? = null,
        keepAudioAfterTranscription: Boolean? = null
    )

    /**
     * Reset all preferences to default values.
     */
    suspend fun resetToDefaults()
}
