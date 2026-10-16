package ci.monprofa.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class CompletedAttemptInput(
    val exerciseId: String,
    val answerJson: String,
    val earnedPoints: Int,
    val possiblePoints: Int,
    val subjectId: String? = null,
    val topicId: String? = null
)

data class ExerciseAttempt(
    val id: String,
    val userId: String,
    val exerciseId: String,
    val answerJson: String,
    val earnedPoints: Int,
    val possiblePoints: Int,
    val completedAtEpochMs: Long,
    val subjectId: String? = null,
    val topicId: String? = null,
    val synced: Boolean = false
)

data class AttemptSummary(
    val attemptsCount: Int,
    val earnedPoints: Long,
    val possiblePoints: Long
) {
    val percentage: Double?
        get() = if (possiblePoints > 0) {
            earnedPoints.toDouble() * 100.0 / possiblePoints
        } else {
            null
        }
}

class AttemptStore(context: Context) :
    SQLiteOpenHelper(
        context.applicationContext,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE exercise_attempts_local (
                id TEXT PRIMARY KEY NOT NULL,
                user_id TEXT NOT NULL,
                exercise_id TEXT NOT NULL,
                answer_json TEXT NOT NULL,
                earned_points INTEGER NOT NULL CHECK (earned_points >= 0),
                possible_points INTEGER NOT NULL CHECK (possible_points > 0),
                completed_at_epoch_ms INTEGER NOT NULL,
                subject_id TEXT,
                topic_id TEXT,
                sync_status TEXT NOT NULL DEFAULT 'PENDING'
                    CHECK (sync_status IN ('PENDING', 'SYNCED')),
                synced_at_epoch_ms INTEGER,
                CHECK (earned_points <= possible_points)
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX idx_attempts_user_date " +
                "ON exercise_attempts_local(user_id, completed_at_epoch_ms DESC)"
        )
        db.execSQL(
            "CREATE INDEX idx_attempts_pending " +
                "ON exercise_attempts_local(user_id, sync_status)"
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Les futures migrations devront préserver les tentatives existantes.
        // Ne pas supprimer la table ni les données enregistrées.
    }

    /**
     * Enregistre une tentative réellement corrigée.
     * L'identité provient de la session sécurisée, pas de l'écran.
     */
    fun recordCompletedAttempt(
        context: Context,
        input: CompletedAttemptInput
    ): ExerciseAttempt {
        require(input.exerciseId.isNotBlank()) {
            "L'identifiant de l'exercice est obligatoire."
        }
        require(input.earnedPoints >= 0) {
            "Le score obtenu ne peut pas être négatif."
        }
        require(input.possiblePoints > 0) {
            "Le score maximal doit être positif."
        }
        require(input.earnedPoints <= input.possiblePoints) {
            "Le score obtenu ne peut pas dépasser le score maximal."
        }

        val answers = JSONObject(input.answerJson)
        val session = SecureSessionStore.load(context)
            ?: throw IllegalStateException(
                "Connecte-toi pour enregistrer une tentative."
            )

        val attempt = ExerciseAttempt(
            id = UUID.randomUUID().toString(),
            userId = session.userId,
            exerciseId = input.exerciseId.trim(),
            answerJson = answers.toString(),
            earnedPoints = input.earnedPoints,
            possiblePoints = input.possiblePoints,
            completedAtEpochMs = System.currentTimeMillis(),
            subjectId = input.subjectId?.trim()?.takeIf { it.isNotEmpty() },
            topicId = input.topicId?.trim()?.takeIf { it.isNotEmpty() }
        )

        // L'écriture SQLite doit réussir avant de programmer la synchronisation.
        insertAttempt(attempt)
        AttemptSyncScheduler.enqueue(context)
        return attempt
    }

    /** Insère une tentative ; utile aussi pour les tests locaux. */
    fun insertAttempt(attempt: ExerciseAttempt) {
        require(attempt.id.isNotBlank() && attempt.userId.isNotBlank())
        require(attempt.exerciseId.isNotBlank())
        require(attempt.earnedPoints >= 0 && attempt.possiblePoints > 0)
        require(attempt.earnedPoints <= attempt.possiblePoints)
        JSONObject(attempt.answerJson)

        val values = ContentValues().apply {
            put("id", attempt.id)
            put("user_id", attempt.userId)
            put("exercise_id", attempt.exerciseId)
            put("answer_json", attempt.answerJson)
            put("earned_points", attempt.earnedPoints)
            put("possible_points", attempt.possiblePoints)
            put("completed_at_epoch_ms", attempt.completedAtEpochMs)
            put("subject_id", attempt.subjectId)
            put("topic_id", attempt.topicId)
            put("sync_status", if (attempt.synced) SYNCED else PENDING)
            if (attempt.synced) {
                put("synced_at_epoch_ms", System.currentTimeMillis())
            }
        }

        writableDatabase.insertOrThrow(TABLE, null, values)
    }

    fun pendingAttempts(userId: String, limit: Int = 100): List<ExerciseAttempt> {
        require(userId.isNotBlank())
        val attempts = mutableListOf<ExerciseAttempt>()

        readableDatabase.query(
            TABLE,
            null,
            "user_id = ? AND sync_status = ?",
            arrayOf(userId, PENDING),
            null,
            null,
            "completed_at_epoch_ms ASC",
            limit.coerceIn(1, 500).toString()
        ).use { cursor ->
            while (cursor.moveToNext()) attempts += cursor.toAttempt()
        }

        return attempts
    }

    fun markSynced(userId: String, ids: List<String>) {
        if (userId.isBlank() || ids.isEmpty()) return

        val db = writableDatabase
        db.beginTransaction()
        try {
            ids.chunked(80).forEach { batch ->
                val placeholders = batch.joinToString(",") { "?" }
                val args = arrayOf(userId, PENDING) + batch.toTypedArray()
                val values = ContentValues().apply {
                    put("sync_status", SYNCED)
                    put("synced_at_epoch_ms", System.currentTimeMillis())
                }

                db.update(
                    TABLE,
                    values,
                    "user_id = ? AND sync_status = ? AND id IN ($placeholders)",
                    args
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun summaryForUser(userId: String): AttemptSummary {
        require(userId.isNotBlank())

        readableDatabase.rawQuery(
            """
            SELECT COUNT(*),
                   COALESCE(SUM(earned_points), 0),
                   COALESCE(SUM(possible_points), 0)
            FROM $TABLE
            WHERE user_id = ?
            """.trimIndent(),
            arrayOf(userId)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return AttemptSummary(
                    attemptsCount = cursor.getInt(0),
                    earnedPoints = cursor.getLong(1),
                    possiblePoints = cursor.getLong(2)
                )
            }
        }

        return AttemptSummary(0, 0, 0)
    }

    fun closeStore() = close()

    private fun Cursor.toAttempt(): ExerciseAttempt {
        return ExerciseAttempt(
            id = getString(getColumnIndexOrThrow("id")),
            userId = getString(getColumnIndexOrThrow("user_id")),
            exerciseId = getString(getColumnIndexOrThrow("exercise_id")),
            answerJson = getString(getColumnIndexOrThrow("answer_json")),
            earnedPoints = getInt(getColumnIndexOrThrow("earned_points")),
            possiblePoints = getInt(getColumnIndexOrThrow("possible_points")),
            completedAtEpochMs = getLong(
                getColumnIndexOrThrow("completed_at_epoch_ms")
            ),
            subjectId = getString(getColumnIndexOrThrow("subject_id")),
            topicId = getString(getColumnIndexOrThrow("topic_id")),
            synced = getString(getColumnIndexOrThrow("sync_status")) == SYNCED
        )
    }

    companion object {
        private const val DATABASE_NAME = "mon_profa_attempts.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE = "exercise_attempts_local"
        private const val PENDING = "PENDING"
        private const val SYNCED = "SYNCED"
    }
}

object AttemptSyncScheduler {
    private const val UNIQUE_WORK = "mon-profa-sync-exercise-attempts"

    fun enqueue(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<AttemptSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UNIQUE_WORK,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }
}

/**
 * Le travail reste en attente sans réseau.
 * Une tentative locale n'est marquée synchronisée qu'après confirmation HTTP.
 */
class AttemptSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val session = SecureSessionStore.load(applicationContext)
            ?: return@withContext Result.success()

        val store = AttemptStore(applicationContext)

        try {
            val pending = store.pendingAttempts(session.userId)
            if (pending.isEmpty()) return@withContext Result.success()

            try {
                SupabaseBackend.uploadAttempts(session.accessToken, pending)
            } catch (error: SupabaseBackend.HttpException) {
                if (error.statusCode != 401) throw error

                val refreshed = SupabaseBackend.refreshSession(session.refreshToken)
                if (refreshed.userId != session.userId) {
                    throw IllegalStateException(
                        "Le compte a changé. Synchronisation interrompue."
                    )
                }

                val newToken = refreshed.accessToken
                    ?: throw IllegalStateException("Jeton de session absent.")

                SecureSessionStore.save(
                    applicationContext,
                    session.copy(
                        accessToken = newToken,
                        refreshToken = refreshed.refreshToken ?: session.refreshToken
                    )
                )

                SupabaseBackend.uploadAttempts(newToken, pending)
            }

            store.markSynced(session.userId, pending.map { it.id })
            Result.success()
        } catch (error: SupabaseBackend.NetworkException) {
            Result.retry()
        } catch (error: SupabaseBackend.HttpException) {
            if (
                error.statusCode == 408 ||
                error.statusCode == 429 ||
                error.statusCode >= 500
            ) {
                Result.retry()
            } else {
                Result.failure()
            }
        } catch (error: Exception) {
            Result.failure()
        } finally {
            store.closeStore()
        }
    }
}
