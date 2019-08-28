package com.leishui.music.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.leishui.music.Model
import com.leishui.music.R
import kotlin.concurrent.thread

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        thread(start = true) {
            Thread.sleep(3000)
            if (Model.getIsLoginByShared(this))
                startActivity(Intent(this,MainActivity::class.java))
            else
                startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }
}
