package com.routon.smartcampus.flower;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.routon.edurelease.R;
import com.routon.smartcampus.graffiti.ColorPickerDialog;
import com.routon.smartcampus.graffiti.DrawUtil;
import com.routon.smartcampus.graffiti.GraffitiColor;
import com.routon.smartcampus.graffiti.GraffitiListener;
import com.routon.smartcampus.graffiti.GraffitiParams;
import com.routon.smartcampus.graffiti.GraffitiSelectableItem;
import com.routon.smartcampus.graffiti.GraffitiText;
import com.routon.smartcampus.graffiti.GraffitiView;
import com.routon.smartcampus.graffiti.TouchGestureDetector;
import com.routon.smartcampus.homework.FeedbackHomeworkFileBean;
import com.routon.smartcampus.utils.FileUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.forward.androids.utils.ImageUtils;

public class BadgePicEditActivity extends Activity{
	private static ImageLoader imageLoader = ImageLoader.getInstance();
//	private DisplayImageOptions options;
	private FeedbackHomeworkFileBean mBean;
	private ImageView paintImgView;
	private TextView paintTextView;
	private ImageView eraserImgView;
	private TextView eraserTextView;
	private ImageView backoutView;
	private ImageView contraryBackoutView;
	private FrameLayout mFrameLayout;
	private GraffitiParams mGraffitiParams;
	
	public static final int RESULT_ERROR = -111; 
    public static final String KEY_PARAMS = "key_graffiti_params";
    public static final String KEY_IMAGE_PATH = "key_image_path";
    private String mImagePath;
    private Bitmap mBitmap;
	private GraffitiView mGraffitiView;
	private boolean mIsMovingPic = false; // 是否是平移缩放模式
    private final float mMaxScale = 4f; // 最大缩放倍数
    private final float mMinScale = 0.25f; // 最小缩放倍数
    private AlphaAnimation mViewShowAnimation, mViewHideAnimation;
    private TouchGestureDetector mTouchGestureDetector;
	private SeekBar mPaintSizeBar;
	private View mBtnColor;
	private GraffitiOnClickListener mOnClickListener;
	private TextView saveView;
	private ImageView backMenu;
	private static String filePath;
	private ImageView textImgView;
	private TextView textTextView;
	private ImageView moveImgView;
	private TextView moveTextView;
	private ImageView centreImgView;
	private TextView centreTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_badge_pic_edit);
		
		
		initView();
		initData();
		
		
	}
	private void initView() {
		
		
		RelativeLayout title_layout = (RelativeLayout) findViewById(R.id.title_layout);
		TextView title_view=(TextView) findViewById(R.id.title_view);
		title_view.setText("图片编辑"); 
		
		
		saveView = (TextView)findViewById(R.id.save_view);
		backMenu = (ImageView) findViewById(R.id.back_btn);
		
		
		
		setTypeSelView();
        
       /* imageLoader.init(ImageLoaderConfiguration.createDefault(BadgePicEditActivity.this));
    	options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.empty_photo)
				.showImageOnFail(R.drawable.empty_photo)
				.resetViewBeforeLoading(true)
				.cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300))
				.build();*/
    	
    	
    	
    	
        
		
	}
	
	private void initData() {
		mBean = (FeedbackHomeworkFileBean) getIntent().getSerializableExtra(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST);
		
		
		if( mBean.fileUrl != null && mBean.fileUrl.startsWith("/")){
			mBitmap = BitmapFactory.decodeFile(mBean.fileUrl);
		}else if( mBean.fileUrl != null && mBean.fileUrl.startsWith("http")){
			mBitmap = getDiscCacheImage(mBean.fileUrl);
		}
		
		
		mGraffitiParams = new GraffitiParams();
        // 初始画笔大小
        mGraffitiParams.mPaintSize = 100;
        mGraffitiParams.mImagePath= filePath;

		mGraffitiView = new GraffitiView(this, mBitmap, mGraffitiParams.mEraserPath, mGraffitiParams.mEraserImageIsResizeable,
                new GraffitiListener() {
                    @Override
                    public void onSaved(Bitmap bitmap, Bitmap bitmapEraser) { // 保存图片为jpg格式
                        if (bitmapEraser != null) {
                            bitmapEraser.recycle(); // 回收图片，不再涂鸦，避免内存溢出相反
                        }
                        File graffitiFile = null;
                        File file = null;
                        String savePath = mGraffitiParams.mSavePath;
                        boolean isDir = mGraffitiParams.mSavePathIsDir;
                        if (TextUtils.isEmpty(savePath)) {
                        	String sdcardPath = FileUtil.getSDPath();
                        	
                        	
                            File destDir = new File(sdcardPath+"/Android/data/com.routon.edurelease");
                            if (destDir.exists() == false) {
                            	destDir.mkdir();
                			}
                            
                            graffitiFile = new File(destDir, "Graffiti");
                            //　保存的路径
                            file = new File(graffitiFile, System.currentTimeMillis() + ".jpg");
                        } else {
                            if (isDir) {
                                graffitiFile = new File(savePath);
                                //　保存的路径
                                file = new File(graffitiFile, System.currentTimeMillis() + ".jpg");
                            } else {
                                file = new File(savePath);
                                graffitiFile = file.getParentFile();
                            }
                        }
                        graffitiFile.mkdirs();

                        FileOutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                            ImageUtils.addImage(getContentResolver(), file.getAbsolutePath());
                            Intent intent = new Intent();
                            intent.putExtra(KEY_IMAGE_PATH, file.getAbsolutePath());
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            onError(GraffitiView.ERROR_SAVE, e.getMessage());
                        } finally {
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(int i, String msg) {
                        setResult(RESULT_ERROR);
                        finish();
                    }

                    @Override
                    public void onReady() {
                        // 设置初始值
                        mGraffitiView.setPaintSize(mGraffitiParams.mPaintSize > 0 ? mGraffitiParams.mPaintSize
                                : mGraffitiView.getPaintSize());
                        mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                        mPaintSizeBar.setMax((int) (Math.min(mGraffitiView.getBitmapWidthOnView(),
                                mGraffitiView.getBitmapHeightOnView()) / 3 * DrawUtil.GRAFFITI_PIXEL_UNIT));
                        // 选择画笔
                        findViewById(R.id.paint_type_ll).performClick();
                    }

                    @Override
                    public void onSelectedItem(GraffitiSelectableItem selectableItem, boolean selected) {
                        if (selected) {
                            if (mGraffitiView.getSelectedItemColor().getType() == GraffitiColor.Type.BITMAP) {
                                mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getSelectedItemColor().getBitmap()));
                            } else {
                                mBtnColor.setBackgroundColor(mGraffitiView.getSelectedItemColor().getColor());
                            }
                            mPaintSizeBar.setProgress((int) ((mGraffitiView.getSelectedItemSize() + 0.5f)*10));
                            Log.e("mSelectedItem", "====="+mGraffitiView.getSelectedItemSize());
                        } else {
                            if (mGraffitiView.getColor().getType() == GraffitiColor.Type.BITMAP) {
                                mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getColor().getBitmap()));
                            } else {
                                mBtnColor.setBackgroundColor(mGraffitiView.getColor().getColor());
                            }

                            mPaintSizeBar.setProgress((int) ((mGraffitiView.getPaintSize() + 0.5f)*10));
                        }
                    }

                    @Override
                    public void onCreateSelectableItem(GraffitiView.Pen pen, float x, float y) {
                        if (pen == GraffitiView.Pen.TEXT) {
                            createGraffitiText(null, x, y);
                        }
                    }
                });

        mGraffitiView.setIsDrawableOutside(mGraffitiParams.mIsDrawableOutside);
        mFrameLayout.addView(mGraffitiView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        
        mTouchGestureDetector = new TouchGestureDetector(this, new GraffitiGestureListener());
        

   	 if (mGraffitiView.getGraffitiColor().getType() == GraffitiColor.Type.COLOR) {
            mBtnColor.setBackgroundColor(mGraffitiView.getGraffitiColor().getColor());
        } else if (mGraffitiView.getGraffitiColor().getType() == GraffitiColor.Type.BITMAP) {
            mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getGraffitiColor().getBitmap()));
        }

   	mPaintSizeBar = (SeekBar) findViewById(R.id.graffiti_paint_bar);
//   	mPaintSizeBar.setProgress(10);
        // 设置画笔的进度条
        mPaintSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0 ||progress<10) {
                    mPaintSizeBar.setProgress(10); 
                    return;
                }
                if (mGraffitiView.isSelectedItem()) {
                	if (isNoText) {
                		mGraffitiView.setPaintSize(progress/10);
					}else {
						mGraffitiView.setSelectedItemSize(progress/10);
					}
                    
                } else {
                    mGraffitiView.setPaintSize(progress/10);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });



        // 添加涂鸦的触摸监听器，移动图片位置
        mGraffitiView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!mIsMovingPic) { // 非移动缩放模式
                    return false;  // 交给下一层的涂鸦View处理
                }
                // 处理手势
                mTouchGestureDetector.onTouchEvent(event);
                return true;
            }
        });



        
        
		
        mViewShowAnimation = new AlphaAnimation(0, 1);
        mViewShowAnimation.setDuration(500);
        mViewHideAnimation = new AlphaAnimation(1, 0);
        mViewHideAnimation.setDuration(500);
		
		
	}
	
	
	public static Bitmap getDiscCacheImage(String uri){  
		
		 File file = DiscCacheUtil.findInCache(uri,  imageLoader.getDiscCache());  
		 try {
			
			 filePath = file.getPath();
			 return BitmapFactory.decodeFile(filePath);


		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		 
		 return null;  
	 }
	
	
	/**
	 * 设置涂鸦类型切换View和事件
	 */
	private void setTypeSelView() {
		
		mFrameLayout = (FrameLayout) findViewById(R.id.graffiti_img);
//		graffitiTypeTextView = (TextView) findViewById(R.id.graffiti_type_text);
		
		LinearLayout paintTypeBtn = (LinearLayout) findViewById(R.id.paint_type_ll);//画笔按钮
		paintImgView = (ImageView) findViewById(R.id.paint_type_img);
		paintTextView = (TextView) findViewById(R.id.paint_type_text);
		
		LinearLayout eraserTypeBtn = (LinearLayout) findViewById(R.id.eraser_type_ll);//橡皮按钮
		eraserImgView = (ImageView) findViewById(R.id.eraser_type_img);
		eraserTextView = (TextView) findViewById(R.id.eraser_type_text);
		
		LinearLayout textTypeBtn = (LinearLayout) findViewById(R.id.text_btn_ll);//文字按钮
		textImgView = (ImageView) findViewById(R.id.text_btn_img);
		textTextView = (TextView) findViewById(R.id.text_btn_text);
		
		LinearLayout moveTypeBtn = (LinearLayout) findViewById(R.id.move_pic_ll);//缩放按钮
		moveImgView = (ImageView) findViewById(R.id.move_pic_img);
		moveTextView = (TextView) findViewById(R.id.move_pic_text);
		
		LinearLayout centreTypeBtn = (LinearLayout) findViewById(R.id.centre_pic_ll);//复位按钮
		centreImgView = (ImageView) findViewById(R.id.centre_pic_img);
		centreTextView = (TextView) findViewById(R.id.centre_pic_text);
		
		
		
		
		backoutView = (ImageView) findViewById(R.id.backout_view);//撤销
		contraryBackoutView = (ImageView) findViewById(R.id.contrary_backout_view);//反撤销
		ImageView allBackoutView = (ImageView) findViewById(R.id.all_backout_view);//清屏
		
		
		mBtnColor = findViewById(R.id.paint_color_view);
		
		mOnClickListener = new GraffitiOnClickListener();
		
		
		backMenu.setOnClickListener(mOnClickListener);
		saveView.setOnClickListener(mOnClickListener);
		textTypeBtn.setOnClickListener(mOnClickListener);
		paintTypeBtn.setOnClickListener(mOnClickListener);
		eraserTypeBtn.setOnClickListener(mOnClickListener);
		backoutView.setOnClickListener(mOnClickListener);
		contraryBackoutView.setOnClickListener(mOnClickListener);
		allBackoutView.setOnClickListener(mOnClickListener);
		mBtnColor.setOnClickListener(mOnClickListener);
		moveTypeBtn.setOnClickListener(mOnClickListener);
		centreTypeBtn.setOnClickListener(mOnClickListener);
		
		
		
	}
	
	
	private class GraffitiOnClickListener implements View.OnClickListener {

        private View mLastPenView, mLastShapeView;
        private boolean mDone = false;
		private View tagV;

        @Override
        public void onClick(View v) {
            mDone = false;
            if (v.getId() == R.id.paint_type_ll) {//手绘
            	setViewClickListener(0);
                mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize()*10 + 0.5f));
                
                mGraffitiView.setPen(GraffitiView.Pen.HAND);
                mDone = true;
            }else if (v.getId() == R.id.eraser_type_ll) {//橡皮擦
            	setViewClickListener(1);
                mPaintSizeBar.setProgress((int) (200f));
                mGraffitiView.setPen(GraffitiView.Pen.ERASER);
                mDone = true;
            } else if (v.getId() == R.id.text_btn_ll) {//文字
            	setViewClickListener(2);
                mGraffitiView.setPen(GraffitiView.Pen.TEXT);
                mDone = true;
            }
            if (mDone) {
                if (mLastPenView != null) {
                    mLastPenView.setSelected(false);
                }
                v.setSelected(true);
                mLastPenView = v;
                return;
            }

            if (v.getId() == R.id.all_backout_view) {//清屏
            	mGraffitiView.clear();
                mDone = true;
            } else if (v.getId() == R.id.backout_view) {//撤销
                mGraffitiView.undo();
                mDone = true;
            } else if (v.getId() == R.id.contrary_backout_view) {//反撤销
                mGraffitiView.udo();
                mDone = true;
            }else if (v.getId() == R.id.paint_color_view) {//颜色
                if (!(GraffitiParams.getDialogInterceptor() != null
                        && GraffitiParams.getDialogInterceptor().onShow(BadgePicEditActivity.this, mGraffitiView, GraffitiParams.DialogType.COLOR_PICKER))) {
                    new ColorPickerDialog(BadgePicEditActivity.this, mGraffitiView.getGraffitiColor().getColor(), "画笔颜色",
                            new ColorPickerDialog.OnColorChangedListener() {
                                public void colorChanged(int color) {
                                    mBtnColor.setBackgroundColor(color);
                                    if (mGraffitiView.isSelectedItem()) {
                                        mGraffitiView.setSelectedItemColor(color);
                                    } else {
                                        mGraffitiView.setColor(color);
                                    }
                                }

                                @Override
                                public void colorChanged(Drawable color) {
                                    mBtnColor.setBackgroundDrawable(color);
                                    if (mGraffitiView.isSelectedItem()) {
                                        mGraffitiView.setSelectedItemColor(ImageUtils.getBitmapFromDrawable(color));
                                    } else {
                                        mGraffitiView.setColor(ImageUtils.getBitmapFromDrawable(color));
                                    }
                                }
                            }).show();
                }
                mDone = true;
            }
            if (mDone) {
                return;
            }

            if (v.getId() == R.id.save_view) {//保存
                mGraffitiView.save();
                mDone = true;
            } else if (v.getId() == R.id.back_btn) {//返回
            	
            	backListener();
            	
            	
            	
                /*if (!mGraffitiView.isModified()) {
                    finish();
                    return;
                }
                if (!(GraffitiParams.getDialogInterceptor() != null
                        && GraffitiParams.getDialogInterceptor().onShow(BadgePicEditActivity.this, mGraffitiView, GraffitiParams.DialogType.SAVE))) {
                    DialogController.showEnterCancelDialog(BadgePicEditActivity.this, getString(R.string.graffiti_saving_picture), null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mGraffitiView.save();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }*/
                mDone = true;
            } else if (v.getId() == R.id.centre_pic_ll) {//恢复原图大小
//            	setViewClickListener(4);
                mGraffitiView.centrePic();
                mDone = true;
            } else if (v.getId() == R.id.move_pic_ll) {//开启单指移动双指缩放
            	setViewClickListener(3);
            	mIsMovingPic=true;
//                v.setSelected(!v.isSelected());
//                mIsMovingPic = v.isSelected();
                if (mIsMovingPic) {
                    Toast.makeText(getApplicationContext(), R.string.graffiti_moving_pic, Toast.LENGTH_SHORT).show();
                }
                mDone = true;
            }
            if (mDone) {
                return;
            }


            if (v.getId() == R.id.graffiti_selectable_edit) {
                if (mGraffitiView.getSelectedItem() instanceof GraffitiText) {
                    createGraffitiText((GraffitiText) mGraffitiView.getSelectedItem(), -1, -1);
                }
                mDone = true;
            }
            if (mDone) {
                return;
            }



            if (mLastShapeView != null) {
                mLastShapeView.setSelected(false);
            }
            v.setSelected(true);
            mLastShapeView = v;
        }
    }
	
	@Override  
    public void onBackPressed() {  
		backListener();
    }  
	
	private void backListener() {
		if (mGraffitiView.mUndoStack.size()>0) {
			 showDialog();
		}else {
			BadgePicEditActivity.this.finish();
			overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
		}
    	
	}
	
	private void showDialog() {
		final AlertDialog.Builder normalDialog = new AlertDialog.Builder(BadgePicEditActivity.this);

		normalDialog.setMessage("您已批改过作业，是否保存？");
		normalDialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mGraffitiView.save();
			}
		});
		normalDialog.setNegativeButton("不保存", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				BadgePicEditActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		});
		normalDialog.show();
	}

	int viewClickTag=0;
	private boolean isNoText=true;
	
	private void setViewClickListener(int type) {
		mIsMovingPic=false;
		switch (viewClickTag) {
		case 0:
			paintImgView.setImageResource(R.drawable.icon_pic_graffiti_paint_gray);
			paintTextView.setTextColor(getResources().getColor(R.color.black));
			break;
		case 1:
			eraserImgView.setImageResource(R.drawable.icon_pic_edit_eraser_gray);
			eraserTextView.setTextColor(getResources().getColor(R.color.black));
			break;
		case 2:
			textImgView.setImageResource(R.drawable.icon_pic_edit_text_gray);
			textTextView.setTextColor(getResources().getColor(R.color.black));
			isNoText = true;
			break;
		case 3:
			moveImgView.setImageResource(R.drawable.icon_pic_edit_zoom_gray);
			moveTextView.setTextColor(getResources().getColor(R.color.black));
			break;
		case 4:
			centreImgView.setImageResource(R.drawable.icon_pic_edit_restoration_gray);
			centreTextView.setTextColor(getResources().getColor(R.color.black));
			break;

		default:
			break;
		}
		
		viewClickTag=type;
		
		switch (type) {
		case 0:
			paintImgView.setImageResource(R.drawable.icon_pic_graffiti_paint);
			paintTextView.setTextColor(getResources().getColor(R.color.text_red));
			break;
		case 1:
			eraserImgView.setImageResource(R.drawable.icon_pic_edit_eraser);
			eraserTextView.setTextColor(getResources().getColor(R.color.text_red));
			
			break;
		case 2:
			textImgView.setImageResource(R.drawable.icon_pic_edit_text);
			textTextView.setTextColor(getResources().getColor(R.color.text_red));
			isNoText = false;
			break;
		case 3:
			moveImgView.setImageResource(R.drawable.icon_pic_edit_zoom);
			moveTextView.setTextColor(getResources().getColor(R.color.text_red));
			break;
		case 4:
			centreImgView.setImageResource(R.drawable.icon_pic_edit_restoration);
			centreTextView.setTextColor(getResources().getColor(R.color.text_red));
			break;

		default:
			break;
		}
		
		
	}
    
		
	// 添加文字
    private void createGraffitiText(final GraffitiText graffitiText, final float x, final float y) {
        Activity activity = this;
        if (isFinishing()) {
            return;
        }

        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Dialog dialog = null;
        if (fullScreen) {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        final Dialog finalDialog1 = dialog;
        activity.getWindow().getDecorView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                       finalDialog1.dismiss();
            }
        });

        ViewGroup container = (ViewGroup) View.inflate(getApplicationContext(), R.layout.graffiti_create_text, null);
        final Dialog finalDialog = dialog;
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });
        dialog.setContentView(container);

        final EditText textView = (EditText) container.findViewById(R.id.graffiti_selectable_edit);
        final View cancelBtn = container.findViewById(R.id.graffiti_text_cancel_btn);
        final TextView enterBtn = (TextView) container.findViewById(R.id.graffiti_text_enter_btn);

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = (textView.getText() + "").trim();
                if (TextUtils.isEmpty(text)) {
                    enterBtn.setEnabled(false);
                    enterBtn.setTextColor(0xffb3b3b3);
                } else {
                    enterBtn.setEnabled(true);
                    enterBtn.setTextColor(0xff232323);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        textView.setText(graffitiText == null ? "" : graffitiText.getText());

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBtn.setSelected(true);
                finalDialog.dismiss();
            }
        });

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (cancelBtn.isSelected()) {
                    return;
                }
                String text = (textView.getText() + "").trim();
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                if (graffitiText == null) {
                    mGraffitiView.addSelectableItem(new GraffitiText(mGraffitiView.getPen(), text, mGraffitiView.getPaintSize(), mGraffitiView.getColor().copy(),
                            0, mGraffitiView.getGraffitiRotateDegree(), x, y, mGraffitiView.getOriginalPivotX(), mGraffitiView.getOriginalPivotY()));
                } else {
                    graffitiText.setText(text);
                }
                mGraffitiView.invalidate();
            }
        });

    }
	
	
	
    private class GraffitiGestureListener extends TouchGestureDetector.OnTouchGestureListener {

        private Float mLastFocusX;
        private Float mLastFocusY;
        // 手势操作相关
        private float mToucheCentreXOnGraffiti, mToucheCentreYOnGraffiti, mTouchCentreX, mTouchCentreY;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mGraffitiView.setTrans(mGraffitiView.getTransX() - distanceX, mGraffitiView.getTransY() - distanceY);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mLastFocusX = null;
            mLastFocusY = null;
            return true;
        }

        // 手势缩放
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            // 屏幕上的焦点
            mTouchCentreX = detector.getFocusX();
            mTouchCentreY = detector.getFocusY();
            // 对应的图片上的焦点
            mToucheCentreXOnGraffiti = mGraffitiView.toX(mTouchCentreX);
            mToucheCentreYOnGraffiti = mGraffitiView.toY(mTouchCentreY);

            if (mLastFocusX != null && mLastFocusY != null) { // 焦点改变
                final float dx = mTouchCentreX - mLastFocusX;
                final float dy = mTouchCentreY - mLastFocusY;
                // 移动图片
                mGraffitiView.setTrans(mGraffitiView.getTransX() + dx, mGraffitiView.getTransY() + dy);
            }

            // 缩放图片
            float scale = mGraffitiView.getScale() * detector.getScaleFactor();
            if (scale > mMaxScale) {
                scale = mMaxScale;
            } else if (scale < mMinScale) {
                scale = mMinScale;
            }
            mGraffitiView.setScale(scale, mToucheCentreXOnGraffiti, mToucheCentreYOnGraffiti);

            mLastFocusX = mTouchCentreX;
            mLastFocusY = mTouchCentreY;

            return true;
        }
    }
	
	
	
	
}
