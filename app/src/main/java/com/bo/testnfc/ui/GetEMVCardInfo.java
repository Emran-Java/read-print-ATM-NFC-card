package com.bo.testnfc.ui;

import android.app.Activity;
import android.os.RemoteException;
import android.text.TextUtils;

import com.bo.testnfc.app_data.Constant;
import com.bo.testnfc.utility.LogUtil;
import com.bo.testnfc.utility.nfc_ic.TLV;
import com.bo.testnfc.utility.nfc_ic.TLVUtil;
import com.sunmi.pay.hardware.aidl.bean.CardInfo;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetEMVCardInfo {

    ListenEMVCardInfo mListenEMVCardInfo;
    private EMVOptV2 emvOptV2;
    private Activity mActivity;

    private static GetEMVCardInfo singleInstance = null;
    private GetEMVCardInfo(Activity activity, EMVOptV2 eMvOptV2){
        mActivity = activity; emvOptV2 = eMvOptV2;
    }
    public static synchronized GetEMVCardInfo getInstance(Activity activity, EMVOptV2 eMvOptV2)
    {
        if (singleInstance == null){
            singleInstance = new GetEMVCardInfo(activity,eMvOptV2);

        }

        return singleInstance;
    }


    public void getCardInfoListener(ListenEMVCardInfo listenEMVCardInfo){
        mListenEMVCardInfo = listenEMVCardInfo;
    }

    public EMVListenerV2 getEMVListener (){
        return mEMVListener;
    }

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
            mActivity.runOnUiThread(() -> {
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
            CardInfo cardInfo = getCardNo();
            mListenEMVCardInfo.onListenCardInfo(cardInfo);
            //tvCardNo.setText(mCardNo);
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

    /** getCard number */
    private CardInfo getCardNo() {
        LogUtil.e(Constant.TAG, "getCardNo");
        CardInfo cardInfo = new CardInfo();
        try {
            String[] tagList = {"57", "5A"};
            byte[] outData = new byte[256];
//            addStartTime("getCardNo()");
            int len = emvOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tagList, outData);
//            addEndTime("getCardNo()");
            if (len <= 0) {
                LogUtil.e(Constant.TAG, "getCardNo error,code:" + len);
                return cardInfo;
            }
            byte[] bytes = Arrays.copyOf(outData, len);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(bytes);
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("57")).getValue())) {
                TLV tlv57 = tlvMap.get("57");
                cardInfo = parseTrack2(tlv57.getValue());
                // String displayMessage = "CardNo *_*_*_: " + cardInfo.cardNo + " \nExpireDate: " + cardInfo.expireDate + " \nService Code: " + cardInfo.serviceCode;

//                return cardInfo.cardNo;
                return cardInfo;
            }
            else if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("5A")).getValue())) {
                //return Objects.requireNonNull(tlvMap.get("5A")).getValue();
                cardInfo.cardNo = Objects.requireNonNull(tlvMap.get("5A")).getValue();
                return cardInfo;
            }
            else{
                mListenEMVCardInfo.onListenCardInfoError("TextUtils.isEmpty");
            }
        } catch (RemoteException e) {
            mListenEMVCardInfo.onListenCardInfoError(e.getMessage());
            e.printStackTrace();
        }
//        return "";
        return cardInfo;
    }

    /**
     * Parse track2 data
     */
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
        LogUtil.e(Constant.TAG, "cardNumber:*_*:" + cardNumber + " expireDate:" + expiryDate + " serviceCode:" + serviceCode);

        cardInfo.cardNo = cardNumber;
        cardInfo.expireDate = expiryDate;
        cardInfo.serviceCode = serviceCode;
        return cardInfo;
    }
    /**
     * remove characters not number,=,D
     */
    static String stringFilter(String str) {
        String regEx = "[^0-9=D]";
        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(str);
        return matcher.replaceAll("").trim();
    }

}
