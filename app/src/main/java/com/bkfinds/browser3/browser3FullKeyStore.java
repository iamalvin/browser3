package com.bkfinds.browser3;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

class browser3FullKeyStore {
    private static final String PREFS_NAME = "browser3_main_prefs";
    private Context thisContext;
    private AppCompatActivity a;
    private Context c;
    private WebView webView;

    browser3FullKeyStore(AppCompatActivity parent_activity) {
        a = parent_activity;
        c = parent_activity.getBaseContext();
        thisContext = c;
    }

    @JavascriptInterface
    public String getKeyStore() {
        String serialized_key_store;
        String keystore_file_name = getKeystoreFileName(thisContext);
        serialized_key_store = getFromFile(thisContext, keystore_file_name);
        return serialized_key_store;
    }

    @JavascriptInterface
    public Boolean saveKeyStore(String serialized_key_store) {
        a.runOnUiThread(new Runnable() {
            public void run() {
                webView = (WebView) a.findViewById(R.id.goView);
                String url = webView.getUrl();

                if (!url.equals("file:///android_asset/html/wallet.html")) {
                    throw new IllegalArgumentException(url);
                }
            }
        });
        String keystore_file_name = getKeystoreFileName(thisContext);
        writeToFile(serialized_key_store, thisContext, keystore_file_name);
        return true;
    }

    private String getKeystoreFileName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String node_from_prefs = pref.getString("current_node", "Mainnet (Infura)");

        Map<String, String> node_store_map = new HashMap<>();
        node_store_map.put("Mainnet (Infura)", "walletKeystore");
        node_store_map.put("Ropsten (Infura)", "ropstenWalletKeystore");
        node_store_map.put("Rinkeby (Infura)", "rinkebyWalletKeystore");
        //node_store_map.put("Kovan (Infura)", "kovanWalletKeystore");

        String file_name = node_store_map.get(node_from_prefs);
        return file_name;
    }

    private String getFromFile(Context context, String file_name) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(file_name);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            writeToFile("", context, file_name);
            ret = getFromFile(context, file_name);
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void writeToFile(String data, Context context, String file_name) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file_name, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
