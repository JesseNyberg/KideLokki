package com.skotfrii.kidelokki.JsonClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    public Date birthday;
    public String email;
    public String phone;
    public String streetAddress;
    public String city;
    public String country;
    public String school;
    public String postalCode;
    public boolean isBlocked;
    public boolean isDeleted;
    public boolean isHakaAuthenticated;
    public boolean isProfileComplete;
    public boolean isEmailReal;
    public boolean isLimited;
    public String locale;
    public Date dateLastLogin;
    public Date dateLastActivity;
    public String username;
    public String checkoutMobilePayPhone;
    public String checkoutPivoPhone;
    public boolean hasPassword;
    public int newUserInventoryItems;
    public Object dateDeletionRequested;
    public Cart cart;
    public List<Membership> memberships;
    public Object order;
    public Settings settings;
    public List<Object> externalAccessTokens;
    public boolean hasUsername;
    public String firstName;
    public String lastName;
    public String gender;
    public String initials;
    public String fullName;
    public String id;
    public String name;
    public String description;
    public String ingress;
    public String mediaFilename;
    public int favoritedTimes;
    public Date dateCreated;
    public Date dateModified;
}
