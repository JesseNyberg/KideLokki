package com.skotfrii.kidelokki.JsonClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    public String id;
    public String name;
    public String nameResourceKey;
    public int type;
    public ArrayList<Integer> productTypes;
    public Object parentCategoryId;
    public int orderingNumber;
    public boolean isFilterable;
    public boolean isPublic;
    public Date dateCreated;
    public Date dateModified;
}

