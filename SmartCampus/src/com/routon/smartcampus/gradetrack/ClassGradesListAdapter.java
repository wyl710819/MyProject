package com.routon.smartcampus.gradetrack;

import java.util.ArrayList;

import org.apache.http.entity.mime.content.ContentBody;
import com.routon.edurelease.R;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ClassGradesListAdapter extends BaseAdapter{
	private ArrayList<CourseGrades> allCourseGrades;
	private Context context;
	private int locatePosition;
	private int type;
	private int appType;
	private int length;
	
	public ClassGradesListAdapter(Context context,ArrayList<CourseGrades> allCourseGrades,
			int locatePosition,int type,int appType)
	{
		this.context = context;
		this.allCourseGrades = allCourseGrades;
		this.locatePosition = locatePosition;
		this.type = type;
		this.appType = appType;
	}

	@Override
	public int getCount() {
		return getMaxCount();
	}

	@Override
	public Object getItem(int arg0) {
		return allCourseGrades.get(0).grades.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		View view = null;
		ViewHolder viewHolder = null;
		if(convertView == null)
		{
			view = LayoutInflater.from(context).inflate(R.layout.list_class_grades_item, arg2,false);
			viewHolder = new ViewHolder(view);
			view.setTag(viewHolder);
		}
		else
		{
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.mLinearLayout.removeAllViews();
		TextView tvIndex = (TextView) LayoutInflater.from(context).inflate(R.layout.txt_student_grade, null,false);
		tvIndex.setVisibility(View.VISIBLE);
		tvIndex.setText(String.valueOf(position+1));
		tvIndex.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
		LinearLayout.LayoutParams lpTv = new LinearLayout.LayoutParams(length, LinearLayout.LayoutParams.WRAP_CONTENT);    
		lpTv.setMargins(0, 0, 3, 0);    
		viewHolder.mLinearLayout.addView(tvIndex,lpTv);
		for(int i=0;i<allCourseGrades.size();i++)
		{
			TextView textView = (TextView) LayoutInflater.from(context).inflate(R.layout.txt_student_grade, null,false);
			textView.setVisibility(View.VISIBLE);
			if(type == ClassGradesActivity.ALL_COURSE && appType == SubjectExamActivity.FAMILY_TYPE)//综合成绩
			{
				length = (context.getResources().getDisplayMetrics().widthPixels-
						dip2px(context, 30)-12)/5;
				String grade = String.valueOf(allCourseGrades.get(i).grades.get(position).grade);
				if(position == locatePosition)
				{
					Spanned rank = Html.fromHtml(grade+"<font color='#b94645'>"+"/"+allCourseGrades.get(i).rank+"</font>");
					textView.setText(rank);
					textView.setBackgroundColor(Color.rgb(0xf9, 0xe3, 0xc0));
				}
				else
				{
					textView.setText(grade);
					textView.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
				}
			}
			else if(type == ClassGradesActivity.ONE_COURSE && appType == SubjectExamActivity.FAMILY_TYPE)//单科成绩
			{
				length = (context.getResources().getDisplayMetrics().widthPixels-
						dip2px(context, 30)-12)/5;
				String grade = allCourseGrades.get(i).grades.size()>position?
						String.valueOf(allCourseGrades.get(i).grades.get(position).grade):"-";
				if(position == allCourseGrades.get(i).rank-1)
				{
					Spanned rank = Html.fromHtml(grade+"<font color='#b94645'>"+"/"+allCourseGrades.get(i).rank+"</font>");
					textView.setText(rank);
					textView.setBackgroundColor(Color.rgb(0xf9, 0xe3, 0xc0));
				}
				else
				{
					textView.setText(grade);
					textView.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
				}
			}
			else if(type == ClassGradesActivity.ALL_COURSE && appType == SubjectExamActivity.SCHOOL_TYPE)
			{
				length = (context.getResources().getDisplayMetrics().widthPixels-
						dip2px(context, 111)-9)/4;
				String grade = String.valueOf(allCourseGrades.get(i).grades.get(position).grade);
				textView.setText(grade);
				textView.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
			}
			else if(type == ClassGradesActivity.ONE_COURSE && appType == SubjectExamActivity.SCHOOL_TYPE)
			{
				length = (context.getResources().getDisplayMetrics().widthPixels-
						dip2px(context, 111)-9)/4;
				String grade = allCourseGrades.get(i).grades.size()>position?
						String.valueOf(allCourseGrades.get(i).grades.get(position).grade):"-";
				textView.setText(grade);
				textView.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
			}
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(length, LinearLayout.LayoutParams.WRAP_CONTENT);    
			if(i != allCourseGrades.size()-1)
				lp.setMargins(0, 0, 3, 0);    
			viewHolder.mLinearLayout.addView(textView,lp);
		}
		return view;
	}
	
	class ViewHolder
	{
		LinearLayout mLinearLayout;
		public ViewHolder(View view)
		{
			mLinearLayout = (LinearLayout)view.findViewById(R.id.linearlayout_class_grade_item);
		}
	}
	
	public int getMaxCount()
	{
		int maxCount = 0;
		for(int i=0;i<allCourseGrades.size();i++)
		{
			CourseGrades courseGrades = allCourseGrades.get(i);
			if(courseGrades.grades.size()>maxCount)
				maxCount = courseGrades.grades.size();
		}
		return maxCount;
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f); // +0.5是为了向上取整
	}

}
