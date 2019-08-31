package com.leishui.music.service

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import java.io.IOException
import android.util.Log
import android.media.AudioAttributes
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.os.*
import android.telephony.TelephonyManager
import android.telephony.PhoneStateListener
import com.leishui.music.Model
import kotlin.concurrent.thread
import android.widget.RemoteViews
import android.annotation.TargetApi
import android.app.*
import android.graphics.BitmapFactory.decodeResource
import com.leishui.music.R
import android.app.PendingIntent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.leishui.music.activity.PlayActivity
import com.leishui.music.result.SongBean.Track
import com.leishui.music.result.SongUrlBean.SongUrlBean
import retrofit2.Response
import android.app.NotificationManager
import android.app.NotificationChannel
import android.graphics.Color
import android.media.AudioFocusRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.leishui.music.util.StringUtil


class MediaPlayerService : Service(), MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, AudioManager.OnAudioFocusChangeListener {


    private var mediaPlayerListener: MediaPlayerListener? = null

    fun setListener(mediaPlayerListener: MediaPlayerListener?) {
        this.mediaPlayerListener = mediaPlayerListener
    }

    private var remoteViews: RemoteViews? = null
    private var notification: Notification? = null

    private var list: List<Track>? = null
    private var position = -1
    private var id: String? = null
    private var isNext = true
    var isPlaying = false
    private var isChanging = false
    private var isFirst = true

    private val iBinder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var mediaPath: String = ""
    //Used to pause/resume MediaPlayer
    var resumePosition: Int = 0
    private lateinit var audioManager: AudioManager
    //Handle incoming phone calls
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null

    override fun onBind(intent: Intent): IBinder? {
        return iBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mediaPlayerListener = null
        return super.onUnbind(intent)
    }


    fun getDuration(): Int? {
        return mediaPlayer?.duration
    }

    override fun onCreate() {
        super.onCreate()
        registerBecomingNoisyReceiver()
        callStateListener()
        myReceiver()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        remoteViews = viewsAndIntents()
        val NOTIFICATION_CHANNEL_ID = "com.example.simpleapp"
        val channelName = "My Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_logo_r)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setCategory(Notification.EXTRA_MEDIA_SESSION)
            .setCustomContentView(remoteViews)
            .build()
        startForeground(1, notification)
    }


    fun setForeground2() {
        remoteViews = viewsAndIntents()
        notification = NotificationCompat.Builder(this, "1")
            .setContentTitle("Music")
            .setSmallIcon(R.drawable.ic_logo_c)
            .setLargeIcon(decodeResource(resources, R.drawable.ic_logo_r))
            .setCustomContentView(remoteViews)
            .build()
        startForeground(1, notification)
    }

    private fun viewsAndIntents(): RemoteViews {
        val remoteView = RemoteViews(this.packageName, R.layout.layout_mediaplayer)

        val intentStartAndPause = Intent("playAndPauseMusic")
        val startAndPausePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intentStartAndPause,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        remoteView.setOnClickPendingIntent(R.id.ib_pap_notification, startAndPausePendingIntent)


        val intentPrevious = Intent("previousMusic")
        val previousPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intentPrevious,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        remoteView.setOnClickPendingIntent(R.id.ib_previous_notification, previousPendingIntent)

        val intentNext = Intent("nextMusic")
        val nextPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intentNext,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        remoteView.setOnClickPendingIntent(R.id.ib_next_notification, nextPendingIntent)

        val intent = Intent(this, PlayActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteView.setOnClickPendingIntent(R.id.iv_al_notification, pendingIntent)
        return remoteView
    }

    //The system calls this method when an activity, requests the service be started
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val currentPosition = position
        val currentId = id
        //An audio file is passed to the service through putExtra();
        //mediaPath = intent.getStringExtra("url")!!
        position = intent.getStringExtra("position")!!.toInt()
        Model.saveStringByShared(this, "position", position.toString())
        id = Model.getCurrentListIdByShared(this)!!
        list = Model.getMusicListByShared(this, id!!)!!
        //Request audio focus
        //if (!requestAudioFocus()) {
        //Could not gain focus
//            pauseMedia()
        //}
        if (id != currentId) {
            getUrl(list!!, position)
        } else {
            if (position != currentPosition)
                getUrl(list!!, position)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getUrl(list: List<Track>, position: Int) {
        Model.songUrl(list[position].id.toString(), object : Model.CallBack<SongUrlBean> {
            override fun onSuccess(response: Response<SongUrlBean>) {
                val path = response.body()?.data?.get(0)?.url
                if (path != null) {
                    val alUrl = list[position].al.picUrl
                    Model.saveStringByShared(this@MediaPlayerService, "alUrl", alUrl)
                    mediaPlayerListener?.onInfoChanged(position)
                    mediaPath = path
                    initMediaPlayer()
                    isChanging = false
                } else {
                    isChanging = false
                    Toast.makeText(this@MediaPlayerService, "音乐已失效,已自动跳过", Toast.LENGTH_SHORT)
                        .show()
                    if (isNext)
                        nextMusic()
                    else
                        previousMusic()
                }
            }


            override fun onFailed(t: Throwable) {
                Toast.makeText(this@MediaPlayerService, t.toString(), Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            stopMedia()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        //removeAudioFocus()
    }


    override fun onCompletion(mp: MediaPlayer) {
        nextMusic()
    }

    //Handle errors
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked when there has been an error during an asynchronous operation
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR SERVER DIED $extra"
            )
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR UNKNOWN $extra"
            )
        }
        return false
    }

    override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked to communicate some info.
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        //Invoked when the media source is ready for playback.
        mediaPlayerListener?.onDurationChanged(mediaPlayer!!.duration)
        if (!isFirst) {
            playMedia()
        }
        isFirst = false
        sendBroadcast(Intent("prepared"))
        Model.saveStringByShared(this, "duration", mediaPlayer!!.duration.toString())
    }

    override fun onAudioFocusChange(focusState: Int) {
        //Invoked when the audio focus of the system is updated.
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (mediaPlayer == null)
                    initMediaPlayer()
                else if (!mediaPlayer!!.isPlaying) mediaPlayer!!.start()
                mediaPlayer!!.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer!!.isPlaying) pauseMedia()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer!!.isPlaying) pauseMedia()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.setVolume(0.1f, 0.1f)
        }
    }

    inner class LocalBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }

    fun playMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
            isPlaying = true

            if (Build.VERSION.SDK_INT >= 26) {
                startMyOwnForeground()
            } else {
                setForeground2()
            }

            refreshTextInRemoteViews(
                R.id.tv_music_name_notification,
                StringUtil.musicName(list!![position])
            )
            refreshImageInRemoteViews(
                R.id.ib_pap_notification,
                R.drawable.ic_pause_circle_filled_black_24dp
            )

            mediaPlayerListener?.onStateChanged(isPlaying)

            //sendMessage(4,1)
            thread(start = true) {
                while (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    mediaPlayerListener?.onProgressChanged(mediaPlayer!!.currentPosition)
                    Thread.sleep(500)
                }
            }
        }
        requestAudioFocus()
    }

    private fun stopMedia() {
        if (mediaPlayer!!.isPlaying) {
            //sendMessage(4,0)
            mediaPlayer!!.stop()
        }
    }

    fun pauseMedia() {
        if (mediaPlayer!!.isPlaying) {
            //sendMessage(4,0)
            mediaPlayer!!.pause()
            isPlaying = false

            mediaPlayerListener?.onStateChanged(isPlaying)

            refreshImageInRemoteViews(
                R.id.ib_pap_notification,
                R.drawable.ic_play_circle_filled_black_24dp
            )

            resumePosition = mediaPlayer!!.currentPosition
        }
    }

    fun resumeMedia(resumePosition: Int) {
        mediaPlayer!!.seekTo(resumePosition)
        isPlaying = true
        mediaPlayerListener?.onStateChanged(isPlaying)
        playMedia()
    }

    fun nextMusic() {
        if (position < list!!.size - 1 && !isChanging) {
            position += 1
            Model.saveStringByShared(this, "position", position.toString())
            isNext = true
            isChanging = true
            getUrl(list!!, position)

            mediaPlayerListener?.onStateChanged(true)
        }
    }

    fun previousMusic() {
        if (position > 0 && !isChanging) {
            position -= 1
            Model.saveStringByShared(this, "position", position.toString())
            isNext = false
            isChanging = true
            getUrl(list!!, position)

            mediaPlayerListener?.onStateChanged(true)

            //sendMessage(4,1)
        }
    }

    private fun initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            //Set up MediaPlayer event listeners
            mediaPlayer!!.setOnCompletionListener(this)
            mediaPlayer!!.setOnErrorListener(this)
            mediaPlayer!!.setOnPreparedListener(this)
            mediaPlayer!!.setOnInfoListener(this)
        }
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer!!.reset()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        } else
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            // Set the data source to the mediaFile location
            mediaPlayer!!.setDataSource(mediaPath)
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf()
        }

        mediaPlayer!!.prepareAsync()
    }

    private fun requestAudioFocus(): Boolean {
        val result: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val mAudioAttributes =

                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

            val mAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mAudioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this).build()

            result = audioManager.requestAudioFocus(mAudioFocusRequest)
        } else {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            result = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this)
    }

    //Becoming noisy
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia()
            //buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private val startReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            if (mediaPlayer!!.isPlaying) {
                pauseMedia()
            } else {
                playMedia()
            }
            //buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private fun refreshImageInRemoteViews(id: Int, res: Int) {
        remoteViews?.setImageViewResource(id, res)
        startForeground(1, notification)
    }

    private fun refreshTextInRemoteViews(id: Int, text: CharSequence) {
        remoteViews?.setTextViewText(id, text)
        startForeground(1, notification)
    }


    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    private fun myReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter("playAndPauseMusic")
        val intentFilter2 = IntentFilter("previousMusic")
        val intentFilter3 = IntentFilter("nextMusic")
        registerReceiver(startReceiver, intentFilter)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                previousMusic()
            }

        }, intentFilter2)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                nextMusic()
            }

        }, intentFilter3)
    }

    //Handle incoming phone calls
    private fun callStateListener() {
        // Get the telephony manager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mediaPlayer != null) {
                        pauseMedia()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE ->
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                playMedia()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call mediaPlayerListener.
        telephonyManager?.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )
    }
}