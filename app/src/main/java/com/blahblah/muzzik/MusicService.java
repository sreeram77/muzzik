package com.blahblah.muzzik;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

import static com.blahblah.muzzik.MainActivity.PLAYER_INTENT;

public class MusicService extends Service implements Muzzik.OnCompletionListener,
        Muzzik.OnPreparedListener, Muzzik.OnErrorListener, Muzzik.OnSeekCompleteListener,
        Muzzik.OnInfoListener, Muzzik.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener{

    Muzzik muzzik = new Muzzik();
    private AudioManager audioManager;
    private int resumePosition;
    private List<Uri> musicList = new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (musicList.isEmpty()){
            getSongList();
        }
        try {
            switch (intent.getAction()){
                case "playPause" :
                    togglePlayerState();
                    break;
                case "newStart" :
                    initMuzzikPlayer();
                    Global.musicPos = intent.getIntExtra(PLAYER_INTENT,0);
                    setAndStartSong(musicList.get(intent.getIntExtra(PLAYER_INTENT,0)));
                    break;
                case "next" :
                    playNext(++Global.musicPos);
                    break;
                case "previous" :
                    playPrevious(--Global.musicPos);
                    break;
            }

        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void togglePlayerState(){
        if (muzzik.getPlayerState() == PlayerState.PLAYER_STARTED){
            muzzik.pause();
        } else if (muzzik.getPlayerState() == PlayerState.PLAYER_PAUSED) {
            muzzik.start();
        }
    }

    public void playNext(int pos){
        if (pos <= musicList.size())
        setAndStartSong(musicList.get(pos));
    }

    public void playPrevious(int pos){
        if (pos >= 0) {
            setAndStartSong(musicList.get(pos));
        }
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
        if (muzzik == null) {
            return;
        }
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

    private void getSongList(){

        PermissionHandler readExternalStorage = new PermissionHandler(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (readExternalStorage.checkPermission(this)) {
            ContentResolver musicResolver = getContentResolver();
            Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

            if (musicCursor != null && musicCursor.moveToFirst()) {
                //get columns
                int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                //add songs to list
                do {
                    musicList.add(ContentUris.withAppendedId(musicUri, musicCursor.getInt(idColumn)));
                }
                while (musicCursor.moveToNext());
            }
        }
    }
}
