package com.mogu.androidpermission;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.PermissionChecker;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class AndroidPermission extends ReactContextBaseJavaModule {

    public AndroidPermission(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "AndroidPermission";
    }

    @ReactMethod
    public void check(String permission, Promise promise) {

        /** add by david  at 2019-03-11 start*/
        //  解决oppo手机不管授权与否都会 返回PackageManager.PERMISSION_GRANTED（已授权)
        if (RomUtils.isOppo()) {
            if (permission.equals(Manifest.permission.CAMERA)) {
                promise.resolve(RomUtils.checkCameraPermissions());
                return;
            }
            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                promise.resolve(RomUtils.checkLocationsPermission(getCurrentActivity()));
                return;
            }
        }
        /** add by david  at 2019-03-11 end*/


        // 暂时解决权限问题
        int result = PermissionChecker.checkSelfPermission(getCurrentActivity(), permission);
        promise.resolve(result == PermissionChecker.PERMISSION_GRANTED);
        // if (Build.VERSION.SDK_INT > 23) {
        //     //  Android 6.0+
        //     int result = ContextCompat.checkSelfPermission(getCurrentActivity(), permission);
        //     promise.resolve(result == PackageManager.PERMISSION_GRANTED);
        // } else {
        //     // Android 6.0 以下
        //     int result = PermissionChecker.checkSelfPermission(getCurrentActivity(), permission);
        //     promise.resolve(result == PermissionChecker.PERMISSION_GRANTED);

        // }

    }

    /*
    跳转权限设置
     */
    @ReactMethod
    public void openSettings(Promise promise) {
        try {
            String name = Build.MANUFACTURER.toString().trim().toUpperCase();
            Context context = getCurrentActivity();

            if (name.equals("MEIZU")) {
                //魅族
                MEIZU_permission(context, promise);
            } else {
                //其他
                getAppDetailSettingIntent(context, promise);
            }

        } catch (Exception e) {
            promise.reject("-1", "打开失败");
        }
    }

    public void getAppDetailSettingIntent(Context context, Promise promise) {

        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }

        if (localIntent.resolveActivity(context.getPackageManager()) != null) { //存在
            context.startActivity(localIntent);
            promise.resolve(true);
        } else {//不存在
            promise.reject("-1", "打开失败");
        }

    }

    public void MEIZU_permission(Context context, Promise promise) {

        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);

        if (intent.resolveActivity(context.getPackageManager()) != null) { //存在
            context.startActivity(intent);
            promise.resolve(true);
        } else { //不存在
            getAppDetailSettingIntent(context, promise);
            promise.reject("-1", "打开失败");
        }

    }
}
