package com.rangetech.eventnotify;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;

import com.crowdfire.cfalertdialog.CFAlertDialog;


public class CustomDialogues {
    private Activity activity;
    private Context context;

    public CustomDialogues(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public void displayError(String message){
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(activity)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Error")
                .setIcon(R.drawable.ic_error_black_24dp)
                .setMessage(message)
                .setCancelable(false)
                .addButton("    OK    ", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.END,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });
        builder.show();
    }
    public void displaySuccess(String message){
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(activity)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Success")
                .setIcon(R.drawable.ic_check_circle_black_24dp)
                .setMessage(message)
                .setCancelable(false)
                .addButton("    OK    ", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.END,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });
        builder.show();
    }
    public void displayWarning(String message){
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(activity)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Warning")
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setMessage(message)
                .setCancelable(false)
                .addButton("    OK    ", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.END,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });
        builder.show();
    }
}
