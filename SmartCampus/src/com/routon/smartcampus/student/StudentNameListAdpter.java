package com.routon.smartcampus.student;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.routon.edurelease.R;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.flower.Badge;
import com.routon.smartcampus.flower.BadgeType;
import com.routon.smartcampus.utils.FlowerUtil;
import com.routon.smartcampus.utils.StudentHelper;

public class StudentNameListAdpter extends BaseAdapter {

	private Context mContext;
	private ArrayList<StudentBean> mStudentBadgeCountBeanList;
	private Badge mBadge;
	private BadgeType mBadgeType;
	private Typeface mDigtalFontFace = null;
	
	public boolean showStudentImage = true;
	public boolean showStudentFullName = true;
	public int focusStudentId = -1;
	public boolean isShowName=false;

	public StudentNameListAdpter(Context context, ArrayList<StudentBean> mStudentBadgeCountBeanList) {
		this.mStudentBadgeCountBeanList = mStudentBadgeCountBeanList;
		this.mContext = context;
		this.mBadge =null;
	}

	public StudentNameListAdpter(Context context, ArrayList<StudentBean> mStudentBadgeCountBeanList,
			Badge badge) {
		this.mStudentBadgeCountBeanList = mStudentBadgeCountBeanList;
		this.mContext = context;
		this.mBadge = badge;
	}

	public StudentNameListAdpter(Context context, ArrayList<StudentBean> mStudentBadgeCountBeanList,
			BadgeType badgeType) {
		this.mStudentBadgeCountBeanList = mStudentBadgeCountBeanList;
		this.mContext = context;
		this.mBadgeType = badgeType;
		this.mBadge =null;
	}

	@Override
	public int getCount() {
		return mStudentBadgeCountBeanList.size();
	}

	@Override
	public Object getItem(int position) {
		return mStudentBadgeCountBeanList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View contentView, ViewGroup viewGroup) {
		final ViewHolder holder;
		if (contentView == null) {
			holder = new ViewHolder();
			contentView = View.inflate(mContext, R.layout.student_name_list_item, null);
			holder.name_text = (TextView) contentView.findViewById(R.id.name_text);
			holder.student_img = (ImageView) contentView.findViewById(R.id.student_img);
            holder.points_text=(TextView) contentView.findViewById(R.id.points_text);
			holder.taxis_text = (TextView) contentView.findViewById(R.id.taxis_text);
			holder.badge_img = (ImageView) contentView.findViewById(R.id.badge_img);

			contentView.setTag(holder);
		} else {
			holder = (ViewHolder) contentView.getTag();
		}

		if(showStudentImage){
			holder.student_img.setVisibility(View.VISIBLE);
		}else{
			holder.student_img.setVisibility(View.GONE);
		}
		
		String str = mStudentBadgeCountBeanList.get(position).empName;
		int bonuspoints=0;
		if (mBadge!=null) {
			bonuspoints = mStudentBadgeCountBeanList.get(position).subclassPoint;
		}else {
			bonuspoints = mStudentBadgeCountBeanList.get(position).bonuspoints;
		}
		
		int num = mStudentBadgeCountBeanList.get(position).badgeCount;
		int ranking = mStudentBadgeCountBeanList.get(position).ranking;
		if (ranking >= 1 && ranking <= 9 && bonuspoints>0) {
			if (mDigtalFontFace == null)
				mDigtalFontFace = Typeface.createFromAsset(mContext.getAssets(), "number.ttf");
			holder.taxis_text.setTypeface(mDigtalFontFace, Typeface.ITALIC);
			holder.taxis_text.setTextSize(24);
			holder.taxis_text.setTextColor(Color.RED);
			
		} else {
			holder.taxis_text.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
			holder.taxis_text.setTextSize(18);
			holder.taxis_text.setTextColor(Color.BLACK);
		}
		holder.taxis_text.setText(String.valueOf(ranking ));

		if (mBadge != null) {
			FlowerUtil.loadFlower(mContext, holder.badge_img, mBadge.name, mBadge.imgUrl);
		}else {
			holder.badge_img.setImageResource(R.drawable.flower);
		}
		
		StudentHelper.loadStudentImage(mStudentBadgeCountBeanList.get(position).imgSavePath,mStudentBadgeCountBeanList.get(position).imgUrl,
				mContext, holder.student_img);
		
		holder.name_text.getPaint().setFakeBoldText(false);
		holder.name_text.setTextSize(16);
		contentView.setBackgroundColor(Color.TRANSPARENT);
		if(showStudentFullName){
			holder.name_text.setText(mStudentBadgeCountBeanList.get(position).empName);
		}else{
			if(focusStudentId == mStudentBadgeCountBeanList.get(position).sid){
				holder.name_text.setText(mStudentBadgeCountBeanList.get(position).empName);
				holder.name_text.getPaint().setFakeBoldText(true);
				holder.name_text.setTextSize(18);
				contentView.setBackgroundColor(Color.rgb(11, 184, 234));
			}else{
				
				if (isShowName) {
					holder.name_text.setText(mStudentBadgeCountBeanList.get(position).empName);
				}else {
					holder.name_text.setText(mStudentBadgeCountBeanList.get(position).empName.substring(0, 1) + "**");
				}
				
			}
		}
		setClassBadgeText(holder.points_text, mStudentBadgeCountBeanList.get(position).badgeCount, bonuspoints);
//		holder.points_text.setText("共"+mStudentBadgeCountBeanList.get(position).badgeCount+"枚  "+mStudentBadgeCountBeanList.get(position).bonuspoints+"分");
		return contentView;
	}

	private class ViewHolder {
		public TextView name_text;
		public ImageView student_img;
		public TextView taxis_text;
		public ImageView badge_img;
		public TextView points_text;
	}

	private void setClassBadgeText(TextView name_text, int badgeCount, int badgePoints) {
		String info = mContext.getResources().getString(R.string.student_badge_points);
		String infotext = String.format(info, badgeCount +"", badgePoints+"");
		
		int index[] = new int[2];
		index[0] = infotext.indexOf(badgeCount +"");
		index[1] = infotext.indexOf(badgePoints+"");
		
		// String stSizeS = String.valueOf(badgeNum);
		// index[1] = infotext.indexOf(stSizeS);

		// int markTextSize =
		// getResources().getDimensionPixelSize(R.dimen.student_badge_mark_text_size);
		SpannableStringBuilder style = new SpannableStringBuilder(infotext);

		// style.setSpan(new
		// ForegroundColorSpan(Color.BLUE),index[0],index[0]+studentName.length(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		style.setSpan(new ForegroundColorSpan(Color.RED), index[0], index[0] + (badgeCount+"").length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		// style.setSpan(new AbsoluteSizeSpan(markTextSize),0,index[0],
		// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// style.setSpan(new
		// AbsoluteSizeSpan(markTextSize),0,index[1],Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		name_text.setText(style);
	}
}
