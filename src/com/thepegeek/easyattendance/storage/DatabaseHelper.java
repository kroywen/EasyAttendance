package com.thepegeek.easyattendance.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.Status;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME = "easy_attendance";
	public static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_COURSES = "courses";
	public static final String TABLE_STUDENTS_2_COURSES = "students_2_courses";
	public static final String TABLE_STUDENTS = "students";
	public static final String TABLE_STATUSES = "statuses";
	public static final String TABLE_LABELS = "labels";
	public static final String TABLE_LABELS_2_STUDENTS = "labels_2_students";
	public static final String TABLE_OBSERVATIONS = "observations";
	public static final String TABLE_ATTENDANCES = "attendances";
	public static final String TABLE_STUDENTDS_2_ATTENDANCE = "students_2_attendance";
	public static final String TABLE_GROUPS = "groups";
	
	public static final String FIELD_ID = "id";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_FIRSTNAME = "firstname";
	public static final String FIELD_LASTNAME = "lastname";
	public static final String FIELD_COURSE_ID = "course_id";
	public static final String FIELD_STUDENT_ID = "student_id";
	public static final String FIELD_ABSENT = "absent";
	public static final String FIELD_COMMON = "common";
	public static final String FIELD_DEFAULT = "_default";
	public static final String FIELD_COLOR = "color";
	public static final String FIELD_PHOTO = "photo";
	public static final String FIELD_LABEL_ID = "label_id";
	public static final String FIELD_LABEL_NAME = "label_name";
	public static final String FIELD_VALUE = "value";
	public static final String FIELD_OBSERVATION_ID = "observation_id";
	public static final String FIELD_DATE = "date";
	public static final String FIELD_NOTE = "note";
	public static final String FIELD_ATTENDANCE_ID = "attendance_id";
	public static final String FIELD_STATUS_ID = "status_id";
	public static final String FIELD_COUNT = "count";
	public static final String FIELD_GROUP_ID = "group_id"; 
	
	public static final String CREATE_TABLE_COURSES = 
			"create table if not exists " + TABLE_COURSES + " (" +
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_NAME + " text);";
	
	public static final String DROP_TABLE_COURSES =
			"drop table if exists " + TABLE_COURSES; 
	
	public static final String CREATE_TABLE_STUDENTS_2_COURSES =
			"create table if not exists " + TABLE_STUDENTS_2_COURSES + " (" +
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_COURSE_ID + " integer, " +
			FIELD_STUDENT_ID + " integer);";
	
	public static final String DROP_TABLE_STUDENTS_2_COURSES = 
			"drop table if exists " + TABLE_STUDENTS_2_COURSES;
	
	public static final String CREATE_TABLE_STUDENTS =
			"create table if not exists " + TABLE_STUDENTS + " (" +
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_FIRSTNAME + " text, " +
			FIELD_LASTNAME + " text, " +
			FIELD_PHOTO + " text);";
	
	public static final String DROP_TABLE_STUDENTS =
			"drop table if exists " + TABLE_STUDENTS;
	
	public static final String CREATE_TABLE_STATUSES =
			"create table if not exists " + TABLE_STATUSES + " (" +
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_NAME + " text, " +
			FIELD_ABSENT + " integer, " +
			FIELD_COMMON + " integer, " +
			FIELD_DEFAULT + " integer, " +
			FIELD_COLOR + " integer);";
	
	public static final String DROP_TABLE_STATUSES =
			"drop table if exists " + TABLE_STATUSES;
	
	public static final String CREATE_TABLE_LABELS = 
			"create table if not exists " + TABLE_LABELS + " (" + 
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_NAME + " text);";
	
	public static final String DROP_TABLE_LABELS =
			"drop table if exists " + TABLE_LABELS;
	
	public static final String CREATE_TABLE_LABELS_2_STUDENTS =
			"create table if not exists " + TABLE_LABELS_2_STUDENTS + " (" +
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_STUDENT_ID + " integer, " +
			FIELD_LABEL_ID + " integer, " +
			FIELD_LABEL_NAME + " text, " +
			FIELD_VALUE + " text);";
	
	public static final String DROP_TABLE_LABELS_2_STUDENTS =
			"drop table if exists " + TABLE_LABELS_2_STUDENTS;
	
	public static final String CREATE_TABLE_OBSERVATIONS = 
			"create table if not exists " + TABLE_OBSERVATIONS + " (" +
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_STUDENT_ID + " integer, " +
			FIELD_DATE + " text, " +
			FIELD_NOTE + " text);";
	
	public static final String DROP_TABLE_OBSERVATIONS =
			"drop table if exists " + TABLE_OBSERVATIONS;
	
	public static final String CREATE_TABLE_ATTENDANCES =
			"create table if not exists " + TABLE_ATTENDANCES + " (" +
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_COURSE_ID + " integer, " +
			FIELD_DATE + " integer, " + 
			FIELD_NOTE + " text, " +
			FIELD_PHOTO + " text);";
			
	public static final String DROP_TABLE_ATTENDANCES = 
			"drop table if exists " + TABLE_ATTENDANCES;
	
	public static final String CREATE_TABLE_STUDENTS_2_ATTENDANCE =
			"create table if not exists " + TABLE_STUDENTDS_2_ATTENDANCE + " (" +
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_ATTENDANCE_ID + " integer, " + 
			FIELD_STUDENT_ID + " integer, " +
			FIELD_STATUS_ID + " integer);";
	
	public static final String DROP_TABLE_STUDENTS_2_ATTENDANCE =
			"drop table if exists " + TABLE_STUDENTDS_2_ATTENDANCE;
	
	public static final String CREATE_TABLE_GROUPS = 
			"create table if not exists " + TABLE_GROUPS + " (" +
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_COUNT + " integer);";
	
	public static final String DROP_TABLE_GROUPS =
			"drop table if exists " + TABLE_GROUPS;
	
	protected Context context;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTables(db);
		onCreate(db);
	}
	
	protected void createTables(SQLiteDatabase db) {
		if (db != null) {
			db.execSQL(CREATE_TABLE_COURSES);
			db.execSQL(CREATE_TABLE_STUDENTS_2_COURSES);
			db.execSQL(CREATE_TABLE_STUDENTS);
			db.execSQL(CREATE_TABLE_STATUSES);
			db.execSQL(CREATE_TABLE_LABELS);
			db.execSQL(CREATE_TABLE_LABELS_2_STUDENTS);
			db.execSQL(CREATE_TABLE_OBSERVATIONS);
			db.execSQL(CREATE_TABLE_ATTENDANCES);
			db.execSQL(CREATE_TABLE_STUDENTS_2_ATTENDANCE);
			db.execSQL(CREATE_TABLE_GROUPS);
		}
		insertCommonStatuses(db);
		insertRandomGroup(db);
	}
	
	protected void dropTables(SQLiteDatabase db) {
		if (db != null) {
			db.execSQL(DROP_TABLE_COURSES);
			db.execSQL(DROP_TABLE_STUDENTS_2_COURSES);
			db.execSQL(DROP_TABLE_STUDENTS);
			db.execSQL(DROP_TABLE_STATUSES);
			db.execSQL(DROP_TABLE_LABELS);
			db.execSQL(DROP_TABLE_LABELS_2_STUDENTS);
			db.execSQL(DROP_TABLE_OBSERVATIONS);
			db.execSQL(DROP_TABLE_ATTENDANCES);
			db.execSQL(DROP_TABLE_STUDENTS_2_ATTENDANCE);
			db.execSQL(DROP_TABLE_GROUPS);
		}
	}
	
	protected void insertCommonStatuses(SQLiteDatabase db) {
		String[] statuses = context.getResources().getStringArray(R.array.default_statuses);
		int[] colors = Status.defaultColors;
		for (int i=0; i<statuses.length; i++) {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FIELD_NAME, statuses[i]);
			values.put(DatabaseHelper.FIELD_ABSENT, (i == 1) ? 1 : 0);
			values.put(DatabaseHelper.FIELD_COMMON, 1);
			values.put(DatabaseHelper.FIELD_DEFAULT, statuses[i].equalsIgnoreCase("present") ? 1 : 0);
			values.put(DatabaseHelper.FIELD_COLOR, colors[i]);
			db.insert(DatabaseHelper.TABLE_STATUSES, null, values);
		}
	}
	
	protected void insertRandomGroup(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_COUNT, 0);
		db.insert(DatabaseHelper.TABLE_GROUPS, null, values);
	}

}
