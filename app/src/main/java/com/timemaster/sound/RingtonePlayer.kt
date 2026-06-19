package com.timemaster.sound

import android.content.Context
import android.media.MediaPlayer

class RingtonePlayer(
    context: Context
) {
    private val appContext = context.applicationContext
    private var player: MediaPlayer? = null

    @Synchronized
    fun playLooping(ringtoneId: String) {
        start(ringtoneId = ringtoneId, loop = true)
    }

    @Synchronized
    fun preview(ringtoneId: String) {
        start(ringtoneId = ringtoneId, loop = false)
    }

    @Synchronized
    fun stop() {
        player?.release()
        player = null
    }

    private fun start(ringtoneId: String, loop: Boolean) {
        stop()
        val option = RingtoneCatalog.byId(ringtoneId)
        val nextPlayer = MediaPlayer.create(appContext, option.rawRes) ?: return
        nextPlayer.isLooping = loop
        if (!loop) {
            nextPlayer.setOnCompletionListener { completedPlayer ->
                synchronized(this) {
                    if (player === completedPlayer) {
                        player = null
                    }
                    completedPlayer.release()
                }
            }
        }
        player = nextPlayer
        nextPlayer.start()
    }
}
