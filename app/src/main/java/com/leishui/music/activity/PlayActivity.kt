package com.leishui.music.activity

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.leishui.music.Model
import com.leishui.music.R
import com.leishui.music.service.MediaPlayerListener
import com.leishui.music.service.MediaPlayerService
import com.leishui.music.util.StringUtil
import kotlinx.android.synthetic.main.activity_play.*


class PlayActivity : AppCompatActivity() {

    private lateinit var playerService: MediaPlayerService
    private var duration = 0
    private var position = "0"
    private var isUser = false
    private var isPlaying = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        registerReceiver()
        val imgUrl = getAndSave("alUrl")
        position = getAndSave("position")!!
        val track = Model.getCurrentTrackByShared(this, Model.getCurrentListIdByShared(this)!!,position)
        StringUtil.musicName(track!!,tv_music_name_play)
        StringUtil.arName(track,tv_ar_name)
        Glide.with(this).load(imgUrl).into(allll as ImageView)
        playAudio()
        allll.startMusic()
        ib_finish.setOnClickListener { finish() }
    }

    /*
    Get the info from intent when created from PlayListDetailActivity to play new music
    Get the info from shared preference when created from other activities or notification
     */
    private fun getAndSave(key: String): String? {
        var value = intent.getStringExtra(key)
        if (value == null)
            value = Model.getStringByShared(this, key)
        else
            Model.saveStringByShared(this, key, value)
        return value
    }

    //Resume
    private fun resumeMusic() {
        isPlaying = true
        playerService.playMedia()
        btn_pause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
        allll.resumeMusic()
    }

    //Pause
    private fun pauseMusic() {
        isPlaying = false
        playerService.pauseMedia()
        btn_pause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
        allll.pauseMusic()
    }

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // Cast the IBinder and get Service instance after bound
            val binder = service as MediaPlayerService.LocalBinder
            playerService = binder.service
            //Set MediaPlayerListener
            playerService.setListener(object : MediaPlayerListener {
                override fun onInfoChanged(position:Int) {
                    val id = Model.getCurrentListIdByShared(this@PlayActivity)!!
                    val list = Model.getMusicListByShared(this@PlayActivity,id)!!
                    val music = list[position]
                    StringUtil.musicName(music,tv_music_name_play)
                    StringUtil.arName(music,tv_ar_name)
                    Glide.with(this@PlayActivity).load(music.al.picUrl).into(allll)
                    isPlaying = true
                }

                override fun onDurationChanged(duration: Int) {
                    this@PlayActivity.duration = duration
                    du.text = formatTime(duration)
                    seekbar.max = duration
                }

                override fun onProgressChanged(progress: Int) {
                    runOnUiThread {
                        println("-------------------------->>>>>>>>>>>>>>>>>>>>>>>${position}")
                        if (!isUser) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                seekbar.setProgress(progress, true)
                            } else
                                seekbar.progress = progress
                            pos.text = formatTime(progress)
                        }
                    }
                }

                override fun onStateChanged(state: Boolean) {
                    if (!state)
                        pauseMusic()
                    else
                        resumeMusic()
                }
            })
            //Refresh status after the service have been bound
            //Get the current playback status and refresh the UI
            isPlaying = playerService.isPlaying
            if (!isPlaying) {
                allll.pauseMusic()
                btn_pause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
            }
            //Get the current playback duration and refresh the UI
            if (playerService.getDuration() != null)
                duration = playerService.getDuration()!!
            du.text = formatTime(duration)
            seekbar.max = duration
            //Get the current playback progress and refresh the UI
            val progress = playerService.resumePosition
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                seekbar.setProgress(progress, true)
            } else
                seekbar.progress = progress
            pos.text = formatTime(progress)
            //Set the Play/Pause Music button click listener
            btn_pause.setOnClickListener {
                if (isPlaying) {
                    pauseMusic()
                } else {
                    resumeMusic()
                }
            }
            //Set the Next Music button click listener
            btn_next.setOnClickListener {
                playerService.nextMusic()
                isPlaying = true
                allll.resumeMusic()
                btn_pause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
            }
            //Set the Previous Music button click listener
            btn_previous.setOnClickListener {
                playerService.previousMusic()
                isPlaying = true
                allll.resumeMusic()
                btn_pause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
            }
            seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                var userSelectedPosition = 0
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    if (p2) {
                        userSelectedPosition = p1
                        pos.text = formatTime(p1)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    isUser = true
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    playerService.resumeMedia(userSelectedPosition)

                    isUser = false
                }

            })
            Toast.makeText(this@PlayActivity, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {
        }
    }

    private fun playAudio() {
        val playerIntent = Intent(this, MediaPlayerService::class.java)
        playerIntent.putExtra("position", position)
        startService(playerIntent)
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun formatTime(time: Int): String {
        var min = (time / (1000 * 60)).toString()
        var second = (time % (1000 * 60) / 1000).toString()
        if (min.length < 2)
            min = "0$min"
        if (second.length < 2)
            second = "0$second"
        return "$min:$second"
    }

    private fun registerReceiver(){
        val intentFilter = IntentFilter("prepared")
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                playerService.playMedia()
            }

        }, intentFilter)
    }

}
