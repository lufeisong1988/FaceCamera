package com.example.facecamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginActivity extends Activity implements OnClickListener{
	private Button openBnt,openBnt2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		init();
		listener();
	}
	void init(){
		openBnt = (Button) findViewById(R.id.open);
		openBnt2 =(Button) findViewById(R.id.open2);
	}
	void listener(){
		openBnt.setOnClickListener(this);
		openBnt2.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.open:
			startActivity(new Intent(LoginActivity.this,MainActivity.class));
			break;
		case R.id.open2:
			startActivity(new Intent(LoginActivity.this,MainActivity2.class));
			break;
		}
		
	}
}
