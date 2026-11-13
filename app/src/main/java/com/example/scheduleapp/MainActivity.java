package com.example.scheduleapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

        // 建立根容器 FrameLayout
        rootLayout = new FrameLayout(this);
        setContentView(rootLayout);

        // 1. 建立 WebView（底層載入網頁）
        webView = new WebView(this);
        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        rootLayout.addView(webView, webParams);

        // 2. 建立全螢幕載入畫面（完美遮蔽白屏與載入過程）
        loadingView = new LinearLayout(this);
        loadingView.setOrientation(LinearLayout.VERTICAL);
        loadingView.setGravity(Gravity.CENTER);
        loadingView.setBackgroundColor(Color.parseColor("#121212")); // 沉穩深色背景

        // ==================== 用純程式碼動態繪製專屬圖示 ====================
        TextView logoIcon = new TextView(this);
        logoIcon.setText("均"); // 系統代表字
        logoIcon.setTextColor(Color.WHITE);
        logoIcon.setTextSize(42);
        logoIcon.setGravity(Gravity.CENTER);

        // 動態產生圓角漸層背景（橘色系，呼應主視覺）
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(45f); // 圓角弧度
        drawable.setColor(Color.parseColor("#FF6F00")); // 均安系統代表橘色
        logoIcon.setBackground(drawable);

        int logoSize = 190; // 圖示尺寸 (像素)
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
        logoParams.setMargins(0, 0, 0, 35);
        logoIcon.setLayoutParams(logoParams);
        loadingView.addView(logoIcon);
        // ====================================================================

        // 系統名稱文字
        TextView titleText = new TextView(this);
        titleText.setText("均安服務員系統");
        titleText.setTextColor(Color.parseColor("#FFFFFF"));
        titleText.setTextSize(22);
        titleText.setGravity(Gravity.CENTER);
        loadingView.addView(titleText);

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

                // 自動移除 Google Apps Script 警告橫幅
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

                // 網頁準備好後，快速且無縫地淡出載入畫面
                if (loadingView != null && loadingView.getVisibility() == View.VISIBLE) {
                    loadingView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingView.animate()
                                    .alpha(0f)
                                    .setDuration(250) // 快速流暢的淡出時間
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadingView.setVisibility(View.GONE);
                                        }
                                    })
                                    .start();
                        }
                    }, 150); // 微調等待時間，確保完美銜接
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
