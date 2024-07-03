package com.bo.testnfc.utility;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Utility {
    private Utility() {
        throw new AssertionError("Create instance of Utility is forbidden.");
    }

    /** Convert a Bundle object into a string */
    public static String bundle2String(Bundle bundle) {
        return bundle2String(bundle, 1);
    }

    /**
     * Sort by key and concatenate the Bundle contents into a string
     *
     * @param bundle The bundle to process
     * @param order  Sorting rules, 0-no sorting, 1-ascending, 2-descending
     * @return The concatenated string
     */
    public static String bundle2String(Bundle bundle, int order) {
        if (bundle == null || bundle.keySet().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>(bundle.keySet());
        if (order == 1) { //升序
            Collections.sort(list, String::compareTo);
        } else if (order == 2) {//降序
            Collections.sort(list, Collections.reverseOrder());
        }
        for (String key : list) {
            sb.append(key);
            sb.append(":");
            Object value = bundle.get(key);
            if (value instanceof byte[]) {
                sb.append(ByteUtil.bytes2HexStr((byte[]) value));
            } else {
                sb.append(value);
            }
            sb.append("\n");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /** Convert null to an empty string */
    public static String null2String(String str) {
        return str == null ? "" : str;
    }

    public static String formatStr(String format, Object... params) {
        return String.format(Locale.ENGLISH, format, params);
    }

    /** check whether src is hex format */
    public static boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }

    /** Display Toast */
    /*public static void showToast(final String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(MyApplication.app, msg, Toast.LENGTH_SHORT).show());
    }*/

    /** Display Toast */
    /*public static void showToast(int resId) {
        showToast(MyApplication.app.getString(resId));
    }*/

    /** Get success or failure information based on the result code */
    public static String getStateString(int code) {
        return code == 0 ? "success" : "failed, code:" + code;
    }

    /** Get success or failure information based on the result status */
    public static String getStateString(boolean state) {
        return state ? "success" : "failed";
    }

    /** Convert dp to px */
    /*public static int dp2px(int dp) {
        float density = MyApplication.app.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }*/

    /**Convert px to dp */
    /*public static int px2dp(int px) {
        float density = MyApplication.app.getResources().getDisplayMetrics().density;
        return Math.round(px / density);
    }*/
}
