package com.aos.core.data.diagnostic

import com.aos.core.domain.model.CrashReport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CrashReportTest {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Test
    fun `crash report serializes and deserializes successfully`() {
        val report = CrashReport(
            id = "test-uuid-1234",
            timestamp = 1718000000000L,
            exceptionType = "java.lang.NullPointerException",
            message = "Simulated test crash",
            stackTrace = "at com.aos.launcher.Test.method(Test.kt:42)",
            deviceInfo = "Model: Google Pixel 8, Android API: 35, Brand: Google"
        )

        val serialized = json.encodeToString(report)
        assertNotNull(serialized)

        val deserialized = json.decodeFromString<CrashReport>(serialized)

        assertEquals("test-uuid-1234", deserialized.id)
        assertEquals(1718000000000L, deserialized.timestamp)
        assertEquals("java.lang.NullPointerException", deserialized.exceptionType)
        assertEquals("Simulated test crash", deserialized.message)
        assertEquals("at com.aos.launcher.Test.method(Test.kt:42)", deserialized.stackTrace)
        assertEquals("Model: Google Pixel 8, Android API: 35, Brand: Google", deserialized.deviceInfo)
    }
}
