package com.bkfinds.browser3;

import android.content.Context;
import android.widget.Toast;

import org.xwalk.core.JavascriptInterface;

class web3Provider {
    private Context c;
    private MainActivity m;

    public web3Provider(MainActivity mainActivity) {
        c = mainActivity.getBaseContext();
        m = mainActivity;
    }

    @JavascriptInterface
    public void setProvider(final String provider) {
        m.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(m, provider, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
