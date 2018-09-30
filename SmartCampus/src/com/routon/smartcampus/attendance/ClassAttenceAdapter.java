package com.routon.smartcampus.attendance;

import java.util.List;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.routon.edurelease.R;
import com.routon.smartcampus.coursetable.TeacherCourseBean;
import com.routon.smartcampus.coursetable.TeacherCourseBean.MyCourse;

public class ClassAttenceAdapter extends BaseAdapter{


	private List<TeacherCourseBean.MyCourse> courseList;
	private Context mContext;
	private String sid;
	private int classGroupId;
	
	public ClassAttenceAdapter(List<TeacherCourseBean.MyCourse> courseList,Context context, String sid,
			int classGroupId){
		mContext=context;
		this.courseList=courseList;
		this.sid = sid;
		this.classGroupId = classGroupId;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return courseList.size();
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
		final MyCourse course = courseList.get(position);
		holder.courseName.setText(course.course);
		if(!TextUtils.isEmpty(course.teacherName)){
			holder.teacherName.setText(course.teacherName);
		}else if(!TextUtils.isEmpty(course.className)) {
			holder.teacherName.setText(course.className);
		}
		if(course.absence == 0){
			holder.courseTime.setText("");
			holder.courseTime.setTextColor(Color.WHITE);
		}else {
			holder.courseTime.setText("·缺"+course.absence+"人");
			holder.courseTime.setTextColor(Color.RED);
		}
		if(course.ampm==0){
			if(course.sid.equals(sid)){
				holder.linearRoot.setBackgroundResource(R.drawable.teacher_course_select_blue_rectangle_shape);
			}else {
				holder.linearRoot.setBackgroundResource(R.drawable.teahcer_course_blue_rectangle_shape);
			}
		}else{
			if(course.sid.equals(sid)){
				holder.linearRoot.setBackgroundResource(R.drawable.teacher_course_select_yellow_rectangle_shape);
			}else {
				holder.linearRoot.setBackgroundResource(R.drawable.teahcer_course_yellow_rectangle_shape);
			}
		}	
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(course.absence == 0){
					return;
				}
				Intent intent = new Intent(mContext, ShowClassAbsenceAvtivity.class);
				intent.putExtra("absence", course.absence);
				intent.putExtra("day", course.day);
				intent.putExtra("class", "第"+course.lesson+"节课 "+course.course);
				intent.putExtra("lesson", course.lesson);
				intent.putExtra("groupId", classGroupId);
				mContext.startActivity(intent);
			}
		});
		return convertView;
	}
	private static class ViewHolder{
		TextView courseName;
		TextView teacherName;
		TextView courseTime;
		LinearLayout linearRoot;
	}

}
