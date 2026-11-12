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
        
        // 恢復為最穩定的預設載入模式，避免快取鎖死導致白屏卡死
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

                // 安全且高效率地將整條 Google 警告橫幅（包含按鈕與文字）直接隱藏
                String jsCode = "javascript:(function() {" +
                        "var banners = document.querySelectorAll('div');" +
                        "for (var i = 0; i < banners.length; i++) {" +
                        "    var text = banners[i].innerText || '';" +
                        "    if (text.includes('這個應用程式是由 Google Apps Script') || text.includes('檢舉濫用情形')) {" +
                        "        var el = banners[i];" +
                        "        while (el && el.parentElement && el.parentElement !== document.body && el.parentElement.offsetHeight < 150) {" +
                        "            el = el.parentElement;" +
                        "        }" +
                        "        el.style.display = 'none';" +
                        "    }" +
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
