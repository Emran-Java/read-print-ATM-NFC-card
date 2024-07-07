package com.bo.testnfc.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bo.testnfc.R;
import com.bo.testnfc.app_data.Constant;
import com.bo.testnfc.system.MyApplication;
import com.bo.testnfc.utility.LogUtil;
import com.bo.testnfc.utility.emv.EmvUtil;
import com.bo.testnfc.utility.nfc_ic.ThreadPoolUtil;
import com.bo.testnfc.wrapper.CheckCardCallbackV2Wrapper;
import com.sunmi.pay.hardware.aidl.bean.CardInfo;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVTransDataV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Map;

public class NfcIcActivity extends AppCompatActivity {

    private EMVOptV2 emvOptV2 = MyApplication.app.emvOptV2;
    private int mCarType;

    private Button btnNFC;

    private TextView tvCardNo;
    private GetEMVCardInfo mGetEMVCardInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_ic);

        tvCardNo = findViewById(R.id.tvCardNo);
        connectToSDK();

        btnNFC = findViewById(R.id.btnNFC);

        ThreadPoolUtil.executeInCachePool(
                () -> {
                    EmvUtil.initKey();
                    EmvUtil.initAidAndRid();
                    Map<String, String> map = EmvUtil.getConfig(EmvUtil.COUNTRY_INDIA);
                    EmvUtil.setTerminalParam(map);
                }
        );

        btnNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvCardNo.setText("Ready");
                checkCard();
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
            if(emvOptV2==null)
                emvOptV2 = MyApplication.app.emvOptV2;
            emvOptV2.initEmvProcess(); // clear all TLV data
           // showLoadingDialog("swipe card or insert card");
            int cardType = AidlConstantsV2.CardType.NFC.getValue() | AidlConstantsV2.CardType.IC.getValue();
            //addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
//            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard:" + bundle);
//            showSpendTime();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
//            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
//            showSpendTime();
            mCarType = AidlConstantsV2.CardType.IC.getValue();
            runOnUiThread(
                    () -> {
//                        String text = getString(R.string.card_atr) + atr;
                        LogUtil.e("dMoneyLog", "card_atr:" + atr);
                        //tvAtr.setText(text);
                    }
            );
            transactProcess();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            //addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            //showSpendTime();
            mCarType = AidlConstantsV2.CardType.NFC.getValue();
            runOnUiThread(
                    () -> {
//                        String text = getString(R.string.card_uuid) + uuid;
//                        tvUUID.setText(text);
                        LogUtil.e("dMoneyLog", "card_uuid:" + uuid);
//                        tvAtr.setText(R.string.card_atr);
                    }
            );
            transactProcess();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            //addEndTime("checkCard()");
            String error = "onError:" + message + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
//            dismissLoadingDialog();
//            showSpendTime();
        }
    };

    private void transactProcess() {
        LogUtil.e(Constant.TAG, "transactProcess");
        try {
            EMVTransDataV2 emvTransData = new EMVTransDataV2();
            emvTransData.amount = "1";
            emvTransData.flowType = 0x02;
            emvTransData.cardType = mCarType;
//            addTransactionStartTimes();

//            GetCardInfo.getInstance(this, emvOptV2).getEMVListener();
//            emvOptV2.transactProcess(emvTransData, mEMVListener);

            mGetEMVCardInfo = GetEMVCardInfo.getInstance(this, emvOptV2);
            emvOptV2.transactProcess(emvTransData, mGetEMVCardInfo.getEMVListener());

            mGetEMVCardInfo.getCardInfoListener(new ListenEMVCardInfo() {
                @Override
                public void onListenCardInfo(CardInfo cardInfo) {
                    String displayMessage = "CardNo: " + cardInfo.cardNo + " \nExpireDate: " + cardInfo.expireDate + " \nServiceCode: " + cardInfo.serviceCode;
                    tvCardNo.setText(displayMessage);
                    LogUtil.e(Constant.TAG, "cardNumber::" + cardInfo.cardNo + " expireDate:" + cardInfo.expireDate + " serviceCode:" + cardInfo.serviceCode);
                }

                @Override
                public void onListenCardInfoError(String errorMessage) {
                    showToast(errorMessage);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*

    private final EMVListenerV2 mEMVListener = new EMVListenerV2.Stub() {

        @Override
        public void onWaitAppSelect(List<EMVCandidateV2> appNameList, boolean isFirstSelect) throws RemoteException {
            //addEndTime("onWaitAppSelect()");
            LogUtil.e(Constant.TAG, "onWaitAppSelect isFirstSelect:" + isFirstSelect);
            emvOptV2.importAppSelect(0);
        }

        @Override
        public void onAppFinalSelect(String tag9F06value) throws RemoteException {
            //addEndTime("onAppFinalSelect()");
            LogUtil.e(Constant.TAG, "onAppFinalSelect tag9F06value:" + tag9F06value);
            initEmvTlvData();
            emvOptV2.importAppFinalSelectStatus(0);
        }

        @Override
        public void onConfirmCardNo(String cardNo) throws RemoteException {
            //addEndTime("onConfirmCardNo()");
            LogUtil.e(Constant.TAG, "onConfirmCardNo cardNo:" + cardNo);
            emvOptV2.importCardNoStatus(0);
            runOnUiThread(() -> {
                        String text = "card_NO" + cardNo;
                        //tvCardNo.setText(text);
                    }
            );
        }

        @Override
        public void onRequestShowPinPad(int pinType, int remainTime) throws RemoteException {
            //addEndTime("onRequestShowPinPad()");
            LogUtil.e(Constant.TAG, "onRequestShowPinPad pinType:" + pinType + " remainTime:" + remainTime);
        }

        @Override
        public void onRequestSignature() throws RemoteException {
           // addEndTime("onRequestSignature()");
            LogUtil.e(Constant.TAG, "onRequestSignature");
        }

        @Override
        public void onCertVerify(int certType, String certInfo) throws RemoteException {
           // addEndTime("onCertVerify()");
            LogUtil.e(Constant.TAG, "onCertVerify certType:" + certType + " certInfo:" + certInfo);
        }

        @Override
        public void onOnlineProc() throws RemoteException {
            //addEndTime("onOnlineProc()");
            LogUtil.e(Constant.TAG, "onOnlineProcess");
        }

        @Override
        public void onCardDataExchangeComplete() throws RemoteException {
            //addEndTime("onCardDataExchangeComplete()");
            LogUtil.e(Constant.TAG, "onCardDataExchangeComplete");
        }

        @Override
        public void onTransResult(int code, String desc) throws RemoteException {
            //addEndTime("onTransResult()");
            //dismissLoadingDialog();
            LogUtil.e(Constant.TAG, "onTransResult code:" + code + " desc:" + desc);
            LogUtil.e(Constant.TAG, "***************************************************************");
            LogUtil.e(Constant.TAG, "****************************End Process************************");
            LogUtil.e(Constant.TAG, "***************************************************************");
            String mCardNo = getCardNo();
            tvCardNo.setText(mCardNo);
            //showSpendTime();
            //getExpireDateAndCardholderName();
        }

        @Override
        public void onConfirmationCodeVerified() throws RemoteException {
            //addEndTime("onConfirmationCodeVerified()");
            //dismissLoadingDialog();
            LogUtil.e(Constant.TAG, "onConfirmationCodeVerified");
            //showSpendTime();
        }

        @Override
        public void onRequestDataExchange(String cardNo) throws RemoteException {
            //addEndTime("onRequestDataExchange()");
            LogUtil.e(Constant.TAG, "onRequestDataExchange,cardNo:" + cardNo);
            emvOptV2.importDataExchangeStatus(0);
        }

        @Override
        public void onTermRiskManagement() throws RemoteException {
            //addEndTime("onTermRiskManagement()");
            LogUtil.e(Constant.TAG, "onTermRiskManagement");
            emvOptV2.importTermRiskManagementStatus(0);
        }

        @Override
        public void onPreFirstGenAC() throws RemoteException {
            //addEndTime("onPreFirstGenAC()");
            LogUtil.e(Constant.TAG, "onPreFirstGenAC");
            emvOptV2.importPreFirstGenACStatus(0);
        }

        @Override
        public void onDataStorageProc(String[] containerID, String[] containerContent) throws RemoteException {
            //addEndTime("onDataStorageProc()");
            LogUtil.e(Constant.TAG, "onDataStorageProc");
            //此回调为Dpas2.0专用
            //根据需求配置tag及values
            String[] tags = new String[0];
            String[] values = new String[0];
            emvOptV2.importDataStorage(tags, values);
        }
    };

    private void initEmvTlvData() {
        try {
            // set normal tlv data
            String[] tags = {
                    "5F2A", "5F36"
            };
            String[] values = {
                    "0356", "02"
            };
            emvOptV2.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tags, values);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    */
/** getCard number *//*

    private String getCardNo() {
        LogUtil.e(Constant.TAG, "getCardNo");
        try {
            String[] tagList = {"57", "5A"};
            byte[] outData = new byte[256];
//            addStartTime("getCardNo()");
            int len = emvOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tagList, outData);
//            addEndTime("getCardNo()");
            if (len <= 0) {
                LogUtil.e(Constant.TAG, "getCardNo error,code:" + len);
                return "";
            }
            byte[] bytes = Arrays.copyOf(outData, len);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(bytes);
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("57")).getValue())) {
                TLV tlv57 = tlvMap.get("57");
                CardInfo cardInfo = parseTrack2(tlv57.getValue());
                String displayMessage = "CardNo: " + cardInfo.cardNo + " \nExpireDate: " + cardInfo.expireDate + " \nService Code: " + cardInfo.serviceCode;

//                return cardInfo.cardNo;
                return displayMessage;
            }
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("5A")).getValue())) {
                return Objects.requireNonNull(tlvMap.get("5A")).getValue();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    */
/**
     * Parse track2 data
     *//*

    public static CardInfo parseTrack2(String track2) {
        LogUtil.e(Constant.TAG, "track2:" + track2);
        String track_2 = stringFilter(track2);
        int index = track_2.indexOf("=");
        if (index == -1) {
            index = track_2.indexOf("D");
        }
        CardInfo cardInfo = new CardInfo();
        if (index == -1) {
            return cardInfo;
        }
        String cardNumber = "";
        if (track_2.length() > index) {
            cardNumber = track_2.substring(0, index);
        }
        String expiryDate = "";
        if (track_2.length() > index + 5) {
            expiryDate = track_2.substring(index + 1, index + 5);
        }
        String serviceCode = "";
        if (track_2.length() > index + 8) {
            serviceCode = track_2.substring(index + 5, index + 8);
        }
        LogUtil.e(Constant.TAG, "cardNumber:########:" + cardNumber + " expireDate:" + expiryDate + " serviceCode:" + serviceCode);

        cardInfo.cardNo = cardNumber;
        cardInfo.expireDate = expiryDate;
        cardInfo.serviceCode = serviceCode;
        return cardInfo;
    }
    */
/**
     * remove characters not number,=,D
     *//*

    static String stringFilter(String str) {
        String regEx = "[^0-9=D]";
        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(str);
        return matcher.replaceAll("").trim();
    }
*/



    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


}