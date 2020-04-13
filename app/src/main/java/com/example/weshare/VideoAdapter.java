package com.example.weshare;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

public class VideoAdapter extends FirestorePagingAdapter<VideoData, PlayerViewHolder> {


    private static final String TAG = "VideoAdapter";
    private Context context;

    public VideoAdapter(Context context, FirestorePagingOptions<VideoData> options) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PlayerViewHolder(
                LayoutInflater.
                        from(viewGroup.getContext()).inflate(R.layout.video_player_item_view,
                        viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull PlayerViewHolder holder, int position, @NonNull VideoData model) {

        Log.e(TAG, "model.getUrl() " + model.getUrl());
        holder.onBind(model);
    }


}