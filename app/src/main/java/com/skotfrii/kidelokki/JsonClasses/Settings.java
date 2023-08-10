package com.skotfrii.kidelokki.JsonClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings {
    public boolean completedOnboarding;
    public boolean hideSuccessfulCheckoutDialog;
    public boolean hideOrderBeingProcessedDialog;
    public boolean directMarketingPermissionGranted;
}
