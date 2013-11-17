package com.bratner.bankproto;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class CameraPreview extends SurfaceView implements Callback, FaceDetectionListener,PreviewCallback {
  
	  private static boolean DEBUGGING = true;
	    private static final String LOG_TAG = "CameraPreviewSample";
	    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
	    private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
	    private static final String CAMERA_PARAM_PORTRAIT = "portrait";
	    protected Activity mActivity;
	    private SurfaceHolder mHolder;
	    protected Camera mCamera;
	    protected List<Camera.Size> mPreviewSizeList;
	    protected List<Camera.Size> mPictureSizeList;
	    protected Camera.Size mPreviewSize;
	    protected Camera.Size mPictureSize;
	    private int mSurfaceChangedCallDepth = 0;
	    private int mCameraId;
	    private LayoutMode mLayoutMode;
	    private int mCenterPosX = -1;
	    private int mCenterPosY;
	    private int maxFaces;
	    private boolean faceDetect = false;
	    PreviewReadyCallback mPreviewReadyCallback = null;
	    private Rect lastFace;
	    private int lastAngle = 90;
	    private RectF rectf = null;
	    private boolean stopping = false;
	    public static enum LayoutMode {
	        FitToParent, // Scale to the size that no side is larger than the parent
	        NoBlank // Scale to the size that no side is smaller than the parent
	    };
	    
	    public interface PreviewReadyCallback {
	        public void onPreviewReady();
	    }
	 
	    /**
	     * State flag: true when surface's layout size is set and surfaceChanged()
	     * process has not been completed.
	     */
	    protected boolean mSurfaceConfiguring = false;

	    public CameraPreview(Activity activity, int cameraId) {
	        super(activity); // Always necessary
	        mActivity = activity;
	        mLayoutMode = LayoutMode.FitToParent;
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	       // mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
	            if (Camera.getNumberOfCameras() > cameraId) {
	                mCameraId = cameraId;
	            } else {
	                mCameraId = 0;
	            }
	        } else {
	            mCameraId = 0;
	        }

	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
	            mCamera = Camera.open(mCameraId);
	        } else {
	            mCamera = Camera.open();
	        }
	        Camera.Parameters cameraParams = mCamera.getParameters();
	        mPreviewSizeList = cameraParams.getSupportedPreviewSizes();
	        mPictureSizeList = cameraParams.getSupportedPictureSizes();
	        //maxFaces = cameraParams.getMaxNumDetectedFaces();
	        //mCamera.setFaceDetectionListener(this);
	        //mCamera.setPreviewCallback(this);
	        rectf = new RectF(10, 10, 100, 100);
	        //if(maxFaces > 0)
	        //	faceDetect = true;
	    }

	    @Override
	    public void surfaceCreated(SurfaceHolder holder) {
	    	Log.d(LOG_TAG,"Surface created");
	    	if(stopping)
	    		return;
	        try {
	            mCamera.setPreviewDisplay(mHolder);
	        } catch (IOException e) {
	            mCamera.release();
	            mCamera = null;
	        }
	    }
	    
	    @Override
	    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	    	Log.d(LOG_TAG,"Surface changed");
	    	if(stopping)
	    		return;
	        mSurfaceChangedCallDepth++;
	        doSurfaceChanged(width, height);
	        mSurfaceChangedCallDepth--;
	    }

	    private void doSurfaceChanged(int width, int height) {
	    	if(stopping)
	    		return;
	    	if(faceDetect){
	    		if(mCamera != null)
	    		   mCamera.stopFaceDetection();
	    	}
	        mCamera.stopPreview();
	        
	        Camera.Parameters cameraParams = mCamera.getParameters();
	        boolean portrait = isPortrait();

	        // The code in this if-statement is prevented from executed again when surfaceChanged is
	        // called again due to the change of the layout size in this if-statement.
	        if (!mSurfaceConfiguring) {
	            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
	            Camera.Size pictureSize = determinePictureSize(previewSize);
	            if (DEBUGGING) { Log.v(LOG_TAG, "Desired Preview Size - w: " + width + ", h: " + height); }
	            mPreviewSize = previewSize;
	            mPictureSize = pictureSize;
	            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
	            // Continue executing this method if this method is called recursively.
	            // Recursive call of surfaceChanged is very special case, which is a path from
	            // the catch clause at the end of this method.
	            // The later part of this method should be executed as well in the recursive
	            // invocation of this method, because the layout change made in this recursive
	            // call will not trigger another invocation of this method.
	            if (mSurfaceConfiguring && (mSurfaceChangedCallDepth <= 1)) {
	                return;
	            }
	        }

	        configureCameraParameters(cameraParams, portrait);
	        mSurfaceConfiguring = false;

	        try {
	            mCamera.startPreview();
	        } catch (Exception e) {
	            Log.w(LOG_TAG, "Failed to start preview: " + e.getMessage());

	            // Remove failed size
	            mPreviewSizeList.remove(mPreviewSize);
	            mPreviewSize = null;

	            // Reconfigure
	            if (mPreviewSizeList.size() > 0) { // prevent infinite loop
	                surfaceChanged(null, 0, width, height);
	            } else {
	                Toast.makeText(mActivity, "Can't start preview", Toast.LENGTH_LONG).show();
	                Log.w(LOG_TAG, "Gave up starting preview");
	            }
	        }
	        //mCamera.setPreviewCallback(this);
	        if(faceDetect)
	        	mCamera.startFaceDetection();
	        if (null != mPreviewReadyCallback) {
	            mPreviewReadyCallback.onPreviewReady();
	        }
	    }
	    
	    /**
	     * @param cameraParams
	     * @param portrait
	     * @param reqWidth must be the value of the parameter passed in surfaceChanged
	     * @param reqHeight must be the value of the parameter passed in surfaceChanged
	     * @return Camera.Size object that is an element of the list returned from Camera.Parameters.getSupportedPreviewSizes.
	     */
	    protected Camera.Size determinePreviewSize(boolean portrait, int reqWidth, int reqHeight) {
	        // Meaning of width and height is switched for preview when portrait,
	        // while it is the same as user's view for surface and metrics.
	        // That is, width must always be larger than height for setPreviewSize.
	        int reqPreviewWidth; // requested width in terms of camera hardware
	        int reqPreviewHeight; // requested height in terms of camera hardware
	        if (portrait) {
	            reqPreviewWidth = reqHeight;
	            reqPreviewHeight = reqWidth;
	        } else {
	            reqPreviewWidth = reqWidth;
	            reqPreviewHeight = reqHeight;
	        }

	        if (DEBUGGING) {
	            Log.v(LOG_TAG, "Listing all supported preview sizes");
	            for (Camera.Size size : mPreviewSizeList) {
	                Log.v(LOG_TAG, "  w: " + size.width + ", h: " + size.height);
	            }
	            Log.v(LOG_TAG, "Listing all supported picture sizes");
	            for (Camera.Size size : mPictureSizeList) {
	                Log.v(LOG_TAG, "  w: " + size.width + ", h: " + size.height);
	            }
	        }

	        // Adjust surface size with the closest aspect-ratio
	        float reqRatio = ((float) reqPreviewWidth) / reqPreviewHeight;
	        float curRatio, deltaRatio;
	        float deltaRatioMin = Float.MAX_VALUE;
	        Camera.Size retSize = null;
	        for (Camera.Size size : mPreviewSizeList) {
	            curRatio = ((float) size.width) / size.height;
	            deltaRatio = Math.abs(reqRatio - curRatio);
	            if (deltaRatio < deltaRatioMin) {
	                deltaRatioMin = deltaRatio;
	                retSize = size;
	            }
	        }

	        return retSize;
	    }

	    protected Camera.Size determinePictureSize(Camera.Size previewSize) {
	        Camera.Size retSize = null;
	        for (Camera.Size size : mPictureSizeList) {
	            if (size.equals(previewSize)) {
	                return size;
	            }
	        }
	        
	        if (DEBUGGING) { Log.v(LOG_TAG, "Same picture size not found."); }
	        
	        // if the preview size is not supported as a picture size
	        float reqRatio = ((float) previewSize.width) / previewSize.height;
	        float curRatio, deltaRatio;
	        float deltaRatioMin = Float.MAX_VALUE;
	        for (Camera.Size size : mPictureSizeList) {
	            curRatio = ((float) size.width) / size.height;
	            deltaRatio = Math.abs(reqRatio - curRatio);
	            if (deltaRatio < deltaRatioMin) {
	                deltaRatioMin = deltaRatio;
	                retSize = size;
	            }
	        }
	        
	        return retSize;
	    }
	    
	    protected boolean adjustSurfaceLayoutSize(Camera.Size previewSize, boolean portrait,
	            int availableWidth, int availableHeight) {
	        float tmpLayoutHeight, tmpLayoutWidth;
	        if (portrait) {
	            tmpLayoutHeight = previewSize.width;
	            tmpLayoutWidth = previewSize.height;
	        } else {
	            tmpLayoutHeight = previewSize.height;
	            tmpLayoutWidth = previewSize.width;
	        }

	        float factH, factW, fact;
	        factH = availableHeight / tmpLayoutHeight;
	        factW = availableWidth / tmpLayoutWidth;
	        if (mLayoutMode == LayoutMode.FitToParent) {
	            // Select smaller factor, because the surface cannot be set to the size larger than display metrics.
	            if (factH < factW) {
	                fact = factH;
	            } else {
	                fact = factW;
	            }
	        } else {
	            if (factH < factW) {
	                fact = factW;
	            } else {
	                fact = factH;
	            }
	        }

	        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)this.getLayoutParams();

	        int layoutHeight = (int) (tmpLayoutHeight * fact);
	        int layoutWidth = (int) (tmpLayoutWidth * fact);
	        if (DEBUGGING) {
	            Log.v(LOG_TAG, "Preview Layout Size - w: " + layoutWidth + ", h: " + layoutHeight);
	            Log.v(LOG_TAG, "Scale factor: " + fact);
	        }

	        boolean layoutChanged;
	        if ((layoutWidth != this.getWidth()) || (layoutHeight != this.getHeight())) {
	            layoutParams.height = layoutHeight;
	            layoutParams.width = layoutWidth;
	            if (mCenterPosX >= 0) {
	                layoutParams.topMargin = mCenterPosY - (layoutHeight / 2);
	                layoutParams.leftMargin = mCenterPosX - (layoutWidth / 2);
	            }
	            this.setLayoutParams(layoutParams); // this will trigger another surfaceChanged invocation.
	            layoutChanged = true;
	        } else {
	            layoutChanged = false;
	        }

	        return layoutChanged;
	    }

	    /**
	     * @param x X coordinate of center position on the screen. Set to negative value to unset.
	     * @param y Y coordinate of center position on the screen.
	     */
	    public void setCenterPosition(int x, int y) {
	        mCenterPosX = x;
	        mCenterPosY = y;
	    }
	    
	    protected void configureCameraParameters(Camera.Parameters cameraParams, boolean portrait) {
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and before
	            if (portrait) {
	                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
	            } else {
	                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
	            }
	        } else { // for 2.2 and later
	            int angle;
	            Display display = mActivity.getWindowManager().getDefaultDisplay();
	            switch (display.getRotation()) {
	                case Surface.ROTATION_0: // This is display orientation
	                    angle = 90; // This is camera orientation
	                    break;
	                case Surface.ROTATION_90:
	                    angle = 0;
	                    break;
	                case Surface.ROTATION_180:
	                    angle = 270;
	                    break;
	                case Surface.ROTATION_270:
	                    angle = 180;
	                    break;
	                default:
	                    angle = 90;
	                    break;
	            }
	            Log.v(LOG_TAG, "angle: " + angle);
	            lastAngle = angle;
	            mCamera.setDisplayOrientation(angle);
	        }

	        cameraParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
	        cameraParams.setPictureSize(mPictureSize.width, mPictureSize.height);
	        if (DEBUGGING) {
	            Log.v(LOG_TAG, "Preview Actual Size - w: " + mPreviewSize.width + ", h: " + mPreviewSize.height);
	            Log.v(LOG_TAG, "Picture Actual Size - w: " + mPictureSize.width + ", h: " + mPictureSize.height);
	        }

	        mCamera.setParameters(cameraParams);
	    }

	    @Override
	    public void surfaceDestroyed(SurfaceHolder holder) {
	    	Log.d(LOG_TAG,"surfaceDestroyed");
	        stop();
	    }
	    
	    public void stop() {
	    	Log.d(LOG_TAG,"Stop");
	    	if(stopping)
	    		return;
	    	stopping = true;
	        if (null == mCamera) {
	            return;
	        }
	        try{	        
	         mCamera.stopPreview();
	         
	         mCamera.release();
	         mCamera = null;
	        } catch (Exception e){
	           Log.d(LOG_TAG,"cp.stop "+e);	
	        }
	    }

	    public boolean isPortrait() {
	        return (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
	    }
	    
	    public void setOneShotPreviewCallback(PreviewCallback callback) {
	        if (null == mCamera) {
	            return;
	        }
	        mCamera.setOneShotPreviewCallback(callback);
	    }
	    
	    public void setPreviewCallback(PreviewCallback callback) {
	        if (null == mCamera) {
	            return;
	        }
	        mCamera.setPreviewCallback(callback);
	    }
	    
	    public Camera.Size getPreviewSize() {
	        return mPreviewSize;
	    }
	    
	    public void setOnPreviewReady(PreviewReadyCallback cb) {
	        mPreviewReadyCallback = cb;
	    }

		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			    Face f = faces[0];		
			    Rect r = translateFaceCoordinates(f);
				Log.d(LOG_TAG, "Detected a face: "+f.rect);
				Log.d(LOG_TAG, "Translated to: "+r);
		}
	    private Rect translateFaceCoordinates(Face f){
	    	
	    	Rect frame = mHolder.getSurfaceFrame();	    
	    	RectF rectf = new RectF(frame);
	    	Rect ret = new Rect(frame);
	    	Matrix matrix = new Matrix();	    	 
	    	 // Need mirror for front camera.
	    	boolean mirror = true;
	    	matrix.setScale(mirror ? -1 : 1, 1);
	    	 // This is the value for android.hardware.Camera.setDisplayOrientation.	    	
	    	matrix.postRotate(lastAngle);
	    	 // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
	    	 // UI coordinates range from (0, 0) to (width, height).
	    	
	    	matrix.postScale(frame.right / 2000f, frame.bottom / 2000f);
	    	matrix.postTranslate(frame.right / 2f, frame.bottom / 2f);
	    	Log.d(LOG_TAG, "Took a rectf "+rectf);
	    	
            matrix.mapRect(rectf);
            Log.d(LOG_TAG, "Mapped it to "+rectf+"by matrix"+matrix);
            rectf.roundOut(ret);
	    	return ret;
	    }

	    
	    @Override
		public void onAttachedToWindow() {			
			super.onAttachedToWindow();
			Log.d(LOG_TAG,"onAttachedToWindow");
		/*	   mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | 
			           
			            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
			            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
			            WindowManager.LayoutParams.FLAG_FULLSCREEN | 
			            WindowManager.LayoutParams.FLAG_FULLSCREEN |
			            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | 
			            
			            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
			            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
			            */
		}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Log.d(LOG_TAG,"onPreviewFrame");
	//	Canvas c = mHolder.lockCanvas();
	//	Paint p = new Paint();
	//	p.setColor(Color.RED);
	//	c.drawRect(rectf, p);
	//	Log.d(LOG_TAG,"onPreviewFrame Got "+data.length+" bytes of data.");		
	//	mHolder.unlockCanvasAndPost(c);
	}

}
