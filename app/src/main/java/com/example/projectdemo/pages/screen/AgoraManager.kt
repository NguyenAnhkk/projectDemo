package com.example.projectdemo.pages.screen

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration

class AgoraManager(
    private val context: Context,
    private val appId: String,
    private val channelName: String,
    private val listener: AgoraListener
) {
    private var agoraEngine: RtcEngine? = null
    private var localUid: Int = 0

    interface AgoraListener {
        fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int)
        fun onUserJoined(uid: Int, elapsed: Int)
        fun onUserOffline(uid: Int, reason: Int)
        fun onError(err: Int)
    }

    fun initialize() {
        try {
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            
            // Configure video settings
            agoraEngine?.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                )
            )
            
            // Enable video
            agoraEngine?.enableVideo()
            
        } catch (e: Exception) {
            Log.e("AgoraManager", "Error initializing Agora: ${e.message}")
        }
    }

    fun setupLocalVideo(surfaceView: SurfaceView) {
        agoraEngine?.setupLocalVideo(
            VideoCanvas(
                surfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )
    }

    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        agoraEngine?.setupRemoteVideo(
            VideoCanvas(
                surfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                uid
            )
        )
    }

    fun joinChannel(token: String? = null) {
        agoraEngine?.joinChannel(
            token,
            channelName,
            "",
            0
        )
    }

    fun leaveChannel() {
        agoraEngine?.leaveChannel()
    }

    fun switchCamera() {
        agoraEngine?.switchCamera()
    }

    fun muteLocalAudio(mute: Boolean) {
        agoraEngine?.muteLocalAudioStream(mute)
    }

    fun muteLocalVideo(mute: Boolean) {
        agoraEngine?.muteLocalVideoStream(mute)
    }

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            localUid = uid
            listener.onJoinChannelSuccess(channel, uid, elapsed)
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            listener.onUserJoined(uid, elapsed)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            listener.onUserOffline(uid, reason)
        }

        override fun onError(err: Int) {
            listener.onError(err)
        }
    }

    fun release() {
        RtcEngine.destroy()
        agoraEngine = null
    }
} 