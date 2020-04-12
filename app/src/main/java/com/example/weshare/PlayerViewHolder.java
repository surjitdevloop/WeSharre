package com.example.weshare;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class PlayerViewHolder extends RecyclerView.ViewHolder {

    FrameLayout media_container;
    TextView title;
    ProgressBar progressBar;
    View parent;

    public PlayerViewHolder(@NonNull View itemView) {
        super(itemView);
        parent = itemView;
        media_container = itemView.findViewById(R.id.player_container);
        title = itemView.findViewById(R.id.title);
        progressBar = itemView.findViewById(R.id.progressBar);
    }

    public void onBind(VideoData videoData) {
        parent.setTag(this);
        title.setText(videoData.getTitle());
    }

}














