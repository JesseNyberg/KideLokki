package com.skotfrii.kidelokki;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.skotfrii.kidelokki.databinding.ActivityLoginBinding;



public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private Animation fadeIn;
    private Animation bottomDown;

    private EditText inputEmail;
    private EditText inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeAnimations();
        setAnimations();

        tryExistingLogin();

        initializeViews();
    }

    /**
     * Initialize animations
     */
    private void initializeAnimations() {
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
    }

    /**
     * Set animations
     */
    private void setAnimations() {
        binding.topLinearLayout.setAnimation(bottomDown);
        Handler handler = new Handler();
        Runnable runnable = () -> {
            binding.cardView1.setAnimation(fadeIn);
            binding.cardView2.setAnimation(fadeIn);
            binding.logoText.setAnimation(fadeIn);
        };
        handler.postDelayed(runnable, 1000);
    }

    /**
     * Initialize GUI items
     */
    private void initializeViews() {
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> handleLogin());
    }

    /**
     * Tries to get access token
     * If exists, moves to MainActivity
     */
    private void tryExistingLogin() {
        try {
            SharedPreferences sharedToken = getSharedPreferences("tokenPrefs", MODE_PRIVATE);
            String accessToken = sharedToken.getString("access_token", null);

            if (accessToken != null) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
        catch (Exception err) {
            err.printStackTrace();
        }
    }

    /**
     * Handles login through auth.kide.app
     * and checks if the inputs are entered
     */
    private void handleLogin() {
        String emailInput = inputEmail.getText().toString();
        String passwordInput = inputPassword.getText().toString();

        if (!emailInput.isEmpty() && !passwordInput.isEmpty()) {
            AuthTask authTask = new AuthTask(LoginActivity.this);
            authTask.execute(inputEmail.getText().toString(), inputPassword.getText().toString());
        } else {
            Toast.makeText(this, "The input fields can't be empty.", Toast.LENGTH_SHORT).show();
        }
    }
}