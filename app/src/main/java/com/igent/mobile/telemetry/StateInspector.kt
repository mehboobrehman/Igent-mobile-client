package com.igent.mobile.telemetry

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StateInspector @Inject constructor(
    private val registry: StateProviderRegistry
) {
    companion object {
        const val MAX_PAYLOAD_SIZE_BYTES = 64 * 1024 // 64 KB cap
    }

    fun listProviders(): Set<String> {
        return registry.getRegisteredProviderNames()
    }

    fun inspectState(providerName: String): InspectionResult {
        if (!registry.isAllowed(providerName)) {
            return InspectionResult.Error("Provider not allow-listed: $providerName")
        }

        val provider = registry.getProvider(providerName)
            ?: return InspectionResult.Error("Provider not found: $providerName")

        return try {
            val rawState = provider()
            val redactedState = StateRedactor.redact(rawState) as? Map<String, Any?> ?: emptyMap()
            
            // Check size cap (approximate string representation size)
            val payloadString = redactedState.toString()
            val payloadBytes = payloadString.toByteArray(Charsets.UTF_8).size

            if (payloadBytes > MAX_PAYLOAD_SIZE_BYTES) {
                return InspectionResult.Error("Payload size exceeds maximum allowed limit (${payloadBytes} bytes > ${MAX_PAYLOAD_SIZE_BYTES} bytes)")
            }

            val treeView = buildTreeView(redactedState)
            InspectionResult.Success(redactedState, treeView, payloadBytes)
        } catch (e: Exception) {
            InspectionResult.Error("Failed to inspect state for $providerName: ${e.localizedMessage}")
        }
    }

    private fun buildTreeView(state: Map<String, Any?>, prefix: String = ""): List<TreeNode> {
        val nodes = mutableListOf<TreeNode>()
        for ((key, value) in state) {
            val path = if (prefix.isEmpty()) key else "$prefix.$key"
            when (value) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val childMap = value as? Map<String, Any?> ?: emptyMap()
                    nodes.add(TreeNode(path, key, "Map", childMap.size, true))
                    nodes.addAll(buildTreeView(childMap, path))
                }
                is List<*> -> {
                    nodes.add(TreeNode(path, key, "List", value.size, true))
                }
                else -> {
                    nodes.add(TreeNode(path, key, value?.javaClass?.simpleName ?: "Null", value, false))
                }
            }
        }
        return nodes
    }

    data class TreeNode(
        val path: String,
        val key: String,
        val type: String,
        val value: Any?,
        val isContainer: Boolean
    )

    sealed class InspectionResult {
        data class Success(
            val data: Map<String, Any?>,
            val treeView: List<TreeNode>,
            val sizeBytes: Int
        ) : InspectionResult()

        data class Error(val message: String) : InspectionResult()
    }
}
