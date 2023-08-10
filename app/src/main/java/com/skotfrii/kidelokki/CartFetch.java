package com.skotfrii.kidelokki;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skotfrii.kidelokki.JsonClasses.CartModel;
import com.skotfrii.kidelokki.JsonClasses.Reservation;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CartFetch {

    // API url where we get the cart information
    private static final String RESERVATION_URL = "https://api.kide.app/api/reservations";

    private final Context context;

    private String bearerToken;

    private final TextView resultProduct;
    private final TextView resultVariant;
    private final CardView cardView;


    public CartFetch(Context context, TextView resultProduct, TextView resultVariant, CardView cardView) {
        this.context = context;
        this.resultProduct = resultProduct;
        this.resultVariant = resultVariant;
        this.cardView = cardView;
    }

    public void fetchCartItems() {
        new FetchCartItemsTask().execute();
    }

    /**
     * Method for GET Requesting cart information
     * to give user information on which tickets were reserved
     */
    @SuppressLint("StaticFieldLeak")
    private class FetchCartItemsTask extends AsyncTask<Void, Void, Map<String, Map<String, Integer>>> {

        @Override
        protected Map<String, Map<String, Integer>> doInBackground(Void... voids) {

            if (!checkTokenExistence())
                return Collections.emptyMap();

            Request request = new Request.Builder()
                    .url(RESERVATION_URL)
                    .addHeader("Authorization", "Bearer " + bearerToken)
                    .get()
                    .build();

            OkHttpClient requestClient = new OkHttpClient();
            try (Response response = requestClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    CartModel model = mapper.readValue(responseBody, CartModel.class);

                    Map<String, Map<String, Integer>> productReservations = new HashMap<>();

                    if (model.model.reservations != null) {
                        for (Reservation reservation : model.model.reservations) {
                            productReservations
                                    .computeIfAbsent(reservation.productName, k -> new HashMap<>())
                                    .merge(reservation.variantName, reservation.reservedQuantity, Integer::sum);
                        }
                    }
                    return productReservations;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyMap();
        }

        /**
         * Gets the map of productName, variantName and reservableQuantity
         * and sets it to the correct TextViews
         * @param productReservations .
         *
         */
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Map<String, Map<String, Integer>> productReservations) {
            if (productReservations.isEmpty()) {
                resultProduct.setText("Reserve failed, no products were reserved");
                resultProduct.setTextColor(Color.RED);
                animateInCardView();
                animateIn(resultProduct);
                return;
            }

            StringBuilder productText = new StringBuilder();
            StringBuilder variantText = new StringBuilder();

            for (Map.Entry<String, Map<String, Integer>> productEntry : productReservations.entrySet()) {
                productText.append(productEntry.getKey()).append("\n");

                for (Map.Entry<String, Integer> variantEntry : productEntry.getValue().entrySet()) {
                    variantText.append(variantEntry.getValue())
                            .append(" x ")
                            .append(variantEntry.getKey())
                            .append("\n");
                }
            }

            resultProduct.setText(productText.toString().trim());
            resultProduct.setTextColor(Color.WHITE);

            resultVariant.setText(variantText.toString().trim());
            resultVariant.setTextColor(Color.WHITE);

            animateInCardView();
            animateIn(resultProduct);
            animateIn(resultVariant);
        }
    }

    private void animateInCardView() {
            Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
            cardView.setVisibility(View.VISIBLE);
            cardView.startAnimation(fadeIn);
    }

    /**
     * Checks if bearer token exists in sharedPrefs
     * @return .
     */
    private boolean checkTokenExistence() {
        try {
            SharedPreferences sharedToken = context.getSharedPreferences("tokenPrefs", Context.MODE_PRIVATE);
            bearerToken = sharedToken.getString("access_token", null);

            return bearerToken != null;
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
    }

    private void animateIn(final View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(fadeIn);
    }

}
