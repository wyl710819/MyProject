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

public class ReplaceTeacherAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<ReplaceTeacherBean> mDatas=new ArrayList<ReplaceTeacherBean>();

	public ReplaceTeacherAdapter(Context context,List<ReplaceTeacherBean> datas){
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.replace_teacher_item, null);
			holder.itemTeacher = (TextView) convertView.findViewById(R.id.leave_dialog_item_teacher);
			holder.itemType = (TextView) convertView.findViewById(R.id.leave_dialog_item_type);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ReplaceTeacherBean bean=mDatas.get(position);
		
		if (bean.teacherType==0) {
			holder.itemTeacher.setText(bean.teacherClass+" "+bean.teacherName);
			holder.itemType.setText("空闲");
			holder.itemTeacher.setTextColor(mContext.getResources().getColor(R.color.leave_text_green));
			holder.itemType.setTextColor(mContext.getResources().getColor(R.color.leave_text_green));
		}else if (bean.teacherType==1) {
			holder.itemTeacher.setText(bean.teacherClass+" "+bean.teacherName);
			holder.itemType.setText("有课");
			holder.itemTeacher.setTextColor(mContext.getResources().getColor(R.color.leave_text_grey));
			holder.itemType.setTextColor(mContext.getResources().getColor(R.color.leave_text_grey));
		}

		return convertView;
        
    }
    private class ViewHolder {
		public TextView itemTeacher;
		public TextView itemType;

	}
    

}
