package com.thepegeek.easyattendance.screen;

import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.Student;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.util.Utils;

public class RandomStudentScreen extends BaseScreen implements OnClickListener {
	
	protected ImageView photo;
	protected TextView name;
	protected Button nextStudentBtn;
	
	protected long courseId;
	protected List<Student> students;
	protected int current = -1;
	
	protected Handler handler = new Handler();
	protected boolean dimensionsInited;
	protected int targetW;
	protected int targetH;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.random_student_screen);
		setScreenTitle(R.string.random_student);
		getIntentData();
		initializeViews();
		
		students = dbStorage.getStudentsByCourseId(courseId);
		Collections.shuffle(students);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!dimensionsInited) {
							targetW = photo.getWidth();
							targetH = photo.getHeight();
							dimensionsInited = true;
						}
						nextStudent();
					}
				});
			}
		}, 100);
	}
	
	protected void getIntentData() {
		Intent intent = getIntent();
		if (intent != null && intent.hasExtra(DatabaseHelper.FIELD_COURSE_ID)) {
			courseId = intent.getLongExtra(DatabaseHelper.FIELD_COURSE_ID, 0);
		}
	}
	
	protected void initializeViews() {
		photo = (ImageView) findViewById(R.id.photo);
		name = (TextView) findViewById(R.id.name);
		nextStudentBtn = (Button) findViewById(R.id.nextStudentBtn);
		nextStudentBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.nextStudentBtn) {
			nextStudent();
		}
	}
	
	protected void nextStudent() {
		if (!Utils.isEmpty(students)) {
			current = (current == students.size()-1) ? 0 : current+1;
			Student student = students.get(current);
			name.setText(student.getFullname());
			updatePhoto();
		}
	}
	
	protected void updatePhoto() {
		Student student = students.get(current);
		if (!TextUtils.isEmpty(student.getPhoto())) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(student.getPhoto(), options);
			int photoW = options.outWidth;
			int photoH = options.outHeight;
			
			int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
			options.inJustDecodeBounds = false;
			options.inSampleSize = scaleFactor;
			options.inPurgeable = true;
			
			Bitmap bm = BitmapFactory.decodeFile(student.getPhoto(), options);
			final Bitmap scaled = Bitmap.createScaledBitmap(bm, targetW, targetH, false);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					photo.setImageBitmap(scaled);
				}
			});
		} else {
			photo.setImageResource(R.drawable.no_photo);
		}
	}

}
