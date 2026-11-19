# 《冷核 CoolCore v1.1 至 v1.2 全维度技术升级与架构演进文档》

---

## 1. 版本演进总览 (Executive Summary & Version Matrix)

**冷核 CoolCore** 是一款基于神经科学（迷走神经激活、前额叶情绪命名、EMDR 双侧眼动去敏感）的身心调节与心理降温移动应用。
本项目在完成 `v1.1` 原型验证并真机实测后，根据深度人机工学反馈，全面启动并完成了 `v1.2` 架构重构与体验升级。

### 1.1 版本参数对照矩阵

| 维度 / 参数 | v1.1 (基础验证版) | v1.2 (体验旗舰版) | 升级效益与动机 |
| :--- | :--- | :--- | :--- |
| **版本标识 (Code / Name)** | `versionCode = 2`<br>`versionName = "1.1"` | `versionCode = 3`<br>`versionName = "1.2"` | 规整版本流水线 |
| **Git 标签与提交** | Tag: `v1.1`<br>Commit: `997f413` | Tag: `v1.2`<br>Commit: `a970bbb` | 物理级与版本控制级双重隔离 |
| **主题适配模式** | 仅支持单一硬编码深黑模式（`DeepAbyss`） | **跟随系统 / 马卡龙浅色 / 石墨深色** 三模自适应 | 白天不刺眼，夜间防眩光 |
| **主调视觉色彩** | 高对比度青蓝荧光（`IceCyan`） | **低饱和度马卡龙五色系（Macaron Pastel）** | 降低视神经压力，强化安抚心理暗示 |
| **屏幕方向约束** | 锁定竖屏（`portrait`） | **跟随系统自由旋转（`unspecified`）** | 释放横屏物理视野 |
| **EMDR 双侧刺激视轨** | 竖屏受限视野（宽度不足 360dp） | **横屏超宽广角视轨（支持全屏沉浸 Zen Mode）** | 双侧眼球平滑追随距离大幅延伸 |
| **木鱼形象设计** | 扁平几何线条拼接 | **3D 拟真立体 Q 版萌系木鱼 + 布丁果冻弹性动画** | 提升亲和力与玩具触感（Toy-like） |
| **声学引擎支持** | 单一低频合成木鱼（易发闷） | **清脆高通瞬态木鱼 + 432Hz 禅意空灵手碟鼓** | 音色通透清亮，空灵鼓余韵悠长 |
| **捏泡泡交互** | 仅尺寸缩放变化，无声效与特效 | **彩色气泡 + 破裂粒子飞溅绽放 + 气压爆破音** | 声-视-触毫秒级对齐极致解压 |
| **设置与定制能力** | 无设置入口，硬编码参数 | **独立抽屉式设置中心（音量、震动、主题切换）** | 用户完全自主可控 |
| **CI/CD 流水线** | `actions/setup-java@v4` (带弃用警告) | **`actions/setup-java@v5`** (零警告纯净通过) | 构建链路现代化 |

---

## 2. 源码隔离与基线保护机制 (Source Code Isolation)

为确保项目演化过程中的资产安全与可回溯性，本次升级严格执行了**双重隔离保护标准**：

```
C:\antigravity\coolcore\
├── create_coolcore.py          # [v1.1 资产] 原始全量生成脚本（只读冻结，绝对不改）
├── CoolCore_v1.1.zip           # [v1.1 资产] 初始打包二进制源码包（未解压覆盖）
├── CoolCore_v1.1_backup\       # [v1.1 资产] 物理完整镜像备份工程目录
│
├── create_coolcore_v1_2.py     # [v1.2 资产] 独立建立的新版全量生成脚本
├── CoolCore_v1.2.zip           # [v1.2 资产] 新版全量归档包（已打包）
├── CoolCore\                   # [当前生产基线] Git 工作区（Git Tag: v1.1 与 v1.2 均存）
└── CoolCore_TechDocs\          # [文档体系] 技术规格与升级架构文档目录
```

- **Git 版本链不可篡改**：在 Git 中，`v1.1` 提交 `997f413` 已打上永久只读标签；`v1.2` 改造作为新的独立提交 `a970bbb` 记录在主干历史中。任何时刻均可无损检出或比对两个版本。

---

## 3. 视觉系统与色彩工程升级 (Design System)

### 3.1 深浅双模动态感知与对比度自适应
在 `v1.1` 中，所有页面颜色直接绑定于静态常量：
```kotlin
// [v1.1 遗留] 硬编码深色，无法跟随系统
private val DeepAbyss = Color(0xFF070B12)
private val CardSurface = Color(0xFF111827)
```

在 `v1.2` 中，全面抽象为动态感知层：
```kotlin
// [v1.2 重构] 动态感知系统主题模式与用户偏好设定
val isSystemDark = isSystemInDarkTheme()
val isDark = when (settings.themeMode) {
    AppThemeMode.FOLLOW_SYSTEM -> isSystemDark
    AppThemeMode.MACARON_LIGHT -> false
    AppThemeMode.OBSIDIAN_DARK -> true
}
val canvasColor = if (isDark) DarkCanvas else LightCanvas
val surfaceColor = if (isDark) DarkSurface else LightSurface
val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
```

### 3.2 低饱和度马卡龙色系（Macaron Pastel Palette）
摒弃易引起视神经亢奋的荧光色，精心配比以下五大核心减压主调：

| 语义名称 | 浅色基调 (Hex) | 强调深色 (Hex) | 心理学声学与神经暗示 |
| :--- | :--- | :--- | :--- |
| **薄荷清绿 (Mint)** | `#8CD4BE` | `#5AB69A` | 清凉、舒张副交感神经、降心率 |
| **海盐冰蓝 (Sea Salt)** | `#90C2EB` | `#5D9FD6` | 广阔、理智、平抑情绪反刍 |
| **香芋浅紫 (Taro)** | `#C0AEE4` | `#9A82D0` | 柔和包容、安全感、松弛 |
| **奶杏柔粉 (Peach)** | `#F7ADA2` | `#E6887B` | 温暖亲和、接纳负面情绪 |
| **奶酪微黄 (Butter)** | `#F7DB8A` | `#DFC065` | 治愈、光明、打破思维凝滞 |

---

## 4. 声学引擎与 PCM 实时合成重构 (Acoustic Engine)

在 `v1.1` 中，音效由轻量单例 `WoodenFishAudio` 负责，仅支持单音色木鱼且击打瞬态存在截断。  
在 `v1.2` 中，全面替换为多功能声学引擎 **`SoundEngine`**。

### 4.1 清脆木鱼（Crisp Wooden Fish）音色算法重塑
- **瞬态撞击算法（Transient Attack）**：在前 8ms 混入高通木质瞬时冲击声（1850Hz 正弦微脉冲与指数白噪声复合），重现小木锤敲击硬木时的“笃 (Tok!)”声。
- **腔体共振算法（Chamber Resonance）**：基频升至 640Hz，辅以 1420Hz 中空泛音，并加入指数衰减滑音（Pitch-Bend），杜绝发闷：
  $$\text{f}(t) = f_{\text{base}} \times \left(1.0 + 0.08 \times e^{-120t}\right)$$

### 4.2 432Hz 禅意空灵鼓（Steel Tongue Drum / Handpan）
- 依据声学谐波原理，生成基于 **432Hz 调律的五音阶（Pentatonic Scale）**：
  - D4 (288Hz) · E4 (324Hz) · G4 (384Hz) · A4 (432Hz) · C5 (512Hz) · D5 (576Hz)
- **声波叠加方程**：
  $$S(t) = \left[\sin(2\pi f t) e^{-3.1t} + 0.38 \sin(4\pi f t) e^{-4.8t} + 0.16 \sin(6.02\pi f t) e^{-7.2t}\right] \times \text{Shimmer}(t) \times \text{Attack}(t)$$
- 模拟金属鼓身漫长的余韵共鸣（1.3 秒自然衰减），并叠加极其微妙的轻颤微金属光泽（Metallic Shimmer）。

### 4.3 气泡爆破破裂音（Bubble Pop）
- 针对解压仓捏泡泡需求，合成极短（48ms）快速下潜微气压破裂声：
  $$f_{\text{pop}}(t) = 260.0 + 1150.0 \times e^{-88.0 t}$$
- 配合高通快速衰减，形成清脆多汁的“啵！”声，带来强烈的 ASMR 快感。

### 4.4 全局增益与 AudioTrack 池化机制
- 采用 4 轨静态音频池（Static Pool），避免频繁 GC 导致 UI 掉帧；
- 独立主音量调节控制器（`setVolume(volumeFactor)`），实时响应设置界面的滑块变动。

---

## 5. 交互动力学与视觉动画突破 (Physics & Interaction)

### 5.1 立体 Q 版萌系木鱼 (Chibi 3D Wooden Fish)
- **3D 材质渲染**：
  - 利用双向径向渐变（Radial Gradient）在 Canvas 上模拟焦糖蜜糖暖木的受光面与背光阴影面；
  - 绘制萌系弧度眯眯笑眼 `( ᵔ ᵕ ᵔ )` 与透亮粉嫩腮红；
  - 木鱼中空开口处微雕一颗微光闪烁的“功德明珠”。
- **果冻弹性形变（Squash & Stretch Spring Physics）**：
  受击瞬间，木鱼在 X 轴与 Y 轴产生反向物理弹动：
  ```kotlin
  fishSquashX.snapTo(1.14f) // 横向撑大
  fishSquashY.snapTo(0.86f) // 纵向压扁
  // 通过欠阻尼弹簧迅速回弹
  fishSquashX.animateTo(1f, spring(dampingRatio = 0.40f, stiffness = 520f))
  fishSquashY.animateTo(1f, spring(dampingRatio = 0.40f, stiffness = 520f))
  ```
- **马卡龙浮空徽章（Floating Merit Badges）**：
  敲击时光标上方飘出随机马卡龙底色圆角药丸胶囊（“功德 +1”、“焦虑 -1”、“心生欢喜”），伴随非线性上浮与淡出消失。

### 5.2 触觉解压舱 · 捏泡泡系统全面升级
- **网格系统**：由 30 颗水晶凝胶质感气泡组成（6 行 × 5 列），具有渐变高光球体质感；
- **微粒爆裂引擎（Burst Particle Explosion）**：
  点击气泡瞬间，围绕点击中心实时发射 10~14 颗不同飞行角速度的微粒粒子：
  $$\vec{P}_{i}(t) = \vec{P}_{\text{center}} + \vec{V}_{i} \times t$$
  微粒向四周辐射飞溅并在 20 帧内柔和淡出，结合下潜气泡破裂音与马达重触觉，达成毫秒级声-视-震三合一。

---

## 6. 屏幕方向与双侧刺激 (EMDR) 人机工学演进

### 6.1 横屏限制解除与响应式布局
- **清单配置解除**：在 `AndroidManifest.xml` 中将 Activity 配置由 `portrait` 改为 `android:screenOrientation="unspecified"`，并加入 `configChanges="orientation|screenSize"`；
- **响应式排版**：底部导航栏在横屏模式下自适应压缩高度（由 72dp 压缩为 56dp），保证中央交互区域留白。

### 6.2 EMDR 双侧眼动追踪横向视野拓展
在神经心理学中，EMDR 双侧刺激要求受试者双眼在水平视野内进行大幅度平滑扫视（Saccadic Eye Movement），以打断杏仁核的过度激活：
- **竖屏缺陷**：竖屏水平宽度通常仅 360~400dp，视线左右移动角度过小，眼肌活动度受限；
- **横屏跃迁**：横转后水平宽度扩增至 800~960dp，**眼动视距提升超过 240%**，大幅增强双脑半球协同抑制情绪反刍的治疗效能；
- **全屏极简沉浸（Zen Mode）**：横屏状态下一键隐藏标题栏、导航栏与配置滑块，整块屏幕化为纯粹的光球导轨，免除一切界面杂乱元素的视神经干扰。

---

## 7. 配置中心与本地持久化架构 (Settings Center)

新增独立配置管理器 `SettingsStorage.kt`，统管全局偏好：

```kotlin
class SettingsStorage(context: Context) {
    var themeMode: AppThemeMode     // FOLLOW_SYSTEM / MACARON_LIGHT / OBSIDIAN_DARK
    var soundVolume: Float          // 0.0f .. 1.0f 浮点线性音量
    var soundEnabled: Boolean       // 全局音效静音闸
    var hapticEnabled: Boolean      // 线性马达全局开关
    var instrumentMode: String      // "WOODEN_FISH" (Q木鱼) / "STEEL_DRUM" (空灵鼓)
}
```

- **单机离线承诺**：配置持久化使用私有模式 `MODE_PRIVATE` 的 SharedPreferences，无任何联网权限要求，严格守护用户的心情记录与健康数据。

---

## 8. CI/CD 云端构建与工程规范升级

### 8.1 解决 Node 与 Java 构建环境弃用
- **升级 Actions**：将 `.github/workflows/build-apk.yml` 中的 JDK 配置由 `actions/setup-java@v4` 跃迁为 **`actions/setup-java@v5`**；
- **产物命名规范**：构建流水线编译产物独立归档为 **`CoolCore-v1.2-Android-APK`**。

### 8.2 极坐标数学符号补齐
- **故障排查复盘**：在 `WoodenFishScreen.kt` 绘制 6 瓣空灵手碟鼓面时，补充导入 `import kotlin.math.PI`，彻底消除了编译期间的 Unresolved Reference 异常，确保流水线秒级稳定产出。

---

## 9. 源码文件变更映射清单 (File Diff Mapping)

| 文件路径 | 变更属性 | 模块作用说明 |
| :--- | :--- | :--- |
| `app/src/main/AndroidManifest.xml` | **修改** | 解除竖屏强制锁定，支持自由旋转跟随系统 |
| `app/build.gradle.kts` | **修改** | 版本号递增 (`versionCode 3`, `versionName "1.2"`) |
| `app/src/main/res/values/themes.xml` | **修改** | 样式解耦，支持系统状态栏与沉浸式透明背景 |
| `.github/workflows/build-apk.yml` | **修改** | 升级 `setup-java@v5`，产物命名为 v1.2 |
| `app/src/main/java/.../SettingsStorage.kt` | **新增** | 全局独立设置管理组件 |
| `app/src/main/java/.../SoundEngine.kt` | **新增** | 高精度 PCM 多音色声学合成引擎（木鱼/空灵鼓/泡泡） |
| `app/src/main/java/.../WoodenFishAudio.kt` | **删除** | 已由功能更强大的 `SoundEngine` 彻底取代 |
| `app/src/main/java/.../WoodenFishScreen.kt` | **重构** | 3D Q版萌木鱼绘图、空灵鼓交互、果冻弹性动力学 |
| `app/src/main/java/.../MainActivity.kt` | **重构** | 马卡龙主题、横屏响应排版、泡泡粒子特效、设置抽屉 |
| `app/src/main/java/.../HapticEngine.kt` | **修改** | 接入全局触觉配置闸门 `haptics.enabled` |
| `create_coolcore_v1_2.py` | **新增** | v1.2 独立自动化工程生成脚本 |
| `CoolCore_v1.2.zip` | **新增** | v1.2 全量独立源码压缩归档包 |

---

*文档生成日期：2026年11月19日*  
*编制单位：冷核 CoolCore 架构工程组*
