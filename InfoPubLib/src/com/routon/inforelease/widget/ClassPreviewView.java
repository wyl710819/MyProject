package com.routon.inforelease.widget;

import java.io.File;
import java.util.List;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.routon.ad.element.StringUtils;
import com.routon.ad.pkg.FileGetTask;
import com.routon.ad.pkg.HttpGetTask;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.util.ImageUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ClassPreviewView extends RelativeLayout implements OnPageChangeListener {
	
   public static final int TYPE_DEFAULT = 0; 
   public static final int TYPE_CLASSLIST = 1;
   public static final int TYPE_CLASSEDIT = 2;
   public static final int TYPE_CLASSPREVIEW = 3;
   public interface OnPreviewClickListener {
     
       void onPreviewClickListener(View v, int position);
   }

	private ViewPager picVp;
	private List<String> imagePathList;
	private ClassInfoListdatasBean mBean;
//	private ClassInfoEditActivity mClassInfoEditActivity;
//	private String mStartBy;
	private ImageLoader mImageLoader;
	private OnPreviewClickListener mPreviewClickListener = null;
	private int mType = TYPE_DEFAULT;

	public ClassPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public ClassPreviewView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClassPreviewView(Context context) {
		this(context, null);
	}

	private void init() {
		mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue, new BitmapCache(this.getContext()));  

		View.inflate(getContext(), R.layout.pic_vp_layout, this);
		picVp = (ViewPager) findViewById(R.id.pic_vp);
		dotContainer = (LinearLayout) findViewById(R.id.dot_container);

		picVp.setOnPageChangeListener(this);

	}

//	public void setItems(List<String> imagePathList, String imagePath, String mStartBy,ClassInfoListdatasBean bean) {
//
//		this.mStartBy = mStartBy;
//		this.imagePathList = imagePathList;
//		this.mBean = bean;
//		picVp.setAdapter(adapter);
//
//		// dotcontainer
//		dotContainer.removeAllViews();
//		addDots();
//		dotLocation = 0;
//		for (int i = 0; i < imagePathList.size(); i++) {
//			if (imagePathList.get(i).equals(imagePath)) {
//				dotLocation = i;
//			}
//		}
//		picVp.setCurrentItem(dotLocation);
//
//		// 初始状态
//		onPageSelected(dotLocation);
//	}

	public void setItems(List<String> imagePathList, String imagePath, ClassInfoListdatasBean bean,
			OnPreviewClickListener listener,int type) {
//		this.mClassInfoEditActivity = classInfoEditActivity;
		mType = type;
		mPreviewClickListener = listener;
		this.mBean = bean;
		this.imagePathList = imagePathList;
		picVp.setAdapter(adapter);

		// dotcontainer
		dotContainer.removeAllViews();
		addDots();
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

	private void addDots() {
		if (imagePathList == null) {
			return;
		}

		int dotWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
				getResources().getDisplayMetrics());

		for (int i = 0; i < imagePathList.size(); i++) {
			View dotView = new View(getContext());

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dotWidth, dotWidth);
			lp.setMargins(0, 0, dotWidth, 0);

			dotView.setLayoutParams(lp);
			dotView.setBackgroundResource(R.drawable.dot_selector);

			dotContainer.addView(dotView);
		}
	}
	private TextView titleView;
	private TextView subTitle1View;
	private TextView subTitle2View;
	private TextView escView;
	private String mTitle;
	private String mSubTitle1;
	private String mSubTitle2;
	private String mDesc;
	public void updateText(String title, String subTitle1, String subTitle2, String desc){
		isUpdateText = true;
		this.mTitle=title;
		this.mSubTitle1=subTitle1;
		this.mSubTitle2=subTitle2;
		this.mDesc=desc;
		onPageSelected(0);
		picVp.setAdapter(adapter);
	}
	
	private PagerAdapter adapter = new PagerAdapter() {
		private LinearLayout titleLayout;
		
		

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
			DisplayMetrics  dm = new DisplayMetrics();    
			((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);    
		         
		    final int screenWidth = dm.widthPixels;	      
		      
			View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_class_info_edit_grid_item, null);
				// final RelativeLayout titlebar=(RelativeLayout)
				// view.findViewById(R.id.titlebar);
			final ImageView imageView = (ImageView) view.findViewById(R.id.image);
			titleView = (TextView) view.findViewById(R.id.item_title);
			subTitle1View = (TextView) view.findViewById(R.id.item_subtitle1);
			subTitle2View = (TextView) view.findViewById(R.id.item_subtitle2);
			escView = (TextView) view.findViewById(R.id.item_desc);
			titleLayout = (LinearLayout)view. findViewById(R.id.item_title_layout);
			if (isUpdateText) {
				titleView.setText(mTitle);
				subTitle1View.setText(mSubTitle1);
				subTitle2View.setText(mSubTitle2);
				escView.setText(mDesc);
			}else{
				if (mBean != null ) {
					titleView.setText(mBean.title);
					subTitle1View.setText(mBean.subtitle1);
					subTitle2View.setText(mBean.subtitle2);
					escView.setText(mBean.desc);
					}
			}
			
			if ( mType == TYPE_CLASSEDIT ) {//内容编辑界面
				//下载小图片
//				String smallImageUrl = ImageUtils.getSmallImageUrl(getContext(), imagePathList.get(position));
////				Log.d("ClassPreviewView","smallImageUrl:"+smallImageUrl);
//				mImageLoader.get(smallImageUrl, new ImageListener(){
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
//						// TODO Auto-generated method stub
////						Log.d("ClassPreviewView","bitmap "+response.getBitmap());
////						Log.d("ClassPreviewView","bitmap size "+response.getBitmap().getWidth()+","+response.getBitmap().getHeight());
//						RelativeLayout.LayoutParams editParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
//								LayoutParams.MATCH_PARENT);
//						if( response.getBitmap() != null ){
//							ImageViewHelper.setSpecialBitmap(imageView, response.getBitmap(),editParams);
//						}
//					}
//					
//				});
//						
//				view.setBackgroundColor(Color.BLACK);
				
				String smallImageUrl = ImageUtils.getSmallImageUrl(getContext(), imagePathList.get(position));
				ImageListener listener = ImageLoader.getImageListener(imageView, 0, 0);  
				mImageLoader.get(smallImageUrl, listener);					
				RelativeLayout.LayoutParams editParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);				
				imageView.setLayoutParams(editParams);
				
			}else if ( mType == TYPE_CLASSLIST ) {//内容列表界面	
				String smallImageUrl = ImageUtils.getSmallImageUrl(getContext(), imagePathList.get(position));
//				Log.d("ClassPreviewView","smallImageUrl:"+smallImageUrl);
				ImageListener listener = ImageLoader.getImageListener(imageView, 0, 0);  
				mImageLoader.get(smallImageUrl, listener);					
				RelativeLayout.LayoutParams editParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);				
				imageView.setLayoutParams(editParams);					
			} else if ( mType == TYPE_CLASSPREVIEW ) {//内容预览界面
				mImageLoader.get(imagePathList.get(position), new ImageListener(){

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onResponse(ImageContainer response,
							boolean isImmediate) {
						RelativeLayout.LayoutParams editParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT);
//						Log.d("ClassPreviewView","bitmap size "+response.getBitmap().getWidth()+","+response.getBitmap().getHeight());
						if( response.getBitmap() != null ){
							ImageViewHelper.setSpecialBitmap(imageView, response.getBitmap(),editParams);
						}
					}
					
				});
				view.setBackgroundColor(Color.BLACK);
				
				RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT);
				titleView.setTextSize(24);
				subTitle1View.setTextSize(20);
				subTitle2View.setTextSize(20);
				escView.setTextSize(18);
				titleParams.setMargins(0, screenWidth/2+50, 0, 0);									
				titleLayout.setLayoutParams(titleParams);
				
			} else {
				ImageListener listener = ImageLoader.getImageListener(imageView, 0, 0);  
				mImageLoader.get(imagePathList.get(position), listener);					
				RelativeLayout.LayoutParams editParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);				
				imageView.setLayoutParams(editParams);					
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

	private LinearLayout dotContainer;
	private int dotLocation;

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

//	private Bitmap mBitmap;
//	private Bitmap loadImage(int position) {
//		mBitmap=null;
//		String url =imagePathList.get(position);
//		String name = StringUtils.getFileName(url);
//		File file = new File(getContext().getCacheDir(), name);
//		String path = file.getPath();
//		
//		if (file.exists()) {
//			mBitmap = BitmapFactory.decodeFile(path);
//
//			return mBitmap;
//		}
//		FileGetTask task = new FileGetTask(url, path, url);
//		task.setOnHttpGetTaskListener(new HttpGetTask.OnHttpGetTaskListener() {
//
//			@Override
//			public void onTaskStarted(HttpGetTask task) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void onTaskFinished(HttpGetTask task, int code) {
//
//				if (code == HttpGetTask.ERR_NONE) {
//					FileGetTask t = (FileGetTask) task;
//					String path = t.getFilePath();
//					mBitmap = BitmapFactory.decodeFile(path);
//				}
//			}
//		});
//		new Thread(task).start();
//		return mBitmap;
//	}
	
//	private void getBitmap(int position) {
//		HttpURLConnection connection = null;
//		InputStream input = null;
//		try {
//			URL url = new URL(imagePathList.get(position));
//			connection=(HttpURLConnection) url.openConnection();
//			connection.setDoInput(true);
//			connection.connect();
//			input=connection.getInputStream();
//			mBitmap = BitmapFactory.decodeStream(input);
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
////			getBitmap(position);
//		}finally{
//			if( connection != null ){
//				connection.disconnect();
//			}
//			if( input != null ){
//				try {
//					input.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		
//	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		if (imagePathList == null) {
			return;
		}

		for (int i = 0; i < adapter.getCount(); i++) {
			dotContainer.getChildAt(i).setEnabled(arg0 != i);
		}
	}

	private boolean isUpdateText=false;

//	@Override
//	public boolean dispatchTouchEvent(MotionEvent event) {
//		switch (event.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			xDown = event.getRawX();
//			yDown = event.getRawY();
//			break;
//		case MotionEvent.ACTION_MOVE:
//			xMove = event.getRawX();
//			yMove = event.getRawY();
//			// 滑动的距离
//			distanceX = (int) (xMove - xDown);
//			distanceY = (int) (yMove - yDown);
//
//			break;
//		case MotionEvent.ACTION_UP:
//			if (distanceX < 5 && distanceX > -5) {
//				getParent().requestDisallowInterceptTouchEvent(false);
//			}
//			break;
//		case MotionEvent.ACTION_CANCEL:
//
//			break;
//		default:
//			break;
//		}
//		return super.dispatchTouchEvent(event);
//
//	}
	

}
