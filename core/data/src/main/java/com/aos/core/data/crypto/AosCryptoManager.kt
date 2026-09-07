package com.aos.core.data.crypto

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AosCryptoManager @Inject constructor() {

    companion object {
        private val MAGIC_HEADER = byteArrayOf(0x41, 0x4F, 0x53, 0x42) // "AOSB"
        private const val ITERATION_COUNT = 10_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 16
        private const val IV_LENGTH_BYTES = 12
        private const val TAG_LENGTH_BITS = 128
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    }

    private val secureRandom = SecureRandom()

    fun encrypt(data: ByteArray, password: String): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }

        val keySpec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keyBytes = factory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val cipherText = cipher.doFinal(data)

        val buffer = ByteBuffer.allocate(MAGIC_HEADER.size + salt.size + iv.size + cipherText.size)
        buffer.put(MAGIC_HEADER)
        buffer.put(salt)
        buffer.put(iv)
        buffer.put(cipherText)
        return buffer.array()
    }

    fun decrypt(encryptedData: ByteArray, password: String): ByteArray {
        val minLength = MAGIC_HEADER.size + SALT_LENGTH_BYTES + IV_LENGTH_BYTES + 16
        if (encryptedData.size < minLength) {
            throw IllegalArgumentException("Yedek dosyası boyutu geçersiz veya bozuk.")
        }

        val buffer = ByteBuffer.wrap(encryptedData)
        val header = ByteArray(MAGIC_HEADER.size)
        buffer.get(header)
        if (!header.contentEquals(MAGIC_HEADER)) {
            throw IllegalArgumentException("Geçersiz yedek formatı: AOS Backup başlığı bulunamadı.")
        }

        val salt = ByteArray(SALT_LENGTH_BYTES)
        buffer.get(salt)

        val iv = ByteArray(IV_LENGTH_BYTES)
        buffer.get(iv)

        val cipherText = ByteArray(buffer.remaining())
        buffer.get(cipherText)

        val keySpec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keyBytes = factory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            cipher.doFinal(cipherText)
        } catch (e: Exception) {
            throw IllegalArgumentException("Parola hatalı veya yedek verisi bozulmuş.", e)
        }
    }
}
