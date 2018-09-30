package com.routon.smartcampus.schoolcompare;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.routon.edurelease.R;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.smartcampus.homework.FamilyHomeworkAdapter.onCheckListener;
import com.routon.smartcampus.homework.HomeworkListViewAdapter.MyOnClickListener;
import com.routon.smartcampus.view.RippleView;
import com.routon.smartcampus.view.ScrollPickerView;
import com.routon.smartcampus.view.ScrollPickerView.OnChangedListener;
import com.routon.smartcampus.view.StringScrollPicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.routon.widgets.Toast;

public class ClassMarkAdapter extends BaseAdapter {

	private Context mContext;
	private List<SubprojectBean> mDataList;
	public List<SubprojectBean> tagDataList=new ArrayList<SubprojectBean>();
	public boolean isCompareFinish;
	private int userId;
	private static final int DECIMAL_DIGITS = 1;//小数的位数
	private boolean isFirstLoadAdapter=true;
	private int pos=0;
    private int index = -1;  
	
	private MyOnClickListener  mClickListener=null;
	public interface MyOnClickListener{
		 public  void click(View v,int position); 
	} 
	public void setListener(MyOnClickListener mClickListener){
		this.mClickListener=mClickListener;
	}
	public ClassMarkAdapter(Context context, List<SubprojectBean> classStr) {
		this.mContext = context;
		this.mDataList = classStr;
		userId = InfoReleaseApplication.authenobjData.userId;
		positionList = new ArrayList<Integer>();
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
	public View getView(final int position,  View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.class_marking_item, null);
			viewHolder.marking_type_text = (TextView) convertView.findViewById(R.id.marking_type_text);
			viewHolder.score_text = (TextView) convertView.findViewById(R.id.score_text);
//			viewHolder.pickerView=(StringScrollPicker) convertView.findViewById(R.id.scroll_picker);
			viewHolder.inputScore=(EditText) convertView.findViewById(R.id.inputScore);
			viewHolder.averageText = (TextView)convertView.findViewById(R.id.average_text);
			
			viewHolder.inputScore.setOnTouchListener(new View.OnTouchListener() {  
	            @Override  
	            public boolean onTouch(View v, MotionEvent event) {  
	            	if (event.getAction() == MotionEvent.ACTION_UP) {  
	                    index = position;  
	                }  
	                return false;  
	            }  
	    });  
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final SubprojectBean bean;
		if (positionList.contains(position)) {
			bean = tagDataList.get(position);
		}else {
			bean = mDataList.get(position);
		}
		viewHolder.marking_type_text.setText(bean.name);
		DecimalFormat df = new DecimalFormat("0.00");
		viewHolder.score_text.setText(bean.score==-9999 ? "-" : String.valueOf(df.format(bean.score)));
		
		
		if(isFirstLoadAdapter){
			if(bean.score==-9999){
				viewHolder.inputScore.setText("");
			}else{
				viewHolder.inputScore.setText(String.valueOf(mDataList.get(position).score));
			}
			
		}else{
			
			viewHolder.inputScore.setText(viewHolder.inputScore.getText().toString());
			
		}
		
//			viewHolder.inputScore.requestFocus();
//		    viewHolder.inputScore.callOnClick();
		
		
//		viewHolder.inputScore.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				 	viewHolder.inputScore.requestFocus();
//				    viewHolder.inputScore.callOnClick();
//			}
//		});
		
		
		viewHolder.inputScore.setTag(position);
		 
		
		viewHolder.inputScore.setOnFocusChangeListener(new View.OnFocusChangeListener() {  
	        @Override  
	        public void onFocusChange(View v, boolean hasFocus) {  
	        	   
	        	if(hasFocus){
	        		index = position; 
	        		viewHolder.inputScore.requestFocus();
	        		viewHolder.inputScore.setCursorVisible(true);
	        		viewHolder.inputScore.setText(viewHolder.inputScore.getText().toString());
	        		viewHolder.inputScore.setSelection(viewHolder.inputScore .getText().length());
	        		
	        	} else{
	        		viewHolder.inputScore.clearFocus();
	        		viewHolder.inputScore.setCursorVisible(false);
	        	}
	        }  
	    });  
		viewHolder.inputScore.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > DECIMAL_DIGITS) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + DECIMAL_DIGITS+1);
                        viewHolder.inputScore.setText(s);
                        viewHolder.inputScore.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    viewHolder.inputScore.setText(s);
                    viewHolder.inputScore.setSelection(2);
                }
                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                    	viewHolder.inputScore.setText(s.subSequence(0, 1));
                    	viewHolder.inputScore.setSelection(1);
                        return;
                    }
                }
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				isFirstLoadAdapter=false;
				if(viewHolder.inputScore.getText().toString()!=null&&viewHolder.inputScore.getText().length()>0&&viewHolder.inputScore.getText().toString()!=null
						&&!viewHolder.inputScore.getText().toString().trim().equals("")&&!viewHolder.inputScore.getText().toString().trim().equals("-")
						&&(isInteger(viewHolder.inputScore.getText().toString())||isDouble(viewHolder.inputScore.getText().toString()))){
					
						tagDataList.get(position).score=Double.parseDouble(viewHolder.inputScore.getText().toString());
						if (onChangeListener!=null) {
							onChangeListener.onSelected(position, Double.parseDouble(viewHolder.inputScore.getText().toString()));
						}
				
					
//					saveEditContent(saveScoreKey, saveScore);
//					mDataList.get(position).score=Double.parseDouble(viewHolder.inputScore.getText().toString());
				}
				if(viewHolder.inputScore.getText().toString()==null||viewHolder.inputScore.getText().length()==0){
					tagDataList.get(position).score=-9999;
					if (onChangeListener!=null) {
						onChangeListener.onSelected(position, -9999);
					}
			    }	
			}
		});
		
//		list.add(0, "-");
//		viewHolder.pickerView.setData(list);
//		viewHolder.pickerView.setSelectedPosition(bean.score==-1 ? 0 : bean.score+1);

//		viewHolder.pickerView.setOnSelectedListener(new ScrollPickerView.OnSelectedListener() {
//            @Override
//            public void onSelected(ScrollPickerView scrollPickerView, int j) {
//            	if (onChangeListener!=null) {
//            		onChangeListener.onSelected(position,j-1);
//            		positionList.add(position);
//            		tagDataList.get(position).score=j-1;
//				}
//            	
//            }
//		});
		
//		viewHolder.pickerView.setOnChangedListener(new OnChangedListener() {
//
//			@Override
//			public void onChanged() {
//				if (onItemChangeListener!=null) {
//					onItemChangeListener.onChange();
//				}
//			}
//		});
//		


		if (isCompareFinish) {
//			viewHolder.pickerView.setVisibility(View.INVISIBLE);
			viewHolder.inputScore.setVisibility(View.INVISIBLE);
			viewHolder.score_text.setVisibility(View.VISIBLE);
			viewHolder.averageText.setVisibility(View.VISIBLE);
			
			String averageString = new String();
			
			double currentScore = (bean.score==-9999 ? 0 : bean.score);
			double deltaAvg = currentScore - bean.itemAvg;
			Log.i("songjian", "name: "+bean.name+" currentScore:"+currentScore+"  itemAvg:"+bean.itemAvg);
			if(deltaAvg > 0){
				viewHolder.averageText.setTextColor(Color.rgb(28, 137, 30));
				viewHolder.averageText.setText("+"+df.format(deltaAvg));
			}else if(deltaAvg<0){
				viewHolder.averageText.setTextColor(Color.rgb(248, 58, 45));
				viewHolder.averageText.setText(""+df.format(deltaAvg));
			}else{
				viewHolder.averageText.setTextColor(Color.rgb(248, 58, 45));
				viewHolder.averageText.setText(""+0);
			}
		}else {
			/*if (bean.isGrade && Integer.valueOf(viewHolder.score_text.getText().toString())!=0) {
				viewHolder.score_text .setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			}*/

			viewHolder.averageText.setVisibility(View.INVISIBLE);
			if (bean.isPermit) {
//				viewHolder.pickerView.setVisibility(View.VISIBLE);
				viewHolder.inputScore.setVisibility(View.VISIBLE);
				viewHolder.score_text.setVisibility(View.INVISIBLE);
				viewHolder.marking_type_text.setTextColor(mContext.getResources().getColor(R.color.black));
				viewHolder.score_text.setTextColor(mContext.getResources().getColor(R.color.black));
			}else {
				viewHolder.score_text.setVisibility(View.VISIBLE);
//				viewHolder.pickerView.setVisibility(View.INVISIBLE);
				viewHolder.inputScore.setVisibility(View.INVISIBLE);
				viewHolder.marking_type_text.setTextColor(mContext.getResources().getColor(R.color.text_grey));
				viewHolder.score_text.setTextColor(mContext.getResources().getColor(R.color.text_grey));
			}
		}
//		
		if(index != -1 && index == position){  
		     //强制加上焦点  
			viewHolder.inputScore.requestFocus();  
		     //设置光标显示到编辑框尾部  
			viewHolder.inputScore.setSelection(viewHolder.inputScore .getText().length());  
		     //重置  
		     index = -1;  
		}  else{
			viewHolder.inputScore.clearFocus();  
		}
		return convertView;
	}
	private static class ViewHolder {
		TextView marking_type_text;
		TextView plusBut;
		TextView score_text;
		TextView minusBut;
//		StringScrollPicker pickerView;
		TextView averageText;
		EditText inputScore;
	}
	
	public interface onChangeListener{
		public void onSelected(int position, double d);
	}
	public interface onItemChangeListener{
		public void onChange();
	}
	private onChangeListener onChangeListener = null;
	private onItemChangeListener onItemChangeListener = null;
	private List<Integer> positionList;
	
	
	public void setOnChangeListener(onChangeListener listener){
		onChangeListener = listener;
	}
	public void setOnItemChangeListener(onItemChangeListener listener){
		onItemChangeListener = listener;
	}
	
	private  boolean useList(int[] arr, int value) {
		for(int s: arr){
				 if(s==value)
				      return true;
				 }
		return false;
	}
	/**
	 * 判断字符串是否是整数
	 */
	public static boolean isInteger(String value) {
	    try {
	        Integer.parseInt(value);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}

	/**
	 * 判断字符串是否是浮点数
	 */
	public static boolean isDouble(String value) {
	    try {
	        Double.parseDouble(value);
	        if (value.contains("."))
	            return true;
	        return false;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
}
