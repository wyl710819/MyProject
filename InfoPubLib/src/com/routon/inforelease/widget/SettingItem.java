package com.routon.inforelease.widget;

import com.routon.inforelease.R;

import android.content.Context;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingItem extends RelativeLayout {
	private TextView mTvName = null;
	private TextView mTvInfo = null;
	private ImageView mInfoIcon = null;
	private ImageView infoImg;

	public SettingItem(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView(context);
	}

    public SettingItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SettingItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }
    
    private void initView(Context context){
    	LayoutInflater.from(context).inflate(R.layout.setting_item, this);  
    	infoImg = (ImageView) findViewById(R.id.info_img);
    	mTvName = (TextView)findViewById(R.id.name);
    	mTvInfo = (TextView)findViewById(R.id.info);
    	mInfoIcon = (ImageView)findViewById(R.id.infoicon);
    }
    public void setInfoImg(Integer integer){
    	infoImg.setBackgroundResource(integer);
    	infoImg.setVisibility(View.VISIBLE);
    }
    public void setName(String name){
    	mTvName.setText(name);
    }
    
    public void setName(Spanned name){
    	mTvName.setText(name);
    }
    
    public void setName(int name){
    	mTvName.setText(this.getContext().getResources().getString(name));
    }
    
    public void setInfo(String info){
    	mTvInfo.setText(info);
    }
    
    public void setInfoColor(int color){
    	mTvInfo.setTextColor(color);
    }
    
    public String getInfo(){
    	return mTvInfo.getText().toString();
    }
    
    public void setInfoMaxWidth(int width){
    	mTvInfo.setMaxWidth(width);
    }
    
    public void hideBottomLine(){
    	findViewById(R.id.bottom_line).setVisibility(View.GONE);
    }
    
    public void showNewMark(boolean newMark){
    	if( newMark == true ){
    		findViewById(R.id.dot_iv).setVisibility(View.VISIBLE);
    	}else{
    		findViewById(R.id.dot_iv).setVisibility(View.INVISIBLE);
    	}
    	
    	
    }
    
    public void setMoreClicked(boolean clicked){
    	if( clicked == true ){
    		mInfoIcon.setVisibility(View.VISIBLE);
    	}else{
    		mInfoIcon.setVisibility(View.INVISIBLE);
    	}
    }
}
