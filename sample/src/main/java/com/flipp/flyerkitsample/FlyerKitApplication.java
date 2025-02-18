package com.flipp.flyerkitsample;

import android.app.Application;

public class FlyerKitApplication extends Application {
    // Postal/ZIP code given by user (L1B9C3, 90210)
    String postalCode = "L4W1L6";
    // Store code selected by user
    String storeCode = "001";
    // Access token provided by Flipp
    final String accessToken = "";
    // Locale of user (en, fr, en-US, en-CA)
    final String locale = "en-CA";
    // Flipp's name identifier of merchant
    final String merchantIdentifier = "flippflyerkit";
    // Root URL of API calls
    final String rootUrl = "https://api.flipp.com/";
    // API version number (vX.X)
    final String apiVersion = "v3.0";
    // default flyer id
    final int defaultFlyerId = 788309;
    // Flipp's merchant ID
    final String merchantId = "4489";
    // Default loyalty card ID - should be provided by user
    final String loyaltyCardId = "3333";
    // Default loyalty card programID
    final String loyaltyCardProgramId = "1234567890";

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }
}