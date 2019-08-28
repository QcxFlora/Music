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
import android.os.Build
import androidx.annotation.RequiresApi


class MediaPlayerService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener,
    MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {


    private var mediaPlayerListener: MediaPlayerListener? = null

    fun setListener(mediaPlayerListener: MediaPlayerListener?) {
        this.mediaPlayerListener = mediaPlayerListener
    }

    private lateinit var list: List<Track>
    private var position = -1
    private var id = ""
    private var isNext = true
    var isPlaying = true
    private var isChanging = false

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

    fun getDuration():Int?{
        return mediaPlayer?.duration
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            startMyOwnForeground()
        } else {
            setForeground2()
        }
        myReceiver()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        val channelName = "My Background Service";
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.lightColor = Color.BLUE;
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE;
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //assert manager != null;
        manager.createNotificationChannel(chan);

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_logo_r)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build();
        startForeground(2, notification);
    }

    @TargetApi(26)
    fun setForeground() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        val channel = NotificationChannel("myChannel", "myChannel", NotificationManager.IMPORTANCE_HIGH)
//        manager.createNotificationChannel(channel)
        val channel2 = NotificationChannel("cnm", "cnm", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel2)

        val remoteView = viewsAndIntents()

        val notification = Notification.Builder(this, "1")
            .setContentTitle("Music")
            .setSmallIcon(R.drawable.ic_logo_c)
            .setLargeIcon(decodeResource(resources, R.drawable.ic_logo_r))
            .setCustomContentView(remoteView)
            .build()
        startForeground(1, notification)
    }


    fun setForeground2() {
        val remoteView = viewsAndIntents()
        val notification = NotificationCompat.Builder(this, "1")
            .setContentTitle("Music")
            .setSmallIcon(R.drawable.ic_logo_c)
            .setLargeIcon(decodeResource(resources, R.drawable.ic_logo_r))
            .setCustomContentView(remoteView)
            .build()
        startForeground(1, notification)
    }

    private fun viewsAndIntents(): RemoteViews {
        val remoteView = RemoteViews(this.packageName, R.layout.layout_mediaplayer)

        val intentStart = Intent("playMusic")
        val startPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intentStart,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        remoteView.setOnClickPendingIntent(R.id.tv_play, startPendingIntent)

        val intentPause = Intent("pauseMusic")
        val pausePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intentPause,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        remoteView.setOnClickPendingIntent(R.id.tv_pause, pausePendingIntent)

        val intent = Intent(this, PlayActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteView.setOnClickPendingIntent(R.id.iv, pendingIntent)
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
        list = Model.getMusicListByShared(this, id)!!
        //Request audio focus
        if (requestAudioFocus() === false) {
            //Could not gain focus
            stopSelf()
        }
        if (id != currentId) {
            getUrl(list, position)
        } else {
            if (position != currentPosition)
                getUrl(list, position)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getUrl(list: List<Track>, position: Int) {
        Model.songUrl(list[position].id.toString(), object : Model.CallBack<SongUrlBean> {
            override fun onSuccess(response: Response<SongUrlBean>) {
                val path = response.body()!!.data[0].url
                if (path != null) {
                    val alUrl = list[position].al.picUrl
                    Model.saveStringByShared(this@MediaPlayerService, "alUrl", alUrl)
                    mediaPlayerListener?.onInfoChanged(position)
                    mediaPath = path
                    initMediaPlayer()
                    isChanging = false
                } else {
                    isChanging = false
                    Toast.makeText(this@MediaPlayerService, "音乐已失效,已自动跳过", Toast.LENGTH_SHORT).show()
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
        removeAudioFocus()
    }

//    override fun onUnbind(intent: Intent?): Boolean {
//        if (mediaPlayer != null) {
//            stopMedia()
//            mediaPlayer!!.release()
//            mediaPlayer = null
//        }
//        removeAudioFocus()
//        return super.onUnbind(intent)
//    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    override fun onCompletion(mp: MediaPlayer) {
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>completion")
        //Invoked when playback of a media source has completed.
        nextMusic()
        //stopMedia()
        //stop the service
        //stopSelf()
    }

    //Handle errors
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked when there has been an error during an asynchronous operation
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED $extra")
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN $extra")
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
        playMedia()
//        val msg1 = Message()
//        msg1.what = 0
//        msg1.arg1 = mediaPlayer!!.duration
//        handler?.sendMessage(msg1)
        Model.saveStringByShared(this, "duration", mediaPlayer!!.duration.toString())
    }

    override fun onSeekComplete(mp: MediaPlayer) {
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>SeekCompletion")
        //Invoked indicating the completion of a seek operation.
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
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.pause()
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

            mediaPlayerListener?.onStateChanged(isPlaying)

            //sendMessage(4,1)
            thread(start = true) {
                while (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    mediaPlayerListener?.onProgressChanged(mediaPlayer!!.currentPosition)
                    Thread.sleep(500)
                }
            }
        }
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

            resumePosition = mediaPlayer!!.currentPosition
        }
    }

    fun resumeMedia(resumePosition: Int) {
        mediaPlayer!!.seekTo(resumePosition)
        //sendMessage(4,1)
        isPlaying = true

        mediaPlayerListener?.onStateChanged(isPlaying)

        playMedia()
    }

    fun resumeMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
            //sendMessage(4,1)
            isPlaying = true

            mediaPlayerListener?.onStateChanged(isPlaying)

            mediaPlayer!!.start()
        }
    }

    fun nextMusic() {
        if (position < list.size - 1 && !isChanging) {
            position += 1
            Model.saveStringByShared(this, "position", position.toString())
            isNext = true
            isChanging = true
            getUrl(list, position)

            mediaPlayerListener?.onStateChanged(true)

            //sendMessage(4,1)
        }
    }

    fun previousMusic() {
        if (position > 0 && !isChanging) {
            position -= 1
            Model.saveStringByShared(this, "position", position.toString())
            isNext = false
            isChanging = true
            getUrl(list, position)

            mediaPlayerListener?.onStateChanged(true)

            //sendMessage(4,1)
        }
    }

    private fun initMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = MediaPlayer()
        //Set up MediaPlayer event listeners
        mediaPlayer!!.setOnCompletionListener(this)
        mediaPlayer!!.setOnErrorListener(this)
        mediaPlayer!!.setOnPreparedListener(this)
        mediaPlayer!!.setOnBufferingUpdateListener(this)
        mediaPlayer!!.setOnSeekCompleteListener(this)
        mediaPlayer!!.setOnInfoListener(this)
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
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
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
            playMedia()
            //buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private val pauseReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            pauseMedia()
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    private fun myReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter("playMusic")
        val intentFilter2 = IntentFilter("pauseMusic")
        registerReceiver(startReceiver, intentFilter)
        registerReceiver(pauseReceiver, intentFilter2)
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
                                resumeMedia()
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