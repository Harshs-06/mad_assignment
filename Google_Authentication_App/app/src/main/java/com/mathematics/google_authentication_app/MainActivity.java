package com.mathematics.google_authentication_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // Google sign-in client to handle logout
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable drawing under system bars
        setContentView(R.layout.activity_main); // Set the main layout

        // Get reference to TextView to display user's name
        TextView nametext = findViewById(R.id.name);

        // Get the user's name from the previous activity (passed via intent)
        String name = getIntent().getStringExtra("name");
        nametext.setText(name); // Set it on screen

        // Initialize GoogleSignInClient to manage Google account sign-in/out
        mGoogleSignInClient = GoogleSignIn.getClient(
                this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        );

        // Find the logout CardView (acts like a logout button)
        CardView logout = findViewById(R.id.logout);

        // Set click listener on logout button
        logout.setOnClickListener(view -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Also sign out from Google account
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // After logout is complete, go back to login screen
                startActivity(new Intent(MainActivity.this, login_page.class));
                finish(); // Close the current activity
            });
        });

        // Handle system bar insets like notch, status bar, or navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
