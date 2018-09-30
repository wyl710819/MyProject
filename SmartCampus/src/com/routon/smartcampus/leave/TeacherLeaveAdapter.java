package com.routon.smartcampus.leave;

import java.util.ArrayList;
import java.util.List;

import com.routon.edurelease.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TeacherLeaveAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<TeacherLeaveBean> mDatas=new ArrayList<TeacherLeaveBean>();

	public TeacherLeaveAdapter(Context context,List<TeacherLeaveBean> datas){
		this.mContext=context;
		this.mDatas=datas;
	}

	@Override
	public int getCount() {
		return mDatas==null ? 0 : mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.teacher_leave_item, null);
			holder.leaveItemTime = (TextView) convertView.findViewById(R.id.leave_item_time);
			holder.leaveItemTeacherName = (TextView) convertView.findViewById(R.id.leave_item_teacher_name);
			holder.leaveItemType = (TextView) convertView.findViewById(R.id.leave_item_type);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		TeacherLeaveBean bean=mDatas.get(position);
		holder.leaveItemTime.setText(bean.startTime+"~"+bean.endTime);
		if (bean.catalog==0) {
			holder.leaveItemType.setText("请假");
			holder.leaveItemType.setTextColor(mContext.getResources().getColor(R.color.leave_text_red));
		}else {
			holder.leaveItemType.setText("出差");
			holder.leaveItemType.setTextColor(mContext.getResources().getColor(R.color.leave_text_green));
		}
		
		String teacherStr=listToString(bean.agentTeachers);
		if (bean.status==0) {
			holder.leaveItemTeacherName.setText("(待审批)"+teacherStr);
		}else if (bean.status==1) {
			holder.leaveItemTeacherName.setText("(已批准)"+teacherStr);
		}else if (bean.status==2) {
			holder.leaveItemTeacherName.setText("(已驳回)"+teacherStr);
		}
		

		return convertView;
        
    }
    private class ViewHolder {
		public TextView leaveItemTime;
		public TextView leaveItemTeacherName;
		public TextView leaveItemType;

	}
    
    private String listToString(List<String> list) {
    	String str="";
    	for (int i = 0; i < list.size(); i++) {
    		str+=i == 0 ? list.get(i) : "、"+list.get(i);
		}
    	if (list.size()>0) {
    		return "-"+str+"代课";
		}else {
			return "";
		}
		
    	
    }

}
