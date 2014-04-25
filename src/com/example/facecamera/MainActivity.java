package com.example.facecamera;
/**
 * first commit
 * user jugg
 */

import java.io.IOException;
import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.app.Activity;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity implements SurfaceHolder.Callback{
	private SurfaceView mSurFaceView;
	private Camera camera;
	private SurfaceHolder holder;
	private Camera.PreviewCallback previewCallback;
	
	private double scale = Double.MAX_VALUE;
	private double scaleScreen = Double.MAX_VALUE;
	
	private int screenW,screenH;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}
	void init(){
		Display display = getWindowManager().getDefaultDisplay();
		screenW = display.getWidth();
		screenH = display.getHeight();
		mSurFaceView = (SurfaceView) findViewById(R.id.face_camera);
		holder = mSurFaceView.getHolder();
		previewCallback = new mPreviewCallback();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(this); 
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
	
	class mPreviewCallback implements Camera.PreviewCallback{

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
