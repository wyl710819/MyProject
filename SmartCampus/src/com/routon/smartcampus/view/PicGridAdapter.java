package com.routon.smartcampus.view;

import java.io.File;

import javax.xml.transform.ErrorListener;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.LruBitmapCache;
import com.routon.edurelease.R;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


public class PicGridAdapter extends BaseAdapter {
	private String[] files;

	private LayoutInflater mLayoutInflater;
	private Context mContext = null;

	public PicGridAdapter(String[] files, Context context) {
		this.files = files;
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return files == null ? 0 : files.length;
	}

	@Override
	public String getItem(int position) {
		return files[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MyGridViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new MyGridViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.gridview_pic_item,
					parent, false);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.album_image);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (MyGridViewHolder) convertView.getTag();
		}
		String path = getItem(position);
//		Log.d("PicGridAdapter","getView position:"+position+",path:"+path);
		
		if( path.startsWith("/") ){
			path = "file://"+path;
		}
		int picW = mContext.getResources().getDimensionPixelSize(R.dimen.badge_remark_image_w);
		int picH = mContext.getResources().getDimensionPixelSize(R.dimen.badge_remark_image_h);
		if( path != null && path.isEmpty() == false ){
			Picasso.with(mContext).load(path).placeholder(R.drawable.empty_photo)  
			.error(R.drawable.empty_photo).resize(picW, picH).into(viewHolder.imageView); 
		}else{
			viewHolder.imageView.setImageResource(R.drawable.empty_photo);
		}
		
		return convertView;
	}

	static class MyGridViewHolder {
		ImageView imageView;
	}
}
