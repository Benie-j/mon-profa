package ci.monprofa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Blue = Color(0xFF2457C5)
private val Green = Color(0xFF15966A)
private val Yellow = Color(0xFFFFC857)
private val Violet = Color(0xFF8457D9)
private val PaleBackground = Color(0xFFF5F8FF)
private val DarkText = Color(0xFF202B45)
private val MutedText = Color(0xFF74809A)
private val BorderColor = Color(0xFFDCE5F7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var page by remember { mutableStateOf("welcome") }
                var selectedClass by remember { mutableStateOf("") }
                var selectedSeries by remember { mutableStateOf("") }

                when (page) {
                    "welcome" -> WelcomeScreen(
                        onStart = { page = "class" },
                        onLogin = { page = "login" }
                    )

                    "class" -> ClassSelectionScreen(
                        onBack = { page = "welcome" },
                        onContinue = { schoolClass, series ->
                            selectedClass = schoolClass
                            selectedSeries = series
                            page = "register"
                        }
                    )

                    "register" -> RegistrationScreen(
                        schoolClass = selectedClass,
                        series = selectedSeries,
                        onBack = { page = "class" },
                        onLogin = { page = "login" }
                    )

                    "login" -> LoginScreen(
                        onBack = { page = "welcome" },
                        onRegister = { page = "register" }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PaleBackground,
                        Color(0xFFF0F5FF),
                        Color(0xFFFAFBFF)
                    )
                )
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            drawCircle(
                color = Blue.copy(alpha = 0.045f),
                radius = 170.dp.toPx(),
                center = Offset(size.width * 0.92f, 30.dp.toPx())
            )
            drawCircle(
                color = Violet.copy(alpha = 0.035f),
                radius = 115.dp.toPx(),
                center = Offset(size.width * 0.05f, 180.dp.toPx())
            )
        }

        content()
    }
}

@Composable
private fun BrandLogo() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Blue),
            contentAlignment = Alignment.Center
        ) {
            BookIllustration()
        }

        Column {
            Text(
                text = "Mon profa",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Blue
            )
            Text(
                text = "Apprendre, comprendre, réussir.",
                fontSize = 11.sp,
                color = MutedText
            )
        }
    }
}

@Composable
private fun BookIllustration() {
    Canvas(modifier = Modifier.size(32.dp)) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.12f, h * 0.22f),
            size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.60f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
        )

        drawLine(
            color = Blue,
            start = Offset(w * 0.50f, h * 0.28f),
            end = Offset(w * 0.50f, h * 0.78f),
            strokeWidth = 1.5.dp.toPx()
        )

        drawLine(
            color = Green,
            start = Offset(w * 0.23f, h * 0.40f),
            end = Offset(w * 0.42f, h * 0.40f),
            strokeWidth = 1.5.dp.toPx()
        )

        drawLine(
            color = Green,
            start = Offset(w * 0.23f, h * 0.52f),
            end = Offset(w * 0.42f, h * 0.52f),
            strokeWidth = 1.5.dp.toPx()
        )

        drawLine(
            color = Violet,
            start = Offset(w * 0.58f, h * 0.40f),
            end = Offset(w * 0.78f, h * 0.40f),
            strokeWidth = 1.5.dp.toPx()
        )

        drawLine(
            color = Violet,
            start = Offset(w * 0.58f, h * 0.52f),
            end = Offset(w * 0.78f, h * 0.52f),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
private fun ForwardArrow(color: Color = Color.White) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val startX = size.width * 0.16f
        val endX = size.width * 0.84f
        val middleY = size.height * 0.50f

        drawLine(
            color = color,
            start = Offset(startX, middleY),
            end = Offset(endX, middleY),
            strokeWidth = 2.2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.58f, size.height * 0.24f),
            end = Offset(endX, middleY),
            strokeWidth = 2.2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.58f, size.height * 0.76f),
            end = Offset(endX, middleY),
            strokeWidth = 2.2.dp.toPx()
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue,
            contentColor = Color.White,
            disabledContainerColor = Blue.copy(alpha = 0.35f),
            disabledContentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(12.dp))
        ForwardArrow()
    }
}

@Composable
private fun PageHeading(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkText
        )
        Text(
            text = subtitle,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = MutedText
        )
    }
}

@Composable
private fun WelcomeScreen(
    onStart: () -> Unit,
    onLogin: () -> Unit
) {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 26.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            BrandLogo()

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(42.dp))
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Yellow.copy(alpha = 0.28f),
                            radius = size.minDimension * 0.36f,
                            center = Offset(size.width * 0.72f, size.height * 0.26f)
                        )
                        drawCircle(
                            color = Green.copy(alpha = 0.12f),
                            radius = size.minDimension * 0.25f,
                            center = Offset(size.width * 0.24f, size.height * 0.76f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Blue),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(78.dp)) {
                            val w = size.width
                            val h = size.height

                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(w * 0.08f, h * 0.24f),
                                size = androidx.compose.ui.geometry.Size(
                                    w * 0.84f,
                                    h * 0.55f
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                    9.dp.toPx()
                                )
                            )
                            drawLine(
                                color = Blue,
                                start = Offset(w * 0.50f, h * 0.29f),
                                end = Offset(w * 0.50f, h * 0.76f),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = Green,
                                start = Offset(w * 0.19f, h * 0.40f),
                                end = Offset(w * 0.42f, h * 0.40f),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = Green,
                                start = Offset(w * 0.19f, h * 0.52f),
                                end = Offset(w * 0.42f, h * 0.52f),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = Violet,
                                start = Offset(w * 0.58f, h * 0.40f),
                                end = Offset(w * 0.81f, h * 0.40f),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = Violet,
                                start = Offset(w * 0.58f, h * 0.52f),
                                end = Offset(w * 0.81f, h * 0.52f),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Bienvenue sur Mon profa !",
                        fontSize = 25.sp,
                        lineHeight = 31.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Nous allons procéder à la configuration de ton espace.",
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                        color = MutedText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Tu es prêt ?",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = "Commencer",
                    onClick = onStart
                )

                Text(
                    text = "J'ai déjà un compte",
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLogin() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue
                )
            }
        }
    }
}

@Composable
private fun ClassSelectionScreen(
    onBack: () -> Unit,
    onContinue: (String, String) -> Unit
) {
    var selectedClass by remember { mutableStateOf("3e") }
    var selectedSeries by remember { mutableStateOf("") }
    var showSeriesMenu by remember { mutableStateOf(false) }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            BrandLogo()

            PageHeading(
                title = "Choisis ta classe",
                subtitle = "Nous allons adapter ton espace à ton niveau scolaire."
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ClassOptionCard(
                    title = "3e",
                    subtitle = "Préparation au BEPC",
                    accent = Green,
                    selected = selectedClass == "3e",
                    onClick = {
                        selectedClass = "3e"
                        selectedSeries = ""
                        showSeriesMenu = false
                    }
                )

                ClassOptionCard(
                    title = "Terminale",
                    subtitle = "Préparation au BAC",
                    accent = Violet,
                    selected = selectedClass == "Terminale",
                    onClick = {
                        selectedClass = "Terminale"
                        showSeriesMenu = true
                    }
                )
            }

            if (selectedClass == "Terminale") {
                Text(
                    text = "Choisis ta série",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("A", "C", "D").forEach { series ->
                        val selected = selectedSeries == series
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(15.dp))
                                .background(
                                    if (selected) Blue else Color.White
                                )
                                .clickable { selectedSeries = series }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Série $series",
                                color = if (selected) Color.White else DarkText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            PrimaryButton(
                text = "Continuer",
                enabled = selectedClass == "3e" ||
                    (selectedClass == "Terminale" && selectedSeries.isNotEmpty()),
                onClick = {
                    onContinue(selectedClass, selectedSeries)
                }
            )

            Text(
                text = "Retour",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onBack() }
                    .padding(10.dp),
                color = MutedText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ClassOptionCard(
    title: String,
    subtitle: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = accent,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = DarkText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = MutedText,
                fontSize = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) Blue else Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BorderColor.copy(alpha = 0.6f))
                )
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
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MutedText.copy(alpha = 0.8f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (password && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = {
                if (password) {
                    Text(
                        text = if (passwordVisible) "Masquer" else "Voir",
                        modifier = Modifier
                            .clickable { passwordVisible = !passwordVisible }
                            .padding(horizontal = 10.dp),
                        color = Blue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                cursorColor = Blue
            )
        )
    }
}

@Composable
private fun RegistrationScreen(
    schoolClass: String,
    series: String,
    onBack: () -> Unit,
    onLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val emailLooksValid =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    val formValid =
        fullName.trim().isNotEmpty() &&
        emailLooksValid &&
        password.length >= 8 &&
        confirmPassword == password

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(19.dp)
        ) {
            BrandLogo()

            PageHeading(
                title = "Crée ton compte",
                subtitle = "Quelques informations suffisent pour préparer ton espace d'apprentissage."
            )

            if (schoolClass.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Blue.copy(alpha = 0.08f))
                        .padding(13.dp)
                ) {
                    Text(
                        text = if (schoolClass == "Terminale") {
                            "Ta classe : Terminale • Série $series"
                        } else {
                            "Ta classe : 3e"
                        },
                        color = Blue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                FormField(
                    label = "Nom complet",
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        message = ""
                    },
                    placeholder = "Ex. : Kouassi Jean"
                )

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

                FormField(
                    label = "Mot de passe",
                    value = password,
                    onValueChange = {
                        password = it
                        message = ""
                    },
                    placeholder = "Au moins 8 caractères",
                    keyboardType = KeyboardType.Password,
                    password = true
                )

                FormField(
                    label = "Confirme ton mot de passe",
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        message = ""
                    },
                    placeholder = "Retape ton mot de passe",
                    keyboardType = KeyboardType.Password,
                    password = true
                )
            }

            if (password.isNotEmpty() && password.length < 8) {
                Text(
                    text = "Le mot de passe doit contenir au moins 8 caractères.",
                    color = Color(0xFFB54747),
                    fontSize = 12.sp
                )
            }

            if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                Text(
                    text = "Les deux mots de passe ne correspondent pas.",
                    color = Color(0xFFB54747),
                    fontSize = 12.sp
                )
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = Blue,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            PrimaryButton(
                text = "Créer mon compte",
                enabled = formValid,
                onClick = {
                    message =
                        "Le formulaire est valide. La création de compte sera activée après la configuration de l'authentification."
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tu as déjà un compte ? ",
                    color = MutedText,
                    fontSize = 13.sp
                )
                Text(
                    text = "Se connecter",
                    modifier = Modifier
                        .clickable { onLogin() }
                        .padding(vertical = 8.dp),
                    color = Blue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Retour au choix de la classe",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onBack() }
                    .padding(8.dp),
                color = MutedText,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun LoginScreen(
    onBack: () -> Unit,
    onRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showRecoveryInfo by remember { mutableStateOf(false) }

    val emailLooksValid =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            BrandLogo()

            PageHeading(
                title = "Content de te revoir !",
                subtitle = "Connecte-toi pour retrouver ton espace d'apprentissage."
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
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

                    FormField(
                        label = "Mot de passe",
                        value = password,
                        onValueChange = {
                            password = it
                            message = ""
                        },
                        placeholder = "Ton mot de passe",
                        keyboardType = KeyboardType.Password,
                        password = true
                    )

                    Text(
                        text = "Mot de passe oublié ?",
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                showRecoveryInfo = true
                                message = "La récupération du mot de passe sera activée avec le système d'authentification."
                            }
                            .padding(vertical = 5.dp),
                        color = Blue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (showRecoveryInfo) MutedText else Blue,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            PrimaryButton(
                text = "Se connecter",
                enabled = emailLooksValid && password.isNotEmpty(),
                onClick = {
                    showRecoveryInfo = false
                    message =
                        "La connexion sera activée après la configuration de l'authentification."
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pas encore de compte ? ",
                    color = MutedText,
                    fontSize = 13.sp
                )
                Text(
                    text = "S'inscrire",
                    modifier = Modifier
                        .clickable { onRegister() }
                        .padding(vertical = 8.dp),
                    color = Blue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Retour à l'accueil",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onBack() }
                    .padding(8.dp),
                color = MutedText,
                fontSize = 13.sp
            )
        }
    }
}
