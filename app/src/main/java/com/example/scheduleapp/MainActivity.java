package com.example.scheduleapp;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebBackForwardList;
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
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        
        // 關鍵修改：改用快取優先策略，大幅縮短載入等待時間
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        String userAgent = webSettings.getUserAgentString();
        webSettings.setUserAgentString(userAgent.replace("; wv", ""));

        // Cookie 設定與持久化
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
                // 每次頁面載入完成後，強制同步 Cookie 寫入儲存空間，防止手勢上一頁時 Token 遺失
                CookieManager.getInstance().flush();

                // 注入 JavaScript 瞬間隱藏 Google Apps Script 的頂端警告橫幅
                String jsCode = "javascript:(function() {" +
                        "if (window.gasBannerObserverInjected) return;" +
                        "window.gasBannerObserverInjected = true;" +
                        "function hideBanner() {" +
                        "    var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);" +
                        "    while (walker.nextNode()) {" +
                        "        if (walker.currentNode.nodeValue.includes('這個應用程式是由 Google Apps Script')) {" +
                        "            var el = walker.currentNode.parentElement;" +
                        "            while (el && el !== document.body && el.offsetHeight < 150) {" +
                        "                var style = window.getComputedStyle(el);" +
                        "                if (style.position === 'fixed' || style.position === 'absolute' || el.tagName === 'DIV') {" +
                        "                    el.style.display = 'none';" +
                        "                    return true;" +
                        "                }" +
                        "                el = el.parentElement;" +
                        "            }" +
                        "        }" +
                        "    }" +
                        "    return false;" +
                        "}" +
                        "if (!hideBanner()) {" +
                        "    var observer = new MutationObserver(function(mutations, obs) {" +
                        "        if (hideBanner()) { obs.disconnect(); }" +
                        "    });" +
                        "    observer.observe(document.body, { childList: true, subtree: true });" +
                        "    setTimeout(function() { observer.disconnect(); }, 2000);" +
                        "}" +
                        "})();";
                
                view.loadUrl(jsCode);
            }
        });

        // 如果不是因為畫面重建（例如轉向），才載入首頁
        if (savedInstanceState == null) {
            webView.loadUrl(GAS_URL);
        }
    }

    // 讓 WebView 在畫面因旋轉等狀況重建時能保留狀態
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
        // 檢查 WebView 內是否還有上一頁
        if (webView != null && webView.canGoBack()) {
            WebBackForwardList historyList = webView.copyBackForwardList();
            int currentIndex = historyList.getCurrentIndex();
            
            // 如果當前索引大於 1（代表堆疊超過 2 個頁面），直接強制跳回首頁（索引 0）
            if (currentIndex > 1) {
                webView.goBackOrForward(-currentIndex); 
            } else {
                webView.goBack();
            }
        } else {
            // 如果已經在首頁，則執行返回桌面，而不是關閉 App
            moveTaskToBack(true);
        }
    }
}
