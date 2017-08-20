package com.bkfinds.browser3;


import android.content.Context;

public class ProviderEngine {
    Context c;

    public ProviderEngine(MainActivity mainActivity) {
        c = mainActivity.getBaseContext();
    }

    @org.xwalk.core.JavascriptInterface
    public String sayMyName() {
        return "Creator";
    }
}
