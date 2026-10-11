package ci.monprofa.app

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stockage chiffré de la session avec une clé conservée dans Android Keystore.
 * Seul le contenu chiffré est écrit dans les préférences.
 */
object SecureSessionStore {
    private const val PREFS = "mon_profa_secure_session"
    private const val ENTRY = "encrypted_session"
    private const val ALIAS = "mon_profa_session_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    data class Session(
        val accessToken: String,
        val refreshToken: String,
        val userId: String,
        val pseudo: String,
        val schoolClass: String,
        val series: String
    )

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = ks.getKey(ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(
            android.security.keystore.KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val spec = android.security.keystore.KeyGenParameterSpec.Builder(
            ALIAS,
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                android.security.keystore.KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setRandomizedEncryptionRequired(true)
            .build()

        generator.init(spec)
        return generator.generateKey()
    }

    fun save(context: Context, session: Session) {
        val json = JSONObject()
            .put("accessToken", session.accessToken)
            .put("refreshToken", session.refreshToken)
            .put("userId", session.userId)
            .put("pseudo", session.pseudo)
            .put("schoolClass", session.schoolClass)
            .put("series", session.series)
            .toString()

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(json.toByteArray(Charsets.UTF_8))
        val combined = cipher.iv + encrypted
        val encoded = Base64.encodeToString(combined, Base64.NO_WRAP)

        check(
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(ENTRY, encoded).commit()
        ) { "Impossible d'enregistrer la session." }
    }

    fun load(context: Context): Session? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val encoded = prefs.getString(ENTRY, null) ?: return null

        return try {
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            require(combined.size > 12) { "Session chiffrée invalide." }

            val iv = combined.copyOfRange(0, 12)
            val encrypted = combined.copyOfRange(12, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            val json = JSONObject(
                String(cipher.doFinal(encrypted), Charsets.UTF_8)
            )

            val access = json.optString("accessToken")
            val refresh = json.optString("refreshToken")
            val userId = json.optString("userId")
            if (access.isBlank() || refresh.isBlank() || userId.isBlank()) {
                clear(context)
                null
            } else {
                Session(
                    accessToken = access,
                    refreshToken = refresh,
                    userId = userId,
                    pseudo = json.optString("pseudo"),
                    schoolClass = json.optString("schoolClass"),
                    series = json.optString("series")
                )
            }
        } catch (_: Exception) {
            // Une clé supprimée ou une session illisible ne doit pas bloquer l'application.
            clear(context)
            null
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(ENTRY).commit()
    }
}
