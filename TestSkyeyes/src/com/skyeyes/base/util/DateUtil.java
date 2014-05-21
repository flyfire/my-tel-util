package com.skyeyes.base.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期处理帮助类
 * 
 * @author chao.xu
 */
public class DateUtil {
	// 时间格式
	public final static String TIME_FORMAT_YMDHMS = "yyyy-MM-dd HH:mm:ss";
	public final static String TIME_FORMAT_YMDHM = "yyyy-MM-dd HH:mm";
	public final static String TIME_FORMAT_YMD = "yyyy-MM-dd";
	public final static String TIME_FORMAT_HM = "HH:mm";
	
	   /**
     * Unix 时间 1970-01-01 00:00:00 与 Win32 FileTime 时间 1601-01-01 00:00:00
     * 毫秒数差 
     */
    public final static long UNIX_FILETIME_DIFF = 11644473600000L;
     
    /**
     * Win32 FileTime 采用 100ns 为单位的，定义 100ns 与 1ms 的倍率
     */
    public final static int MILLISECOND_MULTIPLE = 10000;
    

	   /**
     * 将 Win32 的 FileTime 结构转为 Java 中的 Date 类型
     * @param fileTime
     * @return
     */
    public static Date fileTime2Date(long fileTime) {
        return new Date(fileTime / MILLISECOND_MULTIPLE - UNIX_FILETIME_DIFF);
    }
    
    /**
     * 将 Java 中的 Date 类型转为 Win32 的 FileTime 结构
     * @param date
     * @return
     */
    public static long date2FileTime(Date date) {
        return (UNIX_FILETIME_DIFF + date.getTime()) * MILLISECOND_MULTIPLE;
    }

	
	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @param dateFormat
	 * @return String
	 */
	public static String getTimeStringFormat(Date date, String dateFormat) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		return simpleDateFormat.format(date);
	}

	public static String getTimeStringFormat(Calendar cal, String dateFormat) {
		return getTimeStringFormat(cal.getTime(), dateFormat);
	}

	/**
	 * 得到默认时间的日期字符串
	 * 
	 * @param dateFormat
	 * @return String
	 */
	public static String getDefaultTimeStringFormat(String dateFormat) {
		return getTimeStringFormat(new Date(), dateFormat);
	}

	/**
	 * 得到相隔day的日期
	 * 
	 * @param date
	 * @param day
	 * @return Date
	 */
	public static Date operationDate(Date date, int day) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.setTimeInMillis(c.getTimeInMillis() + ((long) day) * 24 * 3600 * 1000);
		return c.getTime();
	}

	/**
	 * 第day天的日期
	 * 
	 * @param c
	 * @param day
	 * @return
	 */
	public static Calendar getCalendarByNumDay(Calendar c, int day) {
		Calendar cc = Calendar.getInstance();
		cc.setTimeInMillis(c.getTimeInMillis() + ((long) day) * 24 * 3600 * 1000);
		return cc;
	}

	/**
	 * 解析日期
	 * 
	 * @param dateString
	 * @param day
	 * @return
	 */
	public static String parseDateString(String dateString, int day) {
		Calendar c = DateUtil.getCalendarByString(dateString, "yyyy-MM-dd");
		Calendar whenCalendar = DateUtil.getCalendarByNumDay(c, day);
		return DateUtil.getDateTimeStringFormat(whenCalendar);
	}

	/**
	 * 根据字符串得到Calendar
	 * 
	 * @param dateString
	 *            "2012-12-13"
	 * @param dateFormat
	 *            yyyy-MM-dd
	 * @return
	 */
	public static Calendar getCalendarByString(String dateString, String dateFormat) {
		Calendar c = Calendar.getInstance();
		Date date = null;
		try {
			date = new SimpleDateFormat(dateFormat).parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.setTime(date);
		return c;
	}

	public static String getDateTimeStringFormat(Calendar date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT_YMD);
		return simpleDateFormat.format(date.getTime());
	}

	public static String getDateTimeStringFormatHhMm(Calendar date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT_HM);
		return simpleDateFormat.format(date.getTime());
	}



	public static String getSystemDateFormat(int year, int month, int day) {
		String nMonth, nDay;
		if (month < 9) {
			nMonth = "0" + (month + 1);
		} else {
			nMonth = "" + (month + 1);
		}
		if (day < 10) {
			nDay = "0" + day;
		} else {
			nDay = "" + day;
		}
		return year + "-" + nMonth + "-" + nDay;
	}

	/**
	 * 将格式为 yyyy-MM-dd HH:mm:ssss 的时间 转成 yyyy-MM-dd HH:mm
	 */
	public static String getSimpleDateString(String date) {
		return null == date ? "" : date.substring(0, 16);
	}

	/**
	 * 将格式为 yyyy-MM-dd HH:mm:ssss 的时间 转成 HH:mm
	 */
	public static String getSimpleHhMmDateString(String date) {
		return null == date ? "" : date.substring(11, 16);
	}

	/**
	 * 获得指定日期的前N天
	 * 
	 * @param specifiedDay
	 * @return
	 * @throws Exception
	 */

	public static Calendar getSpecifiedDayBeforeNumDay(String specifiedDay, int beforeDay) {
		Calendar c = Calendar.getInstance();
		Date date = null;
		try {
			date = new SimpleDateFormat(TIME_FORMAT_YMD).parse(specifiedDay);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day - beforeDay);
		return c;
	}

	/**
	 * 获得指定日期的后N天
	 * 
	 * @param specifiedDay
	 * @return
	 */
	public static Calendar getSpecifiedDayAfterNumDay(String specifiedDay, int beforeDay) {
		Calendar c = Calendar.getInstance();
		Date date = null;
		try {
			date = new SimpleDateFormat(TIME_FORMAT_YMD).parse(specifiedDay);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day + beforeDay);
		return c;
	}

	/** 判断是否为当天日期 */
	public static boolean isToday(String date) {
		DateFormat df = new SimpleDateFormat(TIME_FORMAT_YMD);
		try {
			Date dt1 = df.parse(date);
			Date dt2 = df.parse(getDefaultTimeStringFormat(TIME_FORMAT_YMD));
			if (dt1.getTime() > dt2.getTime()) {
				return false;
			} else if (dt1.getTime() < dt2.getTime()) {
				return false;
			} else {
				return true;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return false;
	}

	/** 判断医嘱执行时间是否小于或大于计划时间2h */
	public static boolean surpassPlaningTime(String execDate, String planDate) {
		boolean isSurpass = false;
		final String executeDate = DateUtil
				.getTimeStringFormat(DateUtil.getCalendarByString(execDate, TIME_FORMAT_YMDHM),
						TIME_FORMAT_YMDHMS);
		final String planingDate = DateUtil
				.getTimeStringFormat(DateUtil.getCalendarByString(planDate, TIME_FORMAT_YMDHM),
						TIME_FORMAT_YMDHMS);
		SimpleDateFormat dfs = new SimpleDateFormat(TIME_FORMAT_YMDHMS);
		try {
			Date eDate = dfs.parse(executeDate);
			Date pDate = dfs.parse(planingDate);
			long between = Math.abs(eDate.getTime() - pDate.getTime()) / 1000;
			if (between > 7200) {
				isSurpass = true;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isSurpass;
	}
}