package com.routon.smartcampus.flower;

import java.util.ArrayList;

import com.routon.edurelease.R;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RemarkEditPopWin extends PopupWindow implements OnClickListener {

	private Context mContext;
	private View view;
	private BadgeInfo mBean;
	public TextView titleEditView;
	private EditText remarkEditView;
	private ImageView decreaseView;
	private ImageView increaseView;
	private TextView bonuspointView;
	private TextView finishBtn;
	private ArrayList<BadgeInfo> mbadgeList;
	private int positionTag;

	public RemarkEditPopWin(Context context, BadgeInfo bean,ArrayList<BadgeInfo> badgeList, int position, OnFinishListener listener) {
		this.mContext=context;
		this.mBean=bean;
		this.onFinishListener=listener;
		this.mbadgeList=badgeList;
		this.positionTag=position;
		
		if (mBean.bonuspoint>=0 && mBean.prop==1) {
        	mBean.prop=0;
        }else if(mBean.bonuspoint<0){
        	mBean.prop=1;
		}
		
		init();
		initData();
	}
	
	public RemarkEditPopWin(Context context, BadgeInfo bean,OnFinishListener listener) {
		this.mContext=context;
		this.mBean=bean;
		this.onFinishListener=listener;
		
		 if (mBean.bonuspoint>=0 && mBean.prop==1) {
	        	mBean.prop=0;
	        }else if(mBean.bonuspoint<0){
	        	mBean.prop=1;
			}
		 
		init();
		initData();
	}


	private void init() {
		
		view = LayoutInflater.from(mContext).inflate(R.layout.often_badge_edit_layout, null);
		this.setOutsideTouchable(true);
		this.setContentView(this.view);
		this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
		this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
		this.setFocusable(true);
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		this.setBackgroundDrawable(dw);
		this.setAnimationStyle(R.style.take_photo_anim);
		
		
		titleEditView = (TextView) view.findViewById(R.id.title_edit_view);
		remarkEditView = (EditText) view.findViewById(R.id.remark_edit_view);
		decreaseView = (ImageView) view.findViewById(R.id.decrease_img);
		increaseView = (ImageView) view.findViewById(R.id.increase_img);
		bonuspointView = (TextView) view.findViewById(R.id.bonuspoint_text);
		finishBtn = (TextView) view.findViewById(R.id.finish_btn);
		
		
		decreaseView.setOnClickListener(this);
		increaseView.setOnClickListener(this);
		finishBtn.setOnClickListener(this);
	}
	
	private void initData() {
		titleEditView.setText(mBean.badgeTitle);
//		if (mBean.badgeTitle!=null) {
//			titleEditView.setSelection(mBean.badgeTitle.length());
//		}
		
		remarkEditView.setText(mBean.badgeRemark);
		bonuspointView.setText(String.valueOf(mBean.bonuspoint));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.decrease_img:
			setBonuspoint(-1);
			break;
		case R.id.increase_img:
			setBonuspoint(1);
			break;
		case R.id.finish_btn:
			if (!String.valueOf(titleEditView.getText()).trim().equals("")) {
				
				if (mbadgeList!=null) {
					for (int i = 0; i < mbadgeList.size(); i++) {
						if (mbadgeList.get(i).badgeTitle.equals(titleEditView.getText().toString()) && i != positionTag) {
							Toast.makeText(mContext, "常用小红花标题不能重复！", Toast.LENGTH_SHORT).show();
							 return;
						}
					}
				}
				
				mBean.badgeTitle=String.valueOf(titleEditView.getText());
				mBean.badgeRemark=String.valueOf(remarkEditView.getText());
				mBean.bonuspoint=Integer.valueOf(bonuspointView.getText().toString());
				
				
				
				
				
				if (onFinishListener!=null) {
					OftenBadgeBean oftenBadgeBean=new OftenBadgeBean();
					oftenBadgeBean.badgeId=mBean.badgeId;
					oftenBadgeBean.badgeTitle=mBean.badgeTitle;
					oftenBadgeBean.badgeRemark=mBean.badgeRemark;
					oftenBadgeBean.bonuspoint=mBean.bonuspoint;
					oftenBadgeBean.id=mBean.id;
					oftenBadgeBean.imgUrl=mBean.imgUrl;
					oftenBadgeBean.prop=mBean.prop;
					oftenBadgeBean.badgeTitleId=mBean.badgeTitleId;
					
					
					onFinishListener.onFinishClick(oftenBadgeBean);
				}
			}else {
				Toast.makeText(mContext, "评语标题不能为空", Toast.LENGTH_SHORT).show();
			}
			
			break;

		default:
			break;
		}
	}

	private void setBonuspoint(int count) {
		int bonuspoint =Integer.valueOf(bonuspointView.getText().toString());
		if (mBean.prop==0) {//正面
			if (bonuspoint+count>10 || bonuspoint+count<0) {
				return;
			}
		}else {//负面
			if (bonuspoint+count>0 || bonuspoint+count<-10) {
				return;
			}
		}
		
		bonuspointView.setText(String.valueOf(bonuspoint+count));
	}

	public interface OnFinishListener {

		public void onFinishClick(OftenBadgeBean bean);
	}

	private OnFinishListener onFinishListener = null;

	public void setOnFinishListener(OnFinishListener listener) {
		onFinishListener = listener;
	}
}
