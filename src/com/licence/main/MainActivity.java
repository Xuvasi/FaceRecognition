package com.licence.main;

import java.io.File;
import java.io.FileNotFoundException;

import org.opencv.samples.facedetect.FdActivity;
import org.opencv.samples.facedetect.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.licence.templateface.TemplateFace;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((Button) findViewById(R.id.recognize_face))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						recognizeFaces();
					}
				});
					

		((Button) findViewById(R.id.template_face))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						templateFace();
					}
				});
	}

	private void recognizeFaces() {

		Intent i = new Intent(this, FdActivity.class);
		this.startActivity(i);
	}

	private void templateFace() {
		Intent i = new Intent(this, TemplateFace.class);
		this.startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
