package com.igent.mobile.data.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeyObfuscator {
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "IgentKeyAlias"
    private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128
    private const val VERSION_BYTE: Byte = 0x01

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
            throw IllegalStateException("Android KeyStore operation failed: ${e.message}", e)
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
            throw IllegalStateException("Failed to obfuscate sensitive key: ${e.message}", e)
        }
    }

    fun deobfuscate(cipherText: String): String {
        if (cipherText.isBlank()) return ""
        return try {
            val decoded = Base64.decode(cipherText, Base64.NO_WRAP)
            if (decoded.isEmpty()) return ""
            
            if (decoded[0] != VERSION_BYTE) {
                throw IllegalStateException("Unsupported obfuscated key version or format")
            }
            
            val ivSize = decoded[1].toInt()
            if (ivSize <= 0 || ivSize + 2 > decoded.size) {
                throw IllegalStateException("Invalid initialization vector size in cipherText")
            }

            val iv = decoded.copyOfRange(2, 2 + ivSize)
            val encrypted = decoded.copyOfRange(2 + ivSize, decoded.size)
            
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_SIZE, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            
            val decrypted = cipher.doFinal(encrypted)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to deobfuscate key: ${e.message}", e)
        }
    }
}
