package com.mathematics.photoshop;

import android.net.Uri;

// Model class representing a single image item
public class ImageItem {
    private Uri imageUri; // URI of the image

    // Constructor to initialize the image URI
    public ImageItem(Uri imageUri) {
        this.imageUri = imageUri;
    }

    // Getter method to retrieve the image URI
    public Uri getImageUri() {
        return imageUri;
    }
}
