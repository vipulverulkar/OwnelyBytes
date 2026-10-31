package com.ovenlybytes.app.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.util.UUID

object SecurityManager {

    private const val SECURE_KEY_PREFS = "ovenly_secure_keys"
    private const val KEY_DB_PASSPHRASE = "db_passphrase_v2"

    @Volatile
    private var cachedPassphrase: ByteArray? = null

    @Synchronized
    fun getDatabasePassphrase(context: Context): ByteArray {
        cachedPassphrase?.let { return it }

        val prefs = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                SECURE_KEY_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.values().first(),
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences(SECURE_KEY_PREFS, Context.MODE_PRIVATE)
        }

        var passphraseStr = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (passphraseStr.isNullOrBlank()) {
            passphraseStr = (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replace("-", "")
            prefs.edit().putString(KEY_DB_PASSPHRASE, passphraseStr).apply()
        }

        val bytes = passphraseStr.toByteArray(Charsets.UTF_8)
        cachedPassphrase = bytes
        return bytes
    }

    fun getSQLCipherFactory(context: Context): SupportOpenHelperFactory {
        val passphrase = getDatabasePassphrase(context)
        return SupportOpenHelperFactory(passphrase)
    }
}
