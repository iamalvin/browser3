package com.bkfinds.browser3;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

import java.io.IOException;
import java.io.InputStream;

class Browser3ResourceClient extends XWalkResourceClient {
    private static final String LOG_TAG;

    static {
        LOG_TAG = "Browser3_resource";
    }

    private final String finalProviderString;
    private TextView loadingTxt;
    private ProgressBar loadingBar;
    private Context c;

    Browser3ResourceClient(XWalkView webView) {
        super(webView);
        c = webView.getContext();
        loadingBar = (ProgressBar) ((Activity) c).findViewById(R.id.loadingbar);
        loadingTxt = (TextView) ((Activity) c).findViewById(R.id.loadingtxt);

        String providerString;
        providerString = "";
        InputStream input;
        AssetManager assetManager = c.getAssets();

        try {
            input = assetManager.open(c.getString(R.string.providerURL));

            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            providerString = new String(buffer);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        finalProviderString = providerString;
    }

    @Override
    public void onProgressChanged(XWalkView view, int progress) {
        if (progress < 100 && loadingBar.getVisibility() == ProgressBar.GONE) {
            loadingBar.setVisibility(ProgressBar.VISIBLE);
            loadingTxt.setVisibility(View.GONE);
        }

        loadingBar.setProgress(progress);

        if (progress == 100) {
            loadingBar.setVisibility(ProgressBar.GONE);
            loadingTxt.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoadStarted(final XWalkView view, String url) {

        view.evaluateJavascript("(function(){ return !window.web3 })();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d(LOG_TAG, "check for web3 result: " + value);

                if (value.equalsIgnoreCase("true")) {
                    view.evaluateJavascript(finalProviderString, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String scriptResult) {
                            Log.d(LOG_TAG, "Inject JS result: " + scriptResult);
                        }
                    });
                } else {
                    Log.d(LOG_TAG, " already loaded not injected");
                }
            }
        });


        super.onLoadStarted(view, url);
    }

    @Override
    public void onLoadFinished(XWalkView view, String url) {
        Log.d("url page", url);
        String sourceURL = view.getOriginalUrl();

        if (sourceURL.equals("file:///android_asset/html/wallet.html")) {
            ((EditText) ((Activity) c).findViewById(R.id.editURL)).setText(R.string.edit_url_hint);
            Log.d("wallet", "YES");
        } else {
            ((EditText) ((Activity) c).findViewById(R.id.editURL)).setText(sourceURL);
            Log.d("wallet", "NO");
        }
        super.onLoadFinished(view, url);

    }
}