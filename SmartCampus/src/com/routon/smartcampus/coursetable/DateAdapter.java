package com.routon.smartcampus.coursetable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.routon.edurelease.R;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DateAdapter extends BaseAdapter {
	
private String lauarDay;//记录阴历日期
	
	private boolean isLeapyear = false; // 是否为闰年
	private int daysOfMonth = 0; // 某月的天数
	private int dayOfWeek = 0; // 具体某一天是星期几
	private int lastDaysOfMonth = 0; // 上一个月的总天数
	private Context context;
	private SpecialCalendar sc = null;
	private String[] dayNumber = new String[7];
//	private String[] lauarDayNumber=new String[7];
	private String[] weekNumber={"日","一","二","三","四","五","六"};
	private String[] fiveWeek={"周一","周二","周三","周四","周五"};
	private String[] fiveDay=new String[5];
	private List<String> listDay=new ArrayList<String>();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
	// private int currentFlag = -1; // 用于标记当天
	// 系统当前时间
	private String sysDate = "";
	private String sys_year = "";
	private String sys_month = "";
	private String sys_day = "";
	private String currentYear = "";
	private String currentMonth = "";
	private String currentWeek = "";
	private String currentDay = "";
	private int weeksOfMonth;
	private int clickTemp = -1;
	private boolean isStart;
	private String mSysCurrDay;
	private String mClickCurrDaye;

	public String dateTag;
	public boolean isScroll;
	public List<String> dateTagList;

	// 标识选择的Item
	public void setSeclection(int position) {
		clickTemp = position;
	}

	public DateAdapter() {
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d");
		mSysCurrDay=df.format(date);
		sysDate = sdf.format(date); // 当期日期
		sys_year = sysDate.split("-")[0];
		sys_month = sysDate.split("-")[1];
		sys_day = sysDate.split("-")[2];
	}

	public DateAdapter(Context context, int year_c, int month_c, int week_c,
			boolean isStart) {
		this();
		this.context = context;
		this.isStart = isStart;
		sc = new SpecialCalendar();

		currentYear = String.valueOf(year_c);// 得到当前的年份
		currentMonth = String.valueOf(month_c); // 得到本月
												// （jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
		currentDay = String.valueOf(sys_day); // 得到当前日期是哪天
		getCalendar(Integer.parseInt(currentYear),
				Integer.parseInt(currentMonth));
		currentWeek = String.valueOf(week_c);
		getWeek(Integer.parseInt(currentYear), Integer.parseInt(currentMonth),
				Integer.parseInt(currentWeek));
		if(listDay.size()>0){
			for(int i=0;i<listDay.size();i++){
				fiveDay[i]=listDay.get(i);
			}
		}
		
		//getLauarDay(currentYear,currentMonth);
	}

	public int getTodayPosition() {
		int todayWeek = sc.getWeekDayOfLastMonth(Integer.parseInt(sys_year),
				Integer.parseInt(sys_month), Integer.parseInt(sys_day));
		if (todayWeek == 7) {
			clickTemp = 0;
		} else {
			clickTemp = todayWeek;
		}
		return clickTemp;
	}

	// 根据选择的位置返回当前月份
	public int getCurrentMonth(int position) {
		int thisDayOfWeek = sc.getWeekdayOfMonth(Integer.parseInt(currentYear),
				Integer.parseInt(currentMonth));
		if (isStart) {
			if (thisDayOfWeek != 7) {
				if (position < thisDayOfWeek) {
					return Integer.parseInt(currentMonth) - 1 == 0 ? 12
							: Integer.parseInt(currentMonth) - 1;
				} else {
					return Integer.parseInt(currentMonth);
				}
			} else {
				return Integer.parseInt(currentMonth);
			}
		} else {
			return Integer.parseInt(currentMonth);
		}

	}

	// 根据选择的位置返回当前年份
	public int getCurrentYear(int position) {
		int thisDayOfWeek = sc.getWeekdayOfMonth(Integer.parseInt(currentYear),
				Integer.parseInt(currentMonth));
		if (isStart) {
			if (thisDayOfWeek != 7) {
				if (position < thisDayOfWeek) {
					return Integer.parseInt(currentMonth) - 1 == 0 ? Integer
							.parseInt(currentYear) - 1 : Integer
							.parseInt(currentYear);
				} else {
					return Integer.parseInt(currentYear);
				}
			} else {
				return Integer.parseInt(currentYear);
			}
		} else {
			return Integer.parseInt(currentYear);
		}
	}

	public void getCalendar(int year, int month) {
		isLeapyear = sc.isLeapYear(year); // 是否为闰年
		daysOfMonth = sc.getDaysOfMonth(isLeapyear, month); // 某月的总天数
		dayOfWeek = sc.getWeekdayOfMonth(year, month); // 某月第一天为星期几
		lastDaysOfMonth = sc.getDaysOfMonth(isLeapyear, month - 1);
	}

	public void getWeek(int year, int month, int week) {
		for (int i = 0; i < dayNumber.length; i++) {
			if (dayOfWeek == 7) {
				dayNumber[i] = String.valueOf((i + 1) + 7 * (week - 1));
			} else {
				if (week == 1) {
					if (i < dayOfWeek) {
						dayNumber[i] = String.valueOf(lastDaysOfMonth
								- (dayOfWeek - (i + 1)));
					} else {
						dayNumber[i] = String.valueOf(i - dayOfWeek + 1);
					}
				} else {
					dayNumber[i] = String.valueOf((7 - dayOfWeek + 1 + i) + 7
							* (week - 2));
				}
			}
			if(i!=0&&i!=6){
				listDay.add(dayNumber[i]);
			}
		}
	}
	
	public String[] getDayNumbers() {
		return dayNumber;
	}

	/**
	 * 得到某月有几周(特殊算法)
	 */
	public int getWeeksOfMonth() {
		// getCalendar(year, month);
		int preMonthRelax = 0;
		if (dayOfWeek != 7) {
			preMonthRelax = dayOfWeek;
		}
		if ((daysOfMonth + preMonthRelax) % 7 == 0) {
			weeksOfMonth = (daysOfMonth + preMonthRelax) / 7;
		} else {
			weeksOfMonth = (daysOfMonth + preMonthRelax) / 7 + 1;
		}
		return weeksOfMonth;
	}

	/**
	 * 某一天在第几周
	 */
	
	public void getDayInWeek(int year, int month) {

	}

	@Override
	public int getCount() {
		return fiveDay.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
//		lauarDay=LauarUtils.getLunar(currentYear, currentMonth, currentDay);
//		Log.d("TAG",currentYear+currentMonth+currentDay);
		mClickCurrDaye=getCurrentYear(position)+"-"+getCurrentMonth(position+1)+"-"+dayNumber[position];
		Calendar calendar = Calendar.getInstance();
		int mWeek = calendar.get(Calendar.DAY_OF_WEEK);  
		Holder holder;
		if (convertView == null) {
			holder = new Holder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_teacher_course_calendar, null);
			holder.tvNumber = (TextView) convertView.findViewById(R.id.tv_calendar);
			//holder.tvLauarNumber=(TextView) convertView.findViewById(R.id.lauarDay);
			holder.tvWeekNumber=(TextView)convertView.findViewById(R.id.tv_week);
			holder.mWeekRoot=(LinearLayout) convertView.findViewById(R.id.week_root);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		
		//Log.d("TAG",lauarDay);
		//holder.tvLauarNumber.setText(lauarDayNumber[position]);
		
		holder.tvWeekNumber.setText(fiveWeek[position]);
		holder.tvNumber.setText(getCurrentMonth(position+1)+"/"+fiveDay[position]);
		
		if(position==mWeek-2){
			holder.mWeekRoot.setBackgroundColor(Color.parseColor("#6fc0de"));
		}else{
			holder.mWeekRoot.setBackgroundColor(Color.TRANSPARENT);
		}
		if (clickTemp == position) {
			if(mSysCurrDay.equals(mClickCurrDaye)){
				holder.tvNumber.setSelected(true);
//				holder.tvNumber.setTextColor(Color.WHITE);
				//holder.tvLauarNumber.setTextColor(Color.WHITE);
				//holder.clickDate.setBackgroundResource(R.drawable.click_color);
			}
//				else{
//				holder.tvNumber.setSelected(true);
//				holder.tvNumber.setTextColor(Color.WHITE);
//				//holder.tvLauarNumber.setTextColor(Color.WHITE);
//				holder.clickDate.setBackgroundResource(R.drawable.click_color_blue);
//			}
			
			//holder.tvNumber.setBackgroundResource(R.drawable.circle_message);
		} else {
			/*holder.tvNumber.setSelected(false);
			
			if(position==0||position==6){
				holder.tvNumber.setTextColor(Color.GRAY);
				//holder.tvLauarNumber.setTextColor(Color.GRAY);
				holder.tvWeekNumber.setTextColor(Color.GRAY);
			}else if (isScroll && compareTime(dateTagList.get(position),dateTag)) {
				holder.tvNumber.setTextColor(Color.parseColor("#33B014"));
				//holder.tvLauarNumber.setTextColor(Color.parseColor("#33B014"));
//				holder.tvWeekNumber.setTextColor(Color.parseColor("#33B014"));
			}else {*/
				holder.tvNumber.setTextColor(Color.parseColor("#999999"));
				//holder.tvLauarNumber.setTextColor(Color.BLACK);
				holder.tvWeekNumber.setTextColor(Color.BLACK);
			//}
			if(mSysCurrDay.equals(mClickCurrDaye)){
				if (isScroll && compareTime(dateTagList.get(position),dateTag)) {
					
				}else {
					holder.tvNumber.setTextColor(Color.parseColor("#FF0000"));
					//holder.tvLauarNumber.setTextColor(Color.parseColor("#FF0000"));
				}
			}
			
			//holder.tvNumber.setBackgroundColor(Color.TRANSPARENT);
//			holder.mWeekRoot.setBackgroundColor(Color.TRANSPARENT);
		}
		return convertView;
	}

	class Holder {
		TextView tvNumber;
//		TextView tvLauarNumber;
		TextView tvWeekNumber;
		LinearLayout mWeekRoot;
	}
	
	private boolean compareTime(String listStr, String currentStr) {
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-M-d");
		long listLong = 0;
		long currentLong = 0;
		try {
			listLong=myFormatter.parse(listStr).getTime();
			currentLong=myFormatter.parse(currentStr).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listLong>currentLong ?  false:true;
	}
}
