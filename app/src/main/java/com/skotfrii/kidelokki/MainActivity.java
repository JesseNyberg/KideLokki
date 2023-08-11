package com.skotfrii.kidelokki;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.skotfrii.kidelokki.JsonClasses.Root;
import com.skotfrii.kidelokki.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Animation fadeIn;
    private Animation bottomDown;

    private EditText inputUrl;
    private UrlValidator urlValidator;
    private Button reserveButton;
    private Button cancelButton;
    private Spinner quantitySpinner;
    private TextView countDownText;
    private TextView invalidText;
    private TextView resultProduct;
    private TextView resultVariant;
    private ProgressBar progressBar;
    private CardView cardView;

    private Root fetchedProduct;

    private ReserveBackgroundService reserveBackgroundService;
    private SharedPreferences sharedPrefs;
    BroadcastReceiver countdownUpdatedReceiver;
    private boolean bound = false;
    private Intent serviceIntent;

    private long timeRemainingMillis;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPrefs = getSharedPreferences("ProductPrefs", MODE_PRIVATE);
        boolean isServiceRunning = sharedPrefs.getBoolean("isServiceRunning", false);

        initializeAnimations();
        setAnimations();

        initializeViews();

        serviceIntent = new Intent(this, ReserveBackgroundService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        bound = true;

        boolean isCountdownActive = sharedPrefs.getBoolean("isCountdownActive", false);

        if (!isServiceRunning) {
            startService(serviceIntent);
        }

        if (isCountdownActive) {
            cancelButton.setVisibility(View.VISIBLE);
            countDownText.setVisibility(View.VISIBLE);
        }


        initializeListeners();

        checkBearerToken();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!bound) {
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
            bound = true;
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(countdownUpdatedReceiver, new IntentFilter("com.skotfrii.kidelokki.COUNTDOWN_UPDATED"));
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (bound) {
            unbindService(connection);
            bound = false;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(countdownUpdatedReceiver);
    }

    /**
     * Prevent accidental back button presses
     * so users won't go back to LoginActivity
     */
    @Override
    public void onBackPressed() {
    }

    /**
     * Checks if url matches RegEx
     * @return .
     */
    private boolean checkUrlStatus() {
        return !urlValidator.isUrlValid();
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ReserveBackgroundService.LocalBinder binder = (ReserveBackgroundService.LocalBinder) service;
            reserveBackgroundService = binder.getService();
            reserveBackgroundService.setMainActivity(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            reserveBackgroundService = null;
        }
    };

    /**
     * Add listeners for reserving and url validating
     */
    private void initializeListeners() {
        reserveButton.setOnClickListener(v -> handleProductFetch());
        setupSpinnerListener();
        urlValidator = new UrlValidator(invalidText, quantitySpinner, reserveButton, this);
        inputUrl.addTextChangedListener(urlValidator);
        cancelButton.setOnClickListener(v -> cancelCountdown());

        countdownUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long countdown = intent.getLongExtra("countdown", 0);
                // Convert countdown to your desired format, e.g., "HH:mm:ss"
                countDownText.setText(convertTimeFormat(countdown));
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(countdownUpdatedReceiver, new IntentFilter("com.skotfrii.kidelokki.COUNTDOWN_UPDATED"));
    }

    /**
     * Initializing GUI items
     */
    private void initializeViews() {
        inputUrl = findViewById(R.id.eventURLInput);
        quantitySpinner = findViewById(R.id.quantityInput);
        countDownText = findViewById(R.id.countDownText);
        invalidText = findViewById(R.id.invalidText);
        reserveButton = findViewById(R.id.reserveButton);
        resultProduct = findViewById(R.id.resultProduct);
        resultVariant = findViewById(R.id.resultVariant);
        cardView = findViewById(R.id.cardView3);
        cancelButton = findViewById(R.id.cancelButton);
        progressBar = findViewById(R.id.progressBar);

        reserveButton.setVisibility(View.GONE);
    }

    /**
     * Call bearer token exist check
     */
    private void checkBearerToken() {
        CheckBearerTask checkBearerTask = new CheckBearerTask(this);
        checkBearerTask.execute();
    }

    /**
     * If bearer token exists
     * we move to mainActivity
     * @param isBearerValid .
     */
    public void onBearerTokenCheckResult(boolean isBearerValid) {
        if (!isBearerValid) {
            Toast.makeText(this, "Bearer token is invalid. Redirecting to login.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Handles product fetch and checks URL validity before calling
     */
    public void handleProductFetch() {
        animateOutResult();
        if (checkUrlStatus()) { return; }

        restartService();

        progressBar.setVisibility(View.VISIBLE);

        fetchProductData();
    }

    public void restartService() {
        // Unbind from the service if it was previously bound
        if (bound) {
            unbindService(connection);
            bound = false;

            // Stop the service
            stopService(serviceIntent);
        }

        // Start the service again
        startService(serviceIntent);

        // Bind to the service again
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        bound = true;
    }

    /**
     * Calling product fetch
     * and setting reserve button values
     */
    public void fetchProductData() {
        reserveButton.setEnabled(false);
        reserveButton.setAlpha(0.5f);

        try {
            ProductFetch productFetch = new ProductFetch(this, getFetchProductCallback(), inputUrl.getText().toString());
            productFetch.execute();
        } catch (Exception err) {
            err.printStackTrace();
            reserveButton.setEnabled(true);
            reserveButton.setAlpha(1.0f);
        }
    }

    /**
     * Starts calling reserve methods
     * instantly after product fetch
     * @return .
     */
    private FetchProductCallback getFetchProductCallback() {
        return fetchedProduct -> {
            MainActivity.this.fetchedProduct = fetchedProduct;

            progressBar.setVisibility(View.GONE);

            if (fetchedProduct != null) {
                Boolean salesStarted = fetchedProduct.model.product.salesStarted;
                boolean salesEnded = fetchedProduct.model.product.salesEnded;

                if (salesEnded) {
                    showToast("Sales have ended.");
                    reserveButton.setEnabled(true);
                    reserveButton.setAlpha(1.0f);

                } else {
                    timeRemainingMillis = fetchedProduct.model.product.timeUntilSalesStart;
                    timeRemainingMillis *= 1000;
                    handleSalesStart(salesStarted);
                }
            }
        };
    }

    /**
     * Checks if sales have already started
     * and if not, a countdown timer starts
     * @param salesStarted .
     */
    private void handleSalesStart(Boolean salesStarted) {
        if (!salesStarted) {
            if (timeRemainingMillis > 0) {
                countDownText.setVisibility(View.VISIBLE);
                startCountdown(timeRemainingMillis);
            } else {
                handleReserve();
            }
        } else {
            handleReserve();
        }
    }

    /**
     * Calls the reserve task
     */
    private void reserveProduct() {
        try {
            String spinnerQuantity = quantitySpinner.getSelectedItem().toString();
            ReserveTask reserveTask = new ReserveTask(this, spinnerQuantity, resultProduct, resultVariant, cardView, fetchedProduct);
            reserveTask.execute();
        } catch (Exception err) {
            showToast("Reserving failed");
            reserveButton.setEnabled(true);
            reserveButton.setAlpha(1.0f);
        }
        reserveButton.setEnabled(true);
        reserveButton.setAlpha(1.0f);
    }

    /**
     * Checks if url is valid before attempting to reserve
     */
    private void handleReserve() {
        if (checkUrlStatus()) {
            return;
        }

        reserveProduct();
    }

    private void startCountdown(long timeRemainingMillis) {
        reserveBackgroundService.startReservation(timeRemainingMillis);
        cancelButton.setVisibility(View.VISIBLE);
    }

    private void cancelCountdown() {
        reserveBackgroundService.cancelCountdown();
        stopService(serviceIntent);

        if (bound) {
           unbindService(connection);
           bound = false;
        }

        countDownText.setVisibility(View.GONE);  // Make the countDownText invisible
        reserveButton.setEnabled(true);               // Enable the reserveButton
        reserveButton.setAlpha(1.0f);                 // Make sure the reserveButton is fully opaque
        cancelButton.setVisibility(View.GONE);
    }


    public String convertTimeFormat(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Shows toast with the inputted message
     * @param message .
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Setup spinner listener
     * Mainly for Invalid URL text
     */
    private void setupSpinnerListener() {
        quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i > 0) { // This means an actual option (not the placeholder) has been selected
                    if (!urlValidator.getReserveButtonHasBeenVisible()) {
                        animateIn(reserveButton);
                        urlValidator.setReserveButtonHasBeenVisible(true);
                    }
                } else if (urlValidator.getReserveButtonHasBeenVisible()) {
                    animateOut(reserveButton);
                    urlValidator.setReserveButtonHasBeenVisible(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (urlValidator.getReserveButtonHasBeenVisible()) {
                    animateOut(reserveButton);
                    urlValidator.setReserveButtonHasBeenVisible(false);
                }
            }
        });
    }

    /**
     * Initialize animations
     */
    private void initializeAnimations() {
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
    }

    /**
     * Set the animations for cardView
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
     * Handle animate in
     * @param view .
     */
    private void animateIn(final View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
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

    /**
     * Handle animate out
     * @param view .
     */
    private void animateOut(final View view) {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(fadeOut);
    }

    /**
     * Handle animation for results of ticket reserving
     */
    private void animateOutResult() {
        if (resultProduct.getVisibility() == View.VISIBLE) {
            animateOut(resultProduct);
            animateOut(resultVariant);
            animateOut(cardView);
        }
    }
}