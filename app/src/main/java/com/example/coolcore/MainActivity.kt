package com.example.coolcore

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val DeepAbyss = Color(0xFF070B12)
private val DarkBar = Color(0xFF0C121E)
private val CardSurface = Color(0xFF111827)
private val IceCyan = Color(0xFF4FD1C5)
private val CalmBlue = Color(0xFF63B3ED)
private val SoftViolet = Color(0xFF9F7AEA)
private val AlertCoral = Color(0xFFFC8181)
private val TextPrimary = Color(0xFFEBF4FF)
private val TextSecondary = Color(0xFF8A9AB5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent { CoolCoreApp() }
    }
}

@Composable
fun CoolCoreApp() {
    val context = LocalContext.current
    val haptics = remember { HapticEngine(context) }
    val storage = remember { MoodStorage(context) }
    var currentTab by remember { mutableIntStateOf(0) }

    val scheme = darkColorScheme(
        primary = IceCyan, secondary = CalmBlue, background = DeepAbyss,
        surface = CardSurface, onBackground = TextPrimary, onSurface = TextPrimary
    )

    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = DeepAbyss,
            bottomBar = {
                NavigationBar(containerColor = DarkBar, tonalElevation = 0.dp) {
                    val tabs = listOf("🧊" to "呼吸冷控", "🪘" to "敲木鱼", "👁️" to "着陆双侧", "⚡" to "触觉解压", "🌡️" to "温度记录")
                    tabs.forEachIndexed { i, (emoji, title) ->
                        NavigationBarItem(
                            selected = currentTab == i,
                            onClick = { haptics.magneticDetent(); currentTab = i },
                            icon = { Text(emoji, fontSize = 19.sp) },
                            label = { Text(title, fontSize = 10.sp, color = if (currentTab == i) IceCyan else TextSecondary) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = IceCyan.copy(0.16f))
                        )
                    }
                }
            }
        ) { pad ->
            Box(Modifier.fillMaxSize().background(DeepAbyss).padding(pad)) {
                when (currentTab) {
                    0 -> BreathingCoolingScreen(haptics)
                    1 -> WoodenFishScreen(haptics)
                    2 -> GroundingAndEmdrScreen(haptics)
                    3 -> HapticFidgetLabScreen(haptics)
                    4 -> AmygdalaLogScreen(haptics, storage)
                }
            }
        }
    }
}

enum class BreathMode(val label: String, val desc: String) {
    SIGH("生理性叹气", "双吸一长呼 · 神经科学最快降压法"),
    BOX("箱式呼吸", "4-4-4-4 均衡节律 · 迅速恢复冷静"),
    RELAX_478("4-7-8 镇静", "延长呼气激活迷走神经 · 缓解心悸")
}

@Composable
fun BreathingCoolingScreen(haptics: HapticEngine) {
    var selectedMode by remember { mutableStateOf(BreathMode.SIGH) }
    var isRunning by remember { mutableStateOf(false) }
    var phaseText by remember { mutableStateOf("点击下方开始冷却杏仁核") }
    var cycleCount by remember { mutableIntStateOf(0) }
    val orbScale = remember { Animatable(0.45f) }

    LaunchedEffect(isRunning, selectedMode) {
        if (!isRunning) {
            orbScale.animateTo(0.45f, tween(500))
            phaseText = "准备就绪 · 握紧手机感受震动节拍"
            return@LaunchedEffect
        }
        cycleCount = 0
        while (isActive) {
            when (selectedMode) {
                BreathMode.SIGH -> {
                    phaseText = "鼻吸气（第一口）"; haptics.breathCue(false); orbScale.animateTo(0.80f, tween(2500, easing = FastOutSlowInEasing))
                    phaseText = "再短吸一口（顶满肺泡）"; haptics.magneticDetent(); orbScale.animateTo(1.00f, tween(1100, easing = FastOutSlowInEasing))
                    phaseText = "嘴巴缓缓长呼气……"; haptics.breathCue(true); orbScale.animateTo(0.42f, tween(6000, easing = LinearOutSlowInEasing))
                }
                BreathMode.BOX -> {
                    phaseText = "缓慢吸气 (4秒)"; haptics.breathCue(false); orbScale.animateTo(1.0f, tween(4000, easing = LinearEasing))
                    phaseText = "轻柔屏息 (4秒)"; haptics.magneticDetent(); delay(4000)
                    phaseText = "均匀呼气 (4秒)"; haptics.breathCue(true); orbScale.animateTo(0.42f, tween(4000, easing = LinearEasing))
                    phaseText = "放空屏息 (4秒)"; haptics.magneticDetent(); delay(4000)
                }
                BreathMode.RELAX_478 -> {
                    phaseText = "轻柔吸气 (4秒)"; haptics.breathCue(false); orbScale.animateTo(1.0f, tween(4000, easing = FastOutSlowInEasing))
                    phaseText = "安静屏息 (7秒)"; haptics.magneticDetent(); delay(7000)
                    phaseText = "彻底呼出 (8秒)"; haptics.breathCue(true); orbScale.animateTo(0.40f, tween(8000, easing = LinearOutSlowInEasing))
                }
            }
            cycleCount++
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("冷核 · 生理刹车", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("通过副交感神经直接给过热的杏仁核降温", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            BreathMode.entries.forEach { mode ->
                val active = mode == selectedMode
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (active) IceCyan.copy(0.22f) else CardSurface)
                        .border(1.dp, if (active) IceCyan else Color.Transparent, RoundedCornerShape(14.dp))
                        .clickable { haptics.magneticDetent(); selectedMode = mode }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(mode.label, fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) IceCyan else TextSecondary)
                }
            }
        }
        Text(selectedMode.desc, fontSize = 12.sp, color = CalmBlue, modifier = Modifier.padding(top = 12.dp))

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(280.dp)) {
                val r = (size.minDimension / 2f) * orbScale.value
                drawCircle(Brush.radialGradient(listOf(IceCyan.copy(0.35f), CalmBlue.copy(0.12f), Color.Transparent), center, r * 1.35f), r * 1.35f)
                drawCircle(Brush.radialGradient(listOf(Color(0xFFB2F5EA), IceCyan, Color(0xFF2B6CB0)), center, r), r)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(phaseText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                if (cycleCount > 0) Text("已完成 $cycleCount 轮冷却呼吸", fontSize = 12.sp, color = Color.White.copy(0.8f), modifier = Modifier.padding(top = 6.dp))
            }
        }

        Button(
            onClick = { haptics.bubblePop(); isRunning = !isRunning },
            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) AlertCoral.copy(0.85f) else IceCyan, contentColor = DeepAbyss)
        ) { Text(if (isRunning) "暂停冷却" else "一键启动冷控呼吸", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun GroundingAndEmdrScreen(haptics: HapticEngine) {
    var subTab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("切断情绪反刍", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("左右眼球追踪或感官锚定，强制退出焦虑死循环", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
            listOf("👁️ 双侧刺激 (EMDR)", "🌿 5-4-3-2-1 着陆").forEachIndexed { idx, title ->
                val sel = subTab == idx
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .background(if (sel) CalmBlue.copy(0.22f) else CardSurface)
                        .border(1.dp, if (sel) CalmBlue else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { haptics.magneticDetent(); subTab = idx }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) { Text(title, fontSize = 13.sp, color = if (sel) CalmBlue else TextSecondary) }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (subTab == 0) EmdrBilateralPanel(haptics) else Grounding54321Panel(haptics)
    }
}

@Composable
fun EmdrBilateralPanel(haptics: HapticEngine) {
    var running by remember { mutableStateOf(false) }
    var speedMs by remember { mutableFloatStateOf(1400f) }
    val posX = remember { Animatable(0.5f) }

    LaunchedEffect(running, speedMs) {
        if (!running) { posX.animateTo(0.5f, tween(300)); return@LaunchedEffect }
        while (isActive) {
            posX.animateTo(0.92f, tween(speedMs.toInt(), easing = FastOutSlowInEasing)); haptics.bilateralPulse()
            posX.animateTo(0.08f, tween(speedMs.toInt(), easing = FastOutSlowInEasing)); haptics.bilateralPulse()
        }
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("头部保持不动，仅用双眼跟随光球左右平滑移动。\n每次触边震动会协助双侧大脑半球同步降温。", fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)).background(CardSurface)
                .border(1.dp, CalmBlue.copy(0.25f), RoundedCornerShape(24.dp))
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val y = size.height / 2f; val x = size.width * posX.value
                drawLine(CalmBlue.copy(0.15f), Offset(size.width * 0.08f, y), Offset(size.width * 0.92f, y), 4f)
                drawCircle(Brush.radialGradient(listOf(IceCyan.copy(0.6f), Color.Transparent), Offset(x, y), 85f), 85f, Offset(x, y))
                drawCircle(IceCyan, 30f, Offset(x, y))
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("摆动节律", color = TextSecondary, fontSize = 13.sp)
            Text(when { speedMs < 1000f -> "偏快（深度打断反刍）"; speedMs < 1700f -> "舒缓（标准安抚）"; else -> "极慢（助眠放松）" }, color = IceCyan, fontSize = 13.sp)
        }
        Slider(speedMs, { speedMs = it }, valueRange = 700f..2400f, colors = SliderDefaults.colors(thumbColor = IceCyan, activeTrackColor = IceCyan))
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { haptics.bubblePop(); running = !running },
            modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (running) AlertCoral else CalmBlue, contentColor = DeepAbyss)
        ) { Text(if (running) "停止追踪" else "开始双侧视觉安抚", fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun Grounding54321Panel(haptics: HapticEngine) {
    val steps = remember {
        listOf(
            Triple(5, "👀 看见 5 样东西", "环顾四周，说出5个物体的颜色或形状"),
            Triple(4, "✋ 触摸 4 种质感", "感受衣服面料、桌面温度、手机边框、脚底踩地"),
            Triple(3, "👂 听见 3 种声音", "分辨远处车声、风声、空调声或自己的呼吸"),
            Triple(2, "👃 闻到 2 种气味", "留意空气中的气味，或闻一闻衣领与手背"),
            Triple(1, "👅 品尝 1 种味道", "喝一口温水，或感受口腔当下的味道")
        )
    }
    val counts = remember { mutableStateListOf(0, 0, 0, 0, 0) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        steps.forEachIndexed { idx, (target, title, hint) ->
            val cur = counts[idx]; val done = cur >= target
            Card(colors = CardDefaults.cardColors(containerColor = if (done) IceCyan.copy(0.14f) else CardSurface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text("$cur / $target", color = if (done) IceCyan else TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Text(hint, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(target) { d ->
                            val chk = d < cur
                            Box(
                                Modifier.size(34.dp).clip(CircleShape).background(if (chk) IceCyan else DeepAbyss)
                                    .border(1.5.dp, IceCyan.copy(0.6f), CircleShape)
                                    .clickable { haptics.bubblePop(); counts[idx] = if (chk) d else d + 1 },
                                contentAlignment = Alignment.Center
                            ) { Text(if (chk) "✓" else "${d + 1}", color = if (chk) DeepAbyss else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        }
                    }
                }
            }
        }
        if (counts.sum() == 15) {
            Text("❄️ 你的感官已完全回到安全当下，杏仁核警报解除。", color = IceCyan, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            OutlinedButton(onClick = { haptics.magneticDetent(); for (i in 0..4) counts[i] = 0 }, modifier = Modifier.fillMaxWidth()) { Text("重置清单", color = TextSecondary) }
        }
    }
}

@Composable
fun HapticFidgetLabScreen(haptics: HapticEngine) {
    var toolIdx by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("触觉解压舱", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("调用手机线性马达，把焦躁能量转化为物理阻尼释放", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            listOf("🫧 捏泡泡", "🧲 磁吸推牌", "❄️ 烦恼粉碎").forEachIndexed { idx, label ->
                val sel = toolIdx == idx
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .background(if (sel) SoftViolet.copy(0.25f) else CardSurface)
                        .border(1.dp, if (sel) SoftViolet else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { haptics.magneticDetent(); toolIdx = idx }.padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) { Text(label, fontSize = 13.sp, color = if (sel) Color.White else TextSecondary) }
            }
        }
        Spacer(Modifier.height(16.dp))
        when (toolIdx) {
            0 -> BubblePopWidget(haptics)
            1 -> MagneticDetentWidget(haptics)
            2 -> WorryShatterWidget(haptics)
        }
    }
}

@Composable
fun BubblePopWidget(haptics: HapticEngine) {
    val popped = remember { mutableStateListOf<Boolean>().apply { repeat(30) { add(false) } } }
    var totalPopped by remember { mutableIntStateOf(0) }
    LaunchedEffect(popped.count { it }) {
        if (popped.all { it }) { haptics.shatterRelease(); delay(550); for (i in popped.indices) popped[i] = false }
    }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("已捏破 $totalPopped 颗压力泡泡（全破后自动刷新）", color = IceCyan, fontSize = 13.sp, modifier = Modifier.padding(bottom = 14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            for (r in 0 until 6) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until 5) {
                        val idx = r * 5 + c; val isPop = popped[idx]
                        val sc by animateFloatAsState(if (isPop) 0.72f else 1f, spring(0.45f, 500f), label = "bs")
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).scale(sc).clip(CircleShape)
                                .then(if (isPop) Modifier.background(CardSurface) else Modifier.background(Brush.radialGradient(listOf(Color(0xFFB2F5EA), IceCyan.copy(0.75f), Color(0xFF234E52)))))
                                .border(1.5.dp, if (isPop) TextSecondary.copy(0.25f) else Color.White.copy(0.4f), CircleShape)
                                .clickable { if (!isPop) { popped[idx] = true; totalPopped++; haptics.bubblePop() } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MagneticDetentWidget(haptics: HapticEngine) {
    val notches = 12
    var fraction by remember { mutableFloatStateOf(0.85f) }
    var lastNotch by remember { mutableIntStateOf(10) }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current
    val barColor by animateColorAsState(
        Color((0.31f + fraction * 0.65f).coerceIn(0f, 1f), (0.82f - fraction * 0.35f).coerceIn(0f, 1f), (0.77f - fraction * 0.30f).coerceIn(0f, 1f)),
        label = "tc"
    )

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("向左推动磁吸滑块 · 给大脑降温", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text("当前档位：${lastNotch + 1} / ${notches + 1}", fontSize = 13.sp, color = barColor, modifier = Modifier.padding(top = 6.dp, bottom = 36.dp))
        Box(
            Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(46.dp)).background(CardSurface)
                .border(2.dp, barColor.copy(0.5f), RoundedCornerShape(46.dp))
                .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, drag ->
                        change.consume()
                        fraction = (fraction + drag / trackWidthPx).coerceIn(0f, 1f)
                        val n = (fraction * notches).roundToInt()
                        if (n != lastNotch) { lastNotch = n; haptics.magneticDetent() }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 36.dp), Arrangement.SpaceBetween) {
                repeat(notches + 1) { i ->
                    Box(Modifier.size(if (i == lastNotch) 10.dp else 5.dp).clip(CircleShape).background(if (i == lastNotch) barColor else TextSecondary.copy(0.3f)))
                }
            }
            val maxOffsetDp = with(density) { (trackWidthPx - 92.dp.toPx()).coerceAtLeast(0f).toDp() }
            Box(
                Modifier.padding(horizontal = 10.dp).offset(x = maxOffsetDp * fraction).size(72.dp).clip(CircleShape)
                    .background(barColor).border(3.dp, Color.White.copy(0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(if (fraction < 0.35f) "🧊" else "🔥", fontSize = 26.sp) }
        }
        Text("模拟实体 EDC 磁吸棘轮阻尼，来回滑动可有效释放手指与神经紧绷感。", fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 36.dp, start = 16.dp, end = 16.dp))
    }
}

private data class IceParticle(var x: Float, var y: Float, val vx: Float, val vy: Float, val radius: Float)

@Composable
fun WorryShatterWidget(haptics: HapticEngine) {
    var selectedEmotion by remember { mutableStateOf("焦虑") }
    var worryText by remember { mutableStateOf("") }
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var msg by remember { mutableStateOf<String?>(null) }
    val particles = remember { mutableStateListOf<IceParticle>() }
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            msg = null
            while (isHolding && holdProgress < 1f) {
                delay(40); holdProgress = (holdProgress + 0.025f).coerceAtMost(1f); haptics.holdTick(holdProgress)
            }
            if (holdProgress >= 1f) {
                haptics.shatterRelease(); worryText = ""; holdProgress = 0f; isHolding = false
                msg = "「$selectedEmotion」已被命名、冻结并消散。"
                particles.clear()
                repeat(42) {
                    val a = Random.nextFloat() * 6.28f; val s = 4f + Random.nextFloat() * 14f
                    particles.add(IceParticle(0f, 0f, cos(a) * s, sin(a) * s, 4f + Random.nextFloat() * 8f))
                }
            }
        } else holdProgress = 0f
    }

    LaunchedEffect(particles.size) {
        if (particles.isNotEmpty()) {
            repeat(45) { delay(16); particles.forEach { it.x += it.vx; it.y += it.vy }; tick++ }
            particles.clear()
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("第一步：给情绪命名（科学证实可激活前额叶、抑制杏仁核）", fontSize = 12.sp, color = IceCyan)
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("焦虑", "愤怒", "恐慌", "委屈", "过度思考", "信息过载")) { tag ->
                FilterChip(selectedEmotion == tag, { haptics.magneticDetent(); selectedEmotion = tag }, { Text(tag) })
            }
        }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            worryText, { worryText = it }, label = { Text("写下此刻占据大脑的烦恼（选填，不会上传）") },
            modifier = Modifier.fillMaxWidth().height(110.dp), shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IceCyan, unfocusedBorderColor = TextSecondary.copy(0.4f))
        )
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            if (particles.isNotEmpty()) {
                Canvas(Modifier.fillMaxSize()) {
                    val u = tick
                    particles.forEach { p ->
                        drawCircle(IceCyan.copy((1f - (u % 45) / 45f).coerceIn(0.1f, 0.9f)), p.radius, Offset(center.x + p.x, center.y + p.y))
                    }
                }
            }
            Box(
                Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(20.dp)).background(CardSurface)
                    .border(2.dp, IceCyan, RoundedCornerShape(20.dp))
                    .pointerInput(Unit) { detectTapGestures(onPress = { isHolding = true; tryAwaitRelease(); isHolding = false }) },
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(holdProgress).align(Alignment.CenterStart).background(IceCyan.copy(0.45f)))
                Text(if (isHolding) "正在急速冷冻粉碎…… ${(holdProgress * 100).toInt()}%" else "❄️ 长按此处 2 秒：冻结并粉碎烦恼", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        msg?.let { Text(it, color = IceCyan, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) }
    }
}

@Composable
fun AmygdalaLogScreen(haptics: HapticEngine, storage: MoodStorage) {
    var records by remember { mutableStateOf(storage.loadRecords()) }
    var emotion by remember { mutableStateOf("焦虑") }
    var beforeHeat by remember { mutableFloatStateOf(7f) }
    var afterHeat by remember { mutableFloatStateOf(3f) }
    var note by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("杏仁核温度计", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("纯本地记录冷却前后的情绪燥热度（1=冰点平静，10=过载警报）", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
        Card(colors = CardDefaults.cardColors(containerColor = CardSurface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("焦虑", "愤怒", "紧绷", "恐慌", "低落", "过载")) { tag ->
                        FilterChip(emotion == tag, { haptics.magneticDetent(); emotion = tag }, { Text(tag, fontSize = 12.sp) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("冷却前燥热度：${beforeHeat.roundToInt()} °C", fontSize = 13.sp, color = AlertCoral)
                Slider(beforeHeat, { beforeHeat = it }, valueRange = 1f..10f, steps = 8, colors = SliderDefaults.colors(thumbColor = AlertCoral, activeTrackColor = AlertCoral))
                Text("冷却后燥热度：${afterHeat.roundToInt()} °C", fontSize = 13.sp, color = IceCyan)
                Slider(afterHeat, { afterHeat = it }, valueRange = 1f..10f, steps = 8, colors = SliderDefaults.colors(thumbColor = IceCyan, activeTrackColor = IceCyan))
                OutlinedTextField(note, { note = it }, placeholder = { Text("触发原因简记（选填）", fontSize = 13.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        haptics.bubblePop()
                        storage.saveRecord(CoolRecord(System.currentTimeMillis(), emotion, beforeHeat.roundToInt(), afterHeat.roundToInt(), note.trim()))
                        note = ""; records = storage.loadRecords()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IceCyan, contentColor = DeepAbyss)
                ) { Text("记录本次冷控成果", fontWeight = FontWeight.Bold) }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("历史冷控曲线（共 ${records.size} 条）", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(records) { rec ->
                val drop = rec.heatBefore - rec.heatAfter
                Card(colors = CardDefaults.cardColors(containerColor = CardSurface), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${rec.emotionTag} · ${rec.formattedTime()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            if (rec.note.isNotEmpty()) Text(rec.note, fontSize = 12.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${rec.heatBefore}° → ${rec.heatAfter}°", fontWeight = FontWeight.Bold, color = IceCyan, fontSize = 15.sp)
                            Text(if (drop > 0) "成功降温 ${drop}°" else "保持平稳", fontSize = 11.sp, color = if (drop > 0) IceCyan else TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
