package com.bkfinds.browser3;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkJavascriptResult;

import java.math.BigInteger;
import java.util.Locale;

class ModifierDialog extends AlertDialog.Builder {
    private BigInteger one_eth = new BigInteger("1000000000000000000");
    private BigInteger one_gwei = new BigInteger("1000000000");

    ModifierDialog(Context c, String message, final XWalkJavascriptResult result) {
        super(c);

        final LayoutInflater factory = LayoutInflater.from(c);
        final View modifier_view = factory.inflate(R.layout.js_modifier_dialog, null);

        setView(modifier_view);
        setTitle("Review transaction");

        setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                result.cancel();
            }
        });

        setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                result.cancel();
            }
        });

        try {
            final JSONObject tx = new JSONObject(message);

            final EditText value_input = (EditText) modifier_view.findViewById(R.id.transaction_value);
            final EditText gas_input = (EditText) modifier_view.findViewById(R.id.gas);
            final EditText gas_price_input = (EditText) modifier_view.findViewById(R.id.gas_price);

            final String from = tx.optString("from", "N/A");
            ((EditText) modifier_view.findViewById(R.id.sender)).setText(from);
            ((EditText) modifier_view.findViewById(R.id.sender)).setKeyListener(null);

            final String to = tx.optString("to", "N/A");
            ((EditText) modifier_view.findViewById(R.id.recipient)).setText(to);
            ((EditText) modifier_view.findViewById(R.id.recipient)).setKeyListener(null);

            final String data = tx.optString("data", "0x0");
            ((EditText) modifier_view.findViewById(R.id.data)).setText(data);
            ((EditText) modifier_view.findViewById(R.id.data)).setKeyListener(null);

            String value = tx.optString("value", "0x0");
            BigInteger eth_value = new BigInteger(value, 16).divide(one_eth);
            value_input.setText(String.format(Locale.getDefault(), "%d", eth_value));

            String gas = tx.optString("gas", "0x0");
            BigInteger gas_amt = new BigInteger(gas, 16);
            gas_input.setText(String.format(Locale.getDefault(), "%d", gas_amt));

            BigInteger default_gas_price = new BigInteger("22000000000", 10);
            final String gas_price = tx.optString("gasPrice", default_gas_price.toString(16));
            BigInteger gas_price_gwei = new BigInteger(gas_price, 16).divide(one_gwei);
            gas_price_input.setText(String.format(Locale.getDefault(), "%d", gas_price_gwei));

            setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String reviewed_value = new BigInteger(value_input.getText().toString()).multiply(one_eth).toString(16);
                    String reviewed_gas = new BigInteger(gas_input.getText().toString()).toString(16);
                    String reviewed_gas_price = new BigInteger(gas_price_input.getText().toString()).multiply(one_gwei).toString(16);

                    try {
                        JSONObject txParams = new JSONObject();
                        txParams.put("from", from);
                        txParams.put("to", to);
                        txParams.put("data", data);
                        txParams.put("value", reviewed_value);
                        txParams.put("gas", reviewed_gas);
                        txParams.put("gas_price", reviewed_gas_price);

                        JSONObject reviewed_tx = new JSONObject();
                        reviewed_tx.put("success", true);
                        reviewed_tx.put("txParams", txParams);

                        String response = reviewed_tx.toString();
                        result.confirmWithResult(response);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
