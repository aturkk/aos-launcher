package com.aos.core.data.diagnostic

import android.content.Context
import android.os.Build
import com.aos.core.domain.model.CrashReport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.UUID

object AosCrashReporter {

    private var isInitialized = false
    private lateinit var crashDir: File
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    fun init(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext
        crashDir = File(appContext.filesDir, "crash_reports").apply { mkdirs() }

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                recordCrash(throwable, "Uncaught Exception on thread: ${thread.name}")
            } catch (e: Exception) {
                // Ignore failure in crash handler
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
        isInitialized = true
    }

    fun recordCrash(throwable: Throwable, messagePrefix: String = "") {
        if (!::crashDir.isInitialized) return
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()

            val msg = if (messagePrefix.isNotBlank()) {
                "$messagePrefix - ${throwable.message ?: "No message"}"
            } else {
                throwable.message ?: "No message"
            }

            val deviceInfo = "Model: ${Build.MANUFACTURER} ${Build.MODEL}, Android API: ${Build.VERSION.SDK_INT}, Brand: ${Build.BRAND}"
            val report = CrashReport(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                exceptionType = throwable.javaClass.name,
                message = msg,
                stackTrace = stackTrace,
                deviceInfo = deviceInfo
            )

            val file = File(crashDir, "crash_${report.timestamp}_${report.id.take(8)}.json")
            file.writeText(json.encodeToString(report))
        } catch (e: Exception) {
            // Ignore write errors during crash handling
        }
    }

    fun getReports(): List<CrashReport> {
        if (!::crashDir.isInitialized || !crashDir.exists()) return emptyList()
        return try {
            crashDir.listFiles { file -> file.extension == "json" }
                ?.mapNotNull { file ->
                    runCatching { json.decodeFromString<CrashReport>(file.readText()) }.getOrNull()
                }
                ?.sortedByDescending { it.timestamp }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearReports() {
        if (!::crashDir.isInitialized || !crashDir.exists()) return
        try {
            crashDir.listFiles { file -> file.extension == "json" }?.forEach { it.delete() }
        } catch (e: Exception) {
            // Ignore cleanup failure
        }
    }
}
