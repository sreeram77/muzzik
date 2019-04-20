package com.blahblah.muzzik;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends Activity {

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int MY_PERMISSIONS_REQUEST_WAKE_LOCK = 124;
    public static final int PLAYER_NOTIFICATION = 101;
    private static final String CHANNEL_ID = "music";
    public static final String PLAYER_INTENT = "player";

    private RecyclerView musicRecycler;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<MusicData> musicDataList = new ArrayList<>();
    private MusicService musicService;
    boolean serviceBound = false;
    private TextView musicTitle;
    NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicRecycler = findViewById(R.id.musicList);
        musicRecycler.hasFixedSize();

        getSongList();

        musicTitle = findViewById(R.id.music_title);

        layoutManager = new LinearLayoutManager(this);
        musicRecycler.setLayoutManager(layoutManager);

        MusicOnClickListener musicOnClickListener = new MusicOnClickListener() {
            @Override
            public void onItemClick(View v, int musicPos) {
                setAndStartSong(musicPos);
                Global.musicPos = musicPos;
                refreshMusicTitle();
            }
        };

        FloatingActionButton fab = findViewById(R.id.fab_play);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TogglePlayerState();
            }
        });

        ImageButton mediaPrev = findViewById(R.id.media_prev);
        mediaPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaSelectPrevious();
                refreshMusicTitle();
            }
        });

        ImageButton mediaNext = findViewById(R.id.media_next);
        mediaNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaSelectNext();
                refreshMusicTitle();
            }
        });

        mAdapter = new MyAdapter(musicOnClickListener, musicDataList);
        musicRecycler.setAdapter(mAdapter);

        initializeNotification();
    }

    private void refreshMusicTitle(){
        musicTitle.setText(musicDataList.get(Global.musicPos).getSongTitle());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Muzzik player";
            String description = "Music player";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void initializeNotification() {
        
        Intent prevIntent = new Intent(MainActivity.this, MusicService.class);
        prevIntent.setAction("previous");
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 0, prevIntent, 0);

        Intent nextIntent = new Intent(MainActivity.this, MusicService.class);
        nextIntent.setAction("next");
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        Intent pauseIntent = new Intent(MainActivity.this, MusicService.class);
        pauseIntent.setAction("playPause");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        Intent actIntent = new Intent(this, MainActivity.class);
        PendingIntent actPendingIntent = PendingIntent.getActivity(this, 0, actIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_menu_white_24)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add media control buttons that invoke intents in your media service
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent) // #0
                .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)  // #1
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)     // #2
                // Apply the media style template
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1))
                .setContentTitle(musicDataList.get(Global.musicPos).getSongTitle())
                .setContentText(musicDataList.get(Global.musicPos).getSongArtist())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(actPendingIntent)
                .setOngoing(true);

        createNotificationChannel();

        notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(PLAYER_NOTIFICATION, builder.build());
    }

    private void updateNotification(){
        notificationManager.notify();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void mediaSelectPrevious(){
        if (serviceBound && Global.musicPos > 0) {
            musicService.playPrevious(--Global.musicPos);
        }
    }

    private void mediaSelectNext(){
        if (serviceBound && Global.musicPos < musicDataList.size() - 1){
            musicService.playNext(++Global.musicPos);
        }
    }

    private void TogglePlayerState(){
        if (serviceBound){
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

    private void setAndStartSong(int musicPos){
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("newStart");
        intent.putExtra(PLAYER_INTENT, musicPos);
        startService(intent);
        if(!serviceBound) {
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
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
