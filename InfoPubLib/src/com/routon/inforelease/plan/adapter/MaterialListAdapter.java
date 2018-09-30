package com.routon.inforelease.plan.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.routon.inforelease.json.PlanMaterialrowsBean;

public abstract class MaterialListAdapter extends BaseAdapter {
	private static final String TAG = "MaterialListAdapter";
	
	protected List<PlanMaterialrowsBean> mMaterialList;

	protected Context mContext;
	protected LayoutInflater mInflater;
	
	protected List<PlanMaterialrowsBean> selectMaterails = new ArrayList<PlanMaterialrowsBean>();
	
	protected boolean mEditable = true;
	
	public MaterialListAdapter(Context context, List<PlanMaterialrowsBean> imageList) {
		mContext = context;
		if (imageList != null)
			mMaterialList = imageList;
		else
			mMaterialList = new ArrayList<PlanMaterialrowsBean>();
		
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setEditable(boolean editable) {
		mEditable = editable;
	}

	@Override
	public int getCount() {
		return mMaterialList.size();
	}

	@Override
	public Object getItem(int position) {
		return mMaterialList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public List<PlanMaterialrowsBean> getSelectMaterial() {
		return selectMaterails;
	}
	
	public void clearSelect() {
		selectMaterails.clear();
	}
	
	public void selectAll() {
		selectMaterails.clear();
		for (PlanMaterialrowsBean bean : mMaterialList) {
			Log.v(TAG, "add select: " + bean);
			selectMaterails.add(bean);
		}
//		selectMaterails.addAll(mMaterialList);
	}

	public void selectReverse() {
		List<PlanMaterialrowsBean> list = new ArrayList<PlanMaterialrowsBean>();
		list.addAll(mMaterialList);
		for (PlanMaterialrowsBean bean : selectMaterails) {
			list.remove(bean);
		}
		selectMaterails = list;
	}
}
