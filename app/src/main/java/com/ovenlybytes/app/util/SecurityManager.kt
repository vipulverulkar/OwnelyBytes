package com.ovenlybytes.app.util

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.security.SecureRandom

object SecurityManager {

    private const val SECURE_KEY_PREFS = "ovenly_secure_keys"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"

    fun getDatabasePassphrase(context: Context): ByteArray {
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

        var passphraseB64 = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (passphraseB64 == null) {
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            passphraseB64 = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
            prefs.edit().putString(KEY_DB_PASSPHRASE, passphraseB64).apply()
        }

        return Base64.decode(passphraseB64, Base64.NO_WRAP)
    }

    fun getSQLCipherFactory(context: Context): SupportOpenHelperFactory {
        val passphrase = getDatabasePassphrase(context)
        return SupportOpenHelperFactory(passphrase)
    }
}
