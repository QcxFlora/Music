package com.leishui.music

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.leishui.music.result.CheckPhoneBean
import com.leishui.music.result.PlayListBean.PlayListBean
import com.leishui.music.result.PlayListBean.Playlist
import com.leishui.music.result.SongBean.SongBean
import com.leishui.music.result.SongBean.Track
import com.leishui.music.result.SongUrlBean.SongUrlBean
import com.leishui.music.result.UserInfoBean.UserInfoBean
import com.leishui.music.util.ApiOfClouldMusic
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Model {

    interface CallBack<T> {
        fun onSuccess(response: Response<T>)
        fun onFailed(t: Throwable)
    }

    private val retrofit = Retrofit.Builder().baseUrl("http://39.100.233.149:3000").addConverterFactory(
        GsonConverterFactory.create()
    )
        .build()
    private val api = retrofit.create(ApiOfClouldMusic::class.java)

    //网络请求
    fun loginByPhone(phone: String, password: String, callback: CallBack<UserInfoBean>) {
        api.login(phone, password).enqueue(object : Callback<UserInfoBean> {
            override fun onFailure(call: Call<UserInfoBean>, t: Throwable) {
                callback.onFailed(t)
            }

            override fun onResponse(call: Call<UserInfoBean>, response: Response<UserInfoBean>) {
                callback.onSuccess(response)
            }
        })
    }

    fun playList(uid: String, callback: CallBack<PlayListBean>) {
        api.playlist(uid).enqueue(object : Callback<PlayListBean> {
            override fun onFailure(call: Call<PlayListBean>, t: Throwable) {
                callback.onFailed(t)
            }

            override fun onResponse(call: Call<PlayListBean>, response: Response<PlayListBean>) {
                callback.onSuccess(response)
            }
        })
    }

    fun checkPhone(phone: String, callback: CallBack<CheckPhoneBean>) {
        api.checkPhone(phone).enqueue(object : Callback<CheckPhoneBean> {
            override fun onFailure(call: Call<CheckPhoneBean>, t: Throwable) {
                callback.onFailed(t)
            }

            override fun onResponse(call: Call<CheckPhoneBean>, response: Response<CheckPhoneBean>) {
                callback.onSuccess(response)
            }
        })
    }

    fun logout(callback: CallBack<CheckPhoneBean>) {
        api.logout().enqueue(object : Callback<CheckPhoneBean> {
            override fun onFailure(call: Call<CheckPhoneBean>, t: Throwable) {
                callback.onFailed(t)
            }

            override fun onResponse(call: Call<CheckPhoneBean>, response: Response<CheckPhoneBean>) {
                callback.onSuccess(response)
            }

        })
    }

    fun playlistSong(id: String, callback: CallBack<SongBean>) {
        api.playlistSong(id).enqueue(object : Callback<SongBean> {
            override fun onFailure(call: Call<SongBean>, t: Throwable) {
                callback.onFailed(t)
            }

            override fun onResponse(call: Call<SongBean>, response: Response<SongBean>) {
                callback.onSuccess(response)
            }

        })
    }

    fun songUrl(id: String, callback: CallBack<SongUrlBean>) {
        api.songUrl(id).enqueue(object : Callback<SongUrlBean> {
            override fun onFailure(call: Call<SongUrlBean>, t: Throwable) {
                callback.onFailed(t)
            }

            override fun onResponse(call: Call<SongUrlBean>, response: Response<SongUrlBean>) {
                callback.onSuccess(response)
            }

        })
    }



















    //SharedPreference相关操作
    fun saveUserInfoByShared(context: Context, userInfoBean: UserInfoBean) {
        val editor = context.getSharedPreferences("userInfo", MODE_PRIVATE).edit()
        editor.putString("imageUrl", userInfoBean.profile.avatarUrl)
        editor.putString("nickname", userInfoBean.profile.nickname)
        editor.putString("uid", userInfoBean.profile.userId.toString())
        editor.apply()
    }

    fun getUserInfoByShared(context: Context, type: String): String {
        val sharedPreferences = context.getSharedPreferences("userInfo", MODE_PRIVATE)
        return sharedPreferences.getString(type, "")!!
    }

    fun saveIsLoginByShared(context: Context, isLogin: Boolean) {
        val editor = context.getSharedPreferences("isLogin", MODE_PRIVATE).edit()
        editor.putBoolean("isLogin", isLogin)
        editor.apply()
    }

    fun getIsLoginByShared(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("isLogin", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLogin", false)
    }

    fun savePlayListByShared(context: Context, list: List<Playlist>) {
        val gson = Gson()
        val string = gson.toJson(list)
        val editor = context.getSharedPreferences("playList", MODE_PRIVATE).edit()
        editor.putString("playList", string)
        editor.apply()
    }

    fun getPlayListByShared(context: Context): List<Playlist> {
        val sharedPreferences = context.getSharedPreferences("playList", MODE_PRIVATE)
        val gson = Gson()
        val string = sharedPreferences.getString("playList", null)
        return if (string == null)
            emptyList()
        else
            gson.fromJson(string, object : TypeToken<List<Playlist>>() {}.type)
    }

    fun saveMusicListByShared(context: Context, list: List<Track>, listId: String) {
        val gson = Gson()
        val string = gson.toJson(list)
        val editor = context.getSharedPreferences("musicList", MODE_PRIVATE).edit()
        editor.putString(listId, string)
        editor.apply()
    }

    fun getMusicListByShared(context: Context, listId: String): List<Track>? {
        val sharedPreferences = context.getSharedPreferences("musicList", MODE_PRIVATE)
        val gson = Gson()
        val string = sharedPreferences.getString(listId, null)
        return if (string == null)
            null
        else
            gson.fromJson(string, object : TypeToken<List<Track>>() {}.type)
    }

    fun saveCurrentListIdByShared(context: Context, listId:String) {
        val editor = context.getSharedPreferences("currentListId", MODE_PRIVATE).edit()
        editor.putString("currentListId", listId)
        editor.apply()
    }

    fun getCurrentListIdByShared(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("currentListId", MODE_PRIVATE)
        return sharedPreferences.getString("currentListId","")
    }

    fun saveStringByShared(context: Context, key:String, value:String){
        val editor = context.getSharedPreferences("stringData", MODE_PRIVATE).edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringByShared(context: Context,key: String): String? {
        val sharedPreferences = context.getSharedPreferences("stringData", MODE_PRIVATE)
        return sharedPreferences.getString(key,"-1")
    }

    fun saveBooleanByShared(context: Context, key: String,boolean: Boolean) {
        val editor = context.getSharedPreferences("boolData", MODE_PRIVATE).edit()
        editor.putBoolean(key, boolean)
        editor.apply()
    }

    fun getBooleanByShared(context: Context,key: String): Boolean {
        val sharedPreferences = context.getSharedPreferences("boolData", MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, true)
    }

    fun getCurrentTrackByShared(context: Context,id: String,position:String): Track? {
        val sharedPreferences = context.getSharedPreferences("musicList", MODE_PRIVATE)
        val gson = Gson()
        val string = sharedPreferences.getString(id, null)
        return if (string == null)
            null
        else{
            val list:List<Track> = gson.fromJson(string, object : TypeToken<List<Track>>() {}.type)
            list[position.toInt()]
        }
    }

}