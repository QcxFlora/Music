package com.leishui.music.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.leishui.music.Model
import com.leishui.music.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btn_login_tel.setOnClickListener { startActivity(Intent(this,LoginWithTelActivity::class.java)) }
    }
}
