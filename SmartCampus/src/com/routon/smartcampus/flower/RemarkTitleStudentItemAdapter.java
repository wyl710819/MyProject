package com.routon.smartcampus.flower;

import java.util.List;

import com.routon.edurelease.R;
import com.routon.smartcampus.bean.StudentBean;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RemarkTitleStudentItemAdapter extends BaseAdapter{

	private Context mContext;
	private List<StudentBean> mStudentList;
	private LayoutInflater mInflater;

	public RemarkTitleStudentItemAdapter(Context context, List<StudentBean> list) {
		mContext=context;
		mStudentList=list;
		mInflater = LayoutInflater.from(context);
		
	}

	@Override
	public int getCount() {
		return mStudentList==null ? 0 : mStudentList.size();
	}

	@Override
	public Object getItem(int position) {
		return mStudentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.remark_title_student_item, null);
			holder.studentImg=(ImageView)convertView.findViewById(R.id.badge_remark_student_img);
			holder.studentName=(TextView)convertView.findViewById(R.id.badge_remark_user_name);			

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		StudentBean bean = mStudentList.get(position);
		
		holder.studentName.setText(bean.empName);
		String url = bean.imgUrl;
		if(  url != null && url.isEmpty() == false ){
			Picasso.with(mContext).load(url).placeholder(R.drawable.default_student)  
				.error(R.drawable.default_student).into(holder.studentImg);
		}else{
			holder.studentImg.setImageResource(R.drawable.default_student);
		}		
		
		return convertView;
	}

	
	private class ViewHolder {
		public ImageView studentImg;
		public TextView studentName;

	}
	
	
}
