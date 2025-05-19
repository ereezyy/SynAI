package com.example.synapseai.data.repository

import com.example.synapseai.data.model.ActivityType
import com.example.synapseai.data.model.AnalyticsEvent
import com.example.synapseai.data.model.AppStatistics
import com.example.synapseai.data.model.ErrorLog
import com.example.synapseai.data.model.EventType
import com.example.synapseai.data.model.FeatureUsage
import com.example.synapseai.data.model.MeetingStatistics
import com.example.synapseai.data.model.MilestoneType
import com.example.synapseai.data.model.PerformanceMetric
import com.example.synapseai.data.model.PerformanceMetricType
import com.example.synapseai.data.model.TimeSeriesData
import com.example.synapseai.data.model.UserActivityMetrics
import com.example.synapseai.data.model.UserSession
import com.example.synapseai.data.model.UsageMilestone
import com.example.synapseai.util.Result
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for tracking and analyzing app usage.
 */
interface AnalyticsRepository {
    /**
     * Track an analytics event.
     * @param eventType The type of event to track
     * @param eventDetails Optional details about the event
     * @param meetingId Optional meeting ID associated with the event
     * @param duration Optional duration of the event in milliseconds
     * @param metadata Optional additional metadata about the event
     * @return Result containing the created event or an error
     */
    suspend fun trackEvent(
        eventType: EventType,
        eventDetails: String? = null,
        meetingId: Long? = null,
        duration: Long? = null,
        metadata: Map<String, String> = emptyMap()
    ): Result<AnalyticsEvent>

    /**
     * Start a new user session.
     * @param deviceInfo Optional device information
     * @param appVersion Optional app version
     * @return Result containing the created session or an error
     */
    suspend fun startSession(
        deviceInfo: String? = null,
        appVersion: String? = null
    ): Result<UserSession>

    /**
     * End a user session.
     * @param sessionId The ID of the session to end
     * @return Result containing the updated session or an error
     */
    suspend fun endSession(sessionId: String): Result<UserSession>

    /**
     * Record a screen view.
     * @param screenName The name of the screen being viewed
     * @param sessionId Optional session ID (if not provided, the current active session will be used)
     * @return Result containing the created event or an error
     */
    suspend fun trackScreenView(
        screenName: String,
        sessionId: String? = null
    ): Result<AnalyticsEvent>

    /**
     * Start tracking feature usage.
     * @param featureName The name of the feature being used
     * @param sessionId Optional session ID (if not provided, the current active session will be used)
     * @param metadata Optional additional metadata about the feature usage
     * @return Result containing the created feature usage or an error
     */
    suspend fun startFeatureUsage(
        featureName: String,
        sessionId: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Result<FeatureUsage>

    /**
     * End tracking feature usage.
     * @param featureUsageId The ID of the feature usage to end
     * @return Result containing the updated feature usage or an error
     */
    suspend fun endFeatureUsage(featureUsageId: String): Result<FeatureUsage>

    /**
     * Track a performance metric.
     * @param metricType The type of performance metric
     * @param value The value of the metric
     * @param deviceInfo Optional device information
     * @param appVersion Optional app version
     * @param metadata Optional additional metadata about the metric
     * @return Result containing the created metric or an error
     */
    suspend fun trackPerformanceMetric(
        metricType: PerformanceMetricType,
        value: Float,
        deviceInfo: String? = null,
        appVersion: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Result<PerformanceMetric>

    /**
     * Log an error.
     * @param errorType The type of error
     * @param errorMessage The error message
     * @param stackTrace Optional stack trace
     * @param deviceInfo Optional device information
     * @param appVersion Optional app version
     * @param screenName Optional screen name where the error occurred
     * @param metadata Optional additional metadata about the error
     * @return Result containing the created error log or an error
     */
    suspend fun logError(
        errorType: String,
        errorMessage: String,
        stackTrace: String? = null,
        deviceInfo: String? = null,
        appVersion: String? = null,
        screenName: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Result<ErrorLog>

    /**
     * Track a usage milestone.
     * @param milestoneType The type of milestone
     * @param metadata Optional additional metadata about the milestone
     * @return Result containing the created milestone or an error
     */
    suspend fun trackMilestone(
        milestoneType: MilestoneType,
        metadata: Map<String, String> = emptyMap()
    ): Result<UsageMilestone>

    /**
     * Get app statistics for a given date range.
     * @param startDate Optional start date (if not provided, all-time statistics will be returned)
     * @param endDate Optional end date (if not provided, statistics up to the current date will be returned)
     * @return Result containing the app statistics or an error
     */
    suspend fun getAppStatistics(
        startDate: Date? = null,
        endDate: Date? = null
    ): Result<AppStatistics>

    /**
     * Get user activity metrics.
     * @param userId Optional user ID (if not provided, metrics for the current user will be returned)
     * @param startDate Optional start date (if not provided, all-time metrics will be returned)
     * @param endDate Optional end date (if not provided, metrics up to the current date will be returned)
     * @return Result containing the user activity metrics or an error
     */
    suspend fun getUserActivityMetrics(
        userId: String? = null,
        startDate: Date? = null,
        endDate: Date? = null
    ): Result<UserActivityMetrics>

    /**
     * Get meeting statistics.
     * @param meetingId The ID of the meeting
     * @return Result containing the meeting statistics or an error
     */
    suspend fun getMeetingStatistics(meetingId: Long): Result<MeetingStatistics>

    /**
     * Get time series data for a specific metric.
     * @param metricType The type of metric to get time series data for
     * @param timeRange The time range to get data for (daily, weekly, monthly, yearly)
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @param filter Optional filter criteria
     * @return Result containing the time series data or an error
     */
    suspend fun getTimeSeriesData(
        metricType: String,
        timeRange: TimeRange,
        startDate: Date? = null,
        endDate: Date? = null,
        filter: Map<String, String> = emptyMap()
    ): Result<TimeSeriesData>

    /**
     * Get recent events.
     * @param eventTypes Optional list of event types to filter by
     * @param limit Maximum number of events to return
     * @param offset Pagination offset
     * @return Result containing a list of events or an error
     */
    suspend fun getRecentEvents(
        eventTypes: List<EventType>? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<AnalyticsEvent>>

    /**
     * Get recent errors.
     * @param limit Maximum number of errors to return
     * @param offset Pagination offset
     * @return Result containing a list of error logs or an error
     */
    suspend fun getRecentErrors(
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<ErrorLog>>

    /**
     * Get most active users.
     * @param limit Maximum number of users to return
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @return Result containing a list of user IDs and activity counts or an error
     */
    suspend fun getMostActiveUsers(
        limit: Int = 10,
        startDate: Date? = null,
        endDate: Date? = null
    ): Result<List<Pair<String, Int>>>

    /**
     * Get most used features.
     * @param limit Maximum number of features to return
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @return Result containing a list of feature names and usage counts or an error
     */
    suspend fun getMostUsedFeatures(
        limit: Int = 10,
        startDate: Date? = null,
        endDate: Date? = null
    ): Result<List<Pair<String, Int>>>

    /**
     * Get usage trends.
     * @param metric The metric to get trends for (e.g., "active_users", "meeting_count", "recording_time")
     * @param timeRange The time range to get trends for (daily, weekly, monthly, yearly)
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @return Result containing the time series data or an error
     */
    suspend fun getUsageTrends(
        metric: String,
        timeRange: TimeRange,
        startDate: Date? = null,
        endDate: Date? = null
    ): Result<TimeSeriesData>

    /**
     * Get performance trends.
     * @param metricType The type of performance metric to get trends for
     * @param timeRange The time range to get trends for (daily, weekly, monthly, yearly)
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @return Result containing the time series data or an error
     */
    suspend fun getPerformanceTrends(
        metricType: PerformanceMetricType,
        timeRange: TimeRange,
        startDate: Date? = null,
        endDate: Date? = null
    ): Result<TimeSeriesData>

    /**
     * Export analytics data.
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @param format The format to export data in (e.g., "csv", "json")
     * @return Result containing the exported data as a string or an error
     */
    suspend fun exportAnalyticsData(
        startDate: Date? = null,
        endDate: Date? = null,
        format: String = "json"
    ): Result<String>

    /**
     * Clear all analytics data.
     * @return Result indicating success or an error
     */
    suspend fun clearAllData(): Result<Boolean>
}

/**
 * Time range options for analytics data.
 */
enum class TimeRange {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
