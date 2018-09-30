package com.routon.inforelease.plan.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import com.routon.inforelease.R;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.inforelease.util.TimeUtils;
import com.routon.inforelease.widget.ClassPreviewView;
import com.routon.inforelease.widget.ClassPreviewView.OnPreviewClickListener;

public class ClassInfoListAdapter extends CheckedListAdapter<ClassInfoListdatasBean> {
	private OnItemClickListener mItemClickListener = null;
	private Boolean isShowPublishFlag = true;
	
	public void setIsShowPublishFlag(Boolean isShowPublishFlag) {
		this.isShowPublishFlag = isShowPublishFlag;
	}

	public interface OnItemClickListener {
	     
	      void onItemClickListener(View v, int position);
	 }
	
	
	public void setOnItemClickClickListener(OnItemClickListener listener){
		mItemClickListener = listener;
	}
	
	public ClassInfoListAdapter(Context context, List<ClassInfoListdatasBean> datas) {
		super(context, datas);
		
		// 初始化mImageLoader，并且传入了自定义的内存缓存
//		mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue, new LruBitmapCache()); // 初始化一个loader对象，可以进行自定义配置
	}
	
	public void setDatas(List<ClassInfoListdatasBean> datas) {
		this.mMaterialList = datas;
		this.notifyDataSetChanged();
	}
	
	public boolean mIsTerminalClassInfoList = false;

	@Override
	public View getView( final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder(); 
			convertView = mInflater.inflate(R.layout.layout_class_info_grid_item, null);

			holder.imageView = (ClassPreviewView) convertView.findViewById(R.id.image);
			holder.picVp = (ViewPager) holder.imageView.findViewById(R.id.pic_vp);
			holder.textView = (TextView)convertView.findViewById(R.id.item_title);
//			holder.checkBox = (CheckBox)convertView.findViewById(R.id.item_select);
			//holder.statusView = (TextView)convertView.findViewById(R.id.item_status);
			holder.subTitle1View = (TextView) convertView.findViewById(R.id.item_subtitle1);
			holder.subTitle2View = (TextView) convertView.findViewById(R.id.item_subtitle2);
			holder.descView = (TextView) convertView.findViewById(R.id.item_desc);
			holder.itemPublish=(ImageView) convertView.findViewById(R.id.item_publish);
			holder.priorityView = convertView.findViewById(R.id.item_priority);
			convertView.setTag(holder);
			
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		DisplayMetrics  dm = new DisplayMetrics();    
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);    
	         
	    int screenWidth = dm.widthPixels;
	    int screenHeight= dm.heightPixels;
	    AbsListView.LayoutParams classParams = new AbsListView.LayoutParams(screenWidth,
				screenWidth*9/16);
		convertView.setLayoutParams(classParams);
//		picVp.setTag(position);
		Log.i("ClassInfoListAdapter", "-------position:" +position+ "  smallImageUrl:");
		
		if( mIsTerminalClassInfoList == true ){
			holder.itemPublish.setVisibility(View.GONE);
			holder.imageView.setEnabled(false);
		}		
		ClassInfoListdatasBean dataItem = (ClassInfoListdatasBean) getItem(position);
		
		holder.itemPublish.setVisibility(View.GONE);
//		List<String> idStr=getIdS();
//		if (idStr!=null&&idStr.size()>0) {
			
//		if (idStr.contains(String.valueOf(dataItem.id))) {
		if(dataItem.status == ClassInfoListdatasBean.STATUS_AUDIT_THROUGH ||dataItem.status == ClassInfoListdatasBean.STATUS_AUDIT_TOBE){
			if(isShowPublishFlag){
				holder.itemPublish.setVisibility(View.VISIBLE);
				holder.itemPublish.setImageResource(R.drawable.publish_tip);
			}else{
				holder.itemPublish.setVisibility(View.GONE);
			}
			if( dataItem.endTime != null ){
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm);
				try {
					calendar.setTime(sdf.parse(dataItem.endTime));
					Calendar curCalendar = Calendar.getInstance();
					if( TimeUtils.isTimeBeforeTilMinute(calendar,curCalendar)){
						holder.itemPublish.setImageResource(R.drawable.ic_release_yellow);
					}
				} catch (ParseException e) {
//					e.printStackTrace();
				}
				
			}
					
		}else if(dataItem.status == ClassInfoListdatasBean.STATUS_AUDIT_NOTTHROUGH){
			if(isShowPublishFlag){
				holder.itemPublish.setVisibility(View.VISIBLE);
				holder.itemPublish.setImageResource(R.drawable.publish_flunk);
			}else{
				holder.itemPublish.setVisibility(View.GONE);
			}
		}
			
//		}
		
		holder.textView.setVisibility(View.GONE);
		holder.subTitle1View.setVisibility(View.GONE);
		holder.subTitle2View.setVisibility(View.GONE);
		holder.descView.setVisibility(View.GONE);
		
		if( dataItem.priority == 10 ){
			holder.priorityView.setVisibility(View.VISIBLE);
		}else{
			holder.priorityView.setVisibility(View.GONE);
		}
		
		String imagePath = dataItem.files.get(0).content;
		
		List<String> imagePathList=new ArrayList<String>();
		for (int i = 0; i <dataItem.files.size() ; i++) {
			imagePathList.add(dataItem.files.get(i).content);
		}
		
		int width = mContext.getResources().getDimensionPixelSize(R.dimen.pic_select_item_w);
		int height = mContext.getResources().getDimensionPixelSize(R.dimen.pic_select_item_h);
		String smallImageUrl = new String();
		if (imagePath.length() > 0) {
			int lastIndexOfDot = imagePath.lastIndexOf("."); 
			int tailLength = imagePath.length() - lastIndexOfDot;
			smallImageUrl += imagePath.substring(0, lastIndexOfDot);
			smallImageUrl += "_" + width + "x" + height;
			smallImageUrl += imagePath.substring(imagePath.length()-tailLength, imagePath.length());
			Log.i("ClassInfoListAdapter", "-------position:" +position+ "  smallImageUrl:"+smallImageUrl);
		}
		// 开始加载网络图片
		if( mItemClickListener != null ){
			holder.imageView.setItems(imagePathList, imagePathList.get(0),dataItem,new OnPreviewClickListener(){
	
				@Override
				public void onPreviewClickListener(View v, int imagePosition) {
					// TODO Auto-generated method stub
					mItemClickListener.onItemClickListener(v, position);
				}
				
			},ClassPreviewView.TYPE_CLASSLIST);
		}else{
			holder.imageView.setItems(imagePathList, imagePathList.get(0),dataItem,null,ClassPreviewView.TYPE_CLASSLIST);
		}

		return convertView;
	}
	

	private List<String> getIdS() {
		String idString=PublishStateUtils.readTxtFile(mContext,mContext.getDir("isPublishClass.txt", Context.MODE_PRIVATE).getPath());
		String [] ids = null;
		List<String> idList=new ArrayList<String>();
		if (idString!=null&&!idString.isEmpty()) {
			
			if (idString.contains(",")) {
				ids=idString.split(",");
			}else {
				ids=new String []{idString};
			}
			for (int i = 0; i < ids.length; i++) {
				idList.add(ids[i]);
			}
		}
		
		return idList;
		
	}

	public class ViewHolder{
		ViewPager picVp;
		View priorityView;
		ClassPreviewView imageView;
		TextView textView;
//		CheckBox checkBox;
		//TextView statusView;
		TextView subTitle1View;
		TextView subTitle2View;
		TextView descView;
		ImageView itemPublish;
	}
	
	//按下时的横坐标。
    private float xDown;

    //按下时的纵坐标。
    private float yDown;

    //移动时的横坐标。
    private float xMove;

    //移动时的纵坐标。
    private float yMove;
	private ImageView imgView;

}
