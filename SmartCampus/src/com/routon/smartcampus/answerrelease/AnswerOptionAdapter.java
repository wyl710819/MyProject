package com.routon.smartcampus.answerrelease;

import java.util.List;


import com.routon.edurelease.R;


import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AnswerOptionAdapter extends BaseAdapter {
	private Context mContext;
	private List<OptionContentBean> mContentList;
	private int itemposition=-1;
	private View itemView;
	public boolean isClick=false;

	public AnswerOptionAdapter(Context context,List<OptionContentBean> contentList){
		this.mContext=context;
		this.mContentList=contentList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mContentList == null ? 0 : mContentList.size();
	}
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mContentList.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
	//	if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.item_answer_option, null);
			holder.optionName=(TextView)convertView.findViewById(R.id.answer_option_name);
			holder.selCountText=(TextView)convertView.findViewById(R.id.answer_option_count);
			holder.arrowImg=(ImageView)convertView.findViewById(R.id.option_arrow_img);
	
			convertView.setTag(holder);
	//	} else {
	//		holder = (ViewHolder) convertView.getTag();
	//	}
		
		if (mContentList!=null && mContentList.size()>0) {
			if (position==itemposition) {
				holder.optionName.setText(mContentList.get(position).optionNameStr);
				holder.selCountText.setText(mContentList.get(position).selCountStr);
				
	//			holder.optionName.setTextColor(mContext.getResources().getColor(R.color.but_red));
	//			holder.selCountText.setTextColor(mContext.getResources().getColor(R.color.but_red));
	//			holder.arrowImg.setImageResource(R.drawable.icon_arrow_red);
			}else {
				String answer=null;
				if(mContentList.get(position).optionName.equals("Y")){
					holder.optionName.setText("同意");
					holder.optionName.setTextColor(Color.parseColor("#3CB371"));
					if (mContentList.get(position).selCountStr.length()==6){
						 answer=mContentList.get(position).selCount+"位同学选择    ";
					} else{
						 answer=mContentList.get(position).selCount+"位同学选择  ";
					}
					
					
					SpannableString spannableString = new SpannableString(answer);
					holder.selCountText.setTextColor(Color.parseColor("#b94645"));
					spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.black)), answer.lastIndexOf(String.valueOf("位")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					holder.selCountText.setText(spannableString);
					//holder.selCountText.setText(mContentList.get(position).selCountStr);
				}
				else if(mContentList.get(position).optionName.equals("N")){
					holder.optionName.setText("反对");
					holder.optionName.setTextColor(Color.parseColor("#b94645"));
					if (mContentList.get(position).selCountStr.length()==6){
						 answer=mContentList.get(position).selCount+"位同学选择    ";
					} else{
						 answer=mContentList.get(position).selCount+"位同学选择  ";
					}
					SpannableString spannableString = new SpannableString(answer);
					holder.selCountText.setTextColor(Color.parseColor("#b94645"));
					spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.black)), answer.lastIndexOf(String.valueOf("位")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					holder.selCountText.setText(spannableString);
					//holder.selCountText.setText(mContentList.get(position).selCountStr);
				}else if(mContentList.get(position).optionName.equals("")){
					if(mContentList.get(position).type==3){
						if (mContentList.get(position).selCountStr.length()==7){
							 answer=mContentList.get(position).selCount+"位同学选择        弃权";
						} else{
							 answer=mContentList.get(position).selCount+"位同学选择      弃权";
						}
					   
						SpannableString spannableString = new SpannableString(answer);
						holder.selCountText.setTextColor(Color.parseColor("#b94645"));
						spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.black)), answer.lastIndexOf(String.valueOf("位")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						holder.selCountText.setText(spannableString);
						
						spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#191970")), answer.lastIndexOf(String.valueOf("弃")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						holder.selCountText.setText(spannableString);
						
					}else {
						answer=mContentList.get(position).selCount+"位同学未选择";
						SpannableString spannableString = new SpannableString(answer);
						holder.selCountText.setTextColor(Color.parseColor("#b94645"));
						spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.black)), answer.lastIndexOf(String.valueOf("位")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						holder.selCountText.setText(spannableString);
					}
					
				}
				else {
					
					holder.optionName.setText(mContentList.get(position).optionNameStr);
					holder.optionName.setTextColor(Color.parseColor("#3CB371"));
					if (mContentList.get(position).selCountStr.length()==6){
						 answer=mContentList.get(position).selCount+"位同学选择  ";
					} else{
						 answer=mContentList.get(position).selCount+"位同学选择";
					}
					
					SpannableString spannableString = new SpannableString(answer);
					holder.selCountText.setTextColor(Color.parseColor("#b94645"));
					spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.black)), answer.lastIndexOf(String.valueOf("位")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					holder.selCountText.setText(spannableString);
				//	holder.selCountText.setText(mContentList.get(position).selCount+"位同学选择");
					
				}
				
				if (mContentList.get(position).isClick) {
					holder.arrowImg.setImageResource(R.drawable.icon_arrow_answer);
				}else {
					holder.arrowImg.setImageResource(R.drawable.icon_arrow_answer);
				}
				
			}
			
		}
		
		return convertView;
	}

	private class ViewHolder {
		public TextView optionName;
		public TextView selCountText;
		public ImageView arrowImg;
	
	}

/*public void updateView(View view, int itemIndex , List<OptionContentBean> mContentBeanList ) {
	  if(view == null) {
	   return;
	  }
	  
	  if (itemIndex==itemposition) {
		  return;
	  }else if (itemposition!=-1) {
		  ViewHolder holders = (ViewHolder) itemView.getTag();
		    holders.optionName=(TextView)itemView.findViewById(R.id.answer_option_name);
			holders.selCountText=(TextView)itemView.findViewById(R.id.answer_option_count);
			holders.arrowImg=(ImageView)itemView.findViewById(R.id.option_arrow_img);
//			holders.optionName.setTextColor(mContext.getResources().getColor(R.color.text_black));
//			holders.selCountText.setTextColor(mContext.getResources().getColor(R.color.text_black));

			if(mContentList.get(itemIndex).optionName.equals("Y")){
				holders.optionName.setText("    同意");
				holders.optionName.setTextColor(Color.GREEN);
				
				String answer=mContentList.get(itemIndex).selCount+"位同学选择";
				SpannableString spannableString = new SpannableString(answer);
				holders.selCountText.setTextColor(Color.RED);
				spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.black)), answer.lastIndexOf(String.valueOf("位")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				holders.selCountText.setText(spannableString);
				//holder.selCountText.setText(mContentList.get(position).selCountStr);
			}
			else if(mContentList.get(itemIndex).optionName.equals("N")){
				holders.optionName.setText("    反对");
				holders.optionName.setTextColor(Color.RED);
				String answer=mContentList.get(itemIndex).selCount+"位同学选择";
				SpannableString spannableString = new SpannableString(answer);
				holders.selCountText.setTextColor(Color.RED);
				spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.black)), answer.lastIndexOf(String.valueOf("位")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				holders.selCountText.setText(spannableString);
				//holder.selCountText.setText(mContentList.get(position).selCountStr);
			}else if(mContentList.get(itemIndex).optionName.equals("")){
				if(mContentList.get(itemIndex).type==3){
					String answer=mContentList.get(itemIndex).selCount+"位同学选择    弃权";
					SpannableString spannableString = new SpannableString(answer);
					holders.selCountText.setTextColor(Color.RED);
					spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.black)), answer.lastIndexOf(String.valueOf("位")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					holders.selCountText.setText(spannableString);
					
					spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), answer.lastIndexOf(String.valueOf("弃")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					holders.selCountText.setText(spannableString);
					
				}else {
					String answer=mContentList.get(itemIndex).selCount+"位同学未选择";
					SpannableString spannableString = new SpannableString(answer);
					holders.selCountText.setTextColor(Color.RED);
					spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.black)), answer.lastIndexOf(String.valueOf("位")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					holders.selCountText.setText(spannableString);
				}
				
			}
			else {
				
				holders.optionName.setText(mContentList.get(itemIndex).optionNameStr);
				holders.optionName.setTextColor(Color.GREEN);
				String answer=mContentList.get(itemIndex).selCount+"位同学选择";
				SpannableString spannableString = new SpannableString(answer);
				holders.selCountText.setTextColor(Color.RED);
				spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.black)), answer.lastIndexOf(String.valueOf("位")),spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				holders.selCountText.setText(spannableString);
			//	holder.selCountText.setText(mContentList.get(position).selCount+"位同学选择");
				
			}
			
			
		
			
			holders.arrowImg.setImageResource(R.drawable.icon_arrow_answer);
	}
	  
	  ViewHolder holder = (ViewHolder) view.getTag();
	    holder.optionName=(TextView)view.findViewById(R.id.answer_option_name);
		holder.selCountText=(TextView)view.findViewById(R.id.answer_option_count);
		holder.arrowImg=(ImageView)view.findViewById(R.id.option_arrow_img);
		
		if (mContentBeanList!=null) {
			holder.selCountText.setText(mContentBeanList.get(itemIndex).selCountStr);
		}else {
//			holder.optionName.setTextColor(mContext.getResources().getColor(R.color.but_red));
//			holder.selCountText.setTextColor(mContext.getResources().getColor(R.color.but_red));
//			holder.arrowImg.setImageResource(R.drawable.icon_arrow_red);
		}
		
		
		itemposition = itemIndex;
		itemView = view;
 }*/

	public interface onCheckListener{
		public void onCheck(int position);
	}
	private onCheckListener onCheckListener = null;
	public void setOnCheckListener(onCheckListener listener){
		onCheckListener = listener;
	 }
}
