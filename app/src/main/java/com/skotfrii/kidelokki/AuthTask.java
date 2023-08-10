package com.skotfrii.kidelokki;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This class handles user authentication using OAuth2. It performs the authentication process
 * by making a POST request to the authentication server and handles the response accordingly.
 */
public class AuthTask {

    // Public method to start the authentication process by passing email and password
    private static final String AUTH_URL = "https://auth.kide.app/oauth2/token";

    private final Context context;

    public AuthTask(Context context) {
        this.context = context;
    }

    /**
     * Public method to start the authentication process by passing email and password.
     *
     * @param email    the email
     * @param password the password
     */
    public void execute(String email, String password) {
        new AuthAsyncTask().execute(email, password);
    }

    // AsyncTask to perform the authentication process in the background
    @SuppressLint("StaticFieldLeak")

    private class AuthAsyncTask extends AsyncTask<String, Void, String> {

        private String errorMessage = null;

        /**
         * Background task to execute the authentication request in a separate thread.
         *
         * @param params The parameters of the task. (email and password)
         * @return The response body from the authentication server.
         */
        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String password = params[1];

            Request request = constructAuthRequest(email, password);

            // Execute the request and return the response body
            return executeRequest(request);
        }

        @Override
        protected void onPostExecute(String responseBody) {
            // Handle the response based on success or failure

            if (responseBody != null) {
                handleSuccessfulResponse(responseBody);
            } else if (errorMessage != null) {
                showToast(errorMessage);
            }
        }

        /**
         * Constructs the authentication request using email and password.
         *
         * @param email    The user's email.
         * @param password The user's password.
         * @return The constructed authentication request.
         */
        private Request constructAuthRequest(String email, String password) {
            // Construct the authentication request using email and password
            MediaType authMediaType = MediaType.parse("application/json");

            String requestBody = "client_id=56d9cbe22a58432b97c287eadda040df&grant_type=password" +
                    "&password=" + password + "&rememberMe=true&username=" + email;
            RequestBody body = RequestBody.create(requestBody, authMediaType);

            return new Request.Builder()
                    .url(AUTH_URL)
                    .post(body)
                    .build();
        }

        /**
         * Executes the authentication request and returns the response body as a string.
         *
         * @param request The authentication request to be executed.
         * @return The response body from the authentication server.
         */
        private String executeRequest(Request request) {
            OkHttpClient requestClient = new OkHttpClient();

            try (Response response = requestClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    // If the response is successful, return the response body as a string
                    if (response.body() != null) {
                        return response.body().string();
                    }
                } else {
                    // Response not successful, most likely incorrect login
                    errorMessage = "E-mail or password is incorrect!";
                }
            } catch (IOException err) {
                // Handle IOException, usually due to network-related issues
                err.printStackTrace();
                errorMessage = "Failed to connect to the server. Please check your internet connection.";
            } catch (NullPointerException err) {
                err.printStackTrace();
                errorMessage = "Error in making the body to a string (NullPointer)";
            }
            return null;
        }

        /**
         * Handles the successful response from the authentication server.
         *
         * @param responseBody The response body from the authentication server.
         */
        private void handleSuccessfulResponse(String responseBody) {
            ObjectMapper jsonManager = new ObjectMapper();

            JsonNode authResponseNode;
            try {
                authResponseNode = jsonManager.readTree(responseBody);
                String accessToken = authResponseNode.path("access_token").asText();
                saveAccessToken(accessToken);
                navigateToMainActivity();
            } catch (JsonProcessingException err) {
                err.printStackTrace();
                showToast("Error parsing JSON response");
            }
        }

        /**
         * Saves the bearer token to sharedPrefs
         * so it can be used later
         * @param accessToken .
         */
        private void saveAccessToken(String accessToken) {
            SharedPreferences sharedToken = context.getSharedPreferences("tokenPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = sharedToken.edit();
            prefsEditor.putString("access_token", accessToken);
            prefsEditor.apply();
        }

        /**
         * Navigate to mainactivity, if the bearer token exists
         */
        private void navigateToMainActivity() {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
            if (context instanceof LoginActivity) {
                ((LoginActivity) context).finish();
            }
        }

        /**
         * Runs the wanted message on UI Thread to show a toast
         * @param message .
         */
        private void showToast(String message) {
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
