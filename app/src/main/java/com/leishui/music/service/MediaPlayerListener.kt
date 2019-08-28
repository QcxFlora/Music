package com.leishui.music.service

interface MediaPlayerListener {
    fun onDurationChanged(duration: Int)

    fun onProgressChanged(progress: Int)

    fun onStateChanged(state: Boolean)

    fun onInfoChanged(position: Int)
}