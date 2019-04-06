package com.blahblah.muzzik;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
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

    private RecyclerView musicRecycler;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<MusicData> musicDataList = new ArrayList<>();
    private MusicService musicService;
    boolean serviceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    private void TogglePlayerState(){
        if(serviceBound){
            Toast.makeText(this, "BLAH", Toast.LENGTH_LONG).show();
            musicService.togglePlayerState();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            musicService.stopSelf();
        }
    }

    private void setAndStartSong(Uri songUri){
        if(!serviceBound){
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("SongUri", songUri.toString());
            startService(intent);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        } else {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("SongUri", songUri.toString());
            startService(intent);
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
