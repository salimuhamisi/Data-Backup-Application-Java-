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

public class DocumentsActivity extends AppCompatActivity {
    private static final int PICK_PDF_REQUEST = 1;
    Button chooseFile, upload;
    TextView notification, uploads;
    private EditText name;
    Uri pdfUri;
    ProgressDialog progressDialog;

    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    private StorageTask mUploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        progressDialog = new ProgressDialog(this);

        storageRef = FirebaseStorage.getInstance().getReference("Documents");
        databaseRef = FirebaseDatabase.getInstance().getReference("Documents");

        chooseFile=findViewById(R.id.choose);
        upload= findViewById(R.id.upload);
        notification=findViewById(R.id.notification);
        name = findViewById(R.id.file_name);
        uploads=findViewById(R.id.uploads);

        chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileChooser();

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pdfUri != null) {
                    uploadFile(pdfUri);
                }
                else {
                    Toast.makeText(DocumentsActivity.this, "Please select a document", Toast.LENGTH_SHORT).show();
                }
            }
        });

        uploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seePDF();

            }
        });
    }

    private void seePDF() {
        Intent intent = new Intent(this, RetrivepdfActivity.class);
        startActivity(intent);
    }


    private void uploadFile(Uri pdfUri) {
        progressDialog.show();
        StorageReference fileReference=storageRef.child(System.currentTimeMillis()+ ".pdf"+ getFileExtension(pdfUri));

        mUploadTask = fileReference.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Upload upload= new Upload(name.getText().toString().trim(),uri.toString());
                        String uploadId=databaseRef.push().getKey();
                        databaseRef.child(uploadId).setValue(upload);
                        Toast.makeText(DocumentsActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(DocumentsActivity.this, "Uploading Failed", Toast.LENGTH_SHORT).show();

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
        intent.setType("pdf/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            pdfUri=data.getData();
            notification.setText("file is selected: "+ data.getData().getLastPathSegment());
        }
    }
}