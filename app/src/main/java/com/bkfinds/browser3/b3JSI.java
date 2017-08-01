package com.bkfinds.browser3;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;
import org.json.JSONTokener;

class b3JSI {
    private AppCompatActivity a;
    private Context c;

    b3JSI(AppCompatActivity parent_activity) {
        a = parent_activity;
        c = parent_activity.getBaseContext();
    }

    @JavascriptInterface
    public void showCoinbase(final String notification) {
        a.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject json = (JSONObject) new JSONTokener(notification).nextValue();

                    String address = json.getString("address");
                    Integer address_length = address.length();
                    String truncated_address = "0x" + address.substring(0, 4) + "..." + address.substring((address_length - 4), address_length);

                    String balance = json.getString("balance");

                    String notification_title = "Bal: " + balance + " Coinbase: " + truncated_address;

                    ActionBar actionbar = a.getSupportActionBar();

                    if (actionbar != null) {
                        if (!actionbar.isShowing()) {
                            actionbar.show();
                        }
                        actionbar.setTitle(notification_title);
                    } else {
                        Log.e("B3 Action Bar Error", "action bar is null");
                    }
                } catch (org.json.JSONException e) {
                    Log.e("Json Error", e.toString());
                }
            }
        });
    }
}
