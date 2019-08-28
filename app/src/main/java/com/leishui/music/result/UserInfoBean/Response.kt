package com.leishui.music.result.UserInfoBean

class Response(val callBack: CallBack){
    interface CallBack{
        fun onSuccess()
        fun onFail()
    }

}