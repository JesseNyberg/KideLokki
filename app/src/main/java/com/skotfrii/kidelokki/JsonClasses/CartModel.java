package com.skotfrii.kidelokki.JsonClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartModel {
    public Model model;

    public static class Model {
        public int finalPrice;
        public int serviceFee;
        public ArrayList<Reservation> reservations;
        public String currencyCode;
        public int reservationsCount;
        public int reservationsTimeLeft;
    }
}
