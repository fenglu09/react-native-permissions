package com.mogu.androidpermission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.facebook.react.BuildConfig;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import pub.devrel.easypermissions.EasyPermissions;

public class AndroidPermission extends ReactContextBaseJavaModule {
    public static Promise promise = null;
    private static final int RC_CAMERA_PERM = 123;
    public static int permissionNum = 0;

    public AndroidPermission(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "AndroidPermission";
    }

//    @ReactMethod
//    public void check(String permission, Promise promise) {
//
//        // 暂时解决权限问题
//        int result = PermissionChecker.checkSelfPermission(getCurrentActivity(), permission);
//        promise.resolve(result == PermissionChecker.PERMISSION_GRANTED);
//        // if (Build.VERSION.SDK_INT > 23) {
//        //     //  Android 6.0+
//        //     int result = ContextCompat.checkSelfPermission(getCurrentActivity(), permission);
//        //     promise.resolve(result == PackageManager.PERMISSION_GRANTED);
//        // } else {
//        //     // Android 6.0 以下
//        //     int result = PermissionChecker.checkSelfPermission(getCurrentActivity(), permission);
//        //     promise.resolve(result == PermissionChecker.PERMISSION_GRANTED);
//
//        // }
//
//    }

    // targetSdkVersion 26 权限申请 start
    @ReactMethod
    public void check(String permission, Promise promise) {
//        try {
//            Context context = getCurrentActivity();
//            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
//            boolean firstRequest = sharedPreferences.getBoolean(permission, true);
//            this.promise = promise;
//            int result = PermissionChecker.checkSelfPermission(getCurrentActivity(), permission);
//            if (result != PermissionChecker.PERMISSION_GRANTED) {
//                if (firstRequest) {
//                    ActivityCompat.requestPermissions(getCurrentActivity(), new String[]{permission},
//                            100);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putBoolean(permission, false);
//                    editor.commit();
//                } else {
//                    promise.resolve(result == PermissionChecker.PERMISSION_GRANTED);
//                }
//            } else {
//                promise.resolve(result == PermissionChecker.PERMISSION_GRANTED);
//            }
//
//        } catch (Exception e) {
//            promise.reject("-1", "检测失败");
//        }
        //add by david 动态申请权限 end
        //promise.resolve(result == PermissionChecker.PERMISSION_GRANTED);

        try {
            Context context = getCurrentActivity();
            this.promise = promise;
            String[] perms = {permission};
            if (permission.equals(Manifest.permission.CAMERA)) {
                perms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
            }
            String tip = "请允许开启相关权限";

            if (EasyPermissions.hasPermissions(context, perms)) {
                for (int i = 0; i < perms.length; i++) {
                    int permission_result = PermissionChecker.checkPermission(context, perms[i], android.os.Process.myPid(), android.os.Process.myUid(), context.getPackageName());
                    if (permission_result != PermissionChecker.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, perms, 100);
                        return;
                    }
                }
                promise.resolve(true);
            } else {
                permissionNum = perms.length;
                // EasyPermissions.requestPermissions((Activity) context, tip,
                //         RC_CAMERA_PERM, perms);
                ActivityCompat.requestPermissions((Activity) context, perms, 100);

            }
        } catch (Exception e) {
            promise.reject("-1", "检测失败");
        }
    }


    @ReactMethod
    public void openNetWorkSettings(Promise promise) {
        try {
            getCurrentActivity().startActivity(new Intent(Settings.ACTION_SETTINGS));
        } catch (Exception e) {
            promise.reject("-1", "打开失败");
        }
    }

    // targetSdkVersion 26 权限申请 end


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