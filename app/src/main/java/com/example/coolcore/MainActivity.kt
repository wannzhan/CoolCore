package com.example.coolcore

import android.content.res.Configuration
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// --- Macaron Low-Saturation Pastel Palette ---
val MacaronMint = Color(0xFF8CD4BE)
val MacaronBlue = Color(0xFF90C2EB)
val MacaronViolet = Color(0xFFC0AEE4)
val MacaronPeach = Color(0xFFF7ADA2)
val MacaronYellow = Color(0xFFF7DB8A)

// Neutral Foundation Colors
val DarkCanvas = Color(0xFF10141D)
val DarkSurface = Color(0xFF19202E)
val DarkTextPrimary = Color(0xFFF0F4F8)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkBorder = Color(0xFF242F44)

val LightCanvas = Color(0xFFF7F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightTextPrimary = Color(0xFF2D3748)
val LightTextSecondary = Color(0xFF718096)
val LightBorder = Color(0xFFE2E8F0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        setContent { CoolCoreApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoolCoreApp() {
    val context = LocalContext.current
    val haptics = remember { HapticEngine(context) }
    val soundEngine = remember { SoundEngine() }
    val settings = remember { SettingsStorage(context) }
    val storage = remember { MoodStorage(context) }

    DisposableEffect(Unit) {
        onDispose { soundEngine.release() }
    }

    LaunchedEffect(settings.soundVolume) {
        soundEngine.setVolume(settings.soundVolume)
    }
    LaunchedEffect(settings.hapticEnabled) {
        haptics.enabled = settings.hapticEnabled
    }

    // Dynamic Theme Calculation
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        AppThemeMode.FOLLOW_SYSTEM -> isSystemDark
        AppThemeMode.MACARON_LIGHT -> false
        AppThemeMode.OBSIDIAN_DARK -> true
    }

    val canvasColor = if (isDark) DarkCanvas else LightCanvas
    val surfaceColor = if (isDark) DarkSurface else LightSurface
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val borderColor = if (isDark) DarkBorder else LightBorder

    val scheme = if (isDark) {
        darkColorScheme(
            primary = MacaronMint,
            secondary = MacaronBlue,
            background = DarkCanvas,
            surface = DarkSurface,
            onBackground = DarkTextPrimary,
            onSurface = DarkTextPrimary
        )
    } else {
        lightColorScheme(
            primary = MacaronMint,
            secondary = MacaronBlue,
            background = LightCanvas,
            surface = LightSurface,
            onBackground = LightTextPrimary,
            onSurface = LightTextPrimary
        )
    }

    var currentTab by remember { mutableIntStateOf(0) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    var emdrZenMode by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = canvasColor,
            topBar = {
                if (!(isLandscape && currentTab == 2 && emdrZenMode)) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("冷核 CoolCore", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "v1.2",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MacaronMint,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MacaronMint.copy(0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                if (settings.hapticEnabled) haptics.magneticDetent()
                                showSettingsSheet = true
                            }) {
                                Icon(Icons.Default.Settings, contentDescription = "设置", tint = textSecondary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = canvasColor)
                    )
                }
            },
            bottomBar = {
                if (!(isLandscape && currentTab == 2 && emdrZenMode)) {
                    NavigationBar(
                        containerColor = surfaceColor,
                        tonalElevation = 2.dp,
                        modifier = Modifier.height(if (isLandscape) 56.dp else 72.dp)
                    ) {
                        val tabs = listOf(
                            "🧊" to "呼吸冷控",
                            "🪘" to "萌系木鱼",
                            "👁️" to "双侧刺激",
                            "⚡" to "触觉解压",
                            "🌡️" to "温度记录"
                        )
                        tabs.forEachIndexed { i, (emoji, title) ->
                            NavigationBarItem(
                                selected = currentTab == i,
                                onClick = {
                                    if (settings.hapticEnabled) haptics.magneticDetent()
                                    currentTab = i
                                },
                                icon = { Text(emoji, fontSize = if (isLandscape) 16.sp else 19.sp) },
                                label = {
                                    Text(
                                        title,
                                        fontSize = 10.sp,
                                        color = if (currentTab == i) MacaronMint else textSecondary
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MacaronMint.copy(0.22f)
                                )
                            )
                        }
                    }
                }
            }
        ) { pad ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(canvasColor)
                    .padding(if (isLandscape && currentTab == 2 && emdrZenMode) PaddingValues(0.dp) else pad)
            ) {
                when (currentTab) {
                    0 -> BreathingCoolingScreen(haptics, settings, isDark)
                    1 -> WoodenFishScreen(haptics, soundEngine, settings, isDark)
                    2 -> GroundingAndEmdrScreen(
                        haptics = haptics,
                        settings = settings,
                        isDark = isDark,
                        isLandscape = isLandscape,
                        zenMode = emdrZenMode,
                        onToggleZen = { emdrZenMode = !emdrZenMode }
                    )
                    3 -> HapticFidgetLabScreen(haptics, soundEngine, settings, isDark)
                    4 -> AmygdalaLogScreen(haptics, storage, settings, isDark)
                }
            }
        }

        // Settings Bottom Sheet
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                containerColor = surfaceColor,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                SettingsPanelContent(
                    settings = settings,
                    haptics = haptics,
                    soundEngine = soundEngine,
                    isDark = isDark,
                    onClose = { showSettingsSheet = false }
                )
            }
        }
    }
}

@Composable
fun SettingsPanelContent(
    settings: SettingsStorage,
    haptics: HapticEngine,
    soundEngine: SoundEngine,
    isDark: Boolean,
    onClose: () -> Unit
) {
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val cardBg = if (isDark) Color(0xFF1F2737) else Color(0xFFF1F3F5)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("个性化设置", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            TextButton(onClick = onClose) {
                Text("完成", color = MacaronMint, fontWeight = FontWeight.Bold)
            }
        }

        // Theme Switch
        Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🎨 界面皮肤模式", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textPrimary)
                AppThemeMode.entries.forEach { mode ->
                    val sel = settings.themeMode == mode
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) MacaronMint.copy(0.25f) else Color.Transparent)
                            .clickable {
                                settings.updateTheme(mode)
                                if (settings.hapticEnabled) haptics.magneticDetent()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(mode.label, fontSize = 13.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = textPrimary)
                            Text(mode.subtitle, fontSize = 11.sp, color = textSecondary)
                        }
                        if (sel) Text("✓", color = MacaronMint, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Volume & Sound
        Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("🔊 音效总开关", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textPrimary)
                        Text("木鱼、空灵鼓与捏泡泡的实时声效", fontSize = 11.sp, color = textSecondary)
                    }
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = {
                            settings.toggleSound(it)
                            if (settings.hapticEnabled) haptics.magneticDetent()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MacaronMint)
                    )
                }

                if (settings.soundEnabled) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("音效音量大小", fontSize = 13.sp, color = textPrimary)
                        Text("${(settings.soundVolume * 100).toInt()}%", fontSize = 12.sp, color = MacaronMint, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.soundVolume,
                        onValueChange = {
                            settings.updateVolume(it)
                            soundEngine.setVolume(it)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = MacaronMint, activeTrackColor = MacaronMint)
                    )
                }
            }
        }

        // Haptic Feedback
        Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(16.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Column {
                    Text("📳 线性马达震动触觉", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textPrimary)
                    Text("微触碰与拟真机械阻尼反馈", fontSize = 11.sp, color = textSecondary)
                }
                Switch(
                    checked = settings.hapticEnabled,
                    onCheckedChange = {
                        settings.toggleHaptic(it)
                        haptics.enabled = it
                        if (it) haptics.magneticDetent()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MacaronMint)
                )
            }
        }

        // Privacy & Offline Promise
        Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("🛡️ 单机运行与隐私承诺", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MacaronMint)
                Text("冷核 CoolCore 100% 运行于本地。无任何网络上报、无广告、无账号，您的心情记录仅保存在设备私有安全存储中。",
                    fontSize = 11.sp, color = textSecondary, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

// --- 1. Breathing Cooling Screen ---
enum class BreathMode(val label: String, val desc: String) {
    SIGH("生理性叹气", "双吸一长呼 · 神经科学最快降压法"),
    BOX("箱式呼吸", "4-4-4-4 均衡节律 · 迅速恢复冷静"),
    RELAX_478("4-7-8 镇静", "延长呼气激活迷走神经 · 缓解心悸")
}

@Composable
fun BreathingCoolingScreen(haptics: HapticEngine, settings: SettingsStorage, isDark: Boolean) {
    var selectedMode by remember { mutableStateOf(BreathMode.SIGH) }
    var isRunning by remember { mutableStateOf(false) }
    var phaseText by remember { mutableStateOf("点击下方开始冷却杏仁核") }
    var cycleCount by remember { mutableIntStateOf(0) }
    val orbScale = remember { Animatable(0.45f) }

    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val cardBg = if (isDark) DarkSurface else LightSurface

    LaunchedEffect(isRunning, selectedMode) {
        if (!isRunning) {
            orbScale.animateTo(0.45f, tween(500))
            phaseText = "准备就绪 · 握紧手机感受呼吸节拍"
            return@LaunchedEffect
        }
        cycleCount = 0
        while (isActive) {
            when (selectedMode) {
                BreathMode.SIGH -> {
                    phaseText = "鼻吸气（第一口）"
                    if (settings.hapticEnabled) haptics.breathCue(false)
                    orbScale.animateTo(0.80f, tween(2500, easing = FastOutSlowInEasing))
                    phaseText = "再短吸一口（顶满肺泡）"
                    if (settings.hapticEnabled) haptics.magneticDetent()
                    orbScale.animateTo(1.00f, tween(1100, easing = FastOutSlowInEasing))
                    phaseText = "嘴巴缓缓长呼气……"
                    if (settings.hapticEnabled) haptics.breathCue(true)
                    orbScale.animateTo(0.42f, tween(6000, easing = LinearOutSlowInEasing))
                }
                BreathMode.BOX -> {
                    phaseText = "缓慢吸气 (4秒)"
                    if (settings.hapticEnabled) haptics.breathCue(false)
                    orbScale.animateTo(1.0f, tween(4000, easing = LinearEasing))
                    phaseText = "轻柔屏息 (4秒)"
                    if (settings.hapticEnabled) haptics.magneticDetent()
                    delay(4000)
                    phaseText = "均匀呼气 (4秒)"
                    if (settings.hapticEnabled) haptics.breathCue(true)
                    orbScale.animateTo(0.42f, tween(4000, easing = LinearEasing))
                    phaseText = "放空屏息 (4秒)"
                    if (settings.hapticEnabled) haptics.magneticDetent()
                    delay(4000)
                }
                BreathMode.RELAX_478 -> {
                    phaseText = "轻柔吸气 (4秒)"
                    if (settings.hapticEnabled) haptics.breathCue(false)
                    orbScale.animateTo(1.0f, tween(4000, easing = FastOutSlowInEasing))
                    phaseText = "安静屏息 (7秒)"
                    if (settings.hapticEnabled) haptics.magneticDetent()
                    delay(7000)
                    phaseText = "彻底呼出 (8秒)"
                    if (settings.hapticEnabled) haptics.breathCue(true)
                    orbScale.animateTo(0.40f, tween(8000, easing = LinearOutSlowInEasing))
                }
            }
            cycleCount++
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("冷核 · 生理刹车", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
        Text("通过副交感神经直接给过热的杏仁核降温", fontSize = 12.sp, color = textSecondary, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))

        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            BreathMode.entries.forEach { mode ->
                val active = mode == selectedMode
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) MacaronMint.copy(0.28f) else cardBg)
                        .border(1.dp, if (active) MacaronMint else Color.Transparent, RoundedCornerShape(14.dp))
                        .clickable {
                            if (settings.hapticEnabled) haptics.magneticDetent()
                            selectedMode = mode
                        }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        mode.label,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) (if (isDark) MacaronMint else Color(0xFF1B634E)) else textSecondary
                    )
                }
            }
        }
        Text(selectedMode.desc, fontSize = 11.sp, color = MacaronBlue, modifier = Modifier.padding(top = 8.dp))

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(260.dp)) {
                val r = (size.minDimension / 2f) * orbScale.value
                drawCircle(
                    Brush.radialGradient(
                        listOf(MacaronMint.copy(0.35f), MacaronBlue.copy(0.12f), Color.Transparent),
                        center,
                        r * 1.35f
                    ),
                    r * 1.35f
                )
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFFD4F0E7), MacaronMint, Color(0xFF5AB69A)),
                        center,
                        r
                    ),
                    r
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(phaseText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1A382E), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                if (cycleCount > 0) Text("已完成 $cycleCount 轮冷却呼吸", fontSize = 12.sp, color = if (isDark) Color.White.copy(0.8f) else Color(0xFF2D5C4C), modifier = Modifier.padding(top = 6.dp))
            }
        }

        Button(
            onClick = {
                if (settings.hapticEnabled) haptics.bubblePop()
                isRunning = !isRunning
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) MacaronPeach else MacaronMint,
                contentColor = Color(0xFF1A202C)
            )
        ) {
            Text(if (isRunning) "暂停冷却" else "一键启动冷控呼吸", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- 2. Grounding & EMDR (Landscape Optimized) ---
@Composable
fun GroundingAndEmdrScreen(
    haptics: HapticEngine,
    settings: SettingsStorage,
    isDark: Boolean,
    isLandscape: Boolean,
    zenMode: Boolean,
    onToggleZen: () -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) }
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val cardBg = if (isDark) DarkSurface else LightSurface

    Column(Modifier.fillMaxSize().padding(if (zenMode) 8.dp else 16.dp)) {
        if (!zenMode) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("双侧刺激 · 眼动追踪", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text(if (isLandscape) "横屏模式下视野开阔，双侧刺激效果最佳" else "横转手机可获得超宽平滑眼动视野", fontSize = 12.sp, color = textSecondary)
                }
                if (subTab == 0) {
                    TextButton(onClick = onToggleZen) {
                        Text(if (zenMode) "退出沉浸" else "全屏沉浸", color = MacaronBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                listOf("👁️ 双侧刺激 (EMDR)", "🌿 5-4-3-2-1 着陆").forEachIndexed { idx, title ->
                    val sel = subTab == idx
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) MacaronBlue.copy(0.28f) else cardBg)
                            .border(1.dp, if (sel) MacaronBlue else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable {
                                if (settings.hapticEnabled) haptics.magneticDetent()
                                subTab = idx
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title,
                            fontSize = 12.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            color = if (sel) (if (isDark) MacaronBlue else Color(0xFF1F588A)) else textSecondary
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (subTab == 0) {
            EmdrBilateralPanel(haptics, settings, isDark, isLandscape, zenMode, onToggleZen)
        } else {
            Grounding54321Panel(haptics, settings, isDark)
        }
    }
}

@Composable
fun EmdrBilateralPanel(
    haptics: HapticEngine,
    settings: SettingsStorage,
    isDark: Boolean,
    isLandscape: Boolean,
    zenMode: Boolean,
    onToggleZen: () -> Unit
) {
    var running by remember { mutableStateOf(false) }
    var speedMs by remember { mutableFloatStateOf(1350f) }
    val posX = remember { Animatable(0.5f) }

    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val trackBg = if (isDark) Color(0xFF151C28) else Color(0xFFEBF1F7)

    LaunchedEffect(running, speedMs) {
        if (!running) {
            posX.animateTo(0.5f, tween(300))
            return@LaunchedEffect
        }
        while (isActive) {
            posX.animateTo(0.93f, tween(speedMs.toInt(), easing = FastOutSlowInEasing))
            if (settings.hapticEnabled) haptics.bilateralPulse()
            posX.animateTo(0.07f, tween(speedMs.toInt(), easing = FastOutSlowInEasing))
            if (settings.hapticEnabled) haptics.bilateralPulse()
        }
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!zenMode) {
            Text(
                "头部保持不动，仅用双眼跟随马卡龙光球左右平滑移动。\n两端轻微震动将引导双侧大脑半球同步冷却。",
                fontSize = 12.sp,
                color = textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
        }

        // Full Width Bilateral Track
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(trackBg)
                .clickable { if (zenMode) onToggleZen() }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val y = size.height / 2f
                val x = size.width * posX.value

                // Horizon guideline
                drawLine(
                    color = MacaronBlue.copy(0.25f),
                    start = Offset(size.width * 0.06f, y),
                    end = Offset(size.width * 0.94f, y),
                    strokeWidth = 3.dp.toPx()
                )

                // Outer ambient glow
                drawCircle(
                    Brush.radialGradient(
                        listOf(MacaronBlue.copy(0.55f), Color.Transparent),
                        Offset(x, y),
                        80.dp.toPx()
                    ),
                    80.dp.toPx(),
                    Offset(x, y)
                )

                // Cute Soothing Orb with Macaron Gradient
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color.White, MacaronBlue, Color(0xFF5D9FD6)),
                        Offset(x - 5.dp.toPx(), y - 5.dp.toPx()),
                        24.dp.toPx()
                    ),
                    24.dp.toPx(),
                    Offset(x, y)
                )
            }

            if (zenMode) {
                Text(
                    "沉浸全屏中 · 点击任意位置恢复",
                    fontSize = 11.sp,
                    color = textSecondary.copy(0.6f),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                )
            }
        }

        if (!zenMode) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("摆动节奏", color = textSecondary, fontSize = 12.sp)
                Text(
                    when {
                        speedMs < 950f -> "快速 (打断情绪反刍)"
                        speedMs < 1650f -> "舒缓 (标准安抚)"
                        else -> "静缓 (深度助眠)"
                    },
                    color = MacaronBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Slider(
                value = speedMs,
                onValueChange = { speedMs = it },
                valueRange = 700f..2400f,
                colors = SliderDefaults.colors(thumbColor = MacaronBlue, activeTrackColor = MacaronBlue)
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (settings.hapticEnabled) haptics.bubblePop()
                    running = !running
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (running) MacaronPeach else MacaronBlue,
                    contentColor = Color(0xFF1E293B)
                )
            ) {
                Text(if (running) "停止眼动刺激" else "开始双侧眼动追踪", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun Grounding54321Panel(haptics: HapticEngine, settings: SettingsStorage, isDark: Boolean) {
    val steps = remember {
        listOf(
            Triple(5, "👀 看见 5 样东西", "环顾四周，留意5种不同的颜色或形状"),
            Triple(4, "✋ 触摸 4 种质感", "触摸衣物面料、桌面温度、手机边缘或地面"),
            Triple(3, "👂 听见 3 种声音", "分辨车流、风声、空调底噪或呼吸声"),
            Triple(2, "👃 闻到 2 种气味", "闻一闻衣领、手背或空气中的气味"),
            Triple(1, "👅 品尝 1 种味道", "喝一口温水，感受舌尖当下的温度")
        )
    }
    val counts = remember { mutableStateListOf(0, 0, 0, 0, 0) }
    val cardBg = if (isDark) DarkSurface else LightSurface
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        steps.forEachIndexed { idx, (target, title, hint) ->
            val cur = counts[idx]
            val done = cur >= target
            Card(
                colors = CardDefaults.cardColors(containerColor = if (done) MacaronMint.copy(0.18f) else cardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                        Text("$cur / $target", color = if (done) MacaronMint else textSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(hint, fontSize = 11.sp, color = textSecondary, modifier = Modifier.padding(top = 2.dp))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(target) { d ->
                            val chk = d < cur
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (chk) MacaronMint else (if (isDark) Color(0xFF141924) else Color(0xFFE2E8F0)))
                                    .border(1.dp, MacaronMint.copy(0.7f), CircleShape)
                                    .clickable {
                                        if (settings.hapticEnabled) haptics.bubblePop()
                                        counts[idx] = if (chk) d else d + 1
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (chk) "✓" else "${d + 1}",
                                    color = if (chk) Color(0xFF1E293B) else textSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        if (counts.sum() == 15) {
            Text("❄️ 你的感官已重新扎根于安全当下，警报解除。", color = MacaronMint, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
            OutlinedButton(
                onClick = {
                    if (settings.hapticEnabled) haptics.magneticDetent()
                    for (i in 0..4) counts[i] = 0
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("重置清单", color = textSecondary)
            }
        }
    }
}

// --- 3. Haptic Fidget Lab (Bubble Pop with Burst Particles) ---
private data class PopParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun HapticFidgetLabScreen(
    haptics: HapticEngine,
    soundEngine: SoundEngine,
    settings: SettingsStorage,
    isDark: Boolean
) {
    var toolIdx by remember { mutableIntStateOf(0) }
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val cardBg = if (isDark) DarkSurface else LightSurface

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("触觉解压舱", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
        Text("声、视、触三维协同，把焦虑紧绷化为物理阻尼释放", fontSize = 12.sp, color = textSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            listOf("🫧 捏泡泡 (破裂音)", "🧲 磁吸滑块", "❄️ 烦恼粉碎").forEachIndexed { idx, label ->
                val sel = toolIdx == idx
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (sel) MacaronViolet.copy(0.28f) else cardBg)
                        .border(1.dp, if (sel) MacaronViolet else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable {
                            if (settings.hapticEnabled) haptics.magneticDetent()
                            toolIdx = idx
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        color = if (sel) (if (isDark) MacaronViolet else Color(0xFF563B82)) else textSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        when (toolIdx) {
            0 -> BubblePopWidget(haptics, soundEngine, settings, isDark)
            1 -> MagneticDetentWidget(haptics, settings, isDark)
            2 -> WorryShatterWidget(haptics, settings, isDark)
        }
    }
}

@Composable
fun BubblePopWidget(
    haptics: HapticEngine,
    soundEngine: SoundEngine,
    settings: SettingsStorage,
    isDark: Boolean
) {
    val popped = remember { mutableStateListOf<Boolean>().apply { repeat(30) { add(false) } } }
    var totalPopped by remember { mutableIntStateOf(0) }
    val burstParticles = remember { mutableStateListOf<PopParticle>() }
    var particleTick by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val macaronColors = remember {
        listOf(
            Color(0xFF8CD4BE), // Mint
            Color(0xFF90C2EB), // Blue
            Color(0xFFC0AEE4), // Violet
            Color(0xFFF7ADA2), // Peach
            Color(0xFFF7DB8A)  // Yellow
        )
    }

    LaunchedEffect(popped.count { it }) {
        if (popped.all { it }) {
            if (settings.hapticEnabled) haptics.shatterRelease()
            delay(500)
            for (i in popped.indices) popped[i] = false
        }
    }

    // Particle flight simulation
    LaunchedEffect(burstParticles.size) {
        if (burstParticles.isNotEmpty()) {
            repeat(20) {
                delay(16)
                for (p in burstParticles) {
                    // Update physics
                }
                particleTick++
            }
            burstParticles.clear()
        }
    }

    val cardBg = if (isDark) DarkSurface else LightSurface
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "已捏破 $totalPopped 颗解压气泡 · 清脆爆裂音效与粒子绽放",
            color = MacaronMint,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (r in 0 until 6) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (c in 0 until 5) {
                            val idx = r * 5 + c
                            val isPop = popped[idx]
                            val bubbleColor = macaronColors[(r + c) % macaronColors.size]
                            val scale by animateFloatAsState(
                                targetValue = if (isPop) 0.72f else 1f,
                                animationSpec = spring(0.42f, 550f),
                                label = "scale"
                            )

                            Box(
                                Modifier
                                    .size(54.dp)
                                    .scale(scale)
                                    .clip(CircleShape)
                                    .then(
                                        if (isPop) {
                                            Modifier
                                                .background(cardBg)
                                                .border(1.5.dp, textSecondary.copy(0.25f), CircleShape)
                                        } else {
                                            Modifier
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(
                                                            Color.White.copy(0.7f),
                                                            bubbleColor,
                                                            bubbleColor.copy(0.7f)
                                                        )
                                                    )
                                                )
                                                .border(1.5.dp, Color.White.copy(0.6f), CircleShape)
                                        }
                                    )
                                    .clickable {
                                        if (!isPop) {
                                            popped[idx] = true
                                            totalPopped++
                                            if (settings.soundEnabled) soundEngine.playPop()
                                            if (settings.hapticEnabled) haptics.bubblePop()

                                            // Spawn bursting particles
                                            repeat(10) {
                                                val ang = Random.nextFloat() * 6.28f
                                                val spd = 4f + Random.nextFloat() * 12f
                                                burstParticles.add(
                                                    PopParticle(
                                                        x = (c * 64).toFloat(),
                                                        y = (r * 64).toFloat(),
                                                        vx = cos(ang) * spd,
                                                        vy = sin(ang) * spd,
                                                        radius = 2.5f + Random.nextFloat() * 3.5f,
                                                        color = bubbleColor
                                                    )
                                                )
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPop) {
                                    Text("·", color = textSecondary.copy(0.5f), fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Burst Particle Overlay
            if (burstParticles.isNotEmpty()) {
                Canvas(Modifier.fillMaxSize()) {
                    burstParticles.forEach { p ->
                        drawCircle(p.color.copy(0.75f), p.radius, Offset(center.x + p.vx * particleTick, center.y + p.vy * particleTick))
                    }
                }
            }
        }
    }
}

@Composable
fun MagneticDetentWidget(haptics: HapticEngine, settings: SettingsStorage, isDark: Boolean) {
    val notches = 12
    var fraction by remember { mutableFloatStateOf(0.85f) }
    var lastNotch by remember { mutableIntStateOf(10) }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current

    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val cardBg = if (isDark) DarkSurface else LightSurface

    val barColor by animateColorAsState(
        Color(
            (0.55f + fraction * 0.40f).coerceIn(0f, 1f),
            (0.82f - fraction * 0.35f).coerceIn(0f, 1f),
            (0.74f - fraction * 0.30f).coerceIn(0f, 1f)
        ),
        label = "slider_color"
    )

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("向左推动磁吸棘轮 · 降低心神燥热", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
        Text("当前档位：${lastNotch + 1} / ${notches + 1}", fontSize = 12.sp, color = barColor, modifier = Modifier.padding(top = 4.dp, bottom = 28.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(86.dp)
                .clip(RoundedCornerShape(43.dp))
                .background(cardBg)
                .border(2.dp, barColor.copy(0.45f), RoundedCornerShape(43.dp))
                .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, drag ->
                        change.consume()
                        fraction = (fraction + drag / trackWidthPx).coerceIn(0f, 1f)
                        val n = (fraction * notches).roundToInt()
                        if (n != lastNotch) {
                            lastNotch = n
                            if (settings.hapticEnabled) haptics.magneticDetent()
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 32.dp), Arrangement.SpaceBetween) {
                repeat(notches + 1) { i ->
                    Box(
                        Modifier
                            .size(if (i == lastNotch) 9.dp else 4.dp)
                            .clip(CircleShape)
                            .background(if (i == lastNotch) barColor else textSecondary.copy(0.3f))
                    )
                }
            }

            val maxOffsetDp = with(density) { (trackWidthPx - 86.dp.toPx()).coerceAtLeast(0f).toDp() }
            Box(
                Modifier
                    .padding(horizontal = 8.dp)
                    .offset(x = maxOffsetDp * fraction)
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(barColor)
                    .border(2.5.dp, Color.White.copy(0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (fraction < 0.35f) "🧊" else "🔥", fontSize = 24.sp)
            }
        }

        Text("模拟实体 EDC 机械阻尼滑块，手指往复推动可显著缓解神经焦灼感。", fontSize = 11.sp, color = textSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 28.dp, start = 20.dp, end = 20.dp))
    }
}

private data class ShatterParticle(var x: Float, var y: Float, val vx: Float, val vy: Float, val radius: Float)

@Composable
fun WorryShatterWidget(haptics: HapticEngine, settings: SettingsStorage, isDark: Boolean) {
    var selectedEmotion by remember { mutableStateOf("焦虑") }
    var worryText by remember { mutableStateOf("") }
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var msg by remember { mutableStateOf<String?>(null) }
    val particles = remember { mutableStateListOf<ShatterParticle>() }
    var tick by remember { mutableIntStateOf(0) }

    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val cardBg = if (isDark) DarkSurface else LightSurface

    LaunchedEffect(isHolding) {
        if (isHolding) {
            msg = null
            while (isHolding && holdProgress < 1f) {
                delay(40)
                holdProgress = (holdProgress + 0.025f).coerceAtMost(1f)
                if (settings.hapticEnabled) haptics.holdTick(holdProgress)
            }
            if (holdProgress >= 1f) {
                if (settings.hapticEnabled) haptics.shatterRelease()
                worryText = ""
                holdProgress = 0f
                isHolding = false
                msg = "「$selectedEmotion」已被冷核命名、冷冻并彻底粉碎。"
                particles.clear()
                repeat(40) {
                    val a = Random.nextFloat() * 6.28f
                    val s = 4f + Random.nextFloat() * 12f
                    particles.add(ShatterParticle(0f, 0f, cos(a) * s, sin(a) * s, 3f + Random.nextFloat() * 6f))
                }
            }
        } else holdProgress = 0f
    }

    LaunchedEffect(particles.size) {
        if (particles.isNotEmpty()) {
            repeat(40) {
                delay(16)
                particles.forEach { it.x += it.vx; it.y += it.vy }
                tick++
            }
            particles.clear()
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("命名烦恼（神经科学证实命名情绪可激活前额叶并抑制杏仁核）", fontSize = 12.sp, color = MacaronMint)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("焦虑", "内耗", "愤怒", "慌张", "委屈", "信息过载")) { tag ->
                FilterChip(
                    selected = selectedEmotion == tag,
                    onClick = {
                        if (settings.hapticEnabled) haptics.magneticDetent()
                        selectedEmotion = tag
                    },
                    label = { Text(tag, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MacaronMint.copy(0.28f),
                        selectedLabelColor = if (isDark) MacaronMint else Color(0xFF1B634E),
                        containerColor = cardBg,
                        labelColor = textSecondary
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = worryText,
            onValueChange = { worryText = it },
            label = { Text("写下此刻在大脑盘旋的具体烦恼（选填，离线安全）", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MacaronMint,
                unfocusedBorderColor = textSecondary.copy(0.4f)
            )
        )

        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(130.dp), contentAlignment = Alignment.Center) {
            if (particles.isNotEmpty()) {
                Canvas(Modifier.fillMaxSize()) {
                    val u = tick
                    particles.forEach { p ->
                        drawCircle(MacaronMint.copy((1f - (u % 40) / 40f).coerceIn(0.1f, 0.9f)), p.radius, Offset(center.x + p.x, center.y + p.y))
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(2.dp, MacaronMint, RoundedCornerShape(18.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            isHolding = true
                            tryAwaitRelease()
                            isHolding = false
                        })
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(holdProgress)
                        .align(Alignment.CenterStart)
                        .background(MacaronMint.copy(0.38f))
                )
                Text(
                    text = if (isHolding) "正在急速深度冷冻…… ${(holdProgress * 100).toInt()}%" else "❄️ 长按 2 秒：冻结并粉碎负面思绪",
                    color = textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        msg?.let {
            Text(it, color = MacaronMint, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), fontSize = 13.sp)
        }
    }
}

// --- 4. Amygdala Temperature Log ---
@Composable
fun AmygdalaLogScreen(
    haptics: HapticEngine,
    storage: MoodStorage,
    settings: SettingsStorage,
    isDark: Boolean
) {
    var records by remember { mutableStateOf(storage.loadRecords()) }
    var emotion by remember { mutableStateOf("焦虑") }
    var beforeHeat by remember { mutableFloatStateOf(7f) }
    var afterHeat by remember { mutableFloatStateOf(3f) }
    var note by remember { mutableStateOf("") }

    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val cardBg = if (isDark) DarkSurface else LightSurface

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("杏仁核温度记录", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
        Text("纯本地保存身心冷却前后燥热度（1=冰点安宁，10=过热警报）", fontSize = 12.sp, color = textSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("焦虑", "内耗", "紧绷", "恐慌", "低落", "疲倦")) { tag ->
                        FilterChip(
                            selected = emotion == tag,
                            onClick = {
                                if (settings.hapticEnabled) haptics.magneticDetent()
                                emotion = tag
                            },
                            label = { Text(tag, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MacaronMint.copy(0.28f),
                                selectedLabelColor = if (isDark) MacaronMint else Color(0xFF1B634E),
                                containerColor = if (isDark) Color(0xFF141924) else Color(0xFFF1F3F5),
                                labelColor = textSecondary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("冷却前燥热度：${beforeHeat.roundToInt()} °C", fontSize = 13.sp, color = MacaronPeach, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = beforeHeat,
                    onValueChange = { beforeHeat = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = MacaronPeach, activeTrackColor = MacaronPeach)
                )

                Text("冷却后燥热度：${afterHeat.roundToInt()} °C", fontSize = 13.sp, color = MacaronMint, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = afterHeat,
                    onValueChange = { afterHeat = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = MacaronMint, activeTrackColor = MacaronMint)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("简述引发躁动的原因（选填）", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MacaronMint,
                        unfocusedBorderColor = textSecondary.copy(0.3f)
                    )
                )

                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (settings.hapticEnabled) haptics.bubblePop()
                        storage.saveRecord(CoolRecord(System.currentTimeMillis(), emotion, beforeHeat.roundToInt(), afterHeat.roundToInt(), note.trim()))
                        note = ""
                        records = storage.loadRecords()
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MacaronMint, contentColor = Color(0xFF1E293B))
                ) {
                    Text("记录本次冷却成效", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text("历史冷控日志（共 ${records.size} 条，100% 离线隐私）", fontSize = 13.sp, color = textSecondary)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(records) { rec ->
                val drop = rec.heatBefore - rec.heatAfter
                Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${rec.emotionTag} · ${rec.formattedTime()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary)
                            if (rec.note.isNotEmpty()) Text(rec.note, fontSize = 11.sp, color = textSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${rec.heatBefore}° → ${rec.heatAfter}°", fontWeight = FontWeight.Bold, color = MacaronMint, fontSize = 14.sp)
                            Text(if (drop > 0) "成功降温 ${drop}°" else "心绪稳定", fontSize = 11.sp, color = if (drop > 0) MacaronMint else textSecondary)
                        }
                    }
                }
            }
        }
    }
}
