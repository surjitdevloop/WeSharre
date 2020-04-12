package com.example.weshare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class LandingActivity extends AppCompatActivity {

    private static final String TAG = "LandingActivity";
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mRecyclerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    private VideoRecyclerView mRecyclerView;
    private VideoAdapter videoAdapter;
    private ArrayList<VideoData> videoDataList;

    private FirebaseFirestore firebaseFirestore;
    private boolean isInitial = true;
    private int visibleItemPos;

    private String collectionPath = "videoList";
    private View gesture;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_landing);

        mVisible = true;
        firebaseFirestore = FirebaseFirestore.getInstance();
        videoDataList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.video_recycler_video);
        gesture = findViewById(R.id.gestureListener);
        gesture.setOnClickListener(v -> Log.e(TAG, "gesture Clicked"));
        initRecyclerView();

        GestureDetector gestureDetector = new GestureDetector(this, onGestureListener);
        gesture.setOnTouchListener((v, event) -> {
            Log.e(TAG, "gesture onTouch");
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }
            return super.onTouchEvent(event);
        });
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder<VideoData>()
                .setLifecycleOwner(this)
                .setQuery(firebaseFirestore.collection(collectionPath)
                                .orderBy("timestamp"),
                        snapshot -> {
                            Log.e(TAG, "parseSnapshot");
                            VideoData object = snapshot.toObject(VideoData.class);
                            Log.e(TAG, "url " + object.getUrl());
                            videoDataList.add(object);
                            setNV(videoDataList);
                            return object;
                        }).build();
        videoAdapter = new VideoAdapter(this, options);
        mRecyclerView.setAdapter(videoAdapter);


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                Log.e(TAG, "RCV " + newState);
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                Log.e(TAG, "onScrolled " + dy);
                if (isInitial) {
                    isInitial = false;
                    mRecyclerView.playVideo(false);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });


        findViewById(R.id.record_button).setOnClickListener(v -> {
            /*
            Intent intent = new Intent(LandingActivity.this, RecordActivity.class);
            startActivity(intent);
            finish();*/
        });
    }

    private void setNV(ArrayList<VideoData> videoData) {
        mRecyclerView.setVideoDataList(videoData);
    }

    @Override
    protected void onDestroy() {
        if (mRecyclerView != null)
            mRecyclerView.releasePlayer();
        super.onDestroy();
    }


    GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.e(TAG, "onFling: event1" + event1.toString());
            if (velocityY < 0) { //Up nextItem show
                visibleItemPos += 1;
                if (visibleItemPos >= videoAdapter.getItemCount()) {
                    visibleItemPos = 0;
                    mRecyclerView.scrollToPosition(visibleItemPos);
                } else {
                    mRecyclerView.smoothScrollToPosition(visibleItemPos);
                }

            } else {//Up previousItem show
                visibleItemPos -= 1;
                if (visibleItemPos < 0) {
                    visibleItemPos = videoAdapter.getItemCount() - 1;
                    mRecyclerView.scrollToPosition(visibleItemPos);
                } else {
                    mRecyclerView.smoothScrollToPosition(visibleItemPos);
                }
            }


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                                float distanceY) {
            Log.d(TAG, "onScroll: " + event1.toString() + event2.toString());
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }
    };


    // Fullscreen Setting
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


        delayedHide(100);
    }

    private void hide() {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;


        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {

        mRecyclerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;


        mHideHandler.removeCallbacks(mHidePart2Runnable);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
