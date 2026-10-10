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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bleu = Color(0xFF2457C5)
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Fond
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo Mon profa
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

                    Text(
                        text = "✦",
                        fontSize = 14.sp,
                        color = Jaune,
                        modifier = Modifier.padding(
                            start = 3.dp,
                            bottom = 20.dp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .width(44.dp)
                        .height(4.dp)
                        .background(
                            Jaune,
                            RoundedCornerShape(50)
                        )
                )
            }

            // Message de bienvenue
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nous allons procéder à la configuration de ton espace.",
                    fontSize = 19.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Medium,
                    color = Texte,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Tu es prêt ?",
                    fontSize = 29.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.6).sp,
                    color = Bleu,
                    textAlign = TextAlign.Center
                )
            }

            // Bouton Commencer
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Bleu,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 0.dp
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
                        .size(38.dp)
                        .background(
                            color = Color(0xFF4778D8),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Flèche dessinée sans bibliothèque supplémentaire
                    Canvas(
                        modifier = Modifier.size(22.dp)
                    ) {
                        val stroke = 2.2.dp.toPx()
                        val startX = size.width * 0.16f
                        val endX = size.width * 0.82f
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
                                size.width * 0.55f,
                                size.height * 0.22f
                            ),
                            end = Offset(endX, centerY),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = Color.White,
                            start = Offset(endX, centerY),
                            end = Offset(
                                size.width * 0.55f,
                                size.height * 0.78f
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
