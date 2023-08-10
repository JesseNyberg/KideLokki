package com.skotfrii.kidelokki.JsonClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Membership {
    public String id;
    public boolean isDateValid;
    public boolean isDisabled;
    public boolean isMembershipToUserBindingDisabled;
}