package com.routon.inforelease.plan.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.routon.ad.pkg.SNoticePkgTools;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.R;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.json.PlanMaterialBean;
import com.routon.inforelease.json.PlanMaterialBeanParser;
import com.routon.inforelease.json.PlanMaterialparamsBean;
import com.routon.inforelease.json.PlanMaterialrowsBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.offline.OfflinePackageMgr;
import com.routon.inforelease.plan.AdParams;
import com.routon.inforelease.plan.PlanListFragment;
import com.routon.inforelease.plan.adapter.ClassInfoListAdapter.OnItemClickListener;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.inforelease.util.TimeUtils;
import com.routon.inforelease.widget.AutoScrollTextView;
import com.routon.inforelease.widget.ClassPreviewView;
import com.routon.inforelease.widget.PlanPreviewView;

public class PlanListAdapter extends BaseAdapter {
	private Context mContext;
	private List<PlanListrowsBean> mPlanListrowsBeanList;
	private LayoutInflater mInflater;
    private OnItemClickListener mItemClickListener = null;
	
	public interface OnItemClickListener {
	     
	      void onItemClickListener(View v, int position);
	 }
	
	public void setOnItemClickClickListener(OnItemClickListener listener){
		mItemClickListener = listener;
	}
	
	public PlanListAdapter(Context context, List<PlanListrowsBean> planDataList) {
		mContext = context;
		if (planDataList != null)
			mPlanListrowsBeanList = planDataList;
		else
			mPlanListrowsBeanList = new ArrayList<PlanListrowsBean>();
		
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setDatas(List<PlanListrowsBean> list) {
		mPlanListrowsBeanList = list;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mPlanListrowsBeanList.size();
	}

	@Override
	public Object getItem(int position) {
		return mPlanListrowsBeanList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		DisplayMetrics  dm = new DisplayMetrics();    
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);    
	         
	      int screenWidth = dm.widthPixels;
//	      int screenHeight= dm.heightPixels;
		
		ViewHolder holder=null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.layout_plan_list_item, null);
			holder = new ViewHolder();

			holder.item_publish = (ImageView) convertView.findViewById(R.id.item_publish);
			holder.planVpView=(PlanPreviewView) convertView.findViewById(R.id.plan_vp_view);
//			holder.picVp = (ViewPager) holder.planVpView.findViewById(R.id.pic_vp);
			holder.dotText=(TextView) holder.planVpView.findViewById(R.id.dot_text);
			convertView.setTag(holder);
			
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		PlanListrowsBean mPlanBean = mPlanListrowsBeanList.get(position);
		
		holder.item_publish.setVisibility(View.GONE);

		
		if( mPlanBean.published == true ){
			holder.item_publish.setVisibility(View.VISIBLE);
			holder.item_publish.setImageResource(R.drawable.publish_tip);
			if( TimeUtils.isTimeBeforeTilMinute(TimeUtils.getFormatCalendar(mPlanBean.endTime,TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss), Calendar.getInstance()) ){
				holder.item_publish.setImageResource(R.drawable.ic_release_yellow);
				
			}
		}
		
	
		List<PlanMaterialrowsBean> materialList = mPlanBean.materialList;
		List<PlanMaterialrowsBean> subTitleList = mPlanBean.subTitleList;
		List<String> picUrlList = new ArrayList<String>();
		
		if ( subTitleList != null && subTitleList.size() > 0 ) {
			PlanMaterialrowsBean titleBean = subTitleList.get(0);
			String textcolor = "#042398";
			String textBgColor = "#ffffff";
			String textBgAlpha = "80";
			for (PlanMaterialparamsBean param : titleBean.params) {
				if( param.adParamId == AdParams.TEXT_BG_COLOR ){
					textBgColor = param.adParamValue;
				}else if( param.adParamId == AdParams.TEXT_COLOR ){
					textcolor = param.adParamValue;
				}else if( param.adParamId == AdParams.TEXT_BG_ALPHA ){
					textBgAlpha = param.adParamValue;
				}				
			}
			holder.planVpView.setText(titleBean.thumbnail, false, textcolor, textBgColor, textBgAlpha, 16);
		}else {
			holder.planVpView.setText(null,false,null,null,null,16);
		}
		
		if ( materialList != null && materialList.size()>0) {
			for (int i = 0; i < materialList.size(); i++) {
				picUrlList.add(materialList.get(i).thumbnail);
			}		
		}else {
			holder.dotText.setText("");
			picUrlList.add("0");
		}	
		if( mItemClickListener != null ){
			holder.planVpView.setItems(picUrlList, picUrlList.get(0),new PlanPreviewView.OnPreviewClickListener(){
	
				@Override
				public void onPreviewClickListener(View v, int imagePosition) {
					// TODO Auto-generated method stub
					mItemClickListener.onItemClickListener(v, position);
				}
				
			},PlanPreviewView.TYPE_PLANLIST);
		}else{
			holder.planVpView.setItems(picUrlList, picUrlList.get(0),null,PlanPreviewView.TYPE_PLANLIST);
		}
		
	    AbsListView.LayoutParams params = new AbsListView.LayoutParams(screenWidth,
				screenWidth*9/16);
		convertView.setLayoutParams(params);
		
		return convertView;
	}
	
	
	
	private class ViewHolder {
		ImageView item_publish;
		PlanPreviewView planVpView;
//		ViewPager picVp;
		TextView dotText;
	}
	
//	private List<String> getIdS(String filePath) {
//		String idString=PublishStateUtils.readTxtFile(mContext,filePath);
//		String [] ids = null;
//		List<String> idList=new ArrayList<String>();
//		if (idString!=null&&!idString.isEmpty()) {
//			
//			if (idString.contains(",")) {
//				ids=idString.split(",");
//			}else {
//				ids=new String []{idString};
//			}
//			for (int i = 0; i < ids.length; i++) {
//				idList.add(ids[i]);
//			}
//		}
//		
//		return idList;
//		
//	}

}