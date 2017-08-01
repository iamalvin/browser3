package com.bkfinds.browser3;


import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

class browser3KeyStoreGetter {
    private Context thisContext;

    browser3KeyStoreGetter(Context c) {
        thisContext = c;
    }

    @JavascriptInterface
    public String getKeyStore() {
        String serialized_key_store;
        serialized_key_store = getFromFile(thisContext);
        //Log.i("serialized w getter", serialized_key_store);
        return serialized_key_store;
    }

    private String getFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("walletKeystore");

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
            writeToFile("", context);
            ret = getFromFile(context);
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("walletKeystore", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
