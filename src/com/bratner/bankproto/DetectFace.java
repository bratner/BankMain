package com.bratner.bankproto;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class DetectFace extends Activity implements OnClickListener, ShutterCallback, PictureCallback{
    private int cameraId = -1;    
    private CameraPreview cp;
    private SurfaceView sv;
    private Button bCancel;
    private Button bApprove;
    private String reqId;
    private String ammount;
    public byte[] image_data = null;
    
    private class DoPostRequestAsync extends AsyncTask<URL, Void, String> {
        private String boundary;
        private static final String LINE_FEED = "\r\n";
        
        protected void onPostExecute(String result) {
            //This gets called on the interface (main) thread!
        	Log.d("Brat", "Executed the post request with result "+result);
           
        }

		@Override
		protected String doInBackground(URL... params) {
			
			boundary = "==="+System.currentTimeMillis()+"===";
			Log.d("Brat", "Request for URL "+params[0]);
			//HttpURLConnection urlcon = params[0]
			try {
				HttpURLConnection urlcon = (HttpURLConnection) params[0].openConnection();
				urlcon.setDoOutput(true);
				urlcon.setDoInput(true);
				urlcon.setUseCaches(false);
				urlcon.setRequestMethod("POST");
				urlcon.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
				urlcon.setRequestProperty("User-Agent","Bratners Agent");
				
				OutputStream out = urlcon.getOutputStream();
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(out),true);
				writer.append("--").append(boundary).append(LINE_FEED);	
				String fileName = reqId+".jpg";
				writer.append(
		                "Content-Disposition: form-data; name=\"selfshot\"; filename=\"" + fileName + "\"")
		                .append(LINE_FEED);
		        writer.append(
		                "Content-Type: "
		                        + URLConnection.guessContentTypeFromName(fileName))
		                .append(LINE_FEED);
		        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		        writer.append(LINE_FEED);
		        writer.flush();
		        out.write(image_data);
		        out.flush();
		        writer.append(LINE_FEED);
		        writer.flush();     
		        writer.append(LINE_FEED).flush();
		        writer.append("--" + boundary + "--").append(LINE_FEED);
		        writer.close();
		        int status = urlcon.getResponseCode();
		 		return Integer.toString(status);			
			} catch (IOException e) {
				Log.d("Brat","Error with http request " + e.toString());
				return "Shit man";
			}
		}
    }
    
    @Override
	public void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();	
		Log.d("Brat","onAttachedToWindow");
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON ,
	            
	             
	            WindowManager.LayoutParams.FLAG_FULLSCREEN |
	            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | 	            
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		    //make the activity show even the screen is locked.
		  //  Window window = getWindow();

		    /*window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		    window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		    window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD); */

		 /*   window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
		            + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		            + WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		            + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD); */		
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
				
		Log.d("Brat", "onCreate");
		setContentView(R.layout.activity_detect_face);
		// Show the Up button in the action bar.
		//setupActionBar();
		bCancel = (Button) findViewById(R.id.bCancel);
		bApprove = (Button) findViewById(R.id.bApprove);
		bCancel.setOnClickListener(this);
		bApprove.setOnClickListener(this);
		
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON ,
	            
	             
	            WindowManager.LayoutParams.FLAG_FULLSCREEN |
	            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | 	            
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				
		Intent myIntent = getIntent();
		Bundle myBundle = null;
		if (myIntent != null)
			myBundle = myIntent.getExtras();		
		if( myBundle!=null ){
			reqId = myBundle.getString("req_id");
			ammount = myBundle.getString("ammount");
		}
		reqId = reqId == null?"nada":reqId;			
		ammount = ammount == null?"nada":ammount;
		
		
		 
		
		//Find the camera id for the first front facing camera
		Log.d("Brat", "Hardware:"+ Build.HARDWARE);
		int totalCams = Camera.getNumberOfCameras();
	//	Toast.makeText(this, "got "+totalCams+" cameras", Toast.LENGTH_SHORT).show();
		for(int i=0; i<totalCams;i++){
			CameraInfo c = new CameraInfo();
			Camera.getCameraInfo(i, c);	
			if (c.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ){
				cameraId = i;
				break;
			}		
		}		
		// Toast.makeText(this, "choose cam"+cameraId, Toast.LENGTH_SHORT).show();
		if ( cameraId < 0) {
		   	 int duration = Toast.LENGTH_SHORT;     	
	    	 Toast toast = Toast.makeText(getApplicationContext(), "Can't find front facing cam.", duration);
	     	toast.show();
	    	this.finish();	
		}		
		FrameLayout fl = (FrameLayout) findViewById(R.id.flay);
		cp = new CameraPreview(this, cameraId);
		fl.addView(cp);	
		//Toast.makeText(this,"Request "+reqId+" for "+ammount, Toast.LENGTH_SHORT).show();
		
		
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("Brat", "onStart");
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("Brat", "onPause");		
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d("Brat", "onResume");
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("Brat", "onStop");		

	}

	@Override
	public void onClick(View v) {
		
		if( bCancel == (Button)v ){
						
			cp.stop();
			this.finish();
					
		}
		if (bApprove == (Button) v){
			// Take a photo 
			cp.takePhoto(this,this);			
			//this.onStop();
			// resize-convert-send
		}
		
	}


	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d("Brat", "onPictureTaken");
		if (camera == null) {
			Log.d("Brat", "Picture was taken and camera was null. Got "+data.length+" bytes of data.");
			return;
		}
			
		Camera.Parameters camParams = camera.getParameters();
		if(camParams == null) {
			Log.d("Brat", "Unable to get camera params. Got "+data.length+" bytes of data.");
			return;
		}
		if(data == null){
			Log.d("Brat", "data is a null. Probably raw image missing");
			return;
		}
		Size picSize = camParams.getPictureSize();
		int picFormat = camParams.getPictureFormat();
		image_data = data;
		DoPostRequestAsync async = new DoPostRequestAsync();
		
		try {
			async.execute(new URL("http://inna.co.il/smsauthjpeg"));
		} catch (MalformedURLException e) {
			Log.d("Brat","Error creating url."+e.toString());
		}						
		cp.stop();
		this.finish();
		
	}


	@Override
	public void onShutter() {
		Log.d("Brat", "onShutter()");
	}
	
	

}
	