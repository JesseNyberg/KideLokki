package com.skotfrii.kidelokki.JsonClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cart {
    public double finalPrice;
    public double serviceFee;
    public Object reservations;
    public Object currencyCode;
    public int reservationsCount;
    public int reservationsTimeLeft;
}
