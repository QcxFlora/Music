package com.leishui.music.result.UserInfoBean

data class UserInfoBean(
    val account: Account,
    val bindings: List<Binding>,
    val code: Int,
    val loginType: Int,
    val profile: Profile,
    val message: String
)
