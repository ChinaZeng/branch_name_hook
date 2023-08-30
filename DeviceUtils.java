package com.gaoding.foundations.sdk.core;

import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.gaoding.gdstorage.shadow.AbsShadowGDKVStorage;
import com.gaoding.shadowinterface.manager.ShadowManager;

/**
 * Created by LinHongHong on 2018/9/17 09:54
 * E-Mail Address：371655539@qq.com
 */
public class DeviceUtils {
    @Nullable
    private static volatile String sAndroidId = null;
    private static final String XEndPoint_PHONE = "2";
    private static final String XEndPoint_HUAWEI = "26";
    private static final String XEndPoint_HONOR = "23";
    private static boolean sIsAgree = false;
    /**
     * 厂商信息
     **/
    private static final String carrier = android.os.Build.MANUFACTURER;

    // 同GDKVStorageUtils.MAIN_INSTANCE_NAME
    private static final String MAIN_INSTANCE_NAME = "gaoding_instance_pref";

    /**
     * 获取适配Q的唯一id，后续所有获取设备id统一用这个方法
     * fix(2022-12-08): 修复DeviceId可能会随着系统版本（指Build.Display）的升级而发生变更。
     * 详见：https://doc.huanleguang.com/pages/viewpage.action?pageId=269091748
     * 解决思路：
     * 1. 将当下生成的deviceId写入到磁盘（沙盒）
     * 2. 每次先从磁盘拿deviceId，拿不到再重新生成
     */
    public static synchronized String getGDDeviceId(Context context) {
        return DeviceIdGenerator.INSTANCE.generate(context);
    }

    public static synchronized String getGDDevicedIdWithCheck(Context context) {
        return sIsAgree ? getGDDeviceId(context) : "";
    }

    /**
     * 获取Android Id
     *
     * @param context
     * @return
     */
    public static String getAndroidId(Context context) {
        //Log.d("woody", Log.getStackTraceString( new Throwable()));
        if (!TextUtils.isEmpty(sAndroidId)) {
            return sAndroidId;
        }
        sAndroidId = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return sAndroidId;
    }

    public static String getOSVersionRelease() {
        return android.os.Build.VERSION.RELEASE;
    }

    public int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 判断品牌：vivo
     *
     * @return
     */
    public static boolean isVivo() {
        return "vivo".equalsIgnoreCase(android.os.Build.BRAND);
    }

    /**
     * 判断品牌：魅族
     *
     * @return
     */
    public static boolean isMeizu() {
        return "Meizu".equalsIgnoreCase(android.os.Build.BRAND);
    }

    public static boolean isHuawei() {
        return "Huawei".equalsIgnoreCase(android.os.Build.BRAND);
    }

    public static boolean isHonor() {
        return "Honor".equalsIgnoreCase(android.os.Build.BRAND);
    }

    /**
     * 获取设备型号
     *
     * @return 型号
     */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    public static boolean isPad(Context context) {
        return (isHuaweiOrHonorDevice() && isForcePad() && isLargeScreen(context)) || isPadProject();
    }

    public static boolean isGeneralPad(Context context) {
        return isHuaweiOrHonorDevice() && isLargeScreen(context);
    }

    public static boolean isLargeScreen(Context context) {
        return ((context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE);
    }

    private static boolean isHuaweiOrHonorDevice() {
        return "HUAWEI".equalsIgnoreCase(carrier) || "HONOR".equalsIgnoreCase(carrier);
    }

    private static final String IS_PRELOAD_DEVICE = "is_preload_device";
    private static final String IS_FORCE_PAD = "is_force_pad";
    private static final String HAS_SHOW_PAD_SIMPLE_TIP = "has_show_pad_simple_tip";
    private static final String IS_PAD_PROJECT = "is_pad_project";

    private static AbsShadowGDKVStorage getMainKVInstance() {
        return ShadowManager.getGDKVStorageBridge().getInstance(MAIN_INSTANCE_NAME);
    }

    public static void setPreloadDevice(boolean isPreloadDevice) {
        getMainKVInstance().putInt(IS_PRELOAD_DEVICE, isPreloadDevice ? 1 : 0);
    }

    public static boolean isPreloadDevice() {
        return getMainKVInstance().getInt(IS_PRELOAD_DEVICE, 0) == 1;
    }

    public static void setForcePad(boolean isForcePad) {
        getMainKVInstance().putInt(IS_FORCE_PAD, isForcePad ? 1 : 0);
    }

    public static boolean isForcePad() {
        return getMainKVInstance().getInt(IS_FORCE_PAD, 0) == 1;
    }

    //todo 是否展示过pad精简版弹窗，该代码待换位置
    public static void setHasShowSimpleTip(boolean hasShow) {
        getMainKVInstance().putInt(HAS_SHOW_PAD_SIMPLE_TIP, hasShow ? 1 : 0);
    }

    public static boolean hasShowSimpleTip() {
        return getMainKVInstance().getInt(HAS_SHOW_PAD_SIMPLE_TIP, 0) == 1;
    }

    public static void setPadProject(boolean isPadProject) {
        getMainKVInstance().putBooleanSync(IS_PAD_PROJECT, isPadProject);
    }

    /**
     * 是否为Pad项目
     */
    public static boolean isPadProject() {
        return getMainKVInstance().getBoolean(IS_PAD_PROJECT, false);
    }


    /**
     * 所有pad设备都启用pad入口
     *
     * @param context
     * @return
     */
    public static String getXEndpoint4AllPad(Context context) {
        if (isPad(context)) {
            if (isHuawei()) {
                return XEndPoint_HUAWEI;
            }
            if (isHonor()) {
                return XEndPoint_HONOR;
            }
            return XEndPoint_HUAWEI;
        }
        return XEndPoint_PHONE;
    }

    public static String getChannelIdPad(Context context) {
        if (DeviceUtils.isHuawei() && DeviceUtils.isPad(context)) {
            return "124";
        }
        if (DeviceUtils.isHonor() && DeviceUtils.isPad(context)) {
            return "35";
        }
        return "9";
    }

    /**
     * 只有预装的pad设备才启用pad入口
     *
     * @return
     */
    public static String getXEndpoint4PreloadPad(Context context) {
        if (isPreloadDevice()) {
            if (isHuawei()) {
                return XEndPoint_HUAWEI;
            }
            if (isHonor()) {
                return XEndPoint_HONOR;
            }
            return XEndPoint_HUAWEI;
        }
        return XEndPoint_PHONE;
    }

    public static void setAgreePrivacyProtocol(boolean isAgree) {
        sIsAgree = isAgree;
    }

    public static boolean isAgreePrivacyProtocol() {
        return sIsAgree;
    }

}
