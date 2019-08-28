package com.leishui.music.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.leishui.music.Model
import com.leishui.music.R
import com.leishui.music.result.CheckPhoneBean
import com.leishui.music.result.UserInfoBean.UserInfoBean
import kotlinx.android.synthetic.main.activity_login_with_tel.*
import retrofit2.Response

class LoginWithTelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_with_tel)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp)
        }
        btn_commit.setOnClickListener {
            val phone = edit_tel.text.toString()
            if (phone.length != 11)
                Toast.makeText(this@LoginWithTelActivity, "请输入11位手机号码", Toast.LENGTH_SHORT).show()
            else {
                Model.checkPhone(phone,object :Model.CallBack<CheckPhoneBean>{
                    override fun onSuccess(response: Response<CheckPhoneBean>) {
                        if (response.body()?.exist == 1){
                            val intent = Intent(this@LoginWithTelActivity,InputPasswordActivity::class.java)
                            intent.putExtra("phone",phone)
                            startActivity(intent)
                        }else{
                            Toast.makeText(this@LoginWithTelActivity,"该手机号尚未注册",Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailed(t: Throwable) {
                        Toast.makeText(this@LoginWithTelActivity, t.toString(), Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
