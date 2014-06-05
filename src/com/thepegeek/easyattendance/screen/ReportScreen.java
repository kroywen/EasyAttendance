package com.thepegeek.easyattendance.screen;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.ReportAdapter;
import com.thepegeek.easyattendance.adapter.ReportAdapter.Item;
import com.thepegeek.easyattendance.adapter.ReportAdapter.ItemType;
import com.thepegeek.easyattendance.dialog.DatePickerFragment;
import com.thepegeek.easyattendance.model.Attendance;
import com.thepegeek.easyattendance.model.Course;
import com.thepegeek.easyattendance.model.Student;
import com.thepegeek.easyattendance.util.Utils;

public class ReportScreen extends BaseScreen implements OnClickListener, OnItemClickListener {
	
	public static final String DATE_FORMAT = "LLL dd, yyyy";
	public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()); 
	
	protected EditText dates;
	protected Button betweenDatesBtn;
	protected Button everyAttendanceBtn;
	protected ListView coursesList;
	protected TextView empty;
	
	protected List<Course> courses;
	protected ReportAdapter adapter;
	
	protected boolean allCourses;
	protected boolean everyAttendance;
	protected Course exportCourse;
	protected Calendar from;
	protected Calendar to;
	
	protected int dropboxType;
	protected boolean loggedIn;
	protected boolean showConfirmUploadingDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_screen);
		setScreenTitle(R.string.report);
		initializeViews();
		
		from = Calendar.getInstance();
		from.set(Calendar.HOUR_OF_DAY, 0);
		from.set(Calendar.MINUTE, 0);
		to = Calendar.getInstance();
		from.set(Calendar.HOUR_OF_DAY, 23);
		from.set(Calendar.MINUTE, 59);
		everyAttendance = false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateCourses();
		updateDates();
		
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
						new DropboxUploadTask(dropboxType, exportCourse).execute((Void[]) null);
					}
                }
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
	}
	
	protected void updateDates() {
		dates.setEnabled(!everyAttendance);
		dates.setText(!everyAttendance ? getDatesString() : getString(R.string.every_attendance));
	}
	
	protected String getDatesString() {
		return sdf.format(new Date(from.getTimeInMillis())) + " - " + sdf.format(new Date(to.getTimeInMillis()));
	}
	
	protected void initializeViews() {
		dates = (EditText) findViewById(R.id.dates);
		dates.setOnClickListener(this);
		betweenDatesBtn = (Button) findViewById(R.id.betweenDatesBtn);
		betweenDatesBtn.setOnClickListener(this);
		everyAttendanceBtn = (Button) findViewById(R.id.everyAttendanceBtn);
		everyAttendanceBtn.setOnClickListener(this);
		coursesList = (ListView) findViewById(R.id.coursesList);
		coursesList.setOnItemClickListener(this);
		empty = (TextView) findViewById(R.id.empty);
	}
	
	protected void updateCourses() {
		courses = dbStorage.getCourses();
		if (Utils.isEmpty(courses)) {
			empty.setVisibility(View.VISIBLE);
			coursesList.setVisibility(View.INVISIBLE);
		} else {
			empty.setVisibility(View.INVISIBLE);
			coursesList.setVisibility(View.VISIBLE);
			initAdapter();
		}
	}
	
	protected void initAdapter() {
		adapter = new ReportAdapter(this);
		adapter.addItem(new ReportAdapter.Item(ItemType.ALL_COURSES));
		for (Course course : courses) {
			adapter.addItem(new ReportAdapter.Item(course));
		}
		coursesList.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.betweenDatesBtn:
			everyAttendance = false;
			updateDates();
			break;
		case R.id.everyAttendanceBtn:
			everyAttendance = true;
			updateDates();
			break;
		case R.id.dates:
			if (!everyAttendance) {
				showSelectFromDialog();
			}
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Item item = adapter.getItem(position);
		if (item.type == ItemType.ALL_COURSES) {
			allCourses = true;
			exportCourse = null;
		} else {
			allCourses = false;
			exportCourse = item.course;
		}
//		if (hasAttendancesToExport()) {
			showSelectExportDialog();
//		} else {
//			showErrorDialog(R.string.nothing_to_export);
//		}
	}
	
	protected boolean hasAttendancesToExport() {
		if (allCourses) {
			if (!Utils.isEmpty(courses)) {
				for (Course course : courses) {
					List<Attendance> attendances = dbStorage.getAttendancesByCourseId(course.getId(), false);
					if (!Utils.isEmpty(attendances)) {
						if (everyAttendance) {
							return true;
						} else {
							Date fromDate = new Date(from.getTimeInMillis());
							Date toDate = new Date(to.getTimeInMillis());
							for (Attendance attendance : attendances) {
								Date attDate = new Date(attendance.getDate());
								if (attDate.after(fromDate) && attDate.before(toDate))
									return true;
							}
						}
					}
				}
				return false;
			} else {
				return false;
			}
		} else {
			if (exportCourse != null) {
				List<Attendance> attendances = dbStorage.getAttendancesByCourseId(exportCourse.getId(), false);
				if (!Utils.isEmpty(attendances)) {
					if (everyAttendance) {
						return true;
					} else {
						for (Attendance attendance : attendances) {
							return isBetweenDates(attendance.getDate(), from.getTimeInMillis(), to.getTimeInMillis());
						}
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
	
	protected void showSelectExportDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.export)
			.setItems(R.array.report_options, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						new EmailReportTask(EmailReportTask.TYPE_FULL, exportCourse).execute();
						break;
					case 1:
						new EmailReportTask(EmailReportTask.TYPE_SUMMARY, exportCourse).execute();
						break;
					case 2:
						dropboxType = DropboxUploadTask.TYPE_FULL;
						dropboxReport();
						break;
					case 3:
						dropboxType = DropboxUploadTask.TYPE_SUMMARY;
						dropboxReport();
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
	
	protected void dropboxReport() {
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = conManager.getActiveNetworkInfo();
		if (info == null || info.getState() != NetworkInfo.State.CONNECTED) {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
		} else {
			if (loggedIn) {
				new DropboxUploadTask(dropboxType, exportCourse).execute();
            } else {
            	showConfirmUploadingDialog = true;
                mApi.getSession().startAuthentication(this);
            }		
		}
	}
	
	protected void showSelectFromDialog() {
		DatePickerFragment dp = new DatePickerFragment();
		dp.setPositiveButtonText(R.string.to);
		dp.set(from.get(Calendar.YEAR), from.get(Calendar.MONTH), from.get(Calendar.DAY_OF_MONTH));
		dp.setOnDateSetListener(new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				showSelectToDialog(year, monthOfYear, dayOfMonth);
			}
		});
		dp.show(getSupportFragmentManager(), "datePickerFrom");
	}
	
	protected void showSelectToDialog(final int fromYear, final int fromMonthOfYear, final int fromDayOfMonth) {
		DatePickerFragment dp = new DatePickerFragment();
		dp.setPositiveButtonText(R.string.ok);
		dp.set(to.get(Calendar.YEAR), to.get(Calendar.MONTH), to.get(Calendar.DAY_OF_MONTH));
		dp.setOnDateSetListener(new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				from.set(Calendar.YEAR, fromYear);
				from.set(Calendar.MONTH, fromMonthOfYear);
				from.set(Calendar.DAY_OF_MONTH, fromDayOfMonth);
				to.set(Calendar.YEAR, year);
				to.set(Calendar.MONTH, monthOfYear);
				to.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateDates();
			}
		});
		dp.show(getSupportFragmentManager(), "datePickerTo");
	}
	
	@SuppressWarnings("deprecation")
	protected boolean isBetweenDates(long date, long date1, long date2) {
		Date dateTest = new Date(date);
		Date dateFrom = new Date(date1);
		Date dateTo = new Date(date2);
		if (dateTest.getDate() == dateFrom.getDate() && dateTest.getMonth() == dateFrom.getMonth() && dateTest.getYear() == dateFrom.getYear()) {
			return true;
		} else if (dateTest.getDate() == dateTo.getDate() && dateTest.getMonth() == dateTo.getMonth() && dateTest.getYear() == dateTo.getYear()) {
			return true;
		} else {
			return dateTest.after(dateFrom) && dateTest.before(dateTo);
		}
	}
	
	protected String getCourseFullCsv(Course course) {
		String result = "";
		result += course.getName() + "\n";
		result += "Student Name\\Attendance";
		List<Attendance> attendances = filterAttendances(dbStorage.getAttendancesByCourseId(course.getId(), false));
		result += (!Utils.isEmpty(attendances)) ? "," : "\n";
		if (!Utils.isEmpty(attendances)) {
			for (int i=0; i<attendances.size(); i++) {
				result += attendances.get(i).getDateString2();
				result += (i < attendances.size()-1) ? "," : "\n"; 
			}
		}
		List<Student> students = dbStorage.getStudentsByCourseId(course.getId());
		if (!Utils.isEmpty(students)) {
			for (Student student : students) {
				result += student.getFullname();
				result += (!Utils.isEmpty(attendances)) ? "," : "\n";
				for (int i=0; i<attendances.size(); i++) {
					Student st = dbStorage.getStudentForAttendance(student.getId(), attendances.get(i).getId());
					com.thepegeek.easyattendance.model.Status status= st.getStatus();
					if (status != null) {
						result += status.getName();
						result += (i < attendances.size()-1) ? "," : "\n";
					}
				}
			}
		}
		return result;
	}
	
	protected String getCourseSummaryCsv(Course course) {
		String result = "";
		result += course.getName() + "\n";
		List<com.thepegeek.easyattendance.model.Status> statuses = dbStorage.getStatuses();
		if (!Utils.isEmpty(statuses)) {
			result += "Students Name\\Status,";
			for (int i=0; i<statuses.size(); i++) {
				result += statuses.get(i).getName();
				result += (i < statuses.size()-1) ? "," : "\n";
			}
			List<Student> students = dbStorage.getStudentsByCourseId(course.getId());
			if (!Utils.isEmpty(students)) {
				List<Attendance> attendances = filterAttendances(dbStorage.getAttendancesByCourseId(course.getId(), false));
				for (Student student : students) {
					result += student.getFullname();
					result += (!Utils.isEmpty(attendances)) ? "," : "\n";
					if (!Utils.isEmpty(attendances)) {
						for (int i=0; i<statuses.size(); i++) {
							int count = 0;
							for (Attendance attendance : attendances) {
								count += dbStorage.getStatusCountForStudent(student.getId(), statuses.get(i).getId(), attendance.getId()); 
							}
							result += count;
							result += (i < statuses.size()-1) ? "," : "\n";
						}
					}
				}
			}
		}
		return result;
	}
	
	protected List<Attendance> filterAttendances(List<Attendance> attendances) {
		if (Utils.isEmpty(attendances) || everyAttendance) return attendances;
		List<Attendance> filtered = new ArrayList<Attendance>();
		for (Attendance attendance : attendances) {
			if (isBetweenDates(attendance.getDate(), from.getTimeInMillis(), to.getTimeInMillis())) {
				filtered.add(attendance);
			}
		}
		return filtered;
	}
	
	class EmailReportTask extends AsyncTask<Void, Void, Boolean> {
		
		public static final int TYPE_FULL = 0;
		public static final int TYPE_SUMMARY = 1;
		
		protected ProgressDialog dialog;
		protected String error;
		protected List<String> filePaths;
		protected int type;
		protected Course course;
		
		public EmailReportTask(int type, Course course) {
			this.type = type;
			this.course = course;
			filePaths = new LinkedList<String>();
			dialog = new ProgressDialog(ReportScreen.this);
    		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		dialog.setMessage(getString(R.string.please_wait));
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
				File tempFolder = new File(Environment.getExternalStorageDirectory()+"/easyattendance/temp");
				if (tempFolder == null || !tempFolder.exists())
					tempFolder.mkdirs();
				
				if (course != null) { // import selected course
//					List<Attendance> attendances = filterAttendances(dbStorage.getAttendancesByCourseId(course.getId()));
//					if (!Utils.isEmpty(attendances)) {
						String courseCsv = (type == TYPE_FULL) ? getCourseFullCsv(course) : getCourseSummaryCsv(course);
						File courseFile = new File(tempFolder, course.getName() + " - " + getTypeName() + ".csv");
						FileOutputStream fos = new FileOutputStream(courseFile);
						fos.write(courseCsv.getBytes());
						fos.flush();
						filePaths.add(courseFile.getAbsolutePath());
						fos.close();
//					} else {
//						error = getString(R.string.nothing_to_export);
//						return false;
//					}
				} else { // import all courses
//					boolean nothing = true;
					for (Course course : courses) {
//						List<Attendance> attendances = filterAttendances(dbStorage.getAttendancesByCourseId(course.getId()));
//						if (!Utils.isEmpty(attendances)) {
//							nothing = false;
							String courseCsv = (type == TYPE_FULL) ? getCourseFullCsv(course) : getCourseSummaryCsv(course);
							File courseFile = new File(tempFolder, course.getName() + " - " + getTypeName() + ".csv");
							FileOutputStream fos = new FileOutputStream(courseFile);
							fos.write(courseCsv.getBytes());
							fos.flush();
							filePaths.add(courseFile.getAbsolutePath());
							fos.close();
//						}
					}
//					if (nothing) {
//						error = getString(R.string.nothing_to_export);
//						return false;
//					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				error = getString(R.string.upload_error);
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			if (!result.booleanValue()) {
				showErrorDialog(error);
			} else {
				sendEmailIntent();
			}
		}
		
		protected void sendEmailIntent() {
			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("message/rfc822");
			String subject = getExtraSubject();
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
			ArrayList<Uri> uris = new ArrayList<Uri>();
			for (String filePath : filePaths) {
				File file = new File(filePath);
				Uri uri = Uri.fromFile(file);
				uris.add(uri);
			}
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
		}
		
		protected String getExtraSubject() {
			String courseName = (course != null) ? course.getName() : getString(R.string.courses);
			return courseName + " " + getTypeName() + " " + getString(R.string.report);
		}
		
		protected String getTypeName() {
			return (type == TYPE_FULL) ? getString(R.string.full) : getString(R.string.summary);
		}
		
	}
	
	class DropboxUploadTask extends AsyncTask<Void, Long, Boolean> {
		
		public static final int TYPE_FULL = 0;
		public static final int TYPE_SUMMARY = 1;
		
		protected ProgressDialog dialog;
		protected String error;
		protected int type;
		protected Course course;
		protected String currentName;
		
		public DropboxUploadTask(int type, Course course) {
			this.type = type;
			this.course = course;
			dialog = new ProgressDialog(ReportScreen.this);
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
				
				if (course != null) {
					String courseCsv = (type == TYPE_FULL) ? getCourseFullCsv(course) : getCourseSummaryCsv(course);
					currentName = course.getName() + " - " + getTypeName() + ".csv";
					String filePath = rootFolder + "/" + currentName;
					ByteArrayInputStream bais = new ByteArrayInputStream(courseCsv.getBytes());
					try {
						mApi.putFileOverwrite(filePath, bais, courseCsv.getBytes().length, new ProgressListener() {
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
				} else {
					for (Course course : courses) {
						String courseCsv = (type == TYPE_FULL) ? getCourseFullCsv(course) : getCourseSummaryCsv(course);
						currentName = course.getName() + " - " + getTypeName() + ".csv";
						String filePath = rootFolder + "/" + currentName;
						ByteArrayInputStream bais = new ByteArrayInputStream(courseCsv.getBytes());
						try {
							mApi.putFileOverwrite(filePath, bais, courseCsv.getBytes().length, new ProgressListener() {
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
					}
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
				Toast.makeText(ReportScreen.this, error, Toast.LENGTH_SHORT).show();
			} else {
				showInfoDialog(R.string.success, R.string.csv_uploaded);
			}
		}
		
		protected String getTypeName() {
			return (type == TYPE_FULL) ? getString(R.string.full) : getString(R.string.summary);
		}
		
	}

}
