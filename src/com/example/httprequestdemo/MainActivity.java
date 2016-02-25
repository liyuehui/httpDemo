package com.example.httprequestdemo;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private Button bt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		bt = (Button) findViewById(R.id.bt);
		bt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				HashMap<String,String> map = new HashMap<String,String>();
				map.put("jwt", "userid 2016-2-25 16:32");
				new HttpUtil().start("http://localhost:8080/WebDemo/helloworld", 
						map, null, HttpConRun.GET, null, null);
			}
		});
	}

	
	
}
