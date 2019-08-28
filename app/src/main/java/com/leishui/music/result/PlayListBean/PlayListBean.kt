package com.leishui.music.result.PlayListBean

data class PlayListBean(
    val code: Int,
    val more: Boolean,
    val playlist: List<Playlist>
)