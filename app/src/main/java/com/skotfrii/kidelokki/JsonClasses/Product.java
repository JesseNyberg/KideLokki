package com.skotfrii.kidelokki.JsonClasses;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    public int productType;
    public String city;
    public String country;
    public Date dateActualFrom;
    public Date dateActualUntil;
    public double latitude;
    public double longitude;
    public String postalCode;
    public String streetAddress;
    public String place;
    public String companyId;
    public Date datePublishFrom;
    public Date datePublishUntil;
    public Date dateSalesFrom;
    public Date dateSalesUntil;
    public boolean isDeleted;
    public boolean isPublic;
    public boolean isPublished;
    public boolean isFavorited;
    public Object pricingInformation;
    public Object minTotalReservationsPerCheckout;
    public Object maxTotalReservationsPerCheckout;
    public int availability;
    public Object maxPrice;
    public Object minPrice;
    public boolean hasFreeInventoryItems;
    public boolean hasInventoryItems;
    public boolean isLong;
    public boolean isActual;
    public boolean salesStarted;
    public boolean salesEnded;
    public boolean salesOngoing;
    public boolean salesPaused;
    public int time;
    public int timeUntilSalesStart;
    public String id;
    public String name;
    public String description;
    public String ingress;
    public String mediaFilename;
    public int favoritedTimes;
    public Date dateCreated;
    public Date dateModified;
}

