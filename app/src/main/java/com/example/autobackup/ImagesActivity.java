package com.example.autobackup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.util.jar.Attributes;

public class ImagesActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private Button choose;
    private Button upload;
    private TextView uploads;
    private EditText name;
    private ImageView view;
    ProgressDialog progressDialog;

    private Uri imageUri;
    String downloadUrl;

    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        progressDialog = new ProgressDialog(this);

        choose = findViewById(R.id.choose);
        upload = findViewById(R.id.upload);
        uploads = findViewById(R.id.uploads);
        name = findViewById(R.id.name);
        view = findViewById(R.id.view);

        storageRef = FirebaseStorage.getInstance().getReference("Images");
        databaseRef = FirebaseDatabase.getInstance().getReference("Images");

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileChooser();

            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    uploadFile(imageUri);
                }
                else {
                    Toast.makeText(ImagesActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(this, ImgActivity.class);
        startActivity(intent);
    }

    private void uploadFile(Uri uri) {
        progressDialog.show();
        StorageReference fileReference=storageRef.child(System.currentTimeMillis()+ "."+ getFileExtension(uri));

        mUploadTask = fileReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Upload upload= new Upload(name.getText().toString().trim(),uri.toString());
                        String uploadId=databaseRef.push().getKey();
                        databaseRef.child(uploadId).setValue(upload);
                        Toast.makeText(ImagesActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ImagesActivity.this, "Uploading Failed", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr= getContentResolver();
        MimeTypeMap mime= MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void fileChooser() {
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
        && data != null && data.getData() != null) {
            imageUri=data.getData();
            Picasso.with(this).load(imageUri).into(view);
        }
    }
}