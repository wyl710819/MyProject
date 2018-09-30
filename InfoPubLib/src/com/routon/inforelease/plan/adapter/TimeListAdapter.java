package com.routon.inforelease.plan.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import com.routon.widgets.Toast;

import com.routon.inforelease.R;
import com.routon.inforelease.plan.StringUtils;
import com.routon.inforelease.plan.TimeData;
import com.routon.inforelease.util.TimeUtils;

public class TimeListAdapter extends BaseAdapter {
	private Context mContext;
	private List<TimeData> mTimeDataList;
	private LayoutInflater mInflater;
	private List<TimeData> mSelTimeDataList;
	
	public TimeListAdapter(Context context, List<TimeData> timeDataList) {
		mContext = context;
		mTimeDataList = timeDataList;
		
		mSelTimeDataList = new ArrayList<TimeData>();
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mTimeDataList.size();
	}
	
	public void deleteSelDatas(){
		for(TimeData data:mSelTimeDataList){
			mTimeDataList.remove(data);
		}
		Log.d("111","mSelTimeDataList count:"+mSelTimeDataList.size()+",mTimeDataList:"+mTimeDataList.size());
		mSelTimeDataList.clear();
		this.notifyDataSetChanged();
	}
	
	public int getSelCount(){
		return mSelTimeDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mTimeDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (view == null) {
			view = mInflater.inflate(R.layout.plan_make_time_item, parent, false);
			holder = new ViewHolder();
			holder.name_text = (TextView) view.findViewById(R.id.time_item_name);
			holder.start_time_text = (TextView) view.findViewById(R.id.start_time);
			holder.end_time_text = (TextView) view.findViewById(R.id.end_time);
			holder.repeat_time = (TextView) view.findViewById(R.id.edit_repeat_time);
			holder.max_repeat_time = (TextView) view.findViewById(R.id.edit_max_repeat_time);
			holder.sel_cb = (CheckBox)view.findViewById(R.id.sel_item);
			
			holder.start_time_text.setOnClickListener(mOnBtnSelectTimeClickListener);
			holder.end_time_text.setOnClickListener(mOnBtnSelectTimeClickListener);
//			holder.repeat_time.addTextChangedListener(mOnEditTextChangeListener);
//			holder.max_repeat_time.addTextChangedListener(mOnEditTextChangeListener);
			holder.repeat_time.setOnClickListener(mOnBtnSelectTimeClickListener);
			holder.max_repeat_time.setOnClickListener(mOnBtnSelectTimeClickListener);
			

			view.setTag(holder);			
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		
		
		final TimeData timeData = mTimeDataList.get(position);
		holder.name_text.setText(mContext.getResources().getString(R.string.plan_time) + (position + 1));
		holder.start_time_text.setText(timeData.start_time);
		holder.end_time_text.setText(timeData.end_time);
		holder.repeat_time.setText(Integer.toString(timeData.repeat_time));
		holder.max_repeat_time.setText(Integer.toString(timeData.max_repeat_time));
		
		holder.sel_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if( isChecked == true ){
					mSelTimeDataList.add(timeData);
				}else{
					mSelTimeDataList.remove(timeData);
				}
			}
		});
		
		if( mSelTimeDataList != null && mSelTimeDataList.contains(timeData) ){
			holder.sel_cb.setChecked(true);
		}else{
			holder.sel_cb.setChecked(false);
		}
		
		Integer tag = new Integer(position);
		holder.start_time_text.setTag(tag);
		holder.end_time_text.setTag(tag);
		holder.repeat_time.setTag(tag);
		holder.max_repeat_time.setTag(tag);
		
		return view;
	}
	
	private void onDeleteItem(int position) {
		mTimeDataList.remove(position);
		this.notifyDataSetChanged();
	}
	
	private View.OnClickListener mOnBtnDelClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Integer tag = (Integer) v.getTag();
			int position = tag.intValue();
			
			onDeleteItem(position);
		}
	};
	
	private View.OnClickListener mOnBtnSelectTimeClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Integer tag = (Integer) v.getTag();
			int position = tag.intValue();
			
			Calendar calendar = Calendar.getInstance();
			if( v.getId() == R.id.start_time ){
				TextView tv = (TextView) v;
				String s = tv.getText().toString();
				SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_HH_mm);
				try {
					calendar.setTime(sdf.parse(s));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				showTimePicker(v.getId(), position, calendar);
			}else if( v.getId() == R.id.end_time ){
				calendar.add(Calendar.HOUR, 1);
				TextView tv = (TextView) v;
				String s = tv.getText().toString();
				SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_HH_mm);
				try {
					calendar.setTime(sdf.parse(s));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				showTimePicker(v.getId(), position, calendar);
			}else if( v.getId() == R.id.edit_repeat_time ){
				showModifyDataDialog((TextView) v, position);
			}else if( v.getId() == R.id.edit_max_repeat_time ){
				showModifyDataDialog((TextView) v, position);
			}	
		}
	};
	
	private void showModifyDataDialog(final TextView textView, final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	      
		LayoutInflater inflater = mInflater;
		final View layout = inflater.inflate(R.layout.dialog_modify_data, null);
		EditText edit = (EditText)(layout.findViewById(R.id.edit));
		edit.setInputType(InputType.TYPE_CLASS_NUMBER);
		edit.setText(textView.getText());
		builder.setView(layout);
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int arg1) {
				EditText edit = (EditText)(layout.findViewById(R.id.edit));
				String data = edit.getText().toString();
				
				if( data == null || data.isEmpty() ){//输入数据为空
					Toast.makeText(mContext, R.string.data_is_null, Toast.LENGTH_SHORT).show();				
					return;
				}
				
				TimeData timeData = mTimeDataList.get(position);
				int id = textView.getId();
				if( id == R.id.edit_repeat_time ){
					timeData.repeat_time = StringUtils.toInteger(data, 1);
				}else if( id == R.id.edit_max_repeat_time ){
					timeData.max_repeat_time = StringUtils.toInteger(data, 65535);
				}
				notifyDataSetChanged();
    	  	}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int arg1) {
				dlg.dismiss();
    	  	}
		});
		AlertDialog dlg = builder.create();
		dlg.show();
	}
	
//	private TextWatcher mOnEditTextChangeListener = new TextWatcher() {
//		
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before, int count) {
//			
//		}
//		
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//		@Override
//		public void afterTextChanged(Editable s) {
//		}
//	};
	
	private void showTimePicker(final int id, final int position, Calendar calendar) {
		TimePickerDialog dlg = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_HH_mm);
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);
				
				TimeData timeData = mTimeDataList.get(position);
				String time = sdf.format(calendar.getTime());
				
				if( id == R.id.start_time ){
					timeData.start_time = time;
				}else if( id == R.id.end_time ){
					timeData.end_time = time;
				}
				notifyDataSetChanged();
			}
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
		dlg.show();
	}

	private class ViewHolder {
		TextView name_text;
		TextView start_time_text;
		TextView end_time_text;
		TextView repeat_time;
		TextView max_repeat_time;
		
		CheckBox sel_cb;
		
//		View btn_del_item;
//		View btn_select_start_time;
//		View btn_select_end_time;
	}
}
