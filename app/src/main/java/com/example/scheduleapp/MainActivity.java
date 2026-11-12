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
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout rootLayout;
    private LinearLayout loadingView;
    private final String GAS_URL = "https://script.google.com/macros/s/AKfycbwzncswrdn5FtkvwjifAdJj-njIw-DlK6mRmeDlNKVNF-BQPVupPCUWHzqS29sQUrEk/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 建立根容器 FrameLayout，用來疊加 WebView 與載入畫面
        rootLayout = new FrameLayout(this);
        setContentView(rootLayout);

        // 1. 建立 WebView
        webView = new WebView(this);
        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        rootLayout.addView(webView, webParams);

        // 2. 建立全螢幕載入畫面（覆蓋在最上層，遮蔽白屏）
        loadingView = new LinearLayout(this);
        loadingView.setOrientation(LinearLayout.VERTICAL);
        loadingView.setGravity(Gravity.CENTER);
        loadingView.setBackgroundColor(Color.parseColor("#121212")); // 深色背景

        // ==================== Logo 圖片顯示 ====================
        ImageView logoImageView = new ImageView(this);
        // 確保你的 res/drawable 資料夾內有對應名稱的圖片 (例如 app_logo.png -> R.drawable.app_logo)
        logoImageView.setImageResource(R.drawable.app_logo); 
        
        // 設定 Logo 大小（單位是像素 px）
        int logoSize = 220; 
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
        logoParams.setMargins(0, 0, 0, 30); // 距離下方文字的間距
        logoImageView.setLayoutParams(logoParams);
        
        // 讓圖片保持比例縮放
        logoImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        loadingView.addView(logoImageView);
        // ========================================================

        // 系統名稱文字
        TextView titleText = new TextView(this);
        titleText.setText("均安服務員系統");
        titleText.setTextColor(Color.WHITE);
        titleText.setTextSize(22);
        titleText.setGravity(Gravity.CENTER);
        loadingView.addView(titleText);

        // （已移除轉圈圈 ProgressBar，畫面更乾淨俐落）

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
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager.getInstance().flush();

                // 移除 Google Apps Script 警告橫幅的 JavaScript
                String jsCode = "javascript:(function() {" +
                        "var divs = document.getElementsByTagName('div');" +
                        "for (var i = 0; i < divs.length; i++) {" +
                        "    var text = divs[i].innerText || '';" +
                        "    if (text.includes('這個應用程式是由 Google Apps Script') || text.includes('檢舉濫用情形')) {" +
                        "        var target = divs[i];" +
                        "        while (target && target.parentElement && target.parentElement !== document.body && target.parentElement.offsetHeight < 180) {" +
                        "            target = target.parentElement;" +
                        "        }" +
                        "        target.style.display = 'none';" +
                        "        break;" +
                        "    }" +
                        "}" +
                        "})();";
                view.loadUrl(jsCode);

                // 稍微延遲 200 毫秒才淡出載入畫面，確保橫幅已經被 JS 徹底刪除且畫面完美銜接
                if (loadingView != null && loadingView.getVisibility() == View.VISIBLE) {
                    loadingView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingView.animate()
                                    .alpha(0f)
                                    .setDuration(250)
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadingView.setVisibility(View.GONE);
                                        }
                                    })
                                    .start();
                        }
                    }, 200);
                }
            }
        });

        if (savedInstanceState == null) {
            webView.loadUrl(GAS_URL);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            WebBackForwardList historyList = webView.copyBackForwardList();
            int currentIndex = historyList.getCurrentIndex();
            if (currentIndex > 1) {
                webView.goBackOrForward(-currentIndex); 
            } else {
                webView.goBack();
            }
        } else {
            moveTaskToBack(true);
        }
    }
}
