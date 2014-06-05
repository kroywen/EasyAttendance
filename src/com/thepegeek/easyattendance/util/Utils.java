package com.thepegeek.easyattendance.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

public class Utils {
	
	public static final int MILLISECONDS_IN_DAY = 86400000;
//	public static final int START_YEAR = 1900;
		
	public static final String DATE_FORMAT = "E LLL d";
	public static SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
	
	public static final String DATE_TIME_FORMAT = "M/d/y, h:mm a";
	public static SimpleDateFormat datetimeFormatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.US);
	
	public static String[] ampmItems = {"AM", "PM"};
	
	public static long start = 0;
	public static long end = 0;
	public static int count = 0;
	
	public static Date[] dates;
	public static String[] minutes;
	public static String[] hours;
	public static String[] datesStr;
	
	public static final long[] getRandomNumbers(long[] numbers, int count) {
		if (numbers == null || numbers.length == 0 || count == 0) return null;
		if (count == numbers.length) return numbers;
		
		Random r = new Random(System.currentTimeMillis());
		
		boolean[] selected = new boolean[numbers.length];
		long[] result = new long[count];
		
		for (int i=0; i<count; i++) {
			int j = 0;
			do {
				j = r.nextInt(numbers.length);
			} while (selected[j]);
			selected[j] = true;
			result[i] = numbers[j];
		}
		
		return result;
	}
	
	public static final long[] getRandomNumbers(long[] numbers) {
		Random r = new Random(System.currentTimeMillis());
		return getRandomNumbers(numbers, r.nextInt(numbers.length-1)+1);
	}
	
	public static final String arrayToSQLRange(long[] array) {
		if (array == null || array.length == 0) return "()";
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (int i=0; i<array.length; i++) {
			sb.append(array[i]);
			if (i < array.length-1) {
				sb.append(',');
			}
		}
		sb.append(')');
		return sb.toString();
	}
	
	public static final boolean isEmpty(List<?> list) {
		return (list == null || list.isEmpty());
	}
	
	public static String uppercaseFirstChar(String str) {
	    return TextUtils.isEmpty(str) ? str : 
	    	Character.toUpperCase(str.charAt(0)) + (str.length() > 1 ? str.substring(1) : "");
	}
	
	public static boolean hasTrue(boolean[] array) {
		if (array == null || array.length == 0) return false;
		for (boolean b : array) {
			if (b) return true;
		}
		return false;
	}
	
	public static final String getReadableFilesize(long bytes) {
		String s = " B";
		String size = bytes + s;
		
		while (bytes > 1024) {
			if (s.equals(" B"))	s = " KB";
			else if (s.equals(" KB")) s = " MB";
			else if (s.equals(" MB")) s = " GB";
			else if (s.equals(" GB")) s = " TB";
			
			size = (bytes / 1024) + "." + (bytes % 1024) + s;
			bytes = bytes / 1024;
		}
		
		return size;
	}
	
	public static boolean isEmail(String text) {
		Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);
		return m.find();
	}
	
	public static boolean isPhone(String text) {
		if (TextUtils.isEmpty(text)) return false;
		if (text.length() < 9) return false;
		for (char c : text.toCharArray()) {
			if (!(c == '+' || Character.isDigit(c)))
				return false;
		}
		return true;
	}
	
	public static String[] getDateItems(long currentTimeMillis) {		
		Calendar cal = Calendar.getInstance(Locale.US);
		if (dates == null) {
			cal.setTimeInMillis(currentTimeMillis);
			int year = cal.get(Calendar.YEAR);
			
			cal.set(year, 0, 1, 0, 0, 0);
			start = cal.getTimeInMillis();
			
			cal.set(year, 11, 31, 23, 59, 59);
			end = cal.getTimeInMillis();
			
			count = (int) Math.floor((end - start) / MILLISECONDS_IN_DAY) + 1;
			
			dates = new Date[count];
			cal.setTimeInMillis(start);
			for (int i = 0; i < count; i++) {
				dates[i] = new Date(cal.getTimeInMillis());
				cal.add(Calendar.MILLISECOND, MILLISECONDS_IN_DAY);
			}
		}
		if (datesStr == null) {
			datesStr = new String[count];
			cal.setTimeInMillis(start);
			for (int i = 0; i < count; i++) {
				datesStr[i] = formatDate(dates[i]);
				cal.add(Calendar.MILLISECOND, MILLISECONDS_IN_DAY);
			}
		}
		
		return datesStr;
	}
	
	public static String formatDate(Date date) {
		return dateFormatter.format(date);
	}
	
	public static int getAmpm(long currentTimeMillis) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTimeInMillis(currentTimeMillis);
		return cal.get(Calendar.AM_PM);
	}
	
	public static int getDatePosition(long timestamp) {
		if (datesStr != null) {
			String curDate = dateFormatter.format(new Date(timestamp));
			for (int i=0; i<datesStr.length; i++) {
				if (datesStr[i].equalsIgnoreCase(curDate)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static String[] getHoursItems() {
		if (hours == null) {
			hours = new String[24];
			for (int i=0; i<24; i++) {
				hours[i] = i > 9 ? String.valueOf(i) : "0"+i;
			}
		}
		return hours;
	}
	
	public static String[] getMinutesItems() {
		if (minutes == null) {
			minutes = new String[60];
			for (int i=0; i<60; i++) {
				minutes[i] = i > 9 ? String.valueOf(i) : "0"+i;
			}
		}
		return minutes;
	}
	
	public static int getHours(long currentTimeMillis) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTimeInMillis(currentTimeMillis);
		return cal.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int getMinutes(long currentTimeMillis) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTimeInMillis(currentTimeMillis);
		return cal.get(Calendar.MINUTE);
	}
	
	public static Date getDate(String datetime) {
		Date date = null;
		try {
			date = datetimeFormatter.parse(datetime);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static Date getDate(int position) {
		if (dates != null && dates.length > 0 && 
			position >= 0 && position < dates.length) {
			return dates[position];
		} else {
			return null;
		}
	}

}
