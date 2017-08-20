package com.bkfinds.browser3;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by alvin on 06/08/2017.
 */

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
