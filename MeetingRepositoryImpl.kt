package com.example.synapseai.data.repository

import com.example.synapseai.data.database.dao.MeetingDao
import com.example.synapseai.data.database.entity.MeetingEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the MeetingRepository interface.
 */
@Singleton
class MeetingRepositoryImpl @Inject constructor(
    private val meetingDao: MeetingDao
) : MeetingRepository {

    override suspend fun createMeeting(
        title: String,
        date: Date,
        durationMs: Long
    ): Long {
        val meeting = MeetingEntity(
            title = title,
            date = date,
            durationMs = durationMs,
            recordingFilePath = null,
            transcriptFilePath = null,
            status = MeetingStatus.PENDING.name,
            keyPoints = null,
            actionItems = null,
            isSummarized = false
        )
        return meetingDao.insertMeeting(meeting)
    }

    override suspend fun getMeetingById(id: Long): MeetingEntity? {
        return meetingDao.getMeetingById(id)
    }

    override fun getAllMeetings(): Flow<List<MeetingEntity>> {
        return meetingDao.getAllMeetings()
    }

    override fun getRecentMeetings(): Flow<List<MeetingEntity>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7) // Last 7 days
        return meetingDao.getRecentMeetings(calendar.time)
    }

    override suspend fun updateRecordingFilePath(meetingId: Long, filePath: String): Boolean {
        val meeting = meetingDao.getMeetingById(meetingId) ?: return false
        val updatedMeeting = meeting.copy(
            recordingFilePath = filePath,
            status = MeetingStatus.RECORDING_COMPLETED.name,
            updatedAt = Date()
        )
        meetingDao.updateMeeting(updatedMeeting)
        return true
    }

    override suspend fun updateTranscriptFilePath(meetingId: Long, filePath: String): Boolean {
        val meeting = meetingDao.getMeetingById(meetingId) ?: return false
        val updatedMeeting = meeting.copy(
            transcriptFilePath = filePath,
            status = MeetingStatus.TRANSCRIPTION_COMPLETED.name,
            updatedAt = Date()
        )
        meetingDao.updateMeeting(updatedMeeting)
        return true
    }

    override suspend fun updateMeetingStatus(meetingId: Long, status: String): Boolean {
        val meeting = meetingDao.getMeetingById(meetingId) ?: return false
        val updatedMeeting = meeting.copy(
            status = status,
            updatedAt = Date()
        )
        meetingDao.updateMeeting(updatedMeeting)
        return true
    }

    override suspend fun updateMeetingSummary(
        meetingId: Long,
        keyPoints: List<String>,
        actionItems: List<String>
    ): Boolean {
        val meeting = meetingDao.getMeetingById(meetingId) ?: return false
        val updatedMeeting = meeting.copy(
            keyPoints = keyPoints,
            actionItems = actionItems,
            status = MeetingStatus.SUMMARIZATION_COMPLETED.name,
            isSummarized = true,
            updatedAt = Date()
        )
        meetingDao.updateMeeting(updatedMeeting)
        return true
    }

    override suspend fun deleteMeeting(meetingId: Long): Boolean {
        val meeting = meetingDao.getMeetingById(meetingId) ?: return false
        meetingDao.deleteMeeting(meeting)
        return true
    }

    override fun searchMeetings(query: String): Flow<List<MeetingEntity>> {
        return meetingDao.searchMeetings(query)
    }
}

/**
 * Enum representing the possible statuses of a meeting.
 */
enum class MeetingStatus {
    PENDING,
    RECORDING,
    RECORDING_COMPLETED,
    TRANSCRIBING,
    TRANSCRIPTION_COMPLETED,
    SUMMARIZING,
    SUMMARIZATION_COMPLETED,
    ERROR
}
