package com.rotai.iq.core.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

interface SecureStorage {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
    fun clearAll()
}

/**
 * Armazenamento seguro utilizando AES-GCM com chave simétrica protegida.
 * Não armazena dados em texto puro no disco do dispositivo.
 */
class AndroidSecureStorage(context: Context) : SecureStorage {

    private val prefs: SharedPreferences = context.getSharedPreferences("rotai_secure_vault", Context.MODE_PRIVATE)
    private val transformation = "AES/GCM/NoPadding"
    private val keyAliasPref = "rotai_sec_key"
    private val secretKey: SecretKey

    init {
        secretKey = getOrCreateKey()
    }

    override fun saveString(key: String, value: String) {
        val encrypted = encrypt(value)
        prefs.edit().putString(key, encrypted).apply()
    }

    override fun getString(key: String): String? {
        val raw = prefs.getString(key, null) ?: return null
        return decrypt(raw)
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val existingKeyB64 = prefs.getString(keyAliasPref, null)
        if (existingKeyB64 != null) {
            val keyBytes = Base64.decode(existingKeyB64, Base64.NO_WRAP)
            return SecretKeySpec(keyBytes, "AES")
        }

        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val generatedKey = keyGen.generateKey()
        val keyB64 = Base64.encodeToString(generatedKey.encoded, Base64.NO_WRAP)
        prefs.edit().putString(keyAliasPref, keyB64).apply()
        return generatedKey
    }

    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(transformation)
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + cipherBytes
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encryptedB64: String): String? {
        return try {
            val combined = Base64.decode(encryptedB64, Base64.NO_WRAP)
            if (combined.size < 13) return null
            val iv = combined.copyOfRange(0, 12)
            val cipherBytes = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance(transformation)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
