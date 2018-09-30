package com.routon.smartcampus.flower;

import java.io.File;
import java.util.ArrayList;

import com.routon.edurelease.R;
import com.routon.smartcampus.utils.FlowerUtil;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.utils.StudentHelper;
import com.routon.smartcampus.view.NoScrollGridView;
import com.routon.smartcampus.view.PicGridAdapter;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

//最近颁发界面列表适配器
public class RecentIssuedListAdapter extends BaseAdapter{

	private ArrayList<StudentBadge> mList;
	private LayoutInflater mInflater;
	private Context mContext;
	
	public interface onClickListener{
		public static int CANCEL_TYPE = 0;
		public static int ADD_TYPE = 1;
		public void onClick(int position,int type);
	}
	
	private onClickListener mOnClickListener = null;
	public void setOnClickListener(onClickListener listener){
		mOnClickListener = listener;
	}
	
	public RecentIssuedListAdapter(Context context,ArrayList<StudentBadge> list) {
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
			convertView = mInflater.inflate(R.layout.list_item_recent_issued, null);
			holder.avator = (ImageView)convertView.findViewById(R.id.avator);
			holder.name = (TextView)convertView.findViewById(R.id.name);
			holder.content = (TextView) convertView.findViewById(R.id.content);
			holder.gridView = (NoScrollGridView)convertView.findViewById(R.id.gridView);
			holder.addBtn = convertView.findViewById(R.id.addBtn);
			holder.cancelBtn = (Button)convertView.findViewById(R.id.cancelBtn);
			holder.flowerImage = (ImageView)convertView.findViewById(R.id.flowerImage);
			holder.timeBtn = (TextView) convertView.findViewById(R.id.time);
			holder.agencyView =  convertView.findViewById(R.id.agency_tv);
			holder.gradeTv = (TextView) convertView.findViewById(R.id.GradeTv);
			holder.cardImage = (ImageView) convertView.findViewById(R.id.cardImage);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final StudentBadge bean = getItem(position);
		
		if( bean.status == 0 ){
			holder.agencyView.setVisibility(View.GONE);
		}else{
			holder.agencyView.setVisibility(View.VISIBLE);
		}
		
		if(bean.origin == 0){
			holder.cardImage.setVisibility(View.VISIBLE);
			holder.cardImage.setImageResource(R.drawable.phone_issue_flag);
		}else if(bean.origin == 1){//1701颁发
			holder.cardImage.setVisibility(View.VISIBLE);
			holder.cardImage.setImageResource(R.drawable.card_issue_flag);
		}else{
			holder.cardImage.setVisibility(View.GONE);
		}
		
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
		
//		if(holder.gradeTv != null){
//			if(bean.student.bonuspoints == 0){
//				if( bean.badge.prop == 0 ){
//					holder.gradeTv.setText("+"+bean.badge.bonuspoint);
//				}else{
//					holder.gradeTv.setText(String.valueOf(bean.badge.bonuspoint));
//				}
//			}else{
//				if(bean.student.bonuspoints > 0){
//					holder.gradeTv.setText("+"+bean.student.bonuspoints);
//				}else{
//					holder.gradeTv.setText(String.valueOf(bean.badge.bonuspoint));
//				}
//			}
//		}
		
		holder.timeBtn.setText(bean.createTime);
		
		//加载学生照片
		if( bean.student != null ){	
			StudentHelper.loadStudentImage(bean.student.imgSavePath, bean.student.imgUrl, mContext, holder.avator);
			holder.name.setText(bean.student.empName);
		}
		
		//加载徽章
		if( bean.badge != null ){
			FlowerUtil.loadFlower(mContext, holder.flowerImage, bean.badge.name, bean.badge.imgUrl);
		}else{
			holder.flowerImage.setImageResource(R.drawable.flower);
		}	
		
		holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( mOnClickListener != null ){
					mOnClickListener.onClick(position, onClickListener.CANCEL_TYPE);
				}
			}
		});

		String content = "";
		if( bean.badgeRemarkBean.badgeTitle != null && bean.badgeRemarkBean.badgeTitle.trim().length() > 0 ){
			content += bean.badgeRemarkBean.badgeTitle;
		}
		
		if( bean.badgeRemarkBean.badgeRemark != null && bean.badgeRemarkBean.badgeRemark.trim().length() > 0 ){
			if( content != null && content.trim().length() > 0 ){
				content +=  ":";
			}
			content += bean.badgeRemarkBean.badgeRemark;
		}

		if( ( content == null || content.trim().isEmpty() == true  )
				&& ( bean.badgeRemarkBean.imgList == null || bean.badgeRemarkBean.imgList.length == 0 )){
			holder.content.setVisibility(View.GONE);
			holder.gridView.setVisibility(View.GONE);
			holder.addBtn.setVisibility(View.VISIBLE);
			holder.addBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if( mOnClickListener != null ){
						mOnClickListener.onClick(position, onClickListener.ADD_TYPE);
					}
				}
			});
		}else{
			holder.content.setVisibility(View.VISIBLE);
			holder.gridView.setVisibility(View.VISIBLE);
			holder.addBtn.setVisibility(View.GONE);
			holder.content.setText(content);
			if( bean.badgeRemarkBean.imgList != null && bean.badgeRemarkBean.imgList.length > 0 ){
				holder.gridView.setVisibility(View.VISIBLE);
				holder.gridView.setAdapter(new PicGridAdapter(bean.badgeRemarkBean.imgList, mContext));
				holder.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						imageBrower(position,bean.badgeRemarkBean.imgList);
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

		public TextView name;
		public ImageView avator;
		ImageView flowerImage;
		TextView gradeTv;
		TextView timeBtn;
		View agencyView;
		TextView content;
		NoScrollGridView gridView;
		View addBtn;
		Button cancelBtn;
		ImageView cardImage;
	}
}
