package com.bo.testnfc.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bo.testnfc.R;
import com.bo.testnfc.system.MyApplication;
import com.bo.testnfc.utility.LogUtil;
import com.bo.testnfc.utility.SystemDateTime;
import com.bo.testnfc.utility.Utility;
import com.bo.testnfc.wrapper.CheckCardCallbackV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import com.sunmi.peripheral.printer.InnerResultCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity_bkp extends AppCompatActivity {

    private TextView txtDisplayMsg;
    private Button btnNFC;
    private Button btnPrint;

    private SunmiPrinterService sunmiPrinterService;

    private static final String TAG = "TestNFC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initFunctions();
        listeners();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!MyApplication.app.isConnectPaySDK()) {
            MyApplication.app.bindPaySDKService();
        }
    }


    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        cancelCheckCard();
        super.onDestroy();
    }
    //---end over ride methods---


    private void listeners() {
        btnNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCard();
            }
        });

        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPrintClick();
            }
        });
    }

    private void initFunctions() {
        connectToSDK();
    }

    private void initView() {

        setContentView(R.layout.activity_main);
        txtDisplayMsg = findViewById(R.id.txtDisplayMsg);

        btnNFC = findViewById(R.id.btnNFC);
        btnPrint = findViewById(R.id.btnPrint);
    }


    /************************************************************/
    /*For NFC card read*/
    /************************************************************/
    private final Handler handler = new Handler();
    private String result = "";
    private final Map<String, Long> timeMap = new LinkedHashMap<>();

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e("TestNFC", "findMagCard:" + Utility.bundle2String(info));
            showSpendTime();
        }

        /**
         * Find IC card
         *
         * @param info return data，contain the following keys:
         *             <br/>cardType: card type (int)
         *             <br/>atr: card's ATR (String)
         */
        @Override
        public void findICCardEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findICCard:" + Utility.bundle2String(info));
            showSpendTime();
        }

        /**
         * Find RF card
         *
         * @param info return data，contain the following keys:
         *             <br/>cardType: card type (int)
         *             <br/>uuid: card's UUID (String)
         *             <br/>ats: card's ATS (String)
         *             <br/>sak: card's SAK, if exist (int) (M1 S50:0x08, M1 S70:0x18, CPU:0x28)
         *             <br/>cardCategory: card's category,'A' or 'B', if exist (int)
         *             <br/>atqa: card's ATQA, if exist (byte[])
         */
        @Override
        public void findRFCardEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findRFCard:" + Utility.bundle2String(info));
            handleResult(true, info);
            showSpendTime();
        }

        /**
         * Check card error
         *
         * @param info return data，contain the following keys:
         *             <br/>cardType: card type (int)
         *             <br/>code: the error code (String)
         *             <br/>message: the error message (String)
         */
        @Override
        public void onErrorEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            int code = info.getInt("code");
            String msg = info.getString("message");
            String error = "onError:" + msg + " -- " + code;
            LogUtil.e(TAG, error);
            showToast(error);
            txtDisplayMsg.setText(error);
            handleResult(false, info);
            showSpendTime();
        }
    };

    protected void addEndTime(String key) {
        timeMap.put("end_" + key, SystemClock.elapsedRealtime());
    }

    protected void addStartTimeWithClear(String key) {
        timeMap.clear();
        timeMap.put("start_" + key, SystemClock.elapsedRealtime());
    }

    protected void showSpendTime() {
        Map<String, Long> map = new LinkedHashMap<>(timeMap);
        Long startValue = null, endValue = null;
        for (String key : map.keySet()) {
            if (!key.startsWith("start_")) {
                continue;
            }
            key = key.substring("start_".length());
            startValue = map.get("start_" + key);
            endValue = map.get("end_" + key);
            if (startValue == null || endValue == null) {
                continue;
            }
            LogUtil.e(TAG, key + ", spend time(ms):" + (endValue - startValue));
        }
    }

    private void handleResult(boolean success, Bundle info) {
        if (isFinishing()) {
            return;
        }
        handler.post(() -> {
//            totalCount++;
            if (success) {
//                successCount++;
                /*tvDepictor.setText(getString(R.string.card_check_rf_card));
                tvCardType.setText(getCardType(info.getInt("cardType")));
                tvCardCate.setText(getCardCategory(info.getInt("cardCategory")));
                tvUUID.setText(info.getString("uuid"));
                tvAts.setText(info.getString("ats"));*/

                result = "Depictor: Find RF card\n" +
                        "CardType: " + getCardType(info.getInt("cardType")) + "\n" +
                        "CardCate: " + getCardCategory(info.getInt("cardCategory")) + "\n" +
                        "UUID: " + info.getString("uuid") + "\n" +
                        "Ats: " + info.getString("ats") + "\n" +
                        "";
            } else {//on Error
//                failCount++;
//                tvDepictor.setText(getString(R.string.card_check_card_error));
                result = "Depicter: Check card error";
            }

            /*tvTotal.setText(Utility.formatStr("%s %d", getString(R.string.card_total), totalCount));
            tvSuccess.setText(Utility.formatStr("%s %d", getString(R.string.card_success), successCount));
            tvFail.setText(Utility.formatStr("%s %d", getString(R.string.card_fail), failCount));*/

            txtDisplayMsg.setText(result);
            // 继续检卡
            if (!isFinishing()) {
                handler.postDelayed(this::checkCard, 500);
            }
        });
    }

    private void connectToSDK() {
        if (!MyApplication.app.isConnectPaySDK()) {
            MyApplication.app.bindPaySDKService();
            showToast("connecting to sdk");
            //return;
        } else {
            showToast("SDK connected");
        }
    }

    private void checkCard() {
        try {
            int cardType = CardType.NFC.getValue() | CardType.MIFARE.getValue() | CardType.FELICA.getValue() | CardType.ISO15693.getValue();
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCardType(int type) {
        for (CardType ct : CardType.values()) {
            if (type == ct.getValue()) {
                return ct.toString();
            }
        }
        return "Unknown";
    }

    private String getCardCategory(int cate) {
        switch (cate) {
            case 'A':
                return "A";
            case 'B':
                return "B";
            default:
                return "Unknown";
        }
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(CardType.NFC.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /************************************************************/
     /*for printer*/
    /************************************************************/
    private void onPrintClick() {
        try {
            if (!checkPrint()) {
                return;
            }

            int repeatCount = 1;
            int textSize = 24;
            setHeight(0x12);
            String hHmmss = SystemDateTime.getHHmmss();
            addStartTimeWithClear("print total");
            sunmiPrinterService.enterPrinterBuffer(true);
            sunmiPrinterService.printTextWithFont(hHmmss + "\n", "", textSize, innerResultCallbcak);
            for (int i = 0; i < repeatCount; i++) {
                sunmiPrinterService.printTextWithFont(result + "\n", "", textSize, innerResultCallbcak);
            }
            sunmiPrinterService.exitPrinterBufferWithCallback(true, innerResultCallbcak);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkPrint() {
        if (MyApplication.app.sunmiPrinterService == null) {
            showToast("Print not supported");
            return false;
        }
        sunmiPrinterService = MyApplication.app.sunmiPrinterService;
        return true;
    }

    public void setHeight(int height) throws RemoteException {
        byte[] returnText = new byte[3];
        returnText[0] = 0x1B;
        returnText[1] = 0x33;
        returnText[2] = (byte) height;
        sunmiPrinterService.sendRAWData(returnText, null);
    }

    private boolean is = true;
    private final InnerResultCallback innerResultCallbcak = new InnerResultCallback() {
        @Override
        public void onRunResult(boolean isSuccess) {
            LogUtil.e("lxy", "isSuccess:" + isSuccess);
            if (is) {
                try {
                    sunmiPrinterService.printTextWithFont(SystemDateTime.getHHmmss() + "\n", "", 30, innerResultCallbcak);
                    sunmiPrinterService.lineWrap(6, innerResultCallbcak);
                    is = false;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onReturnString(String result) {
            LogUtil.e("lxy", "result:" + result);
        }

        @Override
        public void onRaiseException(int code, String msg) {
            addEndTime("print total");
            LogUtil.e("lxy", "code:" + code + ",msg:" + msg);
            showSpendTime();
        }

        @Override
        public void onPrintResult(int code, String msg) {
            addEndTime("print total");
            LogUtil.e("lxy", "code:" + code + ",msg:" + msg);
            showSpendTime();
        }
    };


    //helper functions

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


}