package com.routon.smartcampus.guide;


import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.routon.edurelease.R;
 
/**
 * Copyright: Copyright (c) 2017-2025
 * Company:
 * 实现首次启动的引导页面
 * @author: 赵小贱()
 * @date: 2017/10/18
 * describe:
 */
public class GuideActivity extends Activity implements ViewPager.OnPageChangeListener{
 
    private ViewPager vp;
    private String[] imageIdArray;//图片资源的数组
    private List<View> viewList;//图片资源的集合
    private ViewGroup vg;//放置圆点
 
    //实例化原点View
    private ImageView iv_point;
    private ImageView []ivPointArray;
 
    //最后一页的按钮
    private ImageButton ib_start;
    private LinearLayout.LayoutParams layoutParams;
    
    public static final String INTENT_URI_TAG = "IntentUri";
    public static final String IMAGES_ARRAY_TAG = "Images";
    public static final String INTENT_BUNDLES_TAG = "Bundles";
    public static final String INTENT_SERIAL = "Serializable";
    public static final String INTENT_SERIAL_STR = "SerializableStr";
    
    private String mIntentUri = null;
    private Bundle mIntentBundle = null;
    private String mSerialStr = null;
    private Serializable mSerial = null;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_guide);
        
        mIntentUri = this.getIntent().getStringExtra(INTENT_URI_TAG);   
        mIntentBundle = this.getIntent().getBundleExtra(INTENT_BUNDLES_TAG);
        mSerial = this.getIntent().getSerializableExtra(INTENT_SERIAL);
        mSerialStr = this.getIntent().getStringExtra(INTENT_SERIAL_STR);   

        imageIdArray =  this.getIntent().getStringArrayExtra(IMAGES_ARRAY_TAG);//new String[]{"files/0_2_0.png","files/0_3_0.png","files/0_8_0.png"};
        
        ib_start = (ImageButton) findViewById(R.id.guide_ib_start);
        ib_start.setEnabled(true);
        ib_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	startNewActivity(); 
            }
        });
 
        //加载ViewPager
        initViewPager();
 
        //加载底部圆点
        initPoint();
    }
    
    void startNewActivity(){
    	Log.d("GuideActivity","startNewActivity");
    	GuideHelper.saveAddImages(GuideActivity.this, imageIdArray);
    	Intent intent;
		try {
			//uri并不会对bundle参数进行处理
			intent = Intent.getIntent(mIntentUri);
			if( mIntentBundle != null ){	
				intent.putExtras(mIntentBundle);
			}
			if( mSerialStr != null && mSerial != null){	
				intent.putExtra(mSerialStr, mSerial);
			}
			startActivity(intent);
            finish();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
    }
 
    /**
     * 加载底部圆点
     */
    private void initPoint() {
    	if( viewList.size() == 1 ){
    		return;
    	}
        //这里实例化LinearLayout
        vg = (ViewGroup) findViewById(R.id.guide_ll_point);
        //根据ViewPager的item数量实例化数组
        ivPointArray = new ImageView[viewList.size()];
        //循环新建底部圆点ImageView，将生成的ImageView保存到数组中
        int size = viewList.size();
        for (int i = 0;i<size;i++){
            iv_point = new ImageView(this);
            layoutParams = new LinearLayout.LayoutParams(15,15);
            iv_point.setBackgroundResource(R.drawable.dot_selector);
 
            //第一个页面需要设置为选中状态，这里采用两张不同的图片
            if (i == 0){
                iv_point.setEnabled(false);
            }else{
                layoutParams.leftMargin=20;
                iv_point.setEnabled(true);
            }
            iv_point.setLayoutParams(layoutParams);
            iv_point.setPadding(30,0,30,0);//left,top,right,bottom
            ivPointArray[i] = iv_point;
 
            //将数组中的ImageView加入到ViewGroup
            vg.addView(ivPointArray[i]);
        }
    }
    
    private Bitmap getImageFromAssetsFile(String fileName)  
    {  
        Bitmap image = null;  
        AssetManager am = getResources().getAssets();  
        try  
        {  
            InputStream is = am.open(fileName);  
            image = BitmapFactory.decodeStream(is);  
            is.close();  
        }  
        catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
    
        return image;  
    
    }  
 
    /**
     * 加载图片ViewPager
     */
    private void initViewPager() {
        vp = (ViewPager) findViewById(R.id.guide_vp);
        viewList = new ArrayList<>();
        //获取一个Layout参数，设置为全屏
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        
        //循环创建View并加入到集合中
        int len = imageIdArray.length;
        for (int i = 0;i<len;i++){
            //new ImageView并设置全屏和图片资源
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params);
            imageView.setBackground(new BitmapDrawable(getResources(),getImageFromAssetsFile(GuideHelper.ASSET_DIR_FILE+"/"+imageIdArray[i])));
            //将ImageView加入到集合中
            viewList.add(imageView);
        }
 
        //View集合初始化好后，设置Adapter
        vp.setAdapter(new GuidePageAdapter(viewList));
        vp.setOnPageChangeListener(this);
        
        viewList.get(viewList.size()-1).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startNewActivity();
			}
		});
        viewList.get(viewList.size()-1).setEnabled(true);
        if( viewList.size() == 1 ){ 
        	ib_start.setVisibility(View.VISIBLE);
        }
    }
 
 
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
 
    }
 
    /**
     * 滑动后的监听
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
        //循环设置当前页的标记图
        int length = imageIdArray.length;
        for (int i = 0;i<length;i++){
            ivPointArray[position].setEnabled(false);
            if (position != i){
                ivPointArray[i].setEnabled(true);
            }
        }
 
        //判断是否是最后一页，若是则显示按钮
        if (position == imageIdArray.length - 1){
            ib_start.setVisibility(View.VISIBLE);
        }else {
            ib_start.setVisibility(View.GONE);
        }
    }
    @Override
    public void onPageScrollStateChanged(int state) {
 
    }
}
