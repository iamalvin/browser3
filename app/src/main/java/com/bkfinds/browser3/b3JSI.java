package com.bkfinds.browser3;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

class b3JSI {
    private static final String PREFS_NAME = "browser3_main_prefs";
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
                        actionbar.setTitle("");
                        actionbar.setSubtitle(notification_title);
                    } else {
                        Log.e("B3 Action Bar Error", "action bar is null");
                    }
                } catch (org.json.JSONException e) {
                    Log.e("Json Error", e.toString());
                }
            }
        });
    }

    @JavascriptInterface
    public String getCurrentNode() {
        Map<String, String> node_url_map = new HashMap<>();
        node_url_map.put("Mainnet (Infura)", "https://mainnet.infura.io/KQVpBo7jJIBfKQLFg60S");
        node_url_map.put("Ropsten (Infura)", "https://ropsten.infura.io/KQVpBo7jJIBfKQLFg60S");
        node_url_map.put("Rinkeby (Infura)", "https://rinkeby.infura.io/KQVpBo7jJIBfKQLFg60S");
        node_url_map.put("Kovan (Infura)", "https://kovan.infura.io/KQVpBo7jJIBfKQLFg60S");

        SharedPreferences pref = a.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String node_from_prefs = pref.getString("current_node", "Mainnet (Infura)");

        String node_url = node_url_map.get(node_from_prefs);
        Toast.makeText(a, node_from_prefs, Toast.LENGTH_SHORT).show();
        return node_url;
    }

    @JavascriptInterface
    public void scanForAddress() {
        IntentIntegrator integrator = new IntentIntegrator(a);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan QR Code");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }
}
