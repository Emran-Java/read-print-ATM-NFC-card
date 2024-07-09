package com.bo.testnfc.utility;

import android.os.Build;
import android.text.TextUtils;

public final class DeviceUtil {
    private DeviceUtil() {
        throw new AssertionError("create instance of DeviceUtil is prohibited");
    }

    /** 是否是P1N */
    public static boolean isP1N() {
        String model = getModel();
        return model.matches("p1n(-.+)?");
    }

    /** 是否是P1_4G */
    public static boolean isP14G() {
        String model = getModel();
        return model.matches("(p1\\(4g\\)|p1_4g)(-.+)?");
    }

    /** 是否是P2lite */
    public static boolean isP2Lite() {
        String model = getModel();
        return model.matches("p2lite(-.+)?");
    }

    /** 是否是P2 */
    public static boolean isP2() {
        String model = getModel();
        return model.matches("p2(-.+)?");
    }

    /** 是否是P2_PRO */
    public static boolean isP2Pro() {
        String model = getModel();
        return model.matches("p2_pro(-.+)?");
    }

    /** 是否是P2mini */
    public static boolean isP2Mini() {
        String model = getModel();
        return model.matches("p2mini(-.+)?");
    }

    /** 是否是巴西CKD */
    public static boolean isBrazilCKD() {
        String model = Build.MODEL.toLowerCase();
        return model.matches("(p2-b|p2mini-b).*");
    }

    /** 是否是p2_smartpad(Banjul改名为p2_retail，再改名为p2_smartpad) */
    public static boolean isP2SmartPad() {
        String model = getModel();
        return model.matches("(p2_smartpad|p2_retail|pinpad|qcm2150|t6a10)(-.+)?");
    }

    /** 是否是P2_Xpro(P2H改名为P2_Xpro) */
    public static boolean isP2XPro() {
        String model = getModel();
        return model.matches("(p2_xpro|p2h|uis8581e5h10_natv)(-.+)?");
    }

    /** 是否是P2_SE */
    public static boolean isP2Se() {
        String model = getModel();
        return model.matches("(p2_se|bengal_32go)(-.+)?");
    }

    /** 是否是Terminal机型（无NFC功能） */
    public static boolean isTossTerminal() {
        String model = getModel();
        return model.matches("toss_terminal(-.+)?");
    }

    /** 是否是Front机型（有NFC功能） */
    public static boolean isTossFront() {
        String model = getModel();
        return model.matches("toss_front(-.+)?");
    }

    /** 是否是P2_LITE_SE */
    public static boolean isP2liteSE() {
        String model = getModel();
        return model.matches("p2_lite_se(-.+)?");
    }

    public static boolean isCT621() {
        String model = getModel();
        return model.matches("(ct621|x30tr|430trs)(-.+)?");
    }

    public static boolean isP3Mix() {
        String model = getModel();
        return model.matches("p3_mix(-.+)?");
    }

    public static boolean isP3() {
        String model = getModel();
        return model.matches("p3(-.+)?");
    }

    /** 是否是FT2 */
    public static boolean isFT2() {
        String model = getModelWithDeviceCode();
        return model.matches("(ft2|t6711|t6712)(-.+)?");
    }

    /** 是否是FT2Mini */
    public static boolean isFT2Mini() {
        String model = getModel();
        return model.matches("ft2mini(-.+)?");
    }

    /** 是否是V2_SE */
    public static boolean isV2SE() {
        String model = getModel();
        return model.matches("(v2_se|xqt530)(-.+)?");
    }

    /** 是否是V3_MIX */
    public static boolean isV3Mix() {
        String model = getModel();
        return model.matches("(v3_mix)(-.+)?");
    }

    /** 是否是F2-H */
    public static boolean isF2H() {
        String model = getModelWithDeviceCode();
        return model.matches("(f2-h|t6f01)(-.+)?");
    }

    /** 是否是T3B00（蜻蜓） */
    public static boolean isT3B00() {
        String model = getModelWithDeviceCode();
        return !isF2H() && model.matches("(t3b00|f2|p371)(-.+)?"); // 蜻蜓T3B00
    }

    /** 是否是金融设备 */
    public static boolean isFinanceDevice() {
        return isP1N() || isP14G() || isP2() || isP2Pro() || isP2Lite()
                || isP2Mini() || isP2SmartPad() || isP2XPro() || isP2Se() || isTossFront() || isTossTerminal()
                || isP2liteSE() || isCT621() || isP3Mix() || isP3();
    }

    /** 是否是非金融设备 */
    public static boolean isNPDevice() {
        return isFT2() || isFT2Mini() || isV2SE() || isV3Mix() || isF2H() || isT3B00();
    }

    /** 获取model */
    private static String getModel() {
        String model = SystemPropertiesUtil.get("ro.sunmi.hardware");
        if (!TextUtils.isEmpty(model)) {
            return model.toLowerCase();
        }
        String regex = "(p1n|p1_4g|p2|p2_pro|p2lite|p2mini|p2_smartpad|p2_retail|pinpad"
                + "|p2_xpro|p2h|p2_se|p2_lite_se|toss|ct621|p3_mix|p3|ft2|v2_se|v3_mix).*";
        model = Build.PRODUCT.toLowerCase();
        if (model.matches(regex)) {
            return model;
        }
        return Build.MODEL.toLowerCase();
    }

    /** 获取model */
    private static String getModelWithDeviceCode() {
        String model = SystemPropertiesUtil.get("ro.sunmi.hardware");
        if (TextUtils.isEmpty(model)) {
            model = SystemPropertiesUtil.get("ro.sunmi.devicecode");
        }
        if (TextUtils.isEmpty(model)) {
            model = Build.MODEL;
        }
        if (TextUtils.isEmpty(model)) {
            model = Build.UNKNOWN;
        }
        return model == null ? "" : model.toLowerCase();
    }
}
