package com.skotfrii.kidelokki.JsonClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reservation{
    public String inventoryId;
    public int pricePerItem;
    public String currencyCode;
    public int vat;
    public Object notes;
    public Date reservationDateCreated;
    public String notesInstructions;
    public ArrayList<Object> requiredInventoryIdReservations;
    public int availability;
    public int reservedQuantity;
    public boolean isProductVariantActive;
    public boolean isProductVariantMarkedAsOutOfStock;
    public boolean isProductVariantTransferable;
    public boolean isProductVariantHakaAuthenticationRequired;
    public boolean productVariantHasDeliveryMethods;
    public boolean productVariantCheckoutOnlyViaDeliveryMethods;
    public int productVariantMaximumItemQuantityPerUser;
    public int productVariantMaximumReservableQuantity;
    public int productVariantMinimumReservableQuantity;
    public boolean productVariantIsCountedTowardsMinTotalProductReservationsPerCheckout;
    public String variantId;
    public String variantName;
    public String variantMediaFilename;
    public ArrayList<Object> linkedProductVariants;
    public ArrayList<Object> accessControlMemberships;
    public ArrayList<Object> contentsMemberships;
    public String productId;
    public int productType;
    public String productName;
    public String productMediaFilename;
    public Date productDateSalesFrom;
    public Object minTotalReservationsPerCheckout;
    public String companyId;
    public String companyName;
}
