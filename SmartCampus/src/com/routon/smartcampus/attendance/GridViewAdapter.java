package com.routon.smartcampus.attendance;

import java.io.File;
import java.util.List;


import com.routon.edurelease.R;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter{

	
	private Context mContext;
	private List<AttendanceBean> mContentList;
	public String baseUrl;
	public  GridViewAdapter(Context context , List<AttendanceBean> studentdatalist) {
		this.mContext=context;
		this.mContentList=studentdatalist;
		
	}
	@Override
	public int getCount() {
		return mContentList == null ? 0 : mContentList.size();
	}

	@Override
	public Object getItem(int position) {
		return mContentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder;
		
		if (convertView==null) {
			viewHolder=new ViewHolder();
			convertView=View.inflate(mContext, R.layout.item_attendance_grid_layout, null);
			viewHolder.studentImg=(ImageView)convertView.findViewById(R.id.student_img_view);
			viewHolder.studentName=(TextView)convertView.findViewById(R.id.student_name_view);
			convertView.setTag(viewHolder);
		}else {
			viewHolder=(ViewHolder) convertView.getTag();
		}		
		viewHolder.studentName.setText(mContentList.get(position).empName);
			///data/routon/ext/attence/studentImg/17739_20170612105653.jpg
		if(mContentList.get(position).attenceType==1){
			viewHolder.studentImg.setBackgroundColor(Color.parseColor("#FFFFFF"));
			viewHolder.studentName.setBackgroundColor(Color.parseColor("#FFFFFF"));
		}else{
			viewHolder.studentImg.setBackgroundColor(Color.parseColor("#B7B7B7"));
			viewHolder.studentName.setBackgroundColor(Color.parseColor("#B7B7B7"));
		}
		
		String s=mContentList.get(position).imgSavePath;
		File imgFile=new File(mContentList.get(position).imgSavePath);
		
        if (imgFile.exists()) {
			
        	 try{  
        		 
        		 Bitmap bitmap  = BitmapFactory.decodeFile(mContentList.get(position).imgSavePath);
        		 
        		 if (bitmap!=null) {
        			 viewHolder.studentImg.setImageBitmap(bitmap);
				}else {
					viewHolder.studentImg.setImageResource(R.drawable.default_student);
				}       		 
					
        	    } catch (Exception e)  {  
        	      Log.d("GridViewAdapter", "Exception:  "+e.getMessage());
        	    }  
            
		}else {
			Picasso.with(mContext).load(baseUrl+mContentList.get(position).imgUrl)
			.placeholder(R.drawable.default_student).into(viewHolder.studentImg);	
		}	
		return convertView;
	}
	
	private class ViewHolder{
		public TextView studentName;
		public ImageView studentImg;

	}

}
