package com.routon.smartcampus.student;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.routon.edurelease.R;
import com.routon.edurelease.R.color;
import com.routon.inforelease.util.TimeUtils;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.utils.FlowerUtil;
import com.routon.smartcampus.utils.StudentHelper;
import com.routon.smartcampus.view.InitialIndexGridView;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StudentItemAdapter extends BaseAdapter implements OnScrollListener, IPinnedHeader, Filterable{

	private static final int TYPE_ITEM = 0;
	private static final int TYPE_SECTION = 1;
	private static final int TYPE_MAX_COUNT = TYPE_SECTION + 1;
	
	public static final int ITEM_TYPE_BADGE = 0;
	public static final int ITEM_TYPE_ATTENDANCE = 1;
	public static final int ITEM_TYPE_GRADE = 2;
	public int mItemType = ITEM_TYPE_BADGE;

	LayoutInflater mLayoutInflater;
	int mCurrentSectionPosition = 0, mNextSectionPostion = 0;

	ArrayList<Integer> mListSectionPos;

	ArrayList<String> mListItems;

	Context mContext;
	private Filter mFilter;
	private ArrayList<StudentBean> mStudentDataList;
	private boolean listType;
//	public List<View> views;
//	private List<Integer> positionList;
//	private ImageLoader mImageLoader = null;
	
	public boolean showMultiSelector = false;
	
	public OnStudentBeanChangeListener listener;

	public StudentItemAdapter(Context context, ArrayList<String> listItems,
			ArrayList<Integer> listSectionPos,Filter filter, ArrayList<StudentBean> studentDataList,
			boolean listType, OnStudentBeanChangeListener listener) {
		this.mFilter = filter;
		if (context!=null) {
			this.mContext = context;
		}
		this.mListItems = listItems;
		this.mListSectionPos = listSectionPos;
		this.mStudentDataList = studentDataList;
		this.listType = listType;
		this.listener = listener;
//		this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		this.mLayoutInflater = LayoutInflater.from(context);
//		views = new ArrayList<View>();
//		positionList = new ArrayList<Integer>();
	}

	@Override
	public int getCount() {
		if( mStudentDataList == null ) return 0;
		return mStudentDataList.size();
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_MAX_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		if( mListSectionPos == null ) return 0;
		return mListSectionPos.contains(position) ? TYPE_SECTION : TYPE_ITEM;
	}

	@Override
	public Object getItem(int position) {
		if( mListItems == null ) return null;
		return mListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		if( mListItems == null ) return 0;
		return mListItems.get(position).hashCode();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        final View selfView;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item_view, null);
			holder.imgView = (ImageView) convertView.findViewById(R.id.item_img);
			holder.dotView = (ImageView) convertView.findViewById(R.id.green_dot_view);
			
			holder.infoView = (TextView)convertView.findViewById(R.id.item_info);
			holder.nameView = (TextView) convertView.findViewById(R.id.item_name);
			holder.badgeExchangeNum = (TextView) convertView.findViewById(R.id.item_badge_num);
			holder.badgeNum = (TextView) convertView.findViewById(R.id.item_badge_num2);
			holder.badgeInfoRrl=(RelativeLayout) convertView.findViewById(R.id.badge_info_rl);
			holder.mAgencyView = convertView.findViewById(R.id.item_agency);
			holder.itemRateText = (TextView) convertView.findViewById(R.id.item_rate_text);
			
			holder.selectorBtn = (ImageButton) convertView.findViewById(R.id.item_selector);
			holder.lowPowerIv = (ImageView) convertView.findViewById(R.id.low_power_iv);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		selfView = convertView;
		
		View badgeInfo = convertView.findViewById(R.id.badge_info_rl);
		if( mItemType == ITEM_TYPE_BADGE ){
			holder.infoView.setVisibility(View.GONE);
			if( badgeInfo != null ){
				badgeInfo.setVisibility(View.VISIBLE);
			}
		}else if( mItemType == ITEM_TYPE_ATTENDANCE ){
			holder.infoView.setVisibility(View.VISIBLE);
			if( badgeInfo != null ){
				badgeInfo.setVisibility(View.GONE);
			}
		}else if( mItemType == ITEM_TYPE_GRADE){
			holder.infoView.setVisibility(View.GONE);
			if( badgeInfo != null ){
				badgeInfo.setVisibility(View.GONE);
			}
		}
		
		final StudentBean studentBean = mStudentDataList.get(position);
		StudentHelper.loadStudentImage(studentBean.imgSavePath, null, mContext, holder.imgView);

		if(showMultiSelector){
			holder.selectorBtn.setVisibility(View.VISIBLE);
			if(studentBean.isSelect){
				holder.selectorBtn.setImageResource(R.drawable.checkbox_pressed);
			}else{
				holder.selectorBtn.setImageResource(R.drawable.checkbox_normal);
			}
			
			holder.selectorBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					studentBean.isSelect = !studentBean.isSelect;
					studentBean.currentIndex = position;
					ImageButton btn = (ImageButton)v;
					if(studentBean.isSelect){
						btn.setImageResource(R.drawable.checkbox_pressed);
					}else{
						btn.setImageResource(R.drawable.checkbox_normal);
					}
					listener.onSelect(studentBean, position);
				}
			});
			
			//低电量图标显示
			if (studentBean.battery!=null && studentBean.battery.equals("0")) {
				holder.lowPowerIv.setVisibility(View.VISIBLE);
			}else {
				holder.lowPowerIv.setVisibility(View.GONE);
			}
			
			
		}else{
			holder.selectorBtn.setVisibility(View.GONE);
			holder.lowPowerIv.setVisibility(View.GONE);
		}
		
		holder.imgView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.onClicked(studentBean, position, selfView);
			}
		});
		
		if (listType) {
			holder.badgeInfoRrl.setVisibility(View.GONE);
			if (studentBean.isCheck) {
				holder.nameView.setTextColor(mContext.getResources().getColor(color.text_green_color));
			}else {
				holder.nameView.setTextColor(mContext.getResources().getColor(color.homework_text_color));
			}
			if (studentBean.rateStr==null || studentBean.rateStr.equals("") ||studentBean.rateStr.equals("null")) {
				if (studentBean.homeworkRate==1) {
					holder.mAgencyView.setBackgroundResource(R.drawable.ic_homework_excellent);
					holder.mAgencyView.setVisibility(View.VISIBLE);
				}else if(studentBean.homeworkRate==2){
					holder.mAgencyView.setBackgroundResource(R.drawable.ic_homework_good);
					holder.mAgencyView.setVisibility(View.VISIBLE);
				}else if(studentBean.homeworkRate==3){
					holder.mAgencyView.setBackgroundResource(R.drawable.ic_homework_general);
					holder.mAgencyView.setVisibility(View.VISIBLE);
				}else if(studentBean.homeworkRate==4){
					holder.mAgencyView.setBackgroundResource(R.drawable.ic_homework_weak);
					holder.mAgencyView.setVisibility(View.VISIBLE);
				}else if(studentBean.homeworkRate==5){
					holder.mAgencyView.setBackgroundResource(R.drawable.ic_homework_unfinished);
					holder.mAgencyView.setVisibility(View.VISIBLE);
				}else if(studentBean.homeworkRate==6){
					holder.mAgencyView.setBackgroundResource(R.drawable.ic_homework_remark);
					holder.mAgencyView.setVisibility(View.VISIBLE);
				}else {
					holder.mAgencyView.setVisibility(View.GONE);
					holder.itemRateText.setVisibility(View.GONE);
				}
			}else {
				holder.mAgencyView.setBackgroundResource(R.drawable.ic_homework_rate);
				holder.mAgencyView.setVisibility(View.VISIBLE);
				if (studentBean.rateStr.length()==1) {
					holder.itemRateText.setTextSize(sp2px(7));
				}else if (studentBean.rateStr.length()==2) {
					holder.itemRateText.setTextSize(sp2px(5));
				}else if (studentBean.rateStr.length()==3) {
					holder.itemRateText.setTextSize(sp2px(4));
				}
				holder.itemRateText.setText(studentBean.rateStr);
				holder.itemRateText.setVisibility(View.VISIBLE);
			}
			
		}else {
			if( studentBean.isStaffUserAgent == true ){
				holder.mAgencyView.setBackgroundResource(R.drawable.student_v);
				holder.mAgencyView.setVisibility(View.VISIBLE);
			}else{
				holder.mAgencyView.setVisibility(View.GONE);
			}
		}
		
		if( studentBean.lastLoginTime != null && studentBean.lastLoginTime.isEmpty() == false && studentBean.parentPhone != 0 ){
			Calendar loginCalendar = TimeUtils.getFormatCalendar(studentBean.lastLoginTime, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
			if( loginCalendar != null ){
				Calendar curCalendar = Calendar.getInstance();
				Calendar tmpCalendar =  Calendar.getInstance();
				tmpCalendar.setTime(loginCalendar.getTime());
				tmpCalendar.add(Calendar.DATE, 3);
				boolean isAfterThreeDays = curCalendar.after(tmpCalendar);
				tmpCalendar.setTime(loginCalendar.getTime());
				tmpCalendar.add(Calendar.DATE, 7);
				boolean isAfterSevenDays = curCalendar.after(tmpCalendar);
				GradientDrawable mm= (GradientDrawable)holder.dotView.getBackground(); 
				if( isAfterThreeDays == false ){
					mm.setColor(Color.GREEN);
				}else if( isAfterSevenDays == true ){
					mm.setColor(Color.RED);
				}else{
					mm.setColor(Color.YELLOW);
				}
				holder.dotView.setVisibility(View.VISIBLE);
			}else{
				holder.dotView.setVisibility(View.GONE);
			}
			
		}else{
			holder.dotView.setVisibility(View.GONE);
		}
		
		holder.nameView.setText(studentBean.empName);
		holder.badgeExchangeNum.setText(String.valueOf(studentBean.bonuspoints)+"分");
		holder.badgeNum.setText("/"+studentBean.badgeCount);
		
		holder.infoView.setText("缺勤"+studentBean.absenceCount+"次");
		
//		Log.d("InitialIndexAdapter","getView position:"+position);
//		if (positionList.size()>0) {
//			if (!positionList.contains(position)) {
//				positionList.add(position);
//				views.add(convertView);
//			}
//		}else{
//			positionList.add(position);
//			views.add(convertView);
//		}
		
		return convertView;
	}

	@Override
	public int getPinnedHeaderState(int position) {
		if( mListSectionPos == null ) return 0;
		if (getCount() == 0 || position < 0 || mListSectionPos.indexOf(position) != -1) {
			return PINNED_HEADER_GONE;
		}

		mCurrentSectionPosition = getCurrentSectionPosition(position);
		mNextSectionPostion = getNextSectionPosition(mCurrentSectionPosition);
		if (mNextSectionPostion != -1 && position == mNextSectionPostion - 1) {
			return PINNED_HEADER_PUSHED_UP;
		}

		return PINNED_HEADER_VISIBLE;
	}

	public int getCurrentSectionPosition(int position) {
		if( mListItems == null ) return 0;
		String listChar = mListItems.get(position).toString().substring(0, 1).toUpperCase(Locale.getDefault());
		return mListItems.indexOf(listChar);
	}

	public int getNextSectionPosition(int currentSectionPosition) {
		if( mListItems == null ) return 0;
		int index = mListSectionPos.indexOf(currentSectionPosition);
		if ((index + 1) < mListSectionPos.size()) {
			return mListSectionPos.get(index + 1);
		}
		return mListSectionPos.get(index);
	}

	@Override
	public void configurePinnedHeader(View v, int position) {
		if( mListItems == null ) return;
		TextView header = (TextView) v;
		mCurrentSectionPosition = getCurrentSectionPosition(position);
		header.setText(mListItems.get(mCurrentSectionPosition));
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
		if (view instanceof InitialIndexGridView) {
			((InitialIndexGridView) view).configureHeaderView(firstVisibleItem);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}
	
	public boolean isShowMulitSelector(){
		return showMultiSelector;
	}
	
	public void enableMultiSelector(boolean enable){
		showMultiSelector = enable;
	}
	
	public void setOnStudentBeanChangeListener(OnStudentBeanChangeListener listener) {
		this.listener = listener;
	}

	public static class ViewHolder {
		public ImageView lowPowerIv;
		public TextView nameView;
		public ImageView imgView;
		public ImageView dotView;
		public TextView infoView;
		public TextView badgeNum;
		public View mAgencyView;
		public TextView badgeExchangeNum;
		public RelativeLayout badgeInfoRrl;
		public ImageButton selectorBtn;
		public TextView itemRateText;
	}
	
	private int sp2px(int value) {
		float v = mContext.getResources().getDisplayMetrics().scaledDensity;
		return (int) (v * value + 0.5f);
	}
}
