package com.qfq.tainzhi.videoplayer.my_media;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.source.dash.manifest.SegmentBase;
import com.orhanobut.logger.Logger;

import java.lang.reflect.Method;

/**
 * @author: tainzhi
 * @mail: qfq61@qq.com
 * @date: 2019-11-10 19:10
 * @description: android 系统自带播放器
 **/

public class MediaSystem extends MediaInterface implements
		MediaPlayer.OnPreparedListener,
				MediaPlayer.OnCompletionListener,
				MediaPlayer.OnBufferingUpdateListener,
				MediaPlayer.OnSeekCompleteListener,
				MediaPlayer.OnErrorListener,
				MediaPlayer.OnInfoListener,
				MediaPlayer.OnVideoSizeChangedListener {
	
	public MediaPlayer mMediaPlayer;
	
	public MediaSystem(BaseVideoView baseVideoView) {
		super(baseVideoView);
	}
	
	@Override
	public void start() {
		Logger.d("");
		mMediaHandler.post(() -> { mMediaPlayer.start();});
	}
	
	@Override
	public void prepare() {
		Logger.d("create mediaplayer");
		release();
		mMediaHandlerThread = new HandlerThread("QVideoPlayer");
		mMediaHandlerThread.start();
		mMediaHandler = new Handler(mMediaHandlerThread.getLooper());
		mHandler = new Handler();
		
		mMediaHandler.post(() -> {
			try {
				mMediaPlayer = new MediaPlayer();
				// TODO: 2019-11-10 是否循环
				mMediaPlayer.setLooping(false);
				mMediaPlayer.setScreenOnWhilePlaying(true);
				mMediaPlayer.setOnPreparedListener(MediaSystem.this);
				mMediaPlayer.setOnCompletionListener(MediaSystem.this);
				mMediaPlayer.setOnBufferingUpdateListener(MediaSystem.this);
				mMediaPlayer.setOnSeekCompleteListener(MediaSystem.this);
				mMediaPlayer.setOnErrorListener(MediaSystem.this);
				mMediaPlayer.setOnInfoListener(MediaSystem.this);
				mMediaPlayer.setOnVideoSizeChangedListener(MediaSystem.this);
				Class<MediaPlayer> clazz = MediaPlayer.class;
				Method method = clazz.getDeclaredMethod("setDataSource", Context.class, Uri.class);
				method.invoke(mMediaPlayer, mBaseVideoView.getContext(), mBaseVideoView.videoUri);
				mBaseVideoView.mSurfaceHodler.bindToMediaPlayer(mMediaPlayer);
				mMediaPlayer.prepareAsync();
				// fixme
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	@Override
	public void pause() {
		mMediaHandler.post(() -> mMediaPlayer.pause());
	}
	
	@Override
	public boolean isPlaying() {
		return mMediaPlayer.isPlaying();
	}
	
	@Override
	public void seekTo(long time) {
		mMediaHandler.post(() -> {
			try {
				mMediaPlayer.seekTo((int)time);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		});
	}
	
	@Override
	public void release() {
		if (mMediaHandler != null && mMediaHandlerThread != null && mMediaHandler != null) {
			HandlerThread tmpHandlerThread = mMediaHandlerThread;
			MediaPlayer tmpMediaPlayer = mMediaPlayer;
			MediaInterface.sIRenderView = null;
			
			mMediaHandler.post(() -> {
				tmpMediaPlayer.setSurface(null);
				tmpMediaPlayer.release();
				tmpHandlerThread.quit();
			});
			mMediaPlayer = null;
		}
	}
	
	@Override
	public long getCurrentPosition() {
		if (mMediaPlayer != null) {
			return mMediaPlayer.getCurrentPosition();
		} else {
			return 0;
		}
	}
	
	@Override
	public long getDuration() {
		if (mMediaPlayer !=null) {
			return mMediaPlayer.getDuration();
		} else {
			return 0;
		}
	}
	
	@Override
	public void setVolume(float leftVoluem, float rightVolume) {
		if (mMediaHandler == null) return;
		mMediaHandler.post(() -> {
			if (mMediaPlayer != null) {
				mMediaPlayer.setVolume(leftVoluem, rightVolume);
			}
		});
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public void setSpeed(float speed) {
		PlaybackParams pp = mMediaPlayer.getPlaybackParams();
		pp.setSpeed(speed);
		mMediaPlayer.setPlaybackParams(pp);
	}
	
	@Override
	public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
		Logger.d("");
		mHandler.post(()->mBaseVideoView.setBufferProgress(percent));
	}
	
	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		mHandler.post(() -> mBaseVideoView.onAutoCompletion());
	}
	
	@Override
	public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
		Logger.d("");
		mHandler.post(() -> mBaseVideoView.onError(what, extra));
		return true;
	}
	
	@Override
	public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
		mHandler.post(() -> mBaseVideoView.onInfo(what, extra));
		return false;
	}
	
	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		Logger.d("");
		mHandler.post(() -> mBaseVideoView.onPrepared());
	}
	
	@Override
	public void onSeekComplete(MediaPlayer mediaPlayer) {
		mHandler.post(() -> mBaseVideoView.onSeekComplete());
	}
	
	@Override
	public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
		Logger.d("width=" + width + ", height=" + height);
		mHandler.post(() -> mBaseVideoView.onVideoSizeChanged(width, height));
	}
}
