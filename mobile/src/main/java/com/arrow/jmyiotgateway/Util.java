package com.arrow.jmyiotgateway;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.arrow.acn.api.AcnApiService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.arrow.jmyiotgateway.Constant.DEFAULT_API_KEY;
import static com.arrow.jmyiotgateway.Constant.DEFAULT_API_SECRET;

public class Util {
    private final static String TAG = Util.class.getSimpleName();

    public static void showSimpleAlertDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        //Retrieve all services that can match the given intent
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentServices(implicitIntent, 0);

        //Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        //Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        //Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        //Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    public static String getFormattedDateTime(Long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String formattedDate = format.format(date);
        return formattedDate;
    }

    public static String toBinaryString(byte... bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Integer.toBinaryString(256 + b));
        }
        return sb.toString();
    }

    public static String getVersionNumber() {
        String version = BuildConfig.VERSION_NAME;
        return version;
    }
}
