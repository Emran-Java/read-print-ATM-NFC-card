package com.bo.testnfc.ui;

import com.sunmi.pay.hardware.aidl.bean.CardInfo;

public interface ListenCardInfo {

    public void onListenCardInfo(CardInfo cardInfo);
    public void onListenCardInfoError(String errorMessage);
}
