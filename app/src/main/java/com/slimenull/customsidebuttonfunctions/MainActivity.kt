package com.slimenull.customsidebuttonfunctions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.RoundedCorner
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import com.slimenull.customsidebuttonfunctions.data.SettingsStore
import com.slimenull.customsidebuttonfunctions.model.ActionType
import com.slimenull.customsidebuttonfunctions.model.AppSettings
import com.slimenull.customsidebuttonfunctions.model.CommonAction
import com.slimenull.customsidebuttonfunctions.model.CustomActionSettings
import com.slimenull.customsidebuttonfunctions.model.CursorControlMode
import com.slimenull.customsidebuttonfunctions.model.CursorLongPressAction
import com.slimenull.customsidebuttonfunctions.model.MAX_CURSOR_LONG_PRESS_MS
import com.slimenull.customsidebuttonfunctions.model.MAX_CURSOR_REPEAT_INTERVAL_MS
import com.slimenull.customsidebuttonfunctions.model.MIN_CURSOR_LONG_PRESS_MS
import com.slimenull.customsidebuttonfunctions.model.MIN_CURSOR_REPEAT_INTERVAL_MS
import com.slimenull.customsidebuttonfunctions.model.MorseBinding
import com.slimenull.customsidebuttonfunctions.model.OperationMode
import com.slimenull.customsidebuttonfunctions.model.SideKeyCombinationAction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

private val AppBackground = Color(0xFFF3F7FD)
private val AppBlue = Color(0xFF347FE8)
private val AppBlueDark = Color(0xFF1954A6)
private val AppBlueSoft = Color(0xFFE7F1FF)
private val AppText = Color(0xFF152238)
private val AppMuted = Color(0xFF718198)
private val AppGreen = Color(0xFF2DAE78)
private const val PageShadeOpacity = 0.24f
private const val PageSlideFraction = 0.25f
private const val PageTransitionDurationMs = 350
private val AppColors = lightColorScheme(
    primary = AppBlue,
    onPrimary = Color.White,
    primaryContainer = AppBlueSoft,
    onPrimaryContainer = AppBlueDark,
    inversePrimary = Color(0xFFB3D1FF),
    secondary = Color(0xFF466A80),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0EEF4),
    onSecondaryContainer = Color(0xFF254C61),
    tertiary = Color(0xFF28765D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDF3E9),
    onTertiaryContainer = Color(0xFF17513D),
    background = AppBackground,
    onBackground = AppText,
    surface = Color.White,
    onSurface = AppText,
    surfaceVariant = Color(0xFFE9F0F8),
    onSurfaceVariant = AppMuted,
    surfaceTint = AppBlue,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE8EEF5),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFCFDFF),
    surfaceContainer = Color(0xFFF8FBFF),
    surfaceContainerHigh = Color(0xFFF1F5FA),
    surfaceContainerHighest = Color(0xFFE8F0F7),
    inverseSurface = AppText,
    inverseOnSurface = Color.White,
    outline = Color(0xFF889AAF),
    outlineVariant = Color(0xFFD6E1EC),
    error = Color(0xFFB43F43),
    onError = Color.White,
    errorContainer = Color(0xFFFDE8E8),
    onErrorContainer = Color(0xFF6D1C22),
    scrim = Color.Black
)

private data class ActivityPreset(
    @get:StringRes val titleRes: Int,
    val title: String,
    val packageName: String,
    val className: String,
    val action: String = "",
    val launchInSmallWindow: Boolean = true
)

private data class InstalledApp(
    val packageName: String,
    val label: String
)

private val activityPresets = listOf(
    ActivityPreset(
        titleRes = R.string.preset_quick_note,
        title = "便签速记",
        packageName = "com.coloros.note",
        className = "com.nearme.note.activity.richedit.QuickNoteViewRichEditActivity"
    )
)

private enum class BottomTab { HOME, OTHER, ABOUT }

private enum class GestureKind(
    @get:StringRes val titleRes: Int,
    @get:StringRes val subtitleRes: Int,
    val defaultTitle: String
) {
    SINGLE(R.string.gesture_single_title, R.string.gesture_single_subtitle, "单击"),
    DOUBLE(R.string.gesture_double_title, R.string.gesture_double_subtitle, "双击"),
    LONG(R.string.gesture_long_title, R.string.gesture_long_subtitle, "长按");

    val title: String get() = defaultTitle
}

private sealed interface Route {
    data object Home : Route
    data object Other : Route
    data object About : Route
    data object Morse : Route
    data object Feedback : Route
    data object Advanced : Route
    data class Gesture(val kind: GestureKind) : Route
}

private data class ActionChoice(val action: ActionType, val common: CommonAction? = null) {
    @get:StringRes
    val titleRes: Int get() = common?.titleRes ?: action.titleRes
    val title: String get() = common?.title ?: action.title
    val icon: ImageVector get() = if (common != null) Icons.Default.Tune else actionIcon(action)
}

private val actionChoices = ActionType.entries.flatMap { action ->
    if (action == ActionType.COMMON_FUNCTION) {
        CommonAction.entries.map { ActionChoice(action, it) }
    } else {
        listOf(ActionChoice(action))
    }
}
private val morseActionChoices = actionChoices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CustomSideButtonApp() }
        requestShizukuPermissionIfNeeded()
    }

    private fun requestShizukuPermissionIfNeeded() {
        runCatching {
            if (Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED
            ) {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private companion object {
        private const val SHIZUKU_PERMISSION_REQUEST_CODE = 735
    }
}

@Composable
fun rememberScreenCornerRadius(): Dp {
    val view = LocalView.current
    var radiusPx by remember(view) { mutableIntStateOf(0) }

    DisposableEffect(view) {
        val observer = view.viewTreeObserver
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            radiusPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                view.rootWindowInsets
                    ?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
                    ?.radius ?: 0
            } else {
                0
            }
        }

        observer.addOnGlobalLayoutListener(listener)
        listener.onGlobalLayout()
        onDispose {
            if (observer.isAlive) observer.removeOnGlobalLayoutListener(listener)
        }
    }

    return with(LocalDensity.current) { radiusPx.toDp() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun CustomSideButtonApp() {
    val context = LocalContext.current
    val view = LocalView.current
    var settings by remember { mutableStateOf(SettingsStore.load(context)) }
    var route by remember { mutableStateOf<Route>(Route.Home) }
    var displayedDetail by remember { mutableStateOf<Route?>(null) }
    var predictiveTarget by remember { mutableStateOf<Route?>(null) }
    val predictiveProgress = remember { Animatable(0f) }
    var routeTransitionEpoch by remember { mutableIntStateOf(0) }
    var skipCommittedReturn by remember { mutableStateOf(false) }
    var contentWidth by remember { mutableFloatStateOf(0f) }

    fun persist(next: AppSettings) {
        settings = next
        SettingsStore.save(next)
    }

    fun navigate(next: Route) {
        if (route != next) {
            skipCommittedReturn = false
            if (!isPrimaryRoute(next)) displayedDetail = next
            route = next
        }
    }

    val primaryRoute: Route = when (route) {
        Route.Other -> Route.Other
        Route.About -> Route.About
        else -> Route.Home
    }

    val animatedShade by animateFloatAsState(
        targetValue = if (isPrimaryRoute(route)) 0f else PageShadeOpacity,
        animationSpec = tween(PageTransitionDurationMs),
        label = "Page shade"
    )
    val pageShade = when {
        predictiveTarget != null -> animatedShade * (1f - predictiveProgress.value)
        skipCommittedReturn -> 0f
        else -> animatedShade
    }
    val animatedRootOffset by animateFloatAsState(
        targetValue = if (isPrimaryRoute(route)) 0f else -PageSlideFraction,
        animationSpec = tween(PageTransitionDurationMs),
        label = "Primary page offset"
    )
    val rootOffset = when {
        predictiveTarget != null -> -PageSlideFraction * (1f - predictiveProgress.value)
        skipCommittedReturn -> 0f
        else -> animatedRootOffset
    }
    val cornerDp = rememberScreenCornerRadius()

    PredictiveBackHandler(enabled = !isPrimaryRoute(route)) { progress ->
        val destination = backDestination()
        try {
            predictiveTarget = destination
            progress.collect { event ->
                predictiveProgress.snapTo(event.progress.coerceIn(0f, 1f))
            }
            predictiveProgress.animateTo(1f, tween(PageTransitionDurationMs))
            // Replace the transition host at commit so it cannot retain the outgoing page.
            routeTransitionEpoch++
            skipCommittedReturn = true
            route = destination
            predictiveTarget = null
        } catch (_: CancellationException) {
            predictiveProgress.animateTo(0f, tween(180))
        } finally {
            predictiveTarget = null
            predictiveProgress.snapTo(0f)
        }
    }

    MaterialTheme(colorScheme = AppColors) {
        Scaffold(
            containerColor = AppBackground
        ) { padding ->
            Box(Modifier.fillMaxSize().onSizeChanged { contentWidth = it.width.toFloat() }) {
                PrimaryPageLayer(
                    primaryRoute = primaryRoute,
                    settings = settings,
                    navigate = ::navigate,
                    persist = ::persist,
                    padding = padding,
                    contentWidth = contentWidth,
                    rootOffset = rootOffset,
                    onTabSelected = { tab ->
                        clickSound(view)
                        navigate(
                            when (tab) {
                                BottomTab.HOME -> Route.Home
                                BottomTab.OTHER -> Route.Other
                                BottomTab.ABOUT -> Route.About
                            }
                        )
                    }
                )

                Box(Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = pageShade))
                    .then(if (!isPrimaryRoute(route) || pageShade > 0f) Modifier.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) awaitPointerEvent().changes.forEach { it.consume() }
                        }
                    } else Modifier)
                )

                key(routeTransitionEpoch) {
                    AnimatedVisibility(
                        visible = !isPrimaryRoute(route),
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            val progress = if (predictiveTarget != null) predictiveProgress.value else 0f
                            translationX = contentWidth * progress
                            scaleX = 1f - 0.2f * progress
                            scaleY = 1f - 0.2f * progress
                        },
                        enter = slideInHorizontally(tween(PageTransitionDurationMs)) { it } +
                            scaleIn(initialScale = 0.8f, animationSpec = tween(PageTransitionDurationMs)),
                        exit = slideOutHorizontally(tween(PageTransitionDurationMs)) { it } +
                            scaleOut(targetScale = 0.8f, animationSpec = tween(PageTransitionDurationMs)),
                        label = "Detail transition"
                    ) {
                        displayedDetail?.let { detail ->
                            val clipDetail = predictiveTarget != null ||
                                transition.currentState != transition.targetState
                            Box(Modifier.fillMaxSize().graphicsLayer {
                                shape = RoundedCornerShape(cornerDp)
                                clip = clipDetail
                            }.background(AppBackground)) {
                                RouteScreen(
                                    route = detail,
                                    settings = settings,
                                    navigate = ::navigate,
                                    persist = ::persist,
                                    padding = padding
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PrimaryPageLayer(
    primaryRoute: Route,
    settings: AppSettings,
    navigate: (Route) -> Unit,
    persist: (AppSettings) -> Unit,
    padding: PaddingValues,
    contentWidth: Float,
    rootOffset: Float,
    onTabSelected: (BottomTab) -> Unit
) {
    val selectedTab = when (primaryRoute) {
        Route.Other -> BottomTab.OTHER
        Route.About -> BottomTab.ABOUT
        else -> BottomTab.HOME
    }
    Column(
        Modifier.fillMaxSize()
            .graphicsLayer { translationX = contentWidth * rootOffset }
            .background(AppBackground)
    ) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            AnimatedContent(
                targetState = primaryRoute,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val direction = routeDirection(initialState, targetState)
                    (slideInHorizontally(tween(PageTransitionDurationMs)) { it * direction } togetherWith
                        slideOutHorizontally(tween(PageTransitionDurationMs)) { -it * direction })
                        .using(SizeTransform(clip = true))
                },
                label = "Tab transition"
            ) { current ->
                val actualPadding = object : PaddingValues by padding {
                    override fun calculateBottomPadding(): Dp = 0.dp
                }

                RouteScreen(
                    route = current,
                    settings = settings,
                    navigate = navigate,
                    persist = persist,
                    padding = actualPadding
                )
            }
        }
        NavigationBar(containerColor = Color.White) {
            NavigationBarItem(
                selected = selectedTab == BottomTab.HOME,
                onClick = { onTabSelected(BottomTab.HOME) },
                icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.tab_home)) },
                label = { Text(stringResource(R.string.tab_home)) }
            )
            NavigationBarItem(
                selected = selectedTab == BottomTab.OTHER,
                onClick = { onTabSelected(BottomTab.OTHER) },
                icon = { Icon(Icons.Default.MoreHoriz, contentDescription = stringResource(R.string.tab_other)) },
                label = { Text(stringResource(R.string.tab_other)) }
            )
            NavigationBarItem(
                selected = selectedTab == BottomTab.ABOUT,
                onClick = { onTabSelected(BottomTab.ABOUT) },
                icon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.tab_about)) },
                label = { Text(stringResource(R.string.tab_about)) }
            )
        }
    }
}

private fun backDestination(): Route = Route.Home

private fun isPrimaryRoute(route: Route): Boolean =
    route is Route.Home || route is Route.Other || route is Route.About

private fun routeDirection(from: Route, to: Route): Int =
    if (primaryRouteIndex(to) >= primaryRouteIndex(from)) 1 else -1

private fun primaryRouteIndex(route: Route): Int = when (route) {
    Route.Home -> 0
    Route.Other -> 1
    Route.About -> 2
    else -> 0
}

@Composable
private fun RouteScreen(
    route: Route,
    settings: AppSettings,
    navigate: (Route) -> Unit,
    persist: (AppSettings) -> Unit,
    padding: PaddingValues
) {
    when (route) {
        Route.Home -> HomeScreen(settings, navigate, persist, padding)
        Route.Other -> OtherScreen(settings, persist, padding)
        Route.About -> AboutScreen(padding)
        Route.Morse -> MorseScreen(settings, persist, { navigate(Route.Home) }, padding)
        Route.Feedback -> FeedbackScreen(settings, persist, { navigate(Route.Home) }, padding)
        Route.Advanced -> AdvancedScreen(settings, persist, { navigate(Route.Home) }, padding)
        is Route.Gesture -> GestureScreen(route.kind, settings, persist, { navigate(Route.Home) }, padding)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HomeScreen(
    settings: AppSettings,
    navigate: (Route) -> Unit,
    persist: (AppSettings) -> Unit,
    padding: PaddingValues
) {
    val view = LocalView.current
    Column(Modifier.fillMaxSize().padding(padding)) {
        Text(
            stringResource(R.string.side_key_settings_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Default.Tune)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.enable_side_key_functions), fontWeight = FontWeight.SemiBold)
                            Text(stringResource(if (settings.enabled) R.string.status_enabled else R.string.status_disabled), style = MaterialTheme.typography.bodySmall, color = if (settings.enabled) AppGreen else AppMuted)
                        }
                        Switch(settings.enabled, { clickSound(view); persist(settings.copy(enabled = it)) })
                    }
                }
            }
            item { SectionLabel(stringResource(R.string.section_operation_mode)) }
            item {
                val modeColors = SegmentedButtonDefaults.colors(
                    activeContainerColor = AppBlueSoft,
                    activeContentColor = AppBlueDark,
                    activeBorderColor = AppBlue,
                    inactiveContainerColor = Color.White,
                    inactiveContentColor = AppText,
                    inactiveBorderColor = Color(0xFF9AAFC6)
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    OperationMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.operationMode == mode,
                            onClick = { clickSound(view); persist(settings.copy(operationMode = mode)) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index, OperationMode.entries.size, baseShape = RoundedCornerShape(8.dp)
                            ),
                            colors = modeColors,
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(mode.titleRes)) }
                        )
                    }
                }
            }
            item {
                AnimatedContent(
                    targetState = settings.operationMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(PageTransitionDurationMs)) +
                            scaleIn(initialScale = 0.9f, animationSpec = tween(PageTransitionDurationMs)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(PageTransitionDurationMs)) +
                                    scaleOut(targetScale = 0.9f, animationSpec = tween(PageTransitionDurationMs))
                            )
                            .using(
                                SizeTransform(
                                    clip = false,
                                    sizeAnimationSpec = { _, _ -> tween(PageTransitionDurationMs) }
                                )
                            )
                    },
                    label = "Operation mode content"
                ) { mode ->
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionLabel(stringResource(mode.titleRes))
                        when (mode) {
                            OperationMode.DISABLED -> Unit
                            OperationMode.SIMPLE -> {
                                GestureRow(GestureKind.SINGLE, settings.singleAction, settings.singleCustom, navigate)
                                GestureRow(GestureKind.DOUBLE, settings.doubleAction, settings.doubleCustom, navigate)
                                GestureRow(GestureKind.LONG, settings.longAction, settings.longCustom, navigate)
                            }
                            OperationMode.MORSE -> {
                                SettingRow(Icons.Default.Code, stringResource(R.string.morse_code_settings), stringResource(R.string.morse_commands_count, settings.morseBindings.size)) {
                                    navigate(Route.Morse)
                                }
                            }
                        }
                    }
                }
            }
            item { SectionLabel(stringResource(R.string.section_other_settings)) }
            item {
                SettingRow(Icons.Default.Vibration, stringResource(R.string.setting_vibration_feedback), stringResource(R.string.setting_vibration_feedback_desc)) { navigate(Route.Feedback) }
            }
            item {
                SettingRow(Icons.Default.Settings, stringResource(R.string.setting_advanced), stringResource(R.string.setting_advanced_desc)) { navigate(Route.Advanced) }
            }
        }
    }
}

@Composable
private fun GestureRow(
    kind: GestureKind,
    action: ActionType,
    custom: CustomActionSettings,
    navigate: (Route) -> Unit
) {
    val title = when {
        action == ActionType.NONE -> stringResource(R.string.not_configured)
        action == ActionType.COMMON_FUNCTION -> stringResource(custom.commonAction.titleRes)
        else -> stringResource(action.titleRes)
    }
    SettingRow(gestureIcon(kind), stringResource(kind.titleRes), title) {
        navigate(Route.Gesture(kind))
    }
}

@Composable
private fun GestureScreen(
    kind: GestureKind,
    settings: AppSettings,
    persist: (AppSettings) -> Unit,
    back: () -> Unit,
    padding: PaddingValues
) {
    val view = LocalView.current
    var sliderValue by remember(kind, settings) {
        mutableFloatStateOf(
            when (kind) {
                GestureKind.DOUBLE -> settings.doubleClickWindowMs.coerceIn(100L, 800L).toFloat()
                GestureKind.LONG -> settings.longPressMs.coerceIn(100L, 800L).toFloat()
                GestureKind.SINGLE -> 0f
            }
        )
    }
    val currentAction = selectedAction(kind, settings)
    val currentCustom = selectedCustom(kind, settings)
    val currentWakeScreen = selectedWakeScreen(kind, settings)
    val currentVibration = selectedVibration(kind, settings)
    Column(Modifier.fillMaxSize().padding(padding)) {
        BackTitle(stringResource(kind.titleRes), back)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            IntroCard(
                icon = gestureIcon(kind),
                title = stringResource(kind.titleRes),
                description = when (kind) {
                    GestureKind.SINGLE -> stringResource(R.string.gesture_single_hint)
                    GestureKind.DOUBLE -> stringResource(R.string.gesture_double_hint)
                    GestureKind.LONG -> stringResource(R.string.gesture_long_hint)
                }
            )
          }
          if (kind != GestureKind.SINGLE) {
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(stringResource(if (kind == GestureKind.DOUBLE) R.string.gesture_wait_time else R.string.gesture_press_duration), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Slider(
                                value = sliderValue,
                                onValueChange = { sliderValue = it },
                                onValueChangeFinished = {
                                    clickSound(view)
                                    val value = sliderValue.toLong()
                                    persist(if (kind == GestureKind.DOUBLE) settings.copy(doubleClickWindowMs = value) else settings.copy(longPressMs = value))
                                },
                                valueRange = 100f..800f,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            TimeValueEditor(
                                value = sliderValue.toLong(),
                                minValue = 100L,
                                maxValue = 800L,
                                onChange = { value ->
                                    sliderValue = value.toFloat()
                                    clickSound(view)
                                    persist(
                                        if (kind == GestureKind.DOUBLE) {
                                            settings.copy(doubleClickWindowMs = value)
                                        } else {
                                            settings.copy(longPressMs = value)
                                        }
                                    )
                                }
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("100ms", style = MaterialTheme.typography.labelSmall, color = AppMuted)
                            Text("800ms", style = MaterialTheme.typography.labelSmall, color = AppMuted)
                        }
                    }
                }
            }
          }
          item { SectionLabel(stringResource(R.string.select_action_to_execute)) }
          item {
            val selected = ActionChoice(
                currentAction,
                if (currentAction == ActionType.COMMON_FUNCTION) currentCustom.commonAction else null
            )
            ActionPicker(selected) { choice ->
                val next = updateAction(kind, settings, choice.action)
                persist(if (choice.common != null) {
                    updateCustom(kind, next, currentCustom.copy(commonAction = choice.common))
                } else next)
            }
          }
          if (currentAction in listOf(ActionType.XIAOBU_SHORTCUT, ActionType.CUSTOM_ACTIVITY,
                  ActionType.CUSTOM_URL, ActionType.SHELL_COMMAND)) {
            item {
                CustomActionEditor(currentAction, currentCustom) {
                    persist(updateCustom(kind, settings, it))
                }
            }
          }
          item { SectionLabel(stringResource(R.string.section_options)) }
          item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    SettingSwitchRow(stringResource(R.string.option_wake_screen), null, currentWakeScreen) {
                        persist(updateWakeScreen(kind, settings, it))
                    }
                    if (!settings.vibrationEnabled) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFFEAF0F7))
                        SettingSwitchRow(stringResource(R.string.option_vibrate_on_trigger), null, currentVibration) {
                            persist(updateVibration(kind, settings, it))
                        }
                    }
                }
            }
          }
          item {
            InfoCard(if (kind == GestureKind.DOUBLE && settings.doubleAction == ActionType.NONE) stringResource(R.string.gesture_hint_single_no_double) else if (kind == GestureKind.LONG) stringResource(R.string.gesture_hint_long_exclusive) else stringResource(R.string.changes_saved_hot_reload))
          }
        }
    }
}

@Composable
private fun MorseScreen(
    settings: AppSettings,
    persist: (AppSettings) -> Unit,
    back: () -> Unit,
    padding: PaddingValues
) {
    val view = LocalView.current
    var editorOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<MorseBinding?>(null) }
    var deleting by remember { mutableStateOf<MorseBinding?>(null) }

    Column(Modifier.fillMaxSize().padding(padding)) {
        BackTitle(stringResource(R.string.morse_code_operations), back)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    SectionLabel(stringResource(R.string.morse_command_mappings))
                    Button(onClick = { clickSound(view); editing = null; editorOpen = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_add))
                    }
                }
            }
            if (settings.morseBindings.isEmpty()) {
                item { Text(stringResource(R.string.morse_no_commands), color = AppMuted, modifier = Modifier.padding(vertical = 16.dp)) }
            }
            items(settings.morseBindings, key = { it.sequence }) { binding ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).clickable {
                            clickSound(view)
                            editing = binding
                            editorOpen = true
                        }) {
                            Text(morseSequenceTitle(binding.sequence),
                                fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(morseActionTitle(binding), color = AppMuted,
                                style = MaterialTheme.typography.bodySmall, maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { clickSound(view); editing = binding; editorOpen = true }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit_command))
                        }
                        IconButton(onClick = { clickSound(view); deleting = binding }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.action_delete_command))
                        }
                    }
                }
            }
            item { SectionLabel(stringResource(R.string.morse_key_settings)) }
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(14.dp)) {
                        SettingSwitchRow(stringResource(R.string.morse_vibrate_on_press), null, settings.morsePressVibrationEnabled) {
                            persist(settings.copy(morsePressVibrationEnabled = it))
                        }
                        HorizontalDivider(color = Color(0xFFEAF0F7))
                        SettingSwitchRow(
                            stringResource(R.string.morse_vibrate_on_long_press),
                            null,
                            settings.morseLongVibrationEnabled
                        ) {
                            persist(settings.copy(morseLongVibrationEnabled = it))
                        }
                        HorizontalDivider(color = Color(0xFFEAF0F7))
                        SettingSwitchRow(
                            stringResource(R.string.morse_immediate_execution),
                            null,
                            settings.morseImmediateExecutionEnabled
                        ) {
                            persist(settings.copy(morseImmediateExecutionEnabled = it))
                        }
                    }
                }
            }
            item { SectionLabel(stringResource(R.string.section_timing)) }
            item {
                MorseTimingCard(stringResource(R.string.timing_long_press_duration), settings.morseLongPressMs) {
                    persist(settings.copy(morseLongPressMs = it))
                }
            }
            item {
                MorseTimingCard(stringResource(R.string.timing_command_timeout), settings.morseCommandWindowMs) {
                    persist(settings.copy(morseCommandWindowMs = it))
                }
            }
        }
    }

    if (editorOpen) {
        MorseBindingEditorDialog(
            original = editing,
            existing = settings.morseBindings,
            globalVibrationEnabled = settings.vibrationEnabled,
            onDismiss = { editorOpen = false },
            onSave = { binding ->
                val index = settings.morseBindings.indexOfFirst { it.sequence == editing?.sequence }
                val next = settings.morseBindings.toMutableList().apply {
                    if (index >= 0) set(index, binding) else add(binding)
                }
                persist(settings.copy(morseBindings = next))
                editorOpen = false
            }
        )
    }
    deleting?.let { binding ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.dialog_delete_command_title, binding.sequence)) },
            text = { Text(morseActionTitle(binding)) },
            confirmButton = {
                TextButton(onClick = {
                    persist(settings.copy(morseBindings = settings.morseBindings.filterNot { it.sequence == binding.sequence }))
                    deleting = null
                }) { Text(stringResource(R.string.btn_delete)) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.btn_cancel)) } }
        )
    }
}

@Composable
private fun MorseTimingCard(title: String, value: Long, onChange: (Long) -> Unit) {
    val view = LocalView.current
    var sliderValue by remember(value) {
        mutableFloatStateOf(value.coerceIn(100L, 800L).toFloat())
    }
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { clickSound(view); onChange(sliderValue.toLong()) },
                    valueRange = 100f..800f,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                TimeValueEditor(
                    value = sliderValue.toLong(),
                    minValue = 100L,
                    maxValue = 800L,
                    onChange = { newValue ->
                        sliderValue = newValue.toFloat()
                        clickSound(view)
                        onChange(newValue)
                    }
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("100ms", style = MaterialTheme.typography.labelSmall, color = AppMuted)
                Text("800ms", style = MaterialTheme.typography.labelSmall, color = AppMuted)
            }
        }
    }
}

@Composable
private fun morseActionTitle(binding: MorseBinding): String =
    if (binding.action == ActionType.COMMON_FUNCTION) stringResource(binding.custom.commonAction.titleRes) else stringResource(binding.action.titleRes)

@Composable
private fun morseSequenceTitle(sequence: String): String =
    sequence.map {
        when (it) {
            '0' -> stringResource(R.string.morse_key_short_side)
            '1' -> stringResource(R.string.morse_key_long_side)
            'u' -> stringResource(R.string.morse_key_short_up)
            'U' -> stringResource(R.string.morse_key_long_up)
            'd' -> stringResource(R.string.morse_key_short_down)
            'D' -> stringResource(R.string.morse_key_long_down)
            else -> it.toString()
        }
    }.joinToString(" ")

@Composable
private fun MorseBindingEditorDialog(
    original: MorseBinding?,
    existing: List<MorseBinding>,
    globalVibrationEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (MorseBinding) -> Unit
) {
    var sequence by remember(original) { mutableStateOf(original?.sequence.orEmpty()) }
    var action by remember(original) { mutableStateOf(original?.action ?: ActionType.SCREENSHOT) }
    var custom by remember(original) { mutableStateOf(original?.custom ?: CustomActionSettings()) }
    var wakeScreen by remember(original) {
        mutableStateOf(original?.wakeScreen ?: (action == ActionType.CYCLE_RINGER))
    }
    var vibrationEnabled by remember(original) { mutableStateOf(original?.vibrationEnabled ?: false) }
    var showError by remember(original) { mutableStateOf(false) }
    val errorRes = when {
        sequence.isEmpty() -> R.string.morse_input_sequence_prompt
        sequence.first() !in "01" -> R.string.morse_seq_error_start_side
        sequence.any { it !in "01uUdD" } -> R.string.morse_seq_error_chars
        existing.any { it.sequence == sequence && it.sequence != original?.sequence } -> R.string.morse_seq_error_exists
        else -> null
    }
    val selected = ActionChoice(action, if (action == ActionType.COMMON_FUNCTION) custom.commonAction else null)
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.85f
    val view = LocalView.current

    val keyShape = RoundedCornerShape(8.dp)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AppBackground,
            modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight)
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (original == null) stringResource(R.string.dialog_add_command_title) else stringResource(R.string.dialog_edit_command_title),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = sequence,
                    onValueChange = { sequence = it.filter { symbol -> symbol in "01uUdD" } },
                    label = { Text(stringResource(R.string.morse_seq_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    singleLine = true,
                    isError = showError && errorRes != null,
                    supportingText = if (showError && errorRes != null) { { Text(stringResource(errorRes)) } } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Column(modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { clickSound(view); sequence += '0' },
                            modifier = Modifier.weight(1f),
                            shape = keyShape) {
                            Text(stringResource(R.string.morse_key_short_side))
                        }
                        OutlinedButton(onClick = { clickSound(view); sequence += '1' },
                            modifier = Modifier.weight(1f),
                            shape = keyShape) {
                            Text(stringResource(R.string.morse_key_long_side))
                        }
                        IconButton(onClick = { clickSound(view); if (sequence.isNotEmpty()) sequence = sequence.dropLast(1) }) {
                            Icon(Icons.Default.Backspace, contentDescription = stringResource(R.string.morse_key_backspace))
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { clickSound(view); sequence += 'u' },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            shape = keyShape) {
                            Text(stringResource(R.string.morse_key_short_up))
                        }
                        OutlinedButton(onClick = { clickSound(view); sequence += 'U' },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            shape = keyShape) {
                            Text(stringResource(R.string.morse_key_long_up))
                        }
                        OutlinedButton(onClick = { clickSound(view); sequence += 'd' },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            shape = keyShape) {
                            Text(stringResource(R.string.morse_key_short_down))
                        }
                        OutlinedButton(onClick = { clickSound(view); sequence += 'D' },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            shape = keyShape) {
                            Text(stringResource(R.string.morse_key_long_down))
                        }
                    }
                }
                SectionLabel(stringResource(R.string.label_execute_action))
                ActionPicker(selected, morseActionChoices) { choice ->
                    action = choice.action
                    wakeScreen = choice.action == ActionType.CYCLE_RINGER
                    vibrationEnabled = false
                    if (choice.common != null) custom = custom.copy(commonAction = choice.common)
                }
                if (action in listOf(ActionType.XIAOBU_SHORTCUT, ActionType.CUSTOM_ACTIVITY,
                        ActionType.CUSTOM_URL, ActionType.SHELL_COMMAND)) {
                    CustomActionEditor(action, custom) { custom = it }
                }
                SectionLabel(stringResource(R.string.section_options))
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(14.dp)) {
                        SettingSwitchRow(stringResource(R.string.option_wake_screen), null, wakeScreen) { wakeScreen = it }
                        if (!globalVibrationEnabled) {
                            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFFEAF0F7))
                            SettingSwitchRow(
                                stringResource(R.string.option_vibrate_on_trigger),
                                null,
                                vibrationEnabled
                            ) { vibrationEnabled = it }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
                    Button(onClick = {
                        showError = true
                        if (errorRes == null) onSave(MorseBinding(sequence, action, custom, wakeScreen, vibrationEnabled))
                    }) { Text(stringResource(R.string.btn_save)) }
                }
            }
        }
    }
}

@Composable
private fun FeedbackScreen(settings: AppSettings, persist: (AppSettings) -> Unit, back: () -> Unit, padding: PaddingValues) {
    Column(Modifier.fillMaxSize().padding(padding)) {
        BackTitle(stringResource(R.string.setting_vibration_feedback), back)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item { SectionLabel(stringResource(R.string.feedback_vibration_settings)) }
          item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    SettingSwitchRow(stringResource(R.string.option_vibrate_on_trigger), null, settings.vibrationEnabled) {
                        persist(
                            settings.copy(vibrationEnabled = it)
                        )
                    }
                }
            }
          }
          item { SectionLabel(stringResource(R.string.feedback_toast_notifications)) }
          item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingSwitchRow(stringResource(R.string.feedback_show_toast), null, settings.toastEnabled) {
                        persist(
                            settings.copy(toastEnabled = it)
                        )
                    }
                    OutlinedTextField(
                        value = settings.toastText,
                        onValueChange = { persist(settings.copy(toastText = it)) },
                        label = { Text(stringResource(R.string.feedback_toast_message)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
          }
          item { InfoCard(stringResource(R.string.feedback_toast_hint)) }
          item { SectionLabel(stringResource(R.string.feedback_unknown_morse_title)) }
          item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingSwitchRow(stringResource(R.string.feedback_unknown_morse_toast_and_vibe), null, settings.unknownMorseFeedbackEnabled) {
                        persist(settings.copy(unknownMorseFeedbackEnabled = it))
                    }
                    OutlinedTextField(
                        value = settings.unknownMorseToastText,
                        onValueChange = { persist(settings.copy(unknownMorseToastText = it)) },
                        label = { Text(stringResource(R.string.feedback_toast_message)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
          }
        }
    }
}

@Composable
private fun OtherScreen(settings: AppSettings, persist: (AppSettings) -> Unit, padding: PaddingValues) {
    val view = LocalView.current
    Column(Modifier.fillMaxSize().padding(padding)) {
        Text(
            stringResource(R.string.tab_other),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { SectionLabel(stringResource(R.string.section_key_combinations)) }
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingSwitchRow(
                            title = stringResource(R.string.enable_key_combinations),
                            description = null,
                            checked = settings.combinationEnabled
                        ) { persist(settings.copy(combinationEnabled = it)) }
                        if (settings.combinationEnabled) {
                            HorizontalDivider(color = Color(0xFFEAF0F7))
                            SideKeyCombinationPicker(stringResource(R.string.combo_side_vol_up), settings.sideVolumeUpAction) {
                                persist(settings.copy(sideVolumeUpAction = it))
                            }
                            SideKeyCombinationPicker(stringResource(R.string.combo_side_vol_down), settings.sideVolumeDownAction) {
                                persist(settings.copy(sideVolumeDownAction = it))
                            }
                            SideKeyCombinationPicker(stringResource(R.string.combo_side_power), settings.sidePowerAction) {
                                persist(settings.copy(sidePowerAction = it))
                            }
                            HorizontalDivider(color = Color(0xFFEAF0F7))
                            SettingSwitchRow(
                                title = stringResource(R.string.feedback_vibration),
                                description = stringResource(R.string.feedback_vibrate_on_combo),
                                checked = settings.combinationVibrationEnabled
                            ) { persist(settings.copy(combinationVibrationEnabled = it)) }
                        }
                    }
                }
            }
            item {
                InfoCard(stringResource(R.string.combo_hint))
            }
            item { SectionLabel(stringResource(R.string.section_ime_cursor)) }
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.cursor_vol_control), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        val cursorColors = SegmentedButtonDefaults.colors(
                            activeContainerColor = AppBlueSoft,
                            activeContentColor = AppBlueDark,
                            activeBorderColor = AppBlue,
                            inactiveContainerColor = Color.White,
                            inactiveContentColor = AppText,
                            inactiveBorderColor = Color(0xFF9AAFC6)
                        )
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            CursorControlMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = settings.cursorControlMode == mode,
                                    onClick = { clickSound(view); persist(settings.copy(cursorControlMode = mode)) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index, CursorControlMode.entries.size, baseShape = RoundedCornerShape(8.dp)
                                    ),
                                    colors = cursorColors,
                                    modifier = Modifier.weight(1f),
                                    label = {
                                        Text(
                                            stringResource(mode.titleRes),
                                            maxLines = 2,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFEAF0F7))
                        CursorLongPressPicker(settings.cursorLongPressAction) {
                            persist(settings.copy(cursorLongPressAction = it))
                        }
                        if (settings.cursorLongPressAction != CursorLongPressAction.NONE) {
                            HorizontalDivider(color = Color(0xFFEAF0F7))
                            CursorTimingSetting(
                                title = stringResource(R.string.cursor_long_press_delay),
                                value = settings.cursorLongPressMs,
                                minValue = MIN_CURSOR_LONG_PRESS_MS,
                                maxValue = MAX_CURSOR_LONG_PRESS_MS,
                                onChange = { persist(settings.copy(cursorLongPressMs = it)) }
                            )
                            if (settings.cursorLongPressAction == CursorLongPressAction.REPEAT) {
                                HorizontalDivider(color = Color(0xFFEAF0F7))
                                CursorTimingSetting(
                                    title = stringResource(R.string.cursor_repeat_interval),
                                    value = settings.cursorRepeatIntervalMs,
                                    minValue = MIN_CURSOR_REPEAT_INTERVAL_MS,
                                    maxValue = MAX_CURSOR_REPEAT_INTERVAL_MS,
                                    suffix = stringResource(R.string.unit_ms_per_step),
                                    onChange = { persist(settings.copy(cursorRepeatIntervalMs = it)) }
                                )
                            }
                        }
                    }
                }
            }
            item {
                InfoCard(stringResource(R.string.cursor_hint))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SideKeyCombinationPicker(
    title: String,
    selected: SideKeyCombinationAction,
    onSelect: (SideKeyCombinationAction) -> Unit
) {
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { clickSound(view); expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier.fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .background(Color(0xFFF3F7FC), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(selected.titleRes), style = MaterialTheme.typography.bodySmall, color = AppMuted)
                }
                Icon(Icons.Default.ExpandMore, contentDescription = stringResource(R.string.select_key_combo_action), tint = AppMuted)
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(14.dp)
        ) {
            SideKeyCombinationAction.entries.forEach { action ->
                DropdownMenuItem(
                    text = { Text(stringResource(action.titleRes)) },
                    trailingIcon = if (action == selected) {
                        { Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.label_selected), tint = AppBlue) }
                    } else null,
                    onClick = {
                        clickSound(view)
                        expanded = false
                        onSelect(action)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CursorLongPressPicker(
    selected: CursorLongPressAction,
    onSelect: (CursorLongPressAction) -> Unit
) {
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { clickSound(view); expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier.fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .background(Color(0xFFF3F7FC), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.cursor_long_press_action), fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(selected.titleRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.Default.ExpandMore, contentDescription = stringResource(R.string.select_long_press_action), tint = AppMuted)
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(14.dp)
        ) {
            CursorLongPressAction.entries.forEach { action ->
                DropdownMenuItem(
                    text = { Text(stringResource(action.titleRes)) },
                    trailingIcon = if (action == selected) {
                        { Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.label_selected), tint = AppBlue) }
                    } else null,
                    onClick = {
                        clickSound(view)
                        expanded = false
                        onSelect(action)
                    }
                )
            }
        }
    }
}

@Composable
private fun CursorTimingSetting(
    title: String,
    value: Long,
    minValue: Long,
    maxValue: Long,
    suffix: String = "ms",
    onChange: (Long) -> Unit
) {
    val view = LocalView.current
    var sliderValue by remember(value) {
        mutableFloatStateOf(value.coerceIn(minValue, maxValue).toFloat())
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = {
                    clickSound(view)
                    onChange(sliderValue.toLong().coerceIn(minValue, maxValue))
                },
                valueRange = minValue.toFloat()..maxValue.toFloat(),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            TimeValueEditor(
                value = sliderValue.toLong(),
                minValue = minValue,
                maxValue = maxValue,
                suffix = suffix,
                onChange = { newValue ->
                    sliderValue = newValue.toFloat()
                    onChange(newValue)
                }
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${minValue}ms", style = MaterialTheme.typography.labelSmall, color = AppMuted)
            Text("${maxValue}ms", style = MaterialTheme.typography.labelSmall, color = AppMuted)
        }
    }
}

@Composable
private fun AdvancedScreen(settings: AppSettings, persist: (AppSettings) -> Unit, back: () -> Unit, padding: PaddingValues) {
    Column(Modifier.fillMaxSize().padding(padding)) {
        BackTitle(stringResource(R.string.setting_advanced), back)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item { SectionLabel(stringResource(R.string.advanced_system_behavior)) }
          item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    SettingSwitchRow(
                        stringResource(R.string.advanced_prefer_su),
                        stringResource(R.string.advanced_prefer_su_desc),
                        settings.preferSuForShellCommands
                    ) { persist(settings.copy(preferSuForShellCommands = it)) }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFFEAF0F7))
                    SettingSwitchRow(
                        stringResource(R.string.advanced_wake_screen),
                        stringResource(R.string.advanced_wake_screen_desc),
                        settings.wakeScreenWhenOff
                    ) {
                        persist(
                            settings.copy(wakeScreenWhenOff = it)
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFFEAF0F7))
                    Text(stringResource(R.string.advanced_key_event_source), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text("Oplus StrategyActionButtonKeyLaunchApp", style = MaterialTheme.typography.bodySmall, color = AppMuted)
                }
            }
          }
          item { SectionLabel(stringResource(R.string.advanced_input_device)) }
          item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = settings.inputDevicePath,
                        onValueChange = { persist(settings.copy(inputDevicePath = it)) },
                        label = { Text(stringResource(R.string.advanced_device_path)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    NumberField(settings.keyCode.toString(), stringResource(R.string.advanced_key_code)) {
                        it.toIntOrNull()?.let { code -> persist(settings.copy(keyCode = code)) }
                    }
                }
            }
          }
          item { InfoCard(stringResource(R.string.advanced_device_hint)) }
        }
    }
}

@Composable
private fun AboutScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val view = LocalView.current
    Column(Modifier.fillMaxSize().padding(padding)) {
        Text(stringResource(R.string.tab_about), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
          item {
            Spacer(Modifier.height(34.dp))
            IconBadge(Icons.Default.Tune, size = 76.dp, iconSize = 38.dp)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.about_app_name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.about_app_slogan), color = AppMuted, modifier = Modifier.padding(top = 6.dp))
            Text("com.slimenull.customsidebuttonfunctions", style = MaterialTheme.typography.bodySmall, color = AppMuted, modifier = Modifier.padding(top = 8.dp))
            Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodySmall, color = AppMuted)
          }
           item {
             Spacer(Modifier.height(22.dp))
             Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                     Text(stringResource(R.string.about_developer), style = MaterialTheme.typography.labelMedium, color = AppMuted)
                    Text("SlimeNull Issac", modifier = Modifier.padding(top = 4.dp))
                 }
             }
           }
           item {
             Row(
                 Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.spacedBy(10.dp)
             ) {
                 OutlinedButton(
                     onClick = {
                         clickSound(view)
                         openExternalUrl(context, "https://www.gnu.org/licenses/lgpl-3.0.html")
                     },
                     modifier = Modifier.weight(1f)
                 ) {
                     Icon(Icons.Default.Code, contentDescription = null)
                     Spacer(Modifier.width(6.dp))
                     Text(stringResource(R.string.about_license))
                 }
                 OutlinedButton(
                     onClick = {
                         clickSound(view)
                         openExternalUrl(context, "https://github.com/SlimeNull/OppoCustomSideButtonFunctions")
                     },
                     modifier = Modifier.weight(1f)
                 ) {
                     Icon(Icons.Default.OpenInNew, contentDescription = null)
                     Spacer(Modifier.width(6.dp))
                     Text(stringResource(R.string.about_repository))
                 }
             }
           }
           item {
             Text(
                 "GNU Lesser General Public License v3.0",
                 style = MaterialTheme.typography.bodySmall,
                 color = AppMuted,
                 modifier = Modifier.fillMaxWidth(),
                 textAlign = androidx.compose.ui.text.style.TextAlign.Center
             )
           }
        }
    }
}

private fun openExternalUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addCategory(Intent.CATEGORY_BROWSABLE)
        )
    }.onXposedFailure("open external URL")
}

@Composable
private fun BackTitle(title: String, back: () -> Unit) {
    val view = LocalView.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { clickSound(view); back() }) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back), tint = AppText) }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val view = LocalView.current
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().clip(shape).clickable {
            clickSound(view)
            onClick()
        }
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AppMuted)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AppMuted, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun IntroCard(icon: ImageVector, title: String, description: String) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, size = 48.dp, iconSize = 26.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = AppMuted, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionPicker(
    selected: ActionChoice,
    choices: List<ActionChoice> = actionChoices,
    onSelect: (ActionChoice) -> Unit
) {
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { clickSound(view); expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(selected.icon, size = 34.dp, iconSize = 19.dp)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(selected.titleRes), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ExpandMore, contentDescription = stringResource(R.string.cd_select_action), tint = AppMuted)
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(14.dp)
        ) {
            choices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(stringResource(choice.titleRes)) },
                    leadingIcon = { Icon(choice.icon, contentDescription = null) },
                    trailingIcon = if (choice == selected) {
                        { Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.cd_selected), tint = AppBlue) }
                    } else null,
                    onClick = {
                        clickSound(view)
                        expanded = false
                        onSelect(choice)
                    }
                )
            }
        }
    }
}

@Composable
private fun ActivityPresetPicker(onSelect: (ActivityPreset) -> Unit) {
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { clickSound(view); expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.load_preset), modifier = Modifier.weight(1f))
            Icon(Icons.Default.ExpandMore, contentDescription = stringResource(R.string.cd_select_preset))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(14.dp)
        ) {
            activityPresets.forEach { preset ->
                DropdownMenuItem(
                    text = { Text(stringResource(preset.titleRes)) },
                    onClick = {
                        clickSound(view)
                        expanded = false
                        onSelect(preset)
                    }
                )
            }
        }
    }
}

private fun loadInstalledApps(context: Context): List<InstalledApp> {
    val packageManager = context.packageManager
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return packageManager.queryIntentActivities(launcherIntent, 0)
        .asSequence()
        .mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
            val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim().orEmpty()
            InstalledApp(packageName, label.ifBlank { packageName })
        }
        .distinctBy { it.packageName }
        .sortedWith(
            compareBy<InstalledApp, String>(String.CASE_INSENSITIVE_ORDER) { it.label }
                .thenBy { it.packageName }
        )
        .toList()
}

@Composable
private fun ActivityPackagePicker(
    currentPackage: String,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var dialogVisible by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var availableApps by remember { mutableStateOf<List<InstalledApp>?>(null) }
    val filteredApps = remember(availableApps, query) {
        val apps = availableApps.orEmpty()
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) apps else apps.filter {
            it.label.contains(normalizedQuery, ignoreCase = true) ||
                it.packageName.contains(normalizedQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(dialogVisible) {
        if (dialogVisible && availableApps == null) {
            availableApps = withContext(Dispatchers.Default) {
                loadInstalledApps(context)
            }
        }
    }

    OutlinedTextField(
        value = currentPackage,
        onValueChange = onSelect,
        label = { Text(stringResource(R.string.field_package_name)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(
                onClick = {
                    clickSound(view)
                    query = ""
                    availableApps = null
                    dialogVisible = true
                }
            ) {
                Icon(Icons.Default.Apps, contentDescription = stringResource(R.string.cd_select_app), tint = AppBlue)
            }
        }
    )

    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = { dialogVisible = false },
            title = { Text(stringResource(R.string.title_select_app)) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.search_apps)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    Spacer(Modifier.height(8.dp))
                    if (availableApps == null) {
                        Text(stringResource(R.string.loading_apps), color = AppMuted, modifier = Modifier.padding(16.dp))
                    } else if (filteredApps.isEmpty()) {
                        Text(stringResource(R.string.no_matching_apps), color = AppMuted, modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp)
                        ) {
                            items(filteredApps, key = { it.packageName }) { app ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            clickSound(view)
                                            onSelect(app.packageName)
                                            dialogVisible = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        app.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AppText
                                    )
                                    Text(
                                        app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppText
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogVisible = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }
}

@Composable
private fun CustomActionEditor(
    action: ActionType,
    custom: CustomActionSettings,
    onChange: (CustomActionSettings) -> Unit
) {
    when (action) {
        ActionType.COMMON_FUNCTION -> {
            ParameterCard(stringResource(R.string.title_common_actions)) {
                Text(stringResource(R.string.selected_with_title, stringResource(custom.commonAction.titleRes)), color = AppMuted)
            }
        }
        ActionType.XIAOBU_SHORTCUT -> {
            ParameterCard(stringResource(R.string.title_xiaobu_shortcut)) {
                OutlinedTextField(
                    value = custom.xiaobuShortcutId,
                    onValueChange = { onChange(custom.copy(xiaobuShortcutId = it)) },
                    label = { Text(stringResource(R.string.field_shortcut_id)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                SettingSwitchRow(
                    title = stringResource(R.string.show_execution_toast),
                    description = null,
                    checked = custom.shellToastEnabled
                ) { onChange(custom.copy(shellToastEnabled = it)) }
            }
        }
        ActionType.CUSTOM_ACTIVITY -> {
            ParameterCard(stringResource(R.string.title_activity_parameters)) {
                ActivityPackagePicker(
                    currentPackage = custom.activityPackage,
                    onSelect = { onChange(custom.copy(activityPackage = it)) }
                )
                OutlinedTextField(custom.activityClass, { onChange(custom.copy(activityClass = it)) }, label = { Text(stringResource(R.string.field_activity_class)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(custom.activityAction, { onChange(custom.copy(activityAction = it)) }, label = { Text(stringResource(R.string.field_intent_action)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                SettingSwitchRow(
                    title = stringResource(R.string.launch_in_small_window),
                    description = null,
                    checked = custom.launchInSmallWindow
                ) { onChange(custom.copy(launchInSmallWindow = it)) }
                ActivityPresetPicker { preset ->
                    onChange(
                        custom.copy(
                            activityPackage = preset.packageName,
                            activityClass = preset.className,
                            activityAction = preset.action,
                            launchInSmallWindow = preset.launchInSmallWindow
                        )
                    )
                }
            }
        }
        ActionType.CUSTOM_URL -> {
            ParameterCard(stringResource(R.string.title_url_scheme_parameters)) {
                OutlinedTextField(
                    value = custom.urlScheme,
                    onValueChange = { onChange(custom.copy(urlScheme = it)) },
                    label = { Text(stringResource(R.string.field_url_scheme)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        ActionType.SHELL_COMMAND -> {
            ParameterCard(stringResource(R.string.title_shell_parameters)) {
                OutlinedTextField(
                    value = custom.shellCommand,
                    onValueChange = { onChange(custom.copy(shellCommand = it)) },
                    label = { Text(stringResource(R.string.field_shell_command)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                SettingSwitchRow(
                    title = stringResource(R.string.show_execution_toast),
                    description = null,
                    checked = custom.shellToastEnabled
                ) { onChange(custom.copy(shellToastEnabled = it)) }
            }
        }
        else -> Unit
    }
}

@Composable
private fun ParameterCard(title: String, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val view = LocalView.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title)
            if (!description.isNullOrBlank()) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted)
            }
        }
        Switch(checked, { clickSound(view); onCheckedChange(it) })
    }
}

@Composable
private fun IconBadge(icon: ImageVector, size: androidx.compose.ui.unit.Dp = 40.dp, iconSize: androidx.compose.ui.unit.Dp = 21.dp) {
    Box(Modifier.size(size).background(AppBlueSoft, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = AppBlueDark, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp, bottom = 2.dp))
}

@Composable
private fun InfoCard(text: String) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FF))) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = AppBlue)
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = AppBlueDark)
        }
    }
}

@Composable
private fun TimeValueEditor(
    value: Long,
    minValue: Long,
    maxValue: Long,
    suffix: String = "ms",
    onChange: (Long) -> Unit
) {
    var text by remember(value) { mutableStateOf(value.coerceIn(minValue, maxValue).toString()) }
    val focusManager = LocalFocusManager.current

    fun commit() {
        val committed = (text.toLongOrNull() ?: value).coerceIn(minValue, maxValue)
        text = committed.toString()
        if (committed != value) onChange(committed)
    }

    BasicTextField(
        value = text,
        onValueChange = { input ->
            text = input.filter(Char::isDigit).take(6)
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.labelMedium.copy(color = AppText),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            commit()
            focusManager.clearFocus()
        }),
        modifier = Modifier
            .width(54.dp)
            .height(32.dp)
            .onFocusChanged { state -> if (!state.isFocused) commit() },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF3F7FC), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFD6E1EC), RoundedCornerShape(8.dp))
                    .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) { innerTextField() }
                Text(suffix, style = MaterialTheme.typography.labelSmall, color = AppMuted)
            }
        }
    )
}

@Composable
private fun NumberField(value: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun clickSound(view: View) {
    view.playSoundEffect(SoundEffectConstants.CLICK)
}

private fun selectedAction(kind: GestureKind, settings: AppSettings): ActionType = when (kind) {
    GestureKind.SINGLE -> settings.singleAction
    GestureKind.DOUBLE -> settings.doubleAction
    GestureKind.LONG -> settings.longAction
}

private fun selectedCustom(kind: GestureKind, settings: AppSettings): CustomActionSettings = when (kind) {
    GestureKind.SINGLE -> settings.singleCustom
    GestureKind.DOUBLE -> settings.doubleCustom
    GestureKind.LONG -> settings.longCustom
}

private fun selectedWakeScreen(kind: GestureKind, settings: AppSettings): Boolean = when (kind) {
    GestureKind.SINGLE -> settings.singleWakeScreen
    GestureKind.DOUBLE -> settings.doubleWakeScreen
    GestureKind.LONG -> settings.longWakeScreen
}

private fun selectedVibration(kind: GestureKind, settings: AppSettings): Boolean = when (kind) {
    GestureKind.SINGLE -> settings.singleVibrationEnabled
    GestureKind.DOUBLE -> settings.doubleVibrationEnabled
    GestureKind.LONG -> settings.longVibrationEnabled
}

private fun updateAction(kind: GestureKind, settings: AppSettings, action: ActionType): AppSettings = when (kind) {
    GestureKind.SINGLE -> settings.copy(singleAction = action, singleWakeScreen = action == ActionType.CYCLE_RINGER, singleVibrationEnabled = false)
    GestureKind.DOUBLE -> settings.copy(doubleAction = action, doubleWakeScreen = action == ActionType.CYCLE_RINGER, doubleVibrationEnabled = false)
    GestureKind.LONG -> settings.copy(longAction = action, longWakeScreen = action == ActionType.CYCLE_RINGER, longVibrationEnabled = false)
}

private fun updateWakeScreen(kind: GestureKind, settings: AppSettings, enabled: Boolean): AppSettings = when (kind) {
    GestureKind.SINGLE -> settings.copy(singleWakeScreen = enabled)
    GestureKind.DOUBLE -> settings.copy(doubleWakeScreen = enabled)
    GestureKind.LONG -> settings.copy(longWakeScreen = enabled)
}

private fun updateVibration(kind: GestureKind, settings: AppSettings, enabled: Boolean): AppSettings = when (kind) {
    GestureKind.SINGLE -> settings.copy(singleVibrationEnabled = enabled)
    GestureKind.DOUBLE -> settings.copy(doubleVibrationEnabled = enabled)
    GestureKind.LONG -> settings.copy(longVibrationEnabled = enabled)
}

private fun updateCustom(kind: GestureKind, settings: AppSettings, custom: CustomActionSettings): AppSettings = when (kind) {
    GestureKind.SINGLE -> settings.copy(singleCustom = custom)
    GestureKind.DOUBLE -> settings.copy(doubleCustom = custom)
    GestureKind.LONG -> settings.copy(longCustom = custom)
}

private fun gestureIcon(kind: GestureKind): ImageVector = when (kind) {
    GestureKind.SINGLE -> Icons.Default.Vibration
    GestureKind.DOUBLE -> Icons.Default.Vibration
    GestureKind.LONG -> Icons.Default.AccessTime
}

private fun actionIcon(action: ActionType): ImageVector = when (action) {
    ActionType.NONE -> Icons.Default.RemoveCircleOutline
    ActionType.SHOW_RINGER -> Icons.Default.VolumeUp
    ActionType.CYCLE_RINGER -> Icons.Default.VolumeUp
    ActionType.MEDIA_PREVIOUS -> Icons.Default.ArrowBack
    ActionType.MEDIA_NEXT -> Icons.Default.ArrowForward
    ActionType.MEDIA_PLAY_PAUSE -> Icons.Default.VolumeUp
    ActionType.TOGGLE_DND -> Icons.Default.NotificationsOff
    ActionType.CAMERA -> Icons.Default.CameraAlt
    ActionType.FLASHLIGHT -> Icons.Default.FlashOn
    ActionType.RECORDING -> Icons.Default.Mic
    ActionType.SCREENSHOT -> Icons.Default.CropFree
    ActionType.COMMON_FUNCTION -> Icons.Default.Tune
    ActionType.XIAOBU_SHORTCUT -> Icons.Default.Tune
    ActionType.CUSTOM_ACTIVITY -> Icons.Default.Code
    ActionType.CUSTOM_URL -> Icons.Default.OpenInNew
    ActionType.SHELL_COMMAND -> Icons.Default.Code
}
