package com.aos.core.data.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class AosCryptoManagerTest {

    private lateinit var cryptoManager: AosCryptoManager

    @Before
    fun setUp() {
        cryptoManager = AosCryptoManager()
    }

    @Test
    fun `encrypt and decrypt returns original data when password is correct`() {
        val originalText = "AOS Launcher Test Payload 123456 Özel Karakterler: ğüşıöç"
        val originalBytes = originalText.toByteArray(Charsets.UTF_8)
        val password = "StrongSecretPassword#2026"

        val encryptedBytes = cryptoManager.encrypt(originalBytes, password)

        assertNotEquals(0, encryptedBytes.size)
        assertTrue(encryptedBytes.size > originalBytes.size)

        val decryptedBytes = cryptoManager.decrypt(encryptedBytes, password)
        val decryptedText = String(decryptedBytes, Charsets.UTF_8)

        assertEquals(originalText, decryptedText)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decrypt throws exception when password is wrong`() {
        val originalBytes = "Top Secret Backup".toByteArray(Charsets.UTF_8)
        val encryptedBytes = cryptoManager.encrypt(originalBytes, "correctPassword")

        cryptoManager.decrypt(encryptedBytes, "wrongPassword")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decrypt throws exception when payload is tampered`() {
        val originalBytes = "Data to be tampered".toByteArray(Charsets.UTF_8)
        val encryptedBytes = cryptoManager.encrypt(originalBytes, "password")

        // Tamper with last byte (part of ciphertext or authentication tag)
        encryptedBytes[encryptedBytes.size - 1] = (encryptedBytes[encryptedBytes.size - 1].toInt() xor 0xFF).toByte()

        cryptoManager.decrypt(encryptedBytes, "password")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decrypt throws exception when header is invalid`() {
        val invalidHeaderData = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F)

        cryptoManager.decrypt(invalidHeaderData, "password")
    }
}
