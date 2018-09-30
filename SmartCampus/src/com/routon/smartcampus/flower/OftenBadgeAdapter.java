package com.routon.smartcampus.flower;

import java.util.List;

import com.routon.edurelease.R;
import com.routon.smartcampus.utils.FlowerUtil;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OftenBadgeAdapter extends BaseAdapter {

	private Context mContext;
	private List<BadgeInfo> mBadgeList;
	private boolean mIsManage;
	private LayoutInflater mInflater;

	public OftenBadgeAdapter(Context context, List<BadgeInfo> badgeList, float density, boolean isManage) {
		mContext = context;
		mBadgeList = badgeList;
		mDensity = density;
		mIsManage = isManage;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mBadgeList == null ? 0 : mBadgeList.size();
	}
	
	public void setData(List<BadgeInfo> badgeList){
		mBadgeList = badgeList;
	}

	@Override
	public Object getItem(int position) {
		return mBadgeList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.often_badge_layout, null);
			holder.awardBtn = (TextView) convertView.findViewById(R.id.award_btn);
			holder.dragBtn = (ImageView)convertView.findViewById(R.id.drag_handle);
			holder.badgeBonuspointView = (TextView) convertView.findViewById(R.id.often_badge_bonuspoint);
			holder.badgeNameView = (TextView) convertView.findViewById(R.id.often_badge_name);
			holder.badgeRemarkView = (TextView) convertView.findViewById(R.id.often_badge_remark);
			holder.badgeImgView = (ImageView) convertView.findViewById(R.id.often_badge_img);
			holder.delBtn = (ImageView) convertView.findViewById(R.id.click_remove);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		OftenBadgeBean bean = (OftenBadgeBean) mBadgeList.get(position);

		
		
		if (mIsManage) {
			holder.awardBtn.setVisibility(View.INVISIBLE);
			holder.dragBtn.setVisibility(View.VISIBLE);
			holder.delBtn.setVisibility(View.VISIBLE);
		}else{
			holder.awardBtn.setVisibility(View.VISIBLE);
			holder.dragBtn.setVisibility(View.INVISIBLE);
			holder.delBtn.setVisibility(View.GONE);
		}


		if (!bean.imgUrl.equals("")) {
			FlowerUtil.loadFlower(mContext, holder.badgeImgView, bean.badgeTitle, bean.imgUrl);
		}
		

		holder.badgeNameView.setText(bean.badgeTitle);

		if (bean.prop == 1) {// 负面
			holder.badgeBonuspointView.setTextColor(mContext.getResources().getColor(R.color.text_red));
			if (bean.bonuspoint>=0) {
				holder.badgeBonuspointView.setText("-" + bean.bonuspoint);
			}else {
				holder.badgeBonuspointView.setText(""+bean.bonuspoint);
			}
		} else {// 正面
			holder.badgeBonuspointView.setTextColor(mContext.getResources().getColor(R.color.text_green_color));
			if (bean.bonuspoint>=0) {
				holder.badgeBonuspointView.setText("+" + bean.bonuspoint);
			}else {
				holder.badgeBonuspointView.setText( ""+bean.bonuspoint);
			}
		}
		
		holder.badgeRemarkView.setText(bean.badgeRemark);


		holder.awardBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (onAwardListener != null) {
					onAwardListener.onAwardClick(position);
				}
			}
		});
		
		holder.delBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (onDelBtnClickListener != null) {
					onDelBtnClickListener.onDelBtnClick(position);
				}
			}
		});
		
		return convertView;
	}
	
	private class ViewHolder {
		public ImageView delBtn;
		public TextView awardBtn;
		public ImageView dragBtn;
		public TextView badgeBonuspointView;
		public TextView badgeNameView;
		public TextView badgeRemarkView;
		public ImageView badgeImgView;
	}

	// GridView item点击事件回调
	public interface OnItemListener {
		public void onItemClick(int position);
	}

	private OnItemListener onItemListener = null;

	public void setOnItemListener(OnItemListener listener) {
		onItemListener = listener;
	}

	// 颁发和删除按钮点击事件回调
	public interface OnAwardListener {
		public void onAwardClick(int position);
	}

	private OnAwardListener onAwardListener = null;

	public void setOnAwardListener(OnAwardListener listener) {
		onAwardListener = listener;
	}

	// 上移按钮点击事件回调
	public interface OnDelBtnClickListener {
		public void onDelBtnClick(int position);
	}

	private OnDelBtnClickListener onDelBtnClickListener = null;
	private float mDensity;

	public void setOnDelBtnClickListener(OnDelBtnClickListener listener) {
		onDelBtnClickListener = listener;
	}
	
	public int dp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
				mContext.getResources().getDisplayMetrics());
	}
}
