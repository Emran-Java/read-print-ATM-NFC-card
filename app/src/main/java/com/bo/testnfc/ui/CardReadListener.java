package com.bo.testnfc.ui;

import com.sunmi.pay.hardware.aidl.bean.CardInfo;

public interface CardReadListener {
    public void onReadResponse(CardInfo cardInfo);
    public void onReadError(String errorMessage);
}
