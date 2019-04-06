package com.blahblah.muzzik;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends Activity {

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int MY_PERMISSIONS_REQUEST_WAKE_LOCK = 124;

    Muzzik muzzik = new Muzzik();
    private RecyclerView musicRecycler;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<MusicData> musicDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //muzzik.setScreenOnWhilePlaying(true);
        PermissionHandler wakeLockPermission = new PermissionHandler(Manifest.permission.WAKE_LOCK);
        if (wakeLockPermission.checkPermission(this)){
            muzzik.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        }
        musicRecycler = findViewById(R.id.musicList);
        musicRecycler.hasFixedSize();

        getSongList();

        layoutManager = new LinearLayoutManager(this);
        musicRecycler.setLayoutManager(layoutManager);

        MusicOnClickListener musicOnClickListener = new MusicOnClickListener() {
            @Override
            public void onItemClick(View v, int musicPos) {
                setAndStartSong(musicDataList.get(musicPos).getSongUri());
            }
        };

        FloatingActionButton fab = findViewById(R.id.fab_play);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TogglePlayerState();
            }
        });

        mAdapter = new MyAdapter(musicOnClickListener, musicDataList);
        musicRecycler.setAdapter(mAdapter);
    }

    private void TogglePlayerState(){
        if (muzzik.getPlayerState() == PlayerState.PLAYER_STARTED){
            muzzik.pause();
        } else if (muzzik.getPlayerState() == PlayerState.PLAYER_PAUSED){
            muzzik.start();
        }
    }

    @Override
    protected void onStop() {
        muzzik.release();
        muzzik = null;
        super.onStop();
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
        try {
             muzzik.prepare();
        } catch (IOException e) {
            Log.e("Error", e.toString());
        }
        muzzik.start();
    }

    private void getSongList(){

        PermissionHandler readExternalStorage = new PermissionHandler(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (readExternalStorage.checkPermission(this)) {
            ContentResolver musicResolver = getContentResolver();
            Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

            if (musicCursor != null && musicCursor.moveToFirst()) {
                //get columns
                int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                //add songs to list
                do {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    Uri uri = ContentUris.withAppendedId(musicUri, musicCursor.getInt(idColumn));
                    musicDataList.add(new MusicData(uri, thisId, thisTitle, thisArtist));
                }
                while (musicCursor.moveToNext());
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_WAKE_LOCK:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(this, "Wake Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }
}
