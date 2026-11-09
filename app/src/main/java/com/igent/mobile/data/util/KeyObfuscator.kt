package com.igent.mobile.data.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object KeyObfuscator {
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "IgentKeyAlias"
    private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val AES_ECB_TRANSFORMATION = "AES" // Legacy
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128
    private const val VERSION_BYTE: Byte = 0x01

    private val FIXED_KEY_BYTES = byteArrayOf(
        0x49, 0x67, 0x65, 0x6e, 0x74, 0x41, 0x49, 0x4b,
        0x65, 0x79, 0x53, 0x65, 0x63, 0x75, 0x72, 0x65
    ) // "IgentAIKeySecure"

    private fun getOrCreateKey(): SecretKey {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            val key = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (key != null) return key

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            return keyGenerator.generateKey()
        } catch (e: Exception) {
            // Fallback for non-Android environments (like unit tests) or KeyStore failures
            return SecretKeySpec(FIXED_KEY_BYTES, "AES")
        }
    }

    fun obfuscate(plainText: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // Output format: [VersionByte] [IV_SIZE_Byte] [IV] [EncryptedPayload]
            val output = ByteArray(1 + 1 + iv.size + encrypted.size)
            output[0] = VERSION_BYTE
            output[1] = iv.size.toByte()
            System.arraycopy(iv, 0, output, 2, iv.size)
            System.arraycopy(encrypted, 0, output, 2 + iv.size, encrypted.size)
            
            Base64.encodeToString(output, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Ultimate fallback to legacy obfuscation if GCM fails
            obfuscateLegacy(plainText)
        }
    }

    fun deobfuscate(cipherText: String): String {
        if (cipherText.isBlank()) return ""
        return try {
            val decoded = Base64.decode(cipherText, Base64.NO_WRAP)
            if (decoded.isEmpty()) return cipherText
            
            if (decoded[0] != VERSION_BYTE) {
                // Not versioned, might be old fixed key encryption or plaintext
                return deobfuscateLegacy(cipherText)
            }
            
            val ivSize = decoded[1].toInt()
            val iv = decoded.copyOfRange(2, 2 + ivSize)
            val encrypted = decoded.copyOfRange(2 + ivSize, decoded.size)
            
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_SIZE, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            
            val decrypted = cipher.doFinal(encrypted)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            // Fallback in case it was stored in legacy format or is plaintext
            deobfuscateLegacy(cipherText)
        }
    }
    
    private fun obfuscateLegacy(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance(AES_ECB_TRANSFORMATION)
            val keySpec = SecretKeySpec(FIXED_KEY_BYTES, AES_ECB_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    private fun deobfuscateLegacy(cipherText: String): String {
        return try {
            val cipher = Cipher.getInstance(AES_ECB_TRANSFORMATION)
            val keySpec = SecretKeySpec(FIXED_KEY_BYTES, AES_ECB_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = Base64.decode(cipherText, Base64.NO_WRAP)
            val decrypted = cipher.doFinal(decoded)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }
}
