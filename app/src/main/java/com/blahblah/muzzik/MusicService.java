package com.blahblah.muzzik;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import androidx.annotation.Nullable;

public class MusicService extends Service implements Muzzik.OnCompletionListener,
        Muzzik.OnPreparedListener, Muzzik.OnErrorListener, Muzzik.OnSeekCompleteListener,
        Muzzik.OnInfoListener, Muzzik.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener{

    Muzzik muzzik = new Muzzik();
    private AudioManager audioManager;
    private int resumePosition;
    private Uri songUri;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //An audio file is passed to the service through putExtra();
            songUri = Uri.parse(intent.getExtras().getString("SongUri"));
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (songUri != null && !songUri.toString().isEmpty()){
            initMuzzikPlayer();
            setAndStartSong(songUri);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (muzzik != null) {
            stopMedia();
            muzzik.release();
        }
        removeAudioFocus();
    }

    private void initMuzzikPlayer(){
        muzzik.setOnCompletionListener(this);
        muzzik.setOnErrorListener(this);
        muzzik.setOnPreparedListener(this);
        muzzik.setOnBufferingUpdateListener(this);
        muzzik.setOnSeekCompleteListener(this);
        muzzik.setOnInfoListener(this);
        muzzik.reset();
    }

    private void setAndStartSong(Uri songUri){
        if (muzzik.getPlayerState() == PlayerState.PLAYER_STARTED ||
                muzzik.getPlayerState() == PlayerState.PLAYER_PAUSED){
            muzzik.reset();
        }
        try {
            muzzik.setDataSource(getApplicationContext(), songUri);
        } catch (IOException e) {
            Log.e("Error", e.toString());
        }
        muzzik.prepareAsync();
        muzzik.start();
    }

    private void playMedia() {
        if (!muzzik.isPlaying()) {
            muzzik.start();
        }
    }

    private void stopMedia() {
        if (muzzik == null) return;
        if (muzzik.isPlaying()) {
            muzzik.stop();
        }
    }

    private void pauseMedia() {
        if (muzzik.isPlaying()) {
            muzzik.pause();
            resumePosition = muzzik.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!muzzik.isPlaying()) {
            muzzik.seekTo(resumePosition);
            muzzik.start();
        }
    }

    private final IBinder iBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (muzzik == null)
                    initMuzzikPlayer();
                else if (!muzzik.isPlaying())
                    muzzik.start();
                muzzik.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (muzzik.isPlaying())
                    muzzik.stop();
                muzzik.release();
                muzzik = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (muzzik.isPlaying()) muzzik.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (muzzik.isPlaying()) muzzik.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
