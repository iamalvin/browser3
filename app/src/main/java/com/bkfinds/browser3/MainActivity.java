package com.bkfinds.browser3;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    EditText editURL;
    WebView webView;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        final Context b3 = this;

        if (actionBar != null) {
            actionBar.hide();
        }

        webView = (WebView) findViewById(R.id.goView);
        webView.getSettings().setJavaScriptEnabled(true);


        webView.addJavascriptInterface(new browser3KeyStoreGetter(this), "browser3KeyStoreGetter");
        webView.addJavascriptInterface(new browser3FullKeyStore(this), "browser3FullKeyStore");
        webView.addJavascriptInterface(new b3JSI(this), "b3JSI");


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                if (defaultValue.equalsIgnoreCase("password")) {

                    final LayoutInflater factory = LayoutInflater.from(b3);
                    final View pass_dialog_view = factory.inflate(R.layout.js_prompt_dialog, null);

                    ((EditText) pass_dialog_view.findViewById(R.id.pass_put)).setText("");


                    new AlertDialog.Builder(b3)
                            .setView(pass_dialog_view)
                            .setTitle(message)
                            .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String value = ((EditText) pass_dialog_view.findViewById(R.id.pass_put)).getText().toString();
                                    result.confirm(value);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    result.cancel();
                                }
                            })
                            .show();
                    return true;
                } else if (defaultValue.equalsIgnoreCase("confirmation")) {

                    final LayoutInflater factory = LayoutInflater.from(b3);
                    final View confirm_dialog_view = factory.inflate(R.layout.js_custom_prompt_dialog, null);

                    ((EditText) confirm_dialog_view.findViewById(R.id.confirmation_input)).setText("");
                    ((TextView) confirm_dialog_view.findViewById(R.id.confirmation_info)).setText(message);

                    new AlertDialog.Builder(b3)
                            .setView(confirm_dialog_view)
                            .setTitle("Type 'yes' to confirm transaction.")
                            .setPositiveButton("Yes, continue.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String value = ((EditText) confirm_dialog_view.findViewById(R.id.confirmation_input)).getText().toString();
                                    result.confirm(value);
                                }
                            })
                            .setNegativeButton("Undo this.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    result.cancel();
                                }
                            })
                            .show();
                    return true;
                } else {
                    return super.onJsPrompt(view, url, message, defaultValue, result);
                }
            }

        });


        final String walletURL = "file:///android_asset/html/wallet.html";

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.equals("file:///android_asset/html/wallet.html")) {
                    ((EditText) findViewById(R.id.editURL)).setText("Wallet");
                } else {
                    ((EditText) findViewById(R.id.editURL)).setText(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("url page", url);
                if (url.equals("file:///android_asset/html/wallet.html")) {
                    Log.d("wallet", "YES");
                } else {
                    Log.d("wallet", "NO");
                    attachScriptFile(view, "https://com.bkfinds.browser3/android_asset/js/browser3Bundle.js", "browser3Bundle");
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (request.getUrl().toString().equals("https://com.bkfinds.browser3/android_asset/js/browser3Bundle.js")) {
                        Log.d("resource intercepted", "true");
                        try {
                            InputStream input;
                            input = getAssets().open("js/browser3Bundle.eth-lightwallet.hooked-web3-provider.js");

                            return new WebResourceResponse("text/javascript", "UTF-8", input);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return null;
                    }
                }
                return null;
            }

            @Override
            @TargetApi(20)
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    if (url.equals("https://com.bkfinds.browser3/android_asset/js/browser3Bundle.js")) {
                        try {
                            InputStream input;
                            input = getAssets().open("js/browser3Bundle.eth-lightwallet.hooked-web3-provider.js");
                            input.read();
                            input.close();

                            return new WebResourceResponse("text/javascript", "UTF-8", input);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return super.shouldInterceptRequest(view, url);
                    }
                }
                return null;
            }
        });


        Button goButton = (Button) findViewById(R.id.goButton);
        editURL = (EditText) findViewById(R.id.editURL);
        editURL.setSelectAllOnFocus(true);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editURL.getText().toString();
                webView.loadUrl(url);
            }
        });

        Button walletButton = (Button) findViewById(R.id.walletButton);

        walletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(walletURL);
            }
        });

        webView.loadUrl(walletURL);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void attachScriptFile(WebView view, String src, String id) {
        Log.d("script being attached", id);
        Log.d("script source", src);
        view.loadUrl(
                "javascript:" + "(function (){ " +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var script = document.createElement('script');" +
                        "script.type='text/javascript';" +
                        "script.id='browser3Bundle';" +
                        "script.src='https://com.bkfinds.browser3/android_asset/js/browser3Bundle.js';" +
                        "parent.appendChild(script);" +
                        "})();"
        );
    }
}