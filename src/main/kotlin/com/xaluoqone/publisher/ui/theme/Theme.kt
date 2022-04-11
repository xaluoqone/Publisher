package com.xaluoqone.publisher.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

private val LocalThemeColors = compositionLocalOf {
    TealColorPalette
}

@Stable
object AppTheme {
    val colors: ThemeColors
        @Composable
        get() = LocalThemeColors.current

    sealed interface Theme {
        object Teal : Theme
        object Cyan : Theme
        object BlueGray : Theme
    }
}

@Stable
class ThemeColors(
    primary: Color,
    primaryVariant: Color,
    secondary: Color,
    topBarTitle: Color,
    background: Color
) {
    var primary by mutableStateOf(primary)
        private set
    var primaryVariant by mutableStateOf(primaryVariant)
        private set
    var secondary by mutableStateOf(secondary)
        private set
    var topBarTitle by mutableStateOf(topBarTitle)
        private set
    var background by mutableStateOf(background)
        private set
}

private val TealColorPalette = ThemeColors(
    primary = Teal500,
    primaryVariant = Teal700,
    secondary = Teal200,
    topBarTitle = White,
    background = LightGrey
)

private val CyanColorPalette = ThemeColors(
    primary = Cyan500,
    primaryVariant = Cyan700,
    secondary = Cyan200,
    topBarTitle = White,
    background = LightGrey
)

private val BlueGrayColorPalette = ThemeColors(
    primary = BlueGray500,
    primaryVariant = BlueGray700,
    secondary = BlueGray200,
    topBarTitle = White,
    background = LightGrey
)

@Composable
fun AppTheme(theme: AppTheme.Theme, content: @Composable () -> Unit) {
    val targetColors = when (theme) {
        AppTheme.Theme.BlueGray -> BlueGrayColorPalette
        AppTheme.Theme.Cyan -> CyanColorPalette
        AppTheme.Theme.Teal -> TealColorPalette
    }
    val primary = animateColorAsState(targetColors.primary, TweenSpec(600))
    val primaryVariant = animateColorAsState(targetColors.primaryVariant, TweenSpec(600))
    val secondary = animateColorAsState(targetColors.secondary, TweenSpec(600))
    val topBarTitle = animateColorAsState(targetColors.topBarTitle, TweenSpec(600))
    val background = animateColorAsState(targetColors.background, TweenSpec(600))

    val colors = ThemeColors(
        primary = primary.value,
        primaryVariant = primaryVariant.value,
        secondary = secondary.value,
        topBarTitle = topBarTitle.value,
        background = background.value
    )

    CompositionLocalProvider(LocalThemeColors provides colors, content = content)
}