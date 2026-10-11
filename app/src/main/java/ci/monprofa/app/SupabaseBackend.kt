package ci.monprofa.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

/**
 * Client REST léger pour Supabase Auth et la table profiles.
 * La clé utilisée ici est une clé publique publishable, jamais une clé secrète.
 */
object SupabaseBackend {
    private const val BASE_URL = "https://rywhszxsouqosbqsrezp.supabase.co"
    private const val PUBLISHABLE_KEY = "sb_publishable_XLKfs_djwlzZC_9vTKpFVA_HzBQrxsG"

    data class AuthResult(
        val accessToken: String?,
        val userId: String?,
        val refreshToken: String? = null
    )

    data class StudentProfile(
        val pseudo: String,
        val schoolClass: String,
        val series: String?
    )

    suspend fun signUp(
        email: String,
        password: String,
        pseudo: String,
        schoolClass: String,
        series: String
    ): AuthResult {
        val metadata = JSONObject()
            .put("pseudo", pseudo.trim())
            .put("school_class", schoolClass.trim())
            .put("series", series.trim())

        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .put("data", metadata)

        val response = JSONObject(request("/auth/v1/signup", "POST", body))
        val user = response.optJSONObject("user") ?: response
        val userId = user.optString("id").takeIf { it.isNotBlank() }

        if (userId == null) {
            throw IllegalStateException("Supabase n'a pas retourné le compte créé.")
        }

        return AuthResult(
            accessToken = response.optString("access_token").takeIf { it.isNotBlank() },
            userId = userId,
            refreshToken = response.optString("refresh_token").takeIf { it.isNotBlank() }
        )
    }

    suspend fun verifySignupCode(email: String, code: String): AuthResult {
        val body = JSONObject()
            .put("type", "signup")
            .put("email", email.trim())
            .put("token", code.trim())

        val response = JSONObject(request("/auth/v1/verify", "POST", body))
        val user = response.optJSONObject("user") ?: JSONObject()

        return AuthResult(
            accessToken = response.optString("access_token").takeIf { it.isNotBlank() },
            userId = user.optString("id").takeIf { it.isNotBlank() },
            refreshToken = response.optString("refresh_token").takeIf { it.isNotBlank() }
        )
    }

    suspend fun resendSignupCode(email: String) {
        val body = JSONObject()
            .put("type", "signup")
            .put("email", email.trim())

        request("/auth/v1/resend", "POST", body)
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)

        val response = JSONObject(
            request("/auth/v1/token?grant_type=password", "POST", body)
        )
        val user = response.optJSONObject("user") ?: JSONObject()

        val token = response.optString("access_token").takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Session absente. Vérifie ton e-mail et ton mot de passe.")

        val userId = user.optString("id").takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Identifiant du compte absent.")

        return AuthResult(
            accessToken = token,
            userId = userId,
            refreshToken = response.optString("refresh_token").takeIf { it.isNotBlank() }
        )
    }

    suspend fun refreshSession(refreshToken: String): AuthResult {
        val response = JSONObject(
            request(
                "/auth/v1/token?grant_type=refresh_token",
                "POST",
                JSONObject().put("refresh_token", refreshToken)
            )
        )
        val user = response.optJSONObject("user") ?: JSONObject()
        val token = response.optString("access_token").takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Session expirée. Connecte-toi à nouveau.")
        val userId = user.optString("id").takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Identifiant du compte absent.")

        return AuthResult(
            accessToken = token,
            userId = userId,
            refreshToken = response.optString("refresh_token").takeIf { it.isNotBlank() }
        )
    }

    suspend fun signInWithPseudo(pseudo: String, password: String): AuthResult {
        val body = JSONObject()
            .put("pseudo", pseudo.trim())
            .put("password", password)
        val response = JSONObject(
            request("/functions/v1/sign-in-pseudo", "POST", body)
        )
        val token = response.optString("access_token").takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(
                response.optString("error").ifBlank { "Pseudo ou mot de passe incorrect." }
            )
        val userId = response.optString("user_id").takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Identifiant du compte absent.")
        return AuthResult(token, userId)
    }

    suspend fun getProfile(accessToken: String, userId: String): StudentProfile {
        val path = "/rest/v1/profiles?select=pseudo,school_class,series&id=eq.$userId"
        val rows = JSONArray(request(path, "GET", accessToken = accessToken))
        val row = rows.optJSONObject(0)
            ?: throw IllegalStateException("Profil introuvable. Contacte le support.")

        return StudentProfile(
            pseudo = row.optString("pseudo"),
            schoolClass = row.optString("school_class"),
            series = row.optString("series")
                .takeIf { it.isNotBlank() && it != "null" }
        )
    }

    suspend fun sendPasswordReset(email: String) {
        request(
            "/auth/v1/recover",
            "POST",
            JSONObject().put("email", email.trim())
        )
    }

    private suspend fun request(
        path: String,
        method: String = "GET",
        body: JSONObject? = null,
        accessToken: String? = null
    ): String = withContext(Dispatchers.IO) {
        val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("apikey", PUBLISHABLE_KEY)
            connection.setRequestProperty("Accept", "application/json")

            if (!accessToken.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
            }

            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use {
                    it.write(body.toString())
                }
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

            if (status !in 200..299) {
                val json = runCatching { JSONObject(response) }.getOrNull()
                val rawMessage = listOf("msg", "message", "error_description", "error")
                    .firstNotNullOfOrNull { key ->
                        json?.optString(key)?.takeIf { it.isNotBlank() && it != "null" }
                    } ?: "Erreur serveur (HTTP $status)."

                throw IllegalStateException(friendlyMessage(rawMessage))
            }

            response
        } catch (e: UnknownHostException) {
            throw IllegalStateException("Pas de connexion Internet. Vérifie ton réseau.")
        } catch (e: SocketTimeoutException) {
            throw IllegalStateException("Le serveur met trop de temps à répondre. Réessaie.")
        } finally {
            connection.disconnect()
        }
    }

    private fun friendlyMessage(message: String): String {
        val lower = message.lowercase()

        return when {
            "invalid login credentials" in lower ->
                "Adresse e-mail ou mot de passe incorrect."
            "email not confirmed" in lower ->
                "Confirme d'abord ton adresse e-mail."
            "user already registered" in lower || "already registered" in lower ->
                "Cette adresse e-mail possède déjà un compte."
            "profiles_pseudo_lower_unique" in lower || "duplicate key" in lower ->
                "Ce pseudo est peut-être déjà utilisé. Choisis-en un autre."
            "password should be at least" in lower ->
                "Le mot de passe est trop court."
            "token has expired" in lower || "otp_expired" in lower ->
                "Ce code a expiré. Demande un nouveau code."
            "invalid token" in lower || "token is invalid" in lower ->
                "Code incorrect. Vérifie les six chiffres reçus."
            else -> message
        }
    }
}
