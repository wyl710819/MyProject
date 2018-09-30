package com.routon.smartcampus.flower;

import java.util.ArrayList;

import com.routon.edurelease.R;
import com.routon.smartcampus.utils.FlowerUtil;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.NoScrollGridView;
import com.routon.smartcampus.view.PicGridAdapter;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//最近颁发界面列表适配器
public class NewStudentBadgeListAdapter extends BaseAdapter{
	private static final String TAG = "NewStudentBadgeListAdapter";
	private ArrayList<StudentBadge> mList;
	private LayoutInflater mInflater;
	private Context mContext;
	
	public interface onClickListener{
		public static int CANCEL_TYPE = 0;
		public static int ADD_TYPE = 1;
		public void onClick(int position,int type);
	}
	
	private boolean mEditable = false;
	//默认不可编辑，即撤销和备注
	public void setEditable(boolean editable){
		mEditable = editable;
	}
	
	private onClickListener mOnClickListener = null;
	public void setOnClickListener(onClickListener listener){
		mOnClickListener = listener;
	}
	
	public NewStudentBadgeListAdapter(Context context,ArrayList<StudentBadge> list) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		this.mList = list;
	}

	@Override
	public int getCount() {
		return mList==null?0:mList.size();
	}

	@Override
	public StudentBadge getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).id;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_new_student_badge, null);
			holder.avator = (ImageView)convertView.findViewById(R.id.avator);
			holder.flowerIv = (ImageView)convertView.findViewById(R.id.flowerImage);		
			holder.content = (TextView) convertView.findViewById(R.id.content);
			holder.gridView = (NoScrollGridView)convertView.findViewById(R.id.gridView);
			holder.gradeTv = (TextView) convertView.findViewById(R.id.GradeTv);
			holder.addBtn = convertView.findViewById(R.id.addBtn);
//			holder.cancelBtn = (Button)convertView.findViewById(R.id.cancelBtn);
			holder.timeTv = (TextView) convertView.findViewById(R.id.time);
			holder.nameTv = (TextView) convertView.findViewById(R.id.nameTv);		
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final StudentBadge bean = getItem(position);
		if( bean.teacheImgUrl != null && bean.teacheImgUrl.isEmpty() == false ){
			int avatorW = (int) mContext.getResources().getDimension(R.dimen.recentissued_avator_w);
			int avatorH = (int) mContext.getResources().getDimension(R.dimen.recentissued_avator_h);
			Picasso.with(mContext).load(bean.teacheImgUrl).placeholder(R.drawable.default_student)  
		    .error(R.drawable.default_student).resize(avatorW, avatorH).into(holder.avator);  
		}else{
			holder.avator.setImageResource(R.drawable.default_student);
		}
		
		if( bean.badge != null && bean.badge.imgUrl != null && bean.badge.imgUrl.trim().isEmpty() == false ){
//			int flowerW = mContext.getResources().getDimensionPixelSize(R.dimen.list_view_flower_image_width);
//			int flowerH = mContext.getResources().getDimensionPixelSize(R.dimen.list_view_flower_image_height);
//			Picasso.with(mContext).load(bean.badge.imgUrl).placeholder(R.drawable.flower)  
//				.error(R.drawable.flower).resize(flowerW, flowerH).into(holder.flowerIv);  
			FlowerUtil.loadFlower(mContext, holder.flowerIv, bean.badge.name, bean.badge.imgUrl);						
		}else{
			holder.flowerIv.setImageResource(R.drawable.flower);
		}
		
		holder.timeTv.setText(bean.createTime);
		
		if( bean.badge != null && holder.gradeTv != null ){
//			if( bean.badge.prop == 0 ){
//				holder.gradeTv.setText("+"+bean.badge.bonuspoint);
//			}else{
//				holder.gradeTv.setText(String.valueOf(bean.badge.bonuspoint));
//			}
			
			if(bean.bonusPoint > 0){
				holder.gradeTv.setText("+"+bean.bonusPoint);
			}else{
				holder.gradeTv.setText(String.valueOf(bean.bonusPoint));
			}
		}
		holder.nameTv.setText(bean.teacherName);
		
//		if( mEditable == true ){//可编辑，可撤销
//			holder.cancelBtn.setVisibility(View.VISIBLE);
//			holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					if( mOnClickListener != null ){
//						mOnClickListener.onClick(position, onClickListener.CANCEL_TYPE);
//					}
//				}
//			});
//		}else{
//			holder.cancelBtn.setVisibility(View.GONE);
//		}
		
//		Badge badge = bean.badge;
		BadgeRemarkBean badgeRemark = bean.badgeRemarkBean;
//		if( badge != null ){
//			if ( badge.badgeRemarkList != null && badge.badgeRemarkList.size() > 0 ){
//				badgeRemark = badge.badgeRemarkList.get(0);
//			}
//		}
		
//		if( badgeRemark == null || 
//				(badgeRemark.badgeRemark == null && ( badgeRemark.imgList == null || badgeRemark.imgList.length == 0 ))){
//			holder.content.setVisibility(View.GONE);
//			holder.gridView.setVisibility(View.GONE);
//			if( mEditable == true ){//可编辑，可添加
//				holder.addBtn.setVisibility(View.VISIBLE);
//				holder.addBtn.setOnClickListener(new View.OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						if( mOnClickListener != null ){
//							mOnClickListener.onClick(position, onClickListener.ADD_TYPE);
//						}
//					}
//				});
//			}
//		}else{
		
//		holder.addBtn.setVisibility(View.VISIBLE);
//		holder.addBtn.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if( mOnClickListener != null ){
//					mOnClickListener.onClick(position, onClickListener.ADD_TYPE);
//				}
//			}
//		});
		if( badgeRemark != null ){
			final String[] imgList = badgeRemark.imgList;
			holder.content.setVisibility(View.VISIBLE);
			holder.gridView.setVisibility(View.VISIBLE);
//			holder.addBtn.setVisibility(View.GONE);
			String content = badgeRemark.badgeTitle;
			if( badgeRemark.badgeRemark != null && badgeRemark.badgeRemark.trim().length() > 0 ){
				if( content != null && content.trim().length() > 0 ){
					content +=  ":";
				}
				content += badgeRemark.badgeRemark;
			}
			holder.content.setText(content);
			if( badgeRemark.imgList != null && badgeRemark.imgList.length > 0 ){
				holder.gridView.setVisibility(View.VISIBLE);
				holder.gridView.setAdapter(new PicGridAdapter(badgeRemark.imgList, mContext));
				holder.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						imageBrower(position,imgList);
					}
				});
			}else{
				holder.gridView.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	private void imageBrower(int position, String[] urls) {
		Intent intent = new Intent(mContext, RemarkImagePreviewActivity.class);
		 
	    Bundle bundle = new Bundle();    
	    ArrayList<String> list = new ArrayList<String>();
	    for(int i=0;i<urls.length;i++){ 
	    	list.add(urls[i]); 
	    } 
		bundle.putStringArrayList(MyBundleName.BADGE_REMARK_PIC_LIST, list);
		bundle.putInt(MyBundleName.BADGE_REMARK_PIC_POSITION, position);
	    
	    intent.putExtras(bundle);
	    mContext.startActivity(intent);
	}
	private static class ViewHolder {
		public ImageView avator;
		ImageView flowerIv;
		TextView nameTv;
		TextView content;
		TextView gradeTv;
		NoScrollGridView gridView;
		View addBtn;
		TextView timeTv;
//		Button cancelBtn;
	}
}
