package com.blahblah.muzzik;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.blahblah.muzzik.MainActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;
import static com.blahblah.muzzik.MainActivity.MY_PERMISSIONS_REQUEST_WAKE_LOCK;

public class PermissionHandler {

    private int requestCode;
    private String permissionType;
    private String permissionTitle;
    private String permissionMessage;

    public PermissionHandler(String permissionType) {

        this.permissionType = permissionType;
        switch (permissionType){
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                requestCode = MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;
                permissionMessage = "Permission necessary";
                permissionTitle = "External storage permission is necessary";
                break;
            case Manifest.permission.WAKE_LOCK:
                requestCode = MY_PERMISSIONS_REQUEST_WAKE_LOCK;
                permissionMessage = "Permission necessary";
                permissionTitle = "Wake permission is necessary";
        }

    }

    public int getRequestCode() {
        return requestCode;
    }

    public String getPermissionTitle() {
        return permissionTitle;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }
    public boolean checkPermission(final Context context) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, permissionType) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permissionType)) {
                    showDialog(context);

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{permissionType},
                            getRequestCode());
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final Context context) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(getPermissionTitle());
        alertBuilder.setMessage(getPermissionMessage());
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{permissionType}, getRequestCode()
                        );
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}
