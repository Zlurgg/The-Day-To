package com.jbrightman.thedayto.feature_thedayto.presentation.util

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalDim = compositionLocalOf { Dimensions() }

data class Dimensions(
    val default: Dp = 0.dp,
    val paddingXXSmall: Dp = 2.dp,
    val paddingExtraSmall: Dp = 4.dp,
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 32.dp,
    val paddingVeryLarge: Dp = 48.dp,
    val paddingExtraLarge: Dp = 64.dp,
    val paddingXXLarge: Dp = 128.dp,
    val paddingXXXLarge: Dp = 256.dp
)