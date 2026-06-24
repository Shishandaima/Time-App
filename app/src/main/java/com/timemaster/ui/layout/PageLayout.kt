package com.timemaster.ui.layout

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal val PageContentHorizontalPadding = 20.dp
internal val PageContentTopPadding = 4.dp
internal val PageContentBottomPadding = 20.dp

fun Modifier.pageContentPadding(): Modifier = padding(
    start = PageContentHorizontalPadding,
    top = PageContentTopPadding,
    end = PageContentHorizontalPadding,
    bottom = PageContentBottomPadding
)
