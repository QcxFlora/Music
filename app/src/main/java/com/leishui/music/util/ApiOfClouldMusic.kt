package com.leishui.music.util

import com.leishui.music.result.CheckPhoneBean
import com.leishui.music.result.SongUrlBean.SongUrlBean
import com.leishui.music.result.PlayListBean.PlayListBean
import com.leishui.music.result.SongBean.SongBean
import com.leishui.music.result.UserInfoBean.UserInfoBean
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiOfClouldMusic{
    @GET("/login/cellphone")
    fun login(@Query("phone") phone:String,@Query("password")passwor:String):Call<UserInfoBean>

    @GET("/user/playlist")
    fun playlist(@Query("uid") uid:String):Call<PlayListBean>

    @GET("/cellphone/existence/check")
    fun checkPhone(@Query("phone") phone: String):Call<CheckPhoneBean>

    @GET("/logout")
    fun logout():Call<CheckPhoneBean>

    @GET("/playlist/detail")
    fun playlistSong(@Query("id") id:String):Call<SongBean>

    @GET("/song/url")
    fun songUrl(@Query("id") id:String):Call<SongUrlBean>
}