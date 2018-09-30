package com.routon.inforelease.widget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.routon.ad.element.PicInfo;
import com.routon.ad.element.TemplateEditInfo;
import com.routon.inforelease.R;
import com.routon.inforelease.util.TimeUtils;

public class PictureEditView extends ViewGroup {
	
	// Add
	private List<TemplateEditInfo> templateEditInfos = new ArrayList<TemplateEditInfo>();
	private List<View> mViews = new ArrayList<View>();
	private int position = -1;
	private static final String TAG = "PicEdit";
	private Bitmap mBitmap;
	private int mBitmapWidthOnScreen = 0;

	private Paint mFramePaint;
	public static final int FOCUS_NONE = 0;
	public static final int FOCUS_ON_TEXT = 1;
	public static final int FOCUS_ON_IMAGE = 2;
	//静态变量，全局变量
	private static int mTextSize = 60;
	private static int mTextColor = 0xff000000;
	
	public boolean mModified = false;

	public PictureEditView(Context context) {
		super(context);
		init();
	}

	public PictureEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PictureEditView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		mFramePaint = new Paint();
		mFramePaint.setStyle(Paint.Style.STROKE);
		mFramePaint.setStrokeWidth(2);
		mFramePaint.setColor(0xff808080);
		PathEffect effects = new DashPathEffect(new float[] { 5, 5, 5, 5 }, 1);
		mFramePaint.setPathEffect(effects);
	}

	@Override
	protected void onLayout(boolean arg0, int left, int top, int right,
			int bottom) {
		// Log.v(TAG, "onLayout l: " + left + " t: " + top + " r: " + right +
		// " b: " + bottom);
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);

			int l = child.getLeft();
			int t = child.getTop();
			int r = l + child.getMeasuredWidth();
			int b = child.getTop() + child.getMeasuredHeight();
			// Log.v(TAG, "onLayout: " + i + " l: " + l + " t: " + t + " r: " +
			// r + " b: " + b);
			child.layout(l, t, r, b);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);

			measureChild(child, widthMeasureSpec, heightMeasureSpec);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private float mBitmapScale = 1;

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mBitmap != null) {
			// canvas.drawBitmap(mBitmap, 0, 0, null);
			int bw = mBitmap.getWidth();
			int bh = mBitmap.getHeight();

			int w = this.getWidth();// canvas.getWidth();
			int h = this.getHeight();// canvas.getHeight();

			Matrix matrix = new Matrix();

			float sx = (float) w / bw;
			float sy = (float) h / bh;
			// Log.v(TAG, "sx: " + sx + " sy: " + sy);

			int dw = w;
			int dh = h;
			if (sx < sy) {
				dh = (int) (bh * sx);
				matrix.postScale(sx, sx);
				mBitmapScale = sx;
			} else {
				dw = (int) (bw * sy);
				matrix.postScale(sy, sy);
				mBitmapScale = sy;
			}

			// 等比例缩放显示
			canvas.drawBitmap(mBitmap, matrix, null);
			
			mBitmapWidthOnScreen = (int) (mBitmap.getWidth()*mBitmapScale);

			Rect src = new Rect(0, 0, dw, dh);
			canvas.drawRect(src, mFramePaint);

			super.dispatchDraw(canvas);
		} else {
			super.dispatchDraw(canvas);
		}
	}

	public static synchronized Bitmap decodeSampledBitmapFromUri(
			Context context, Uri imageUri, int reqWidth, int reqHeight) {

		InputStream in = null;
		try {
			in = context.getContentResolver().openInputStream(imageUri);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (in == null) {
			return null;
		}

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		//Log.d(TAG, "calculateInSampleSize height:" + options.outHeight + ",width:" + options.outWidth);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		Log.d(TAG, "decodeSampledBitmapFromStream inSampleSize:"
				+ options.inSampleSize);

		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			in = context.getContentResolver().openInputStream(imageUri);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);

		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bitmap;
	}

	/**
	 * Calculate an inSampleSize for use in a {@link BitmapFactory.Options}
	 * object when decoding bitmaps using the decode* methods from
	 * {@link BitmapFactory}. This implementation calculates the closest
	 * inSampleSize that will result in the final decoded bitmap having a width
	 * and height equal to or larger than the requested width and height. This
	 * implementation does not ensure a power of 2 is returned for inSampleSize
	 * which can be faster when decoding but results in a larger bitmap which
	 * isn't as useful for caching purposes.
	 * 
	 * @param options
	 *            An options object with out* params already populated (run
	 *            through a decode* method with inJustDecodeBounds==true
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		Log.d(TAG, "calculateInSampleSize height:" + height + ",width:" + width);
		int inSampleSize = 1;

		// 先根据宽度进行缩小
		while (width / inSampleSize > reqWidth) {
			inSampleSize++;
		}
		// 然后根据高度进行缩小
		while (height / inSampleSize > reqHeight) {
			inSampleSize++;
		}
		return inSampleSize;
	}

	public void scaleImage(float scale) {
		if (mActiveView == null || mActiveView instanceof ImageView == false) {
			return;
		}
		Log.d(TAG, "scale:" + scale);
		templateEditInfos.get(position).setScale(scale);
		mActiveView.setScaleX(scale);
		mActiveView.setScaleY(scale);
		mViews.set(position, mActiveView);
		
	}

	public void enlargeImage() {
		if (mActiveView == null || mActiveView instanceof ImageView == false) {
			return;
		}
		scaleImage(mActiveView.getScaleX() * 1.1f);
		mModified = true;
	}

	public void reduceImage() {
		if (mActiveView == null || mActiveView instanceof ImageView == false) {
			return;
		}
		scaleImage(mActiveView.getScaleX() / 1.1f);
		mModified = true;
	}

	public void rotateImage() {
		if (mActiveView == null || mActiveView instanceof ImageView == false) {
			return;
		}
		float rotation = mActiveView.getRotation() + 90f;
		Log.d(TAG, "rotateImage rotation:" + rotation);
		mActiveView.setRotation(rotation);
		mViews.set(position, mActiveView);
		templateEditInfos.get(position).setRotate(rotation);
		mModified = true;
	}

	//返回当前图片的位置
	public PicInfo addImage(Uri imageUri) {
		int reqW = this.getWidth();
		int reqH = this.getHeight();
		Log.d(TAG, "before addImage reqW:" + reqW + ",reqH:" + reqH);
		if (mBitmap != null) {
			reqW = mBitmap.getWidth();
			reqH = mBitmap.getHeight();
		}
		Log.d(TAG, "addImage reqW:" + reqW + ",reqH:" + reqH);

		Bitmap bitmap = decodeSampledBitmapFromUri(getContext(), imageUri,
				reqW, reqH);
		Log.d(TAG, "bitmap:" + bitmap);
		// BitmapFactory.decodeStream(this.getContext().getContentResolver().openInputStream(
		// imageUri));
		if (bitmap == null)
			return null;

		ImageView view = new ImageView(getContext());
		view.setImageBitmap(bitmap);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		width = (int) (width * mBitmapScale);
		height = (int) (height * mBitmapScale);
		
		Log.d(TAG, "addImage bitmap width:" + bitmap.getWidth()
				+ ",bitmap height:" + bitmap.getHeight() + ",width:" + width
				+ ",height:" + height + ",mBitmapScale:" + mBitmapScale);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width,height);
		this.addView(view, params);
		view.setLeft(100);
		view.setTop(200);
		view.setPadding(2, 2, 2, 2);
		float centerX=(view.getLeft()+width/2)/mBitmapScale;
		float centerY=(view.getTop()+height/2)/mBitmapScale;
		if (mActiveView != null) {
			mActiveView.setBackground(null);
		}
		mActiveView = view;
		mActiveView.setBackgroundResource(R.drawable.picture_edit_item_active_bg);
		//
		mViews.add(mActiveView);
		
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyyMMddHHmmss);
		String time = sdf.format(calendar.getTime());
		String content = "pic"+ time + ".png";
		//根据时间生成文件名，文件名不重复
		TemplateEditInfo tInfo = new TemplateEditInfo("pic", centerX, centerY, content,1);
		templateEditInfos.add(tInfo);
		position = mViews.size() - 1;
		//
		notifyItemActive(FOCUS_ON_IMAGE);
		PicInfo picInfo = new PicInfo(bitmap, content);
		mModified = true; 
		return picInfo;
	}
	
	public Bitmap getBgBitmap(){
		return mBitmap;
	}
	
	//返回当前图片的位置
	public boolean addBgImage(Uri imageUri) {
		int reqW = this.getWidth();
		int reqH = this.getHeight();
		Log.d(TAG, "before addImage reqW:" + reqW + ",reqH:" + reqH);
		if (mBitmap != null) {
			reqW = mBitmap.getWidth();
			reqH = mBitmap.getHeight();
		}
		Log.d(TAG, "addImage reqW:" + reqW + ",reqH:" + reqH);

		Bitmap tmpBitmap = decodeSampledBitmapFromUri(getContext(), imageUri,
				reqW, reqH);
		if( tmpBitmap == null ){
			return false;
		}
		Bitmap bitmap = Bitmap.createScaledBitmap(tmpBitmap, reqW, reqH, true);
		Log.d(TAG, "width:" + bitmap.getWidth()+",height:"+bitmap.getHeight());
		// BitmapFactory.decodeStream(this.getContext().getContentResolver().openInputStream(
		// imageUri));
		mBitmap = bitmap;
		mModified = true;
		this.invalidate();
		return true;

//		ImageView view = new ImageView(getContext());
//		view.setImageBitmap(bitmap);
//		int width = bitmap.getWidth();
//		int height = bitmap.getHeight();
//		width = (int) (width * mBitmapScale);
//		height = (int) (height * mBitmapScale);
//		
//		Log.d(TAG, "addImage bitmap width:" + bitmap.getWidth()
//				+ ",bitmap height:" + bitmap.getHeight() + ",width:" + width
//				+ ",height:" + height + ",mBitmapScale:" + mBitmapScale);
//		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width,height);
//		this.addView(view, params);
////		view.setLeft(100);
////		view.setTop(200);
////		view.setPadding(2, 2, 2, 2);
//		float centerX=(view.getLeft()+width/2)/mBitmapScale;
//		float centerY=(view.getTop()+height/2)/mBitmapScale;
//		if (mActiveView != null) {
//			mActiveView.setBackground(null);
//		}
//		mActiveView = view;
//		mActiveView.setBackgroundResource(R.drawable.picture_edit_item_active_bg);
//		//
//		mViews.add(mActiveView);
//		
//		Calendar calendar = Calendar.getInstance();
//		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyyMMddHHmmss);
//		String time = sdf.format(calendar.getTime());
//		String content = "pic"+ time + ".png";
//		//根据时间生成文件名，文件名不重复
//		TemplateEditInfo tInfo = new TemplateEditInfo("pic", centerX, centerY, content,1);
//		templateEditInfos.add(tInfo);
//		position = mViews.size() - 1;
//		//
//		notifyItemActive(FOCUS_ON_IMAGE);
//		PicInfo picInfo = new PicInfo(bitmap, content);
//		mModified = true; 
//		return picInfo;
	}
	
	public boolean addImage(TemplateEditInfo mTemplateEditInfo,String dirPath) {
		int reqW = this.getWidth();
		int reqH = this.getHeight();
		Log.d(TAG, "before addImage reqW:" + reqW + ",reqH:" + reqH);
		if (mBitmap != null) {
			reqW = mBitmap.getWidth();
			reqH = mBitmap.getHeight();
		}
		Log.d(TAG, "addImage reqW:" + reqW + ",reqH:" + reqH);
		File file = new File(dirPath);
		File picFile = null;
		Bitmap bitmap=null;
		if( file.exists() && file.isDirectory())
		{
			picFile = new File(file,mTemplateEditInfo.getContent());
		}
		if( picFile != null )
		{
			bitmap = BitmapFactory.decodeFile(picFile.getAbsolutePath());
		    Log.d(TAG, "bitmap:" + bitmap);
		}
		if (bitmap == null)
			return false;

		ImageView view = new ImageView(getContext());
		view.setImageBitmap(bitmap);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		width = (int) (width * mBitmapScale);
		height = (int) (height * mBitmapScale);
		Log.d(TAG, "addImage bitmap width:" + bitmap.getWidth()
				+ ",bitmap height:" + bitmap.getHeight() + ",width:" + width
				+ ",height:" + height + ",mBitmapScale:" + mBitmapScale);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width,height);
		this.addView(view, params);
		view.setPadding(2, 2, 2, 2);
		view.setRotation(mTemplateEditInfo.getRotate());
		view.setScaleX(mTemplateEditInfo.getScale());
		view.setScaleY(mTemplateEditInfo.getScale());
		view.setLeft((int)(mTemplateEditInfo.getX()*mBitmapScale-width/2));
		view.setTop((int)(mTemplateEditInfo.getY()*mBitmapScale-height/2));
//		view.setOnTouchListener(new TouchListener());
		mViews.add(view);
		TemplateEditInfo tInfo = mTemplateEditInfo;
		templateEditInfos.add(tInfo);
		return true;
	}
	
	
	

	public void addText(String data) {
//		String data = text.replace("\\n", "n");
		Log.v(TAG, "addText2: " + data.indexOf("\n"));
		StrokeTextView view = new StrokeTextView(getContext());
//		view.setMaxLines(1);
		
		view.setLeft(this.getWidth()/6);
		view.setTop(this.getHeight()/3);
		view.setMaxWidth(mBitmapWidthOnScreen-view.getLeft());		
		view.setText(data);
		view.setTextColor(mTextColor);
		    
		view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
		this.addView(view);

		

		if (mActiveView != null) {
			mActiveView.setBackground(null);
		}
		mActiveView = view;
		mActiveView.setBackgroundResource(R.drawable.picture_edit_item_active_bg);
		//
		mViews.add(mActiveView);
		
		String strColor = String.format("#%06X", 0xFFFFFF & mTextColor);
		TemplateEditInfo tInfo;
		
		Log.d(TAG,"addText data:"+data);
		tInfo = new TemplateEditInfo("text", view.getLeft()/mBitmapScale, view.getTop()/mBitmapScale, data,mTextSize, strColor);
		templateEditInfos.add(tInfo);
		position = mViews.size() - 1;
		//
		notifyItemActive(FOCUS_ON_TEXT);
		
		mModified = true;
	}
	
	public void addText(TemplateEditInfo mTemplateEditInfo) {
		StrokeTextView view = new StrokeTextView(getContext());
		Log.d(TAG,"addText mTemplateEditInfo.getContent():"+mTemplateEditInfo.getContent());
		Log.v(TAG, "addText3: " + mTemplateEditInfo.getContent().indexOf("\n"));
		view.setText(mTemplateEditInfo.getContent());
//		view.setMaxLines(1);
		view.setLeft((int)(mTemplateEditInfo.getX()*mBitmapScale));
		view.setTop((int)(mTemplateEditInfo.getY()*mBitmapScale));
		view.setMaxWidth(mBitmapWidthOnScreen-view.getLeft());
		String color = mTemplateEditInfo.getColor();
		if( color != null ){
			if(color.startsWith("#") ){
				mTextColor = Color.parseColor(mTemplateEditInfo.getColor());
			}else if( color.startsWith("0x")){
				long colorLong = (int) Long.parseLong(color.substring(2), 16);
				if (color.length() == 8) {
		                // Set the alpha value
					 colorLong |= 0x00000000ff000000;
					 mTextColor = (int)colorLong;
		        } 					
			}
		}
		view.setTextColor(mTextColor);
		mTextSize = (int) mTemplateEditInfo.getScale();
		view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize*mBitmapScale);
		this.addView(view);
		Log.v(TAG, "addText: " + mTemplateEditInfo.getContent());
		mViews.add(view);
		TemplateEditInfo tInfo = mTemplateEditInfo;
		templateEditInfos.add(tInfo);
	}

	float mDownX;
	float mDownY;
	private View mActiveView;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		// Log.v(TAG, "action: " + action + " x: " + x + " y: " + y);
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			View viewAtDown = null;
			Rect r = new Rect();
			Rect r1 = new Rect();
			// 触摸位置在编辑View内
			if (mActiveView != null) {
				// mActiveView.getLocalVisibleRect(r);
				mActiveView.getGlobalVisibleRect(r);
				this.getGlobalVisibleRect(r1);
				r.offset(-r1.left, -r1.top);
				// r.offset(mActiveView.getLeft(), mActiveView.getTop());
				if (r.contains((int) x, (int) y)) {
					viewAtDown = mActiveView;
					// Add
					position = getThePosition();

				}
			}

			if (viewAtDown == null) {
				for (int i = getChildCount() - 1; i >= 0; i--) {
					View child = getChildAt(i);
					child.getGlobalVisibleRect(r);
					this.getGlobalVisibleRect(r1);
					r.offset(-r1.left, -r1.top);
					// this.getGlobalVisibleRect(r1);
					// r.offset(r1.left,r1.top);
					// child.getLocalVisibleRect(r);
					// r.offset(child.getLeft(), child.getTop());
					// Log.v(TAG, "child: " + i + " rect: " +
					// r.toShortString());
					if (r.contains((int) x, (int) y)) {
						viewAtDown = child;
						break;
					}
				}
			}
			if (viewAtDown == null) {
				if (mActiveView != null) {
					mActiveView.setBackground(null);
					mActiveView = null;
					position = -1;
					notifyItemActive(FOCUS_NONE);
				}
			} else {
				if (viewAtDown != mActiveView) {
					if (mActiveView != null) {
						mActiveView.setBackground(null);
					}

					mActiveView = viewAtDown;
					position = getThePosition();
					mActiveView.bringToFront();
					mActiveView.setBackgroundResource(R.drawable.picture_edit_item_active_bg);
				}
				if (mActiveView instanceof TextView) {
					notifyItemActive(FOCUS_ON_TEXT);
				} else if (mActiveView instanceof ImageView) {
					notifyItemActive(FOCUS_ON_IMAGE);
				}
			}
			mDownX = x;
			mDownY = y;
			break;

		case MotionEvent.ACTION_MOVE:
			if (mActiveView != null) {
				mActiveView.setTranslationX(x - mDownX);
				mActiveView.setTranslationY(y - mDownY);
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mActiveView != null) {
				mModified = true; 
				mActiveView.setLeft((int) (x - mDownX) + mActiveView.getLeft());
				mActiveView.setTop((int) (y - mDownY) + mActiveView.getTop());
				mActiveView.setTranslationX(0);
				mActiveView.setTranslationY(0);
				mViews.set(position, mActiveView);
				templateEditInfos.get(position).changeX((x-mDownX)/mBitmapScale);
				templateEditInfos.get(position).changeY((y-mDownY)/mBitmapScale);
				//将最后一次编辑的view调整到末尾
				View view=mViews.get(position);
				TemplateEditInfo templateEditInfo=templateEditInfos.get(position);
				mViews.remove(position);
				templateEditInfos.remove(position);
				mViews.add(view);
				templateEditInfos.add(templateEditInfo);
				position=mViews.size()-1;
				//
				requestLayout();
				
				correctTextViewMaxWidth(mActiveView);
			}
			break;
		}
		return true;
	}
	
	void correctTextViewMaxWidth(View view){
		if( view instanceof TextView ){
			TextView textview = (TextView)view;
			int newMaxWidth = mBitmapWidthOnScreen-view.getLeft();
			textview.setMaxWidth(newMaxWidth);
		}
	}
	
	
	public boolean getModifiedFlag(){
		return mModified;
	}

	public void setImage(Bitmap bitmap) {
		mBitmap = bitmap;
		invalidate();
	}

	public void setTextColor(int color) {
		mTextColor = color;
		if (mActiveView != null && mActiveView instanceof TextView == true) {
			Log.d(TAG, String.valueOf(color));
			
			String strColor = String.format("#%06X", 0xFFFFFF & mTextColor);
			
			templateEditInfos.get(position).setColor(strColor);
			
			((TextView) mActiveView).setTextColor(color);
			mViews.set(position, mActiveView);
			mTextColor = color;
			mModified = true;
		}
	}

	public void setTextSize(int size) {
		if (size < 8 || size > 200)
			return;
		mTextSize = size;
		if (mActiveView != null && mActiveView instanceof TextView == true) {
			templateEditInfos.get(position).setScale(mTextSize/mBitmapScale);
			((TextView) mActiveView).setTextSize(TypedValue.COMPLEX_UNIT_PX,size);
			mViews.set(position, mActiveView);
			mTextSize = size;
			mModified = true;
		}
	}

	public int getTextSize() {
		int size = mTextSize;
		if (mActiveView != null && mActiveView instanceof TextView == true) {
			size = (int) ((TextView) mActiveView).getTextSize();
		}

		Log.v(TAG, "return size: " + size);
		return size;
	}

	public void deleteActive() {
		if (mActiveView != null) {
			this.removeView(mActiveView);
			mViews.remove(position);
			templateEditInfos.remove(position);
			mActiveView = null;
			notifyItemActive(FOCUS_NONE);
			mModified = true;
		}
	}

	public int getActiveState() {
		if (mActiveView == null)
			return FOCUS_NONE;
		if (mActiveView instanceof TextView == true)
			return FOCUS_ON_TEXT;
		return FOCUS_ON_IMAGE;
	}

	public CharSequence getActiveText() {
		if (mActiveView != null && mActiveView instanceof TextView == true) {
			return ((TextView) mActiveView).getText();
		}

		return null;
	}

	public void modifyText(String text) {
		if (mActiveView != null && mActiveView instanceof TextView == true) {
			templateEditInfos.get(position).setContent(text);
			((TextView) mActiveView).setText(text);
			mViews.set(position, mActiveView);
			mModified = true;
		}
	}

	public boolean generatePicture(String path) {
		if (mActiveView != null) {
			mActiveView.setBackground(null);
			mActiveView = null;
			notifyItemActive(FOCUS_NONE);
		}
		if( mBitmap == null ){
			return false;
		}

		int bw = mBitmap.getWidth();
		int bh = mBitmap.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(mBitmap, 0, 0, null);

		int w = this.getWidth();// canvas.getWidth();
		int h = this.getHeight();// canvas.getHeight();

		int dw = w;
		int dh = h;
		float sw = (float) bw / w;
		float sh = (float) bh / h;
		Log.v(TAG, "sw: " + sw + " sh: " + sh);
		if (sw > sh) {
			dh = (int) (bh / sw);

			canvas.scale(sw, sw);
		} else {
			dw = (int) (bw / sh);

			canvas.scale(sh, sh);
		}
		super.dispatchDraw(canvas);

		boolean ret = false;
		File file = new File(path);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);

			bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
			ret = true;
		} catch (Exception e) {
			ret = false;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return ret;
	}

	private void notifyItemActive(int state) {
		if (mOnEditListener != null) {
			mOnEditListener.onItemAcitve(state);
		}
	}

	private OnEditListener mOnEditListener;

	public void setOnEditListener(OnEditListener listener) {
		mOnEditListener = listener;
	}

	public interface OnEditListener {
		void onItemAcitve(int state);
	}

	// Add找出mActiveView当前位置
	public int getThePosition() {
		for (int i = 0; i < mViews.size(); i++) {
			if (mViews.get(i) == mActiveView)
				return i;
		}
		return -1;
	}

	// Add得到模板编辑信息
	public List<TemplateEditInfo> gettInfos() {
		return templateEditInfos;
	}
	
}
