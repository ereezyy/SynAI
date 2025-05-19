package com.example.synapseai.firebase

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.synapseai.MainActivity
import com.example.synapseai.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screenshot tests for notifications to ensure visual consistency across app versions.
 * This test class captures screenshots of various notification types to track visual
 * regression and ensure that notifications appear correctly on different devices.
 */
@RunWith(AndroidJUnit4::class)
class NotificationScreenshotTest {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_TIMEOUT = 5000L // 5 seconds
        private const val SCREENSHOT_DIRECTORY = "notification_screenshots"
    }

    private lateinit var uiDevice: UiDevice
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var screenshotDirectory: File

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

        // Create screenshot directory
        screenshotDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            SCREENSHOT_DIRECTORY
        ).apply {
            if (!exists()) mkdirs()
        }
    }

    @After
    fun tearDown() {
        // Clear notifications
        notificationManager.cancelAll()
    }

    @Test
    fun testMeetingReminderNotificationScreenshot() {
        // Create and display a meeting reminder notification
        val notification = createNotification(
            title = "Meeting Reminder",
            content = "You have an upcoming meeting with John Doe at 2:30 PM",
            type = SynapseFirebaseMessagingService.TYPE_MEETING_REMINDER
        )
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Wait for notification to appear
        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Meeting Reminder")), NOTIFICATION_TIMEOUT)

        // Take screenshot of notification
        val screenshotFile = generateScreenshotFilename("meeting_reminder_notification")
        takeScreenshot(screenshotFile)
    }

    @Test
    fun testTranscriptionCompleteNotificationScreenshot() {
        // Create and display a transcription complete notification
        val notification = createNotification(
            title = "Transcription Complete",
            content = "Your meeting transcription for 'Q1 Planning' is ready to view",
            type = SynapseFirebaseMessagingService.TYPE_TRANSCRIPTION_COMPLETE
        )
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Wait for notification to appear
        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Transcription Complete")), NOTIFICATION_TIMEOUT)

        // Take screenshot of notification
        val screenshotFile = generateScreenshotFilename("transcription_complete_notification")
        takeScreenshot(screenshotFile)
    }

    @Test
    fun testSummaryCompleteNotificationScreenshot() {
        // Create and display a summary complete notification
        val notification = createNotification(
            title = "Summary Complete",
            content = "AI summary for 'Team Standup' is now available",
            type = SynapseFirebaseMessagingService.TYPE_SUMMARY_COMPLETE
        )
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Wait for notification to appear
        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Summary Complete")), NOTIFICATION_TIMEOUT)

        // Take screenshot of notification
        val screenshotFile = generateScreenshotFilename("summary_complete_notification")
        takeScreenshot(screenshotFile)
    }

    @Test
    fun testEmailDraftNotificationScreenshot() {
        // Create and display an email draft notification
        val notification = createNotification(
            title = "Email Draft Ready",
            content = "Your follow-up email draft for 'Client Meeting' is ready to review",
            type = SynapseFirebaseMessagingService.TYPE_EMAIL_DRAFT_READY
        )
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Wait for notification to appear
        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Email Draft Ready")), NOTIFICATION_TIMEOUT)

        // Take screenshot of notification
        val screenshotFile = generateScreenshotFilename("email_draft_notification")
        takeScreenshot(screenshotFile)
    }

    @Test
    fun testImportantActionItemNotificationScreenshot() {
        // Create and display an important action item notification
        val notification = createNotification(
            title = "Important Action Item",
            content = "High priority: Update project timeline by tomorrow",
            type = SynapseFirebaseMessagingService.TYPE_IMPORTANT_ACTION_ITEM
        )
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Wait for notification to appear
        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Important Action Item")), NOTIFICATION_TIMEOUT)

        // Take screenshot of notification
        val screenshotFile = generateScreenshotFilename("important_action_item_notification")
        takeScreenshot(screenshotFile)
    }

    /**
     * Create a notification with the specified properties for testing
     */
    private fun createNotification(
        title: String,
        content: String,
        type: String
    ): Notification {
        val channelId = "synapse_notifications"

        // Build notification
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
    }

    /**
     * Generate a unique filename for a screenshot
     */
    private fun generateScreenshotFilename(prefix: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(screenshotDirectory, "${prefix}_${timestamp}.png")
    }

    /**
     * Take a screenshot of the current screen and save it to a file
     */
    private fun takeScreenshot(file: File) {
        try {
            // Capture screenshot
            val screenshot = uiDevice.takeScreenshot()

            // Save to file
            FileOutputStream(file).use { out ->
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            println("Screenshot saved to: ${file.absolutePath}")
        } catch (e: Exception) {
            println("Failed to take screenshot: ${e.message}")
        }
    }
}
