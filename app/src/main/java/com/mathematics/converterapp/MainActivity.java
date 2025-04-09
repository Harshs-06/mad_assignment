package com.mathematics.converterapp;

import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ImageView gifimg = findViewById(R.id.gifimg);
        Glide.with(this)
                        .asGif()
                                .load(R.drawable.animation)
                                        .into(gifimg);

        Typeface typeface = ResourcesCompat.getFont(this,R.font.amarante);
        TextView title = findViewById(R.id.title);

        title.setTypeface(typeface);


        Spinner from = findViewById(R.id.from);
        Spinner to = findViewById(R.id.to);
        List<String> items = Arrays.asList("feet", "inches", "centimeters","meters","yards");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_layout, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        from.setAdapter(adapter);
        to.setAdapter(adapter);
        TextInputEditText user_value = findViewById(R.id.user_value);
        LinearLayout loadingview = findViewById(R.id.loadingview);
        LinearLayout mainbox = findViewById(R.id.linearLayout3);
        LinearLayout resultlayout = findViewById(R.id.resultlayout);
        TextView resultview = findViewById(R.id.resultview);
        Button convert_unit = findViewById(R.id.convert_unit);
        convert_unit.setOnClickListener(v -> {
            resultview.setText("");
            mainbox.setEnabled(false);
            resultlayout.setEnabled(false);
            loadingview.setVisibility(View.VISIBLE);
            String fromUnit = from.getSelectedItem().toString();
            String toUnit = to.getSelectedItem().toString();
            double value=0;
            if(!user_value.getText().toString().isEmpty()){
                value = Double.parseDouble(user_value.getText().toString());
            }
            else {
                Toast.makeText(this,"Enter a value to convert!!",Toast.LENGTH_SHORT).show();
                loadingview.setVisibility(View.GONE);
                mainbox.setEnabled(true);
                resultlayout.setEnabled(true);
                return;
            }
            double converted = convertUnits(value, fromUnit, toUnit);
            new Handler().postDelayed(() -> {
                loadingview.setVisibility(View.GONE);
                resultview.setText(String.valueOf(converted));
                mainbox.setEnabled(true);
                resultlayout.setEnabled(true);
            }, 5000);


        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private double convertUnits(double value, String from, String to) {

        double inMeters;
        switch (from) {
            case "feet": inMeters = value * 0.3048; break;
            case "inches": inMeters = value * 0.0254; break;
            case "centimeters": inMeters = value * 0.01; break;
            case "meters": inMeters = value; break;
            case "yards": inMeters = value * 0.9144; break;
            default: inMeters = value;
        }


        switch (to) {
            case "feet": return inMeters / 0.3048;
            case "inches": return inMeters / 0.0254;
            case "centimeters": return inMeters / 0.01;
            case "meters": return inMeters;
            case "yards": return inMeters / 0.9144;
            default: return inMeters;
        }
    }

}