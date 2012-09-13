package com.nandeesh.fileprovider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class MainActivity extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.button1).setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onClick(View v) {
		Uri mUri = Uri.parse("content://com.nandeesh.fileprovider");
		Intent in = new Intent();
		
		// Ideally we should be able to launch gallery, but for some strange
		// reason gallery is trying to get the absolute filepath, which in our
		// case is not present So cannot use ACTION_VIEW
		
		
		// in.setAction(Intent.ACTION_VIEW); 
		 //in.setDataAndType(mUri,"image/jpeg");
		 
		 
		 
		in.setClassName("com.example.imageviewer",
				"com.example.imageviewer.MainActivity");
		in.setData(mUri);
		try {
			startActivity(in);	
		} catch (Exception e) {
			Toast.makeText(this, "Please install ImageViewer app", Toast.LENGTH_LONG).show();
		}
	}

}
