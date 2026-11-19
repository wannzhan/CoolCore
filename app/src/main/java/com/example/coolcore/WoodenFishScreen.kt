package com.example.coolcore

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class FloatingBadge(
    val id: Long,
    val text: String,
    val xOffsetDp: Float,
    val color: Color
)

@Composable
fun WoodenFishScreen(
    haptics: HapticEngine,
    soundEngine: SoundEngine,
    settings: SettingsStorage,
    isDark: Boolean
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("coolcore_muyu_v12", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var totalCount by remember { mutableIntStateOf(prefs.getInt("total_knocks", 0)) }
    var sessionCount by remember { mutableIntStateOf(0) }
    var selectedTone by remember { mutableStateOf(FishTone.CRISP_TEAK) }
    var selectedPhrase by remember { mutableStateOf("功德 +1") }
    var autoMode by remember { mutableStateOf(false) }
    var autoBpm by remember { mutableIntStateOf(72) }

    // Squash & stretch spring animation
    val fishSquashX = remember { Animatable(1f) }
    val fishSquashY = remember { Animatable(1f) }
    val malletAngle = remember { Animatable(-32f) }
    val rippleProgress = remember { Animatable(1f) }
    var activeDrumPetal by remember { mutableIntStateOf(-1) }
    val floatingItems = remember { mutableStateListOf<FloatingBadge>() }

    // Palette adaptation
    val cardBg = if (isDark) Color(0xFF1A1F2C) else Color.White
    val textMain = if (isDark) Color(0xFFF0F4F8) else Color(0xFF2D3748)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF718096)
    val macaronPill = if (isDark) Color(0xFF232A3B) else Color(0xFFF1F3F5)

    val macaronColors = listOf(
        Color(0xFF8CD4BE), // Mint
        Color(0xFF90C2EB), // Sea Salt Blue
        Color(0xFFC0AEE4), // Taro Violet
        Color(0xFFF7ADA2), // Peach
        Color(0xFFF7DB8A)  // Butter
    )

    fun triggerKnock(petalIdx: Int = -1) {
        sessionCount++
        totalCount++
        prefs.edit().putInt("total_knocks", totalCount).apply()

        if (settings.soundEnabled) {
            if (settings.instrumentMode == "STEEL_DRUM") {
                soundEngine.playDrum(petalIdx)
            } else {
                soundEngine.playFish()
            }
        }
        if (settings.hapticEnabled) {
            haptics.woodenFishKnock()
        }

        // Spawn floating badge
        val badgeColor = macaronColors[Random.nextInt(macaronColors.size)]
        floatingItems.add(
            FloatingBadge(
                System.nanoTime(),
                selectedPhrase,
                Random.nextInt(-65, 66).toFloat(),
                badgeColor
            )
        )
        if (floatingItems.size > 12) floatingItems.removeAt(0)

        // Cute Jelly bounce
        scope.launch {
            fishSquashX.snapTo(1.14f)
            fishSquashY.snapTo(0.86f)
            launch { fishSquashX.animateTo(1f, spring(0.40f, 520f)) }
            launch { fishSquashY.animateTo(1f, spring(0.40f, 520f)) }
        }
        scope.launch {
            malletAngle.snapTo(4f)
            malletAngle.animateTo(-32f, tween(190, easing = FastOutSlowInEasing))
        }
        scope.launch {
            rippleProgress.snapTo(0f)
            rippleProgress.animateTo(1f, tween(500, easing = LinearOutSlowInEasing))
        }
    }

    LaunchedEffect(autoMode, autoBpm) {
        if (!autoMode) return@LaunchedEffect
        val ms = 60_000L / autoBpm.coerceIn(40, 140)
        while (isActive) {
            triggerKnock()
            delay(ms)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (settings.instrumentMode == "STEEL_DRUM") "禅意空灵鼓" else "Q版萌系木鱼",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )
                Text(
                    text = if (settings.instrumentMode == "STEEL_DRUM") "432Hz 治愈五音阶 · 空灵疗愈" else "清脆萌感 · 抚平杏仁核焦虑",
                    fontSize = 12.sp,
                    color = textMuted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "本次 $sessionCount",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF68B79F)
                )
                Text(
                    text = "累计 $totalCount 次",
                    fontSize = 11.sp,
                    color = textMuted
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Instrument Switcher Tab
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("WOODEN_FISH" to "🪵 Q版萌木鱼", "STEEL_DRUM" to "🎐 432Hz空灵鼓").forEach { (mode, title) ->
                val active = settings.instrumentMode == mode
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (active) (if (isDark) Color(0xFF2C394F) else Color(0xFFD4E8DC))
                            else Color.Transparent
                        )
                        .clickable {
                            settings.updateInstrument(mode)
                            haptics.magneticDetent()
                        }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        fontSize = 13.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) (if (isDark) Color(0xFF9AD3BC) else Color(0xFF2D634E)) else textMuted
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Tone chips (for wooden fish) or Pentatonic Scale hints (for steel drum)
        if (settings.instrumentMode == "WOODEN_FISH") {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                FishTone.entries.forEach { tone ->
                    val active = tone == selectedTone
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) Color(0xFFF7DB8A).copy(0.35f) else cardBg)
                            .border(
                                1.dp,
                                if (active) Color(0xFFDFC065) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedTone = tone
                                soundEngine.reloadFishTone(tone)
                                if (settings.soundEnabled) soundEngine.playFish()
                                if (settings.hapticEnabled) haptics.magneticDetent()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tone.label,
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) (if (isDark) Color(0xFFF7DB8A) else Color(0xFF8C6D1F)) else textMuted
                        )
                    }
                }
            }
        } else {
            // Steel drum info chip
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "轻点任一鼓瓣唤醒宫商角徵羽五音谐波，长音余韵回荡。",
                    fontSize = 11.sp,
                    color = Color(0xFF7FA9C9)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Floating Phrase Selection
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(listOf("功德 +1", "焦虑 -1", "杂念清空", "心生欢喜", "内耗清零", "心率平稳", "好运降临")) { phrase ->
                val sel = selectedPhrase == phrase
                FilterChip(
                    selected = sel,
                    onClick = {
                        selectedPhrase = phrase
                        if (settings.hapticEnabled) haptics.magneticDetent()
                    },
                    label = { Text(phrase, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF8CD4BE).copy(0.28f),
                        selectedLabelColor = if (isDark) Color(0xFF8CD4BE) else Color(0xFF1B634E),
                        containerColor = cardBg,
                        labelColor = textMuted
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Interactive Instrument Body
        Box(
            Modifier
                .size(280.dp)
                .clickable(remember { MutableInteractionSource() }, null) { triggerKnock() },
            contentAlignment = Alignment.Center
        ) {
            // Ripple animation
            if (rippleProgress.value < 0.98f) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = 80.dp.toPx() + rippleProgress.value * 55.dp.toPx()
                    drawCircle(
                        color = (if (settings.instrumentMode == "STEEL_DRUM") Color(0xFF90C2EB) else Color(0xFFF7DB8A))
                            .copy(alpha = (1f - rippleProgress.value) * 0.45f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            if (settings.instrumentMode == "STEEL_DRUM") {
                // Steel Tongue Drum Graphic
                Canvas(
                    Modifier
                        .size(250.dp)
                        .scale(fishSquashX.value, fishSquashY.value)
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = 110.dp.toPx()

                    // Drop shadow
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color.Black.copy(0.35f), Color.Transparent),
                            Offset(cx, cy + 12.dp.toPx()),
                            r * 1.15f
                        ),
                        r * 1.15f,
                        Offset(cx, cy + 12.dp.toPx())
                    )

                    // Metallic handpan outer rim
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFF5B728D), Color(0xFF334358), Color(0xFF1F2937)),
                            Offset(cx - 20.dp.toPx(), cy - 25.dp.toPx()),
                            r
                        ),
                        r,
                        Offset(cx, cy)
                    )
                    drawCircle(
                        Color(0xFF90C2EB).copy(0.4f),
                        r,
                        Offset(cx, cy),
                        style = Stroke(2.dp.toPx())
                    )

                    // Center Ding
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFF88A2BF), Color(0xFF405368)),
                            Offset(cx, cy),
                            32.dp.toPx()
                        ),
                        32.dp.toPx(),
                        Offset(cx, cy)
                    )

                    // 6 Tongue Petals
                    val petalRadius = 68.dp.toPx()
                    for (i in 0 until 6) {
                        val angle = (i * 60.0 - 90.0) * PI / 180.0
                        val px = cx + (petalRadius * cos(angle)).toFloat()
                        val py = cy + (petalRadius * sin(angle)).toFloat()
                        drawCircle(
                            Brush.radialGradient(
                                listOf(Color(0xFF7B94B2), Color(0xFF37475C)),
                                Offset(px, py),
                                20.dp.toPx()
                            ),
                            18.dp.toPx(),
                            Offset(px, py)
                        )
                        drawCircle(
                            Color(0xFFD4E8F8).copy(0.6f),
                            18.dp.toPx(),
                            Offset(px, py),
                            style = Stroke(1.5.dp.toPx())
                        )
                    }
                }
            } else {
                // 3D Cute Chibi Wooden Fish Graphic
                Canvas(
                    Modifier
                        .size(260.dp)
                        .scale(fishSquashX.value, fishSquashY.value)
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f + 8.dp.toPx()

                    // Soft ambient drop shadow underneath
                    drawOval(
                        Brush.radialGradient(
                            listOf(Color.Black.copy(0.35f), Color.Transparent),
                            Offset(cx, cy + 62.dp.toPx()),
                            95.dp.toPx()
                        ),
                        Offset(cx - 95.dp.toPx(), cy + 42.dp.toPx()),
                        Size(190.dp.toPx(), 44.dp.toPx())
                    )

                    // Cute Chibi Round Wooden Body
                    val bodyW = 185.dp.toPx()
                    val bodyH = 145.dp.toPx()
                    val bodyL = cx - bodyW / 2f
                    val bodyT = cy - bodyH / 2f

                    // 3D Warm Caramel wood gradient with top-left specular highlight
                    drawOval(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFDFB0), Color(0xFFE5A96E), Color(0xFF9E6534), Color(0xFF5E3615)),
                            Offset(cx - 30.dp.toPx(), cy - 28.dp.toPx()),
                            bodyW * 0.72f
                        ),
                        Offset(bodyL, bodyT),
                        Size(bodyW, bodyH)
                    )

                    // Tail Fin (Little cute rounded tail at the left back)
                    drawOval(
                        Brush.linearGradient(
                            listOf(Color(0xFFE5A96E), Color(0xFF9E6534)),
                            Offset(bodyL - 10.dp.toPx(), cy),
                            Offset(bodyL + 25.dp.toPx(), cy)
                        ),
                        Offset(bodyL - 18.dp.toPx(), cy - 18.dp.toPx()),
                        Size(36.dp.toPx(), 36.dp.toPx())
                    )

                    // Wooden Fish Slit (Cute smiling mouth)
                    val mouthY = cy + 18.dp.toPx()
                    drawArc(
                        color = Color(0xFF381F0A),
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(cx - 48.dp.toPx(), mouthY - 14.dp.toPx()),
                        size = Size(96.dp.toPx(), 28.dp.toPx()),
                        style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Golden Merit Pearl resting inside mouth
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFF7D6), Color(0xFFF7DB8A), Color(0xFFC49826)),
                            Offset(cx - 2.dp.toPx(), mouthY + 2.dp.toPx()),
                            10.dp.toPx()
                        ),
                        9.dp.toPx(),
                        Offset(cx, mouthY + 3.dp.toPx())
                    )

                    // Adorable Eyes: Curved Happy Arcs ( ᵔ ᵕ ᵔ )
                    val eyeY = cy - 14.dp.toPx()
                    // Left Eye
                    drawArc(
                        color = Color(0xFF3D210A),
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(cx - 38.dp.toPx(), eyeY - 8.dp.toPx()),
                        size = Size(18.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Right Eye
                    drawArc(
                        color = Color(0xFF3D210A),
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(cx + 20.dp.toPx(), eyeY - 8.dp.toPx()),
                        size = Size(18.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Rosy Blushing Cheeks (粉嫩萌系腮红)
                    drawCircle(
                        Color(0xFFFF9AA2).copy(0.48f),
                        11.dp.toPx(),
                        Offset(cx - 48.dp.toPx(), cy - 2.dp.toPx())
                    )
                    drawCircle(
                        Color(0xFFFF9AA2).copy(0.48f),
                        11.dp.toPx(),
                        Offset(cx + 48.dp.toPx(), cy - 2.dp.toPx())
                    )

                    // Little Wooden Mallet (Striking Hammer) at top-right
                    val hammerPivotX = cx + 85.dp.toPx()
                    val hammerPivotY = cy - 75.dp.toPx()
                    rotate(malletAngle.value, Offset(hammerPivotX, hammerPivotY)) {
                        // Mallet rod
                        drawLine(
                            color = Color(0xFFB57C48),
                            start = Offset(hammerPivotX, hammerPivotY),
                            end = Offset(cx + 25.dp.toPx(), cy - 35.dp.toPx()),
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        // Round Wooden Hammer Head
                        drawCircle(
                            Brush.radialGradient(
                                listOf(Color(0xFFFFE0BA), Color(0xFFBA7A41), Color(0xFF704216)),
                                Offset(cx + 22.dp.toPx(), cy - 38.dp.toPx()),
                                16.dp.toPx()
                            ),
                            16.dp.toPx(),
                            Offset(cx + 25.dp.toPx(), cy - 35.dp.toPx())
                        )
                    }
                }
            }

            // Floating Badges Overlay
            floatingItems.forEach { badge ->
                key(badge.id) {
                    FloatingBadgeItem(badge = badge, onFinished = { floatingItems.remove(badge) })
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Auto Knock Section
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("自动静心敲击 (BPM)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textMain)
                        Text("闭目养神 · 自动节律伴随", fontSize = 11.sp, color = textMuted)
                    }
                    Switch(
                        checked = autoMode,
                        onCheckedChange = {
                            autoMode = it
                            if (settings.hapticEnabled) haptics.magneticDetent()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8CD4BE)
                        )
                    )
                }

                if (autoMode) {
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("节律速度: $autoBpm 次/分钟", fontSize = 12.sp, color = Color(0xFF68B79F))
                        Text(if (autoBpm < 60) "深沉助眠" else if (autoBpm < 90) "平稳舒心" else "活力提神", fontSize = 11.sp, color = textMuted)
                    }
                    Slider(
                        value = autoBpm.toFloat(),
                        onValueChange = { autoBpm = it.toInt() },
                        valueRange = 40f..120f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF8CD4BE),
                            activeTrackColor = Color(0xFF8CD4BE)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingBadgeItem(badge: FloatingBadge, onFinished: () -> Unit) {
    val animY = remember { Animatable(0f) }
    val animAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch { animY.animateTo(-120f, tween(750, easing = LinearOutSlowInEasing)) }
        launch {
            delay(350)
            animAlpha.animateTo(0f, tween(400))
            onFinished()
        }
    }

    Box(
        Modifier
            .offset(x = badge.xOffsetDp.dp, y = animY.value.dp)
            .alpha(animAlpha.value)
            .clip(RoundedCornerShape(12.dp))
            .background(badge.color.copy(0.92f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = badge.text,
            color = Color(0xFF1E293B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
