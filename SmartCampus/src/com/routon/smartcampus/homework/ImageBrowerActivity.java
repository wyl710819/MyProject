package com.routon.smartcampus.homework;

import java.io.File;
import java.util.ArrayList;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.widget.ImageViewHelper;
import com.routon.smartcampus.MainActivity;

import com.routon.edurelease.R;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;
import android.view.ViewGroup;

public class ImageBrowerActivity extends Activity{
	
	private ViewPager viewPager;
	private ArrayList<FeedbackWorkBean> mPicUrlList;
	private int currentPosition;
	private ArrayList<String> listImgPath;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private ImagePagerAdapter myAdapter;
	private boolean isAddRemark;
	private String isHomework;
	private ArrayList<String>mFeedBackPicLists;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.badge_remark_image_preview_layout);
		
		isAddRemark = getIntent().getBooleanExtra(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_ADD_PIC, false);
		mPicUrlList = getIntent().getParcelableArrayListExtra(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST);
		int position=getIntent().getIntExtra(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_POSITION, 0);
		isHomework=getIntent().getStringExtra("homework");
		View title_layout = findViewById(R.id.title_layout);
		TextView delete_view=(TextView)findViewById(R.id.delete_view);
		ImageView back_menu=(ImageView) findViewById(R.id.back_btn);
		if(isHomework!=null&&isHomework.equals("homework")){
			if(isAddRemark){
				delete_view.setVisibility(View.VISIBLE);
			}
			
			title_layout.setVisibility(View.VISIBLE);
			TextView title_view=(TextView) findViewById(R.id.title_view);
			title_view.setText("图片预览"); 
//			title_view.setTextSize(24);
//			delete_view.setTextSize(24);
			RelativeLayout.LayoutParams layoutParams=
				    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			title_view.setLayoutParams(layoutParams);
		}
		back_menu.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				FeedbackWorkBean bean=new FeedbackWorkBean();
				bean.fileUrl="null";
				mPicUrlList.add(bean);
				bean=new FeedbackWorkBean();
				bean.fileUrl="record_null";
				mPicUrlList.add(bean);
				Intent intent = new Intent();
//				mFeedBackPicLists=new ArrayList<String>();
//				for(int i=0;i<mPicUrlList.size();i++){
//					mFeedBackPicLists.add(mPicUrlList.get(i).fileUrl);
//				}
				intent.putParcelableArrayListExtra("img_data", mPicUrlList);
				setResult(RESULT_OK, intent);
				ImageBrowerActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
				
			}
		});
		
		if(isAddRemark){
			delete_view.setVisibility(View.VISIBLE);
			delete_view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

    				mPicUrlList.remove(currentPosition);
    				listImgPath.remove(currentPosition);
    				if (listImgPath.size()>0) {
    					myAdapter = new ImagePagerAdapter(listImgPath);
    			        viewPager.setAdapter(myAdapter);
    			        if (currentPosition>0) {
    			        	viewPager.setCurrentItem(currentPosition-1);
    					}else {
    						viewPager.setCurrentItem(currentPosition);
    					}
    			        
    				}else {
    					FeedbackWorkBean bean=new FeedbackWorkBean();
    					bean.fileUrl="null";
    					mPicUrlList.add(bean);
    					bean=new FeedbackWorkBean();
    					bean.fileUrl="record_null";
    					mPicUrlList.add(bean);
    					Intent intent = new Intent();
//    					mFeedBackPicLists=new ArrayList<String>();
//    					for(int i=0;i<mPicUrlList.size();i++){
//    						mFeedBackPicLists.add(mPicUrlList.get(i).fileUrl);
//    					}
    					intent.putParcelableArrayListExtra("img_data", mPicUrlList);
    					setResult(RESULT_OK, intent);
    					finish();
    				}
    				
    			
				}
			});
		}
        viewPager = (ViewPager)findViewById(R.id.viewPager); 
        vpCountView = (TextView) findViewById(R.id.image_vp_count);
        
        imageLoader.init(ImageLoaderConfiguration.createDefault(ImageBrowerActivity.this));
    	options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.empty_photo)
				.showImageOnFail(R.drawable.empty_photo)
				.resetViewBeforeLoading(true)
				.cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300))
				.build();
    	 
    	listImgPath=new ArrayList<String>();
    	
		if (mPicUrlList!=null && mPicUrlList.size()>0) {
			if (isAddRemark) {
				mPicUrlList.remove(mPicUrlList.size()-1);
				mPicUrlList.remove(mPicUrlList.size()-1);
			}
			
			
			for (int i = 0; i < mPicUrlList.size(); i++) {
				if(!(mPicUrlList.get(i).fileType==2)){
					if (mPicUrlList.get(i).fileUrl.startsWith("http")) {
						listImgPath.add(mPicUrlList.get(i).fileUrl);
					}else {
						listImgPath.add("file://" + mPicUrlList.get(i).fileUrl);
					}
				}
			}
			
			myAdapter = new ImagePagerAdapter(listImgPath);
        viewPager.setAdapter(myAdapter);
        viewPager.setCurrentItem(position);
		}
		
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				currentPosition = arg0;
				vpCountView.setText((arg0+1)+"/"+listImgPath.size());
			}

			@Override
			public void onPageSelected(int arg0) {
			}
		});
	}
	
	
	private TextView vpCountView;
	
    private class ImagePagerAdapter extends PagerAdapter {

		private ArrayList<String> images;
		private LayoutInflater inflater;
		

		ImagePagerAdapter(ArrayList<String> images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return images.size();
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.remark_vp_item, view, false);
			assert imageLayout != null;
			ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image_vp_item);
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			
//			if (isAddRemark) {
				imageLoader.displayImage(images.get(position), imageView, options, new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						spinner.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						String message = null;
						switch (failReason.getType()) {
							case IO_ERROR:
								message = "Input/Output error";
								break;
							case DECODING_ERROR:
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED:
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY:
								message = "Out Of Memory error";
								break;
							case UNKNOWN:
								message = "Unknown error";
								break;
						}
//						Toast.makeText(RemarkImagePreviewActivity.this, message, Toast.LENGTH_SHORT).show();

						spinner.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						spinner.setVisibility(View.GONE);
					}
				});
//			}else {
//				mImageLoader.get(path, new ImageListener(){
//
//					@Override
//					public void onErrorResponse(VolleyError error) {
//						// TODO Auto-generated method stub
//						
//					}
//
//					@Override
//					public void onResponse(ImageContainer response,
//							boolean isImmediate) {
//						if( response.getBitmap() != null ){
//							ImageViewHelper.setSpecialBitmap(imageView, response.getBitmap(),params);
//				}
//					}
//					
//				});
//			}

			

			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	 if ((keyCode == KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN)) {    
    		 FeedbackWorkBean bean=new FeedbackWorkBean();
				bean.fileUrl="null";
				mPicUrlList.add(bean);
				bean=new FeedbackWorkBean();
				bean.fileUrl="record_null";
				mPicUrlList.add(bean);
				Intent intent = new Intent();
//				mFeedBackPicLists=new ArrayList<String>();
//				for(int i=0;i<mPicUrlList.size();i++){
//					mFeedBackPicLists.add(mPicUrlList.get(i).fileUrl);
//				}
				intent.putParcelableArrayListExtra("img_data", mPicUrlList);
				setResult(RESULT_OK, intent);
        }   
    	return super.onKeyDown(keyCode, event);
    }
		

}
