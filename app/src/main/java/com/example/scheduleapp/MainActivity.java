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

                // 升級版 JS：往上層尋找更完整的父容器，將整條橫幅連同按鈕區塊一次隱藏
                String jsCode = "javascript:(function() {" +
                        "if (window.gasBannerObserverInjected) return;" +
                        "window.gasBannerObserverInjected = true;" +
                        "function hideBanner() {" +
                        "    var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);" +
                        "    while (walker.nextNode()) {" +
                        "        if (walker.currentNode.nodeValue.includes('這個應用程式是由 Google Apps Script')) {" +
                        "            var el = walker.currentNode.parentElement;" +
                        "            var target = null;" +
                        "            while (el && el !== document.body) {" +
                        "                var text = el.innerText || '';" +
                        "                if (text.includes('檢舉濫用情形') || text.includes('瞭解詳情') || el.offsetHeight < 200) {" +
                        "                    target = el;" +
                        "                } else {" +
                        "                    break;" +
                        "                }" +
                        "                el = el.parentElement;" +
                        "            }" +
                        "            if (target) {" +
                        "                target.style.display = 'none';" +
                        "                return true;" +
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
