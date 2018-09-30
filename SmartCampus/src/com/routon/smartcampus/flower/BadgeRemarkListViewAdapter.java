package com.routon.smartcampus.flower;

import java.util.List;

import com.routon.edurelease.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class BadgeRemarkListViewAdapter extends BaseAdapter{

	private Context mContext;
	private List<BadgeInfo> mBadgeRemarkList;
	private LayoutInflater mInflater;
	private int mRightViewWidth;
	private BadgeRemarkActivity mActivity;
	
	public interface DelListener{
		void del(int position);
	}
	
	private DelListener mDelListener;
	public BadgeRemarkListViewAdapter(Context context, List<BadgeInfo> badgeRemarkList,int mRightViewWidth) {
		mContext=context;
		mBadgeRemarkList=badgeRemarkList;
		mInflater = LayoutInflater.from(context);
		mActivity=(BadgeRemarkActivity) mContext;
		this.mRightViewWidth = mRightViewWidth;
	}
	
	public void setDelListener(DelListener delListener){
		mDelListener = delListener;
	}

	@Override
	public int getCount() {
		return mBadgeRemarkList==null ? 0 : mBadgeRemarkList.size();
	}

	@Override
	public Object getItem(int position) {
		return mBadgeRemarkList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.remark_listview_item, null);
			holder.Re_left=(RelativeLayout) convertView.findViewById(R.id.Re_left);
			holder.ll_delete = (LinearLayout) convertView
					.findViewById(R.id.ll_delete_ll_right);
			holder.checkBox=(CheckBox)convertView.findViewById(R.id.remark_item_checkbox);
			holder.badgeTitle=(TextView)convertView.findViewById(R.id.remark_item_badgetitle);
			holder.badgeIntegral=(TextView)convertView.findViewById(R.id.remark_item_integral);
			holder.badgeRemark=(TextView)convertView.findViewById(R.id.remark_item_badgeremark);
			holder.addRemarkRl=(RelativeLayout) convertView.findViewById(R.id.add_remark_rl);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		BadgeInfo bean = (BadgeInfo) mBadgeRemarkList.get(position);
		
		/*if (position==mBadgeRemarkList.size()-1) {
			holder.addRemarkRl.setVisibility(View.VISIBLE);
			holder.Re_left.setVisibility(View.GONE);
			
			
		}else {*/
			holder.addRemarkRl.setVisibility(View.GONE);
			holder.Re_left.setVisibility(View.VISIBLE);
			
			holder.badgeTitle.setText(bean.badgeTitle);
			if (bean.bonuspoint>=0) {
				holder.badgeIntegral.setText("+"+bean.bonuspoint);
			}else {
				holder.badgeIntegral.setText(""+bean.bonuspoint);
			}
			
			holder.badgeRemark.setText(bean.badgeRemark);
		
			if (bean.isSelect) {
				holder.checkBox.setChecked(true);
			}else {
				holder.checkBox.setChecked(false);
			}
//		}
		

		// 设置布局参数

		LayoutParams lp_left = new LayoutParams(
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
		holder.Re_left.setLayoutParams(lp_left);
//
		LayoutParams lp_right = new LayoutParams(mRightViewWidth,
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
		holder.ll_delete.setEnabled(true);
		holder.ll_delete.setTag(position);	
		holder.ll_delete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if( mDelListener != null ){
					mDelListener.del((int)(arg0.getTag()));
				}
			}
		});
		holder.ll_delete.setLayoutParams(lp_right);
		return convertView;
	}
//	class onClick implements OnClickListener {
//
//		int position;
//
//		public void setPosition(int position) {
//			this.position = position;
//		}
//
//		@Override
//		public void onClick(View v) {
//			switch (v.getId()) {
////			case R.id.img_play_Re_left:
////				Toast.makeText(mContext, "play--->position=" + position,
////						Toast.LENGTH_SHORT).show();
////				break;
//			case R.id.ll_delete_ll_right:
//				mBadgeRemarkList.remove(position);
////				mActivity.deleteSaveRemarkArrays(position);
//				BadgeRemarkListViewAdapter.this.notifyDataSetChanged();
//
//				break;
//
//			default:
//				break;
//			}
//
//		}
//		
//	}
	
	private class ViewHolder {
		public RelativeLayout addRemarkRl;
		public CheckBox checkBox;
		public TextView badgeTitle;
		public TextView badgeRemark;
//		public TextView addBadgeView;
//		public ImageView addBadgeImgView;
		public TextView badgeIntegral;
		public RelativeLayout Re_left;
		LinearLayout ll_delete;
	}
	
	
}
