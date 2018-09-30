package com.routon.smartcampus.schoolcompare;

import java.util.List;

import com.routon.edurelease.R;
import com.routon.smartcampus.utils.MyBundleName;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class GradeCompareAdapter extends BaseAdapter{
	private Context mContext;
	private List<List<ClassCompareBean>> mDataList;
	public boolean isTaxis;
	private int ratingMode;
	private int selectId;
	private CompareClassTypeBean compareClassTypeBean;
//	public boolean isGradeList=false;
	public ClassCompareActivity mActivity;
	public GradeCompareAdapter(Context context, List<List<ClassCompareBean>> gradeList, int ratingMode,int selectId, List<ClassCompareBean> classCompareBeanList) {
		this.mContext=context;
		this.mDataList=gradeList;
		this.ratingMode = ratingMode;
		this.selectId = selectId;
		compareClassTypeBean = new CompareClassTypeBean();
		compareClassTypeBean.classCompareBeanList = classCompareBeanList;
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView==null) {
			viewHolder=new ViewHolder();
			convertView=View.inflate(mContext, R.layout.grade_compare_item, null);
			
			viewHolder.class_compare_listview=(ListView) convertView.findViewById(R.id.class_compare_listview);
			
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder) convertView.getTag();
		}
		ClassCompareAdapter mAdapter=new ClassCompareAdapter(mContext, mDataList.get(position));
//		if (isGradeList) {
//			mAdapter.isGradeList=isGradeList;
//		}else {
//			mAdapter.isGradeList=false;
//		}
		mAdapter.isTaxis=isTaxis;
		viewHolder.class_compare_listview.setAdapter(mAdapter);
		setListViewHeightBasedOnChildren(viewHolder.class_compare_listview);
		viewHolder.class_compare_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
//				if (isGradeList) {
//					Intent intent=new Intent(mContext, HistoryQueryActivity.class);
//					intent.putExtra("classCompareBean", mDataList.get(position).get(p));
//					mContext.startActivity(intent);
//				}else {
					Intent intent=new Intent(mContext, ClassMarkActivity.class);
					intent.putExtra("isCompareFinish", isTaxis);
					intent.putExtra("ratingMode", 1);
					intent.putExtra("selectId", mDataList.get(position).get(p).groupId);
					intent.putExtra("compareClassTypeBean", compareClassTypeBean);
					mActivity.startActivityForResult(intent, 1);
//				}
				
			}
		});
		
		return convertView;
	}
	
	private static class ViewHolder {
		ListView class_compare_listview;
	}
	
	private void setListViewHeightBasedOnChildren(ListView listView) {  
        ListAdapter listAdapter = listView.getAdapter();  
        if(listAdapter == null) {  
            return;  
        }  
        int totalHeight = 0;  
        for(int i = 0, len = listAdapter.getCount(); i < len; i++) { 
            View listItem = listAdapter.getView(i, null, listView); 
            if(listItem != null){
            	listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            	listItem.measure(0, 0); 
            	totalHeight += listItem.getMeasuredHeight(); 
            }
        }  
        ViewGroup.LayoutParams params = listView.getLayoutParams();  
        params.height = totalHeight  
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));  
          
        listView.setLayoutParams(params);  
    }  

}
