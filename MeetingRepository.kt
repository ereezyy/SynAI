package com.example.synapseai.data.repository

import com.example.synapseai.data.database.entity.MeetingEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for accessing meeting data.
 */
interface MeetingRepository {

    /**
     * Create a new meeting.
     * @param title The title of the meeting.
     * @param date The date of the meeting.
     * @param durationMs The duration of the meeting in milliseconds.
     * @return The ID of the created meeting.
     */
    suspend fun createMeeting(
        title: String,
        date: Date,
        durationMs: Long = 0
    ): Long

    /**
     * Get a meeting by its ID.
     * @param id The ID of the meeting to retrieve.
     * @return The meeting with the specified ID, or null if not found.
     */
    suspend fun getMeetingById(id: Long): MeetingEntity?

    /**
     * Get all meetings.
     * @return A flow of all meetings.
     */
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    /**
     * Get recent meetings (within the last 7 days).
     * @return A flow of recent meetings.
     */
    fun getRecentMeetings(): Flow<List<MeetingEntity>>

    /**
     * Update the recording file path for a meeting.
     * @param meetingId The ID of the meeting to update.
     * @param filePath The path to the recording file.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateRecordingFilePath(meetingId: Long, filePath: String): Boolean

    /**
     * Update the transcript file path for a meeting.
     * @param meetingId The ID of the meeting to update.
     * @param filePath The path to the transcript file.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateTranscriptFilePath(meetingId: Long, filePath: String): Boolean

    /**
     * Update the status of a meeting.
     * @param meetingId The ID of the meeting to update.
     * @param status The new status.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateMeetingStatus(meetingId: Long, status: String): Boolean

    /**
     * Update the summary information for a meeting.
     * @param meetingId The ID of the meeting to update.
     * @param keyPoints The key points extracted from the meeting.
     * @param actionItems The action items extracted from the meeting.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateMeetingSummary(
        meetingId: Long,
        keyPoints: List<String>,
        actionItems: List<String>
    ): Boolean

    /**
     * Delete a meeting.
     * @param meetingId The ID of the meeting to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    suspend fun deleteMeeting(meetingId: Long): Boolean

    /**
     * Search for meetings by title.
     * @param query The search query.
     * @return A flow of meetings matching the search query.
     */
    fun searchMeetings(query: String): Flow<List<MeetingEntity>>
}
