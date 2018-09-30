package com.routon.smartcampus.selectcourse;

import java.util.List;
import com.routon.edurelease.R;
import com.routon.smartcampus.schoolcompare.CompareClassTypeBean;
import com.routon.smartcampus.view.HorizontalListView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SelectCourseAdapter extends BaseAdapter{

	Context mContext;
	List<SelectCourseBean> mDataList;
	public SelectCourseAdapter(Context context, List<SelectCourseBean> selectsList) {
		this.mContext=context;
		this.mDataList=selectsList;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataList==null ? 0 : mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext,R.layout.select_course_item,
					null);

			holder.name_text = (TextView) convertView
					.findViewById(R.id.name);
			holder.describe_text = (TextView) convertView
					.findViewById(R.id.desc);
			holder.time_text = (TextView) convertView.findViewById(R.id.selecttime);
			holder.selectpeole_text = (TextView) convertView
					.findViewById(R.id.peoplecount);
			holder.select_btn  = (Button)convertView.findViewById(R.id.selectBtn);
			holder.selected_img= (ImageView)convertView.findViewById(R.id.selectedImg);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name_text.setText(mDataList.get(position).entityName);
		holder.describe_text.setText("选课说明：该课程时间有限，需尽快选课"/*mDataList.get(position).selectDesc*/);
		String start_time = mDataList.get(position).selectStartDate;
		if(start_time.length()>16)
		{
			start_time= start_time.substring(5, 15);
		}
		String end_time = mDataList.get(position).selectEndDate;
		if(end_time.length()>16)
		{
			end_time = end_time.substring(5,15);
		}
		String select_time = start_time+"~"+end_time;
		holder.time_text.setText(select_time);
		holder.selectpeole_text.setText(mDataList.get(position).selectCount+"人已选课");
		if(mDataList.get(position).selectStatus == 2)
		{
			holder.select_btn.setText("重新选课");
			holder.select_btn.setBackground(mContext.getResources().getDrawable(R.drawable.shape_reselect_btn));

		}
		else
		{
			holder.select_btn.setText("参与选课");
		}

		holder.select_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(mContext, SelectingActivity.class);
				intent.putExtra("classTypeBean", mDataList.get(position));
				mContext.startActivity(intent);
			}

		});
		return convertView;
	}
	private static class ViewHolder {

		TextView name_text;
		TextView describe_text;
		TextView time_text;
		TextView selectpeole_text;
		Button select_btn;
		ImageView selected_img;
	}
}
