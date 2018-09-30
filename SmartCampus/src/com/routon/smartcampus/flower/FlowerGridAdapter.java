package com.routon.smartcampus.flower;

import java.util.ArrayList;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.NetworkImageView;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.edurelease.R;
import com.routon.smartcampus.utils.FlowerUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * @author sj
 * @version 2017年7月11日 上午8:34:45
 */
public class FlowerGridAdapter extends BaseAdapter{

	private ArrayList<Badge> datas;
	private Context mContext;
//	private int[] flowerImages = {R.drawable.flower1, R.drawable.flower2, R.drawable.flower3, 
//			R.drawable.flower4, R.drawable.flower5, R.drawable.flower6};
	
	public FlowerGridAdapter(Context context, ArrayList<Badge> badges) {
		this.mContext = context;
		this.datas = badges;
	}

	public ArrayList<Badge> getDatas() {
		return datas;
	}

	public void setDatas(ArrayList<Badge> datas) {
		this.datas = datas;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas==null ? 0 : datas.size();
	}

	@Override
	public Object getItem(int position) {
		if(datas != null && datas.size() >= position){
			return datas.get(position);
		}else{
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		if(datas != null && datas.size() >= position){
			return datas.get(position).id;
		}else{
			return 0;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder(); 
			LayoutInflater mInflater = LayoutInflater.from(mContext);
			convertView = mInflater.inflate(R.layout.flower_grid_item, null);

			holder.imageView = (ImageView)convertView.findViewById(R.id.image);
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		final Badge flower = datas.get(position);
		Log.d("FlowerGridAdapter","url:"+flower.imgUrl);
		FlowerUtil.loadFlower(mContext, holder.imageView, flower.name, flower.imgUrl);
	    

		return convertView;
	}

	private class ViewHolder{
		public ImageView imageView;		
	}
}
