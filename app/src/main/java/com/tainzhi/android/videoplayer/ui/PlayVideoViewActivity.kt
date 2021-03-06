package com.tainzhi.android.videoplayer.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.tainzhi.android.videoplayer.R

/**
 * 使用[android.widget.VideoView] 和 [android.widget.MediaController]简单地播放视频
 */
class PlayVideoViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoUri = intent.data
        val videoName = intent.getStringExtra(VIDEO_NAME)
        val videoDuration = intent.getLongExtra(VIDEO_DURATION, 0)
        val videoProcess = intent.getLongExtra(VIDEO_PROGRESS, 0)

        setContentView(R.layout.activity_videoview_mediacontroller)

        val mediaController = MediaController(this)
        findViewById<VideoView>(R.id.videoView).run {
            setMediaController(mediaController)
            setVideoURI(videoUri)
            setOnPreparedListener { _ ->
                start()
            }
        }

    }

    companion object {
        const val VIDEO_NAME = "video_name"
        const val VIDEO_DURATION = "video_duration"
        const val VIDEO_PROGRESS = "video_progress"

        @JvmStatic
        fun startPlay(starter: Context, uri: Uri, name: String, duration: Long = 100, progress: Long = 100) {
            val intent = Intent(starter, PlayVideoViewActivity::class.java)
                    .setData(uri)
                    .putExtra(VIDEO_NAME, name)
                    .putExtra(VIDEO_DURATION, duration)
                    .putExtra(VIDEO_PROGRESS, progress)
            starter.startActivity(intent)
        }
    }
}