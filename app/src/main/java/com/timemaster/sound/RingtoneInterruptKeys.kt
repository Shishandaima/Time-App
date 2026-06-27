package com.timemaster.sound

import android.view.KeyEvent

fun isRingtoneInterruptKey(keyCode: Int): Boolean =
    keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
        keyCode == KeyEvent.KEYCODE_VOLUME_DOWN

fun shouldHandleRingtoneInterruptKey(keyCode: Int, isRinging: Boolean): Boolean =
    isRinging && isRingtoneInterruptKey(keyCode)
