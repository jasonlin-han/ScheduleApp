package com.example.scheduleapp;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

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
        // 1. 如果側拉選單（Drawer）是開著的，先關閉選單（若日後有加 DrawerLayout 的話）
        DrawerLayout drawer = findViewById(R.id.drawer_layout); 
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        // 2. 檢查 WebView 內是否還有上一頁
        if (webView != null && webView.canGoBack()) {
            // 取得當前的歷史紀錄堆疊
            WebBackForwardList historyList = webView.copyBackForwardList();
            int currentIndex = historyList.getCurrentIndex();
            
            // 如果當前索引大於 1（代表堆疊超過 2 個頁面），直接強制跳回首頁（索引 0）
            if (currentIndex > 1) {
                webView.goBackOrForward(-currentIndex); 
            } else {
                // 如果剛好在第 1 層（首頁的下一頁），退回上一頁就會回到首頁
                webView.goBack();
            }
        } else {
            // 3. 如果已經在首頁（無法再往回），則執行返回桌面，而不是直接關閉 App
            moveTaskToBack(true);
        }
    }
}
