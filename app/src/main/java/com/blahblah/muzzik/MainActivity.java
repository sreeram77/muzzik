package com.blahblah.muzzik;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends Activity {

    Muzzik muzzik = new Muzzik();
    Uri currentSong;
    private RecyclerView musicRecycler;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    String[] musicData = new String[] {"song 1", "song 2", "song 3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        musicRecycler = findViewById(R.id.musicList);
        musicRecycler.hasFixedSize();

        layoutManager = new LinearLayoutManager(this);
        musicRecycler.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(musicData);
        musicRecycler.setAdapter(mAdapter);



        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent,1);

    }








    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1) {
            if (resultCode == RESULT_OK){
                currentSong = data.getData();
                try {
                    muzzik.setDataSource(getApplicationContext(), currentSong);
                } catch (IOException e){
                    Log.e("Error", e.toString());
                }
                try {
                    muzzik.prepare();
                } catch (IOException e){
                    Log.e("Error", e.toString());
                }
                muzzik.start();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
