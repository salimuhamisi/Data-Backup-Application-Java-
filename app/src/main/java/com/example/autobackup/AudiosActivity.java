package com.example.autobackup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class AudiosActivity extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST=1;

    private Button choose;
    private Button upload;
    private TextView uploads;
    private EditText name;
    private TextView text;

    ProgressDialog progressDialog;

    private Uri audioUri;
    String downloadUrl;

    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audios);
        progressDialog = new ProgressDialog(this);

        choose = findViewById(R.id.choose);
        upload = findViewById(R.id.upload);
        uploads = findViewById(R.id.uploads);
        name = findViewById(R.id.file_name);
        text = findViewById(R.id.text_view);

        storageRef = FirebaseStorage.getInstance().getReference("Audios");
        databaseRef = FirebaseDatabase.getInstance().getReference("Audios");

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioUri != null) {
                    uploadAudio(audioUri);
                }
                else {
                    Toast.makeText(AudiosActivity.this, "Please select audio", Toast.LENGTH_SHORT).show();
                }
            }
        });

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fileChooser();
            }
        });

        uploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                retriveAudio();
            }
        });
    }

    private void retriveAudio() {

        Intent intent = new Intent(this, Retrieve_AudioActivity.class);
        startActivity(intent);
    }

    private void uploadAudio(Uri audioUri) {

        progressDialog.show();
        StorageReference fileReference=storageRef.child(System.currentTimeMillis()+ "."+ getFileExtension(audioUri));

        mUploadTask = fileReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Upload upload= new Upload(name.getText().toString().trim(),uri.toString());
                        String uploadId=databaseRef.push().getKey();
                        databaseRef.child(uploadId).setValue(upload);
                        Toast.makeText(AudiosActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AudiosActivity.this, "Uploading Failed", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }
    private void fileChooser() {
        Intent intent= new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            audioUri=data.getData();
            text.setText("file is selected: "+ data.getData().getLastPathSegment());
        }
    }
}