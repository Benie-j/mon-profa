package ci.monprofa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
                WelcomeScreen()
            }
        }
    }
}

@Composable
private fun WelcomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF9FBFF),
                        Fond,
                        Color(0xFFEDF3FF)
                    )
                )
            )
    ) {
        // Halos décoratifs en arrière-plan
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 65.dp, y = 115.dp)
                .size(190.dp)
                .alpha(0.38f)
                .background(
                    Color(0xFFDCE8FF),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-75).dp, y = (-85).dp)
                .size(210.dp)
                .alpha(0.32f)
                .background(
                    Color(0xFFD8F2EB),
                    CircleShape
                )
        )

        // Petits éléments décoratifs
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 38.dp, top = 185.dp)
                .size(9.dp)
                .background(Jaune, CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 30.dp)
                .offset(y = (-35).dp)
                .size(7.dp)
                .background(
                    Violet.copy(alpha = 0.65f),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 28.dp,
                    vertical = 34.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo revisité
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mon",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.7).sp,
                        color = Texte
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = "profa",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.7).sp,
                        color = Bleu
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = "✦",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Jaune,
                        modifier = Modifier.padding(
                            bottom = 20.dp
                        )
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(25.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Vert)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Jaune)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Box(
                        modifier = Modifier
                            .width(25.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Violet)
                    )
                }
            }

            // Message principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Petite illustration éducative dessinée en Compose
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .background(
                            Color.White.copy(alpha = 0.88f),
                            RoundedCornerShape(34.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.size(72.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        // Livre ouvert
                        val livreGauche = Path().apply {
                            moveTo(w * 0.12f, h * 0.32f)
                            quadraticTo(
                                w * 0.32f, h * 0.23f,
                                w * 0.48f, h * 0.38f
                            )
                            lineTo(w * 0.48f, h * 0.78f)
                            quadraticTo(
                                w * 0.29f, h * 0.63f,
                                w * 0.12f, h * 0.70f
                            )
                            close()
                        }

                        val livreDroite = Path().apply {
                            moveTo(w * 0.52f, h * 0.38f)
                            quadraticTo(
                                w * 0.70f, h * 0.23f,
                                w * 0.88f, h * 0.32f
                            )
                            lineTo(w * 0.88f, h * 0.70f)
                            quadraticTo(
                                w * 0.69f, h * 0.63f,
                                w * 0.52f, h * 0.78f
                            )
                            close()
                        }

                        drawPath(
                            path = livreGauche,
                            color = Bleu.copy(alpha = 0.18f)
                        )
                        drawPath(
                            path = livreDroite,
                            color = Color(0xFFDCE8FF)
                        )

                        drawLine(
                            color = Bleu,
                            start = Offset(w * 0.50f, h * 0.37f),
                            end = Offset(w * 0.50f, h * 0.80f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = Bleu,
                            start = Offset(w * 0.20f, h * 0.43f),
                            end = Offset(w * 0.37f, h * 0.45f),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = Bleu,
                            start = Offset(w * 0.20f, h * 0.53f),
                            end = Offset(w * 0.37f, h * 0.55f),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = Bleu,
                            start = Offset(w * 0.63f, h * 0.45f),
                            end = Offset(w * 0.80f, h * 0.43f),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = Bleu,
                            start = Offset(w * 0.63f, h * 0.55f),
                            end = Offset(w * 0.80f, h * 0.53f),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Petit éclat jaune
                        drawCircle(
                            color = Jaune,
                            radius = w * 0.07f,
                            center = Offset(w * 0.82f, h * 0.15f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(35.dp))

                Text(
                    text = "Nous allons procéder à la configuration de ton espace.",
                    fontSize = 19.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Medium,
                    color = Texte,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = "Tu es prêt ?",
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.7).sp,
                    color = Bleu,
                    textAlign = TextAlign.Center
                )
            }

            // Bouton principal
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Bleu,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 5.dp,
                    pressedElevation = 1.dp
                )
            ) {
                Text(
                    text = "Commencer",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(14.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = BleuClair,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.size(22.dp)
                    ) {
                        val stroke = 2.2.dp.toPx()
                        val startX = size.width * 0.12f
                        val endX = size.width * 0.84f
                        val centerY = size.height * 0.5f

                        drawLine(
                            color = Color.White,
                            start = Offset(startX, centerY),
                            end = Offset(endX, centerY),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = Color.White,
                            start = Offset(
                                size.width * 0.56f,
                                size.height * 0.20f
                            ),
                            end = Offset(endX, centerY),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = Color.White,
                            start = Offset(endX, centerY),
                            end = Offset(
                                size.width * 0.56f,
                                size.height * 0.80f
                            ),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}
