package com.bratner.bankproto;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class DetectFace extends Activity implements OnClickListener{
    private int cameraId = -1;    
    private CameraPreview cp;
    private SurfaceView sv;
    private Button bCancel;
    private Button bApprove;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detect_face);
		// Show the Up button in the action bar.
		//setupActionBar();
		bCancel = (Button) findViewById(R.id.bCancel);
		bApprove = (Button) findViewById(R.id.bApprove);
		bCancel.setOnClickListener(this);
		bApprove.setOnClickListener(this);
		//prevent from sleeping
		
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON ,
	            
	             
	            WindowManager.LayoutParams.FLAG_FULLSCREEN |
	            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | 	            
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		//Find the camera id for the first front facing camera
		Log.d("Brat", Build.HARDWARE);
		int totalCams = Camera.getNumberOfCameras();		 
		for(int i=0; i<totalCams;i++){
			CameraInfo c = new CameraInfo();
			Camera.getCameraInfo(i, c);		
			if (c.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ){
				cameraId = i;
				break;
			}
		}
		if ( cameraId < 0) {
		   	 int duration = Toast.LENGTH_SHORT;     	
	    	 Toast toast = Toast.makeText(getApplicationContext(), "Can't find front facing cam.", duration);
	     	toast.show();
	    	this.finish();	
		}		
		FrameLayout fl = (FrameLayout) findViewById(R.id.flay);
		cp = new CameraPreview(this, cameraId);
		fl.addView(cp);
		sv = new SurfaceView(this);
		sv.setClickable(true);
		//sv.setOnClickListener(this);
		fl.addView(sv);
		
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.detect_face, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();		
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

	}

	@Override
	public void onClick(View v) {
		
		if( bCancel == (Button)v ){
			cp.stop();
			this.finish();
		}
		
	}
	
	

}
