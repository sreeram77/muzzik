package com.blahblah.muzzik;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaTimestamp;
import android.net.Uri;

import java.io.IOException;

public class Muzzik extends MediaPlayer {

    public PlayerState getPlayerState() {
        return playerState;
    }

    private PlayerState playerState;

    public Muzzik() {
        this.playerState = PlayerState.PLAYER_IDLE;
    }

    @Override
    public void setDataSource(@androidx.annotation.NonNull Context context, @androidx.annotation.NonNull Uri uri) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        playerState = PlayerState.PLAYER_INITIALIZED;
        super.setDataSource(context, uri);
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        playerState = PlayerState.PLAYER_PREPARED;
        super.prepare();
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        playerState = PlayerState.PLAYER_PREPARING;
        super.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        playerState = PlayerState.PLAYER_STARTED;
        super.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        playerState = PlayerState.PLAYER_STOPPED;
        super.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        playerState = PlayerState.PLAYER_PAUSED;
        super.pause();
    }

    @Override
    public boolean isPlaying() {
        return super.isPlaying();
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        super.seekTo(msec);
    }

    @androidx.annotation.Nullable
    @Override
    public MediaTimestamp getTimestamp() {
        return super.getTimestamp();
    }

    @Override
    public int getCurrentPosition() {
        return super.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return super.getDuration();
    }

    @Override
    public void release() {
        playerState = PlayerState.PLAYER_END;
        super.release();
    }

    @Override
    public void reset() {
        playerState = PlayerState.PLAYER_IDLE;
        super.reset();
    }
}
