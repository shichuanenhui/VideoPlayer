package com.tainzhi.qmediaplayer

import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.SurfaceHolder
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener

/**
 * @author: tainzhi
 * @mail: qfq61@qq.com
 * @date: 2019-11-11 11:41
 * @description:
 */
class IMediaExo(videoView: VideoView) : IMediaInterface(videoView), Player.EventListener, VideoListener {
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var callback: Runnable? = null
    private var previousSeek: Long = 0

    companion object {
        private const val TAG = "IMediaExo"
    }

    override fun start() {
        logI(TAG, "start()")
        simpleExoPlayer?.playWhenReady = true
    }

    override fun setDisplay(surfaceHolder: SurfaceHolder) {
        simpleExoPlayer?.setVideoSurfaceHolder(surfaceHolder)
    }

    override fun setDisplay(surface: Surface) {
        simpleExoPlayer?.setVideoSurface(surface)
    }

    override fun prepare() {
        logI(TAG, "prepare()")
        val context = mVideoView.context
        release()
        mMediaHandlerThread = HandlerThread("QMediaPlayer")
        mMediaHandlerThread!!.start()
        mMediaHandler = Handler(mMediaHandlerThread!!.looper)
        mHandler = Handler()
        mMediaHandler!!.post {
            val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
            val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
            val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            val loadControl: LoadControl = DefaultLoadControl(DefaultAllocator(true,
                    C.DEFAULT_BUFFER_SEGMENT_SIZE),
                    360000, 600000, 1000, 5000,
                    C.LENGTH_UNSET,
                    false)
            // 2. Create the player
            val renderersFactory: RenderersFactory = DefaultRenderersFactory(context)
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, renderersFactory, trackSelector, loadControl)
            // Produces DataSource instances through which media data is loaded.
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "QMediaPlayer"))
            // String currUrl = jzvd.jzDataSource.getCurrentUrl().toString();
            val videoSource: MediaSource
            // if (currUrl.contains(".m3u8")) {
            // 	videoSource = new HlsMediaSource.Factory(dataSourceFactory)
            // 			              .createMediaSource(Uri.parse(currUrl), handler, null);
            // } else {
            // 	videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
            // 			              .createMediaSource(Uri.parse(currUrl));
            // }
            videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mVideoView.videoUri)
            simpleExoPlayer?.addVideoListener(this)
            val isLoop = mVideoView.loop
            if (isLoop) {
                simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            } else {
                simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
            }
            simpleExoPlayer?.prepare(videoSource)
            simpleExoPlayer?.playWhenReady = true
            callback = OnBufferUpdate()
            mVideoView.mSurfaceHolder!!.bindToMediaPlayer(this)
        }
    }

    override fun pause() {
        logI(TAG, "pause()")
        simpleExoPlayer?.playWhenReady = false
    }

    override fun resetDataSource(uri: Uri) {
        val context = mVideoView.context
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "QMediaPlayer"))
        // String currUrl = jzvd.jzDataSource.getCurrentUrl().toString();
        val videoSource: MediaSource
        // if (currUrl.contains(".m3u8")) {
        // 	videoSource = new HlsMediaSource.Factory(dataSourceFactory)
        // 			              .createMediaSource(Uri.parse(currUrl), handler, null);
        // } else {
        // 	videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
        // 			              .createMediaSource(Uri.parse(currUrl));
        // }
        videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mVideoView.videoUri)
        simpleExoPlayer?.release()
        simpleExoPlayer?.prepare(videoSource)
    }

    override val isPlaying: Boolean
        get() = simpleExoPlayer?.playWhenReady ?: false

    override fun seekTo(time: Long) {
        if (time != previousSeek) {
            simpleExoPlayer?.seekTo(time)
            previousSeek = time
            // mVideoView.seekInAdvance = time;
        }
    }

    override fun release() {
        logI(TAG, "release()")
        if (mMediaHandler != null && mMediaHandlerThread != null && simpleExoPlayer != null) {
            mMediaHandler!!.post {
                mMediaHandlerThread?.quit()
                simpleExoPlayer?.run {
                    release()
                }
            }
        }
    }

    override val currentPosition: Long
        get() = simpleExoPlayer?.contentPosition ?: 0

    override val duration: Long
        get() = simpleExoPlayer?.duration ?: 0


    override fun setVolume(leftVoluem: Float, rightVolume: Float) {
        simpleExoPlayer?.volume = leftVoluem
        simpleExoPlayer?.volume = rightVolume
    }

    override fun setSpeed(speed: Float) {
        val parameters = PlaybackParameters(speed, 1.0f)
        // TODO: 2020/6/13 set speed
        // simpleExoPlayer.playbackParameters = parameters
    }

    override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) { // handler.post(() -> jzvd.onVideoSizeChanged(width, height));
    }

    override fun onRenderedFirstFrame() {
        logI(TAG, "onRenderedFirstFrame()")
    }

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
        logI(TAG, "onTimelineChanged()")
    }

    override fun onTracksChanged(trackGroupArray: TrackGroupArray,
                                 selectionArray: TrackSelectionArray) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        logI(TAG, "onLoadingChanged")
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        logI(TAG, "onPlayerStateChanged()")
        mHandler!!.post {
            when (playbackState) {
                Player.STATE_IDLE -> {
                }
                Player.STATE_BUFFERING -> mHandler!!.post(callback!!)
                Player.STATE_READY -> if (playWhenReady) { // mVideoView.onStatePlaying();
                } else {
                }
                Player.STATE_ENDED -> {
                    mVideoView.onAutoCompletion()
                }
            }
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {}
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
    override fun onPlayerError(error: ExoPlaybackException) {
        logD()
        mHandler!!.post { mVideoView.onError(1000, 1000) }
    }

    override fun onPositionDiscontinuity(reason: Int) {}
    override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {}
    override fun onSeekProcessed() {
        mHandler!!.post { mVideoView.onSeekCompleted() }
    }

    private inner class OnBufferUpdate : Runnable {
        override fun run() {
            if (simpleExoPlayer != null) {
                val percent = simpleExoPlayer?.bufferedPercentage ?: 0
                mHandler!!.post { mVideoView.setBufferProgress(percent) }
                if (percent < 100) {
                    mHandler!!.postDelayed(callback!!, 300)
                } else {
                    mHandler!!.removeCallbacks(callback!!)
                }
            }
        }
    }
}