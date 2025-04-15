package com.mathematics.photoshop;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enables drawing behind the system bars (status bar, navigation bar)
        EdgeToEdge.enable(this);

        // Sets the layout for this screen to 'activity_main.xml'
        setContentView(R.layout.activity_main);

        // Get references to the camera and gallery card views from the layout
        MaterialCardView camera_button = findViewById(R.id.camera_button);
        MaterialCardView gallery_button = findViewById(R.id.gallery_button);

        // Set a click listener on the camera button
        camera_button.setOnClickListener(v -> {
            // When clicked, start the CameraActivity
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        // Set a click listener on the gallery button
        gallery_button.setOnClickListener(v -> {
            // When clicked, start the GalleryActivity
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        // Makes sure the layout avoids overlapping with system UI areas (notch, status bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Get the safe insets for system bars
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply padding to the view to avoid content being cut off
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
