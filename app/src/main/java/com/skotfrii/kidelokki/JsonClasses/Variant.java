package com.skotfrii.kidelokki.JsonClasses;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Variant {
    public String id;
    public String name;
    public String description;
    public String mediaFilename;
    public String inventoryId;
    public String currencyCode;
    public int pricePerItem;
    public int vat;
    public String notesInstructions;
    public int availability;
    public boolean isProductVariantActive;
    public boolean isProductVariantMarkedAsOutOfStock;
    public boolean isProductVariantTransferable;
    public boolean isProductVariantVisible;
    public boolean isProductVariantHakaAuthenticationRequired;
    public int productVariantMaximumItemQuantityPerUser;
    public int productVariantMaximumReservableQuantity;
    public int productVariantMinimumReservableQuantity;
    public String productId;
    public int productType;
    public Date dateSalesFrom;
    public boolean isProductVariantMembershipRequired;
    public boolean isProductVariantStudentCardRequired;
}
