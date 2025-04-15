package com.mathematics.photoshop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.button.MaterialButton;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {

    ImageView imageView;
    TextView imgName, imgPath, imgSize, imgDate;
    MaterialButton deleteButton;
    Uri imageUri;
    DocumentFile documentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        imageView = findViewById(R.id.imageView);
        imgName = findViewById(R.id.imgname);
        imgPath = findViewById(R.id.imgpath);
        imgSize = findViewById(R.id.imgsize);
        imgDate = findViewById(R.id.imgdate);
        deleteButton = findViewById(R.id.deleteButton);

        imageUri = getIntent().getData();

        if (imageUri != null) {
            documentFile = DocumentFile.fromSingleUri(this, imageUri);

            if (documentFile != null && documentFile.exists()) {
                imageView.setImageURI(imageUri);
                imgName.setText(documentFile.getName());
                imgPath.setText(documentFile.getUri().getPath());

                // Get the file size
                long sizeInBytes = documentFile.length();
                String sizeText = android.text.format.Formatter.formatShortFileSize(this, sizeInBytes);
                imgSize.setText(sizeText);

                // Get the last modified date
                long lastModified = documentFile.lastModified();
                String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(lastModified));
                imgDate.setText(formattedDate);
            } else {
                // If the document file is invalid or doesn't exist
                showFileNotFound();
            }
        }

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (documentFile != null && documentFile.delete()) {
                            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("imageDeleted", true);
                            resultIntent.setData(imageUri);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showFileNotFound() {
        imageView.setImageResource(R.drawable.file_not_found_img);
        imgName.setText("Image Not Found");
        imgPath.setText("File has been deleted");
        imgSize.setText("N/A");
        imgDate.setText("N/A");
        deleteButton.setEnabled(false);
    }
}
