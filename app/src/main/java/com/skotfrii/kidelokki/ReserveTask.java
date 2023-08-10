package com.skotfrii.kidelokki;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.skotfrii.kidelokki.JsonClasses.Root;
import com.skotfrii.kidelokki.JsonClasses.Variant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReserveTask {

    private static final String RESERVATION_URL = "https://api.kide.app/api/reservations";
    private static final int MAX_RETRIES = 50;

    private final Context context;

    private String bearerToken;
    private final Root fetchedProduct;
    private final TextView resultProduct;
    private final TextView resultVariant;
    private final CardView cardView;

    private Boolean successfulReserve = false;

    private final String inputQuantity;

    public ReserveTask(Context context, String inputQuantity, TextView resultProduct, TextView resultVariant, CardView cardView, Root fetchedProduct) {
        this.context = context;
        this.inputQuantity = inputQuantity;
        this.resultProduct = resultProduct;
        this.resultVariant = resultVariant;
        this.cardView = cardView;
        this.fetchedProduct = fetchedProduct;
    }

    public void execute() {
        proceedToTask();
    }

    private void proceedToTask() {
        new ReserveTaskAsync().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class ReserveTaskAsync extends AsyncTask<String, Void, Boolean> {

        /**
         * Constructs the payload with the fetched product information
         * @param urls .
         *
         * @return .
         */
        @Override
        protected Boolean doInBackground(String... urls) {

            if (!checkTokenExistence())
                return false;

            try {
                String payload = createPayload(fetchedProduct.model.variants);
                Log.d("Payload: ", payload);
                Request request = constructReserveRequest(payload);
                return executeWithRetries(request);
            } catch (Exception err) {
                err.printStackTrace();
                return false;
            }
        }

        /**
         * After successful reserve
         * call cart fetching to get information
         * on which tickets were successfully reserved
         * @param successfulFetch .
         *
         */
        @Override
        protected void onPostExecute(Boolean successfulFetch) {
            try {
                CartFetch cartFetch = new CartFetch(context, resultProduct, resultVariant, cardView);
                cartFetch.fetchCartItems();
            }
            catch (Exception err) {
                showToast("Cart Fetch failed");
            }
        }
    }

    /**
     * Checks if bearer token exists in sharedPrefs
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
     * Constructs the JSON Body
     * with payload and authorization token
     * @param payload .
     * @return .
     */
    private Request constructReserveRequest(String payload) {
        RequestBody reserveBody = RequestBody.create(payload, MediaType.parse("application/json"));

        return new Request.Builder()
                .url(RESERVATION_URL)
                .addHeader("Authorization", "Bearer " + bearerToken)
                .post(reserveBody)
                .build();
    }

    /**
     * The actual payload object construct
     * Gets the inventoryId and possible quantity of tickets
     * inventoryId is used to reserve right tickets
     * @param variants .
     * @return .
     */
    private String createPayload(ArrayList<Variant> variants) {
        ArrayNode toCreateArray = JsonNodeFactory.instance.arrayNode();

        for (Variant variant : variants) {
            if (variant.availability == 0) {
                continue;
            }

            int maxQuantity;

            int inputQuantityInt;

            if (Objects.equals(inputQuantity, "Maximum")) {
                maxQuantity = Math.min(variant.availability, variant.productVariantMaximumReservableQuantity);
                inputQuantityInt = maxQuantity;
            } else {
                inputQuantityInt = Math.min(Integer.parseInt(inputQuantity), variant.availability);
            }

            ObjectNode variantObject = JsonNodeFactory.instance.objectNode();

            variantObject.put("inventoryId", variant.inventoryId);
            variantObject.put("quantity", inputQuantityInt);
            variantObject.set("productVariantUserForm", JsonNodeFactory.instance.nullNode());

            toCreateArray.add(variantObject);
        }

        ObjectNode payloadObject = JsonNodeFactory.instance.objectNode();
        payloadObject.set("toCreate", toCreateArray);
        payloadObject.set("toCancel", JsonNodeFactory.instance.arrayNode());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.writeValueAsString(payloadObject);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Try post requesting the constructed JSON Body
     * @param request .
     * @return .
     */
    private boolean executeWithRetries(Request request) {
        OkHttpClient requestClient = new OkHttpClient();

        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try (Response response = requestClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d("Server response on success: ", responseBody);
                        successfulReserve = true;
                        break;
                    }
                } else {
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d("Server response on failure: ", responseBody);
                    }
                    Log.d("Reserve request failed, retrying", "");
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
        return successfulReserve;
    }

    private void showToast(String message) {
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
