package com.bratner.bankproto;

import android.os.Bundle;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BankMain extends Activity {
    private TextView tDevId;
    private TextView tPhoneNum;
    private TextView tProvider;
    private TextView tSIMserial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_bank_main);
        
        tDevId = (TextView) findViewById(R.id.tDevId);
        tPhoneNum = (TextView) findViewById(R.id.tPhoneNum);
        tProvider = (TextView) findViewById(R.id.tProvider);
        tSIMserial = (TextView) findViewById(R.id.tSIMserial);
    
        
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);                
        //android.telephony.TelephonyManager.getDeviceId();
        String deviceId = tm.getDeviceId();
        String number = tm.getLine1Number();
        String simserial = tm.getSimSerialNumber();      
        //String phoneType = tm.getPhoneType();
        String provider = tm.getNetworkOperatorName()+'['+tm.getNetworkOperator()+']';
        tDevId.setText(deviceId);
        tPhoneNum.setText(number);
        tProvider.setText(provider);
        tSIMserial.setText(simserial);
       
    }


    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		
	}


	/* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bank_main, menu);
        return true;
    } */
    public void startDetectFace(View v){
    	Intent intent = new Intent(this, DetectFace.class);
    	/* int duration = Toast.LENGTH_SHORT;     	
    	Toast toast = Toast.makeText(getApplicationContext(), "Run detect face activity!", duration);
    	toast.show(); 
    	*/
    	startActivity(intent);
    }
}
