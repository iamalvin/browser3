package com.bkfinds.browser3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkInitializer;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUpdater;
import org.xwalk.core.XWalkView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements XWalkInitializer.XWalkInitListener, XWalkUpdater.XWalkUpdateListener {
    public static final String PREFS_NAME = "browser3_main_prefs";

    final Context b3 = this;
    EditText editURL;
    ProgressBar loadingBar;
    TextView loadingTxt;
    private XWalkInitializer xWalkInitializer;
    private XWalkUpdater xWalkUpdater;
    private XWalkView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingBar = (ProgressBar) findViewById(R.id.loadingbar);
        loadingTxt = (TextView) findViewById(R.id.loadingtxt);

        setContentView(R.layout.activity_main);

        xWalkInitializer = new XWalkInitializer(this, this);
        xWalkInitializer.initAsync();

        webView = (XWalkView) findViewById(R.id.goView);

        webView.addJavascriptInterface(new ProviderEngine(this), "browser3Engine");
        webView.addJavascriptInterface(new browser3KeyStoreGetter(this), "browser3KeyStoreGetter");
        webView.addJavascriptInterface(new browser3FullKeyStore(this), "browser3FullKeyStore");
        webView.addJavascriptInterface(new b3JSI(this), "b3JSI");

        webView.setResourceClient(new Browser3ResourceClient(webView));
        webView.setUIClient(new Browser3UIClient(webView));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Try to initialize again when the user completed updating and
        // returned to current activity. The initAsync() will do nothing if
        // the initialization is proceeding or has already been completed.

        xWalkInitializer.initAsync();
    }

    @Override
    public void onXWalkInitStarted() {
    }

    @Override
    public void onXWalkInitCancelled() {
        // Perform error handling here
        finish();
    }

    @Override
    public void onXWalkInitFailed() {
        if (xWalkUpdater == null) {
            xWalkUpdater = new XWalkUpdater(this, this);
        }
        xWalkUpdater.updateXWalkRuntime();
    }

    @Override
    public void onXWalkUpdateCancelled() {
        // Perform error handling here
        finish();
    }


    @Override
    public void onXWalkInitCompleted() {

        XWalkSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        XWalkCookieManager xCookieManager = new XWalkCookieManager();
        xCookieManager.setAcceptCookie(true);
        xCookieManager.setAcceptFileSchemeCookies(true);

        webView.setUserAgentString(getString(R.string.userAgentString));

        final String walletURL = getString(R.string.walletURL);

        Button goButton = (Button) findViewById(R.id.goButton);
        editURL = (EditText) findViewById(R.id.editURL);
        editURL.setSelectAllOnFocus(true);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editURL.getText().toString();
                if (isUrl(url)) {
                    if (!url.startsWith("ftp") && !url.startsWith("http")) {
                        url = "http://" + url;
                    }
                } else {
                    String prefix = "https://duckduckgo.com/?q=";
                    url = prefix + url.replace(" ", "+");
                }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.toString(), Toast.LENGTH_LONG).show();
                String address;
                String setAddresses;

                if (result.getContents().startsWith("ethereum:")) {
                    address = result.getContents().replace("ethereum:", "");
                    setAddresses = "Javascript:$('.toAddress').val('" + address + "')";
                    webView.loadUrl(setAddresses);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onBackPressed() {
        if (webView.getNavigationHistory().canGoBack()) {
            webView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_rpc_node:
                final CharSequence nodes[] = new CharSequence[]{"Mainnet (Infura)", "Ropsten (Infura)", "Rinkeby (Infura)"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Pick an RPC node, Mainnet(Infura) default;");
                builder.setItems(nodes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String chosen_node = nodes[which].toString();
                        Log.d("chosen node", chosen_node);

                        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("current_node", chosen_node);
                        editor.commit();

                        String node_from_prefs = pref.getString("current_node", nodes[0].toString());
                        Toast.makeText(b3, node_from_prefs, Toast.LENGTH_SHORT).show();

                        webView.loadUrl("javascript:window.location.reload();");
                    }
                });
                builder.show();
                break;

            default:
                break;
        }
        return true;
    }

    private boolean isUrl(String text) {
        final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

        Pattern p = Pattern.compile(URL_REGEX);
        Matcher m = p.matcher(text);
        return m.find();
    }
}