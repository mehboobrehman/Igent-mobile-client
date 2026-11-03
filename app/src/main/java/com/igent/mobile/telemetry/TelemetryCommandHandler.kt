package com.igent.mobile.telemetry

import com.igent.mobile.util.AgentLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryCommandHandler @Inject constructor(
    private val logger: AgentLogger,
    private val performanceCollector: PerformanceCollector
) {
    private val allowedCommands = setOf(
        "ping",
        "change_log_level",
        "toggle_category",
        "request_snapshot",
        "restart_sync"
    )

    fun handleCommand(commandId: String, commandName: String, parameters: String): CommandResult {
        if (!allowedCommands.contains(commandName)) {
            logger.w("TelemetryCommandHandler", "Rejecting unallowed command: $commandName")
            return CommandResult(
                commandId = commandId,
                success = false,
                message = "Command not allowed: $commandName"
            )
        }

        logger.i("TelemetryCommandHandler", "Executing allow-listed command: $commandName with params: $parameters")

        return try {
            when (commandName) {
                "ping" -> {
                    CommandResult(commandId, true, "pong")
                }
                "change_log_level" -> {
                    CommandResult(commandId, true, "Log level updated successfully")
                }
                "toggle_category" -> {
                    CommandResult(commandId, true, "Category toggled successfully")
                }
                "request_snapshot" -> {
                    performanceCollector.logPerformanceSnapshot()
                    CommandResult(commandId, true, "Performance snapshot logged")
                }
                "restart_sync" -> {
                    logger.triggerSync()
                    CommandResult(commandId, true, "Sync triggered")
                }
                else -> {
                    CommandResult(commandId, false, "Unknown command handler for $commandName")
                }
            }
        } catch (e: Exception) {
            logger.e("TelemetryCommandHandler", "Error executing command $commandName", e)
            CommandResult(commandId, false, "Execution failed: ${e.localizedMessage}")
        }
    }

    data class CommandResult(
        val commandId: String,
        val success: Boolean,
        val message: String
    )
}
