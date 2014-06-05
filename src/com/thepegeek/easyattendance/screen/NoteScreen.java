package com.thepegeek.easyattendance.screen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.Attendance;
import com.thepegeek.easyattendance.storage.DatabaseHelper;

public class NoteScreen extends BaseScreen implements OnClickListener {
	
	public static final int TAKE_PHOTO_REQUEST_CODE = 0;
	public static final int SELECT_FROM_LIBRARY_REQUEST_CODE = 1;
	
	protected EditText note;
	protected ImageView photo;
	protected Button addPhotoBtn;
	
	protected long attendanceId;
	protected Attendance attendance;
	
	protected Handler handler = new Handler();
	protected boolean dimensionsInited;
	protected int targetW;
	protected int targetH;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_screen);
		setScreenTitle(R.string.notes);
		getIntentData();
		initializeViews();
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
						updatePhoto();
					}
				});
			}
		}, 100);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (attendance != null) {
			note.setText(attendance.getNote());
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (attendance != null) {
			attendance.setNote(note.getText().toString().trim());
			dbStorage.updateAttendance(attendance);
		}
	}
	
	protected void getIntentData() {
		Intent intent = getIntent();
		if (intent != null && intent.hasExtra(DatabaseHelper.FIELD_ATTENDANCE_ID)) {
			attendanceId = intent.getLongExtra(DatabaseHelper.FIELD_ATTENDANCE_ID, 0);
			attendance = dbStorage.getAttendanceById(attendanceId);
		}
	}
	
	protected void initializeViews() {
		note = (EditText) findViewById(R.id.note);
		photo = (ImageView) findViewById(R.id.photo);
		addPhotoBtn = (Button) findViewById(R.id.addPhotoBtn);
		addPhotoBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.addPhotoBtn) {
			showSelectOptionDialog();
		}
	}
	
	protected void updatePhoto() {
		if (!TextUtils.isEmpty(attendance.getPhoto())) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(attendance.getPhoto(), options);
			int photoW = options.outWidth;
			int photoH = options.outHeight;
			
			int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
			options.inJustDecodeBounds = false;
			options.inSampleSize = scaleFactor;
			options.inPurgeable = true;
			
			Bitmap bm = BitmapFactory.decodeFile(attendance.getPhoto(), options);
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
	
	protected void showSelectOptionDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_option)
			.setItems(R.array.add_photo_options, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						takePhoto();
						break;
					case 1:
						selectFromLibrary();
						break;
					case 2:
						removePhoto();
						break;
					}
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create()
			.show();
	}
	
	protected void takePhoto() {
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			if (isIntentAvailable(MediaStore.ACTION_IMAGE_CAPTURE)) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
			} else {
				showErrorDialog(R.string.no_app);
			}
		} else {
			showErrorDialog(R.string.no_camera_detected);
		}
	}
	
	protected void selectFromLibrary() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, SELECT_FROM_LIBRARY_REQUEST_CODE);
	}
	
	protected void deletePhoto() {
		if (TextUtils.isEmpty(attendance.getPhoto()))
			return;
		try {
			File f = new File(attendance.getPhoto());
			f.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void removePhoto() {
		deletePhoto();
		attendance.setPhoto(null);
		dbStorage.updateAttendance(attendance);
		updatePhoto();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case TAKE_PHOTO_REQUEST_CODE:
			case SELECT_FROM_LIBRARY_REQUEST_CODE:
				(new AddImageTask(data)).execute((Void[]) null);
				break;
			}
		}
	}
	
	class AddImageTask extends AsyncTask<Void, Void, Boolean> {
		
		protected Intent data;
		
		public AddImageTask(Intent data) {
			this.data = data;
		}
		
		@Override
		protected void onPreExecute() {
			showProgressDialog(getString(R.string.importing));
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Bitmap bitmap = null;
			BitmapFactory.Options options = new BitmapFactory.Options();
			
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			int screenW = metrics.widthPixels;
			int screenH = metrics.heightPixels;
			
			try {
				if (data != null && data.hasExtra("data")) {
					// image captured from camera
					bitmap = (Bitmap) data.getExtras().get("data");
				} else {
					// image taken from gallery
					String realPath = getRealPathFromURI(data.getData());
					options.inJustDecodeBounds = true;
					bitmap = BitmapFactory.decodeFile(realPath, options);
					
					int reqWidth = options.outWidth; 
					int reqHeight = options.outHeight; 
					int scaleFactor = Math.min(reqWidth/screenW, reqHeight/screenH);
					options.inJustDecodeBounds = false;
					options.inSampleSize = scaleFactor;
					options.inPurgeable = true;
					
					bitmap = BitmapFactory.decodeFile(realPath, options);
				}
				
				if (bitmap != null) {
					String path = saveImage(bitmap);
			    	attendance.setPhoto(path);
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			hideProgressDialog();
			if (result) {
				updatePhoto();
			} else {
				Toast.makeText(NoteScreen.this, R.string.add_image_error, Toast.LENGTH_SHORT).show();
			}
		}
		
		public String getRealPathFromURI(Uri contentUri) {
		    String result = "";
		    try {
		    	String[] proj = { MediaStore.Images.Media.DATA };
		    	Cursor c = NoteScreen.this.getContentResolver().query(contentUri, proj, null, null, null);
			    if (c != null && c.moveToFirst()) {
			    	result = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
			    }
			    if (c != null)
			    	c.close();
		    } catch (Exception e) {
				e.printStackTrace();
			}
		    return result;
		}
		
		protected String saveImage(Bitmap image) throws FileNotFoundException, IOException {
			File imageDir = new File(Environment.getExternalStorageDirectory(), "/easyattendance/images/");
			if (!imageDir.exists())
				imageDir.mkdirs();
			
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
			String filename = "IMAGE_" + timeStamp + ".png";
			
			File tmpFile = new File(imageDir, filename);
			FileOutputStream fos = new FileOutputStream(tmpFile);
			image.compress(Bitmap.CompressFormat.PNG, 0, fos);
			fos.close();
			return tmpFile.getAbsolutePath();
		}
		
	}

}
