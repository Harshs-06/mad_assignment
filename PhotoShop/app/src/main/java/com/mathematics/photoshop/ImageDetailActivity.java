package com.mathematics.photoshop; // Package declaration for the app

import android.content.Intent; // Importing Intent to handle activity transitions
import android.net.Uri; // Importing Uri to work with content URIs
import android.os.Bundle; // Importing Bundle to pass data between activities
import android.widget.ImageView; // Importing ImageView to display the image
import android.widget.TextView; // Importing TextView to display text details
import android.widget.Toast; // Importing Toast for short messages

import androidx.appcompat.app.AlertDialog; // Importing AlertDialog to show confirmation dialogs
import androidx.appcompat.app.AppCompatActivity; // Importing AppCompatActivity for activity lifecycle
import androidx.core.graphics.Insets; // Importing Insets for handling system UI
import androidx.core.view.ViewCompat; // Importing ViewCompat for handling window insets
import androidx.core.view.WindowInsetsCompat; // Importing WindowInsetsCompat to adjust UI padding
import androidx.documentfile.provider.DocumentFile; // Importing DocumentFile to work with documents

import com.google.android.material.button.MaterialButton; // Importing MaterialButton for a styled button

import java.text.SimpleDateFormat; // Importing SimpleDateFormat for date formatting
import java.util.Date; // Importing Date to work with time-related data
import java.util.Locale; // Importing Locale for locale-specific formatting

public class ImageDetailActivity extends AppCompatActivity {

    // UI components for displaying the image and its details
    ImageView imageView; // ImageView to display the selected image
    TextView imgName, imgPath, imgSize, imgDate; // TextViews to display image name, path, size, and date
    MaterialButton deleteButton; // MaterialButton to delete the image

    // URI of the image and its corresponding DocumentFile
    Uri imageUri; // URI that represents the image location
    DocumentFile documentFile; // DocumentFile to perform file operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call to the parent class onCreate method
        setContentView(R.layout.activity_image_detail); // Set the layout for the activity

        // Initialize all UI elements from the layout
        imageView = findViewById(R.id.imageView); // Find the ImageView to display the image
        imgName = findViewById(R.id.imgname); // Find the TextView to display the image name
        imgPath = findViewById(R.id.imgpath); // Find the TextView to display the image path
        imgSize = findViewById(R.id.imgsize); // Find the TextView to display the image size
        imgDate = findViewById(R.id.imgdate); // Find the TextView to display the image date
        deleteButton = findViewById(R.id.deleteButton); // Find the MaterialButton for delete action

        // Retrieve the image URI passed from the previous activity
        imageUri = getIntent().getData(); // Get the URI of the image passed through Intent

        if (imageUri != null) { // If the URI is not null
            // Create a DocumentFile from the URI to access metadata and perform file operations
            documentFile = DocumentFile.fromSingleUri(this, imageUri); // Create DocumentFile from the URI

            if (documentFile != null && documentFile.exists()) { // If the DocumentFile exists and is valid
                // Set the image in the ImageView
                imageView.setImageURI(imageUri); // Display the image using the URI

                // Display image name
                imgName.setText(documentFile.getName()); // Set the image name from the DocumentFile

                // Show URI path (note: this may be a content URI, not an actual filesystem path)
                imgPath.setText(documentFile.getUri().getPath()); // Display the path of the URI

                // Get and format the image size
                long sizeInBytes = documentFile.length(); // Get the size of the image in bytes
                String sizeText = android.text.format.Formatter.formatShortFileSize(this, sizeInBytes); // Format the size to a readable format
                imgSize.setText(sizeText); // Display the formatted size

                // Get and format the last modified date
                long lastModified = documentFile.lastModified(); // Get the last modified time of the image
                String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Format the date to a readable format
                        .format(new Date(lastModified)); // Format the last modified time as a string
                imgDate.setText(formattedDate); // Display the formatted date

            } else { // If the file doesn't exist or can't be accessed
                showFileNotFound(); // Call method to show file not found UI
            }
        }

        // Set up delete button click listener with a confirmation dialog
        deleteButton.setOnClickListener(v -> {
            // Display confirmation dialog before deletion
            new AlertDialog.Builder(this) // Create a new AlertDialog builder
                    .setTitle("Delete Image") // Set the title of the dialog
                    .setMessage("Are you sure you want to delete this image?") // Set the message
                    .setPositiveButton("Yes", (dialog, which) -> { // If user clicks "Yes"
                        // Attempt to delete the file if the user confirms
                        if (documentFile != null && documentFile.delete()) { // If the document is deleted successfully
                            // Show a toast notification indicating successful deletion
                            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();

                            // Notify the previous activity about the deletion
                            Intent resultIntent = new Intent(); // Create an intent to send the result back
                            resultIntent.putExtra("imageDeleted", true); // Add an extra to indicate deletion
                            resultIntent.setData(imageUri); // Set the URI of the deleted image
                            setResult(RESULT_OK, resultIntent); // Set the result as OK
                            finish(); // Close the current activity
                        } else { // If deletion fails
                            // Show a toast notification
                            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null) // Dismiss dialog if "Cancel" is pressed
                    .show(); // Show the dialog
        });

        // Adjust padding to handle system UI like status/navigation bars properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Get system UI insets (e.g., status bar, navigation bar)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply padding to adjust content for system UI
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets; // Return the modified insets
        });
    }

    // This method is called when the image file isn't found or accessible
    private void showFileNotFound() {
        // Display a placeholder image when the file is not found
        imageView.setImageResource(R.drawable.file_not_found_img); // Set the ImageView to a "file not found" placeholder image
        imgName.setText("Image Not Found"); // Set fallback name
        imgPath.setText("File has been deleted"); // Set fallback path
        imgSize.setText("N/A"); // Set fallback size
        imgDate.setText("N/A"); // Set fallback date
        deleteButton.setEnabled(false); // Disable delete button when the image is not found
    }
}
