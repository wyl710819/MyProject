package com.routon.smartcampus.flower;

import java.util.ArrayList;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.edurelease.R;
import com.routon.smartcampus.utils.FlowerUtil;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author sj
 * @version 2017年7月11日 上午8:34:45
 */
public class FlowerDetailGridAdapter extends BaseAdapter{

	private ArrayList<StudentBadge> datas;
	private Context mContext;
//	private int[] flowerImages = {R.drawable.flower1, R.drawable.flower2, R.drawable.flower3, 
//			R.drawable.flower4, R.drawable.flower5, R.drawable.flower6, R.drawable.flower7, R.drawable.flower8};
	
	public FlowerDetailGridAdapter(Context context, ArrayList<StudentBadge> stbadges) {
		this.mContext = context;
		this.datas = stbadges;
	}

	public ArrayList<StudentBadge> getDatas() {
		return datas;
	}

	public void setDatas(ArrayList<StudentBadge> datas) {
		this.datas = datas;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		if(datas != null && datas.size() >= position){
			return datas.get(position);
		}else{
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		if(datas != null && datas.size() >= position){
			return datas.get(position).id;
		}else{
			return 0;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder(); 
			LayoutInflater mInflater = LayoutInflater.from(mContext);
			convertView = mInflater.inflate(R.layout.flower_check_grid_item, null);

			holder.imageView = (NetworkImageView)convertView.findViewById(R.id.image);
			holder.teacherName=(TextView) convertView.findViewById(R.id.teacher_name);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		final StudentBadge stFlower = datas.get(position);
		
		if( stFlower != null && stFlower.badge != null ){
			holder.teacherName.setText(stFlower.teacherName);
			if (stFlower.exchangeId==0) {
				ColorStateList color = mContext.getResources().getColorStateList(R.color.text_green_color);
				holder.teacherName.setTextColor(color);
			}else {
				ColorStateList color = mContext.getResources().getColorStateList(R.color.text_yellow_color);
				holder.teacherName.setTextColor(color);
			}
			
			FlowerUtil.loadFlower(mContext, holder.imageView, stFlower.badge.name, stFlower.badge.imgUrl);
		}
//		Bitmap defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.flower1);
//		
//		holder.imageView.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(),FlowerUtil.createTextFlowerBitmap(mContext, defaultBitmap, stFlower.badge.name)));
//		ImageListener imageListener = new ImageListener(){
//
//			@Override
//			public void onErrorResponse(VolleyError error) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void onResponse(ImageContainer response, boolean isImmediate) {
//				// TODO Auto-generated method stub
//				Bitmap bitmap = response.getBitmap();
//				if( bitmap != null ){
//					holder.imageView.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(),FlowerUtil.createTextFlowerBitmap(mContext, bitmap, stFlower.badge.name)));
//				}
//			}
//			
//		};
//		InfoReleaseApplication.mImageLoader.get(stFlower.badge.imgUrl, imageListener);
//		int flowerId = flowerImages[(int)(Math.random()*10)%8];
//		holder.imageView.setImageUrl(stFlower.badge.imgUrl, InfoReleaseApplication.mImageLoader);		
//		holder.imageView.setDefaultImageResId(flowerId);//R.drawable.default_pic);
//		holder.imageView.setErrorImageResId(flowerId);//R.drawable.default_pic);
		return convertView;
	}

	private class ViewHolder{
		public NetworkImageView imageView;		
		public TextView teacherName;
	}
}
