package com.routon.smartcampus.flower;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.LruBitmapCache;
import com.routon.edurelease.R;
import com.routon.smartcampus.view.RippleView;
import com.routon.smartcampus.view.RippleView.OnRippleCompleteListener;
import com.routon.smartcampus.view.RippleView.OnTapListener;

public class StudentBadgeListAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<StudentBadge> stBadges;
//	private static ImageLoader mImageLoader;
	public OnItemRetractListener listener;
//	private int[] flowerImages = {R.drawable.flower1, R.drawable.flower2, R.drawable.flower3, R.drawable.flower4, 
//			R.drawable.flower5, R.drawable.flower6, R.drawable.flower7, R.drawable.flower8};
	
	public StudentBadgeListAdapter(Context context, ArrayList<StudentBadge> data) {
		
		this.mContext = context;
		this.stBadges = data;
		// 初始化mImageLoader，并且传入了自定义的内存缓存
//		mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue, new LruBitmapCache()); // 初始化一个loader对象，可以进行自定义配置
				
	}
	
	public void setDatas( ArrayList<StudentBadge> data){

		this.stBadges = data;
	}

	public void setOnItemRetractListener(OnItemRetractListener listener){
		this.listener = listener;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		final ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder(); 
			LayoutInflater mInflater = LayoutInflater.from(mContext);
			convertView = mInflater.inflate(R.layout.student_badge_item, null);

			holder.image = (NetworkImageView)convertView.findViewById(R.id.image);
			holder.name = (TextView)convertView.findViewById(R.id.name);
			holder.count = (TextView)convertView.findViewById(R.id.count);
			holder.teacherName = (TextView)convertView.findViewById(R.id.teacher_info);
			holder.createTime = (TextView)convertView.findViewById(R.id.create_time);
			holder.retract = (Button)convertView.findViewById(R.id.retract);
			holder.rippleView = (RippleView) convertView.findViewById(R.id.retract_ripple_view);
			convertView.setTag(holder);
			
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		final StudentBadge stBadge = this.stBadges.get(position);
		
		
		int flowerId = R.drawable.flower;
		holder.image.setImageUrl(stBadge.badge.imgUrl, InfoReleaseApplication.mImageLoader);		
		holder.image.setDefaultImageResId(flowerId);
		holder.image.setErrorImageResId(flowerId);
		
		holder.name.setText(stBadge.badge.name);
		if(stBadge.exchangeId == 0){
			holder.count.setText("未兑奖");
			holder.count.setTextColor(Color.rgb(48, 170, 48));
		}else{
			holder.count.setText("已兑奖");
			holder.count.setTextColor(Color.rgb(210, 207, 64));
		}
	
		holder.teacherName.setText("颁发教师:"+stBadge.teacherName);
		holder.createTime.setText("颁发时间:"+stBadge.createTime);
		holder.rippleView.setOnRippleCompleteListener(new OnRippleCompleteListener() {
			
			@Override
			public void onComplete(RippleView rippleView) {
				// TODO Auto-generated method stub
				if (listener != null){
					listener.onItemRetract(position);
				}
			}
		});
		
		return convertView;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return stBadges.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	private class ViewHolder{
		public NetworkImageView image;
		public TextView count;
		public TextView name;
		public TextView teacherName;
		public TextView createTime;
		public Button retract;
		public RippleView rippleView;
	}
	
	interface OnItemRetractListener{
		public void onItemRetract(int position);
	}
}
