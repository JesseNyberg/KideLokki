package com.skotfrii.kidelokki.JsonClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Model {
    public Company company;
    public Product product;
    public ArrayList<Variant> variants;
    public ArrayList<Category> categories;
    public boolean isHakaRequired;
}

