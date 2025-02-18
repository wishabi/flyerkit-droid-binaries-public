package com.flipp.flyerkitsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class IframeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iframe);
        FlyerKitApplication gv = (FlyerKitApplication) getApplicationContext();

        // get iframe url from intent
        String iframeUrl = getIntent().getStringExtra("iframeUrl");
        iframeUrl += "?postal_code=" + gv.postalCode +
                "&locale=" + gv.locale + "&merchant_id=" + gv.merchantId;

        // set up webview
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl(iframeUrl);
    }
}
