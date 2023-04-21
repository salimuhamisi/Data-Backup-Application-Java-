package com.example.autobackup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Member;

public class VideosActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST=1;

    private Button choose;
    private Button upload;
    private TextView uploads;
    private EditText name;
    private VideoView videoView;
    MediaController mediacontroller;
    Member member;

    ProgressDialog progressDialog;

    private Uri videoUri;

    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    private UploadTask uploadTask;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        progressDialog = new ProgressDialog(this);

        choose = findViewById(R.id.choose);
        upload = findViewById(R.id.upload);
        uploads = findViewById(R.id.uploads);
        name = findViewById(R.id.file_name);
        videoView = findViewById(R.id.video_view);

        mediacontroller=new MediaController(this);
        videoView.setMediaController(mediacontroller);
        videoView.start();

        storageRef = FirebaseStorage.getInstance().getReference("Videos");
        databaseRef = FirebaseDatabase.getInstance().getReference("Videos");

    choose.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            chooseFile();
        }
    });

    upload.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (videoUri != null) {
                uploadVideo(videoUri);
            }
            else {
                Toast.makeText(VideosActivity.this, "Please select video", Toast.LENGTH_SHORT).show();
            }
        }
    });

        uploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewUploads();

            }
        });
    }

    private void viewUploads() {
        Intent intent = new Intent(this, RetriveVideosActivity.class);
        startActivity(intent);
    }

    private void uploadVideo(Uri videoUri) {
        progressDialog.show();
        StorageReference fileReference=storageRef.child(System.currentTimeMillis()+ "."+ getFileExtension(videoUri));

        mUploadTask = fileReference.putFile(videoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Upload upload= new Upload(name.getText().toString().trim(),uri.toString());
                        String uploadId=databaseRef.push().getKey();
                        databaseRef.child(uploadId).setValue(upload);
                        Toast.makeText(VideosActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }
                });

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double pr= (100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                progressDialog.setMessage("Uploading "+(int)pr + "%");


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(VideosActivity.this, "Uploading Failed", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void chooseFile() {
        Intent intent= new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            videoUri=data.getData();
            videoView.setVideoURI(videoUri);
        }
    }
}