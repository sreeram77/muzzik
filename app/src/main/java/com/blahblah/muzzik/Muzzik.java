package com.blahblah.muzzik;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaTimestamp;
import android.net.Uri;

import java.io.IOException;

public class Muzzik extends MediaPlayer {
    @Override
    public void setDataSource(@androidx.annotation.NonNull Context context, @androidx.annotation.NonNull Uri uri) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(context, uri);
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        super.prepare();
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        super.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
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
        super.release();
    }

    @Override
    public void reset() {
        super.reset();
    }
}
