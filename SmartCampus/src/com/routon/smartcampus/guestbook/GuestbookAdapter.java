package com.routon.smartcampus.guestbook;

import java.io.File;
import java.util.List;

import com.routon.edurelease.R;
import com.routon.inforelease.util.ImageUtils;
import com.routon.smartcampus.SmartCampusApplication;
import com.routon.smartcampus.bean.StudentBean;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GuestbookAdapter extends BaseAdapter{

	private Context mContext;
	private List<GuestbookBean> mDataList;
	private LayoutInflater mLayoutInflater;
	protected StudentBean studentBean;
	

	public GuestbookAdapter(Context context, List<GuestbookBean> dataList) {
		this.mContext=context;
		this.mDataList=dataList;
		
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public int getItemViewType(int position) {
		return mDataList.get(position).type;
	}
	
	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		switch (getItemViewType(position)) {
		case 0:
			if (convertView==null) {
				viewHolder = new ViewHolder();
				convertView=mLayoutInflater.inflate(R.layout.guestbook_user_item, null);
				viewHolder.imgView=(ImageView) convertView.findViewById(R.id.img_view);
				viewHolder.timeView=(TextView) convertView.findViewById(R.id.time_view);
				viewHolder.msgView=(TextView) convertView.findViewById(R.id.msg_view);
				
				convertView.setTag(viewHolder);
			}else {
				viewHolder=(ViewHolder) convertView.getTag();
			}
			setMsgData(viewHolder,position,0);
			break;

		case 1:
			if (convertView==null) {
				viewHolder=new ViewHolder();
				convertView=mLayoutInflater.inflate(R.layout.guestbook_child_item, null);
				viewHolder.imgView=(ImageView) convertView.findViewById(R.id.img_view);
				viewHolder.timeView=(TextView) convertView.findViewById(R.id.time_view);
				viewHolder.msgView=(TextView) convertView.findViewById(R.id.msg_view);
				
				convertView.setTag(viewHolder);
			}else {
				viewHolder=(ViewHolder) convertView.getTag();
			}
			setMsgData(viewHolder,position,1);
			break;
		case 2:
			
			convertView=new ImageView(mContext);
			convertView.setBackgroundResource(R.drawable.guestbook_illustrate);
			break;
		default:
			break;
		}
		
		return convertView;
	}
	
	private void setMsgData(final ViewHolder viewHolder,  int position, int itemType) {
		viewHolder.timeView.setText(mDataList.get(position).createTime);
		viewHolder.msgView.setText(mDataList.get(position).msg);
		
		if (itemType==1) {
			
			ImageUtils.downloadAndSaveProfilePhoto(mContext, studentBean.imgUrl
					,String.valueOf(studentBean.sid),studentBean.imageLastUpdateTime,
					new ImageUtils.loadCallBack(){

						@Override
						public void loadCb(File file,String portrait) {
							if( studentBean != null && String.valueOf(studentBean.sid).equals(portrait) && file.exists() ) {
								viewHolder.imgView.setImageURI(Uri.fromFile(file));
							}
						}					
			});
		}
		
	}

	

	private class ViewHolder{
		ImageView imgView;
		TextView timeView;
		TextView msgView;
	}
	

}
