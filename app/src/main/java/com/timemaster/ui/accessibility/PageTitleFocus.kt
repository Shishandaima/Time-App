package com.timemaster.ui.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

@Composable
fun Modifier.pageEntryTitleFocus(enabled: Boolean = true): Modifier {
    val focusRequester = remember { FocusRequester() }

    if (enabled) {
        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
        }
    }

    val headingModifier = this.semantics { heading() }
    return if (enabled) {
        headingModifier
            .focusRequester(focusRequester)
            .focusable()
    } else {
        headingModifier
    }
}

@Composable
fun Modifier.requestFocusOnEntry(
    focusKey: Any?,
    onFocusRequested: () -> Unit = {}
): Modifier {
    val focusRequester = remember { FocusRequester() }

    if (focusKey != null) {
        LaunchedEffect(focusKey) {
            focusRequester.requestFocus()
            onFocusRequested()
        }
    }

    return if (focusKey == null) {
        this
    } else {
        this.focusRequester(focusRequester)
    }
}
