package com.thepegeek.easyattendance.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.thepegeek.easyattendance.model.Attendance;
import com.thepegeek.easyattendance.model.Course;
import com.thepegeek.easyattendance.model.Group;
import com.thepegeek.easyattendance.model.Label;
import com.thepegeek.easyattendance.model.Observation;
import com.thepegeek.easyattendance.model.Status;
import com.thepegeek.easyattendance.model.Student;
import com.thepegeek.easyattendance.util.Utils;

public class DatabaseStorage {
	
	public static final String TAG = DatabaseStorage.class.getSimpleName();
	
	protected static DatabaseStorage instance;
	protected Context context;
	protected DatabaseHelper dbHelper;
	protected SQLiteDatabase db;
	
	protected DatabaseStorage(Context context) {
		this.context = context;
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	
	public static DatabaseStorage getInstance(Context context) {
		if (instance == null)
			instance = new DatabaseStorage(context);
		return instance;
	}
	
	/*******************************************************************************************************************
	 ************************************************** C O U R S E S **************************************************
	 *******************************************************************************************************************/
	
	public List<Course> getCourses() {
		List<Course> courses = new ArrayList<Course>();
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_COURSES, null, null, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					long id = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)); 
					long studentsCount = getStudentsCount(id);
					long attendancesCount = getAttendancesCount(id);
					Course course = new Course(
						id,
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME)),
						studentsCount,
						attendancesCount
					);
					courses.add(course);
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return courses;
	}
	
	public Course getCourseById(long id) {
		Course course = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_COURSES, null, DatabaseHelper.FIELD_ID+"="+id, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				long studentsCount = getStudentsCount(id);
				long attendancesCount = getAttendancesCount(id);
				course = new Course(
					id,
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME)),
					studentsCount,
					attendancesCount
				);
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return course;
	}
	
	public long addCourse(Course course) {
		if (course == null) return 0;
		ContentValues values = prepareCourseValues(course);
		return db.insert(DatabaseHelper.TABLE_COURSES, null, values);
	}
	
	public int updateCourse(Course course) {
		if (course == null) return 0;
		ContentValues values = prepareCourseValues(course);
		return db.update(DatabaseHelper.TABLE_COURSES, values, DatabaseHelper.FIELD_ID+"="+course.getId(), null);
	}
	
	protected ContentValues prepareCourseValues(Course course) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_NAME, course.getName());
		return values;
	}
	
	public int deleteCourse(Course course) {
		if (course == null) return 0;
		return db.delete(DatabaseHelper.TABLE_COURSES, DatabaseHelper.FIELD_ID+"="+course.getId(), null);
	}
	
	/*******************************************************************************************************************
	 *************************************** S T U D E N T S   2   C O U R S E S ***************************************
	 *******************************************************************************************************************/
	
	public long getStudentsCount(long courseId) {
		long studentsCount = 0;
		try {
			String query = "select count(*) from " + DatabaseHelper.TABLE_STUDENTS_2_COURSES + 
					" where " + DatabaseHelper.FIELD_COURSE_ID + "=" + courseId;
			Cursor c = db.rawQuery(query, null);
			if (c != null && c.moveToFirst()) {
				studentsCount = c.getLong(0);
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return studentsCount;
	}
	
	public long[] getStudentsIDsByCourseId(long courseId) {
		long[] ids = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_STUDENTS_2_COURSES, null, DatabaseHelper.FIELD_COURSE_ID+"="+courseId, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				ids = new long[c.getCount()];
				int i = 0;
				do {
					ids[i++] = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_STUDENT_ID));
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ids;
	}
	
	public long addStudentToCourse(long studentId, long courseId) {
		if (studentId == 0 || courseId == 0) return 0;
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_STUDENT_ID, studentId);
		values.put(DatabaseHelper.FIELD_COURSE_ID, courseId);
		return db.insert(DatabaseHelper.TABLE_STUDENTS_2_COURSES, null, values);
	}
	
	public int deleteStudentFromCourses(long studentId) {
		if (studentId == 0) return 0;
		return db.delete(DatabaseHelper.TABLE_STUDENTS_2_COURSES, DatabaseHelper.FIELD_STUDENT_ID+"="+studentId, null);				
	}
	
	public int deleteStudentFromCourse(Student student, long courseId) {
		if (student == null || courseId == 0) return 0;
		String whereClause = DatabaseHelper.FIELD_STUDENT_ID+"="+student.getId()+" and "+DatabaseHelper.FIELD_COURSE_ID+"="+courseId;
		return db.delete(DatabaseHelper.TABLE_STUDENTS_2_COURSES, whereClause, null);
	}
	
	/*******************************************************************************************************************
	 ************************************************* S T U D E N T S *************************************************
	 *******************************************************************************************************************/
	
	public List<Student> getStudentsByCourseId(long courseId) {
		long[] ids = getStudentsIDsByCourseId(courseId);
		List<Student> students = new ArrayList<Student>();
		if (ids == null || ids.length == 0) return students;
		try {
			String sqlRange = Utils.arrayToSQLRange(ids);
			Cursor c = db.query(DatabaseHelper.TABLE_STUDENTS, null, DatabaseHelper.FIELD_ID+" in "+sqlRange, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					Student student = new Student(
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_FIRSTNAME)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_LASTNAME)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_PHOTO))
					);
					students.add(student);
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return students;
	}
	
	public Student getStudentById(long id) {
		Student student = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_STUDENTS, null, DatabaseHelper.FIELD_ID+"="+id, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				student = new Student(
					id,
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_FIRSTNAME)),
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_LASTNAME)),
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_PHOTO))
				);
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return student;
	}
	
	public long addStudent(Student student) {
		if (student == null) return 0;
		ContentValues values = prepareStudentValues(student);
		return db.insert(DatabaseHelper.TABLE_STUDENTS, null, values);
	}
	
	public int updateStudent(Student student) {
		if (student == null) return 0;
		ContentValues values = prepareStudentValues(student);
		return db.update(DatabaseHelper.TABLE_STUDENTS, values, DatabaseHelper.FIELD_ID+"="+student.getId(), null);
	}
	
	protected ContentValues prepareStudentValues(Student student) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_FIRSTNAME, student.getFirstname());
		values.put(DatabaseHelper.FIELD_LASTNAME, student.getLastname());
		values.put(DatabaseHelper.FIELD_PHOTO, student.getPhoto());
		return values;
	}
	
	public int deleteStudent(Student student) {
		if (student == null) return 0;
		return db.delete(DatabaseHelper.TABLE_STUDENTS, DatabaseHelper.FIELD_ID+"="+student.getId(), null);
	}
	
	/*******************************************************************************************************************
	 ************************************************* S T A T U S E S *************************************************
	 *******************************************************************************************************************/
	
	public List<Status> getStatuses() {
		List<Status> statuses = new ArrayList<Status>();
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_STATUSES, null, null, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					Status status = new Status(
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME)),
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_ABSENT)) == 1,
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_COMMON)) == 1,
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_DEFAULT)) == 1,
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_COLOR))
					);
					statuses.add(status);
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statuses;
	}
	
	public Status getStatusById(long statusId) {
		Status status = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_STATUSES, null, DatabaseHelper.FIELD_ID+"="+statusId, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				status = new Status(
					statusId,
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME)),
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_ABSENT)) == 1,
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_COMMON)) == 1,
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_DEFAULT)) == 1,
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_COLOR))
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}
	
	public List<Status> getCommonStatuses() {
		List<Status> statuses = new ArrayList<Status>();
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_STATUSES, null, DatabaseHelper.FIELD_COMMON+"=1", null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					Status status = new Status(
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME)),
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_ABSENT)) == 1,
						true,
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_DEFAULT)) == 1,
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_COLOR))
					);
					statuses.add(status);
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statuses;
	}
	
	public List<Status> getUserStatuses() {
		List<Status> statuses = new ArrayList<Status>();
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_STATUSES, null, DatabaseHelper.FIELD_COMMON+"=0", null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					Status status = new Status(
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME)),
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_ABSENT)) == 1,
						false,
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_DEFAULT)) == 1,
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_COLOR))
					);
					statuses.add(status);
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statuses;
	}
	
	public Status getDefaultStatus() {
		Status status = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_STATUSES, null, DatabaseHelper.FIELD_DEFAULT+"="+1, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				status = new Status(
					c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)),
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME)),
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_ABSENT)) == 1,
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_COMMON)) == 1,
					true,
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_COLOR))
				);
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}
	
	public long addStatus(Status status) {
		if (status == null) return 0;
		ContentValues values = prepareStatusValues(status);
		return db.insert(DatabaseHelper.TABLE_STATUSES, null, values);
	}
	
	public int updateStatus(Status status) {
		if (status == null) return 0;
		ContentValues values = prepareStatusValues(status);
		return db.update(DatabaseHelper.TABLE_STATUSES, values, DatabaseHelper.FIELD_ID+"="+status.getId(), null);
	}
	
	protected ContentValues prepareStatusValues(Status status) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_NAME, status.getName());
		values.put(DatabaseHelper.FIELD_ABSENT, status.isAbsent() ? 1 : 0);
		values.put(DatabaseHelper.FIELD_COMMON, status.isCommon() ? 1 : 0);
		values.put(DatabaseHelper.FIELD_DEFAULT, status.isDefault() ? 1 : 0);
		values.put(DatabaseHelper.FIELD_COLOR, status.getColor());
		return values;
	}
	
	public int deleteStatus(Status status) {
		if (status == null) return 0;
		return db.delete(DatabaseHelper.TABLE_STATUSES, DatabaseHelper.FIELD_ID+"="+status.getId(), null);
	}
	
	public void setDefaultStatus(Status status) {
		if (status == null) return;
		
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_DEFAULT, 0);
		db.update(DatabaseHelper.TABLE_STATUSES, values, null, null);
		
		values.put(DatabaseHelper.FIELD_DEFAULT, 1);
		db.update(DatabaseHelper.TABLE_STATUSES, values, DatabaseHelper.FIELD_ID+"="+status.getId(), null);
	}
	
	public boolean statusCanBeDeleted(long statusId) {
		if (statusId == 0) return false;
		boolean canBeDeleted = false;
		try {
			String query = "select count(*) from " + DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE + 
					" where " + DatabaseHelper.FIELD_STATUS_ID + "=" + statusId;
			Cursor c = db.rawQuery(query, null);
			if (c != null && c.moveToFirst()) {
				canBeDeleted = (c.getLong(0) == 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return canBeDeleted;
	}
	
	/*******************************************************************************************************************
	 *************************************************** L A B E L S ***************************************************
	 *******************************************************************************************************************/
	
	public List<Label> getLabels() {
		List<Label> labels = new ArrayList<Label>();
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_LABELS, null, null, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					Label label = new Label(
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME))
					);
					labels.add(label);
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return labels;
	}
	
	public Label getLabelById(long labelId) {
		Label label = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_LABELS, null, DatabaseHelper.FIELD_ID+"="+labelId, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				label = new Label(
					labelId,
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME))
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return label;
	}
	
	public long addLabel(Label label) {
		if (label == null) return 0;
		ContentValues values = prepareLabelValues(label);
		return db.insert(DatabaseHelper.TABLE_LABELS, null, values);
	}
	
	protected ContentValues prepareLabelValues(Label label) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_NAME, label.getName());
		return values;
	}
	
	public int deleteLabel(Label label) {
		if (label == null) return 0;
		return db.delete(DatabaseHelper.TABLE_LABELS, DatabaseHelper.FIELD_ID+"="+label.getId(), null);
	}
	
	/*******************************************************************************************************************
	 ***************************************** L A B E L S   2   S T U D E N T S ***************************************
	 *******************************************************************************************************************/
	
	public List<Label> getLabelsByStudentId(long studentId) {
		List<Label> labels = new ArrayList<Label>();
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_LABELS_2_STUDENTS, null, 
				DatabaseHelper.FIELD_STUDENT_ID+"="+studentId, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					Label label = new Label(
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)),
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_LABEL_ID)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_LABEL_NAME)),
						studentId,
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_VALUE))
					);
					labels.add(label);
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return labels;
	}
	
	public long addLabelToStudent(Label label, long studentId) {
		if (label == null || studentId == 0) return 0;
		ContentValues values = prepareStudentLabelsValues(label, studentId);
		return db.insert(DatabaseHelper.TABLE_LABELS_2_STUDENTS, null, values);
	}
	
	public int updateLabelForStudent(Label label, long studentId) {
		if (label == null || studentId == 0) return 0;
		ContentValues values = prepareStudentLabelsValues(label, studentId);
		String whereClause = DatabaseHelper.FIELD_STUDENT_ID + "=" + studentId + " and " + DatabaseHelper.FIELD_LABEL_ID + "=" + label.getLabelId();
		return db.update(DatabaseHelper.TABLE_LABELS_2_STUDENTS, values, whereClause, null);
	}
	
	public int updateLabelsForStudent(List<Label> labels, long studentId) {
		if (Utils.isEmpty(labels) || studentId == 0) return 0;
		int rowsAffected = 0;
		for (Label label : labels) {
			rowsAffected += updateLabelForStudent(label, studentId);
		}
		return rowsAffected;
	}
	
	protected ContentValues prepareStudentLabelsValues(Label label, long studentId) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_STUDENT_ID, studentId);
		values.put(DatabaseHelper.FIELD_LABEL_ID, label.getId());
		values.put(DatabaseHelper.FIELD_LABEL_NAME, label.getName());
		values.put(DatabaseHelper.FIELD_VALUE, label.getValue());
		return values;
	}
	
	public int deleteLabelFromStudent(Label label, long studentId) {
		if (label == null || studentId == 0) return 0;
		String whereClause = DatabaseHelper.FIELD_STUDENT_ID + "=" + studentId + " and " + DatabaseHelper.FIELD_LABEL_ID + "=" + label.getLabelId();
		return db.delete(DatabaseHelper.TABLE_LABELS_2_STUDENTS, whereClause, null);
	}
	
	public int deleteLabelsFromStudent(List<Label> labels, long studentId) {
		if (Utils.isEmpty(labels) || studentId == 0) return 0;
		int rowsAffected = 0;
		for (Label label : labels) {
			rowsAffected += deleteLabelFromStudent(label, studentId);
		}
		return rowsAffected;
	}
	
	/*******************************************************************************************************************
	 ********************************************** O B S E R V A T I O N S ********************************************
	 *******************************************************************************************************************/
	
	public List<Observation> getObservationsByStudentId(long studentId) {
		List<Observation> observations = new ArrayList<Observation>();
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_OBSERVATIONS, null, DatabaseHelper.FIELD_STUDENT_ID+"="+studentId, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					observations.add(new Observation(
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)),
						studentId,
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_DATE)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NOTE))
					));
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return observations;
	}
	
	public Observation getObservationById(long id) {
		Observation observation = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_OBSERVATIONS, null, DatabaseHelper.FIELD_ID+"="+id, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				observation = new Observation(
					id,
					c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_STUDENT_ID)),
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_DATE)),
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NOTE))
				);
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return observation;
	}
	
	public long getObservationsCountByStudentId(long studentId) {
		long observationsCount = 0;
		try {
			String query = "select count(*) from " + DatabaseHelper.TABLE_OBSERVATIONS + 
					" where " + DatabaseHelper.FIELD_STUDENT_ID + "=" + studentId;
			Cursor c = db.rawQuery(query, null);
			if (c != null && c.moveToFirst()) {
				observationsCount = c.getLong(0);
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return observationsCount;
	}
	
	public long addObservation(Observation observation) {
		if (observation == null) return 0;
		ContentValues values = prepareObservationValues(observation);
		return db.insert(DatabaseHelper.TABLE_OBSERVATIONS, null, values);
	}
	
	public int updateObservation(Observation observation) {
		if (observation == null) return 0;
		ContentValues values = prepareObservationValues(observation);
		return db.update(DatabaseHelper.TABLE_OBSERVATIONS, values, DatabaseHelper.FIELD_ID+"="+observation.getId(), null);
	}
	
	protected ContentValues prepareObservationValues(Observation observation) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_STUDENT_ID, observation.getStudentId());
		values.put(DatabaseHelper.FIELD_DATE, observation.getDate());
		values.put(DatabaseHelper.FIELD_NOTE, observation.getNote());
		return values;
	}
	
	public int deleteObservation(Observation observation) {
		if (observation == null) return 0;
		return db.delete(DatabaseHelper.TABLE_OBSERVATIONS, DatabaseHelper.FIELD_ID+"="+observation.getId(), null);
	}
	
	/*******************************************************************************************************************
	 *********************************************** A T T E N D A N C E S *********************************************
	 *******************************************************************************************************************/
	
	public long getAttendancesCount(long courseId) {
		long studentsCount = 0;
		try {
			String query = "select count(*) from " + DatabaseHelper.TABLE_ATTENDANCES + 
					" where " + DatabaseHelper.FIELD_COURSE_ID + "=" + courseId;
			Cursor c = db.rawQuery(query, null);
			if (c != null && c.moveToFirst()) {
				studentsCount = c.getLong(0);
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return studentsCount;
	}
	
	public List<Attendance> getAttendancesByCourseId(long courseId, boolean addStudents) {
		List<Attendance> attendances = new ArrayList<Attendance>();
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_ATTENDANCES, null, DatabaseHelper.FIELD_COURSE_ID+"="+courseId, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					long id = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID));
					Attendance attendance = new Attendance(
						id,
						courseId,
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_DATE)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NOTE)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_PHOTO))
					);
					if (addStudents) {
						attendance.setStudents(getStudentsForAttendance(id));
					}
					attendances.add(attendance);
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attendances;
	}
	
	public Attendance getAttendanceById(long id) {
		Attendance attendance = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_ATTENDANCES, null, DatabaseHelper.FIELD_ID+"="+id, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				attendance = new Attendance(
					id,
					c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_COURSE_ID)),
					c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_DATE)),
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NOTE)),
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_PHOTO))
				);
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attendance;
	}
	
	public long addAttendance(Attendance attendance) {
		if (attendance == null) return 0;
		ContentValues values = prepareAttendanceValues(attendance);
		return db.insert(DatabaseHelper.TABLE_ATTENDANCES, null, values);
	}
	
	public int updateAttendance(Attendance attendance) {
		if (attendance == null) return 0;
		ContentValues values = prepareAttendanceValues(attendance);
		return db.update(DatabaseHelper.TABLE_ATTENDANCES, values, DatabaseHelper.FIELD_ID+"="+attendance.getId(), null);
	}
	
	protected ContentValues prepareAttendanceValues(Attendance attendance) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_COURSE_ID, attendance.getCourseId());
		values.put(DatabaseHelper.FIELD_DATE, attendance.getDate());
		values.put(DatabaseHelper.FIELD_NOTE, attendance.getNote());
		values.put(DatabaseHelper.FIELD_PHOTO, attendance.getPhoto());
		return values;
	}
	
	public int deleteAttendance(Attendance attendance) {
		if (attendance == null) return 0;
		return db.delete(DatabaseHelper.TABLE_ATTENDANCES, DatabaseHelper.FIELD_ID+"="+attendance.getId(), null);
	}
	
	/*******************************************************************************************************************
	 ************************************ S T U D E N T S   2   A T T E N D A N C E S **********************************
	 *******************************************************************************************************************/
	
	public List<Student> getStudentsForAttendance(long attendanceId) {
		List<Student> students = new ArrayList<Student>();
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, null, 
					DatabaseHelper.FIELD_ATTENDANCE_ID+"="+attendanceId, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					long studentId = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_STUDENT_ID));
					Student student = getStudentById(studentId);
					if (student != null) {
						long statusId = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_STATUS_ID));
						Status status = getStatusById(statusId);
						student.setStatus(status);
						students.add(student);
					}
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return students;
	}
	
	public Student getStudentForAttendance(long studentId, long attendanceId) {
		Student student = null;
		try {
			String selection = DatabaseHelper.FIELD_STUDENT_ID+"="+studentId+" and "+DatabaseHelper.FIELD_ATTENDANCE_ID+"="+attendanceId;
			Cursor c = db.query(DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, null, selection, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				student = getStudentById(studentId);
				if (student != null) {
					long statusId = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_STATUS_ID));
					Status status = getStatusById(statusId);
					student.setStatus(status);
				}
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return student;
	}
	
	public int getStatusCountForStudent(long studentId, long statusId, long attendnaceId) {
		int count = 0;
		try {
			String selection = DatabaseHelper.FIELD_STUDENT_ID+"="+studentId+" and "+DatabaseHelper.FIELD_STATUS_ID+"="+statusId+" and "+DatabaseHelper.FIELD_ATTENDANCE_ID+"="+attendnaceId;
			Cursor c = db.query(DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, null, selection, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				count = 1;
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	
	public void addStudentsToAttendance(Attendance attendance) {
		if (attendance == null) return;
		Status status = getDefaultStatus();
		List<Student> students = getStudentsByCourseId(attendance.getCourseId());
		if (!Utils.isEmpty(students) && status != null) {
			for (Student student : students) {
				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.FIELD_ATTENDANCE_ID, attendance.getId());
				values.put(DatabaseHelper.FIELD_STUDENT_ID, student.getId());
				values.put(DatabaseHelper.FIELD_STATUS_ID, status.getId());
				db.insert(DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, null, values);
			}
		}
	}
	
	public void addStudentToAttendance(long studentId, long attendanceId) {
		if (studentId == 0 || attendanceId == 0) return;
		Status status = getDefaultStatus();
		if (status != null) {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FIELD_ATTENDANCE_ID, attendanceId);
			values.put(DatabaseHelper.FIELD_STUDENT_ID, studentId);
			values.put(DatabaseHelper.FIELD_STATUS_ID, status.getId());
			db.insert(DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, null, values);
		}
	}
	
	public int updateAttendanceStudent(long attendanceId, long studentId, long statusId) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_ATTENDANCE_ID, attendanceId);
		values.put(DatabaseHelper.FIELD_STUDENT_ID, studentId);
		values.put(DatabaseHelper.FIELD_STATUS_ID, statusId);
		String whereClause = DatabaseHelper.FIELD_ATTENDANCE_ID+"="+attendanceId+" and "+DatabaseHelper.FIELD_STUDENT_ID+"="+studentId;
		return db.update(DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, values, whereClause, null);
	}
	
	public void replaceWithDefaultStatus(long statusId) {
		Status defaultStatus = getDefaultStatus();
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_STATUS_ID, defaultStatus.getId());
		db.update(DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, values, DatabaseHelper.FIELD_STATUS_ID+"="+statusId, null);
	}
	
	public int deleteStudentsFromAttendance(long attendanceId) {
		if (attendanceId == 0) return 0;
		return db.delete(DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, DatabaseHelper.FIELD_ATTENDANCE_ID+"="+attendanceId, null);
	}
	
	public int deleteStudentFromAttendances(Student student) {
		if (student == null) return 0;
		return db.delete(DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, DatabaseHelper.FIELD_STUDENT_ID+"="+student.getId(), null);
	}
	
	/*******************************************************************************************************************
	 **************************************************** G R O U P S **************************************************
	 *******************************************************************************************************************/
	
	public List<Group> getGroups() {
		List<Group> groups = new ArrayList<Group>();
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_GROUPS, null, null, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				do {
					groups.add(new Group(
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)),
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_COUNT))
					));
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return groups;
	}
	
	public Group getGroupById(long groupId) {
		Group group = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_GROUPS, null, DatabaseHelper.FIELD_ID+"="+groupId, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				group = new Group(
					groupId,
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_COUNT))
				);
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return group;
	}
	
	public long addGroup(Group group) {
		if (group == null) return 0;
		ContentValues values = prepareGroupValues(group);
		return db.insert(DatabaseHelper.TABLE_GROUPS, null, values);
	}
	
	public int updateGroup(Group group) {
		if (group == null) return 0;
		ContentValues values = prepareGroupValues(group);
		return db.update(DatabaseHelper.TABLE_GROUPS, values, DatabaseHelper.FIELD_ID+"="+group.getId(), null);
	}
	
	protected ContentValues prepareGroupValues(Group group) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_COUNT, group.getCount());
		return values;
	}
	
	public int deleteGroup(Group group) {
		if (group == null) return 0;
		return db.delete(DatabaseHelper.TABLE_GROUPS, DatabaseHelper.FIELD_ID+"="+group.getId(), null);
	}
	
	// Missed classes helper methods
	
	protected long[] getAttendanceIdsFromCourseId(long courseId) {
		long[] attendanceIDs = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_ATTENDANCES, new String[] {DatabaseHelper.FIELD_ID}, DatabaseHelper.FIELD_COURSE_ID+"="+courseId, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				attendanceIDs = new long[c.getCount()];
				int i = 0;
				do {
					attendanceIDs[i++] = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID)); 
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attendanceIDs;
	}
	
	protected long[] getStatusIdsForAttendanceIds(long[] attendanceIDs) {
		long[] statusIDs = null;
		try {
			String sqlRange = Utils.arrayToSQLRange(attendanceIDs);
			Cursor c = db.query(true, DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, new String[] {DatabaseHelper.FIELD_STATUS_ID}, DatabaseHelper.FIELD_ATTENDANCE_ID+" in " + sqlRange, null, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				statusIDs = new long[c.getCount()];
				int i = 0;
				do {
					statusIDs[i++] = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_STATUS_ID));
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusIDs;
	}
	
	protected long[] getStatusIdsForAttendanceId(long attendanceId) {
		long[] statusIDs = null;
		try {
			Cursor c = db.query(true, DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, new String[] {DatabaseHelper.FIELD_STATUS_ID}, DatabaseHelper.FIELD_ATTENDANCE_ID+"=" + attendanceId, null, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				statusIDs = new long[c.getCount()];
				int i = 0;
				do {
					statusIDs[i++] = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_STATUS_ID));
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusIDs;
	}
	
	protected boolean[] getAbsentStatusArray(long[] statusIDs) {
		boolean[] absent = null;
		try {
			String sqlRange = Utils.arrayToSQLRange(statusIDs);
			Cursor c = db.query(DatabaseHelper.TABLE_STATUSES, new String[] {DatabaseHelper.FIELD_ABSENT}, DatabaseHelper.FIELD_ID+" in "+sqlRange, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				absent = new boolean[c.getCount()];
				int i = 0;
				do {
					absent[i++] = (c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_ABSENT)) == 1);
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return absent;
	}
	
	public boolean hasMissedClassesAttendances(long courseId) {
		long[] attendanceIDs = getAttendanceIdsFromCourseId(courseId);
		if (attendanceIDs != null && attendanceIDs.length > 0) {
			long[] statusIDs = getStatusIdsForAttendanceIds(attendanceIDs);
			if (statusIDs != null && statusIDs.length > 0) {
				boolean[] absent = getAbsentStatusArray(statusIDs);
				if (absent != null && absent.length > 0) {
					return Utils.hasTrue(absent);
				}
			}
		}
		return false;
	}
	
	public boolean hasMissedClasses(long attendanceId) {
		long[] statusIDs = getStatusIdsForAttendanceId(attendanceId);
		if (statusIDs != null && statusIDs.length > 0) {
			boolean[] absent = getAbsentStatusArray(statusIDs);
			if (absent != null && absent.length > 0) {
				return Utils.hasTrue(absent);
			}
		}
		return false;
	}
	
	protected long[] getMissedStatusIds() {
		long[] statusIDs = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_STATUSES, new String[] {DatabaseHelper.FIELD_ID}, DatabaseHelper.FIELD_ABSENT+"=1", null, null, null, null);
			if (c != null && c.moveToFirst()) {
				statusIDs = new long[c.getCount()];
				int i = 0;
				do {
					statusIDs[i++] = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ID));
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusIDs;
	}
	
	public List<Attendance> getAttendancesWithMissedStudentStatus(long courseId) {
		List<Attendance> attendances = null;
		try {
			long[] statusIDs = getMissedStatusIds();
			long[] attendanceIDs = getAttendanceIdsFromCourseId(courseId);
			if (statusIDs != null && statusIDs.length > 0 && attendanceIDs != null && attendanceIDs.length > 0) {
				String sqlRange1 = Utils.arrayToSQLRange(statusIDs);
				String sqlRange2 = Utils.arrayToSQLRange(attendanceIDs);
				String selection = DatabaseHelper.FIELD_ATTENDANCE_ID+" in "+sqlRange2+" and "+DatabaseHelper.FIELD_STATUS_ID+" in "+sqlRange1;
				Cursor c = db.query(true, DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, new String[] {DatabaseHelper.FIELD_ATTENDANCE_ID}, selection, null, null, null, null, null);
				if (c != null && c.moveToFirst()) {
					attendances = new ArrayList<Attendance>();
					do {
						long attendanceId = c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_ATTENDANCE_ID));
						Attendance attendance = getAttendanceById(attendanceId);
						List<Student> students = getStudentsForAttendance(attendanceId);
						Iterator<Student> i = students.iterator();
						while (i.hasNext()) {
							Student student = i.next();
							if (student.getStatus() != null && !student.getStatus().isAbsent()) {
								i.remove();
							}
						}
						attendance.setStudents(students);
						attendances.add(attendance);
					} while (c.moveToNext());
				}
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attendances;
	}
	
	public Attendance getAttendanceWithMissedStudentStatus(long attendanceId) {
		Attendance attendance = null;
		try {
			long[] statusIDs = getMissedStatusIds();
			if (statusIDs != null && statusIDs.length > 0) {
				String sqlRange1 = Utils.arrayToSQLRange(statusIDs);
				String selection = DatabaseHelper.FIELD_ATTENDANCE_ID+"="+attendanceId+" and "+DatabaseHelper.FIELD_STATUS_ID+" in "+sqlRange1;
				Cursor c = db.query(true, DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE, new String[] {DatabaseHelper.FIELD_ATTENDANCE_ID}, selection, null, null, null, null, null);
				if (c != null && c.moveToFirst()) {
					attendance = getAttendanceById(attendanceId);
					List<Student> students = getStudentsForAttendance(attendanceId);
					Iterator<Student> i = students.iterator();
					while (i.hasNext()) {
						Student student = i.next();
						if (student.getStatus() != null && !student.getStatus().isAbsent()) {
							i.remove();
						}
					}
					attendance.setStudents(students);
				}
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attendance;
	}
	
	// Attendance details helper methods
	
	public boolean hasStudentTakePartInAnyAttendance(long studentId) {
		boolean takePart = false;
		try {
			String query = "select count(*) from " + DatabaseHelper.TABLE_STUDENTDS_2_ATTENDANCE + 
					" where " + DatabaseHelper.FIELD_STUDENT_ID + "=" + studentId;
			Cursor c = db.rawQuery(query, null);
			if (c != null && c.moveToFirst()) {
				takePart = (c.getLong(0) != 0);
			}
			if (c != null && !c.isClosed()) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return takePart;
	}

}
