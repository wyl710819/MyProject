package com.routon.smartcampus.coursetable;

import java.util.ArrayList;
import java.util.List;

import com.routon.edurelease.R;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CourseDetialAdapter extends BaseAdapter{
	
	private ArrayList<String>courseList;
	private Context mContext;
	private List<Integer> positionList;
	private String appType=null;
	
	
	 public  CourseDetialAdapter(Context mContext, ArrayList<String>mList,List<Integer> positionList,String appType){
		 this.mContext=mContext;
		 this.courseList=mList;
		 this.positionList=positionList;
		 this.appType=appType;
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
			holder = new ViewHolder();
			convertView=LayoutInflater.from(mContext).inflate(R.layout.list_course_detail_item, null);
			
			holder.courseTv=(TextView) convertView.findViewById(R.id.coursedetail_tv);
			
			
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.courseTv.setTextColor(Color.GRAY);
		if(appType!=null){
			if(positionList!=null&&positionList.size()>0){
				for(int i=0;i<positionList.size();i++){
					if(positionList.get(i)==position){
						holder.courseTv.setTextColor(Color.parseColor("#65340B"));
					}
				}
			}else{
				holder.courseTv.setTextColor(Color.GRAY);
			}
		}else{
			holder.courseTv.setTextColor(Color.parseColor("#65340B"));
		}
		
		
		holder.courseTv.setText(courseList.get(position));
		
		
		return convertView;
	}
	private static class ViewHolder {
		
		 TextView courseTv;
	}

}
