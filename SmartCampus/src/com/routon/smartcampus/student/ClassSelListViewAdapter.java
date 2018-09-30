package com.routon.smartcampus.student;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.routon.edurelease.R;

/**
 * Created by yjy on 2016/3/14.
 */
public class ClassSelListViewAdapter extends BaseAdapter {

    private Context mContext;
    public List<String> arrayList = new ArrayList<String>();

    public ClassSelListViewAdapter(Context context, List<String> arrayList) {
        this.arrayList = arrayList;
        this.mContext = context;
    }

    @Override
    public int getCount() {
    	if( arrayList == null ) return 0;
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View contentView, ViewGroup viewGroup) {
    	ViewHolder holder;
		if (contentView == null) {
			holder = new ViewHolder();
			contentView = View.inflate(mContext, R.layout.class_list_item, null);
			holder.item_text = (TextView) contentView.findViewById(R.id.item_text);

			contentView.setTag(holder);
		} else {
			holder = (ViewHolder) contentView.getTag();
		}
		String str = arrayList.get(position);

		holder.item_text.setText(str);

		return contentView;
        
    }
    private class ViewHolder {
		public TextView item_text;

	}
}
