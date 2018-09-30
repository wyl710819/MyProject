package com.routon.smartcampus.leave;

import java.util.ArrayList;
import java.util.List;

import com.routon.edurelease.R;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CouserReplaceAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<CourseReplaceBean> mDatas=new ArrayList<CourseReplaceBean>();

	public CouserReplaceAdapter(Context context,List<CourseReplaceBean> datas){
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

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.teacher_couser_replace_item, null);
			holder.couserTime = (TextView) convertView.findViewById(R.id.couser_item_time);
			holder.couserReplaceCouser = (TextView) convertView.findViewById(R.id.couser_replace_item_couser);
			holder.teacherBtn = (TextView) convertView.findViewById(R.id.teacher_btn);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		CourseReplaceBean bean=mDatas.get(position);
		
		holder.couserTime.setText(bean.courseTime);
		if (bean.replaceTeacherName==null || bean.replaceTeacherName.equals("")) {
			holder.couserReplaceCouser.setText(bean.className+" "+bean.courseName);
		}else {
			holder.couserReplaceCouser.setText(bean.className+" "+bean.courseName+" "+bean.replaceTeacherName+"老师代课");
		}
		
		
		holder.teacherBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (onBtnListener!=null) {
					onBtnListener.onBtnClick(position);
				}
				
			}
		});

		return convertView;
        
    }
    private class ViewHolder {
		public TextView couserTime;
		public TextView couserReplaceCouser;
		public TextView teacherBtn;

	}
    
    public interface OnBtnListener{
    	public void onBtnClick(int position);
    }
    private OnBtnListener onBtnListener;
    public void setOnBtnListener(OnBtnListener listener){
    	onBtnListener=listener;
    }

}
