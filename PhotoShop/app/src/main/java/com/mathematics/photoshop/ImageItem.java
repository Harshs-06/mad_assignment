package com.mathematics.photoshop;

import android.net.Uri;

public class ImageItem {
    private Uri imageUri;
    public ImageItem(Uri imageUri) {
        this.imageUri = imageUri;
    }
    public Uri getImageUri() {
        return imageUri;
    }
}

