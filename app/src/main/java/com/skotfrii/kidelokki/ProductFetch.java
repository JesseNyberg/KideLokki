package com.skotfrii.kidelokki;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skotfrii.kidelokki.JsonClasses.Root;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProductFetch {

    private final String productUrl;
    private final Context context;
    private String bearerToken;
    private Root fetchedProduct;
    private static final int MAX_RETRIES = 50;
    private Boolean successfulFetch = false;
    private final FetchProductCallback callback;

    public ProductFetch(Context context, FetchProductCallback callback, String productUrl) {
        this.context = context;
        this.callback = callback;
        this.productUrl = productUrl;
    }

    public void execute() {
        new ProductFetchTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class ProductFetchTask extends AsyncTask<String, Void, Root> {

        /**
         * Converts the event url to API url
         * and calls the request
         * @param urls .
         *
         * @return .
         */
        @Override
        protected Root doInBackground(String... urls) {

            Log.d("Product fetch started!","");
            if (!checkTokenExistence())
                return null;

            String kideEventUrl;
            try {
                kideEventUrl = convertUrl(productUrl);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            if (kideEventUrl.isEmpty()) {
                return null;
            }

            return executeWithRetries(kideEventUrl, bearerToken);
        }

        /**
         * Informs if the fetch was successful
         * on a toast and informs MainActivity
         * of successful fetch
         * @param fetchedProduct .
         *
         */
        @Override
        protected void onPostExecute(Root fetchedProduct) {
            super.onPostExecute(fetchedProduct);
            Log.d("Product fetch ended!", "");
            if (successfulFetch) {
                showToast("Product fetch was successful!");
                callback.onProductFetched(fetchedProduct);
            } else {
                showToast("Product fetch failed!");
            }
        }
    }

    /**
     * Converts the event URL to API Url
     * @param oldUrl .
     * @return .
     * @throws URISyntaxException .
     */
    public static String convertUrl(String oldUrl) throws URISyntaxException {
        URI uri = new URI(oldUrl);
        String newPath = uri.getPath().replace("/events/", "/api/products/");
        URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost().replace("kide.app", "api.kide.app"),
                uri.getPort(), newPath, uri.getQuery(), uri.getFragment());
        return newUri.toString();
    }

    /**
     * Checks if bearer token exists
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

    /**
     * Parse JSON Data response to an object
     * @param jsonString .
     * @return .
     * @throws IOException .
     */
    private Root parseJsonToRoot(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(jsonString, Root.class);
        }
        catch (JsonProcessingException err) {
            err.printStackTrace();
        }
        return null;
    }

    /**
     * Does the POST request based on the JSON body
     * @param url .
     * @param bearerToken .
     * @return .
     * @throws IOException .
     */
    private Response performRequest(String url, String bearerToken) throws IOException {
        OkHttpClient requestClient = new OkHttpClient();
        Request productRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + bearerToken)
                .build();

        return requestClient.newCall(productRequest).execute();
    }

    /**
     * Tries to perform the request
     * based on the event URL
     * @param url .
     * @param bearerToken .
     * @return .
     */
    private Root executeWithRetries(String url, String bearerToken) {
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                Response response = performRequest(url, bearerToken);

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseString = response.body().string();
                        fetchedProduct = parseJsonToRoot(responseString);
                        successfulFetch = true;
                        break;
                    }
                } else {
                    retryCount++;
                }
            } catch (IOException err) {
                err.printStackTrace();
                showToast("Failed to connect to the server. Please check your internet connection.");
                retryCount++;
            } catch (NullPointerException err) {
                err.printStackTrace();
                showToast("Error in making the body to a string (NullPointer)");
                retryCount++;
            }
        }
        return fetchedProduct;
    }

    private void showToast(String message) {
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
