package com.bratner.bankproto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


public class MSReceiver extends BroadcastReceiver {
	public MSReceiver() {
		Log.d("BRAT","Starting BankProto SMS receiver");
	}

	@Override
	public void onReceive(Context context, Intent intent) {		
	     if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
	            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
	            SmsMessage[] msgs = null;
	            String msg_from="";
	            String msgBody="";
	            if (bundle != null){
	                //---retrieve the SMS message received---
	                try{
	                    Object[] pdus = (Object[]) bundle.get("pdus");
	                    msgs = new SmsMessage[pdus.length];
	                    for(int i=0; i<msgs.length; i++){
	                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
	                        msg_from = msgs[i].getOriginatingAddress();
	                        msgBody = msgs[i].getMessageBody();
	                    }
	                }catch(Exception e){
//	                            Log.d("Exception caught",e.getMessage());
	                }
	                
	                if(msgBody.matches("^bank:\\d+:\\w+$")){
	                	String parts[] = msgBody.split(":");
	                	String howmuch = parts[1];
	                	String req_id = parts[2];
	                	/* Toast toast = Toast.makeText(context, "Снимают "+howmuch+" рублей!", duration);
	                	toast.show(); */
	                	Intent detfaceIntent = new Intent();
	                	detfaceIntent.setClassName("com.bratner.bankproto", "com.bratner.bankproto.DetectFace");
	                	detfaceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                	detfaceIntent.putExtra("ammount", howmuch);
	                	detfaceIntent.putExtra("req_id", req_id);
	                	context.startActivity(detfaceIntent);
	                	abortBroadcast();	                		                
	                }
	            }
	        }
	}
}
