package com.leishui.music.view

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.animation.LinearInterpolator

class MusicView : RoundImageView {

    private var objectAnimator: ObjectAnimator? = null
    var state: Int = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        objectAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f)//添加旋转动画，旋转中心默认为控件中点
        objectAnimator!!.duration = 20000//设置动画时间
        objectAnimator!!.interpolator = LinearInterpolator()//动画时间线性渐变
        objectAnimator!!.repeatCount = ObjectAnimator.INFINITE
        objectAnimator!!.repeatMode = ObjectAnimator.RESTART
    }

    fun startMusic(){
        objectAnimator!!.start()
        state = STATE_PLAYING
    }

    fun resumeMusic() {
        if (state == STATE_PAUSE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                objectAnimator!!.resume()
                state = STATE_PLAYING
            }
        }
    }

    fun pauseMusic() {
        if (state == STATE_PLAYING)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                objectAnimator!!.pause()
                state = STATE_PAUSE
            }
    }

    companion object {
        val STATE_PLAYING = 1//正在播放
        val STATE_PAUSE = 2//暂停
    }

}
