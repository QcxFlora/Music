package com.leishui.music.result

data class CheckPhoneBean(
    val code: Int,
    val exist: Int,
    val hasPassword: Boolean,
    val nickname: Any
)