package com.routon.inforelease.plan.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

public abstract class CheckedListAdapter<T> extends BaseAdapter {
	private static final String TAG = "CheckedListAdapter";
	
	protected List<T> mMaterialList;

	protected Context mContext;
	protected LayoutInflater mInflater;
	
	protected List<T> selectMaterails = new ArrayList<T>();
	
	protected boolean mEditable = true;
	
	public CheckedListAdapter(Context context, List<T> datas) {
		mContext = context;
		if (datas != null)
			mMaterialList = datas;
		else
			mMaterialList = new ArrayList<T>();
		
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
	
	public List<T> getSelectMaterial() {
		return selectMaterails;
	}
	
	public void clearSelect() {
		selectMaterails.clear();
	}

	public void selectAll() {
		selectMaterails.clear();
		for (T bean : mMaterialList) {
			Log.v(TAG, "add select: " + bean);
			selectMaterails.add(bean);
		}
//		selectMaterails.addAll(mMaterialList);
	}

	public void selectReverse() {
		List<T> list = new ArrayList<T>();
		list.addAll(mMaterialList);
		for (T bean : selectMaterails) {
			list.remove(bean);
		}
		selectMaterails = list;
	}
}
