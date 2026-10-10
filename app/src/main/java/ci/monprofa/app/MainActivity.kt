package ci.monprofa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bleu = Color(0xFF2457C5)
private val BleuClair = Color(0xFF4778D8)
private val Vert = Color(0xFF15966A)
private val Jaune = Color(0xFFFFC857)
private val Violet = Color(0xFF8457D9)
private val Fond = Color(0xFFF5F8FF)
private val Texte = Color(0xFF202B45)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
                var page by remember { mutableStateOf(false) }

                if (page) {
                    ClassSelectionScreen(
                        onBack = { page = false }
                    )
                } else {
                    WelcomeScreen(
                        onStart = { page = true }
                    )
                }
            }
        }
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
private fun BrandLogo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Mon",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.7).sp,
                color = Texte
            )
            Spacer(Modifier.width(7.dp))
            Text(
                "profa",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.7).sp,
                color = Bleu
            )
            Text(
                "✦",
                modifier = Modifier.padding(start = 3.dp, bottom = 20.dp),
                fontSize = 15.sp,
                color = Jaune
            )
        }
        Row(
            modifier = Modifier.padding(top = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .width(25.dp)
                    .height(4.dp)
                    .background(Vert, CircleShape)
            )
            Spacer(Modifier.width(5.dp))
            Box(
                Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .background(Jaune, CircleShape)
            )
            Spacer(Modifier.width(5.dp))
            Box(
                Modifier
                    .width(25.dp)
                    .height(4.dp)
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
private fun WelcomeScreen(onStart: () -> Unit) {
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

            PrimaryButton("Commencer", onStart)
        }
    }
}

@Composable
private fun ClassSelectionScreen(onBack: () -> Unit) {
    var selectedClass by remember { mutableStateOf<String?>(null) }
    var selectedSeries by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AppBackground {
        Column(
            Modifier
                .fillMaxSize()
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
                    selectedClass = "3e"
                    selectedSeries = null
                }
            )

            Spacer(Modifier.height(14.dp))

            ClassOptionCard(
                title = "Terminale",
                subtitle = "Préparation au BAC",
                selected = selectedClass == "Terminale",
                accent = Violet,
                onClick = {
                    selectedClass = "Terminale"
                    selectedSeries = null
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
                                        selectedSeries = serie
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                text = "Continuer",
                onClick = { },
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
