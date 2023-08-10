package com.skotfrii.kidelokki.JsonClasses;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Company {
    public String email;
    public String url;
    public String phone;
    public String streetAddress;
    public String city;
    public String postalCode;
    public String country;
    public double latitude;
    public double longitude;
    public int organizationType;
    public boolean isFavorited;
    public int productCount;
    public String id;
    public String name;
    public String description;
    public String ingress;
    public String mediaFilename;
    public int favoritedTimes;
    public Date dateCreated;
    public Date dateModified;
}

