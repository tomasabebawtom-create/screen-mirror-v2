package com.example.screenmirror

import android.app.*
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import org.webrtc.*

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private lateinit var signalingClient: SignalingClient
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private var videoCapturer: VideoCapturer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()

        val resultCode = intent?.getIntExtra("resultCode", 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>("data")
        val signalingUrl = intent?.getStringExtra("signalingUrl") ?: ""

        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (data != null) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        }

        initWebRTC(signalingUrl)

        return START_STICKY
    }

    private fun initWebRTC(signalingUrl: String) {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions()
        )

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(SoftwareVideoEncoderFactory())
            .setVideoDecoderFactory(SoftwareVideoDecoderFactory())
            .createPeerConnectionFactory()

        signalingClient = SignalingClient(signalingUrl) { message ->
            // handle incoming signaling messages (offer/answer/ice) here
        }
        signalingClient.connect()
    }

    private fun startForegroundNotification() {
        val channelId = "screen_mirror_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Screen Mirror", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Screen Mirror")
            .setContentText("Mirroring screen...")
            .build()
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mediaProjection?.stop()
        signalingClient.close()
    }
}
