package com.routon.smartcampus.flower;

import java.util.ArrayList;

import com.routon.edurelease.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OftenRemarkAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<BadgeInfo> mBadges;
	private LayoutInflater mInflater;

	public OftenRemarkAdapter(Context context, ArrayList<BadgeInfo> badges) {
		
		mContext = context;
		mBadges = badges;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mBadges == null ? 0 : mBadges.size();
	}

	@Override
	public Object getItem(int position) {
		return mBadges.get(position);
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
			convertView = mInflater.inflate(R.layout.often_remark_item_layout, null);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.remark_item_checkbox);
			holder.delBtn = (TextView) convertView.findViewById(R.id.del_btn);
			holder.remarkItemRl = (RelativeLayout) convertView.findViewById(R.id.often_remark_rl);
			holder.remarkTitle = (TextView) convertView.findViewById(R.id.remark_item_badgetitle);
			holder.remarkIntegral = (TextView) convertView.findViewById(R.id.remark_item_integral);
			holder.remarkContent = (TextView) convertView.findViewById(R.id.remark_item_badgeremark);
			holder.addRemarkItem = (RelativeLayout) convertView.findViewById(R.id.add_remark_item);
			holder.oftenItemRl = (RelativeLayout) convertView.findViewById(R.id.often_rl);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (position == mBadges.size() - 1) {
			holder.oftenItemRl.setVisibility(View.GONE);
			holder.addRemarkItem.setVisibility(View.VISIBLE);

		} else {
			holder.oftenItemRl.setVisibility(View.VISIBLE);
			holder.addRemarkItem.setVisibility(View.GONE);
			
			OftenBadgeBean bean = (OftenBadgeBean) mBadges.get(position);
			holder.remarkTitle.setText(bean.badgeTitle);
			if (bean.bonuspoint>=0) {
				holder.remarkIntegral.setText("+"+bean.bonuspoint);
				holder.remarkIntegral.setTextColor(mContext.getResources().getColor(R.color.text_green_color));
			}else {
				holder.remarkIntegral.setText(String.valueOf(bean.bonuspoint));
				holder.remarkIntegral.setTextColor(mContext.getResources().getColor(R.color.text_red));
			}
			
			holder.remarkContent.setText(bean.badgeRemark);
		}

		holder.delBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				
			}
		});

		

		return convertView;
	}

	private class ViewHolder {
		public RelativeLayout oftenItemRl;
		public RelativeLayout addRemarkItem;
		public TextView remarkContent;
		public TextView remarkIntegral;
		public TextView remarkTitle;
		public RelativeLayout remarkItemRl;
		public CheckBox checkBox;
		public TextView delBtn;
		
	}


	// 删除按钮点击事件回调
	public interface OnAwardListener {
		public void onAwardClick(int position);
	}

	private OnAwardListener onAwardListener = null;

	public void setOnAwardListener(OnAwardListener listener) {
		onAwardListener = listener;
	}

}
