package com.routon.inforelease.widget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.routon.ad.element.StringUtils;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.util.ImageUtils;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class PlanPreviewView extends RelativeLayout implements OnPageChangeListener {
	public static final int TYPE_DEFAULT = 0; 
	public static final int TYPE_PLANLIST = 1;
	public static final int TYPE_PLANEDIT = 2;
	public static final int TYPE_PLANPREVIEW = 3;
	   
	private ViewPager picVp;
	private int dotLocation;
	private List<String> imagePathList;
	private OnPreviewClickListener mPreviewClickListener = null;
	private int mType = TYPE_DEFAULT;
	private TextView dotText;
	private AutoScrollTextView mPreviewMsgTextView;
	private ImageLoader mImageLoader;
	
	public interface OnPreviewClickListener {     
	      void onPreviewClickListener(View v, int position);
	}

	public PlanPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public PlanPreviewView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PlanPreviewView(Context context) {
		this(context, null);
	}

	private void init() {
		mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue, new BitmapCache(this.getContext()));  
		View.inflate(getContext(), R.layout.pic_vp_layout, this);
		picVp = (ViewPager) findViewById(R.id.pic_vp);
		mPreviewMsgTextView = (AutoScrollTextView) findViewById(R.id.previewMsgTextView);
		mPreviewMsgTextView.setVisibility(View.VISIBLE);
		mPreviewMsgTextView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});
		
		findViewById(R.id.dot_container).setVisibility(View.GONE);
		dotText = (TextView) findViewById(R.id.dot_text);
		picVp.addOnPageChangeListener(this);
	}
	
	public void setText(String text,boolean scrollable,String textcolor,String textBgColor,String textBgAlpha,int textSize){
		if( text == null || text.trim().length() == 0 ){
			mPreviewMsgTextView.setVisibility(View.GONE);
			return;
		}
		mPreviewMsgTextView.setVisibility(View.VISIBLE);
		mPreviewMsgTextView.setScrollable(scrollable);
		mPreviewMsgTextView.setText(text);		
		mPreviewMsgTextView.setTextSize(textSize);	
		mPreviewMsgTextView.setTextColor(Color.parseColor(textcolor));
		mPreviewMsgTextView.setPadding(0, 6, 0, 12);
		//textBgAlpha(0~255)
		int alpha = Integer.parseInt(textBgAlpha);
		
		String strAlphaColor = String.format("%02X", 0xFF & alpha);
		String textBgAlphaColor = "#"+strAlphaColor+textBgColor.substring(1);
//		Log.d("PlanPreviewView","textBgAlphaColor:"+textBgAlphaColor);
		mPreviewMsgTextView.setBackgroundColor(Color.parseColor(textBgAlphaColor));
		if( scrollable == true ){	
			Activity activity = (Activity)(this.getContext());
			WindowManager wm = activity.getWindowManager();
			mPreviewMsgTextView.setSpecialViewWidth(wm.getDefaultDisplay().getHeight());
			mPreviewMsgTextView.init(wm);
			mPreviewMsgTextView.startScroll();
		}
	}

	public void setItems(List<String> imagePathList, String imagePath, OnPreviewClickListener listener,int type) {
		this.imagePathList = imagePathList;
//		this.imagePath = imagePath;
		picVp.setAdapter(adapter);
		
		mType = type;
		mPreviewClickListener = listener;

		
		dotText.setText(imagePathList.size()+"/"+imagePathList.size());    

		dotLocation = 0;
		for (int i = 0; i < imagePathList.size(); i++) {
			if (imagePathList.get(i).equals(imagePath)) {
				dotLocation = i;
			}
		}
		picVp.setCurrentItem(dotLocation);

		// 初始状态
		onPageSelected(dotLocation);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		if (imagePathList == null) {
			return;
		}
		dotText.setText((arg0+1)+"/"+imagePathList.size());
	}

	private PagerAdapter adapter = new PagerAdapter() {

		@Override
		public int getCount() {
			return imagePathList == null ? 0 : imagePathList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_plan_vp_item, null);
			final ImageView imageView = (ImageView) view.findViewById(R.id.image_vp_item);
			final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);	
			if( imagePathList == null || position >= imagePathList.size() ){
				return view;
			}
			 String path = imagePathList.get(position);
			if( path != null && path.startsWith("/")){
				path = "file://"+path;
			}
			Log.d("planpreviewview","path:"+path);
			
			if ( mType == TYPE_PLANLIST ) {	//节目列表界面
				//实际上是无图片，只有文字的计划，显示默认图片
				if ( path.equals("0")) {
					dotText.setVisibility(View.GONE);
					imageView.setImageResource(R.drawable.horizontal_menu_child_default);
				}else {
					dotText.setVisibility(View.VISIBLE);
					if( path != null && path.isEmpty() == false ){
						Picasso.with(getContext()).load(path)
							.placeholder(R.drawable.horizontal_menu_child_default).into(imageView);
					}else{
						imageView.setImageResource(R.drawable.horizontal_menu_child_default);
					}
				}					
				imageView.setLayoutParams(params);
			} 
			else if ( mType == TYPE_PLANPREVIEW ) {//节目预览界面
				DisplayMetrics dm = new DisplayMetrics();
				((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);

				final int screenWidth = dm.widthPixels;
				final int screenHeight = dm.heightPixels;
				if ( path.equals("0")) {
					imageView.setImageResource(R.drawable.horizontal_menu_child_default);
					imageView.setLayoutParams(params);
					dotText.setVisibility(View.GONE);
				}else if(path.startsWith("file://")) {
					path = path.replaceFirst("file://", "");
					ImageViewHelper.setSpecialBitmap(imageView, ImageUtils.loadBitmap(path, screenWidth, screenHeight),params);
					dotText.setVisibility(View.VISIBLE);
				}
				else {
					mImageLoader.get(path, new ImageListener(){

						@Override
						public void onErrorResponse(VolleyError error) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onResponse(ImageContainer response,
								boolean isImmediate) {
							if( response.getBitmap() != null ){
								ImageViewHelper.setSpecialBitmap(imageView, response.getBitmap(),params);
					}
						}
						
					});
				}
				view.setBackgroundColor(Color.BLACK);

			}else if ( mType == TYPE_PLANEDIT ) {//节目编辑界面
				if ( path.equals("0")) {
					imageView.setImageResource(R.drawable.horizontal_menu_child_default);
					dotText.setVisibility(View.GONE);
				}else {
					if( path != null && path.isEmpty() == false ){
						Picasso.with(getContext()).load(path)
						.placeholder(R.drawable.horizontal_menu_child_default).into(imageView);						
					}else{
						imageView.setImageResource(R.drawable.horizontal_menu_child_default);
					}
					dotText.setVisibility(View.VISIBLE);
				}
				imageView.setLayoutParams(params);
			}

			
			if( mPreviewClickListener != null ){
				view.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						
							mPreviewClickListener.onPreviewClickListener(v, position);
						}
				});
			}
			container.addView(view);
			return view;

		}
	};
	
}
