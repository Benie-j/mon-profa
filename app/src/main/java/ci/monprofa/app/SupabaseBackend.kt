package ci.monprofa.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException

/**
 * Client REST léger pour Supabase Auth et la table profiles.
 * La clé utilisée ici est une clé publique publishable, jamais une clé secrète.
 */
object SupabaseBackend {
    private const val BASE_URL = "https://rywhszxsouqosbqsrezp.supabase.co"
    private const val PUBLISHABLE_KEY = "sb_publishable_XLKfs_djwlzZC_9vTKpFVA_HzBQrxsG"

    class HttpException(
        val statusCode: Int,
        message: String
    ) : IllegalStateException(message)

    class NetworkException(
        message: String,
        cause: Throwable? = null
    ) : IllegalStateException(message, cause)

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

        val redirect = URLEncoder.encode(
            "monprofa://auth/callback", "UTF-8"
        )
        val response = JSONObject(
            request(
                "/auth/v1/signup?redirect_to=$redirect",
                "POST",
                body
            )
        )
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

    /**
     * Vérifie le jeton reçu après la confirmation par lien e-mail.
     * Supabase retourne l'identité uniquement si le jeton est valide.
     */
    suspend fun verifyAccessToken(accessToken: String): AuthResult {
        require(accessToken.isNotBlank()) {
            "Jeton de confirmation absent."
        }

        val response = JSONObject(
            request(
                "/auth/v1/user",
                "GET",
                accessToken = accessToken
            )
        )

        val userId = response.optString("id")
            .takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(
                "Supabase n'a pas retourné l'identifiant du compte."
            )

        return AuthResult(
            accessToken = accessToken,
            userId = userId
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
        return AuthResult(
            accessToken = token,
            userId = userId,
            refreshToken = response.optString("refresh_token")
                .takeIf { it.isNotBlank() }
        )
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


    /**
     * Modifie uniquement le pseudo et la série du profil connecté.
     * Le niveau scolaire reste inchangé.
     */
    suspend fun updateProfile(
        accessToken: String,
        userId: String,
        pseudo: String,
        series: String
    ): StudentProfile {
        val cleanPseudo = pseudo.trim()
        val cleanSeries = series.trim()

        require(cleanPseudo.length in 3..20) {
            "Le pseudo doit contenir entre 3 et 20 caractères."
        }

        val body = JSONObject()
            .put("pseudo", cleanPseudo)
            .put("series", cleanSeries.ifBlank { JSONObject.NULL })

        val path = "/rest/v1/profiles?id=eq.$userId"

        request(
            path = path,
            method = "PATCH",
            body = body,
            accessToken = accessToken
        )

        return getProfile(accessToken, userId)
    }

    /**
     * Enregistre l'ouverture quotidienne du tableau de bord et renvoie la série
     * calculée côté PostgreSQL. La date n'est jamais fournie par le client.
     */
    data class StreakStatus(
        val currentStreak: Int,
        val longestStreak: Int,
        val lastActivityDate: String
    )

    suspend fun registerDailyStreakActivity(accessToken: String): StreakStatus {
        require(accessToken.isNotBlank()) { "Session expirée. Reconnecte-toi." }
        val response = JSONObject(
            request(
                "/rest/v1/rpc/register_daily_streak_activity",
                "POST",
                JSONObject(),
                accessToken
            )
        )
        return StreakStatus(
            currentStreak = response.optInt("current_streak", 0),
            longestStreak = response.optInt("longest_streak", 0),
            lastActivityDate = response.optString("last_activity_date")
        )
    }

    suspend fun sendPasswordReset(email: String) {
        request(
            "/auth/v1/recover",
            "POST",
            JSONObject().put("email", email.trim())
        )
    }

    data class Subject(
        val id: String,
        val name: String,
        val schoolClass: String,
        val series: String?,
        val description: String,
        val sortOrder: Int
    )

    suspend fun getSubjects(
        accessToken: String,
        schoolClass: String
    ): List<Subject> {
        require(schoolClass.isNotBlank()) {
            "La classe de ton profil n'est pas renseignée."
        }

        val encodedClass = URLEncoder.encode(schoolClass.trim(), "UTF-8")
        val path =
            "/rest/v1/kalyno_subjects" +
            "?select=id,name,school_class,series,description,sort_order" +
            "&school_class=eq.$encodedClass" +
            "&order=sort_order.asc,name.asc"

        val rows = JSONArray(
            request(path, "GET", accessToken = accessToken.takeIf { it.isNotBlank() })
        )

        return buildList {
            for (index in 0 until rows.length()) {
                val row = rows.optJSONObject(index) ?: continue
                val name = row.optString("name").trim()
                if (name.isBlank()) continue

                add(
                    Subject(
                        id = row.optString("id"),
                        name = name,
                        schoolClass = row.optString("school_class"),
                        series = row.optString("series")
                            .takeIf { it.isNotBlank() && it != "null" },
                        description = row.optString("description")
                            .takeIf { it != "null" }.orEmpty(),
                        sortOrder = row.optInt("sort_order", 0)
                    )
                )
            }
        }
    }

    private suspend fun request(
        path: String,
        method: String = "GET",
        body: JSONObject? = null,
        accessToken: String? = null,
        rawBody: String? = null,
        prefer: String? = null
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

            if (!prefer.isNullOrBlank()) {
                connection.setRequestProperty("Prefer", prefer)
            }

            require(body == null || rawBody == null) {
                "Utilise soit body, soit rawBody, pas les deux."
            }

            if (body != null || rawBody != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use {
                    it.write(rawBody ?: body!!.toString())
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

                throw HttpException(status, friendlyMessage(rawMessage))
            }

            response
        } catch (e: UnknownHostException) {
            throw NetworkException("Pas de connexion Internet. Vérifie ton réseau.", e)
        } catch (e: SocketTimeoutException) {
            throw NetworkException("Le serveur met trop de temps à répondre. Réessaie.", e)
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
