package com.leishui.music.result.SongBean

data class SongBean(
    val code: Int,
    val playlist: Playlist,
    val privileges: List<Privilege>,
    val relatedVideos: Any,
    val urls: Any
)