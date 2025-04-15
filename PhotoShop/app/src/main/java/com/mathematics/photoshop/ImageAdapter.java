package com.mathematics.photoshop; // Declaring the package for the app

import android.content.Context; // Importing Context class for accessing resources and inflating layouts
import android.net.Uri; // Importing Uri class to represent the URI of images
import android.view.LayoutInflater; // Importing LayoutInflater to inflate layout XMLs into views
import android.view.View; // Importing View class for UI elements
import android.view.ViewGroup; // Importing ViewGroup for grouping the view items
import android.widget.ImageView; // Importing ImageView class to display images

import androidx.annotation.NonNull; // Importing NonNull annotation to indicate non-null method parameters
import androidx.recyclerview.widget.RecyclerView; // Importing RecyclerView class to create a scrollable list

import java.util.List; // Importing List class for holding the list of image items

// RecyclerView Adapter to display image items in a grid format
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final List<ImageItem> imageList; // List to hold the image items
    private final Context context;           // Context used to inflate layouts
    private OnItemClickListener onItemClickListener; // Click listener interface for item clicks

    // Constructor to initialize context and image list
    public ImageAdapter(Context context, List<ImageItem> imageList) {
        this.context = context; // Assign context passed in constructor
        this.imageList = imageList; // Assign image list passed in constructor
    }

    // This method is called when RecyclerView needs a new ViewHolder to display
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each image item
        View itemView = LayoutInflater.from(context).inflate(R.layout.image_layout, parent, false);
        return new ImageViewHolder(itemView); // Return a new ViewHolder with the inflated item view
    }

    // This method binds the data to the ViewHolder
    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        ImageItem imageItem = imageList.get(position); // Get the image item at the given position
        holder.imageView.setImageURI(imageItem.getImageUri()); // Set the image URI to ImageView

        // Set up a click listener for the item
        holder.itemView.setOnClickListener(v -> {
            // Check if the listener is not null and call onItemClick with the image URI
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(imageItem.getImageUri());
            }
        });
    }

    // This method returns the total number of image items in the list
    @Override
    public int getItemCount() {
        return imageList.size(); // Return the size of the image list
    }

    // ViewHolder class to hold references to the image view
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView; // Declare an ImageView to display each image item

        // Constructor to initialize the ImageView from the itemView
        public ImageViewHolder(View itemView) {
            super(itemView); // Call the parent class constructor
            imageView = itemView.findViewById(R.id.image_item); // Find the ImageView by ID from the layout
        }
    }

    // Interface to provide a callback when an image item is clicked
    public interface OnItemClickListener {
        void onItemClick(Uri imageUri); // Callback method with the URI of the clicked image
    }

    // Setter method for setting the item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener; // Set the listener
    }
}
