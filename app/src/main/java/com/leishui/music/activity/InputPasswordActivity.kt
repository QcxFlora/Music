package com.leishui.music.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.leishui.music.Model
import com.leishui.music.R
import com.leishui.music.result.ErroBean
import com.leishui.music.result.UserInfoBean.UserInfoBean
import kotlinx.android.synthetic.main.activity_input_password.*
import retrofit2.Response

class InputPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_password)
        val phone = intent.getStringExtra("phone")
        setSupportActionBar(toolbar_inputpassword)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        btn_login.setOnClickListener {
            val password = edit_password.text.toString()
            Model.loginByPhone(phone!!, password, object : Model.CallBack<UserInfoBean> {
                override fun onSuccess(response: Response<UserInfoBean>) {
                    if (response.body() == null) {
                        val gson = Gson()
                        val erro = gson.fromJson<ErroBean>(response.errorBody()?.string(), ErroBean::class.java)
                        Toast.makeText(this@InputPasswordActivity, erro.message, Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(this@InputPasswordActivity, MainActivity::class.java)
                        Model.saveUserInfoByShared(this@InputPasswordActivity, response.body()!!)
                        Model.saveIsLoginByShared(this@InputPasswordActivity,true)
                        startActivity(intent)
                        ActivityCompat.finishAffinity(this@InputPasswordActivity)
                    }
                }

                override fun onFailed(t: Throwable) {
                    Toast.makeText(this@InputPasswordActivity, t.toString(), Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
