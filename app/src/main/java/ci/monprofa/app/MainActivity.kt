package ci.monprofa.app

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.platform.LocalConfiguration

private val Bleu = Color(0xFF2457C5)
private val BleuClair = Color(0xFF4778D8)
private val Vert = Color(0xFF15966A)
private val Jaune = Color(0xFFFFC857)
private val Violet = Color(0xFF8457D9)
private val Fond = Color(0xFFF5F8FF)
private val Texte = Color(0xFF202B45)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.view.animate()
                .alpha(0f)
                .setDuration(350L)
                .withEndAction {
                    splashScreenView.remove()
                }
                .start()
        }
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Bleu,
                    secondary = Vert,
                    tertiary = Violet,
                    background = Fond
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                // La pile mémorise le parcours réel de l'utilisateur.
                val historique = remember {
                    mutableStateListOf("restauration")
                }

                var classeChoisie by remember { mutableStateOf<String?>(null) }
                var serieChoisie by remember { mutableStateOf<String?>(null) }
                var emailInscription by remember { mutableStateOf("") }
                var pseudoUtilisateur by remember { mutableStateOf("") }
                var accessToken by remember { mutableStateOf<String?>(null) }
                val contexteApp = androidx.compose.ui.platform.LocalContext.current


                // Restaure la session sans demander les identifiants à chaque ouverture.
                LaunchedEffect(Unit) {
                    val saved = SecureSessionStore.load(contexteApp)
                    if (saved == null) {
                        historique.clear()
                        historique.add("accueil")
                    } else {
                        fun afficherSession(
                            token: String,
                            pseudo: String,
                            schoolClass: String,
                            series: String
                        ) {
                            accessToken = token
                            pseudoUtilisateur = pseudo
                            classeChoisie = schoolClass
                            serieChoisie = series
                            contexteApp.getSharedPreferences(
                                "mon_profa", android.content.Context.MODE_PRIVATE
                            ).edit().putString("pseudo", pseudo).apply()
                            historique.clear()
                            historique.add("dashboard")
                        }

                        fun estErreurReseau(e: Exception): Boolean {
                            val message = e.message.orEmpty().lowercase()
                            return listOf(
                                "pas de connexion internet",
                                "vérifie ton réseau",
                                "trop de temps",
                                "timeout",
                                "unable to resolve host",
                                "network is unreachable",
                                "connection refused",
                                "failed to connect"
                            ).any { it in message }
                        }

                        try {
                            val refreshed = try {
                                SupabaseBackend.refreshSession(saved.refreshToken)
                            } catch (e: Exception) {
                                if (estErreurReseau(e)) {
                                    afficherSession(
                                        saved.accessToken, saved.pseudo,
                                        saved.schoolClass, saved.series
                                    )
                                    return@LaunchedEffect
                                }
                                throw e
                            }

                            val token = refreshed.accessToken
                                ?: throw IllegalStateException("Session à renouveler.")
                            val userId = refreshed.userId
                                ?: saved.userId
                            val newRefresh = refreshed.refreshToken
                                ?: saved.refreshToken

                            val profile = try {
                                SupabaseBackend.getProfile(token, userId)
                            } catch (e: Exception) {
                                if (estErreurReseau(e)) {
                                    SecureSessionStore.save(
                                        contexteApp,
                                        saved.copy(
                                            accessToken = token,
                                            refreshToken = newRefresh,
                                            userId = userId
                                        )
                                    )
                                    afficherSession(
                                        token, saved.pseudo,
                                        saved.schoolClass, saved.series
                                    )
                                    return@LaunchedEffect
                                }
                                throw e
                            }

                            SecureSessionStore.save(
                                contexteApp,
                                SecureSessionStore.Session(
                                    accessToken = token,
                                    refreshToken = newRefresh,
                                    userId = userId,
                                    pseudo = profile.pseudo,
                                    schoolClass = profile.schoolClass,
                                    series = profile.series.orEmpty()
                                )
                            )
                            afficherSession(
                                token, profile.pseudo,
                                profile.schoolClass, profile.series.orEmpty()
                            )
                        } catch (_: Exception) {
                            // Session refusée par Supabase : demander une nouvelle connexion.
                            SecureSessionStore.clear(contexteApp)
                            historique.clear()
                            historique.add("accueil")
                        }
                    }
                }

                val pageActuelle = historique.last()

                var confirmerSortie by remember { mutableStateOf(false) }

                // Retour Android : confirmation depuis le tableau de bord.
                BackHandler(enabled = true) {
                    if (pageActuelle == "dashboard") {
                        confirmerSortie = true
                    } else if (historique.size > 1) {
                        historique.removeAt(historique.lastIndex)
                    }
                }

                if (confirmerSortie) {
                    AlertDialog(
                        onDismissRequest = { confirmerSortie = false },
                        title = { Text("Quitter Mon profa ?") },
                        text = {
                            Text("Veux-tu vraiment quitter l'application ? Ta session reste enregistrée.")
                        },
                        confirmButton = {
                            TextButton(onClick = { this@MainActivity.finish() }) {
                                Text("Quitter", color = Bleu)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { confirmerSortie = false }) {
                                Text("Annuler", color = Vert)
                            }
                        }
                    )
                }

                when (pageActuelle) {
                    "restauration" -> Box(
                        modifier = Modifier.fillMaxSize().background(Fond),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Bleu)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Vérification de ta session…", color = Texte)
                        }
                    }

                    "accueil" -> WelcomeScreen(
                        onStart = {
                            historique.add("classe")
                        },
                        onLogin = {
                            historique.add("connexion")
                        }
                    )

                    "classe" -> ClassSelectionScreen(
                        selectedClass = classeChoisie,
                        selectedSeries = serieChoisie,
                        onClassChange = { classeChoisie = it },
                        onSeriesChange = { serieChoisie = it },
                        onBack = {
                            if (historique.size > 1) {
                                historique.removeAt(historique.lastIndex)
                            }
                        },
                        onContinue = { classe, serie ->
                            classeChoisie = classe
                            serieChoisie = serie
                            historique.add("inscription")
                        }
                    )

                    "inscription" -> RegistrationScreen(
                        schoolClass = classeChoisie.orEmpty(),
                        series = serieChoisie.orEmpty(),
                        onBack = {
                            historique.removeAt(historique.lastIndex)
                        },
                        onLogin = {
                            historique.add("connexion")
                        },
                        onContinueVerification = { email, pseudo, password, schoolClass, series ->
                            try {
                                val result = SupabaseBackend.signUp(
                                    email = email,
                                    password = password,
                                    pseudo = pseudo,
                                    schoolClass = schoolClass,
                                    series = series
                                )
                                emailInscription = email
                                pseudoUtilisateur = pseudo.trim()
                                classeChoisie = schoolClass
                                serieChoisie = series
                                contexteApp.getSharedPreferences(
                                    "mon_profa", android.content.Context.MODE_PRIVATE
                                ).edit().putString("pseudo", pseudoUtilisateur).apply()

                                if (!result.accessToken.isNullOrBlank()) {
                                    accessToken = result.accessToken
                                    if (!result.refreshToken.isNullOrBlank() &&
                                        !result.userId.isNullOrBlank()
                                    ) {
                                        SecureSessionStore.save(
                                            contexteApp,
                                            SecureSessionStore.Session(
                                                accessToken = result.accessToken,
                                                refreshToken = result.refreshToken,
                                                userId = result.userId,
                                                pseudo = pseudoUtilisateur,
                                                schoolClass = schoolClass,
                                                series = series
                                            )
                                        )
                                    }
                                    historique.add("dashboard")
                                } else {
                                    historique.add("verification")
                                }
                                null
                            } catch (e: Exception) {
                                e.message ?: "Impossible de créer le compte. Réessaie."
                            }
                        }
                    )

                    "verification" -> EmailVerificationScreen(
                        email = emailInscription,
                        onBack = { historique.removeAt(historique.lastIndex) },
                        onContinue = { code ->
                            try {
                                val result = SupabaseBackend.verifySignupCode(
                                    emailInscription, code
                                )
                                accessToken = result.accessToken
                                    ?: throw IllegalStateException(
                                        "Confirmation reçue. Connecte-toi pour continuer."
                                    )
                                if (!result.refreshToken.isNullOrBlank() &&
                                    !result.userId.isNullOrBlank()
                                ) {
                                    SecureSessionStore.save(
                                        contexteApp,
                                        SecureSessionStore.Session(
                                            accessToken = result.accessToken,
                                            refreshToken = result.refreshToken,
                                            userId = result.userId,
                                            pseudo = pseudoUtilisateur,
                                            schoolClass = classeChoisie.orEmpty(),
                                            series = serieChoisie.orEmpty()
                                        )
                                    )
                                }
                                historique.add("dashboard")
                                null
                            } catch (e: Exception) {
                                e.message ?: "Code invalide ou expiré."
                            }
                        },
                        onResend = {
                            try {
                                SupabaseBackend.resendSignupCode(emailInscription)
                                null
                            } catch (e: Exception) {
                                e.message ?: "Impossible de renvoyer le code."
                            }
                        }
                    )

                    "dashboard" -> DashboardScreen(
                        schoolClass = classeChoisie.orEmpty(),
                        series = serieChoisie.orEmpty(),
                        pseudo = pseudoUtilisateur.ifBlank {
                            contexteApp.getSharedPreferences(
                                "mon_profa", android.content.Context.MODE_PRIVATE
                            ).getString("pseudo", "").orEmpty()
                        },
                        email = emailInscription,
                        onUpdateProfile = { nouveauPseudo, nouvelleSerie ->
                            val token = accessToken
                                ?: throw IllegalStateException("Session expirée. Reconnecte-toi.")
                            val session = SecureSessionStore.load(contexteApp)
                                ?: throw IllegalStateException("Session introuvable. Reconnecte-toi.")

                            val profil = SupabaseBackend.updateProfile(
                                accessToken = token,
                                userId = session.userId,
                                pseudo = nouveauPseudo,
                                series = nouvelleSerie
                            )

                            SecureSessionStore.save(
                                contexteApp,
                                session.copy(
                                    accessToken = token,
                                    pseudo = profil.pseudo,
                                    series = profil.series.orEmpty()
                                )
                            )
                            pseudoUtilisateur = profil.pseudo
                            serieChoisie = profil.series.orEmpty()
                            profil
                        },
                        onHome = {
                            confirmerSortie = false
                            historique.clear()
                            historique.add("accueil")
                        },
                        onLogout = {
                            accessToken = null
                            pseudoUtilisateur = ""
                            classeChoisie = null
                            serieChoisie = null
                            emailInscription = ""
                            historique.clear()
                            historique.add("accueil")
                        }
                    )

                    "connexion" -> LoginScreen(
                        onBack = {
                            if (historique.size > 1) {
                                historique.removeAt(historique.lastIndex)
                            }
                        },
                        onRegister = {
                            historique.add("classe")
                        },
                        onLogin = { email, password ->
                            try {
                                val result = SupabaseBackend.signInWithPseudo(email, password)
                                val token = result.accessToken
                                    ?: throw IllegalStateException("Session non reçue.")
                                val userId = result.userId
                                    ?: throw IllegalStateException("Identifiant du compte absent.")
                                val profile = SupabaseBackend.getProfile(token, userId)

                                accessToken = token
                                emailInscription = email
                                pseudoUtilisateur = profile.pseudo
                                classeChoisie = profile.schoolClass
                                serieChoisie = profile.series.orEmpty()
                                contexteApp.getSharedPreferences(
                                    "mon_profa", android.content.Context.MODE_PRIVATE
                                ).edit().putString("pseudo", profile.pseudo).apply()
                                val refresh = result.refreshToken
                                    ?: throw IllegalStateException(
                                        "Supabase n'a pas fourni le jeton de renouvellement. Réessaie."
                                    )
                                SecureSessionStore.save(
                                    contexteApp,
                                    SecureSessionStore.Session(
                                        accessToken = token,
                                        refreshToken = refresh,
                                        userId = userId,
                                        pseudo = profile.pseudo,
                                        schoolClass = profile.schoolClass,
                                        series = profile.series.orEmpty()
                                    )
                                )
                                historique.add("dashboard")
                                null
                            } catch (e: Exception) {
                                e.message ?: "Connexion impossible. Vérifie tes identifiants."
                            }
                        },
                        onRecover = { email ->
                            try {
                                SupabaseBackend.sendPasswordReset(email)
                                null
                            } catch (e: Exception) {
                                e.message ?: "Impossible d'envoyer l'e-mail de récupération."
                            }
                        }
                    )
                }

                var splashVisibleLocal by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(6650)
                    splashVisibleLocal = false
                }
                if (splashVisibleLocal) {
                    MonProfaAnimatedSplash()
                }
                }
            }
        }
    }
}



@Composable
private fun MonProfaAnimatedSplash() {
    val fade = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        fade.animateTo(1f, animationSpec = tween(durationMillis = 450))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF061A68)),
    ) {
        Image(
            painter = painterResource(id = R.drawable.mon_profa_splash_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(72.dp)
                .graphicsLayer { alpha = fade.value }
        )

        Text(
            text = "by Benie groupe",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp)
                .alpha(fade.value),
            color = Color(0xFFF2F7FF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.25.sp
        )
    }
}

@Composable
private fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF9FBFF),
                        Fond,
                        Color(0xFFEDF3FF)
                    )
                )
            )
    ) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 65.dp, y = 115.dp)
                .size(190.dp)
                .alpha(0.38f)
                .background(Color(0xFFDCE8FF), CircleShape)
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-75).dp, y = (-85).dp)
                .size(210.dp)
                .alpha(0.32f)
                .background(Color(0xFFD8F2EB), CircleShape)
        )
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(start = 38.dp, top = 185.dp)
                .size(9.dp)
                .background(Jaune, CircleShape)
        )
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 30.dp)
                .offset(y = (-35).dp)
                .size(7.dp)
                .background(Violet.copy(alpha = 0.65f), CircleShape)
        )
        content()
    }
}

@Composable
private fun BrandLogo(compact: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Mon",
                fontSize = if (compact) 21.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.7).sp,
                color = Texte
            )
            Spacer(Modifier.width(7.dp))
            Text(
                "profa",
                fontSize = if (compact) 21.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.7).sp,
                color = Bleu
            )
            Text(
                "✦",
                modifier = Modifier.padding(start = 2.dp, bottom = if (compact) 12.dp else 20.dp),
                fontSize = if (compact) 10.sp else 15.sp,
                color = Jaune
            )
        }
        Row(
            modifier = Modifier.padding(top = if (compact) 4.dp else 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .width(if (compact) 16.dp else 25.dp)
                    .height(if (compact) 3.dp else 4.dp)
                    .background(Vert, CircleShape)
            )
            Spacer(Modifier.width(5.dp))
            Box(
                Modifier
                    .width(if (compact) 28.dp else 44.dp)
                    .height(if (compact) 3.dp else 4.dp)
                    .background(Jaune, CircleShape)
            )
            Spacer(Modifier.width(5.dp))
            Box(
                Modifier
                    .width(if (compact) 16.dp else 25.dp)
                    .height(if (compact) 3.dp else 4.dp)
                    .background(Violet, CircleShape)
            )
        }
    }
}

@Composable
private fun BookIllustration() {
    Box(
        Modifier
            .size(112.dp)
            .background(
                Color.White.copy(alpha = 0.88f),
                RoundedCornerShape(34.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(72.dp)) {
            val w = size.width
            val h = size.height

            val left = Path().apply {
                moveTo(w * .12f, h * .32f)
                quadraticTo(w * .32f, h * .23f, w * .48f, h * .38f)
                lineTo(w * .48f, h * .78f)
                quadraticTo(w * .29f, h * .63f, w * .12f, h * .70f)
                close()
            }
            val right = Path().apply {
                moveTo(w * .52f, h * .38f)
                quadraticTo(w * .70f, h * .23f, w * .88f, h * .32f)
                lineTo(w * .88f, h * .70f)
                quadraticTo(w * .69f, h * .63f, w * .52f, h * .78f)
                close()
            }

            drawPath(left, Bleu.copy(alpha = .18f))
            drawPath(right, Color(0xFFDCE8FF))
            drawLine(
                Bleu, Offset(w * .5f, h * .37f),
                Offset(w * .5f, h * .8f),
                2.dp.toPx(), cap = StrokeCap.Round
            )
            drawLine(
                Bleu, Offset(w * .2f, h * .43f),
                Offset(w * .37f, h * .45f),
                1.5.dp.toPx(), cap = StrokeCap.Round
            )
            drawLine(
                Bleu, Offset(w * .2f, h * .53f),
                Offset(w * .37f, h * .55f),
                1.5.dp.toPx(), cap = StrokeCap.Round
            )
            drawLine(
                Bleu, Offset(w * .63f, h * .45f),
                Offset(w * .8f, h * .43f),
                1.5.dp.toPx(), cap = StrokeCap.Round
            )
            drawLine(
                Bleu, Offset(w * .63f, h * .55f),
                Offset(w * .8f, h * .53f),
                1.5.dp.toPx(), cap = StrokeCap.Round
            )
            drawCircle(
                Jaune, w * .07f,
                Offset(w * .82f, h * .15f)
            )
        }
    }
}

@Composable
private fun ForwardArrow() {
    Canvas(Modifier.size(22.dp)) {
        val stroke = 2.2.dp.toPx()
        val startX = size.width * .12f
        val endX = size.width * .84f
        val middle = size.height * .5f

        drawLine(
            Color.White, Offset(startX, middle),
            Offset(endX, middle), stroke, cap = StrokeCap.Round
        )
        drawLine(
            Color.White,
            Offset(size.width * .56f, size.height * .20f),
            Offset(endX, middle), stroke, cap = StrokeCap.Round
        )
        drawLine(
            Color.White, Offset(endX, middle),
            Offset(size.width * .56f, size.height * .80f),
            stroke, cap = StrokeCap.Round
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Bleu,
            contentColor = Color.White,
            disabledContainerColor = Bleu.copy(alpha = .35f),
            disabledContentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 5.dp,
            pressedElevation = 1.dp
        )
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(14.dp))
        Box(
            Modifier
                .size(40.dp)
                .background(BleuClair, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            ForwardArrow()
        }
    }
}

@Composable
private fun WelcomeScreen(
    onStart: () -> Unit,
    onLogin: () -> Unit
) {
    AppBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            BrandLogo()

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BookIllustration()
                Spacer(Modifier.height(35.dp))
                Text(
                    "Nous allons procéder à la configuration de ton espace.",
                    fontSize = 19.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Medium,
                    color = Texte,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(25.dp))
                Text(
                    "Tu es prêt ?",
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-.7).sp,
                    color = Bleu,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton("Commencer", onStart)

                Spacer(Modifier.height(8.dp))

                Text(
                    "J'ai déjà un compte",
                    modifier = Modifier
                        .clickable { onLogin() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    color = Bleu,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ClassSelectionScreen(
    selectedClass: String?,
    selectedSeries: String?,
    onClassChange: (String) -> Unit,
    onSeriesChange: (String?) -> Unit,
    onBack: () -> Unit,
    onContinue: (String, String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AppBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLogo()

            Row(
                Modifier.fillMaxWidth().padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("‹ Retour", color = Bleu, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Choisis ta classe",
                fontSize = 29.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-.6).sp,
                color = Bleu,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Sélectionne ta classe pour personnaliser ton espace.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = Texte.copy(alpha = .78f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            ClassOptionCard(
                title = "3e",
                subtitle = "Préparation au BEPC",
                selected = selectedClass == "3e",
                accent = Vert,
                onClick = {
                    onClassChange("3e")
                    onSeriesChange(null)
                }
            )

            Spacer(Modifier.height(14.dp))

            ClassOptionCard(
                title = "Terminale",
                subtitle = "Préparation au BAC",
                selected = selectedClass == "Terminale",
                accent = Violet,
                onClick = {
                    onClassChange("Terminale")
                    onSeriesChange(null)
                }
            )

            if (selectedClass == "Terminale") {
                Spacer(Modifier.height(22.dp))

                Column(Modifier.fillMaxWidth()) {
                    Text(
                        "Choisis ta série",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texte
                    )

                    Spacer(Modifier.height(10.dp))

                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(
                                1.dp,
                                if (selectedSeries != null) Bleu
                                else Color(0xFFD9E2F2)
                            ),
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 17.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedSeries?.let { "Série $it" }
                                        ?: "Sélectionner une série",
                                    fontSize = 16.sp,
                                    fontWeight = if (selectedSeries != null)
                                        FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedSeries != null) Bleu
                                        else Texte.copy(alpha = .65f)
                                )
                                Text(
                                    if (expanded) "⌃" else "⌄",
                                    fontSize = 22.sp,
                                    color = Bleu
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("A", "C", "D").forEach { serie ->
                                DropdownMenuItem(
                                    text = { Text("Série $serie") },
                                    onClick = {
                                        onSeriesChange(serie)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = "Continuer",
                onClick = {
                    if (selectedClass != null) {
                        onContinue(selectedClass, selectedSeries)
                    }
                },
                enabled = selectedClass == "3e" ||
                    (selectedClass == "Terminale" && selectedSeries != null)
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ClassOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFEAF0FF)
                else Color.White
        ),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) Bleu else Color(0xFFE2E9F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 3.dp else 1.dp
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(
                        accent.copy(alpha = .13f),
                        RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (title == "3e") "3e" else "T",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
            }

            Spacer(Modifier.width(15.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Texte
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    fontSize = 14.sp,
                    color = Texte.copy(alpha = .68f)
                )
            }

            Box(
                Modifier
                    .size(24.dp)
                    .background(
                        if (selected) Bleu else Color(0xFFE7ECF5),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Canvas(Modifier.size(14.dp)) {
                        drawLine(
                            Color.White,
                            Offset(size.width * .18f, size.height * .52f),
                            Offset(size.width * .42f, size.height * .75f),
                            2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            Color.White,
                            Offset(size.width * .42f, size.height * .75f),
                            Offset(size.width * .84f, size.height * .25f),
                            2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Texte
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    color = Texte.copy(alpha = .45f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (password && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = {
                if (password) {
                    Text(
                        if (passwordVisible) "Masquer" else "Voir",
                        modifier = Modifier
                            .clickable { passwordVisible = !passwordVisible }
                            .padding(horizontal = 10.dp),
                        color = Bleu,
                        fontSize = 12.sp
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Bleu,
                unfocusedBorderColor = Color(0xFFD9E2F2),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Texte,
                unfocusedTextColor = Texte,
                cursorColor = Bleu
            )
        )
    }
}

@Composable
private fun RegistrationScreen(
    schoolClass: String,
    series: String,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onContinueVerification: suspend (String, String, String, String, String) -> String?
) {
    var pseudo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val emailValide =
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    val formulaireValide =
        pseudo.trim().matches(Regex("^[A-Za-z0-9_.]{3,20}$")) &&
        emailValide &&
        motDePasse.length >= 8 &&
        confirmation == motDePasse

    AppBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLogo()

            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("‹ Retour", color = Bleu, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Créer mon compte",
                fontSize = 28.sp,
                lineHeight = 35.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Bleu,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(9.dp))

            Text(
                "Renseigne tes informations pour préparer ton espace.",
                fontSize = 15.sp,
                lineHeight = 23.sp,
                color = Texte.copy(alpha = .78f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E9F5))
            ) {
                Text(
                    text = if (schoolClass == "Terminale") {
                        "Classe sélectionnée : Terminale • Série $series"
                    } else {
                        "Classe sélectionnée : ${schoolClass.ifEmpty { "Non sélectionnée" }}"
                    },
                    modifier = Modifier.padding(14.dp),
                    color = Bleu,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(18.dp))

            FormField(
                label = "Choisis ton pseudo",
                value = pseudo,
                onValueChange = {
                    pseudo = it
                    message = ""
                },
                placeholder = "Ex. Junior225"
            )

            Spacer(Modifier.height(12.dp))

            FormField(
                label = "Adresse e-mail",
                value = email,
                onValueChange = {
                    email = it
                    message = ""
                },
                placeholder = "exemple@email.com",
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(12.dp))

            FormField(
                label = "Mot de passe",
                value = motDePasse,
                onValueChange = {
                    motDePasse = it
                    message = ""
                },
                placeholder = "Au moins 8 caractères",
                keyboardType = KeyboardType.Password,
                password = true
            )

            Spacer(Modifier.height(12.dp))

            FormField(
                label = "Confirmer le mot de passe",
                value = confirmation,
                onValueChange = {
                    confirmation = it
                    message = ""
                },
                placeholder = "Retape ton mot de passe",
                keyboardType = KeyboardType.Password,
                password = true
            )

            if (motDePasse.isNotEmpty() && motDePasse.length < 8) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Le mot de passe doit contenir au moins 8 caractères.",
                    color = Color(0xFFB54747),
                    fontSize = 12.sp
                )
            }

            if (confirmation.isNotEmpty() && confirmation != motDePasse) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Les mots de passe ne correspondent pas.",
                    color = Color(0xFFB54747),
                    fontSize = 12.sp
                )
            }

            if (message.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    message,
                    color = Bleu,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = if (loading) "Création du compte..." else "Créer mon compte",
                enabled = formulaireValide && !loading,
                onClick = {
                    scope.launch {
                        loading = true
                        message = onContinueVerification(
                            email.trim(), pseudo.trim(), motDePasse,
                            schoolClass, series
                        ).orEmpty()
                        loading = false
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Tu as déjà un compte ? ",
                    color = Texte.copy(alpha = .7f),
                    fontSize = 13.sp
                )
                Text(
                    "Se connecter",
                    modifier = Modifier
                        .clickable { onLogin() }
                        .padding(vertical = 10.dp),
                    color = Bleu,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LoginScreen(
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onLogin: suspend (String, String) -> String?,
    onRecover: suspend (String) -> String?
) {
    var pseudo by remember { mutableStateOf("") }
    var emailRecuperation by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var modeRecuperation by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val pseudoConnexionValide = pseudo.trim().matches(Regex("^[A-Za-z0-9_.]{3,20}$"))
    val emailValide = Patterns.EMAIL_ADDRESS.matcher(emailRecuperation.trim()).matches()

    AppBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLogo()

            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("‹ Retour", color = Bleu, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                if (modeRecuperation) "Mot de passe oublié ?"
                else "Content de te revoir !",
                fontSize = 28.sp,
                lineHeight = 35.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Bleu,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                if (modeRecuperation) {
                    "Saisis ton adresse e-mail pour recevoir les instructions de récupération."
                } else {
                    "Connecte-toi pour retrouver ton espace d'apprentissage."
                },
                fontSize = 15.sp,
                lineHeight = 23.sp,
                color = Texte.copy(alpha = .78f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            if (modeRecuperation) {
                FormField(
                    label = "Adresse e-mail",
                    value = emailRecuperation,
                    onValueChange = {
                        emailRecuperation = it
                        message = ""
                    },
                    placeholder = "exemple@email.com",
                    keyboardType = KeyboardType.Email
                )
            } else {
                FormField(
                    label = "Pseudo",
                    value = pseudo,
                    onValueChange = {
                        pseudo = it
                        message = ""
                    },
                    placeholder = "Ton pseudo",
                    keyboardType = KeyboardType.Text
                )
            }

            if (!modeRecuperation) {
                Spacer(Modifier.height(16.dp))

                FormField(
                    label = "Mot de passe",
                    value = motDePasse,
                    onValueChange = {
                        motDePasse = it
                        message = ""
                    },
                    placeholder = "Ton mot de passe",
                    keyboardType = KeyboardType.Password,
                    password = true
                )

                Text(
                    "Mot de passe oublié ?",
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable {
                            modeRecuperation = true
                            message = ""
                        }
                        .padding(vertical = 12.dp),
                    color = Bleu,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Spacer(Modifier.height(14.dp))
            }

            if (message.isNotEmpty()) {
                Text(
                    message,
                    color = Bleu,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
            }

            PrimaryButton(
                text = when {
                    loading -> "Patiente..."
                    modeRecuperation -> "Envoyer les instructions"
                    else -> "Se connecter"
                },
                enabled = !loading && if (modeRecuperation) emailValide
                    else pseudoConnexionValide && motDePasse.isNotEmpty(),
                onClick = {
                    scope.launch {
                        loading = true
                        message = if (modeRecuperation) {
                            val error = onRecover(emailRecuperation.trim())
                            error ?: "Si cette adresse correspond à un compte, un e-mail de récupération a été envoyé."
                        } else {
                            onLogin(pseudo.trim(), motDePasse).orEmpty()
                        }
                        loading = false
                    }
                }
            )

            if (modeRecuperation) {
                Text(
                    "Retour à la connexion",
                    modifier = Modifier
                        .clickable {
                            modeRecuperation = false
                            message = ""
                        }
                        .padding(vertical = 14.dp),
                    color = Bleu,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Spacer(Modifier.height(18.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Pas encore de compte ? ",
                        color = Texte.copy(alpha = .7f),
                        fontSize = 13.sp
                    )
                    Text(
                        "S'inscrire",
                        modifier = Modifier
                            .clickable { onRegister() }
                            .padding(vertical = 10.dp),
                        color = Bleu,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
private fun EmailVerificationScreen(
    email: String,
    onBack: () -> Unit,
    onContinue: suspend (String) -> String?,
    onResend: suspend () -> String?
) {
    var code by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var verifying by remember { mutableStateOf(false) }
    var resending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AppBackground {
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLogo()

            Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                TextButton(onClick = onBack) {
                    Text("‹ Retour", color = Bleu)
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("✉️", fontSize = 42.sp)
            Spacer(Modifier.height(14.dp))

            Text(
                "Nous y sommes presque !",
                fontSize = 27.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Bleu,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))
            Text(
                "Renseigne le code reçu par mail et on bouge. 😉",
                fontSize = 15.sp,
                lineHeight = 23.sp,
                color = Texte.copy(alpha = .78f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(email, color = Vert, fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center)

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E9F5))
            ) {
                Column(
                    Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Ton code à 6 chiffres",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texte
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Saisis les six chiffres pour continuer.",
                        fontSize = 13.sp,
                        color = Texte.copy(alpha = .7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it.filter(Char::isDigit).take(6)
                            message = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Code de vérification") },
                        placeholder = { Text("123456") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Bleu,
                            unfocusedBorderColor = Color(0xFFD9E2F2),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Texte,
                            unfocusedTextColor = Texte,
                            cursorColor = Bleu
                        )
                    )

                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(
                        text = if (verifying) "Vérification..." else "Confirmer mon compte",
                        enabled = code.length == 6 && !verifying,
                        onClick = {
                            scope.launch {
                                verifying = true
                                message = onContinue(code).orEmpty()
                                verifying = false
                            }
                        }
                    )

                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (resending) "Envoi du code..." else "Tu n'as rien reçu ? Renvoyer le code",
                        modifier = Modifier.clickable {
                            if (!resending) {
                                scope.launch {
                                    resending = true
                                    message = onResend()
                                        ?: "Si ton inscription est en attente, un nouveau code a été envoyé."
                                    resending = false
                                }
                            }
                        }.padding(vertical = 10.dp),
                        color = Bleu,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    if (message.isNotEmpty()) {
                        Text(
                            message,
                            color = Texte.copy(alpha = .75f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ton code est vérifié par le service sécurisé d'authentification.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Texte.copy(alpha = .58f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}

private fun dashboardGreeting(
    context: android.content.Context,
    pseudo: String
): String {
    val calendrier = java.util.Calendar.getInstance()
    val heure = calendrier.get(java.util.Calendar.HOUR_OF_DAY)

    val periode = when {
        heure < 12 -> "matin"
        heure < 18 -> "apres_midi"
        else -> "soir"
    }

    val dateKey = "salutation_${calendrier.get(java.util.Calendar.YEAR)}_" +
        "${calendrier.get(java.util.Calendar.DAY_OF_YEAR)}_$periode"

    val preferences = context.getSharedPreferences(
        "mon_profa_salutations",
        android.content.Context.MODE_PRIVATE
    )
    val visite = preferences.getInt(dateKey, 0)
    preferences.edit().putInt(dateKey, visite + 1).apply()

    val nom = pseudo.trim().ifBlank { "à toi" }

    val salutations = when (periode) {
        "matin" -> listOf(
            "Bonjour, $nom ☀️",
            "C'est comment, $nom ? 👋",
            "On révise quoi, $nom ? 📚",
            "Content de te revoir, $nom ! 💙",
            "Prêt(e) à progresser, $nom ? 💪"
        )
        "apres_midi" -> listOf(
            "Bon après-midi, $nom ! ☀️",
            "Alors, $nom, on avance ? 📚",
            "Tu es prêt(e), $nom ? 💪",
            "Heureux de te revoir, $nom ! 💙",
            "On continue les révisions, $nom ? ✍️"
        )
        else -> listOf(
            "Bonsoir, $nom ! 🌙",
            "Tu m'as manqué, $nom ! 💙",
            "Une dernière révision, $nom ? 📚",
            "Bonsoir et bienvenue, $nom ! ✨",
            "On termine la journée en apprenant, $nom ? 🌟"
        )
    }

    return salutations[visite % salutations.size]
}



@Composable
private fun DashboardScreen(
    schoolClass: String,
    series: String,
    pseudo: String,
    email: String,
    onUpdateProfile: suspend (String, String) -> SupabaseBackend.StudentProfile,
    onHome: () -> Unit,
    onLogout: () -> Unit
) {
    val contexte = androidx.compose.ui.platform.LocalContext.current
    val pile = remember {
        mutableStateListOf("dashboard")
    }
    val page = pile.last()
    var confirmerDeconnexion by remember { mutableStateOf(false) }
    var confirmerQuitter by remember { mutableStateOf(false) }

    fun ouvrir(destination: String) {
        if (pile.lastOrNull() != destination) pile.add(destination)
    }

    fun retourPage() {
        if (pile.size > 1) pile.removeAt(pile.lastIndex)
    }

    BackHandler(enabled = pile.size > 1) {
        retourPage()
    }

    if (confirmerQuitter) {
        AlertDialog(
            onDismissRequest = { confirmerQuitter = false },
            title = { Text("Déjà sur le départ ? 💙") },
            text = {
                Text("Tu nous quittes déjà ? Reviens-nous vite, nous t'attendons !")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmerQuitter = false
                    onHome()
                }) { Text("Quitter", color = Bleu) }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmerQuitter = false
                }) { Text("Rester", color = Vert) }
            }
        )
    }

    if (confirmerDeconnexion) {
        AlertDialog(
            onDismissRequest = { confirmerDeconnexion = false },
            title = { Text("Se déconnecter ?") },
            text = {
                Text("Ta session sera supprimée de cet appareil. Reviens-nous vite ! 💙")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmerDeconnexion = false
                    SecureSessionStore.clear(contexte)
                    onLogout()
                }) { Text("Me déconnecter", color = Bleu) }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmerDeconnexion = false
                }) { Text("Annuler", color = Vert) }
            }
        )
    }

    if (page == "dashboard") {
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrandLogo(compact = true)

                    Surface(
                        onClick = { ouvrir("Profil") },
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E9F5))
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 22.sp)
                        }
                    }
                }

                Spacer(Modifier.height(25.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp)
                ) {
                    Text(
                        dashboardGreeting(contexte, pseudo),
                        fontSize = 25.sp,
                        lineHeight = 33.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Bleu
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        listOf(
                            "Alors, on fait travailler ces neurones ? 🧠",
                            "Les bonnes notes ne vont pas se gagner toutes seules ! 😉",
                            "Prêt à montrer ce que tu sais faire ? 🚀",
                            "Aujourd'hui encore, on devient plus fort ! 💪",
                            "Allez, on apprend un petit quelque chose ? 📚"
                        ).let { phrases ->
                            val jour = java.util.Calendar.getInstance()
                                .get(java.util.Calendar.DAY_OF_YEAR)
                            phrases[jour % phrases.size]
                        },
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = Texte.copy(alpha = 0.76f)
                    )
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    "Ton espace de travail",
                    fontSize = 21.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Texte
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "Choisis ce que tu veux faire aujourd'hui.",
                    fontSize = 13.sp,
                    color = Texte.copy(alpha = 0.70f)
                )
                Spacer(Modifier.height(15.dp))

                val rubriques = listOf(
                    Triple("📚", "Mes disciplines", Bleu),
                    Triple("🗓️", "Mes cours du jour", Vert),
                    Triple("🏆", "Le challenge", Violet),
                    Triple("✍️", "Exercice", Color(0xFFE59A18)),
                    Triple("🎙️", "Faire un boualo", Violet),
                    Triple("📅", "Mon emploi du temps", Vert),
                    Triple("⚙️", "Paramètre", Color(0xFF64748B)),
                    Triple("❔", "Aide", BleuClair),
                    Triple("🛠️", "Signaler un problème", Color(0xFFD45A55))
                )

                rubriques.chunked(2).forEach { ligne ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        ligne.forEach { item ->
                            val description = when (item.second) {
                                "Mes disciplines" -> "Tes matières et tes leçons"
                                "Mes cours du jour" -> "Ce que tu dois étudier"
                                "Le challenge" -> "Teste tes connaissances"
                                "Exercice" -> "Entraîne-toi à ton rythme"
                                "Faire un boualo" -> "Récite et mémorise tes cours"
                                "Mon emploi du temps" -> "Organise ta semaine"
                                "Paramètre" -> "Personnalise ton application"
                                "Aide" -> "Trouve des réponses à tes questions"
                                else -> "Besoin de nous signaler quelque chose ?"
                            }

                            Surface(
                                onClick = {
                                    ouvrir(item.second)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 10.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White,
                                border = BorderStroke(
                                    1.dp, Color(0xFFE4EAF5)
                                ),
                                shadowElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 145.dp)
                                        .padding(13.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(13.dp),
                                        color = item.third.copy(alpha = 0.12f)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(43.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(item.first, fontSize = 23.sp)
                                        }
                                    }

                                    Spacer(Modifier.height(13.dp))
                                    Text(
                                        item.second,
                                        fontSize = 14.sp,
                                        lineHeight = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Texte
                                    )
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        description,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = Texte.copy(alpha = 0.68f)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Explorer",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = item.third
                                        )
                                        Text(
                                            "›",
                                            fontSize = 21.sp,
                                            color = item.third
                                        )
                                    }
                                }
                            }
                        }

                        if (ligne.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Chaque petite révision te rapproche de ton objectif. ✨",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    color = Texte.copy(alpha = 0.68f)
                )
                Spacer(Modifier.height(18.dp))
            }
        }
    } else {
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { retourPage() }) {
                        Text("‹ Retour", color = Bleu)
                    }
                    Spacer(Modifier.weight(1f))
                    BrandLogo(compact = true)
                }

                Spacer(Modifier.height(20.dp))

                when (page) {
                    "Profil" -> {
                        Text(
                            "Mon profil 👤",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Bleu
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Tes informations personnelles",
                            fontSize = 14.sp,
                            color = Texte.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(20.dp))

                        CarteProfil("Pseudo", pseudo.ifBlank { "Non renseigné" })
                        CarteProfil("Adresse e-mail", email.ifBlank {
                            "Adresse à vérifier"
                        })
                        CarteProfil(
                            "Niveau scolaire",
                            schoolClass.ifBlank { "Non renseigné" }
                        )
                        if (series.isNotBlank()) {
                            CarteProfil("Série", series)
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(
                            "La modification du pseudo, de l'adresse e-mail et du mot de passe sera activée dans la prochaine étape avec vérification sécurisée.",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Texte.copy(alpha = 0.72f)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { ouvrir("Modifier mes informations") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gérer mes informations")
                        }
                    }

                    "Paramètre" -> {
                        Text(
                            "Paramètres ⚙️",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Bleu
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            "Configure Mon profa selon tes besoins.",
                            fontSize = 14.sp,
                            color = Texte.copy(alpha = 0.72f)
                        )
                        Spacer(Modifier.height(20.dp))

                        listOf(
                            "Profil" to "Tes informations personnelles",
                            "Thème" to "Apparence de l'application",
                            "Conditions d'utilisation" to "Les règles d'utilisation",
                            "Confidentialité" to "Protection de tes données",
                            "Notifications" to "Préférences de notification",
                            "Version de l'application" to
                                "Informations sur cette version"
                        ).forEach { (titre, sousTitre) ->
                            LigneParametre(
                                titre = titre,
                                description = sousTitre,
                                onClick = {
                                    if (titre == "Profil") ouvrir("Profil")
                                    else ouvrir(titre)
                                }
                            )
                        }

                        Spacer(Modifier.height(18.dp))
                        OutlinedButton(
                            onClick = { confirmerDeconnexion = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFC43D4B)
                            )
                        ) {
                            Text("Se déconnecter")
                        }
                    }

                    "Modifier mes informations" -> {
                        var pseudoSaisi by remember(pseudo) {
                            mutableStateOf(pseudo)
                        }
                        var serieSaisie by remember(series) {
                            mutableStateOf(series)
                        }
                        var enregistrementEnCours by remember {
                            mutableStateOf(false)
                        }
                        var messageProfil by remember {
                            mutableStateOf<String?>(null)
                        }
                        val portee = rememberCoroutineScope()

                        Text(
                            "Gérer mes informations",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Bleu
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Modifie ton pseudo et ta série. Ton niveau scolaire ne peut pas être modifié ici.",
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = Texte.copy(alpha = 0.75f)
                        )
                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = pseudoSaisi,
                            onValueChange = {
                                pseudoSaisi = it
                                messageProfil = null
                            },
                            label = { Text("Pseudo") },
                            singleLine = true,
                            enabled = !enregistrementEnCours,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(14.dp))

                        OutlinedTextField(
                            value = serieSaisie,
                            onValueChange = {
                                serieSaisie = it
                                messageProfil = null
                            },
                            label = { Text("Série") },
                            placeholder = { Text("Ex. : A, C, D…") },
                            singleLine = true,
                            enabled = !enregistrementEnCours,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(14.dp))

                        CarteProfil(
                            "Niveau scolaire — non modifiable",
                            schoolClass.ifBlank { "Non renseigné" }
                        )

                        messageProfil?.let { message ->
                            Spacer(Modifier.height(10.dp))
                            Text(
                                message,
                                color = if (message.startsWith("Profil enregistré")) {
                                    Color(0xFF238636)
                                } else {
                                    Color(0xFFC43D4B)
                                },
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }

                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = {
                                val pseudoNettoye = pseudoSaisi.trim()
                                if (pseudoNettoye.length !in 3..20) {
                                    messageProfil =
                                        "Le pseudo doit contenir entre 3 et 20 caractères."
                                } else {
                                    portee.launch {
                                        enregistrementEnCours = true
                                        messageProfil = null
                                        try {
                                            onUpdateProfile(
                                                pseudoNettoye,
                                                serieSaisie.trim()
                                            )
                                            messageProfil =
                                                "Profil enregistré avec succès."
                                        } catch (e: Exception) {
                                            messageProfil = e.message
                                                ?: "Échec de l'enregistrement. Réessaie."
                                        } finally {
                                            enregistrementEnCours = false
                                        }
                                    }
                                }
                            },
                            enabled = !enregistrementEnCours,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (enregistrementEnCours) {
                                    "Enregistrement…"
                                } else {
                                    "Enregistrer les modifications"
                                }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { retourPage() },
                            enabled = !enregistrementEnCours,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retour au profil")
                        }
                    }

                    "Thème" -> PageInformation(
                        "Thème",
                        "Le choix du thème sera activé dans une prochaine mise à jour."
                    )

                    "Conditions d'utilisation" -> PageInformation(
                        "Conditions d'utilisation",
                        "Les conditions d'utilisation seront présentées ici après validation de leur texte officiel."
                    )

                    "Confidentialité" -> PageInformation(
                        "Confidentialité",
                        "Cette page présentera les données utilisées par Mon profa, leur traitement et les droits de l'utilisateur."
                    )

                    "Notifications" -> PageInformation(
                        "Notifications",
                        "Les préférences de notification seront disponibles ici lorsque les notifications seront configurées."
                    )

                    "Version de l'application" -> PageInformation(
                        "Version de l'application",
                          "Version de l’application non renseignée"
                    )

                    else -> {
                        RubriqueMonProfaScreen(
                            titre = page,
                            onRetour = { retourPage() }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CarteProfil(titre: String, valeur: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(17.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E9F5))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                titre,
                fontSize = 12.sp,
                color = Texte.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(5.dp))
            Text(
                valeur,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Texte
            )
        }
    }
}

@Composable
private fun LigneParametre(
    titre: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE4EAF5))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    titre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Texte
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    fontSize = 12.sp,
                    color = Texte.copy(alpha = 0.68f)
                )
            }
            Text("›", fontSize = 25.sp, color = Bleu)
        }
    }
}

@Composable
private fun PageInformation(titre: String, texte: String) {
    Text(
        titre,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Bleu
    )
    Spacer(Modifier.height(16.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E9F5))
    ) {
        Text(
            texte,
            modifier = Modifier.padding(19.dp),
            fontSize = 15.sp,
            lineHeight = 24.sp,
            color = Texte
        )
    }
}

@Composable
private fun RubriqueMonProfaScreen(
    titre: String,
    onRetour: () -> Unit
) {
    val description = when (titre) {
        "Mes disciplines" ->
            "Retrouve ici tes matières scolaires et les contenus de cours."
        "Mes révisions du jour" ->
            "Organise tes révisions quotidiennes et suis les notions à revoir."
        "Mon emploi du temps" ->
            "Retrouve ici ton planning de cours et tes séances de révision."
        "Exercice" ->
            "Retrouve des exercices pour t'entraîner et progresser."
        "Boualo" ->
            "Ton espace de récitation audio : prépare tes leçons à réciter. L'enregistrement et la réécoute seront ajoutés dans une prochaine étape."
        "Défi du samedi" ->
            "Un défi hebdomadaire pour tester tes connaissances et te mesurer aux autres élèves. Les sujets, le chronomètre et le classement nécessitent encore leur mise en place."
        "Paramètre" ->
            "Les options de ton application seront regroupées dans cet espace."
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp)
    ) {
            TextButton(onClick = onRetour) {
                Text("‹ Retour au tableau de bord", color = Bleu)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                titre,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Bleu
            )
            Spacer(Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E9F5))
            ) {
                Text(
                    description,
                    modifier = Modifier.padding(20.dp),
                    fontSize = 16.sp,
                    lineHeight = 25.sp,
                    color = Texte
                )
            }
        }
}
