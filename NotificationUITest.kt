package com.example.synapseai.firebase

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.synapseai.MainActivity
import com.example.synapseai.R
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * UI test for notifications to verify that they display correctly
 * and that tapping on them navigates to the correct screen.
 */
@RunWith(AndroidJUnit4::class)
class NotificationUITest {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_TIMEOUT = 5000L // 5 seconds
    }

    private lateinit var uiDevice: UiDevice
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        // Initialize UiDevice instance
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from home screen
        uiDevice.pressHome()

        // Get context
        context = ApplicationProvider.getApplicationContext()

        // Get notification manager
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Clear any existing notifications
        notificationManager.cancelAll()
    }

    @After
    fun tearDown() {
        // Clear notifications
        notificationManager.cancelAll()
    }

    @Test
    fun testMeetingReminderNotification() {
        // Create and display a meeting reminder notification
        val notification = createNotification(
            title = "Meeting Reminder",
            content = "You have an upcoming meeting",
            type = SynapseFirebaseMessagingService.TYPE_MEETING_REMINDER
        )
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Wait for notification to appear
        val notificationShown = uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Meeting Reminder")), NOTIFICATION_TIMEOUT)

        // Verify notification content
        val title = uiDevice.findObject(By.text("Meeting Reminder"))
        val text = uiDevice.findObject(By.text("You have an upcoming meeting"))
        assert(title != null) { "Notification title not found" }
        assert(text != null) { "Notification text not found" }

        // Click on the notification
        title.click()

        // Verify navigation to correct screen
        // This assumes we have a meeting details screen that shows when this notification is clicked
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), NOTIFICATION_TIMEOUT)
        Espresso.onView(ViewMatchers.withId(R.id.meeting_detail_container))
            .check { view, noViewFoundException ->
                assert(view != null) { "Meeting detail view not found after clicking notification" }
            }
    }

    @Test
    fun testTranscriptionCompleteNotification() {
        // Create and display a transcription complete notification
        val notification = createNotification(
            title = "Transcription Complete",
            content = "Your meeting transcription is ready",
            type = SynapseFirebaseMessagingService.TYPE_TRANSCRIPTION_COMPLETE
        )
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Wait for notification to appear
        val notificationShown = uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Transcription Complete")), NOTIFICATION_TIMEOUT)

        // Verify notification content
        val title = uiDevice.findObject(By.text("Transcription Complete"))
        val text = uiDevice.findObject(By.text("Your meeting transcription is ready"))
        assert(title != null) { "Notification title not found" }
        assert(text != null) { "Notification text not found" }

        // Click on the notification
        title.click()

        // Verify navigation to correct screen
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), NOTIFICATION_TIMEOUT)
        Espresso.onView(ViewMatchers.withId(R.id.transcription_view))
            .check { view, noViewFoundException ->
                assert(view != null) { "Transcription view not found after clicking notification" }
            }
    }

    /**
     * Create a notification with the specified properties for testing.
     */
    private fun createNotification(
        title: String,
        content: String,
        type: String
    ): Notification {
        val channelId = "synapse_notifications"

        // Create notification intent based on type
        val intent = when (type) {
            SynapseFirebaseMessagingService.TYPE_MEETING_REMINDER -> {
                activityRule.activity.intent.apply {
                    putExtra("notification_type", type)
                    putExtra("meeting_id", 123L)
                }
            }
            SynapseFirebaseMessagingService.TYPE_TRANSCRIPTION_COMPLETE -> {
                activityRule.activity.intent.apply {
                    putExtra("notification_type", type)
                    putExtra("meeting_id", 123L)
                }
            }
            else -> activityRule.activity.intent
        }

        // Create pending intent
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
