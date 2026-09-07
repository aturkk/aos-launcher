package com.aos.core.data.repository

import com.aos.core.data.database.entity.NotificationRecordEntity
import com.aos.core.domain.model.NotificationRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationRecordEntityTest {

    @Test
    fun `toDomain converts entity to domain model correctly`() {
        val entity = NotificationRecordEntity(
            id = 42L,
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "Ahmet",
            content = "Selam, nasılsın?",
            timestamp = 1717000000000L
        )

        val domain = entity.toDomain()

        assertEquals(42L, domain.id)
        assertEquals("com.whatsapp", domain.packageName)
        assertEquals("WhatsApp", domain.appName)
        assertEquals("Ahmet", domain.title)
        assertEquals("Selam, nasılsın?", domain.content)
        assertEquals(1717000000000L, domain.timestamp)
    }

    @Test
    fun `fromDomain converts domain model to entity correctly`() {
        val domain = NotificationRecord(
            id = 101L,
            packageName = "org.telegram.messenger",
            appName = "Telegram",
            title = "Kanal Duyurusu",
            content = "Yeni sürüm v1.3.0 yayınlandı!",
            timestamp = 1717000050000L
        )

        val entity = NotificationRecordEntity.fromDomain(domain)

        assertEquals(101L, entity.id)
        assertEquals("org.telegram.messenger", entity.packageName)
        assertEquals("Telegram", entity.appName)
        assertEquals("Kanal Duyurusu", entity.title)
        assertEquals("Yeni sürüm v1.3.0 yayınlandı!", entity.content)
        assertEquals(1717000050000L, entity.timestamp)
    }
}
