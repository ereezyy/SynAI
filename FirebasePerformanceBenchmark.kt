package com.example.synapseai.firebase

import android.content.Context
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

/**
 * Performance benchmarks for Firebase operations.
 *
 * These benchmarks establish baseline performance metrics for common Firebase operations
 * to ensure that performance does not regress over time and to identify optimization opportunities.
 *
 * Each benchmark measures a specific Firebase operation and reports metrics like execution time,
 * allocations, and iterations per second.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FirebasePerformanceBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var firebaseManager: FirebaseManager

    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    @Inject
    lateinit var networkAwareFirebaseController: NetworkAwareFirebaseController

    @Inject
    lateinit var enhancedAnalyticsTracker: EnhancedAnalyticsTracker

    private lateinit var context: Context
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var remoteConfig: FirebaseRemoteConfig

    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()

        // Initialize Firebase if not already initialized
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        analytics = FirebaseAnalytics.getInstance(context)
        remoteConfig = FirebaseRemoteConfig.getInstance()

        // Initialize our custom managers
        runBlocking {
            remoteConfigManager.initialize()
            enhancedAnalyticsTracker.initialize(true)
            networkAwareFirebaseController.initialize()
        }
    }

    /**
     * Benchmark tracking a simple analytics event
     */
    @Test
    fun benchmarkSimpleAnalyticsEvent() {
        benchmarkRule.measureRepeated {
            val eventParams = HashMap<String, String>().apply {
                put("item_id", "benchmark_item_${Random.nextInt(1000)}")
                put("item_name", "Benchmark Test Item")
                put("item_category", "benchmarks")
            }

            runWithTimingAllowed {
                analytics.logEvent("benchmark_event", analyticsParamsOf(eventParams))
            }
        }
    }

    /**
     * Benchmark enhanced analytics tracking of user action
     */
    @Test
    fun benchmarkEnhancedAnalyticsUserAction() {
        benchmarkRule.measureRepeated {
            runWithTimingAllowed {
                enhancedAnalyticsTracker.trackUserAction(
                    action = "click",
                    category = "benchmark",
                    label = "performance_test_${Random.nextInt(1000)}",
                    value = System.currentTimeMillis(),
                    params = mapOf(
                        "test_param_1" to "value1",
                        "test_param_2" to "value2"
                    )
                )
            }
        }
    }

    /**
     * Benchmark tracking a user journey step
     */
    @Test
    fun benchmarkJourneyStepTracking() {
        val journeyId = "benchmark_journey_${Random.nextInt(1000)}"
        val totalSteps = 5

        benchmarkRule.measureRepeated {
            runWithTimingAllowed {
                val stepNumber = Random.nextInt(1, totalSteps + 1)
                enhancedAnalyticsTracker.trackJourneyStep(
                    journeyId = journeyId,
                    stepName = "Benchmark Step $stepNumber",
                    stepNumber = stepNumber,
                    totalSteps = totalSteps,
                    params = mapOf(
                        "complexity" to "medium",
                        "test_run" to "automated"
                    )
                )
            }
        }
    }

    /**
     * Benchmark fetching remote config
     */
    @Test
    fun benchmarkRemoteConfigFetch() {
        benchmarkRule.measureRepeated {
            val latch = CountDownLatch(1)
            var success = false

            runWithTimingAllowed {
                remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                    success = task.isSuccessful
                    latch.countDown()
                }

                // Wait for completion with timeout
                latch.await(5, TimeUnit.SECONDS)
            }
        }
    }

    /**
     * Benchmark getting boolean value from remote config
     */
    @Test
    fun benchmarkRemoteConfigGetBoolean() {
        // Warm up remote config
        remoteConfig.fetchAndActivate()

        benchmarkRule.measureRepeated {
            runWithTimingAllowed {
                val value = remoteConfigManager.getBoolean(RemoteConfigManager.KEY_ENABLE_NEW_MEETING_UI)
                // Use the value to prevent optimization
                if (value) {
                    // Do nothing, just prevent compiler optimization
                }
            }
        }
    }

    /**
     * Benchmark network state detection
     */
    @Test
    fun benchmarkNetworkStateDetection() {
        benchmarkRule.measureRepeated {
            runWithTimingAllowed {
                val state = networkAwareFirebaseController.networkState.value
                // Use the value to prevent optimization
                if (state == NetworkAwareFirebaseController.NetworkState.WIFI_STRONG) {
                    // Do nothing, just prevent compiler optimization
                }
            }
        }
    }

    /**
     * Benchmark performance metric tracking
     */
    @Test
    fun benchmarkPerformanceMetricTracking() {
        benchmarkRule.measureRepeated {
            runWithTimingAllowed {
                enhancedAnalyticsTracker.trackPerformance(
                    operationType = "benchmark_operation",
                    durationMs = Random.nextLong(100, 500),
                    status = "success",
                    params = mapOf(
                        "complexity" to "high",
                        "memory_used_mb" to "256"
                    )
                )
            }
        }
    }

    /**
     * Benchmark notification event tracking
     */
    @Test
    fun benchmarkNotificationEventTracking() {
        benchmarkRule.measureRepeated {
            val notificationId = "notification_${Random.nextInt(1000)}"

            runWithTimingAllowed {
                enhancedAnalyticsTracker.trackNotificationEvent(
                    notificationType = "benchmark_notification",
                    action = "received",
                    notificationId = notificationId,
                    params = mapOf(
                        "priority" to "high",
                        "channel" to "test"
                    )
                )
            }
        }
    }

    /**
     * Benchmark screen view tracking
     */
    @Test
    fun benchmarkScreenViewTracking() {
        benchmarkRule.measureRepeated {
            runWithTimingAllowed {
                enhancedAnalyticsTracker.trackScreenView(
                    screenName = "BenchmarkScreen",
                    screenClass = "com.example.synapseai.BenchmarkActivity",
                    journeyId = "benchmark_journey_${Random.nextInt(1000)}",
                    params = mapOf(
                        "entry_point" to "direct",
                        "referrer" to "dashboard"
                    )
                )
            }
        }
    }

    /**
     * Helper to convert a Map<String, String> to Bundle for analytics
     */
    private fun analyticsParamsOf(params: Map<String, String>): android.os.Bundle {
        val bundle = android.os.Bundle()
        for ((key, value) in params) {
            bundle.putString(key, value)
        }
        return bundle
    }
}
