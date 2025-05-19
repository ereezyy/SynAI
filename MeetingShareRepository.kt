package com.example.synapseai.data.repository

import com.example.synapseai.data.model.ActivityType
import com.example.synapseai.data.model.CollaborationSession
import com.example.synapseai.data.model.InvitationStatus
import com.example.synapseai.data.model.Meeting
import com.example.synapseai.data.model.MeetingInvitation
import com.example.synapseai.data.model.MeetingShare
import com.example.synapseai.data.model.MeetingShareStats
import com.example.synapseai.data.model.MeetingVisibility
import com.example.synapseai.data.model.SessionActivity
import com.example.synapseai.data.model.ShareMeetingResult
import com.example.synapseai.data.model.UserRole
import com.example.synapseai.data.model.UserSummary
import com.example.synapseai.util.Result
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for managing meeting sharing and collaboration.
 */
interface MeetingShareRepository {
    /**
     * Share a meeting with users by email.
     * @param meetingId The ID of the meeting to share
     * @param emails List of emails to share with
     * @param role The role to assign to the users
     * @param message Optional message to include in the invitation
     * @param sendNotification Whether to send email notifications
     * @return Result containing details about the share operation
     */
    suspend fun shareMeetingByEmail(
        meetingId: Long,
        emails: List<String>,
        role: UserRole,
        message: String? = null,
        sendNotification: Boolean = true
    ): Result<ShareMeetingResult>

    /**
     * Share a meeting with specific users.
     * @param meetingId The ID of the meeting to share
     * @param userIds List of user IDs to share with
     * @param role The role to assign to the users
     * @return Result containing details about the share operation
     */
    suspend fun shareMeetingWithUsers(
        meetingId: Long,
        userIds: List<String>,
        role: UserRole
    ): Result<ShareMeetingResult>

    /**
     * Update the sharing role for a user.
     * @param meetingId The ID of the meeting
     * @param userId The ID of the user
     * @param newRole The new role to assign
     * @return Result containing the updated MeetingShare or an error
     */
    suspend fun updateUserRole(
        meetingId: Long,
        userId: String,
        newRole: UserRole
    ): Result<MeetingShare>

    /**
     * Remove a user's access to a meeting.
     * @param meetingId The ID of the meeting
     * @param userId The ID of the user to remove
     * @return Result indicating success or an error
     */
    suspend fun removeUserAccess(
        meetingId: Long,
        userId: String
    ): Result<Boolean>

    /**
     * Get all users who have access to a meeting.
     * @param meetingId The ID of the meeting
     * @return Result containing a list of user summaries with their roles
     */
    suspend fun getMeetingUsers(
        meetingId: Long
    ): Result<List<Pair<UserSummary, UserRole>>>

    /**
     * Get a list of pending invitations for a meeting.
     * @param meetingId The ID of the meeting
     * @return Result containing a list of pending invitations
     */
    suspend fun getPendingInvitations(
        meetingId: Long
    ): Result<List<MeetingInvitation>>

    /**
     * Cancel a pending invitation.
     * @param meetingId The ID of the meeting
     * @param email The email address of the invitation
     * @return Result indicating success or an error
     */
    suspend fun cancelInvitation(
        meetingId: Long,
        email: String
    ): Result<Boolean>

    /**
     * Accept an invitation to a meeting.
     * @param token The invitation token
     * @return Result containing the accepted meeting or an error
     */
    suspend fun acceptInvitation(
        token: String
    ): Result<Meeting>

    /**
     * Decline an invitation to a meeting.
     * @param token The invitation token
     * @return Result indicating success or an error
     */
    suspend fun declineInvitation(
        token: String
    ): Result<Boolean>

    /**
     * Get all invitations for the current user.
     * @param status Optional filter by invitation status
     * @return Flow emitting a list of meeting invitations
     */
    fun getUserInvitations(
        status: InvitationStatus? = null
    ): Flow<List<MeetingInvitation>>

    /**
     * Change the visibility of a meeting.
     * @param meetingId The ID of the meeting
     * @param visibility The new visibility level
     * @return Result containing the updated meeting or an error
     */
    suspend fun changeMeetingVisibility(
        meetingId: Long,
        visibility: MeetingVisibility
    ): Result<Meeting>

    /**
     * Get statistics about sharing for a meeting.
     * @param meetingId The ID of the meeting
     * @return Result containing sharing statistics or an error
     */
    suspend fun getMeetingShareStats(
        meetingId: Long
    ): Result<MeetingShareStats>

    /**
     * Get meetings shared with the current user.
     * @param role Optional filter by user role
     * @return Flow emitting a list of shared meetings
     */
    fun getSharedMeetings(
        role: UserRole? = null
    ): Flow<List<Meeting>>

    /**
     * Record that a user accessed a meeting.
     * @param meetingId The ID of the meeting
     * @param userId The ID of the user
     * @return Result containing the timestamp of the access or an error
     */
    suspend fun recordMeetingAccess(
        meetingId: Long,
        userId: String
    ): Result<Date>

    /**
     * Start a new collaboration session.
     * @param meetingId The ID of the meeting
     * @return Result containing the created session or an error
     */
    suspend fun startCollaborationSession(
        meetingId: Long
    ): Result<CollaborationSession>

    /**
     * Join an existing collaboration session.
     * @param sessionId The ID of the session
     * @return Result containing the session or an error
     */
    suspend fun joinCollaborationSession(
        sessionId: String
    ): Result<CollaborationSession>

    /**
     * Leave a collaboration session.
     * @param sessionId The ID of the session
     * @return Result indicating success or an error
     */
    suspend fun leaveCollaborationSession(
        sessionId: String
    ): Result<Boolean>

    /**
     * End a collaboration session.
     * @param sessionId The ID of the session
     * @return Result indicating success or an error
     */
    suspend fun endCollaborationSession(
        sessionId: String
    ): Result<Boolean>

    /**
     * Record an activity in a collaboration session.
     * @param sessionId The ID of the session
     * @param activityType The type of activity
     * @param details Optional activity details
     * @return Result containing the recorded activity or an error
     */
    suspend fun recordSessionActivity(
        sessionId: String,
        activityType: ActivityType,
        details: String? = null
    ): Result<SessionActivity>

    /**
     * Get all activities in a collaboration session.
     * @param sessionId The ID of the session
     * @return Result containing a list of session activities
     */
    suspend fun getSessionActivities(
        sessionId: String
    ): Result<List<SessionActivity>>

    /**
     * Get the active users in a collaboration session.
     * @param sessionId The ID of the session
     * @return Result containing a list of user summaries
     */
    suspend fun getActiveSessionUsers(
        sessionId: String
    ): Result<List<UserSummary>>

    /**
     * Check if a collaboration session is active.
     * @param meetingId The ID of the meeting
     * @return Result containing true if there is an active session, or an error
     */
    suspend fun hasActiveSession(
        meetingId: Long
    ): Result<Boolean>

    /**
     * Get the active collaboration session for a meeting, if any.
     * @param meetingId The ID of the meeting
     * @return Result containing the active session or null if none exists
     */
    suspend fun getActiveSession(
        meetingId: Long
    ): Result<CollaborationSession?>
}
