package com.routon.smartcampus.coursetable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.routon.edurelease.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

public class WeekCalendarView extends RelativeLayout implements OnGestureListener{

	private int currentYear;
	private int currentMonth;
	private int currentWeek;
	private int currentDay;
	private int currentNum;
	private String currentDate = "";
	private int year_c = 0;
	private int month_c = 0;
	private int day_c = 0;
	private int week_c = 0;
	private int week_num = 0;
	private int daysOfMonth = 0;
	private int dayOfWeek = 0;
	private SpecialCalendar sc = null;
	private boolean isLeapyear = false;
	private int weeksOfMonth = 0;
	private GridView gridView = null;
	private String dayNumbers[] = new String[7];
	//private TextView tvDate;
	private int selectPostion = 0;
	private ViewFlipper flipperView;
	private DateAdapter dateAdapter;
	private GestureDetector gestureDetector;
	private int gvFlag;

	private String dateParam;
	private boolean isScroll;
	private boolean moveEnable;
	private Context mContext;
	private View calendarView;
	private WeekCalendarListener mWeekCalendarListener;
	private List<String> dateStrList;
	private int currentNumberTag;
	private List<String> currentWeekDateStrList;
	private String dateTag;

	public WeekCalendarView(Context context) {
		super(context);
		init(context);
	}

	public WeekCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public WeekCalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.mContext = context;
		this.calendarView = LayoutInflater.from(mContext).inflate(R.layout.teacher_course_week_calendar_layout, this);
		flipperView = (ViewFlipper) calendarView.findViewById(R.id.flipper_view);
		//tvDate = (TextView) calendarView.findViewById(R.id.tv_date);
		gestureDetector = new GestureDetector(this);
		initData(new Date());
	}
	
	public void setCurrentDate(Date date){
		initData(date);
	}

	private void initData(Date date) {
		flipperView.removeAllViews();
		moveEnable = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年M月d日");
		currentDate = sdf.format(date);
		year_c = Integer.parseInt(currentDate.split("-")[0]);
		month_c = Integer.parseInt(currentDate.split("-")[1]);
		day_c = Integer.parseInt(currentDate.split("-")[2]);
		Log.d("Week","initData day_c:"+day_c);
		currentYear = year_c;
		currentMonth = month_c;
		currentDay = day_c;
		sc = new SpecialCalendar();
		getCalendar(year_c, month_c);
		week_num = getWeeksOfMonth();
		currentNum = week_num;
		if (dayOfWeek == 7) {
			week_c = currentDay / 7 + 1;
		} else {
			if (currentDay <= (7 - dayOfWeek)) {
				week_c = 1;
			} else {
				if ((currentDay - (7 - dayOfWeek)) % 7 == 0) {
					week_c = (currentDay - (7 - dayOfWeek)) / 7 + 1;
				} else {
					week_c = (currentDay - (7 - dayOfWeek)) / 7 + 2;
				}
			}
		}
		currentWeek = week_c;
		getCurrent();

		//String lauarDay = LauarUtils.getLunar(year_c, month_c, day_c);
		//tvDate.setText(year_c + "年" + month_c + "月" + day_c + "日" );
		dateParam = year_c + "-" + month_c + "-" + day_c;
		dateAdapter = new DateAdapter(mContext, currentYear, currentMonth, currentWeek,
				currentWeek == 1 ? true : false);

		dayNumbers = dateAdapter.getDayNumbers();
			dateStrList = new ArrayList<String>();
			currentWeekDateStrList = new ArrayList<String>();
			for (int i = 0; i < 7; i++) {
				dateStrList.add(dateAdapter.getCurrentYear(i) + "-" + dateAdapter.getCurrentMonth(i)
				+ "-" + dayNumbers[i]);
				
				if (dateStrList.get(i).equals(currentDate)) {
					currentNumberTag = i;
				}
			}
			currentWeekDateStrList.addAll(dateStrList);
		
		
		addGridView();
		dateAdapter.dateTagList=dateStrList;
		gridView.setAdapter(dateAdapter);
		gridView.getHeight();
		gridView.getWidth();
		gridView.getChildAt(0);
		selectPostion = dateAdapter.getTodayPosition();
		gridView.setSelection(selectPostion);
		flipperView.addView(gridView, 0);
	}

	public void getCalendar(int year, int month) {
		isLeapyear = sc.isLeapYear(year);
		daysOfMonth = sc.getDaysOfMonth(isLeapyear, month);
		dayOfWeek = sc.getWeekdayOfMonth(year, month);
	}

	public void getCurrent() {
		if (currentWeek > currentNum) {
			if (currentMonth + 1 <= 12) {
				currentMonth++;
			} else {
				currentMonth = 1;
				currentYear++;
			}
			currentWeek = 1;
			currentNum = getWeeksOfMonth(currentYear, currentMonth);
		} else if (currentWeek == currentNum) {
			if (getLastDayOfWeek(currentYear, currentMonth) == 6) {
			} else {
				if (currentMonth + 1 <= 12) {
					currentMonth++;
				} else {
					currentMonth = 1;
					currentYear++;
				}
				currentWeek = 1;
				currentNum = getWeeksOfMonth(currentYear, currentMonth);
			}

		} else if (currentWeek < 1) {
			if (currentMonth - 1 >= 1) {
				currentMonth--;
			} else {
				currentMonth = 12;
				currentYear--;
			}
			currentNum = getWeeksOfMonth(currentYear, currentMonth);
			currentWeek = currentNum - 1;
		}
	}

	public int getWeeksOfMonth(int year, int month) {
		// 先判断某月的第一天为星期几
		int preMonthRelax = 0;
		int dayFirst = getWhichDayOfWeek(year, month);
		int days = sc.getDaysOfMonth(sc.isLeapYear(year), month);
		if (dayFirst != 7) {
			preMonthRelax = dayFirst;
		}
		if ((days + preMonthRelax) % 7 == 0) {
			weeksOfMonth = (days + preMonthRelax) / 7;
		} else {
			weeksOfMonth = (days + preMonthRelax) / 7 + 1;
		}
		return weeksOfMonth;

	}

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

	public int getWhichDayOfWeek(int year, int month) {
		return sc.getWeekdayOfMonth(year, month);

	}

	public int getLastDayOfWeek(int year, int month) {
		return sc.getWeekDayOfLastMonth(year, month, sc.getDaysOfMonth(isLeapyear, month));
	}

	public void setMoveEnable(){
		moveEnable = true;
		gridView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
	}
	
	private void addGridView() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		gridView = new GridView(mContext);
		gridView.setNumColumns(5);
		gridView.setGravity(Gravity.CENTER_VERTICAL);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setVerticalSpacing(1);
		gridView.setHorizontalSpacing(1);
		if(moveEnable){
			gridView.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return gestureDetector.onTouchEvent(event);
				}
			});
		}
		/*gridView.setOnItemClickListener(new OnItemClickListener() {

			

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int index = 8;
				if (isScroll&&currentWeekDateStrList.contains(currentDate)) {
					index = currentWeekDateStrList.indexOf(currentDate);
				}
				
				if (isScroll && position>index) {
					
				}else{
				selectPostion = position;
				dateAdapter.setSeclection(position);
				if (isScroll) {
					gridView.setAdapter(dateAdapter);
				}else {
					dateAdapter.notifyDataSetChanged();
				}
				
				//String lauarDay = LauarUtils.getLunar(dateAdapter.getCurrentYear(selectPostion),
						//dateAdapter.getCurrentMonth(selectPostion), Integer.parseInt(dayNumbers[position]));
				//tvDate.setText(
						//dateAdapter.getCurrentYear(selectPostion) + "年" + dateAdapter.getCurrentMonth(selectPostion)
								//+ "月" + dayNumbers[position] + "日" );
				dateParam = dateAdapter.getCurrentYear(selectPostion) + "-" + dateAdapter.getCurrentMonth(selectPostion)
						+ "-" + dayNumbers[position];

				mWeekCalendarListener.WeekCalendarClickListener(dateParam);
			}
			}
		});
		gridView.setLayoutParams(params);*/
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
		return listLong>currentLong ? true: false ;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (e1.getX() - e2.getX() > 80) {
			// 向左滑
			moveLeft();
			return true;

		} else if (e1.getX() - e2.getX() < -80) {
			moveRight();
			return true;
		}
		return false;
	}

	public void moveLeft(){
		gvFlag = 0;
		if (isScroll) {
		    dateAdapter = new DateAdapter(mContext, currentYear, currentMonth, currentWeek,
				currentWeek == 1 ? true : false);
		
			dateAdapter.dateTagList=currentWeekDateStrList;
			dateAdapter.dateTag=dateTag;
			dateAdapter.isScroll=isScroll;
			
			dateStrList = new ArrayList<String>();
			for (int i = 0; i < 7; i++) {
				
				dateStrList.add(dateParam = dateAdapter.getCurrentYear(i) + "-" + dateAdapter.getCurrentMonth(i)
				+ "-" + dayNumbers[i]);
			}
		}
		
		
		if (isScroll&&dateStrList.contains(currentDate)) {
			
		}else{
			if (isScroll ) {
				if (selectPostion>currentNumberTag && getTwoDay(currentDate,currentWeekDateStrList.get(selectPostion))<7) {
					selectPostion=currentNumberTag;
				}
			}
			
		addGridView();
		currentWeek++;
		getCurrent();
		dateAdapter = new DateAdapter(mContext, currentYear, currentMonth, currentWeek,
				currentWeek == 1 ? true : false);
		
		dayNumbers = dateAdapter.getDayNumbers();
		dateParam = dateAdapter.getCurrentYear(selectPostion) + "-" + dateAdapter.getCurrentMonth(selectPostion)
				+ "-" + dayNumbers[selectPostion];

		mWeekCalendarListener.WeekCalendarClickListener(dateParam);
		if (isScroll) {
			currentWeekDateStrList = new ArrayList<String>();
			for (int i = 0; i < 7; i++) {
				currentWeekDateStrList.add(dateAdapter.getCurrentYear(i) + "-" + dateAdapter.getCurrentMonth(i)
				+ "-" + dayNumbers[i]);
			}
			dateAdapter.dateTagList=currentWeekDateStrList;
			dateAdapter.dateTag=dateTag;
			dateAdapter.isScroll=isScroll;
		}
		
		gridView.setAdapter(dateAdapter);

		gvFlag++;
		flipperView.addView(gridView, gvFlag);
		dateAdapter.setSeclection(selectPostion);
		this.flipperView.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_left_in));
		this.flipperView.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_left_out));
		this.flipperView.showNext();
		flipperView.removeViewAt(0);
		}
	}
	
	public void moveRight(){
		gvFlag = 0;
		addGridView();
		currentWeek--;
		getCurrent();
		dateAdapter = new DateAdapter(mContext, currentYear, currentMonth, currentWeek,
				currentWeek == 1 ? true : false);
		dayNumbers = dateAdapter.getDayNumbers();
		
		dateStrList = new ArrayList<String>();
		for (int i = 0; i < 7; i++) {
			
			dateStrList.add(dateParam = dateAdapter.getCurrentYear(i) + "-" + dateAdapter.getCurrentMonth(i)
			+ "-" + dayNumbers[i]);
		}
		
		//String lauarDay = LauarUtils.getLunar(dateAdapter.getCurrentYear(selectPostion),
				//dateAdapter.getCurrentMonth(selectPostion), Integer.parseInt(dayNumbers[selectPostion]));
		//tvDate.setText(dateAdapter.getCurrentYear(selectPostion) + "年" + dateAdapter.getCurrentMonth(selectPostion)
				//+ "月" + dayNumbers[selectPostion] + "日");
		dateParam = dateAdapter.getCurrentYear(selectPostion) + "-" + dateAdapter.getCurrentMonth(selectPostion)
				+ "-" + dayNumbers[selectPostion];

		mWeekCalendarListener.WeekCalendarClickListener(dateParam);

		if (isScroll) {
			currentWeekDateStrList = new ArrayList<String>();
			for (int i = 0; i < 7; i++) {
				currentWeekDateStrList.add(dateAdapter.getCurrentYear(i) + "-" + dateAdapter.getCurrentMonth(i)
				+ "-" + dayNumbers[i]);
			}
			dateAdapter.dateTagList=currentWeekDateStrList;
			dateAdapter.dateTag=dateTag;
			dateAdapter.isScroll=isScroll;
		}
		
		gridView.setAdapter(dateAdapter);
		gvFlag++;
		flipperView.addView(gridView, gvFlag);
		dateAdapter.setSeclection(selectPostion);
		this.flipperView.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_right_in));
		this.flipperView.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_right_out));
		this.flipperView.showPrevious();
		flipperView.removeViewAt(0);
	}
	
	public void setOnChangeListener(WeekCalendarListener weekCalendarListener) {
		this.mWeekCalendarListener = weekCalendarListener;
	}

	
	 public static int getTwoDay(String sj1, String sj2) {
	        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
	        int day = 0;
	        try {
	            Date date = myFormatter.parse(sj1);
	            Date mydate = myFormatter.parse(sj2);
	            day = (int) ((date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000));
	        } catch (Exception e) {
	            return 0;
	        }
	        return day ;
	    }

	public void updateAdapter(String string, boolean b) {
		this.isScroll=b;
		this.dateTag=string;
		dateAdapter.dateTag=string;
		dateAdapter.isScroll=b;
		dateAdapter.notifyDataSetChanged();
	}
}
