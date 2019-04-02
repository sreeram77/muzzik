package com.blahblah.muzzik;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<MusicData> musicDataList;
    private MusicOnClickListener musicOnClickListener;

    public MyAdapter(MusicOnClickListener listener, List<MusicData> musicDataList) {
        this.musicDataList = musicDataList;
        this.musicOnClickListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView textView;
        private MusicOnClickListener musicOnClickListener;


        public MyViewHolder(View v, MusicOnClickListener musicOnClickListener) {
            super(v);
            textView = v.findViewById(R.id.textView);
            this.musicOnClickListener = musicOnClickListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            musicOnClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,  int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_list_view, parent, false);
        return new MyViewHolder(v, musicOnClickListener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.textView.setText(musicDataList.get(position).getSongTitle());

    }

    @Override
    public int getItemCount() {
        return musicDataList.size();
    }
}
