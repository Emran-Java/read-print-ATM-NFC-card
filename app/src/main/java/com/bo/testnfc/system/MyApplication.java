package com.bo.testnfc.system;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.bo.testnfc.app_data.Constant;
import com.bo.testnfc.utility.CacheHelper;
import com.bo.testnfc.utility.LogUtil;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.etc.ETCOptV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2;
import com.sunmi.pay.hardware.aidlv2.print.PrinterOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;
import com.sunmi.pay.hardware.aidlv2.rfid.RFIDOptV2;
import com.sunmi.pay.hardware.aidlv2.security.DevCertManagerV2;
import com.sunmi.pay.hardware.aidlv2.security.NoLostKeyManagerV2;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2;
import com.sunmi.pay.hardware.aidlv2.tax.TaxOptV2;
import com.sunmi.pay.hardware.aidlv2.test.TestOptV2;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import java.util.Locale;

import sunmi.paylib.SunmiPayKernel;

public class MyApplication extends Application {
    public static MyApplication app;

    public BasicOptV2 basicOptV2;                   // Get the basic operation module
    public ReadCardOptV2 readCardOptV2;             // Get the card reader module
    public PinPadOptV2 pinPadOptV2;                 // PinPad operation module
    public SecurityOptV2 securityOptV2;             // security operation module
    public EMVOptV2 emvOptV2;                       // EMV operation module
    public TaxOptV2 taxOptV2;                       // tax control operation module
    public ETCOptV2 etcOptV2;                       // ETC operation module
    public PrinterOptV2 printerOptV2;               // print operation module
    public TestOptV2 testOptV2;                     // test operation module
    public DevCertManagerV2 devCertManagerV2;       // Certificate Operation Module
    public NoLostKeyManagerV2 noLostKeyManagerV2;   // NoLostKey Operation Module

    //    public HCEManagerV2 hceManagerV2;               // HCE Operation Module
    public RFIDOptV2 rfidOptV2;                     // RFID Operation Module
    public SunmiPrinterService sunmiPrinterService; // Print module

    //public IScanInterface scanInterface;            // Scan code module

//    public BiometricManagerV2 mBiometricManagerV2;            // Biometric Module

    private boolean connectPaySDK;//是否已连接PaySDK

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        initLocaleLanguage();
        bindPrintService();
        /*initEmvTTS();
        bindScannerService();*/
    }

    public static void initLocaleLanguage() {
        Resources resources = app.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        int showLanguage = CacheHelper.getCurrentLanguage();
        if (showLanguage == Constant.LANGUAGE_AUTO) {
            LogUtil.e(Constant.TAG, config.locale.getCountry() + "---default language");
            config.locale = Resources.getSystem().getConfiguration().locale;
        } else if (showLanguage == Constant.LANGUAGE_ZH_CN) {
            LogUtil.e(Constant.TAG, "Chinese");
            config.locale = Locale.SIMPLIFIED_CHINESE;
        } else if (showLanguage == Constant.LANGUAGE_EN_US) {
            LogUtil.e(Constant.TAG, "English");
            config.locale = Locale.ENGLISH;
        } else if (showLanguage == Constant.LANGUAGE_JA_JP) {
            LogUtil.e(Constant.TAG, "Japan");
            config.locale = Locale.JAPAN;
        }
        resources.updateConfiguration(config, dm);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.e(Constant.TAG, "onConfigurationChanged");
    }

    public boolean isConnectPaySDK() {
        return connectPaySDK;
    }

    /**
     * bind PaySDK service
     */
    public void bindPaySDKService() {
        final SunmiPayKernel payKernel = SunmiPayKernel.getInstance();
        payKernel.initPaySDK(this, new SunmiPayKernel.ConnectCallback() {
            @Override
            public void onConnectPaySDK() {
                LogUtil.e(Constant.TAG, "onConnectPaySDK...");
                emvOptV2 = payKernel.mEMVOptV2;
                basicOptV2 = payKernel.mBasicOptV2;
                pinPadOptV2 = payKernel.mPinPadOptV2;
                readCardOptV2 = payKernel.mReadCardOptV2;
                securityOptV2 = payKernel.mSecurityOptV2;
                taxOptV2 = payKernel.mTaxOptV2;
                etcOptV2 = payKernel.mETCOptV2;
                printerOptV2 = payKernel.mPrinterOptV2;
                testOptV2 = payKernel.mTestOptV2;
                devCertManagerV2 = payKernel.mDevCertManagerV2;
                noLostKeyManagerV2 = payKernel.mNoLostKeyManagerV2;
//                mBiometricManagerV2 = payKernel.mBiometricManagerV2;
//                hceManagerV2 = payKernel.mHCEManagerV2;
                rfidOptV2 = payKernel.mRFIDOptV2;
                connectPaySDK = true;
            }

            @Override
            public void onDisconnectPaySDK() {
                LogUtil.e(Constant.TAG, "onDisconnectPaySDK...");
                connectPaySDK = false;
                emvOptV2 = null;
                basicOptV2 = null;
                pinPadOptV2 = null;
                readCardOptV2 = null;
                securityOptV2 = null;
                taxOptV2 = null;
                etcOptV2 = null;
                printerOptV2 = null;
                devCertManagerV2 = null;
                noLostKeyManagerV2 = null;
//                mBiometricManagerV2 = null;
//                hceManagerV2 = null;
                rfidOptV2 = null;
                //Utility.showToast(R.string.connect_fail);
            }
        });
    }

    /**
     * bind printer service
     */
    private void bindPrintService() {
        try {
            InnerPrinterManager.getInstance().bindService(this, new InnerPrinterCallback() {
                @Override
                protected void onConnected(SunmiPrinterService service) {
                    sunmiPrinterService = service;
                }

                @Override
                protected void onDisconnected() {
                    sunmiPrinterService = null;
                }
            });
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
    }

    /**
     * bind scanner service
     */
   /* public void bindScannerService() {
        Intent intent = new Intent();
        intent.setPackage("com.sunmi.scanner");
        intent.setAction("com.sunmi.scanner.IScanInterface");
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                scanInterface = IScanInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                scanInterface = null;
            }
        }, Service.BIND_AUTO_CREATE);
    }
*/
//    private void initEmvTTS() {
//        EmvTTS.getInstance().init();
//    }

}
