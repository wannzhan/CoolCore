package com.example.coolcore

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import kotlin.random.Random

private val ObsidianBg = Color(0xFF070B12)
private val DarkCard = Color(0xFF111827)
private val ZenCyan = Color(0xFF4FD1C5)
private val WarmWoodLight = Color(0xFFD6A874)
private val WarmWoodDark = Color(0xFF6B4423)
private val TextMain = Color(0xFFEBF4FF)
private val TextMuted = Color(0xFF8A9AB5)

private data class FloatingMerit(val id: Long, val text: String, val xOffsetDp: Float)

@Composable
fun WoodenFishScreen(haptics: HapticEngine) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("coolcore_muyu", Context.MODE_PRIVATE) }
    val audio = remember { WoodenFishAudio() }
    val scope = rememberCoroutineScope()
    DisposableEffect(Unit) { onDispose { audio.release() } }

    var totalCount by remember { mutableIntStateOf(prefs.getInt("total_knocks", 0)) }
    var sessionCount by remember { mutableIntStateOf(0) }
    var selectedTone by remember { mutableStateOf(WoodTone.SANDALWOOD) }
    var selectedPhrase by remember { mutableStateOf("焦虑 -1") }
    var soundEnabled by remember { mutableStateOf(true) }
    var hapticEnabled by remember { mutableStateOf(true) }
    var autoMode by remember { mutableStateOf(false) }
    var autoBpm by remember { mutableIntStateOf(72) }

    val fishScale = remember { Animatable(1f) }
    val malletAngle = remember { Animatable(-28f) }
    val rippleProgress = remember { Animatable(1f) }
    val floatingItems = remember { mutableStateListOf<FloatingMerit>() }

    fun triggerKnock() {
        sessionCount++; totalCount++
        prefs.edit().putInt("total_knocks", totalCount).apply()
        if (soundEnabled) audio.play()
        if (hapticEnabled) haptics.woodenFishKnock()
        floatingItems.add(FloatingMerit(System.nanoTime(), selectedPhrase, Random.nextInt(-55, 56).toFloat()))
        if (floatingItems.size > 14) floatingItems.removeAt(0)
        scope.launch { fishScale.snapTo(0.90f); fishScale.animateTo(1f, spring(0.42f, 620f)) }
        scope.launch { malletAngle.snapTo(6f); malletAngle.animateTo(-28f, tween(210, easing = FastOutSlowInEasing)) }
        scope.launch { rippleProgress.snapTo(0f); rippleProgress.animateTo(1f, tween(520, easing = LinearOutSlowInEasing)) }
    }

    LaunchedEffect(autoMode, autoBpm) {
        if (!autoMode) return@LaunchedEffect
        val ms = 60_000L / autoBpm.coerceIn(40, 140)
        while (isActive) { triggerKnock(); delay(ms) }
    }

    Column(
        Modifier.fillMaxSize().background(ObsidianBg).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column {
                Text("禅意木鱼", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Text("规律敲击 · 抚平杏仁核躁动", fontSize = 12.sp, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("本次 $sessionCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ZenCyan)
                Text("累计 $totalCount 次", fontSize = 11.sp, color = TextMuted)
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            WoodTone.entries.forEach { tone ->
                val active = tone == selectedTone
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .background(if (active) WarmWoodLight.copy(0.22f) else DarkCard)
                        .border(1.dp, if (active) WarmWoodLight else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable {
                            selectedTone = tone; audio.loadTone(tone)
                            if (soundEnabled) audio.play(); if (hapticEnabled) haptics.magneticDetent()
                        }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tone.label, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) WarmWoodLight else TextMuted)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(listOf("焦虑 -1", "冷静 +1", "内耗 -1", "功德 +1", "烦躁 -1", "心率平稳")) { phrase ->
                FilterChip(
                    selected = selectedPhrase == phrase,
                    onClick = { selectedPhrase = phrase; if (hapticEnabled) haptics.magneticDetent() },
                    label = { Text(phrase, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ZenCyan.copy(0.2f), selectedLabelColor = ZenCyan,
                        containerColor = DarkCard, labelColor = TextMuted
                    )
                )
            }
        }

        Box(
            Modifier.weight(1f).fillMaxWidth()
                .clickable(remember { MutableInteractionSource() }, null) { triggerKnock() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(290.dp).scale(fishScale.value)) {
                val cx = size.width / 2f; val cy = size.height / 2f + 16.dp.toPx()
                if (rippleProgress.value < 0.99f) {
                    drawCircle(
                        WarmWoodLight.copy((1f - rippleProgress.value) * 0.45f),
                        95.dp.toPx() + rippleProgress.value * 65.dp.toPx(),
                        Offset(cx, cy), style = Stroke(3.dp.toPx())
                    )
                }
                drawOval(
                    Brush.radialGradient(listOf(Color.Black.copy(0.7f), Color.Transparent), Offset(cx, cy + 72.dp.toPx()), 110.dp.toPx()),
                    Offset(cx - 105.dp.toPx(), cy + 50.dp.toPx()), Size(210.dp.toPx(), 46.dp.toPx())
                )
                val bw = 205.dp.toPx(); val bh = 148.dp.toPx(); val bl = cx - bw / 2f; val bt = cy - bh / 2f
                drawOval(Brush.verticalGradient(listOf(WarmWoodLight, Color(0xFF9C6639), WarmWoodDark), bt, bt + bh), Offset(bl, bt), Size(bw, bh))
                drawOval(WarmWoodLight.copy(0.45f), Offset(bl, bt), Size(bw, bh), style = Stroke(2.dp.toPx()))

                val slit = Path().apply {
                    moveTo(cx - 82.dp.toPx(), cy + 10.dp.toPx())
                    quadraticTo(cx - 10.dp.toPx(), cy + 28.dp.toPx(), cx + 52.dp.toPx(), cy + 4.dp.toPx())
                }
                drawPath(slit, Color(0xFF1E120A), style = Stroke(11.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(Color(0xFF1E120A), 14.dp.toPx(), Offset(cx + 52.dp.toPx(), cy + 2.dp.toPx()))
                val deco = Path().apply {
                    moveTo(cx - 56.dp.toPx(), cy - 32.dp.toPx())
                    quadraticTo(cx, cy - 52.dp.toPx(), cx + 54.dp.toPx(), cy - 28.dp.toPx())
                }
                drawPath(deco, Color(0xFFFFE4C4).copy(0.25f), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))

                val pivot = Offset(cx + 115.dp.toPx(), cy - 95.dp.toPx())
                rotate(malletAngle.value, pivot) {
                    drawLine(Color(0xFFC08A55), pivot, Offset(cx + 14.dp.toPx(), cy - 72.dp.toPx()), 9.dp.toPx(), StrokeCap.Round)
                    drawCircle(
                        Brush.radialGradient(listOf(Color(0xFFF3D5B5), WarmWoodLight, WarmWoodDark), Offset(cx + 8.dp.toPx(), cy - 70.dp.toPx()), 22.dp.toPx()),
                        20.dp.toPx(), Offset(cx + 8.dp.toPx(), cy - 70.dp.toPx())
                    )
                }
            }

            floatingItems.forEach { item ->
                key(item.id) { FloatingTextItem(item) { floatingItems.remove(item) } }
            }

            Text(
                if (autoMode) "自动节律敲击中 ($autoBpm BPM) · 点击可手动叠加" else "轻触木鱼敲击 · 释放紧绷情绪",
                fontSize = 12.sp, color = TextMuted,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
            )
        }

        Card(colors = CardDefaults.cardColors(containerColor = DarkCard), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallToggleChip("🔊 音效", soundEnabled) { soundEnabled = !soundEnabled }
                        SmallToggleChip("📳 震感", hapticEnabled) { hapticEnabled = !hapticEnabled }
                    }
                    Button(
                        onClick = { if (hapticEnabled) haptics.magneticDetent(); autoMode = !autoMode },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (autoMode) ZenCyan else Color(0xFF1F2937),
                            contentColor = if (autoMode) ObsidianBg else TextMain
                        )
                    ) { Text(if (autoMode) "⏸ 停止自动" else "▶ 自动敲击", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                }
                if (autoMode) {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("节拍 $autoBpm BPM", fontSize = 12.sp, color = ZenCyan)
                        Spacer(Modifier.width(10.dp))
                        Slider(autoBpm.toFloat(), { autoBpm = it.toInt() }, Modifier.weight(1f), valueRange = 45f..120f,
                            colors = SliderDefaults.colors(thumbColor = ZenCyan, activeTrackColor = ZenCyan))
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallToggleChip(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(if (active) ZenCyan.copy(0.18f) else ObsidianBg)
            .border(1.dp, if (active) ZenCyan.copy(0.6f) else Color(0xFF283548), RoundedCornerShape(10.dp))
            .clickable { onClick() }.padding(horizontal = 10.dp, vertical = 7.dp)
    ) { Text(text, fontSize = 12.sp, color = if (active) ZenCyan else TextMuted) }
}

@Composable
private fun FloatingTextItem(item: FloatingMerit, onFinished: () -> Unit) {
    val p = remember { Animatable(0f) }
    LaunchedEffect(item.id) { p.animateTo(1f, tween(880, easing = LinearOutSlowInEasing)); onFinished() }
    Text(
        item.text, color = ZenCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.offset(x = item.xOffsetDp.dp, y = (-35f - p.value * 115f).dp).alpha((1f - p.value * 1.1f).coerceIn(0f, 1f))
    )
}
