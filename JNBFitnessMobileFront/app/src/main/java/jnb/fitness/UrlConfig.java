package jnb.fitness;

import android.content.Context;
import android.os.Build;

public class UrlConfig {
    private static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("emulator")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("google_sdk");
    }



    public static String getApiBaseUrl(Context ctx) {
        if (isEmulator()) {
            return ctx.getString(R.string.base_url_debug_api);
        } else {
            return ctx.getString(R.string.base_url_device_api);
        }
    }

    public static String getDebugApiBaseUrl(Context ctx){
        return ctx.getString(R.string.base_url_debug_api);
    }
    public static String getDeviceApiBaseUrl(Context ctx){
        return ctx.getString(R.string.base_url_device_api);
    }
}
