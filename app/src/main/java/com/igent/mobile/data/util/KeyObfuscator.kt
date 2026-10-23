package com.igent.mobile.data.util

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object KeyObfuscator {
    private const val ALGORITHM = "AES"
    // Fixed derived application-level obfuscation key for secure local storage of user api keys
    private val FIXED_KEY_BYTES = byteArrayOf(
        0x49, 0x67, 0x65, 0x6e, 0x74, 0x41, 0x49, 0x4b,
        0x65, 0x79, 0x53, 0x65, 0x63, 0x75, 0x72, 0x65
    ) // "IgentAIKeySecure"

    fun obfuscate(plainText: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val keySpec = SecretKeySpec(FIXED_KEY_BYTES, ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    fun deobfuscate(cipherText: String): String {
        if (cipherText.isBlank()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val keySpec = SecretKeySpec(FIXED_KEY_BYTES, ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = Base64.decode(cipherText, Base64.NO_WRAP)
            val decrypted = cipher.doFinal(decoded)
            String(decrypted, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            // Fallback in case it was stored in plaintext previously
            cipherText
        }
    }
}
