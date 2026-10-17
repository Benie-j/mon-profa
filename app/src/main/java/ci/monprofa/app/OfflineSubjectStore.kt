package ci.monprofa.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Cache local des disciplines déjà récupérées depuis Supabase.
 * Les données restent disponibles après fermeture de l'application.
 */
object OfflineSubjectStore {
    private const val PREFS = "mon_profa_offline_content"
    private const val SUBJECTS_KEY = "subjects_v1"

    private fun readAll(context: Context): List<SupabaseBackend.Subject> {
        val raw = context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).getString(SUBJECTS_KEY, null) ?: return emptyList()

        return runCatching {
            val rows = JSONArray(raw)
            buildList {
                for (index in 0 until rows.length()) {
                    val row = rows.optJSONObject(index) ?: continue
                    val name = row.optString("name").trim()
                    if (name.isBlank()) continue

                    val seriesValue = row.optString("series")
                        .takeIf { it.isNotBlank() && it != "null" }

                    add(
                        SupabaseBackend.Subject(
                            id = row.optString("id"),
                            name = name,
                            schoolClass = row.optString("schoolClass"),
                            series = seriesValue,
                            description = row.optString("description")
                                .takeIf { it != "null" }.orEmpty(),
                            sortOrder = row.optInt("sortOrder", 0)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun load(
        context: Context,
        schoolClass: String,
        series: String
    ): List<SupabaseBackend.Subject> {
        if (schoolClass.isBlank()) return emptyList()

        return readAll(context)
            .filter {
                it.schoolClass.equals(schoolClass, ignoreCase = true) &&
                    (
                        it.series.isNullOrBlank() ||
                            it.series.equals(series, ignoreCase = true)
                    )
            }
            .sortedWith(
                compareBy<SupabaseBackend.Subject> { it.sortOrder }
                    .thenBy { it.name.lowercase() }
            )
    }

    fun save(
        context: Context,
        subjects: List<SupabaseBackend.Subject>
    ) {
        // Une réponse vide ne doit pas effacer un cache déjà utile.
        if (subjects.isEmpty()) return

        val existing = readAll(context)
        val classesReceived = subjects
            .map { it.schoolClass.lowercase() }
            .toSet()

        val merged = existing.filterNot {
            it.schoolClass.lowercase() in classesReceived
        } + subjects

        val rows = JSONArray()
        merged.forEach { subject ->
            rows.put(
                JSONObject()
                    .put("id", subject.id)
                    .put("name", subject.name)
                    .put("schoolClass", subject.schoolClass)
                    .put("series", subject.series ?: JSONObject.NULL)
                    .put("description", subject.description)
                    .put("sortOrder", subject.sortOrder)
            )
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(SUBJECTS_KEY, rows.toString())
            .apply()
    }
}
