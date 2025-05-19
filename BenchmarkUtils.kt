package com.example.synapseai.firebase

import androidx.benchmark.junit4.BenchmarkRule

/**
 * Utility functions for Firebase performance benchmarking
 */

/**
 * Extension function for BenchmarkRule.measureRepeated that allows timing to be explicitly
 * enabled inside a block of code. This is useful for benchmarking operations where setup
 * or cleanup should not be included in the measurement.
 *
 * @param block The code block to run with timing enabled
 * @return The result of the block execution
 */
inline fun <T> BenchmarkRule.State.runWithTimingAllowed(block: () -> T): T {
    this.resumeTiming()
    try {
        return block()
    } finally {
        this.pauseTiming()
    }
}

/**
 * Simple utility function to prevent compiler optimizations from removing code
 * that doesn't have visible side effects.
 *
 * @param value The value to prevent from being optimized away
 */
@Suppress("UNUSED_PARAMETER")
fun preventOptimization(value: Any?) {
    // Do nothing, this function simply prevents the compiler from
    // optimizing away code that has no side effects
}
