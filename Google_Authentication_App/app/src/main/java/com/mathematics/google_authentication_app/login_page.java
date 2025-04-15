package com.mathematics.google_authentication_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.*;

public class login_page extends AppCompatActivity {

    // Request code to identify Google sign-in result
    private static final int RC_SIGN_IN = 100;

    // Google Sign-In client for launching Google sign-in flow
    private GoogleSignInClient mGoogleSignInClient;

    // FirebaseAuth instance to authenticate with Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Makes content appear behind system bars
        setContentView(R.layout.activity_login_page); // Set layout for login screen

        // Initialize Firebase Authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Required for Firebase auth
                .requestEmail() // Ask for user's email
                .build();

        // Build GoogleSignInClient using the options
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Find the Google login button in layout
        MaterialButton g_login = findViewById(R.id.g_login);

        // When user taps the Google login button
        g_login.setOnClickListener(view -> {
            // Start the Google sign-in intent and wait for result
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // Adjust UI padding to avoid overlapping system bars (notch, status bar, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Called when user completes sign-in flow and returns to this activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if this result is from the Google sign-in
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Try getting the Google account from the intent result
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // Authenticate this account with Firebase
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google sign-in failed
                Log.e("Google SignIn", "Failed", e);
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Function to sign in to Firebase using Google account credentials
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        // Create Firebase credential from Google ID token
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        // Sign in to Firebase using the credential
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in successful, move to MainActivity
                        String name = acct.getDisplayName(); // Get user's name from account
                        Intent intent = new Intent(login_page.this, MainActivity.class);
                        intent.putExtra("name", name); // Pass name to next screen
                        startActivity(intent);
                        finish(); // Close login activity
                    } else {
                        // Firebase authentication failed
                        Toast.makeText(login_page.this, "Firebase Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
