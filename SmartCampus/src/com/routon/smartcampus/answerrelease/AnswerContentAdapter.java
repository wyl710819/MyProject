package com.routon.smartcampus.answerrelease;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.routon.edurelease.R;

public class AnswerContentAdapter extends BaseAdapter{

	private Context mContext;
	private List<String> optionList;
	private int mPosition=-1;
	//选项点击事件回调
	private OPtionOnClickListener mClickListener = null;
    
	public interface OPtionOnClickListener {
		public void optionClick(View v, int position,ImageView answerTrueBtn,TextView optionView);
	}
	public void setClickListener(OPtionOnClickListener mClickListener) {
		this.mClickListener = mClickListener;
	}
	//回答正确按钮点击事件回调
	private AwardFlowerClickListener mAwardFlowerClickListener=null;
	public interface AwardFlowerClickListener{
		public void awardClick(View v,int position);
	}
	public void setAwardClickListener(AwardFlowerClickListener mAwardFlowerClickListener){
		this.mAwardFlowerClickListener=mAwardFlowerClickListener;
	}
	public AnswerContentAdapter(Context context,List<String> optionList){
		mContext=context;
		this.optionList=optionList;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return optionList.size()-2;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return optionList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if(convertView==null){
			holder=new ViewHolder();
			convertView = View.inflate(mContext, R.layout.select_content_item, null);
			holder.bgView=convertView.findViewById(R.id.select_content_bg);
			holder.tvOption=(TextView) convertView.findViewById(R.id.select_content);
			holder.answerTrueBtn=(ImageView) convertView.findViewById(R.id.img_answer_true_btn);
			convertView.setTag(holder);
		}
		else{
			holder=(ViewHolder) convertView.getTag();
		}
		
		holder.tvOption.setText(optionList.get(position));
		if(Integer.valueOf(optionList.get(7))==position){
			
			holder.bgView.setVisibility(View.VISIBLE);
			holder.tvOption.setBackgroundResource(R.drawable.answer_green_bg);
			holder.answerTrueBtn.setVisibility(View.VISIBLE);
		}else{
			holder.bgView.setVisibility(View.INVISIBLE);
			holder.tvOption.setBackgroundResource(R.drawable.answer_gray_bg);
			holder.answerTrueBtn.setVisibility(View.INVISIBLE);
		}
		holder.tvOption.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mClickListener!=null){
					mClickListener.optionClick(v, position,holder.answerTrueBtn,holder.tvOption);
				}
			}
		});
		holder.answerTrueBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mAwardFlowerClickListener!=null){
					mAwardFlowerClickListener.awardClick(v, position);
				}
			}
		});
		return convertView;
	}
	public class ViewHolder{
		private View bgView;
		private TextView tvOption;
		private ImageView answerTrueBtn;
	}

}
