package com.example.synapseai.data.repository

import com.example.synapseai.data.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for calendar operations.
 */
interface CalendarRepository {
    /**
     * Get a list of available calendars on the device.
     * @return A flow of pairs containing calendar ID and name.
     */
    fun getAvailableCalendars(): Flow<List<Pair<Long, String>>>

    /**
     * Get events from the device calendar within a date range.
     * @param startDate The start date for the query.
     * @param endDate The end date for the query.
     * @return A flow of calendar events.
     */
    fun getCalendarEvents(startDate: Date, endDate: Date): Flow<List<CalendarEvent>>

    /**
     * Get a specific calendar event by ID.
     * @param eventId The ID of the event on the device calendar.
     * @return The calendar event or null if not found.
     */
    suspend fun getCalendarEventById(eventId: Long): CalendarEvent?

    /**
     * Create a new calendar event.
     * @param calendarEvent The event to create.
     * @return The ID of the created event, or null if creation failed.
     */
    suspend fun createCalendarEvent(calendarEvent: CalendarEvent): Long?

    /**
     * Update an existing calendar event.
     * @param calendarEvent The event to update.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateCalendarEvent(calendarEvent: CalendarEvent): Boolean

    /**
     * Delete a calendar event.
     * @param eventId The ID of the event to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    suspend fun deleteCalendarEvent(eventId: Long): Boolean

    /**
     * Link a meeting with a calendar event.
     * @param meetingId The ID of the meeting.
     * @param eventId The ID of the calendar event.
     * @return True if the linking was successful, false otherwise.
     */
    suspend fun linkMeetingWithCalendarEvent(meetingId: Long, eventId: Long): Boolean

    /**
     * Get calendar events linked to a meeting.
     * @param meetingId The ID of the meeting.
     * @return A flow of calendar events linked to the meeting.
     */
    fun getCalendarEventsForMeeting(meetingId: Long): Flow<List<CalendarEvent>>
}
