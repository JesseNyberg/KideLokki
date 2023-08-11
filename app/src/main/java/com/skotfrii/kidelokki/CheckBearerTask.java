package com.skotfrii.kidelokki;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CheckBearerTask {

    private static final String AUTH_URL = "https://api.kide.app/api/authentication/user";
    private final Context context;
    private String bearerToken;

    public CheckBearerTask(Context context) {
        this.context = context;
    }

    public void execute() {
        new CheckBearerTaskAsync().execute();
    }


    @SuppressLint("StaticFieldLeak")
    private class CheckBearerTaskAsync extends AsyncTask<String, Void, Boolean> {

        /**
         * Checks if token exists and if does
         * try the bearer token on the kide.app API
         * @return .
         */
        @Override
        protected Boolean doInBackground(String... params) {
            if (!checkTokenExistence()) {
                Log.d("Token doesn't exist", "");
                showToast("Token doesn't exist");
                return false;
            }

            Request request = constructBearerRequest();
            return executeRequest(request);
        }

        /**
         * Sends information to mainActivity, based on the result
         * of bearer token existing
         */
        @Override
        protected void onPostExecute(Boolean isValid) {
            ((MainActivity) context).onBearerTokenCheckResult(isValid);
        }
    }

    /**
     * Constructs the bearer token on headers
     * @return .
     */
    private Request constructBearerRequest() {
        return new Request.Builder()
                .url(AUTH_URL)
                .addHeader("Authorization", "Bearer " + bearerToken)
                .build();
    }

    /**
     * Tries executing the request
     * to see if bearer token is valid
     * @param request .
     * @return .
     */
    private Boolean executeRequest(Request request) {
        OkHttpClient requestClient = new OkHttpClient();

        try (Response response = requestClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                if (response.body() != null)
                    Log.d("Response:",response.body().string());
                return true;
            } else {
                if (response.body() != null)
                    Log.d("Response:",response.body().string());
                return false;
            }
        } catch (IOException err) {
            err.printStackTrace();
            showToast("Failed to connect to the server. Please check your internet connection.");
            return false;
        } catch (NullPointerException err) {
            err.printStackTrace();
            showToast("Error in making the body to a string (NullPointer)");
            return false;
        }
    }

    /**
     * Checks if bearer token already exists in sharedPrefs
     * @return .
     */
    private boolean checkTokenExistence() {
        try {
            SharedPreferences sharedToken = context.getSharedPreferences("tokenPrefs", MODE_PRIVATE);
            bearerToken = sharedToken.getString("access_token", null);

            if (bearerToken != null) {
                return true;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return false;
    }

    private void showToast(String message) {
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

}
