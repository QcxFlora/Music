package com.leishui.music.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.leishui.music.R
import com.leishui.music.adapter.MainViewPageAdapter
import com.leishui.music.adapter.UserInfoViewPageAdapter
import kotlinx.android.synthetic.main.activity_user_info.*

class UserInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        setSupportActionBar(toolbar_userinfo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Glide.with(this).load(intent.getStringExtra("url")).into(image_background)
        viewpage_userinfo.adapter = UserInfoViewPageAdapter(supportFragmentManager)
        tab_userinfo.setupWithViewPager(viewpage_userinfo)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
