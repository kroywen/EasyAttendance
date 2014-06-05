package com.thepegeek.easyattendance.screen;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.AttendanceAdapter;
import com.thepegeek.easyattendance.model.Attendance;
import com.thepegeek.easyattendance.model.Course;
import com.thepegeek.easyattendance.model.Status;
import com.thepegeek.easyattendance.model.Student;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.util.Utils;
import com.thepegeek.easyattendance.view.wheel.OnWheelChangedListener;
import com.thepegeek.easyattendance.view.wheel.WheelView;
import com.thepegeek.easyattendance.view.wheel.adapters.ArrayWheelAdapter;

public class AttendancesScreen extends BaseScreen implements OnClickListener, OnItemClickListener {
	
	public static final int EDIT_MENU_ITEM = 0;
	public static final int DELETE_MENU_ITEM = 1;
	
	protected Button addAttendanceBtn;
	protected Button missedClassesBtn;
	protected ListView attendancesList;
	protected TextView empty;
	
	protected long courseId;
	protected Course course;
	protected List<Attendance> attendances;
	protected AttendanceAdapter adapter;
	
	protected boolean loggedIn;
	protected boolean showConfirmUploadingDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attendances_screen);
		getIntentData();
		initializeViews();
		
		adapter = new AttendanceAdapter(this, attendances);
		attendancesList.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateAttendances();
		
		AndroidAuthSession session = mApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
                loggedIn = true;
                
                if (showConfirmUploadingDialog) {
                	showConfirmUploadingDialog = false;
					ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
					NetworkInfo info = conManager.getActiveNetworkInfo();
					if (info == null || info.getState() != NetworkInfo.State.CONNECTED) {
						Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
					} else {
						new DropboxUploadTask().execute((Void[]) null);
					}
                }
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
	}
	
	protected void updateAttendances() {
		attendances = dbStorage.getAttendancesByCourseId(courseId, false);
		if (Utils.isEmpty(attendances)) {
			empty.setVisibility(View.VISIBLE);
			attendancesList.setVisibility(View.INVISIBLE);
		} else {
			empty.setVisibility(View.INVISIBLE);
			attendancesList.setVisibility(View.VISIBLE);
			adapter.setAttendances(attendances);
			adapter.notifyDataSetChanged();
		}
	}
	
	protected void getIntentData() {
		Intent intent = getIntent();
		if (intent != null && intent.hasExtra(DatabaseHelper.FIELD_COURSE_ID)) {
			courseId = intent.getLongExtra(DatabaseHelper.FIELD_COURSE_ID, 0);
			course = dbStorage.getCourseById(courseId);
			setScreenTitle(course.getName());
		}
	}
	
	protected void initializeViews() {
		addAttendanceBtn = (Button) findViewById(R.id.addAttendanceBtn);
		addAttendanceBtn.setOnClickListener(this);
		
		missedClassesBtn = (Button) findViewById(R.id.missedClassesBtn);
		missedClassesBtn.setOnClickListener(this);
		
		attendancesList = (ListView) findViewById(R.id.attendancesList);
		attendancesList.setOnItemClickListener(this);
		registerForContextMenu(attendancesList);		
		
		empty = (TextView) findViewById(R.id.empty);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Attendance attendance = attendances.get(position);
		Intent intent = new Intent(this, AttendanceScreen.class);
		intent.putExtra(DatabaseHelper.FIELD_ATTENDANCE_ID, attendance.getId());
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addAttendanceBtn:
			showSelectStartDateDialog();
			break;
		case R.id.missedClassesBtn:
			boolean hasMissedClasses = dbStorage.hasMissedClassesAttendances(courseId);
			if (hasMissedClasses) {
				showSelectExportMethodDialog();
			} else {
				showErrorDialog(R.string.no_missed_classes);
			}
			break;
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Attendance attendance = attendances.get(info.position);
		menu.setHeaderTitle(attendance.getDateString());
		menu.add(0, EDIT_MENU_ITEM, 0, R.string.edit);
		menu.add(0, DELETE_MENU_ITEM, 0, R.string.delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Attendance attendance = attendances.get(info.position);
		switch (item.getItemId()) {
		case DELETE_MENU_ITEM:
			dbStorage.deleteAttendance(attendance);
			dbStorage.deleteStudentsFromAttendance(attendance.getId());
			updateAttendances();
			break;
		}
		return true;
	}
	
	protected void showSelectExportMethodDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_option)
			.setItems(R.array.missed_classes_options, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						exportMissedClassesViaEmail();
						break;
					case 1:
						exportMissedClassesToDropbox();
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
	
	protected void exportMissedClassesViaEmail() {
		try {
			File tempFolder = new File(Environment.getExternalStorageDirectory()+"/easyattendance/temp/");
			if (tempFolder == null || !tempFolder.exists())
				tempFolder.mkdirs();
			
			String missedClassesCSV = getMissedClassesCsv();
			File file = new File(tempFolder, course.getName() + " - Missing.csv");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(missedClassesCSV.getBytes());
			fos.flush();
			fos.close();
			
			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("message/rfc822");
			ArrayList<Uri> uris = new ArrayList<Uri>();
			uris.add(Uri.fromFile(file));
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
		} catch (Exception e) {
			e.printStackTrace();
			showErrorDialog(R.string.upload_error);
		}
	}
	
	protected void exportMissedClassesToDropbox() {
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = conManager.getActiveNetworkInfo();
		if (info == null || info.getState() != NetworkInfo.State.CONNECTED) {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
		} else {
			if (loggedIn) {
				new DropboxUploadTask().execute((Void[]) null);
            } else {
            	showConfirmUploadingDialog = true;
                mApi.getSession().startAuthentication(this);
            }		
		}
	}
	
	protected String getMissedClassesCsv() {
		String result = "";
		result += course.getName() + "\n";
		result += "Attendance Date,Student Name, Miss Status\n";
		List<Attendance> attendances = dbStorage.getAttendancesWithMissedStudentStatus(courseId);
		if (!Utils.isEmpty(attendances)) {
			for (Attendance attendance : attendances) {
				List<Student> students = attendance.getStudents();
				if (!Utils.isEmpty(students)) {
					result += "\"" + attendance.getDateString() + "\"\n";
					for (Student student : students) {
						Status status = student.getStatus();
						if (status != null) {
							result += "," + student.getFullname() + "," + status.getName() + "\n";
						}
					}
				}
			}
		}
		return result;
	}
	
	protected void showSelectStartDateDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.select_start_date_dialog, null);
		
		long currentTime = System.currentTimeMillis();
		
		String[] dateItems = Utils.getDateItems(currentTime);
		final WheelView dateWheel = (WheelView) dialogView.findViewById(R.id.date);
		ArrayWheelAdapter<String> dateAdapter = new ArrayWheelAdapter<String>(this, dateItems);
		dateAdapter.setTextSize(18);
		dateWheel.setViewAdapter(dateAdapter);
		dateWheel.setCurrentItem(Utils.getDatePosition(currentTime));
		
		final WheelView ampmWheel = (WheelView) dialogView.findViewById(R.id.ampmWheel);
		ArrayWheelAdapter<String> ampmAdapter = new ArrayWheelAdapter<String>(this, Utils.ampmItems);
		ampmAdapter.setTextSize(18);
		ampmWheel.setViewAdapter(ampmAdapter);
		ampmWheel.setCurrentItem(Utils.getAmpm(currentTime));
		ampmWheel.setEnabled(false);
		
		final WheelView hoursWheel = (WheelView) dialogView.findViewById(R.id.hours);
		ArrayWheelAdapter<String> hoursAdapter = new ArrayWheelAdapter<String>(this, Utils.getHoursItems());
		hoursAdapter.setTextSize(18);
		hoursWheel.setViewAdapter(hoursAdapter);
		hoursWheel.setCurrentItem(Utils.getHours(currentTime));
		hoursWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				ampmWheel.setCurrentItem(newValue < 12 ? 0 : 1, true);
			}
		});
		
		final WheelView minutesWheel = (WheelView) dialogView.findViewById(R.id.minutes);
		ArrayWheelAdapter<String> minutesAdapter = new ArrayWheelAdapter<String>(this, Utils.getMinutesItems());
		minutesAdapter.setTextSize(18);
		minutesWheel.setViewAdapter(minutesAdapter);
		minutesWheel.setCurrentItem(Utils.getMinutes(currentTime));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Date date = Utils.getDate(dateWheel.getCurrentItem());
				if (date != null) {
					Date selected = new Date();
					selected.setYear(date.getYear());
					selected.setMonth(date.getMonth());
					selected.setDate(date.getDate());
					selected.setHours(hoursWheel.getCurrentItem());
					selected.setMinutes(minutesWheel.getCurrentItem());
					Attendance attendance = new Attendance(0, courseId, selected.getTime(), null, null);
					long attendanceId = dbStorage.addAttendance(attendance);
					attendance.setId(attendanceId);
					dbStorage.addStudentsToAttendance(attendance);
					updateAttendances();
				}
				dialog.dismiss();
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
	
	class DropboxUploadTask extends AsyncTask<Void, Long, Boolean> {
		
		protected ProgressDialog dialog;
		protected String error;
		
		protected String currentName;
		
		public DropboxUploadTask() {
			dialog = new ProgressDialog(AttendancesScreen.this);
    		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		dialog.setMessage(getString(R.string.uploading));
    		dialog.setIndeterminate(true);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String rootFolder = "/Easy Attendance";
				try { mApi.createFolder(rootFolder); } catch (Exception e) { e.printStackTrace(); }
				
				String csv = getMissedClassesCsv();
				currentName = course.getName() + " - Missing.csv";
				
				String filePath = rootFolder + "/" + currentName;
				ByteArrayInputStream bais = new ByteArrayInputStream(csv.getBytes());
				try {
					mApi.putFileOverwrite(filePath, bais, csv.getBytes().length, new ProgressListener() {
						@Override
						public void onProgress(long bytes, long total) {
							publishProgress(bytes, total);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (bais != null)
						bais.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				error = getString(R.string.upload_error);
				return false;
			}
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Long... values) {
			String message = String.format(getString(R.string.upload_pattern), 
					currentName, Utils.getReadableFilesize(values[0]), Utils.getReadableFilesize(values[1]));
			dialog.setMessage(message);
			super.onProgressUpdate(values);
		}
		
		@Override
		public void onPostExecute(Boolean result) {
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			if (!result.booleanValue()) {
				Toast.makeText(AttendancesScreen.this, error, Toast.LENGTH_SHORT).show();
			} else {
				showInfoDialog(R.string.success, R.string.csv_uploaded);
			}
		}
		
	}

}