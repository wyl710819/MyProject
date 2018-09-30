package com.routon.smartcampus.leave;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.routon.smartcampus.coursetable.CourseTableHelper;
import com.routon.smartcampus.coursetable.CourseDataUtil.SchoolTime;

import android.content.Context;
import android.util.Log;

public class LeaveTimeUtil {
	public static final String TAG = "LeaveTimeUtil";
	private Context mContext;
	private CourseTableHelper mCourseTableHelper;
	private int mGroupId;
	private String[] schoolDate;// 学校本学期的开始时间和结束时间
	private String[] workDate;

	public String termStartDate;// 本学期开始时间
	public String termEndDate;// 本学期结束时间

	private String workStartTime = "08:20:00";// 上学时间
	private String workEndTime = "17:20:00";// 放学时间

	private String workAmTime = "12:00:00";// 上午放学时间
	private String workPmTime = "14:00:00";// 下午上课时间

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");

	public LeaveTimeUtil(Context context, int groupId) {
		this.mContext = context;
		this.mGroupId = groupId;
		mCourseTableHelper = new CourseTableHelper(mContext);
		leaveTimeUtil = this;
	}

	public LeaveTimeUtil() {
	}

	private static LeaveTimeUtil leaveTimeUtil;

	public static synchronized LeaveTimeUtil getInstance() {
		if (leaveTimeUtil == null) {
			leaveTimeUtil = new LeaveTimeUtil();
		}
		return leaveTimeUtil;
	}

	public void getSchoolTime(final OnListener listener, final OnErrorListener errorListener) {
		mCourseTableHelper.getSchoolAttendance(mGroupId, new CourseTableHelper.Listener<String>() {

			@Override
			public void onResponse(String response) {
				if (response.equals("Failed")) {
					mCourseTableHelper.dismissDialog();
					if (errorListener != null) {
						errorListener.onError();
					}
					return;
				}
				Calendar calendar = Calendar.getInstance();
				mCourseTableHelper.getSchoolTimeTableAboutXmls(mGroupId, calendar,
						new CourseTableHelper.Listener<String>() {

							@Override
							public void onResponse(String response) {
								schoolDate = mCourseTableHelper.getSchoolBeginEndTime();
								workDate = mCourseTableHelper.getWorkEndTime();
								List<SchoolTime> schoolTimes = mCourseTableHelper.getSchoolTimes();

								// 获取本学期的起始-截止时间
								if (schoolDate != null) {
									termStartDate = schoolDate[0];
									termEndDate = schoolDate[1];
								}

								// 获取放学时间
								if (workDate != null) {
									workStartTime = workDate[0];
									workEndTime = workDate[1];
								}

								// 获取课程表中上午最后一节课和下午第一节课时间
								if (schoolTimes != null) {
									for (int i = 0; i < schoolTimes.size(); i++) {
										if (Integer.valueOf(schoolTimes.get(i).lesson) == 4) {// 上午最后一节课
											workAmTime = schoolTimes.get(i).endTime;
										}
										if (Integer.valueOf(schoolTimes.get(i).lesson) == 5) {// 下午第一节课
											workPmTime = schoolTimes.get(i).beginTime;
										}
									}
								}

								Log.d("run", "amStartTime=" + workStartTime + "----amEndTime=" + workAmTime
										+ "----pmStartTime=" + workPmTime + "----pmEndTime=" + workEndTime);

							}

						}, new CourseTableHelper.ErrorListener() {
							@Override
							public void onResponse(String errorMsg) {
								if (errorListener != null) {
									errorListener.onError();
								}
								Log.e(TAG, "errorMsg=" + errorMsg);
								return;
							}
						});

				if (listener != null) {
					listener.onResponse();
				}
			}

		});
	}

	// 获取两个时间内的请假时长
	public long getTime(String startTime, String endTime) {

		Date startDate = null;
		Date endDate = null;
		Date startDate1 = null;
		Date endDate1 = null;
		Date startTime1 = null;
		Date endTime1 = null;

		Date amEndTime = null;
		Date pmStartTime = null;

		Date amStartTime = null;
		Date pmEndTime = null;

		try {
			// 请假起止时间（yyyy-MM-dd HH:mm）
			startDate = format.parse(startTime);
			endDate = format.parse(endTime);

			// 请假起止时间（yyyy-MM-dd）
			startDate1 = format1.parse(startTime);
			endDate1 = format1.parse(endTime);
			// 请假起止时间（ HH:mm）
			startTime1 = format2.parse(startTime.substring(startTime.indexOf(" "), startTime.length()));
			endTime1 = format2.parse(endTime.substring(endTime.indexOf(" "), endTime.length()));

			// 上午放学时间
			amEndTime = format2.parse(workAmTime);
			// 下午上课时间
			pmStartTime = format2.parse(workPmTime);

			// 上午上学时间
			amStartTime = format2.parse(workStartTime);
			// 下午放学时间
			pmEndTime = format2.parse(workEndTime);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (endDate1.getTime() > startDate1.getTime()) {// 请假起止日期不在同一天
			long dataTag;
			long startTimeTag;
			long endTimeTag;
			// 获取间隔天数
			long nh = 1000 * 60 * 60 * 24;
			int weekDay = getSundayNum(startTime, endTime, "yyyy-MM-dd");//获取周六日天数
			long day = (int) ((endDate1.getTime() - startDate1.getTime()) / nh) - 1- weekDay;

			
			// 获取间隔天数中请假时长
			dataTag = day
					* ((amEndTime.getTime() - amStartTime.getTime()) + (pmEndTime.getTime() - pmStartTime.getTime()));

			// 单独计算开始时间
			if (startTime1.getTime() <= amStartTime.getTime()) {// 早上
				startTimeTag = (amEndTime.getTime() - amStartTime.getTime())
						+ (pmEndTime.getTime() - pmStartTime.getTime());
			} else if (startTime1.getTime() >= amStartTime.getTime() && startTime1.getTime() <= amEndTime.getTime()) {// 上午
				startTimeTag = (amEndTime.getTime() - startTime1.getTime())
						+ (pmEndTime.getTime() - pmStartTime.getTime());
			} else if (startTime1.getTime() >= amEndTime.getTime() && startTime1.getTime() <= pmStartTime.getTime()) {// 午间
				startTimeTag = (pmEndTime.getTime() - pmStartTime.getTime());
			} else if (startTime1.getTime() >= pmStartTime.getTime() && startTime1.getTime() <= pmEndTime.getTime()) {// 下午
				startTimeTag = (pmEndTime.getTime() - startTime1.getTime());
			} else {// 晚上
				startTimeTag = 0;
			}

			// 单独计算结束时间
			if (endTime1.getTime() <= amStartTime.getTime()) {// 早上
				endTimeTag = 0;

			} else if (endTime1.getTime() >= amStartTime.getTime() && endTime1.getTime() <= amEndTime.getTime()) {// 上午
				endTimeTag = endTime1.getTime() - amStartTime.getTime();
			} else if (endTime1.getTime() >= amEndTime.getTime() && endTime1.getTime() <= pmStartTime.getTime()) {// 午间
				endTimeTag = (amEndTime.getTime() - amStartTime.getTime());
			} else if (endTime1.getTime() >= pmStartTime.getTime() && endTime1.getTime() <= pmEndTime.getTime()) {// 下午
				endTimeTag = (endTime1.getTime() - pmStartTime.getTime())
						+ (amEndTime.getTime() - amStartTime.getTime());
			} else {// 晚上
				endTimeTag = (amEndTime.getTime() - amStartTime.getTime())
						+ (pmEndTime.getTime() - pmStartTime.getTime());
			}

			// Log.d("run", "Time="+dataTag+"--"+startTimeTag+"--"+endTimeTag);
			return dataTag + startTimeTag + endTimeTag;

		} else {// 请假起止日期在同一天
			if (startTime1.getTime() <= amEndTime.getTime() && endTime1.getTime() <= amEndTime.getTime()) {// 请假起止时间都在上午

				if (startTime1.getTime() <= amStartTime.getTime() && endTime1.getTime() <= amStartTime.getTime()) {// 请假开始时间和结束时间都早于上学时间，异常提醒
					return 0;
				} else if (startTime1.getTime() <= amStartTime.getTime()
						&& endTime1.getTime() >= amStartTime.getTime()) {// 请假开始时间早于上学时间，结束时间不早于上学时间，减掉早于上学的时间
					return endTime1.getTime() - amStartTime.getTime();
				} else if (startTime1.getTime() >= amStartTime.getTime()
						&& endTime1.getTime() >= amStartTime.getTime()) {// 请假起止时间都在上午上课范围内，正常处理
					return endTime1.getTime() - startTime1.getTime();
				}

			} else if (startTime1.getTime() >= pmStartTime.getTime()) {// 请假起止时间都在下午

				if (startTime1.getTime() >= pmEndTime.getTime() && endTime1.getTime() >= pmEndTime.getTime()) {// 请假开始时间和结束时间都晚于放学时间，异常提醒
					return 0;
				} else if (startTime1.getTime() >= pmEndTime.getTime() && endTime1.getTime() >= pmEndTime.getTime()) {// 请假开始时间早于放学时间，结束时间晚于放学时间，减掉晚于放学的时间
					return pmEndTime.getTime() - startTime1.getTime();
				} else if (endTime1.getTime() <= pmEndTime.getTime()) {// 请假起止时间都在下午上课范围内，正常处理
					return endTime1.getTime() - startTime1.getTime();
				}

			} else {// 请假起止时间分布在上午和下午
				if (startTime1.getTime() >= amEndTime.getTime() && endTime1.getTime() <= pmStartTime.getTime()) {// 请假起止时间都在午休时间内,异常处理
					return 0;
				} else if (startTime1.getTime() < amEndTime.getTime() && endTime1.getTime() <= pmStartTime.getTime()) {// 请假开始时间不在午休时间，结束时间在午休

					if (startTime1.getTime() < amStartTime.getTime()) {// 开始时间早于上学时间
						return amEndTime.getTime() - amStartTime.getTime();
					} else {// 开始时间晚于上学时间
						return amEndTime.getTime() - startTime1.getTime();
					}

				} else if (startTime1.getTime() >= amEndTime.getTime() && endTime1.getTime() > pmStartTime.getTime()) {// 请假开始时间在午休时间，结束时间不在午休
					if (endTime1.getTime() > pmEndTime.getTime()) {// 结束时间晚于放学时间
						return pmEndTime.getTime() - pmStartTime.getTime();
					} else {// 结束时间不晚于放学时间
						return endTime1.getTime() - pmStartTime.getTime();
					}
				} else if (startTime1.getTime() <= amStartTime.getTime() && endTime1.getTime() > pmEndTime.getTime()) {// 请假开始时间早于上学时间，结束时间晚于放学时间

					return (pmEndTime.getTime() - pmStartTime.getTime())
							+ (amEndTime.getTime() - amStartTime.getTime());

				} else if (startTime1.getTime() <= amStartTime.getTime()
						&& endTime1.getTime() > pmStartTime.getTime()) {// 请假开始时间早于上学时间，结束时间在下午上课范围内

					return (amEndTime.getTime() - amStartTime.getTime()) + (endTime1.getTime() - pmStartTime.getTime());

				} else if (startTime1.getTime() <= amEndTime.getTime() && endTime1.getTime() > pmEndTime.getTime()) {// 请假开始时间在上午上课范围内，结束时间晚于下午放学时间

					return (amEndTime.getTime() - startTime1.getTime()) + (pmEndTime.getTime() - pmStartTime.getTime());

				} else {// 请假开始时间在上午上课范围内，结束时间在下午上课范围内
					return (amEndTime.getTime() - startTime1.getTime()) + (endTime1.getTime() - pmStartTime.getTime());
				}
			}

		}

		return 0;
	}

	/**
	 * 获取2个日期之间周六，周日的天数
	 * 
	 * @param startDate
	 * @param endDate
	 * @param format
	 * @return
	 */
	public static int getSundayNum(String startDate, String endDate, String format) {
		List<String> yearMonthDayList = new ArrayList<String>();
		Date start = null, stop = null;
		try {
			start = new SimpleDateFormat(format).parse(startDate);
			stop = new SimpleDateFormat(format).parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (start.after(stop)) {
			Date tmp = start;
			start = stop;
			stop = tmp;
		}
		// 将起止时间中的所有时间加到List中
		Calendar calendarTemp = Calendar.getInstance();
		calendarTemp.setTime(start);
		while (calendarTemp.getTime().getTime() <= stop.getTime()) {
			yearMonthDayList.add(new SimpleDateFormat(format).format(calendarTemp.getTime()));
			calendarTemp.add(Calendar.DAY_OF_YEAR, 1);
		}
		Collections.sort(yearMonthDayList);
		int num = 0;// 周六，周日的总天数
		int size = yearMonthDayList.size();
		int week = 0;
		for (int i = 0; i < size; i++) {
			String day = (String) yearMonthDayList.get(i);
			week = getWeek(day, format);
			if (week == 6 || week == 0) {// 周六，周日
				num++;
			}
		}
		return num;
	}

	/**
	 * 获取某个日期是星期几
	 * 
	 * @param date
	 * @param format
	 * @return 0-星期日
	 */
	public static int getWeek(String date, String format) {
		Calendar calendarTemp = Calendar.getInstance();
		try {
			calendarTemp.setTime(new SimpleDateFormat(format).parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int i = calendarTemp.get(Calendar.DAY_OF_WEEK);
		int value = i - 1;// 0-星期日
		// System.out.println(value);
		return value;
	}


	public interface OnListener {
		public void onResponse();
	}

	public interface OnErrorListener {
		public void onError();
	}
}
