package com.routon.smartcampus.gradetrack;

import java.util.ArrayList;
import java.util.List;

import com.routon.edurelease.R;
import com.routon.smartcampus.bean.ClassAllGradeBean;
import com.routon.smartcampus.bean.ClassCourseExamsDataBean;
import com.routon.smartcampus.bean.ClassExamBean;
import com.routon.smartcampus.bean.ClassExamsDataBean;
import com.routon.smartcampus.utils.MyBundleName;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class SubjectExamListAdapter extends BaseAdapter{
	private Context mContext;
	public static List<String> titles = new ArrayList<String>();
	public List<ClassExamBean> classExams;
	private List<ClassCourseExamsDataBean> classCourseExamsDataBeans;
	public List<String> contents = new ArrayList<String>();
	private SharedPreferences mSharedPreferences;
	public SubjectExamListAdapter(Context context,List<ClassExamBean> classExams,List<ClassCourseExamsDataBean> classCourseExamsDataBeans)
	{
		mContext = context;
		this.classExams = classExams;
		this.classCourseExamsDataBeans = classCourseExamsDataBeans;
		initData();
		mSharedPreferences = mContext.getSharedPreferences("subjectitemclick", mContext.MODE_PRIVATE);
	}

	private void initData()
	{
		titles.clear();
		contents.clear();
		if(classExams != null)
		{
			if(classExams.size()>0)
			{
				for(int i=0;i<classExams.size();i++)
				{
					titles.add(classExams.get(i).examTime.split(" ")[0]+" 综合");
					contents.add(classExams.get(i).examName+"成绩");
				}
			}
		}
		if(classCourseExamsDataBeans != null)
		{
			if(classCourseExamsDataBeans.size()>0)
			{
				for(int i=0;i<classCourseExamsDataBeans.size();i++)
				{
					titles.add(classCourseExamsDataBeans.get(i).courseName);
					contents.add(classCourseExamsDataBeans.get(i).classExams.get(0).examTime.split(" ")[0]+"更新");
				}
			}
		}
	}
	
	@Override
	public int getCount() {
		return titles.size();
	}

	@Override
	public Object getItem(int arg0) {
		return titles.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View contentView, ViewGroup arg2) {
		View view = null;
		ViewHolder viewHolder = null;
		if(contentView == null)
		{
			view = LayoutInflater.from(mContext).inflate(R.layout.list_subject_exam_item, arg2,false);
			viewHolder = new ViewHolder(view);
			view.setTag(viewHolder);
		}
		else {
			view = contentView;
			viewHolder = (ViewHolder) view.getTag();
		}
		TextPaint paint = viewHolder.txtTitle.getPaint();
		paint.setFakeBoldText(true); 
		boolean isClick = mSharedPreferences.getBoolean(titles.get(arg0)+contents.get(arg0), false);
		SubjectExamActivity activity = (SubjectExamActivity)mContext;
		boolean isSchool = (activity.mAppType == SubjectExamActivity.SCHOOL_TYPE);
		if(isClick && !isSchool)
		{
			viewHolder.imgNew.setVisibility(View.INVISIBLE);
			viewHolder.txtTitle.setTextColor(Color.parseColor("#aea097"));
			viewHolder.txtContent.setTextColor(Color.parseColor("#aea097"));
		}
		else if(!isClick && !isSchool) {
			viewHolder.imgNew.setVisibility(View.VISIBLE);
			viewHolder.txtTitle.setTextColor(Color.parseColor("#000000"));
			viewHolder.txtContent.setTextColor(Color.parseColor("#000000"));
		}
		else {
			viewHolder.imgNew.setVisibility(View.INVISIBLE);
			viewHolder.txtTitle.setTextColor(Color.parseColor("#000000"));
			viewHolder.txtContent.setTextColor(Color.parseColor("#000000"));
		}
		viewHolder.txtTitle.setText(titles.get(arg0));
		viewHolder.txtContent.setText(contents.get(arg0));
		return view;
	}

	class ViewHolder
	{
		TextView txtTitle;
		TextView txtContent;
		ImageView imgNextArrow;
		ImageView imgNew;
		public ViewHolder(View view)
		{
			txtTitle = (TextView)view.findViewById(R.id.txt_subject_exam_title);
			txtContent = (TextView)view.findViewById(R.id.txt_subject_exam_content);
			imgNextArrow = (ImageView)view.findViewById(R.id.img_exam_next);
			imgNew = (ImageView)view.findViewById(R.id.img_exam_new);
		}
	}
	
}
