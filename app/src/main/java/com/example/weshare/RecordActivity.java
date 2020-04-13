package com.example.weshare;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RecordActivity extends AppCompatActivity {

    private final int RQ_VIDEO_CAP = 90;
    private final int RQ_PERMISSIONS = 91;

    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;

    TextView message;
    Button capture;

    private static final String TAG = "RecordActivity";

    class PermissionsException extends Exception {

        @Nullable
        @Override
        public String getMessage() {
            return "Permissions not granted. Please grant permission to proceed further";
        }
    }

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        message = findViewById(R.id.message);
        capture = findViewById(R.id.capture);
        capture.setVisibility(View.GONE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Video");
        progressDialog.setCancelable(false);

        openCamera();

        capture.setOnClickListener(v -> openCamera());
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RecordActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    RQ_PERMISSIONS);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            startActivityForResult(intent, RQ_VIDEO_CAP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQ_VIDEO_CAP) {
            if (resultCode == Activity.RESULT_OK) {
                uploadToFireStorage(data.getData());
            } else {
                errorShow(new Exception() {
                    @Nullable
                    @Override
                    public String getMessage() {
                        return "Unable to capture video. Try again";
                    }
                });
            }
        }
    }

    private void uploadToFireStorage(Uri data) {
        progressDialog.show();
        StorageReference ref = firebaseStorage.getReference().child(FirebaseAuth.getInstance().getUid() + "__" + System.currentTimeMillis() + "");
        UploadTask uploadTask = ref.putFile(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful() && task.getException() != null) {
                errorShow(task.getException());
            }

            // Continue with the task to get the download URL
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                updateFirestone(downloadUri.toString());
            } else {
                errorShow(task.getException());
            }
        });
    }

    private void errorShow(Exception exception) {
        progressDialog.dismiss();
        Snackbar.make(findViewById(android.R.id.content),
                "Exception: " + exception.getMessage(), Snackbar.LENGTH_LONG).show();
        capture.setVisibility(View.VISIBLE);
        message.setText("Exception: " + exception.getMessage());
    }

    private void updateFirestone(String url) {
        VideoData videoData = new VideoData("Anonymous", url, "Anonymous", Timestamp.now(), true);
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Const.COLLECTION_PATH);
        collectionReference.document(collectionReference.document().getId())
                .set(videoData).addOnCompleteListener(task1 -> {

            if (task1.isSuccessful()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Uploaded Successfully", Snackbar.LENGTH_LONG).show();
                capture.setVisibility(View.VISIBLE);
                message.setText("Click to upload more");
                progressDialog.dismiss();
            } else {
                errorShow(task1.getException());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case RQ_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    errorShow(new PermissionsException());
                }
                return;
            }
        }
    }
}
