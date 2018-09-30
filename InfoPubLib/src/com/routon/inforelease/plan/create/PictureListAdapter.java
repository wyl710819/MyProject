package com.routon.inforelease.plan.create;

import java.util.ArrayList;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.plan.create.velloyExpand.CookieImageRequest;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

public class PictureListAdapter extends BaseAdapter {
	private final String TAG = "PictureListAdapter";
	private LayoutInflater mInflater;
	private Context mContext;
	private ArrayList<MaterialItem> datas;
	// 位置集合
	private ArrayList<Integer> positons = new ArrayList<Integer>();
	private static ImageLoader mImageLoader;

	public ArrayList<MaterialItem> selectMaterails = new ArrayList<MaterialItem>();

	private OnImageClickedListener listener;

	public PictureListAdapter(Context context, ArrayList<MaterialItem> datas) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		// 初始化mImageLoader，并且传入了自定义的内存缓存
		mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue,
				new LruBitmapCache()); // 初始化一个loader对象，可以进行自定义配置
		this.datas = datas;
	}

	public void setDatas(ArrayList<MaterialItem> datas) {
		this.datas = datas;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas.size();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater
					.inflate(R.layout.pictures_select_item, null);

			holder.imageView = (NetworkImageView) convertView
					.findViewById(R.id.image);
			holder.checkBox = (CheckBox) convertView
					.findViewById(R.id.item_select);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.checkBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						MaterialItem dataItem = datas.get(position);

						if (isItemInSelectList(selectMaterails, dataItem)
								&& isChecked == false) {
							// selectMaterails.remove(dataItem);
							removeItemInList(selectMaterails, dataItem);
							// 删除position
							positons.remove(new Integer(position));
						}

						if (isChecked
								&& !isItemInSelectList(selectMaterails,
										dataItem)) {
							selectMaterails.add(dataItem);
							// 添加position
							positons.add(new Integer(position));
						}

					}
				});

		holder.imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (listener != null)
					listener.onImageClicked(position);
			}
		});

		holder.imageView.setDefaultImageResId(R.drawable.default_pic);
		holder.imageView.setErrorImageResId(R.drawable.default_pic);

		MaterialItem dataItem = datas.get(position);
		String imagePath = dataItem.getContent();
		// Log.i(TAG, "imagePath:"+imagePath);
		holder.checkBox
				.setChecked(isItemInSelectList(selectMaterails, dataItem));

		int width = mContext.getResources().getDimensionPixelSize(
				R.dimen.pic_select_item_w);
		int height = mContext.getResources().getDimensionPixelSize(
				R.dimen.pic_select_item_h);
		if (imagePath.length() > 0) {
			String smallImageUrl = new String();
			int lastIndexOfDot = imagePath.lastIndexOf(".");
			int tailLength = imagePath.length() - lastIndexOfDot;
			smallImageUrl += imagePath.substring(0, lastIndexOfDot);
			smallImageUrl += "_" + width + "x" + height;
			smallImageUrl += imagePath.substring(imagePath.length()
					- tailLength, imagePath.length());
			Log.i(TAG, "-------position:" + position + "  smallImageUrl:"
					+ smallImageUrl);

			// 开始加载网络图片
			holder.imageView.setImageUrl(smallImageUrl, mImageLoader);
		}

		holder.checkBox.setTag(dataItem);
		return convertView;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public class ViewHolder {
		NetworkImageView imageView;
		CheckBox checkBox;
	}

	public void setListener(OnImageClickedListener listener) {
		this.listener = listener;
	}

	interface OnImageClickedListener {
		void onImageClicked(int position);
	}

	private boolean isItemInSelectList(ArrayList<MaterialItem> lists,
			MaterialItem item) {
		for (MaterialItem materialItem : lists) {
			if (materialItem.getId() == item.getId()) {
				// Log.i(TAG, "materialItem id: "+ item.getId());
				return true;
			}
		}

		return false;
	}

	private void removeItemInList(ArrayList<MaterialItem> lists,
			MaterialItem item) {
		for (MaterialItem materialItem : lists) {
			if (materialItem.getId() == item.getId()) {
				lists.remove(materialItem);
				return;
			}
		}
	}
	
	//最小position
	public int getMinPosition()
	{
		int size=positons.size();
		if(size>0)
		{
			int min=positons.get(0);
			for(int i=0;i<size;i++)
			{
				if(positons.get(i)<min)
					min=positons.get(i);
			}
			return min;
		}
		return 0;
	}
}
