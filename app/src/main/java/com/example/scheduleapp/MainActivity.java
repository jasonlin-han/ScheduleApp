package com.example.scheduleapp;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private final String GAS_URL = "https://script.google.com/macros/s/AKfycbwzncswrdn5FtkvwjifAdJj-njIw-DlK6mRmeDlNKVNF-BQPVupPCUWHzqS29sQUrEk/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

       WebSettings webSettings = webView.getSettings();
webSettings.setJavaScriptEnabled(true);
webSettings.setDomStorageEnabled(true);
webSettings.setDatabaseEnabled(true);

webSettings.setSupportZoom(false);
webSettings.setBuiltInZoomControls(false);
webSettings.setDisplayZoomControls(false);
webSettings.setUseWideViewPort(false);
webSettings.setLoadWithOverviewMode(false);

        String userAgent = webSettings.getUserAgentString();
        webSettings.setUserAgentString(userAgent.replace("; wv", ""));

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(GAS_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
