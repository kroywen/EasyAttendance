package com.thepegeek.easyattendance.screen;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.AttendanceStudentAdapter;
import com.thepegeek.easyattendance.model.Attendance;
import com.thepegeek.easyattendance.model.Course;
import com.thepegeek.easyattendance.model.Status;
import com.thepegeek.easyattendance.model.Student;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.util.Utils;

public class AttendanceScreen extends BaseScreen implements OnClickListener {
	
	protected Button addNoteBtn;
	protected Button communicateBtn;
	protected ListView studentsList;
	protected TextView empty;
	
	protected long attendanceId;
	protected Attendance attendance;
	protected List<Student> students;
	protected AttendanceStudentAdapter adapter;
	
	protected boolean loggedIn;
	protected boolean showConfirmUploadingDialog;
	protected enum ExportMethod {DAY_REPORT, MISSING_REPORT, ATTENDANCE_REPORT}
	protected ExportMethod method;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attendance_screen);
		getIntentData();
		setScreenTitle(attendance != null ? attendance.getDateString2() : getString(R.string.attendance));
		initializeViews();
		
		adapter = new AttendanceStudentAdapter(this, students, attendanceId);
		studentsList.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateStudents();
		
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
						if (method != ExportMethod.MISSING_REPORT) {
							new DropboxUploadTask(method).execute((Void[]) null);
						} else{
							boolean result = dbStorage.hasMissedClasses(attendanceId);
							if (result) {
								new DropboxUploadTask(ExportMethod.MISSING_REPORT).execute((Void[]) null);
							} else {
								showErrorDialog(R.string.no_missed_classes_in_attendance);
							}
						}
					}
                }
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
	}
	
	protected void updateStudents() {
		students = dbStorage.getStudentsForAttendance(attendanceId);
		if (Utils.isEmpty(students)) {
			empty.setVisibility(View.VISIBLE);
			studentsList.setVisibility(View.INVISIBLE);
		} else {
			empty.setVisibility(View.INVISIBLE);
			studentsList.setVisibility(View.VISIBLE);
			adapter.setStudents(students);
			adapter.notifyDataSetChanged();
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
		addNoteBtn = (Button) findViewById(R.id.addNoteBtn);
		addNoteBtn.setOnClickListener(this);
		
		communicateBtn = (Button) findViewById(R.id.communicateBtn);
		communicateBtn.setOnClickListener(this);
		
		studentsList = (ListView) findViewById(R.id.studentsList);
		
		empty = (TextView) findViewById(R.id.empty);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addNoteBtn:
			Intent intent = new Intent(this, NoteScreen.class);
			intent.putExtra(DatabaseHelper.FIELD_ATTENDANCE_ID, attendanceId);
			startActivity(intent);
			break;
		case R.id.communicateBtn:
			showSelectCommunicateDialog();
			break;
		}
	}
	
	protected void showSelectCommunicateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_option)
			.setItems(R.array.communicate_options, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						emailDayReport();
						break;
					case 1:
						emailMissingReport();
						break;
					case 2:
						emailAttendanceReport();
						break;
					case 3:
						dropboxDayReport();
						break;
					case 4:
						dropboxMissingReport();
						break;
					case 5:
						dropboxAttendanceReport();
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
	
	protected void emailDayReport() {
		try {
			File tempFolder = new File(Environment.getExternalStorageDirectory()+"/easyattendance/temp");
			if (tempFolder == null || !tempFolder.exists())
				tempFolder.mkdirs();
			
			Course course = dbStorage.getCourseById(attendance.getCourseId());
			String dayReportCsv = getDayReportCsv(course.getName());
			File dayReportFile = new File(tempFolder, course.getName() + " Day Report(" + attendance.getDateString3()+").csv");
			FileOutputStream fos = new FileOutputStream(dayReportFile);
			fos.write(dayReportCsv.getBytes());
			fos.flush();
			fos.close();
			
			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("message/rfc822");
			ArrayList<Uri> uris = new ArrayList<Uri>();
			uris.add(Uri.fromFile(dayReportFile));
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
		} catch (Exception e) {
			e.printStackTrace();
			showErrorDialog(R.string.upload_error);
		}
	}
	
	protected void dropboxDayReport() {
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = conManager.getActiveNetworkInfo();
		if (info == null || info.getState() != NetworkInfo.State.CONNECTED) {
			Toast.makeText(AttendanceScreen.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
		} else {
			if (loggedIn) {
				new DropboxUploadTask(ExportMethod.DAY_REPORT).execute((Void[]) null);
            } else {
            	method = ExportMethod.DAY_REPORT;
            	showConfirmUploadingDialog = true;
                mApi.getSession().startAuthentication(AttendanceScreen.this);
            }		
		}
	}
	
	protected String getDayReportCsv(String courseName) {
		String result = "";
		result += courseName + "\n";
		result += "Student Name\\Attendance,\"" + attendance.getDateString2() + "\"\n";
		if (!Utils.isEmpty(students)) {
			for (Student student : students) {
				result += student.getFullname() + ",";
				Student st = dbStorage.getStudentForAttendance(student.getId(), attendanceId);
				com.thepegeek.easyattendance.model.Status status= st.getStatus();
				if (status != null) {
					result += status.getName() + "\n";
				}
			}
		}
		return result;
	}
	
	protected void emailMissingReport() {
		try {
			boolean result = dbStorage.hasMissedClasses(attendanceId);
			if (result) {
				File tempFolder = new File(Environment.getExternalStorageDirectory()+"/easyattendance/temp");
				if (tempFolder == null || !tempFolder.exists())
					tempFolder.mkdirs();
				
				Course course = dbStorage.getCourseById(attendance.getCourseId());
				String missingReportCsv = getMissingReportCsv(course.getName());
				File missingReportFile = new File(tempFolder, course.getName() + " " + attendance.getDateString4()+" - Missing Report.csv");
				FileOutputStream fos = new FileOutputStream(missingReportFile);
				fos.write(missingReportCsv.getBytes());
				fos.flush();
				fos.close();
				
				Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
				intent.setType("message/rfc822");
				ArrayList<Uri> uris = new ArrayList<Uri>();
				uris.add(Uri.fromFile(missingReportFile));
				intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
			} else {
				showErrorDialog(R.string.no_missed_classes_in_attendance);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showErrorDialog(R.string.upload_error);
		}
	}
	
	protected void dropboxMissingReport() {
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = conManager.getActiveNetworkInfo();
		if (info == null || info.getState() != NetworkInfo.State.CONNECTED) {
			Toast.makeText(AttendanceScreen.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
		} else {
			boolean result = dbStorage.hasMissedClasses(attendanceId);
			if (result) {
				if (loggedIn) {
					new DropboxUploadTask(ExportMethod.MISSING_REPORT).execute((Void[]) null);
	            } else {
	            	method = ExportMethod.MISSING_REPORT;
	            	showConfirmUploadingDialog = true;
	                mApi.getSession().startAuthentication(AttendanceScreen.this);
	            }
			} else {
				showErrorDialog(R.string.no_missed_classes_in_attendance);
			}
		}
	}
	
	protected String getMissingReportCsv(String courseName) {
		String result = "";
		Attendance att = dbStorage.getAttendanceWithMissedStudentStatus(attendanceId);
		if (att != null) {
			result += "Course Name," + courseName + "\n";
			result += "Attendance Date,\"" + att.getDateString() + "\"\n";
			result += "\nStudent Name,Status\n";
			if (!Utils.isEmpty(att.getStudents())) {
				for (Student student : att.getStudents()) {
					Status status = student.getStatus();
					if (status != null) {
						result += student.getFullname() + "," + status.getName() + "\n";
					}
				}
			}
		}
		Log.d("result", result);
		return result;
	}
	
	protected void emailAttendanceReport() {
		try {
			File tempFolder = new File(Environment.getExternalStorageDirectory()+"/easyattendance/temp");
			if (tempFolder == null || !tempFolder.exists())
				tempFolder.mkdirs();
			
			Course course = dbStorage.getCourseById(attendance.getCourseId());
			String attendanceReportCsv = getAttendanceReportCsv(course.getName());
			File attendanceReportFile = new File(tempFolder, course.getName() + " " + attendance.getDateString4()+" - Report.csv");
			FileOutputStream fos = new FileOutputStream(attendanceReportFile);
			fos.write(attendanceReportCsv.getBytes());
			fos.flush();
			fos.close();
			
			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("message/rfc822");
			ArrayList<Uri> uris = new ArrayList<Uri>();
			uris.add(Uri.fromFile(attendanceReportFile));
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
		} catch (Exception e) {
			e.printStackTrace();
			showErrorDialog(R.string.upload_error);
		}
	}
	
	protected void dropboxAttendanceReport() {
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = conManager.getActiveNetworkInfo();
		if (info == null || info.getState() != NetworkInfo.State.CONNECTED) {
			Toast.makeText(AttendanceScreen.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
		} else {
			if (loggedIn) {
				new DropboxUploadTask(ExportMethod.ATTENDANCE_REPORT).execute((Void[]) null);
            } else {
            	method = ExportMethod.ATTENDANCE_REPORT;
            	showConfirmUploadingDialog = true;
                mApi.getSession().startAuthentication(AttendanceScreen.this);
            }		
		}
	}
	
	protected String getAttendanceReportCsv(String courseName) {
		String result = "";
		result += "Course Name," + courseName + "\n";
		result += "Attendance Date,\"" + attendance.getDateString() + "\"\n";
		result += "Student Name,Status\n";
		if (!Utils.isEmpty(students)) {
			for (Student student : students) {
				result += student.getFullname() + ",";
				Student st = dbStorage.getStudentForAttendance(student.getId(), attendanceId);
				com.thepegeek.easyattendance.model.Status status= st.getStatus();
				if (status != null) {
					result += status.getName() + "\n";
				}
			}
		}
		return result;
	}
	
	
	class DropboxUploadTask extends AsyncTask<Void, Long, Boolean> {
		
		protected ProgressDialog dialog;
		protected ExportMethod exportMethod;
		protected String error;
		
		protected String currentName;
		
		public DropboxUploadTask(ExportMethod exportMethod) {
			this.exportMethod = exportMethod;
			dialog = new ProgressDialog(AttendanceScreen.this);
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
				
				Course course = dbStorage.getCourseById(attendance.getCourseId());
				String csv = null;
				if (exportMethod == ExportMethod.DAY_REPORT) {
					csv = getDayReportCsv(course.getName());
					currentName = course.getName() + " Day Report(" + attendance.getDateString3()+").csv";
				} else if (exportMethod == ExportMethod.MISSING_REPORT) {
					csv = getMissingReportCsv(course.getName());
					currentName = course.getName() + " " + attendance.getDateString4()+" - Missing Report.csv";
				} else if (exportMethod == ExportMethod.ATTENDANCE_REPORT) {
					csv = getAttendanceReportCsv(course.getName());
					currentName = course.getName() + " " + attendance.getDateString4()+" - Report.csv";
				}
				
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
				Toast.makeText(AttendanceScreen.this, error, Toast.LENGTH_SHORT).show();
			} else {
				showInfoDialog(R.string.success, R.string.csv_uploaded);
			}
		}
		
	}

}
