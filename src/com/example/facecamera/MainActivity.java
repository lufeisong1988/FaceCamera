package com.example.facecamera;
/**
 * first commit
 * user jugg
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;




import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends Activity implements SurfaceHolder.Callback,OnClickListener, PictureCallback{
	private SurfaceView mSurFaceView;
	private Camera camera;
	private SurfaceHolder holder;
	private Camera.PreviewCallback previewCallback;
	private FaceView faceview;
	private FrameLayout layout;
	private Button takePhoto_bnt;
	
	private double scale = Double.MAX_VALUE;
	private double scaleScreen = Double.MAX_VALUE;
	
	private int screenW,screenH;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		listener();
	}
	@SuppressLint("NewApi")
	void init(){
		Display display = getWindowManager().getDefaultDisplay();
		screenW = display.getWidth();
		screenH = display.getHeight();
		takePhoto_bnt = (Button) findViewById(R.id.takephoto);
		layout = (FrameLayout) findViewById(R.id.camera);
		try {
			faceview = new FaceView(this);
			getPreviewCallBack(faceview);
			mSurFaceView = (SurfaceView) findViewById(R.id.face_camera);
			layout.addView(faceview);
			holder = mSurFaceView.getHolder();
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			holder.addCallback(this); 
		} catch (IOException e) {
			e.printStackTrace();
			new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
		}
		
	}
	void listener(){
		takePhoto_bnt.setOnClickListener(this);
		mSurFaceView.setOnClickListener(this);
	}
	void getPreviewCallBack(Camera.PreviewCallback previewCallback){
    	this.previewCallback = previewCallback;
    }
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if(camera == null){
			camera = Camera.open();
		}
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
			camera.release();
			camera = null;
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		double value = 1.0f;
		double preScale = 1.0f;
		double picValue = 1.0f;
		double width = 0, height = 0;
		double picWidth = 0, picHeight = 0;
		scaleScreen = (double) screenW / screenH;
		Camera.Parameters params = camera.getParameters();
		List<Size> size = params.getSupportedPreviewSizes();
		List<Size> picSize = params.getSupportedPictureSizes();
		// 获取一个适配屏幕大小的分辨率
		for (int i = 0; i < size.size(); i++) {
			Size fitsize = size.get(i);
			scale = (double) fitsize.width / fitsize.height;
			double currentValue = Math.abs(scale - scaleScreen);
			if (currentValue < value) {
				width = fitsize.width;
				height = fitsize.height;
				value = currentValue;
				preScale = scale;
			}
		}
		// 寻找最匹配的照片像素比列
		for (int j = 0; j < picSize.size(); j++) {
			Size fitPicSize = picSize.get(j);
			double picScale = (double) fitPicSize.width / fitPicSize.height;
			double currentPicValue = Math.abs(preScale - picScale);
			if (currentPicValue < picValue || currentPicValue == picValue) {
				picValue = currentPicValue;
				picWidth = fitPicSize.width;
				picHeight = fitPicSize.height;
			}
		}
		params.setPictureSize((int) picWidth, (int) picHeight);
		params.setPreviewSize((int) width, (int) height);
		camera.setParameters(params);
		if (previewCallback != null) {
			camera.setPreviewCallbackWithBuffer(previewCallback);
			Camera.Size length = params.getPreviewSize();
			byte[] data = new byte[length.width * length.height * ImageFormat .getBitsPerPixel(params.getPreviewFormat()) / 8];
			camera.addCallbackBuffer(data);
		}
		camera.startPreview();
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if(camera != null){
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
		
	}
	AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {//自动聚焦
		
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			
		}
	};

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.face_camera:
			camera.autoFocus(mAutoFocusCallback);
			break;
		case R.id.takephoto:
			bTake = true;
			camera.takePicture(null, null, null,MainActivity.this);
			break;
		}
	}
	

	boolean bTake = false;
	@Override
	public void onPictureTaken(byte[] arg0, Camera arg1) {
//		File saveFile = new File(Environment.getExternalStorageDirectory().getPath()  + "/face_camera");
//		if(!saveFile.exists())
//			saveFile.mkdirs();
//		File photoFile = new File(saveFile + "/face_photo.jpg");
//		Bitmap screenBit = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
//		try {
//			photoFile.createNewFile();
//			FileOutputStream fos = new FileOutputStream(photoFile);
//			screenBit.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//			fos.flush();
//			fos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		if(bTake){
			faceview.takePhoto(MainActivity.this,arg0,screenW,screenH);
			bTake = false;
		}
		
	}

}
