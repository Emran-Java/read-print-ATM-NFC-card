package com.bo.testnfc.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;

import com.bo.testnfc.app_data.Constant;
import com.bo.testnfc.system.MyApplication;
import com.bo.testnfc.utility.ByteUtil;
import com.bo.testnfc.utility.DeviceUtil;
import com.bo.testnfc.utility.LogUtil;
import com.bo.testnfc.utility.Utility;
import com.bo.testnfc.utility.emv.EmvUtil;
import com.bo.testnfc.utility.nfc_ic.ThreadPoolUtil;
import com.bo.testnfc.wrapper.CheckCardCallbackV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.bean.CardInfo;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVTransDataV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Map;

public class CardRead {

    private CardReadListener mCardReadListener;
    private static CardRead mCardRead;
    private Activity mActivity;

    private GetEMVCardInfo mGetEMVCardInfo;
    private EMVOptV2 emvOptV2 = MyApplication.app.emvOptV2;
    private int mCarType;
    private static final int TDK_INDEX = 19;
    private CardInfo mCardInfo = new CardInfo();


    private CardRead(Activity activity){
        mActivity = activity;
        connectToSDK();
    }

    public synchronized static CardRead getInstance(Activity activity){
        if(mCardRead==null){
            mCardRead = new CardRead(activity);
        }
        return mCardRead;
    }

    private void connectToSDK() {
        if (!MyApplication.app.isConnectPaySDK()) {
            MyApplication.app.bindPaySDKService();
            // showToast("connecting to sdk");
            //return;
        } else {
            //showToast("SDK connected");
        }
    }

    public void readToReadCard(){

        /*ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            checkForMagnetic();
        }, 1, TimeUnit.SECONDS);*/

        checkForMagnetic();

    }

    private void
    checkForMagnetic() {
        ThreadPoolUtil.executeInCachePool(
                () -> {
                    EmvUtil.initKey();
                    EmvUtil.initAidAndRid();
                    Map<String, String> map = EmvUtil.getConfig(EmvUtil.COUNTRY_INDIA);
                    EmvUtil.setTerminalParam(map);
                }
        );

        saveTDKMagEnc();
        checkCardMagEnc();
    }


    private void checkCard() {
        try {
            if(emvOptV2==null)
                emvOptV2 = MyApplication.app.emvOptV2;

            int cardType = AidlConstantsV2.CardType.NFC.getValue() | AidlConstantsV2.CardType.IC.getValue();

            MyApplication.app.readCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*Magnetic*/
    /** Save a TDK(track data key) for test */
    private void saveTDKMagEnc() {
        try {
            byte[] tdk = ByteUtil.hexStr2Bytes("F2914D44BC2AF05533DD20C9A0B5B861");
            byte[] tdkcv = ByteUtil.hexStr2Bytes("36821ADF5EB5513F");
            //addStartTimeWithClear("savePlaintextKey()");
            int code = MyApplication.app.securityOptV2.savePlaintextKey(AidlConstants.Security.KEY_TYPE_TDK
                    , tdk, tdkcv, AidlConstants.Security.KEY_ALG_TYPE_3DES, TDK_INDEX);
            //addEndTime("savePlaintextKey()");
            LogUtil.e(Constant.TAG, "save TDK " + (code == 0 ? "success" : "failed"));
            //showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * start check card
     */
    private void checkCardMagEnc() {
/*
        Bundle bundle = new Bundle();
        bundle.putInt("cardType", AidlConstants.CardType.MAGNETIC.getValue());
        bundle.putInt("encKeySystem", AidlConstants.Security.SEC_MKSK);
        bundle.putInt("encKeyIndex", TDK_INDEX);
        bundle.putInt("encKeyAlgType", AidlConstants.Security.KEY_ALG_TYPE_3DES);
        bundle.putInt("encMode", AidlConstants.Security.DATA_MODE_ECB);
        bundle.putByteArray("encIv", new byte[16]);
        bundle.putByte("encPaddingMode", (byte) 0);
        bundle.putInt("encMaskStart", 6);
        bundle.putInt("encMaskEnd", 4);
        bundle.putChar("encMaskWord", '*');
        bundle.putInt("ctrCode", 0);
        bundle.putInt("stopOnError", 0);
*/

        try {
            if(emvOptV2==null)
                emvOptV2 = MyApplication.app.emvOptV2;
            emvOptV2.initEmvProcess();
//            addStartTimeWithClear("checkCard()");
//            int code = MyApplication.app.readCardOptV2.checkCardEnc(bundle, mCheckCardCallback, 60);
//            int cardType = AidlConstantsV2.CardType.MAGNETIC.getValue();

            int cardType = AidlConstants.CardType.MAGNETIC.getValue() | AidlConstants.CardType.IC.getValue() | AidlConstants.CardType.NFC.getValue();
            if (DeviceUtil.isTossTerminal()) {
                cardType &= ~AidlConstants.CardType.NFC.getValue();
            }
            mCarType = cardType;

            MyApplication.app.readCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
            //LogUtil.e(Constant.TAG, "checkCardEnc(), code:" + code+"cardType: "+AidlConstants.CardType.MAGNETIC.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(Constant.TAG, e.getMessage());
        }
    }


    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {

            LogUtil.e(Constant.TAG, "findMagCard");

            String track1 = Utility.null2String(bundle.getString("TRACK1"));
            String track2 = Utility.null2String(bundle.getString("TRACK2"));
            String track3 = Utility.null2String(bundle.getString("TRACK3"));
            mActivity.runOnUiThread(() -> {
                String value = "track1:" + track1 + "\ntrack2:" + track2 + "\ntrack3:" + track3;

                LogUtil.e(Constant.TAG, "value: "+value);

                try{
                    CardInfo cardInfo = GetEMVCardInfo.parseTrack2(track2);
                    LogUtil.e(Constant.TAG, "findMagCard_cardInfo###:"+cardInfo.toString());

                    replayResult(cardInfo.cardNo,cardInfo.expireDate);

//                    transactProcess();
                }catch (Exception ex){showError(ex.getMessage());}
            });
            mCardInfo.track1 = track1;
            mCardInfo.track2 = track2;
            mCardInfo.track3 = track3;


        }

        @Override
        public void findICCard(String atr) throws RemoteException {
//            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
//            showSpendTime();
            mCarType = AidlConstantsV2.CardType.IC.getValue();
            mActivity.runOnUiThread(
                    () -> {
                        String text = "atr: " + atr;
                        mCardInfo.atr = atr;
                        LogUtil.e("dMoneyLog", "card_atr:" + atr);
                        //replayResult(atr,null);
                    }
            );
            transactProcess();
        }

        /**
         * Find IC card
         *
         * @paraminfo return data，contain the following keys:
         *             <br/>cardType: card type (int)
         *             <br/>atr: card's ATR (String)
         */
        /*@Override
        public void findICCardEx(Bundle info) throws RemoteException {
//            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard_*_*:" + Utility.bundle2String(info));
//            tvCardNo.setText(Utility.bundle2String(info));
//            handleResult(true, info);
//            showSpendTime();
        }*/


        @Override
        public void findRFCard(String uuid) throws RemoteException {
            //addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            //showSpendTime();
            mCarType = AidlConstantsV2.CardType.NFC.getValue();
            mActivity.runOnUiThread(
                    () -> {
//                        String text = getString(R.string.card_uuid) + uuid;
//                        tvUUID.setText(text);
                        mCardInfo.uuid = uuid;
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
            showError(error);
//            dismissLoadingDialog();
//            showSpendTime();
        }

        @Override
        public void onErrorEx(Bundle bundle) throws RemoteException {
            //addEndTime("checkCard()");
            int code = bundle.getInt("code");
            String msg = bundle.getString("message");
            String error = "onError:" + msg + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showError(error);
            //handleResult(null);
            //showSpendTime();
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

            mGetEMVCardInfo = GetEMVCardInfo.getInstance(mActivity, emvOptV2);
            emvOptV2.transactProcess(emvTransData, mGetEMVCardInfo.getEMVListener());

            mGetEMVCardInfo.getCardInfoListener(new ListenEMVCardInfo() {
                @Override
                public void onListenCardInfo(CardInfo cardInfo) {
                    String displayMessage = "CardNo: " + cardInfo.cardNo + " \nExpireDate: " + cardInfo.expireDate + " \nServiceCode: " + cardInfo.serviceCode;
//                    tvCardNo.setText(displayMessage);
                    replayResult(cardInfo.cardNo,cardInfo.expireDate);
                    LogUtil.e(Constant.TAG, "cardNumber::" + cardInfo.cardNo + " expireDate:" + cardInfo.expireDate + " serviceCode:" + cardInfo.serviceCode);
                }

                @Override
                public void onListenCardInfoError(String errorMessage) {
                    showError(errorMessage);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void replayResult(String cardNo, String expDt) {
        if(cardNo==null)
            cardNo="";
        if(expDt==null)
            expDt="";
        mCardInfo.cardNo = cardNo;
        mCardInfo.expireDate = expDt;
        mCardInfo.cardType = mCarType;

        mCardReadListener.onReadResponse(mCardInfo);
        //tvCardNo.setText("cardNo: "+cardNo+"\nExpire Date: "+expDt);
    }

    private void showError(String errorMessage) {
        mCardReadListener.onReadError(errorMessage);
    }


    public void setOnCardReadListener(CardReadListener cardReadListener){
        mCardReadListener = cardReadListener;
    }


}
