package com.example.facecamera;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class FaceView extends View implements Camera.PreviewCallback {
	public static final int SUBSAMPLING_FACTOR = 4;

    private IplImage grayImage;
    private CvHaarClassifierCascade classifier;
    private CvMemStorage storage;
    private CvSeq faces;
    private byte[] data;

    public FaceView(Context context) throws IOException {
        super(context);
        File classifierFile = Loader.extractResource(getClass(), "/com/example/facecamera/haarcascade_frontalface_alt.xml", context.getCacheDir(), "classifier", ".xml");
        if (classifierFile == null || classifierFile.length() <= 0) {
            throw new IOException("Could not extract the classifier file from Java resource.");
        }
        Loader.load(opencv_objdetect.class);
        classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
        classifierFile.delete();
        if (classifier.isNull()) {
            throw new IOException("Could not load the classifier file.");
        }
        storage = CvMemStorage.create();
    }

    public void onPreviewFrame(final byte[] data, final Camera camera) {
        try {
        	this.data = data;
            Camera.Size size = camera.getParameters().getPreviewSize();
            processImage(data, size.width, size.height);
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
            // The camera has probably just been released, ignore.
        }
    }

    protected void processImage(byte[] data, int width, int height) {
        // First, downsample our image and convert it into a grayscale IplImage
        int f = SUBSAMPLING_FACTOR;
        if (grayImage == null || grayImage.width() != width/f || grayImage.height() != height/f) {
            grayImage = IplImage.create(width/f, height/f, IPL_DEPTH_8U, 1);
        }
        int imageWidth  = grayImage.width();
        int imageHeight = grayImage.height();
        int dataStride = f*width;
        int imageStride = grayImage.widthStep();
        ByteBuffer imageBuffer = grayImage.getByteBuffer();
        for (int y = 0; y < imageHeight; y++) {
            int dataLine = y*dataStride;
            int imageLine = y*imageStride;
            for (int x = 0; x < imageWidth; x++) {
                imageBuffer.put(imageLine + x, data[dataLine + f*x]);
            }
        }

        faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
        postInvalidate();
        cvClearMemStorage(storage);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(80);

        String s = "保持这个方向";
        float textWidth = paint.measureText(s);
        canvas.drawText(s, (getWidth()-textWidth)/2, 80, paint);

        if (faces != null) {
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            float scaleX = (float)getWidth()/grayImage.width();
            float scaleY = (float)getHeight()/grayImage.height();
            int total = faces.total();
            for (int i = 0; i < total; i++) {
                CvRect r = new CvRect(cvGetSeqElem(faces, i));
                int x = r.x(), y = r.y(), w = r.width(), h = r.height();
                canvas.drawRect(x*scaleX, y*scaleY, (x+w)*scaleX, (y+h)*scaleY, paint);
               
            }
        }
    }
    public void takePhoto(Context mContext,byte[] arg0,int screenW,int screenH){
    	String path = String.valueOf(System.currentTimeMillis());
    	File saveFile = new File(Environment.getExternalStorageDirectory().getPath()  + "/face_camera");
		if(!saveFile.exists())
			saveFile.mkdirs();
		File photoFile = new File(saveFile + "/" + path +".jpg");
		Log.i("photoFile",photoFile.toString());
		Bitmap screenBit = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
		Bitmap bit = Bitmap.createScaledBitmap(screenBit, screenW, screenH, false);
		Bitmap resultBit = Bitmap.createBitmap(screenW, screenH, Config.ARGB_8888);
		Canvas canvas = new Canvas(resultBit);
		canvas.drawBitmap(bit, 0, 0, null);
    	Paint paint = new Paint();
        paint.setColor(Color.RED);

    	 if (faces != null) {
             paint.setStrokeWidth(2);
             paint.setStyle(Paint.Style.STROKE);
             float scaleX = (float)getWidth()/grayImage.width();
             float scaleY = (float)getHeight()/grayImage.height();
             
             int total = faces.total();
             for (int i = 0; i < total; i++) {
                 CvRect r = new CvRect(cvGetSeqElem(faces, i));
                 int x = r.x(), y = r.y(), w = r.width(), h = r.height();
                 canvas.drawRect(x*scaleX, y*scaleY, (x+w)*scaleX, (y+h)*scaleY, paint);
                
             }
         }
    	 canvas.save();
		try {
			photoFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(photoFile);
			resultBit.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
			Intent intent = new Intent((Activity) mContext,ShowPhoto.class);
			Bundle bundle = new Bundle();
			bundle.putString("photoName", path);
			intent.putExtras(bundle);
			mContext.startActivity(intent);
			((Activity) mContext).finish();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
