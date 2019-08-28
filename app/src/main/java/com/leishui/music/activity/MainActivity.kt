package com.leishui.music.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.isGone
import com.bumptech.glide.Glide
import com.leishui.music.Model
import com.leishui.music.R
import com.leishui.music.adapter.MainViewPageAdapter
import com.leishui.music.result.CheckPhoneBean
import com.leishui.music.result.SongBean.Track
import com.leishui.music.service.MediaPlayerListener
import com.leishui.music.service.MediaPlayerService
import com.leishui.music.util.StringUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_play.*
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var playerService: MediaPlayerService
    private var isPlaying: Boolean = false
    private val mediaPlayerListener = Listener()
    private var isBound = false
    private var position:String? = null
    private var track: Track? = null
    private var tracks:List<Track>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentId = Model.getCurrentListIdByShared(this)
        if (currentId != "") {
            position = Model.getStringByShared(this, "position")
            tracks = Model.getMusicListByShared(this,currentId!!)!!
            track = tracks!![position!!.toInt()]
            StringUtil.musicName(track!!,tv_musicname_main)
            Glide.with(this).load(track!!.al.picUrl).into(riv_al)
            playAudio()
            rl_main.isGone = false
        } else
            rl_main.isGone = true

        val nickname = Model.getUserInfoByShared(this, "nickname")
        val imageUrl = Model.getUserInfoByShared(this, "imageUrl")
        val headImageView = navigation_view.getHeaderView(0).findViewById<ImageView>(R.id.iv_head)
        val nicknameTextView = navigation_view.getHeaderView(0).findViewById<TextView>(R.id.nickname)

        viewpage.adapter = MainViewPageAdapter(supportFragmentManager)
        tab.setupWithViewPager(viewpage)

        setSupportActionBar(toolbar_main)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_dehaze_black_24dp)
        }

        Glide.with(this).load(imageUrl).into(headImageView)
        nicknameTextView.text = nickname

        headImageView.setOnClickListener {
            val intent = Intent(this, UserInfoActivity::class.java)
            intent.putExtra("url", imageUrl)
            startActivity(intent)
        }
        navigation_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_item_logout -> {
                    Model.logout(object : Model.CallBack<CheckPhoneBean> {
                        override fun onSuccess(response: Response<CheckPhoneBean>) {
                            Toast.makeText(this@MainActivity, "登出成功", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailed(t: Throwable) {
                            Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                        }

                    })
                    Model.saveIsLoginByShared(this, false)
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
        rl_main.setOnClickListener {
            startActivity(Intent(this,PlayActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (isBound){
            playerService.setListener(mediaPlayerListener)
            println(">>>>>>>>>>>>set")
            返回时歌词不同步
            Glide.with(this@MainActivity).load(Model.getStringByShared(this,"alUrl")).into(riv_al)
            isPlaying = playerService.isPlaying
            if (isPlaying) {
                ib_pap.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
            }else
                ib_pap.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> drawerlayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MediaPlayerService.LocalBinder
            playerService = binder.service
            isPlaying = playerService.isPlaying
            playerService.setListener(mediaPlayerListener)
            ib_pap.setOnClickListener {
                if (isPlaying) {
                    playerService.pauseMedia()
                    ib_pap.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
                    isPlaying = false
                } else {
                    playerService.playMedia()
                    ib_pap.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
                    isPlaying = true
                }
            }
            ib_list.setOnClickListener { playerService.nextMusic() }
            if (isPlaying) {
                ib_pap.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
            }
            //val position = playerService.resumePosition
            //duration = Model.getStringByShared(this@PlayActivity, "duration")!!.toInt()

            isBound = true
            Toast.makeText(this@MainActivity, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    private fun playAudio() {
        val playerIntent = Intent(this, MediaPlayerService::class.java)
        if (position != "-1") {
            playerIntent.putExtra("position", position)
            startService(playerIntent)
        }
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        playerService.setListener(null)
        unbindService(serviceConnection)
    }

    inner class Listener :MediaPlayerListener{
        override fun onInfoChanged(position: Int) {
            println(">>>>>>>>wocao")
            track = tracks!![position]
            StringUtil.musicName(track!!,tv_musicname_main)
            Glide.with(this@MainActivity).load(track!!.al.picUrl).into(riv_al)
            isPlaying = true
        }

        override fun onDurationChanged(duration: Int) {
        }

        override fun onProgressChanged(position: Int) {
        }

        override fun onStateChanged(state: Boolean) {
            if (state){
                ib_pap.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
            }
            else
                ib_pap.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
            isPlaying = state
        }
    }

    override fun onPause() {
        super.onPause()
        //playerService.setListener(null)
        //unbindService(serviceConnection)
    }
}
