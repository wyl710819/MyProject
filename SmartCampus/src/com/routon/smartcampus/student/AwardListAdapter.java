package com.routon.smartcampus.student;

import java.util.ArrayList;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.widget.BitmapCache;
import com.routon.edurelease.R;
import com.routon.smartcampus.bean.AwardBean;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AwardListAdapter extends BaseAdapter implements OnClickListener {

	private Context mContext;
	private ArrayList<AwardBean> mAwardListData;
	private ImageLoader mImageLoader;
	private int mExchangBadgeCount;
	private Callback mCallback;

	public interface Callback { 
	    public void click(View v); 
	  } 
	
	public AwardListAdapter(Context context, ArrayList<AwardBean> awardListData, int unExchangBadgeCount,Callback callback) {
		this.mContext = context;
		this.mAwardListData = awardListData;
		this.mExchangBadgeCount = unExchangBadgeCount;
		this.mCallback = callback;
		mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue, new BitmapCache(mContext));
	}

	@Override
	public int getCount() {
		return mAwardListData.size();
	}

	@Override
	public Object getItem(int position) {
		return mAwardListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.award_list_item, null);
			holder.award_name = (TextView) convertView.findViewById(R.id.award_name);
			holder.award_img = (ImageView) convertView.findViewById(R.id.award_img);
			holder.award_num = (TextView) convertView.findViewById(R.id.award_num);
			holder.but_exchange = (Button) convertView.findViewById(R.id.but_exchange);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final AwardBean awardBean = mAwardListData.get(position);
		 holder.award_name.setText(awardBean.name);
		
//	     holder.award_img.setImageResource(R.drawable.pencil);
		 if (awardBean.imgUrl!=null) {
			 String smallImageUrl = ImageUtils.getSmallImageUrl(mContext, awardBean.imgUrl);
				ImageListener listener = ImageLoader.getImageListener(holder.award_img, 0, 0);  
				mImageLoader.get(smallImageUrl, listener);
		}else{
			holder.award_img.setImageResource(R.drawable.default_pic);
		}
	    
		
		 holder.award_num.setText(String.valueOf(awardBean.bonuspoint)+"积分兑换");
		 
		 if (awardBean.bonuspoint > mExchangBadgeCount) {
			 holder.but_exchange.setBackgroundResource(R.drawable.exchange_but_shape_gray);
			 holder.but_exchange.setEnabled(false);
		}else {
			holder.but_exchange.setBackgroundResource(R.drawable.exchange_but_shape);
			 holder.but_exchange.setEnabled(true);
		}
		 
		 holder.but_exchange.setOnClickListener(this); 
		 holder.but_exchange.setTag(position); 

		return convertView;
	}

	private class ViewHolder {
		public TextView award_name;
		public ImageView award_img;
		public TextView award_num;
		public Button but_exchange;

	}

	
	@Override
	public void onClick(View v) {
		 mCallback.click(v); 
	}

}
