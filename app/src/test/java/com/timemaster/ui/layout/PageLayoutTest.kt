package com.timemaster.ui.layout

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class PageLayoutTest {
    @Test
    fun pageContentInsetsKeepHorizontalAndBottomSpacingWhileRaisingTop() {
        assertEquals(20.dp, PageContentHorizontalPadding)
        assertEquals(8.dp, PageContentTopPadding)
        assertEquals(20.dp, PageContentBottomPadding)
    }
}
