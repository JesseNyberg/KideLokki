package com.skotfrii.kidelokki;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlValidator implements TextWatcher {

    private final Context context;
    private final TextView invalidText;
    private final Spinner quantitySpinner;
    private final Button reserveButton;
    private boolean isUrlValid = false;
    private boolean hasBeenVisible = false;
    private boolean reserveButtonHasBeenVisible = false;


    public UrlValidator(TextView invalidText, Spinner quantitySpinner, Button reserveButton, Context context) {
        this.invalidText = invalidText;
        this.quantitySpinner = quantitySpinner;
        this.reserveButton = reserveButton;
        this.context = context;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    /**
     * Check if URL is valid
     * and says Invalid URL in the meantime
     * @param editable .
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void afterTextChanged(Editable editable) {
        String eventUrl = editable.toString();

        if (eventUrl.isEmpty()) {
            invalidText.setVisibility(View.INVISIBLE);
            if (hasBeenVisible) {
                animateOut(quantitySpinner);
                hasBeenVisible = false;
            }
            if (reserveButtonHasBeenVisible) {
                animateOut(reserveButton);
                reserveButtonHasBeenVisible = false;
            }
        } else if (eventUrl.length() == 60 && isValidUrl(eventUrl)) {
            isUrlValid = true;
            invalidText.setVisibility(View.INVISIBLE);

            if (!hasBeenVisible) {
                animateIn(quantitySpinner);
                hasBeenVisible = true;
            }
        } else {
            isUrlValid = false;
            invalidText.setText("Invalid URL");
            invalidText.setVisibility(View.VISIBLE);
            if (hasBeenVisible) {
                animateOut(quantitySpinner);
                hasBeenVisible = false;
            }
            if (reserveButtonHasBeenVisible) {
                animateOut(reserveButton);
                reserveButtonHasBeenVisible = false;
            }
            invalidText.setTextColor(Color.RED);
        }
    }

    /**
     * Checks the URL validity through RegEx
     * @param eventUrl .
     * @return .
     */
    private boolean isValidUrl(String eventUrl) {
        String eventUrlRegex = "https://kide\\.app/events/[a-fA-F\\d-]+";
        Pattern regexPattern = Pattern.compile(eventUrlRegex);
        Matcher matcher = regexPattern.matcher(eventUrl);

        return matcher.matches();
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
                // do nothing
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        view.startAnimation(fadeIn);
    }

    private void animateOut(final View view) {
        Animation fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
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
     * Getters and setters, to make animate out possible
     * for all items related, if some inputs are cleared
     * like spinner input
     * @return .
     */
    public boolean getReserveButtonHasBeenVisible() {
        return reserveButtonHasBeenVisible;
    }

    public void setReserveButtonHasBeenVisible(boolean reserveButtonHasBeenVisible) {
        this.reserveButtonHasBeenVisible = reserveButtonHasBeenVisible;
    }



    public boolean isUrlValid() {
        return isUrlValid;
    }
}
