package com.routon.smartcampus.leave;

import java.util.ArrayList;
import java.util.List;

import com.routon.edurelease.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TeacherLeaveReviewAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<TeacherLeaveBean> mDatas=new ArrayList<TeacherLeaveBean>();

	public TeacherLeaveReviewAdapter(Context context,List<TeacherLeaveBean> datas){
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
			convertView = View.inflate(mContext, R.layout.teacher_leave_review_item, null);
			holder.teacherName= (TextView) convertView.findViewById(R.id.teacher_name_tv);
			holder.teacherLeaveTime = (TextView) convertView.findViewById(R.id.teacher_leave_time);
			holder.teacherLeaveReason = (TextView) convertView.findViewById(R.id.teacher_leave_reason);
			holder.replaceTeacherName = (TextView) convertView.findViewById(R.id.replace_teacher_name);
			
			holder.replaceRl=(RelativeLayout) convertView.findViewById(R.id.replace_rl);
			holder.replaceLl=(LinearLayout) convertView.findViewById(R.id.replace_ll);
			holder.leaveTypeIv=(ImageView)convertView.findViewById(R.id.leave_type_iv);
			
			holder.replaceIv=(ImageView)convertView.findViewById(R.id.replace_iv);
			holder.replaceTv = (TextView) convertView.findViewById(R.id.replace_tv);
			
			holder.leaveConfirmBtn=(RelativeLayout) convertView.findViewById(R.id.leave_ratify_btn);
			holder.leaveRejectBtn=(RelativeLayout) convertView.findViewById(R.id.leave_reject_btn);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		TeacherLeaveBean bean=mDatas.get(position);
		holder.teacherName.setText(bean.teacherName);
		holder.teacherLeaveTime.setText("请假时间："+bean.startTime+"至"+bean.endTime.substring(5, bean.endTime.length()));
		holder.teacherLeaveReason.setText("请假事由："+(bean.reason==null||bean.reason.equals("null") ? "" : bean.reason));
		holder.replaceTeacherName.setText("代课老师："+listToString(bean.agentTeachers));
		
		
		if (bean.status==0) {//待审批
			holder.replaceLl.setVisibility(View.VISIBLE);
			holder.leaveTypeIv.setVisibility(View.VISIBLE);
			holder.replaceRl.setVisibility(View.GONE);
		}else if (bean.status==1) {//已批准
			holder.replaceLl.setVisibility(View.GONE);
			holder.leaveTypeIv.setVisibility(View.GONE);
			holder.replaceRl.setVisibility(View.VISIBLE);
			holder.replaceIv.setImageResource(R.drawable.confirm_icon2);
			holder.replaceTv.setText("已批准");
			
		}else if (bean.status==2) {//已驳回
			holder.replaceLl.setVisibility(View.GONE);
			holder.leaveTypeIv.setVisibility(View.GONE);
			holder.replaceRl.setVisibility(View.VISIBLE);
			holder.replaceIv.setImageResource(R.drawable.reject_icon2);
			holder.replaceTv.setText("已驳回");
			
		}
		
		if (onConfirmListener!=null) {
			holder.leaveConfirmBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onConfirmListener.onConfirmClick(position);
				}
			});
		}
		
		if (onRejectListener!=null) {
			holder.leaveRejectBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onRejectListener.onRejectClick(position);
				}
			});
		}
		
		
		
		return convertView;
        
    }
    private class ViewHolder {
		public RelativeLayout leaveRejectBtn;
		public RelativeLayout leaveConfirmBtn;
		public TextView replaceTv;
		public ImageView replaceIv;
		public ImageView leaveTypeIv;
		public LinearLayout replaceLl;
		public RelativeLayout replaceRl;
		public TextView teacherName;
		public TextView teacherLeaveTime;
		public TextView teacherLeaveReason;
		public TextView replaceTeacherName;

	}
    
    private String listToString(List<String> list) {
    	String str="";
    	for (int i = 0; i < list.size(); i++) {
    		str+=i == 0 ? list.get(i) : "、"+list.get(i);
		}
		return str;
    	
    }
    
    public void setOnConfirmListener(OnConfirmListener listener){
    	onConfirmListener=listener;
    }
    private OnConfirmListener onConfirmListener;
    public interface OnConfirmListener{
    	public void onConfirmClick(int position);
    }
    
    public void setOnRejectListener(OnRejectListener listener){
    	onRejectListener=listener;
    }
    private OnRejectListener onRejectListener;
    public interface OnRejectListener{
    	public void onRejectClick(int position);
    }

}
