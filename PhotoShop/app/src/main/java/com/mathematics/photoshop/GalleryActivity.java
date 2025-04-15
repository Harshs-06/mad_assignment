package com.mathematics.photoshop;

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

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<ImageItem> imageList = new ArrayList<>();
    private ActivityResultLauncher<Intent> folderPickerLauncher;
    private ActivityResultLauncher<Intent> imageDetailLauncher;
    private TextView folderNameView;
    private LinearLayout folderSelectLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.images_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new ImageAdapter(this, imageList);
        recyclerView.setAdapter(imageAdapter);

        folderNameView = findViewById(R.id.folder_name);
        folderSelectLayout = findViewById(R.id.folder_select_layout);

        folderSelectLayout.setOnClickListener(view -> openFolderPicker());

        // Register the folder picker activity result launcher
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri folderUri = result.getData().getData();
                        if (folderUri != null) {
                            loadImagesFromFolder(folderUri);

                            // Extract folder name and set it in the UI
                            String fullPath = folderUri.getLastPathSegment();
                            String folderName = fullPath != null ? fullPath.substring(fullPath.lastIndexOf(":") + 1) : "Selected Folder";
                            folderNameView.setText(folderName);
                        }
                    }
                }
        );

        // Register the image detail activity result launcher
        imageDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri deletedUri = result.getData().getData();
                        if (deletedUri != null) {
                            // Remove the deleted image from the list
                            imageList.removeIf(item -> item.getImageUri().equals(deletedUri));
                            imageAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Adjust system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set on click listener for each image in the RecyclerView to launch ImageDetailActivity
        imageAdapter.setOnItemClickListener(uri -> {
            Intent intent = new Intent(GalleryActivity.this, ImageDetailActivity.class);
            intent.setData(uri);
            imageDetailLauncher.launch(intent);
        });
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderPickerLauncher.launch(intent);
    }

    private void loadImagesFromFolder(Uri folderUri) {
        imageList.clear();
        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
        if (folder != null && folder.isDirectory()) {
            for (DocumentFile file : folder.listFiles()) {
                if (file.isFile() && file.getType() != null && file.getType().startsWith("image/")) {
                    imageList.add(new ImageItem(file.getUri()));
                }
            }
        }
        imageAdapter.notifyDataSetChanged();
    }
}
