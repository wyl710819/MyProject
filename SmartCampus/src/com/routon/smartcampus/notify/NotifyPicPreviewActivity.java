package com.routon.smartcampus.notify;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.smartcampus.flower.RemarkImagePreviewActivity;
import com.routon.smartcampus.utils.MyBundleName;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NotifyPicPreviewActivity extends CustomTitleActivity{
	
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private ImagePagerAdapter myAdapter;
	private ViewPager viewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.badge_remark_image_preview_layout);
		
		mPicUrlList = getIntent().getStringArrayListExtra(MyBundleName.BADGE_REMARK_PIC_LIST);
		int position = getIntent().getIntExtra(MyBundleName.BADGE_REMARK_PIC_POSITION, 0);
		
		initTitleBar("图片预览");
		setTitleBackground(this.getResources().getDrawable(R.drawable.leave_title_bg));
		
		setTitleBackBtnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		setMoveBackEnable(false);
		
		 viewPager = (ViewPager) findViewById(R.id.viewPager); 
	        vpCountView = (TextView) findViewById(R.id.image_vp_count);
	        
	        imageLoader.init(ImageLoaderConfiguration.createDefault(NotifyPicPreviewActivity.this));
	    	options = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(R.drawable.empty_photo)
					.showImageOnFail(R.drawable.empty_photo)
					.resetViewBeforeLoading(true)
					.cacheOnDisc(true)
					.imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.displayer(new FadeInBitmapDisplayer(300))
					.build();
	    	
	    	myAdapter = new ImagePagerAdapter(mPicUrlList);
	        viewPager.setAdapter(myAdapter);
	        viewPager.setCurrentItem(position);
	    	
	    	viewPager.setOnPageChangeListener(new OnPageChangeListener() {
				
				@Override
				public void onPageScrollStateChanged(int arg0) {
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					vpCountView.setText((arg0+1)+"/"+mPicUrlList.size());
				}

				@Override
				public void onPageSelected(int arg0) {
				}
			});
	}
	
private TextView vpCountView;
private ArrayList<String> mPicUrlList;
	
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
			
			
				imageLoader.displayImage(images.get(position), imageView, options, new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						spinner.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						switch (failReason.getType()) {
							case IO_ERROR:
								break;
							case DECODING_ERROR:
								break;
							case NETWORK_DENIED:
								break;
							case OUT_OF_MEMORY:
								break;
							case UNKNOWN:
								break;
						}

						spinner.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						spinner.setVisibility(View.GONE);
					}
				});

			

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

}
