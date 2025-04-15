// Package declaration
package com.mathematics.photoshop;

// Imports
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    // Permission and intent request codes
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_CODE_PICK_FOLDER = 101;

    // UI elements
    private PreviewView previewView;
    private CardView showview;
    private ImageView showimg, photoClick;
    private MaterialButton recapture, done;

    // CameraX and threading components
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;

    // For photo capture and saving
    private File tempPhotoFile;
    private Bitmap capturedBitmap;
    private Uri pickedFolderUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera); // Set layout

        // Initialize views
        previewView = findViewById(R.id.previewView);
        recapture = findViewById(R.id.recapture);
        done = findViewById(R.id.done);
        photoClick = findViewById(R.id.photo_click);
        showview = findViewById(R.id.showview);
        showimg = findViewById(R.id.showimg);

        // Disable action buttons initially
        recapture.setEnabled(false);
        done.setEnabled(false);

        // Create background thread for camera operations
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera(); // Start camera if permission granted
        }

        // Capture button action
        photoClick.setOnClickListener(v -> preparePhoto());

        // Recapture action
        recapture.setOnClickListener(v -> {
            recapture.setEnabled(false);
            done.setEnabled(false);
            previewView.setVisibility(View.VISIBLE);
            showview.setVisibility(View.GONE);
            Toast.makeText(this, "You can retake the photo now", Toast.LENGTH_SHORT).show();
        });

        // Done action: open folder picker
        done.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER);
        });

        // Handle full screen padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera(); // Restart camera after permission granted
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Start CameraX preview
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll(); // Clear previous bindings
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Capture image and show preview
    private void preparePhoto() {
        try {
            // Temporary file to store captured photo
            tempPhotoFile = File.createTempFile("temp_photo", ".jpg", getCacheDir());
            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(tempPhotoFile).build();

            // Capture photo
            imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    runOnUiThread(() -> {
                        previewView.setVisibility(View.GONE);
                        showview.setVisibility(View.VISIBLE);
                        capturedBitmap = BitmapFactory.decodeFile(tempPhotoFile.getAbsolutePath());
                        showimg.setImageBitmap(capturedBitmap);
                        Toast.makeText(CameraActivity.this, "Preview ready. Click Done to save.", Toast.LENGTH_SHORT).show();
                        recapture.setEnabled(true);
                        done.setEnabled(true);
                    });
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Preview failed", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handle folder selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == RESULT_OK && data != null) {
            pickedFolderUri = data.getData();
            if (pickedFolderUri != null && capturedBitmap != null) {
                saveBitmapToFolder(pickedFolderUri, capturedBitmap);
            }
        }
    }

    // Save bitmap to selected folder using SAF
    private void saveBitmapToFolder(Uri folderUri, Bitmap bitmap) {
        try {
            // Grant access permanently to the folder
            getContentResolver().takePersistableUriPermission(folderUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create file and write bitmap
            String name = "IMG_" + System.currentTimeMillis() + ".jpg";
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, folderUri);
            DocumentFile file = pickedDir.createFile("image/jpeg", name);

            OutputStream out = getContentResolver().openOutputStream(file.getUri());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

            Toast.makeText(this, "Saved to selected folder", Toast.LENGTH_SHORT).show();

            // Reset UI after save
            showimg.setImageBitmap(null);
            showview.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
            recapture.setEnabled(false);
            done.setEnabled(false);
            tempPhotoFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Saving failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Shutdown executor on activity destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
