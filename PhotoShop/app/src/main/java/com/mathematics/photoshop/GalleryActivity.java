// Package declaration
package com.mathematics.photoshop;

// Imports: These libraries are necessary for UI, file handling, RecyclerView, and other components.
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    // UI components: Declaring views for the gallery and folder selection
    private RecyclerView recyclerView; // RecyclerView to show images in a grid
    private ImageAdapter imageAdapter;  // Adapter for managing image items in the RecyclerView
    private List<ImageItem> imageList = new ArrayList<>(); // List to hold image items
    private ActivityResultLauncher<Intent> folderPickerLauncher; // ActivityResultLauncher for folder picker
    private ActivityResultLauncher<Intent> imageDetailLauncher; // ActivityResultLauncher for image details (like deleting)
    private TextView folderNameView; // TextView to display the name of the selected folder
    private LinearLayout folderSelectLayout; // Layout containing folder select button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge layout for modern UI
        setContentView(R.layout.activity_gallery); // Set layout for this activity

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.images_recycler_view); // Get reference to RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // Use GridLayoutManager with 3 columns
        imageAdapter = new ImageAdapter(this, imageList); // Create an adapter with image list
        recyclerView.setAdapter(imageAdapter); // Set the adapter to RecyclerView

        // Initialize folder name and folder select layout UI components
        folderNameView = findViewById(R.id.folder_name); // Get reference to folder name display
        folderSelectLayout = findViewById(R.id.folder_select_layout); // Get reference to folder selection layout

        // When the user clicks the folder select layout, open the folder picker
        folderSelectLayout.setOnClickListener(view -> openFolderPicker());

        // Handle folder picking activity result
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), // Register launcher for folder picker
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri folderUri = result.getData().getData(); // Get URI of selected folder
                        if (folderUri != null) {
                            loadImagesFromFolder(folderUri); // Load images from selected folder

                            // Extract and display the folder name
                            String fullPath = folderUri.getLastPathSegment();
                            String folderName = fullPath != null ? fullPath.substring(fullPath.lastIndexOf(":") + 1) : "Selected Folder";
                            folderNameView.setText(folderName); // Display folder name in UI
                        }
                    }
                }
        );

        // Handle the result from ImageDetailActivity, e.g., image deletion
        imageDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), // Register launcher for image detail activity
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri deletedUri = result.getData().getData(); // Get URI of the deleted image
                        if (deletedUri != null) {
                            // Remove deleted image from the list and update RecyclerView
                            imageList.removeIf(item -> item.getImageUri().equals(deletedUri));
                            imageAdapter.notifyDataSetChanged(); // Notify adapter of data change
                            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show(); // Show delete confirmation
                        }
                    }
                }
        );

        // Apply padding to system bars (status bar and navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // Set padding to avoid system bars overlap
            return insets;
        });

        // When an image is clicked, open the ImageDetailActivity
        imageAdapter.setOnItemClickListener(uri -> {
            Intent intent = new Intent(GalleryActivity.this, ImageDetailActivity.class);
            intent.setData(uri); // Pass the URI of the clicked image to ImageDetailActivity
            imageDetailLauncher.launch(intent); // Launch the image detail activity
        });
    }

    // Launches folder picker using Storage Access Framework (SAF)
    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE); // Intent to open folder picker
        folderPickerLauncher.launch(intent); // Launch folder picker activity
    }

    // Loads images from a selected folder using DocumentFile API
    private void loadImagesFromFolder(Uri folderUri) {
        imageList.clear(); // Clear any previously loaded images
        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri); // Get DocumentFile instance for the selected folder
        if (folder != null && folder.isDirectory()) {
            // Iterate over files in the folder
            for (DocumentFile file : folder.listFiles()) {
                if (file.isFile() && file.getType() != null && file.getType().startsWith("image/")) {
                    imageList.add(new ImageItem(file.getUri())); // Add valid image files to the list
                }
            }
        }
        imageAdapter.notifyDataSetChanged(); // Notify the adapter that data has changed, triggering a UI update
    }
}
