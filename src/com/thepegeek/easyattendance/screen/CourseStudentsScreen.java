package com.thepegeek.easyattendance.screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.adapter.StudentAdapter;
import com.thepegeek.easyattendance.model.Attendance;
import com.thepegeek.easyattendance.model.Course;
import com.thepegeek.easyattendance.model.EntryRecord;
import com.thepegeek.easyattendance.model.Label;
import com.thepegeek.easyattendance.model.Student;
import com.thepegeek.easyattendance.storage.DatabaseHelper;
import com.thepegeek.easyattendance.storage.EntryHolder;
import com.thepegeek.easyattendance.util.Utils;

public class CourseStudentsScreen extends BaseScreen implements OnClickListener, OnItemClickListener {
	
	public static final int DELETE_MENU_ITEM = 0;
	public static final int DOWNLOAD_FROM_DROPBOX_REQUEST_CODE = 1001;
	
	protected Button addStudentBtn;
	protected Button importBtn;
	protected Button sortBtn;
	protected ListView studentsList;
	protected TextView empty;
	
	protected long courseId;
	protected Course course;
	protected List<Student> students;
	protected StudentAdapter adapter;
	protected boolean ascending;
	
	protected boolean loggedIn;
	protected boolean showDropboxDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_students_screen);
		getIntentData();
		initializeViews();
		
		adapter = new StudentAdapter(this, students);
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
                
                if (showDropboxDialog) {
                	showDropboxDialog = false;
                	showMyDropboxDialog();
                }
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
	}
	
	protected void updateStudents() {
		students = dbStorage.getStudentsByCourseId(courseId);
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
		if (intent != null && intent.hasExtra(DatabaseHelper.FIELD_COURSE_ID)) {
			courseId = intent.getLongExtra(DatabaseHelper.FIELD_COURSE_ID, 0);
			course = dbStorage.getCourseById(courseId);
			setScreenTitle(course.getName());
		}
	}
	
	protected void initializeViews() {
		addStudentBtn = (Button) findViewById(R.id.addStudentBtn);
		addStudentBtn.setOnClickListener(this);
		
		importBtn = (Button) findViewById(R.id.importBtn);
		importBtn.setOnClickListener(this);
		
		sortBtn = (Button) findViewById(R.id.sortBtn);
		sortBtn.setOnClickListener(this);
		
		studentsList = (ListView) findViewById(R.id.studentsList);
		studentsList.setOnItemClickListener(this);
		registerForContextMenu(studentsList);
		
		empty = (TextView) findViewById(R.id.empty);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Student student = students.get(info.position);
		menu.setHeaderTitle(student.getFullname());
		menu.add(0, DELETE_MENU_ITEM, 0, R.string.delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Student student = students.get(info.position);
		switch (item.getItemId()) {
		case DELETE_MENU_ITEM:
			dbStorage.deleteStudentFromCourse(student, courseId);
			dbStorage.deleteStudentFromAttendances(student);
			updateStudents();
			break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addStudentBtn:
			showAddStudentDialog();
			break;
		case R.id.importBtn:
			showSelectImportMethodDialog();
			break;
		case R.id.sortBtn:
			sort();
			break;
		}
	}
	
	protected void sort() {
		if (!Utils.isEmpty(students)) {
			Collections.sort(students, new Comparator<Student>() {
				@Override
				public int compare(Student lhs, Student rhs) {
					int result = lhs.getFullname().compareToIgnoreCase(rhs.getFullname());  
					return (result == 0) ? result :
						ascending ? result : -result;
				}
				
			});
			adapter.notifyDataSetChanged();
		}
		ascending = !ascending;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Student student = students.get(position);
		Intent intent = new Intent(this, StudentDetailsScreen.class);
		intent.putExtra(DatabaseHelper.FIELD_STUDENT_ID, student.getId());
		startActivity(intent);
	}
	
	protected void showAddStudentDialog() {
		final EditText studentName = new EditText(this);
		studentName.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		studentName.setHint(R.string.firstname_lastname);
		studentName.setSingleLine();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.add_student_text)
			.setView(studentName)
			.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String names = studentName.getText().toString().trim();
					if (!TextUtils.isEmpty(names)) {
						processStudentNames(names);
					} else {
						showErrorDialog(R.string.student_name_empty);
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
	
	protected void processStudentNames(String names) {
		StringTokenizer st = new StringTokenizer(names, ",");
		while (st.hasMoreTokens()) {
			String name = st.nextToken().trim();
			if (!TextUtils.isEmpty(name)) {
				String firstname = name.contains(" ") ? name.substring(0, name.indexOf(" ")) : name;
				String lastname = name.contains(" ") ? name.substring(name.indexOf(" ")+1) : null;
				long studentId = dbStorage.addStudent(new Student(firstname, lastname));
				dbStorage.addStudentToCourse(studentId, courseId);
				List<Attendance> attendances = dbStorage.getAttendancesByCourseId(courseId, false);
				if (!Utils.isEmpty(attendances)) {
					for (Attendance attendance : attendances) {
						dbStorage.addStudentToAttendance(studentId, attendance.getId());
					}
				}
			}
		}
		updateStudents();
	}
	
	protected void showSelectImportMethodDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.import_students)
			.setItems(R.array.import_student_options, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						showSelectCourseDialog();
						break;
					case 1:
						addFromCsv();
						break;
					case 2:
						// TODO show example csv
						break;
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
	
	protected void showMyDropboxDialog() {
		Intent intent = new Intent(this, DropboxScreen.class);
		startActivityForResult(intent, DOWNLOAD_FROM_DROPBOX_REQUEST_CODE);
	}
	
	protected void addFromCsv() {
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = conManager.getActiveNetworkInfo();
		if (info == null || info.getState() != NetworkInfo.State.CONNECTED) {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
		} else {
			if (loggedIn) {
				showMyDropboxDialog();
            } else {
            	showDropboxDialog = true;
                mApi.getSession().startAuthentication(this);
            }
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DOWNLOAD_FROM_DROPBOX_REQUEST_CODE && resultCode == RESULT_OK) {
			new ImportItemsTask().execute((Void[] ) null);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	protected void showSelectCourseDialog() {
		final List<Course> courses = dbStorage.getCourses();
		for (Course course : courses) {
			if (course.getId() == courseId) {
				courses.remove(course);
				break;
			}
		}
		if (!Utils.isEmpty(courses)) {
			CharSequence[] courseNames = new CharSequence[courses.size()];
			for (int i=0; i<courseNames.length; i++) {
				courseNames[i] = courses.get(i).getName();
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.select_course)
					.setItems(courseNames, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							long courseId = courses.get(which).getId();
							showSelectStudentsDialog(courseId);
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
		} else {
			showErrorDialog(R.string.only_one_course);
		}
	}
	
	protected void showSelectStudentsDialog(long courseId) {
		final List<Student> students2 = dbStorage.getStudentsByCourseId(courseId);
		if (!Utils.isEmpty(students2)) {
			if (!Utils.isEmpty(students)) {
				for (Student student : students) {
					Iterator<Student> i = students2.iterator();
					while (i.hasNext()) {
						Student student2 = i.next();
						if (student2.getId() == student.getId()) {
							i.remove();
						}
					}
				}
			}
			if (!Utils.isEmpty(students2)) {
				CharSequence[] studentNames = new CharSequence[students2.size()];
				for (int i=0; i<studentNames.length; i++) {
					studentNames[i] = students2.get(i).getFullname();
				}
				final boolean[] checked = new boolean[studentNames.length];
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.select_students)
					.setMultiChoiceItems(studentNames, null, new DialogInterface.OnMultiChoiceClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							checked[which] = isChecked;
						}
					})
					.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							for (int i=0; i<checked.length; i++) {
								if (checked[i]) {
									dbStorage.addStudentToCourse(students2.get(i).getId(), CourseStudentsScreen.this.courseId);
									List<Attendance> attendances = dbStorage.getAttendancesByCourseId(CourseStudentsScreen.this.courseId, false);
									if (!Utils.isEmpty(attendances)) {
										for (Attendance attendance : attendances) {
											dbStorage.addStudentToAttendance(students2.get(i).getId(), attendance.getId());
										}
									}
								}
							}
							dialog.dismiss();
							updateStudents();
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
			} else {
				showErrorDialog(R.string.all_students_present);
			}
 		} else {
			showErrorDialog(R.string.empty_students_min);
		}
	}
	
	class ImportItemsTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressDialog(getString(R.string.importing));
		}

		@Override
		protected Void doInBackground(Void... params) {
			List<EntryRecord> records = EntryHolder.getInstance(mApi).getDownloadedEntries();
			if (records != null && !records.isEmpty()) {
				for (EntryRecord record : records) {
					try {
						File f = new File(record.getDownloadedPath());
						if (f != null && f.exists()) {
							InputStream is = new FileInputStream(f);
							Reader r = new InputStreamReader(is);
							BufferedReader br = new BufferedReader(r);
							String name;
							boolean firstLine = true;
							List<Label> labels = null;
							while ((name = br.readLine()) != null) {
								if (firstLine) {
									firstLine = false;
									if (!TextUtils.isEmpty(name)) {
										int comma = name.indexOf(',');
										if (comma != -1) {
											String names = name.substring(comma+1);
											labels = processLabels(names);
										}
									}									
									continue;
								} else {
									if (!TextUtils.isEmpty(name)) {
										int comma = name.indexOf(',');
										if (comma == -1) {
											addStudent(name);
										} else {
											String studentName = name.substring(0, comma);
											long studentId = addStudent(studentName);
											String values = name.substring(comma+1);
											int i = 0;
											StringTokenizer st = new StringTokenizer(values, ",");
											if (st.hasMoreTokens()) {
												String value = st.nextToken();
												if (!Utils.isEmpty(labels)) {
													Label label = labels.get(i++);
													label.setValue(value);
													dbStorage.addLabelToStudent(label, studentId);
												}
											}
										}
									}
								}
							}
							is.close();
							r.close();
							br.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}
		
		protected long addStudent(String name) {
			int space = name.indexOf(' ');
			String firstname = null, lastname = null;
			if (space == -1) {
				firstname = name;
			} else {
				firstname = name.substring(0, space);
				lastname = name.substring(space+1);
			}
			Student student = new Student(firstname, lastname);
			long id = dbStorage.addStudent(student);
			if (id != 0) {
				dbStorage.addStudentToCourse(id, courseId);
			}
			return id;
		}
		
		protected List<Label> processLabels(String name) {
			List<Label> labels = null;
			if (!TextUtils.isEmpty(name)) {
				StringTokenizer st = new StringTokenizer(name, ",");
				while (st.hasMoreTokens()) {
					String labelName = st.nextToken();
					if (!TextUtils.isEmpty(labelName)) {
						long id = dbStorage.addLabel(new Label(0, name));
						Label label = dbStorage.getLabelById(id);
						if (labels == null) {
							labels = new ArrayList<Label>();
						}
						labels.add(label);
					}
				}
			}
			return labels;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			EntryHolder entryHolder = EntryHolder.getInstance(mApi);
			entryHolder.setCurrent(entryHolder.getRoot());
			entryHolder.uncheckEntries(entryHolder.getRoot());
			hideProgressDialog();
			updateStudents();
		}
		
	}

}
