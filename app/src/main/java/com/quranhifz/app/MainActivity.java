package com.quranhifz.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;
    private PermissionRequest pendingAudioRequest;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setTheme(com.quranhifz.app.R.style.AppTheme);
        getWindow().setStatusBarColor(0xFF0F5132);
        getWindow().setNavigationBarColor(0xFF0F5132);
        getWindow().getDecorView().setSystemUiVisibility(0);

        webView = new WebView(this);
        webView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setTextZoom(100);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override public void onPermissionRequest(PermissionRequest r){
                runOnUiThread(() -> {
                    boolean wantsAudio = false;
                    for (String resource : r.getResources()) {
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) wantsAudio = true;
                    }
                    if (!wantsAudio) { r.deny(); return; }
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        r.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                    } else {
                        pendingAudioRequest = r;
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 7);
                    }
                });
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 7 && pendingAudioRequest != null) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                pendingAudioRequest.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
            else pendingAudioRequest.deny();
            pendingAudioRequest = null;
        }
    }

    @Override public void onBackPressed(){
        if(webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy(){
        if(webView != null){ webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
