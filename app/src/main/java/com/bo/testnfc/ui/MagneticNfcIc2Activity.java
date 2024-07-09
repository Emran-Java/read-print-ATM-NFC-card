package com.bo.testnfc.ui;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bo.testnfc.R;
import com.bo.testnfc.system.MyApplication;
import com.sunmi.pay.hardware.aidl.bean.CardInfo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MagneticNfcIc2Activity extends AppCompatActivity {

    private Button btnNFC,btnMag;

    private TextView tvCardNo;
    private CardRead mCardRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

        initFunctionality();

        initListener();

    }

    private void initFunctionality() {
        mCardRead = CardRead.getInstance(this);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            readCard(mCardRead);
        }, 1, TimeUnit.SECONDS);
    }

    private void initListener() {

        btnNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvCardNo.setText("Ready");
                mCardRead.readToReadCard();
                //checkCard();
            }
        });

        btnMag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvCardNo.setText("Ready");
                mCardRead.readToReadCard();
                //checkForMagnetic();
            }
        });
    }

    private void initView() {
        setContentView(R.layout.activity_mag_nfc_ic);
        tvCardNo = findViewById(R.id.tvCardNo);
        btnNFC = findViewById(R.id.btnNFC);
        btnMag = findViewById(R.id.btnMag);
    }

    private void readCard(CardRead cardRead) {
        cardRead.readToReadCard();
        cardRead.setOnCardReadListener(new CardReadListener() {
            @Override
            public void onReadResponse(CardInfo cardInfo) {
                Log.d("readCard", cardInfo.toString());
                showDisplay(cardInfo.cardNo,cardInfo.expireDate);

            }

            @Override
            public void onReadError(String errorMessage) {
                Log.d("readCard", errorMessage);
                showError(errorMessage);

            }
        });
    }

    private void showDisplay(String cardNo, String expDt) {
        if(cardNo==null)
            cardNo="";
        if(expDt==null)
            expDt="";
        tvCardNo.setText("cardNo: "+cardNo+"\nExpire Date: "+expDt);
    }

    private void showError(String msg) {
        tvCardNo.setText(msg);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}