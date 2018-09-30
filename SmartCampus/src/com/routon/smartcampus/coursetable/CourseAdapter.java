package com.routon.smartcampus.coursetable;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.routon.edurelease.R;

public class CourseAdapter extends BaseAdapter{

	private List<TeacherCourseBean.MyCourse> courseList;
	private Context mContext;
	private List<TeacherCourseBean.MyCourse> mList=new ArrayList<TeacherCourseBean.MyCourse>();
	private String sid;
	
	public CourseAdapter(List<TeacherCourseBean.MyCourse> courseList,Context context, String sid){
		mContext=context;
		this.courseList=courseList;
		this.sid = sid;
		for(int i=0;i<4;i++){
			mList.add(null);
		}
		for(int i=0;i<4;i++){
			for(int j=0;j<courseList.size();j++){
				if(courseList.get(j).lesson<=4){
					if(courseList.get(j).lesson-1==i){
						mList.set(i, courseList.get(j));
					}
				}else{
					if(courseList.get(j).lesson-1-4==i){
						mList.set(i, courseList.get(j));
					}
				}
			}
		}
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return courseList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		
		if(convertView==null){
			holder=new ViewHolder();
			convertView=LayoutInflater.from(mContext).inflate(R.layout.item_teacher_course_detail, null);
			holder.courseName=(TextView) convertView.findViewById(R.id.tv_course_name);
			holder.teacherName=(TextView) convertView.findViewById(R.id.tv_teacher_name);
			holder.courseTime=(TextView)convertView.findViewById(R.id.tv_course_time);
			holder.linearRoot=(LinearLayout) convertView.findViewById(R.id.course_detail_root);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}
		if(mList!=null&&mList.get(position)!=null){
			
			holder.courseName.setText(mList.get(position).course);
			if(!TextUtils.isEmpty(mList.get(position).teacherName)){
				holder.teacherName.setText(mList.get(position).teacherName);
			}else if(!TextUtils.isEmpty(mList.get(position).className)) {
				holder.teacherName.setText(mList.get(position).className);
			}
			holder.courseTime.setText(mList.get(position).classTime);
			if(mList.get(position).ampm==0){
				if(mList.get(position).sid.equals(sid)){
					holder.linearRoot.setBackgroundResource(R.drawable.teacher_course_select_blue_rectangle_shape);
				}else {
					holder.linearRoot.setBackgroundResource(R.drawable.teahcer_course_blue_rectangle_shape);
				}
			}else{
				if(mList.get(position).sid.equals(sid)){
					holder.linearRoot.setBackgroundResource(R.drawable.teacher_course_select_yellow_rectangle_shape);
				}else {
					holder.linearRoot.setBackgroundResource(R.drawable.teahcer_course_yellow_rectangle_shape);
				}
			}
			
		}else{
			holder.teacherName.setText("");
			holder.courseTime.setText("");
			holder.courseName.setText("");
			holder.linearRoot.setBackgroundColor(Color.TRANSPARENT);
		}
		
		return convertView;
	}
	private static class ViewHolder{
		TextView courseName;
		TextView teacherName;
		TextView courseTime;
		LinearLayout linearRoot;
	}

}
