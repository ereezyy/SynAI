package com.example.synapseai

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.synapseai.firebase.EnhancedCrashReporting
import com.example.synapseai.firebase.FirebaseManager
import com.example.synapseai.firebase.NetworkAwareFirebaseController
import com.example.synapseai.firebase.RemoteConfigManager
import com.example.synapseai.util.BuildConfigValidator
import com.example.synapseai.worker.WorkManagerInitializer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Main application class for SynapseAI.
 * Initializes app components and performs validation checks.
 */
@HiltAndroidApp
class SynapseAIApplication : Application() {

    companion object {
        private const val TAG = "SynapseAI-Application"

        // Flag to determine if this is a production build
        val IS_PRODUCTION = !BuildConfig.DEBUG
    }

    // Firebase services injected by Hilt
    @Inject
    lateinit var firebaseManager: FirebaseManager

    @Inject
    lateinit var networkAwareFirebaseController: NetworkAwareFirebaseController

    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    @Inject
    lateinit var enhancedCrashReporting: EnhancedCrashReporting

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Set up logging - use Timber in debug and controlled logging in production
        setupLogging()

        // Log app initialization
        Timber.i("SynapseAI Application initializing")

        // Initialize WorkManager with Hilt
        WorkManagerInitializer.initialize(applicationContext, workerFactory)
        Timber.d("WorkManager initialized with Hilt integration")

        // Initialize Firebase properly for production/debug environment
        initializeFirebase()

        // Validate build configuration
        BuildConfigValidator.validateAndLogBuildConfig(this)

        // Run additional deployment readiness checks if not in production
        if (!IS_PRODUCTION) {
            validateDeploymentReadiness()
        }
    }

    /**
     * Set up logging appropriately for production vs debug builds
     */
    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            // Use Timber for debug builds with full logging
            Timber.plant(Timber.DebugTree())
        } else {
            // For production, only log to Crashlytics and limit verbosity
            enhancedCrashReporting.initialize()
            Timber.plant(EnhancedCrashReporting.createCrashlyticsTree(firebaseManager.getCrashlytics()!!))
        }
    }

    /**
     * Initialize Firebase services with appropriate configuration for
     * production vs debug environments
     */
    private fun initializeFirebase() {
        try {
            Timber.d("Initializing Firebase services")

            // Initialize core Firebase services
            firebaseManager.initialize(
                enableAnalytics = true,
                enableCrashlytics = true,
                enableMessaging = true
            )

            // Configure remote config with appropriate fetch intervals
            remoteConfigManager.initialize(
                // In production, use the default 12-hour minimum fetch interval
                // In debug, use a 0-second interval for immediate updates
                // This is already handled in RemoteConfigManager via isDebugBuild()
                developerModeEnabled = !IS_PRODUCTION
            )

            // Initialize network-aware controller to optimize Firebase for different networks
            networkAwareFirebaseController.initialize()

            // Configure additional device info for improved crash reports
            if (IS_PRODUCTION) {
                val deviceInfo = mapOf(
                    "device_model" to "${Build.MANUFACTURER} ${Build.MODEL}",
                    "android_version" to Build.VERSION.RELEASE,
                    "app_version" to BuildConfig.VERSION_NAME
                )

                firebaseManager.getCrashlytics()?.let { crashlytics ->
                    deviceInfo.forEach { (key, value) ->
                        crashlytics.setCustomKey(key, value)
                    }
                }
            }

            Timber.i("Firebase services initialized successfully for ${if (IS_PRODUCTION) "PRODUCTION" else "DEBUG"} environment")
        } catch (e: Exception) {
            // Log the error but don't crash the app
            Timber.e(e, "Error initializing Firebase services")
            Log.e(TAG, "Error initializing Firebase: ${e.message}", e)
        }
    }

    /**
     * Validate deployment readiness by checking for common issues.
     * This is for validation purposes only and logs issues that need to be fixed.
     * Only runs in debug builds.
     */
    private fun validateDeploymentReadiness() {
        // Consolidate all deployment readiness logging in one place for easy analysis
        Timber.i("Performing deployment readiness validation checks")

        // Log PDF Export issue
        Timber.e("VALIDATION ISSUE: PDF Export functionality appears to be incomplete or commented out")
        Timber.w("PDF export implementation in ExportImportRepositoryImpl.kt needs to be completed")

        // Log Release Configuration issue (also logged by BuildConfigValidator)
        Timber.e("VALIDATION ISSUE: Release build configuration requires attention")
        Timber.w("minifyEnabled is set to true but ProGuard rules may need review")

        // Log Offline Sync issue
        Timber.e("VALIDATION ISSUE: Offline synchronization uses simulated operations")
        Timber.w("SyncWorker.kt contains placeholder implementations that need real API integration")

        // Log Firebase validation issue
        Timber.i("Firebase configuration validation:")
        if (!::firebaseManager.isInitialized) {
            Timber.e("VALIDATION ISSUE: FirebaseManager is not initialized")
        } else {
            Timber.i("- Firebase Analytics: ${if (firebaseManager.getAnalytics() != null) "ENABLED" else "DISABLED"}")
            Timber.i("- Firebase Crashlytics: ${if (firebaseManager.getCrashlytics() != null) "ENABLED" else "DISABLED"}")
            Timber.i("- Firebase Messaging: ${if (firebaseManager.getMessaging() != null) "ENABLED" else "DISABLED"}")
        }
    }
}
