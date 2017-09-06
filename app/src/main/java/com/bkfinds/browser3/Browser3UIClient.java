package com.bkfinds.browser3;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.xwalk.core.XWalkJavascriptResult;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

class Browser3UIClient extends XWalkUIClient {
    private Context c;


    Browser3UIClient(XWalkView webView) {
        super(webView);
        c = webView.getContext();
    }

    @Override
    public boolean onJsPrompt(XWalkView webView, String url, String message, String defaultValue, final XWalkJavascriptResult result) {
        if (defaultValue.equalsIgnoreCase("password")) {

            final LayoutInflater factory = LayoutInflater.from(c);
            final View pass_dialog_view = factory.inflate(R.layout.js_prompt_dialog, null);

            ((EditText) pass_dialog_view.findViewById(R.id.pass_put)).setText("");

            new AlertDialog.Builder(c)
                    .setView(pass_dialog_view)
                    .setTitle(message)
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String value = ((EditText) pass_dialog_view.findViewById(R.id.pass_put)).getText().toString();
                            result.confirmWithResult(value);
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
            final LayoutInflater factory = LayoutInflater.from(c);
            final View confirm_dialog_view = factory.inflate(R.layout.js_custom_prompt_dialog, null);

            ((EditText) confirm_dialog_view.findViewById(R.id.confirmation_input)).setText("");
            ((TextView) confirm_dialog_view.findViewById(R.id.confirmation_info)).setText(message);

            new AlertDialog.Builder(c)
                    .setView(confirm_dialog_view)
                    .setTitle("Type 'yes' to confirm transaction.")
                    .setPositiveButton("Yes, continue.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String value = ((EditText) confirm_dialog_view.findViewById(R.id.confirmation_input)).getText().toString();
                            result.confirmWithResult(value);
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
        } else if (defaultValue.equalsIgnoreCase("modification, confirmation")) {
            AlertDialog.Builder modifier = new ModifierDialog(c, message, result);
            modifier.show();
            return true;
        } else {
            return super.onJsPrompt(webView, url, message, defaultValue, result);
        }
    }
}
