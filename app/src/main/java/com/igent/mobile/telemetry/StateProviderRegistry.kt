package com.igent.mobile.telemetry

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StateProviderRegistry @Inject constructor() {
    private val providers = ConcurrentHashMap<String, () -> Map<String, Any?>>()

    fun registerProvider(name: String, provider: () -> Map<String, Any?>) {
        require(name.isNotBlank()) { "Provider name cannot be blank" }
        providers[name] = provider
    }

    fun unregisterProvider(name: String) {
        providers.remove(name)
    }

    fun getProvider(name: String): (() -> Map<String, Any?>)? {
        return providers[name]
    }

    fun getRegisteredProviderNames(): Set<String> {
        return providers.keys.toSet()
    }

    fun isAllowed(name: String): Boolean {
        return providers.containsKey(name)
    }
}
