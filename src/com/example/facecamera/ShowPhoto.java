package com.example.facecamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ShowPhoto extends Activity{
	private ImageView showPhoto;
	private Button back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showphoto);
		back = (Button) findViewById(R.id.back);
		showPhoto = (ImageView) findViewById(R.id.showPhoto);
		String path = getIntent().getExtras().getString("photoName");
		showPhoto.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + "/face_camera/"  + path + ".jpg"));
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ShowPhoto.this.startActivity(new Intent(ShowPhoto.this,MainActivity.class));
				ShowPhoto.this.finish();
			}
		});
	}

}
