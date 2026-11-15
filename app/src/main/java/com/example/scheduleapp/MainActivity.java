package com.example.scheduleapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout rootLayout;
    private LinearLayout loadingView;
    private final String GAS_URL = "https://script.google.com/macros/s/AKfycbwzncswrdn5FtkvwjifAdJj-njIw-DlK6mRmeDlNKVNF-BQPVupPCUWHzqS29sQUrEk/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 建立根容器 FrameLayout
        rootLayout = new FrameLayout(this);
        setContentView(rootLayout);
        
        // 讓最底層根容器一開始就是深夜暖可可色
        rootLayout.setBackgroundColor(Color.parseColor("#2B2321"));

        // 1. 建立 WebView（底層載入網頁）
        webView = new WebView(this);
        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        
        // ⚡ 直接把 WebView 本身的背景也設為同款咖啡色，徹底消除載入時的白屏閃爍
        webView.setBackgroundColor(Color.parseColor("#2B2321"));
        
        rootLayout.addView(webView, webParams);

        // 2. 建立全螢幕載入畫面（背景設為指定的 #2B2321）
        loadingView = new LinearLayout(this);
        loadingView.setOrientation(LinearLayout.VERTICAL);
        loadingView.setGravity(Gravity.CENTER);
        loadingView.setBackgroundColor(Color.parseColor("#2B2321"));

        // ==================== 載入你上傳的 Logo 圖片 ====================
        ImageView logoImageView = new ImageView(this);
        logoImageView.setImageResource(R.drawable.app_logo); // 對應 drawable/app_logo.png
        
        int logoSize = 650; 
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
        logoImageView.setLayoutParams(logoParams);
        
        logoImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        loadingView.addView(logoImageView);
        // ==========================================================

        FrameLayout.LayoutParams loadParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        rootLayout.addView(loadingView, loadParams);

        // WebView 基本設定
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        String userAgent = webSettings.getUserAgentString();
        webSettings.setUserAgentString(userAgent.replace("; wv", ""));

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean
