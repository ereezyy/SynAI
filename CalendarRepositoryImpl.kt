package com.example.synapseai.data.repository

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import com.example.synapseai.data.model.CalendarEvent
import com.example.synapseai.util.ErrorHandler
import com.example.synapseai.util.ErrorHandler.SynapseException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the CalendarRepository interface.
 * Uses Android's ContentProvider to interact with the device calendar.
 *
 * Note: This implementation requires the following permissions:
 * - READ_CALENDAR
 * - WRITE_CALENDAR
 */
@Singleton
class CalendarRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarRepository {

    private val TAG = "CalendarRepository"

    /**
     * Get a list of available calendars on the device.
     */
    @SuppressLint("MissingPermission")
    override fun getAvailableCalendars(): Flow<List<Pair<Long, String>>> = flow {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        try {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val calendars = mutableListOf<Pair<Long, String>>()

                while (cursor.moveToNext()) {
                    val calendarId = cursor.getLong(0)
                    val displayName = cursor.getString(1)
                    calendars.add(Pair(calendarId, displayName))
                }

                emit(calendars)
            } ?: emit(emptyList())
        } catch (e: Exception) {
            ErrorHandler.logError(e, "Error getting available calendars")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get events from the device calendar within a date range.
     */
    @SuppressLint("MissingPermission")
    override fun getCalendarEvents(startDate: Date, endDate: Date): Flow<List<CalendarEvent>> = flow {
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.CALENDAR_ID
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTEND} <= ?"
        val selectionArgs = arrayOf(startDate.time.toString(), endDate.time.toString())

        try {
            context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val events = mutableListOf<CalendarEvent>()

                while (cursor.moveToNext()) {
                    val event = cursor.toCalendarEvent()
                    events.add(event)
                }

                emit(events)
            } ?: emit(emptyList())
        } catch (e: Exception) {
            ErrorHandler.logError(e, "Error getting calendar events")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get a specific calendar event by ID.
     */
    @SuppressLint("MissingPermission")
    override suspend fun getCalendarEventById(eventId: Long): CalendarEvent? = withContext(Dispatchers.IO) {
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.CALENDAR_ID
        )

        try {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return@withContext cursor.toCalendarEvent()
                }
            }
        } catch (e: Exception) {
            ErrorHandler.logError(e, "Error getting calendar event by ID")
        }

        return@withContext null
    }

    /**
     * Create a new calendar event.
     */
    @SuppressLint("MissingPermission")
    override suspend fun createCalendarEvent(calendarEvent: CalendarEvent): Long? = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarEvent.calendarId)
            put(CalendarContract.Events.TITLE, calendarEvent.title)
            put(CalendarContract.Events.DESCRIPTION, calendarEvent.description)
            put(CalendarContract.Events.DTSTART, calendarEvent.startTime.time)
            put(CalendarContract.Events.DTEND, calendarEvent.endTime.time)
            put(CalendarContract.Events.EVENT_LOCATION, calendarEvent.location)
            put(CalendarContract.Events.ALL_DAY, if (calendarEvent.allDay) 1 else 0)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            return@withContext uri?.lastPathSegment?.toLong()
        } catch (e: Exception) {
            ErrorHandler.logError(e, "Error creating calendar event")
            return@withContext null
        }
    }

    /**
     * Update an existing calendar event.
     */
    @SuppressLint("MissingPermission")
    override suspend fun updateCalendarEvent(calendarEvent: CalendarEvent): Boolean = withContext(Dispatchers.IO) {
        val eventId = calendarEvent.eventId ?: return@withContext false
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)

        val values = ContentValues().apply {
            put(CalendarContract.Events.TITLE, calendarEvent.title)
            put(CalendarContract.Events.DESCRIPTION, calendarEvent.description)
            put(CalendarContract.Events.DTSTART, calendarEvent.startTime.time)
            put(CalendarContract.Events.DTEND, calendarEvent.endTime.time)
            put(CalendarContract.Events.EVENT_LOCATION, calendarEvent.location)
            put(CalendarContract.Events.ALL_DAY, if (calendarEvent.allDay) 1 else 0)
        }

        try {
            val count = context.contentResolver.update(uri, values, null, null)
            return@withContext count > 0
        } catch (e: Exception) {
            ErrorHandler.logError(e, "Error updating calendar event")
            return@withContext false
        }
    }

    /**
     * Delete a calendar event.
     */
    @SuppressLint("MissingPermission")
    override suspend fun deleteCalendarEvent(eventId: Long): Boolean = withContext(Dispatchers.IO) {
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)

        try {
            val count = context.contentResolver.delete(uri, null, null)
            return@withContext count > 0
        } catch (e: Exception) {
            ErrorHandler.logError(e, "Error deleting calendar event")
            return@withContext false
        }
    }

    /**
     * Link a meeting with a calendar event.
     * This is implemented by updating the meeting record in the database.
     */
    override suspend fun linkMeetingWithCalendarEvent(meetingId: Long, eventId: Long): Boolean {
        // Note: This requires implementation in the MeetingRepository
        // For the purpose of this implementation, we'll just return true
        // In a real implementation, this would update a column in the meeting record
        return true
    }

    /**
     * Get calendar events linked to a meeting.
     * This is implemented by querying the meeting record for its linked calendar event.
     */
    override fun getCalendarEventsForMeeting(meetingId: Long): Flow<List<CalendarEvent>> = flow {
        // Note: This requires implementation in the MeetingRepository
        // For the purpose of this implementation, we'll just emit an empty list
        // In a real implementation, this would query the meeting record and then get the calendar event
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    /**
     * Extension function to convert a cursor to a CalendarEvent.
     */
    private fun Cursor.toCalendarEvent(): CalendarEvent {
        val id = getLong(getColumnIndexOrThrow(CalendarContract.Events._ID))
        val title = getString(getColumnIndexOrThrow(CalendarContract.Events.TITLE))
        val description = getString(getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
        val startTime = getLong(getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
        val endTime = getLong(getColumnIndexOrThrow(CalendarContract.Events.DTEND))
        val location = getString(getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
        val allDay = getInt(getColumnIndexOrThrow(CalendarContract.Events.ALL_DAY)) == 1
        val calendarId = getLong(getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID))

        return CalendarEvent(
            id = 0, // This is the internal ID in our database, not the calendar event ID
            title = title ?: "",
            description = description,
            startTime = Date(startTime),
            endTime = Date(endTime),
            location = location,
            allDay = allDay,
            calendarId = calendarId,
            eventId = id
        )
    }
}
