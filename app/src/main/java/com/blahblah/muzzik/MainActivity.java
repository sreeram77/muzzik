package com.blahblah.muzzik;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends Activity {

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    Muzzik muzzik = new Muzzik();
    private RecyclerView musicRecycler;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<MusicData> musicDataList = new ArrayList<>();

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

        BottomAppBar bar = (BottomAppBar) findViewById(R.id.bottom_app_bar);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
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

        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
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

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
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
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }
}
