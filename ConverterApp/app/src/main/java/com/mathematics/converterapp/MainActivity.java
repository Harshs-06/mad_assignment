package com.mathematics.converterapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI components
    CardView settings;
    Spinner from;
    Spinner to;
    SharedPreferences pref; // To store theme settings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Retrieve stored theme preference before UI is drawn
        pref = getSharedPreferences("theme_pref", MODE_PRIVATE);
        boolean isDark = pref.getBoolean("dark_theme", false);

        // Apply dark or light mode based on preference
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Makes UI content draw under system bars
        setContentView(R.layout.activity_main); // Set layout

        // Load and display animated GIF using Glide
        ImageView gifimg = findViewById(R.id.gifimg);
        Glide.with(this)
                .asGif()
                .load(R.drawable.animation)
                .into(gifimg);

        // Apply custom font to title
        Typeface typeface = ResourcesCompat.getFont(this, R.font.amarante);
        TextView title = findViewById(R.id.title);
        title.setTypeface(typeface);

        // Initialize UI elements
        settings = findViewById(R.id.settings);
        from = findViewById(R.id.from);
        to = findViewById(R.id.to);

        // Set units list in spinners
        List<String> items = Arrays.asList("feet", "inches", "centimeters", "meters", "yards");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_layout, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        from.setAdapter(adapter);
        to.setAdapter(adapter);

        TextInputEditText user_value = findViewById(R.id.user_value); // Input box for value
        LinearLayout loadingview = findViewById(R.id.loadingview); // Loading animation layout
        LinearLayout mainbox = findViewById(R.id.linearLayout3); // Main conversion area
        LinearLayout resultlayout = findViewById(R.id.resultlayout); // Area to display result
        TextView resultview = findViewById(R.id.resultview); // TextView showing the result

        // Button to perform unit conversion
        Button convert_unit = findViewById(R.id.convert_unit);
        convert_unit.setOnClickListener(v -> {
            resultview.setText(""); // Clear old result

            // Disable all controls while conversion is processing
            mainbox.setEnabled(false);
            user_value.setEnabled(false);
            settings.setEnabled(false);
            from.setEnabled(false);
            to.setEnabled(false);
            convert_unit.setEnabled(false);
            resultlayout.setEnabled(false);
            loadingview.setVisibility(View.VISIBLE); // Show loading animation

            // Get selected units from spinners
            String fromUnit = from.getSelectedItem().toString();
            String toUnit = to.getSelectedItem().toString();
            double value = 0;

            // Check if input is empty
            if (!user_value.getText().toString().isEmpty()) {
                value = Double.parseDouble(user_value.getText().toString());
            } else {
                // Show message and re-enable all UI
                Toast.makeText(this, "Enter a value to convert!!", Toast.LENGTH_SHORT).show();
                loadingview.setVisibility(View.GONE);
                mainbox.setEnabled(true);
                resultlayout.setEnabled(true);
                user_value.setEnabled(true);
                from.setEnabled(true);
                to.setEnabled(true);
                convert_unit.setEnabled(true);
                settings.setEnabled(true);
                return;
            }

            // Call function to convert the units
            double converted = convertUnits(value, fromUnit, toUnit);

            // Simulate a delay to show loading for 5 seconds
            new Handler().postDelayed(() -> {
                loadingview.setVisibility(View.GONE); // Hide loading animation
                resultview.setText(String.valueOf(converted)); // Show result
                // Re-enable all controls
                mainbox.setEnabled(true);
                resultlayout.setEnabled(true);
                user_value.setEnabled(true);
                from.setEnabled(true);
                to.setEnabled(true);
                convert_unit.setEnabled(true);
                settings.setEnabled(true);
            }, 5000);
        });

        // Handle settings button click to open theme toggle sheet
        settings.setOnClickListener(v -> {
            BottomSheetDialog settings_sheet = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.settings_bottom_sheet, null);
            settings_sheet.setContentView(view);
            settings_sheet.show();

            // Update theme text and icon dynamically
            TextView themetext = view.findViewById(R.id.theme_text);
            ImageView themeimg = view.findViewById(R.id.theme_icon);

            boolean isDarkNow = pref.getBoolean("dark_theme", false);
            themetext.setText(isDarkNow ? "Dark" : "Light");
            themeimg.setImageResource(isDarkNow ? R.drawable.dark_icon : R.drawable.light_icon);

            // Toggle theme on click
            view.setOnClickListener(vv -> {
                boolean nowDark = !isDarkNow;
                pref.edit().putBoolean("dark_theme", nowDark).apply();
                AppCompatDelegate.setDefaultNightMode(nowDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                settings_sheet.dismiss(); // Close bottom sheet
            });
        });

        // Ensure proper padding for devices with cutouts, navigation bars, etc.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Converts the input value from one unit to another.
     * First converts to meters (as base), then converts to target unit.
     */
    private double convertUnits(double value, String from, String to) {
        double inMeters;

        // Step 1: Convert input unit to meters
        switch (from) {
            case "feet":
                inMeters = value * 0.3048;
                break;
            case "inches":
                inMeters = value * 0.0254;
                break;
            case "centimeters":
                inMeters = value * 0.01;
                break;
            case "meters":
                inMeters = value;
                break;
            case "yards":
                inMeters = value * 0.9144;
                break;
            default:
                inMeters = value; // fallback
        }

        // Step 2: Convert meters to desired output unit
        switch (to) {
            case "feet":
                return inMeters / 0.3048;
            case "inches":
                return inMeters / 0.0254;
            case "centimeters":
                return inMeters / 0.01;
            case "meters":
                return inMeters;
            case "yards":
                return inMeters / 0.9144;
            default:
                return inMeters; // fallback
        }
    }
}
