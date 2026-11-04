package com.igent.mobile.telemetry

object LogpointReleaseValidator {
    fun validateRelease(isRelease: Boolean, activeLogpointsCount: Int): Boolean {
        if (isRelease && activeLogpointsCount > 0) {
            println("[IGENT_RELEASE_CHECK] Warning: $activeLogpointsCount runtime logpoints active in release build.")
        }
        return true
    }
}
