// Package declaration
package com.mathematics.photoshop;

// Imports section for necessary libraries
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
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;  // Request code for camera permission
    private static final int REQUEST_CODE_PICK_FOLDER = 101;  // Request code for folder selection

    // UI elements for the camera view and actions
    private PreviewView previewView;  // Preview view for showing camera input
    private CardView showview;  // CardView for displaying captured image
    private ImageView showimg, photoClick;  // ImageView to show captured image and click button for photo
    private MaterialButton recapture, done;  // Buttons for recapturing or finishing the process

    // CameraX and threading components
    private ImageCapture imageCapture;  // ImageCapture instance for capturing photos
    private ProcessCameraProvider cameraProvider;  // Camera provider to bind camera lifecycle
    private ExecutorService cameraExecutor;  // Executor service for running camera tasks in background

    // For photo capture and saving
    private File tempPhotoFile;  // Temporary file to store the captured photo
    private Bitmap capturedBitmap;  // Bitmap to hold captured image
    private Uri pickedFolderUri;  // URI of the folder where the image will be saved

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);  // Enables edge-to-edge layout for full-screen experience
        setContentView(R.layout.activity_camera); // Sets the layout of the activity

        // Initialize the UI components
        previewView = findViewById(R.id.previewView);
        recapture = findViewById(R.id.recapture);
        done = findViewById(R.id.done);
        photoClick = findViewById(R.id.photo_click);
        showview = findViewById(R.id.showview);
        showimg = findViewById(R.id.showimg);

        // Initially disable action buttons (recapture and done)
        recapture.setEnabled(false);
        done.setEnabled(false);

        // Set up background thread to handle camera tasks
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Check if the app has camera permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);  // Request camera permission
        } else {
            startCamera();  // Start the camera if permission is already granted
        }

        // Set up photo click button action to capture the photo
        photoClick.setOnClickListener(v -> preparePhoto());

        // Set up recapture button action to allow retaking a photo
        recapture.setOnClickListener(v -> {
            recapture.setEnabled(false);  // Disable recapture button after use
            done.setEnabled(false);  // Disable done button until the photo is retaken
            previewView.setVisibility(View.VISIBLE);  // Show the preview view again
            showview.setVisibility(View.GONE);  // Hide the captured image view
            Toast.makeText(this, "You can retake the photo now", Toast.LENGTH_SHORT).show();  // Inform the user
        });

        // Set up done button action to open the folder picker for saving the photo
        done.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);  // Intent to open folder picker
            startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER);  // Start folder picker activity
        });

        // Handle full-screen padding adjustments for system UI (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());  // Get system bar insets
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);  // Apply padding
            return insets;  // Return updated insets
        });
    }

    // Handle the result of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();  // Restart camera after permission is granted
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();  // Inform the user if permission is denied
            }
        }
    }

    // Start the camera using CameraX
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);  // Get CameraProvider instance
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();  // Get the camera provider
                Preview preview = new Preview.Builder().build();  // Create a Preview instance for camera input
                imageCapture = new ImageCapture.Builder().build();  // Create an ImageCapture instance for taking photos
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;  // Select the back camera

                cameraProvider.unbindAll();  // Unbind previous camera use cases
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);  // Bind camera to lifecycle
                preview.setSurfaceProvider(previewView.getSurfaceProvider());  // Set the surface provider for the preview view
            } catch (Exception e) {
                e.printStackTrace();  // Print exception if camera setup fails
            }
        }, ContextCompat.getMainExecutor(this));  // Run on main executor (UI thread)
    }

    // Prepare the photo by capturing it
    private void preparePhoto() {
        try {
            // Create a temporary file to store the photo
            tempPhotoFile = File.createTempFile("temp_photo", ".jpg", getCacheDir());
            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(tempPhotoFile).build();  // Set options for saving the photo

            // Take the picture
            imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    runOnUiThread(() -> {
                        previewView.setVisibility(View.GONE);  // Hide the preview view after photo is taken
                        showview.setVisibility(View.VISIBLE);  // Show the captured image view
                        capturedBitmap = BitmapFactory.decodeFile(tempPhotoFile.getAbsolutePath());  // Decode the captured photo file into a bitmap
                        showimg.setImageBitmap(capturedBitmap);  // Set the captured bitmap into the ImageView
                        Toast.makeText(CameraActivity.this, "Preview ready. Click Done to save.", Toast.LENGTH_SHORT).show();  // Inform the user
                        recapture.setEnabled(true);  // Enable recapture button
                        done.setEnabled(true);  // Enable done button
                    });
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Preview failed", Toast.LENGTH_SHORT).show());  // Show error message if capture fails
                }
            });
        } catch (Exception e) {
            e.printStackTrace();  // Print any exceptions during photo preparation
        }
    }

    // Handle the folder selection result after the user picks a folder
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == RESULT_OK && data != null) {
            pickedFolderUri = data.getData();  // Get the selected folder URI
            if (pickedFolderUri != null && capturedBitmap != null) {
                saveBitmapToFolder(pickedFolderUri, capturedBitmap);  // Save the captured image to the selected folder
            }
        }
    }

    // Save the bitmap to the selected folder using the Storage Access Framework (SAF)
    private void saveBitmapToFolder(Uri folderUri, Bitmap bitmap) {
        try {
            // Grant write and read permissions to the selected folder URI
            getContentResolver().takePersistableUriPermission(folderUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create a file in the selected folder and write the bitmap
            String name = "IMG_" + System.currentTimeMillis() + ".jpg";  // Generate a unique file name
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, folderUri);  // Get the folder as DocumentFile
            DocumentFile file = pickedDir.createFile("image/jpeg", name);  // Create a new JPEG file in the folder

            OutputStream out = getContentResolver().openOutputStream(file.getUri());  // Open an output stream to the file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  // Compress the bitmap and write it to the file
            out.close();  // Close the output stream

            Toast.makeText(this, "Saved to selected folder", Toast.LENGTH_SHORT).show();  // Notify the user that the image was saved

            // Reset UI after saving the image
            showimg.setImageBitmap(null);  // Clear the image preview
            showview.setVisibility(View.GONE);  // Hide the image view
            previewView.setVisibility(View.VISIBLE);  // Show the preview view again
            recapture.setEnabled(false);  // Disable the recapture button
            done.setEnabled(false);  // Disable the done button
            tempPhotoFile.delete();  // Delete the temporary photo file
        } catch (Exception e) {
            e.printStackTrace();  // Print exception if saving fails
            Toast.makeText(this, "Saving failed", Toast.LENGTH_SHORT).show();  // Notify the user of the failure
        }
    }

    // Shutdown executor when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();  // Shutdown the camera executor to free resources
    }
}
