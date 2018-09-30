package com.routon.inforelease.classinfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Volley;
import com.routon.ad.element.PicInfo;
import com.routon.ad.element.StringUtils;
import com.routon.ad.element.TemplateEditInfo;
import com.routon.ad.element.TemplateInfo;
import com.routon.ad.pkg.FileGetTask;
import com.routon.ad.pkg.HttpGetTask;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.ResourceUploadBean;
import com.routon.inforelease.json.ResourceUploadBeanParser;
import com.routon.inforelease.json.ResourceUploadobjBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.MaterialParams;
import com.routon.inforelease.plan.PlanInfoEditActivity;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStack;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStringRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.util.TemplatePkgTool;
import com.routon.inforelease.util.TimeUtils;
import com.routon.inforelease.widget.ColorPickerDialog;
import com.routon.inforelease.widget.PicSelHelper;
import com.routon.inforelease.widget.PictureEditView;

public class ClassPictureEditActivity extends CustomTitleActivity {
	private static final String TAG = "ClassPictureEditActivity";
	private PictureEditView mPicEditView;
	private TemplateInfo mTemplateInfo = null;

	private View mBtnDel;
	private View mBtnFontSizeInc;
	private View mBtnFontSizeDes;
	private View mBtnColor;
	private View mBtnModify;
	private View mBtnRotate;
	private ProgressDialog progressDialog;
	private int classInfoType = 1;

	private String mTemplateUrl;
	private String mTemplateId = null;//模板原型id
	
	private boolean mOffLineEdit = true;
	
	private PicSelHelper mPicSelHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Log.v(TAG, "onCreate");
		// 获得图片地址
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mTemplateUrl = bundle.getString(CommonBundleName.TEMPLATE_URL_TAG);
			mTemplateId = bundle.getString(CommonBundleName.TEMPLATE_ID_TAG);
			mOffLineEdit = bundle.getBoolean(CommonBundleName.OFFLINE_TAG,true);
			mTemplateInfo = (TemplateInfo) bundle.getSerializable(CommonBundleName.TEMPLATE_INFO_TAG);
		}
		
		if( mTemplateInfo == null ){
			mTemplateInfo =  new TemplateInfo();
			mTemplateInfo.mEditDirPath = TemplatePkgTool.createTmpTemplateDir(this);
			mTemplateInfo.mTemplate = "template_"+mTemplateId+".png";
		}
		if( mTemplateUrl == null ){
			mTemplateUrl = mTemplateInfo.mEditDirPath + mTemplateInfo.mTemplate;
		}
		
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.layout_class_picture_edit_landscape);
			classInfoType = 1;
		} else {
			setContentView(R.layout.layout_class_picture_edit_portrait);
			classInfoType = 3;
		}
		
		mPicSelHelper = new PicSelHelper(this);

		initView();
		
		loadImage(mTemplateUrl);
		
		//新建必须重新上传素材,文件类型不同
		if( isFromEdit() == false ){
			mPicEditView.mModified = true;
		}	

		progressDialog = ProgressDialog.show(this, "", "...Loading...");	
	}
	
	private boolean isFromEdit(){
		//没有编辑包数据，返回false
		if( mTemplateInfo == null || mTemplateInfo.mTemplateEditInfos == null )
		{
			return false;
		}
		//有编辑包数据，返回true
		return true;
	}

	private void initView() {
		this.initTitleBar("模板编辑");
		// 编辑模板图片
		mPicEditView = (PictureEditView) findViewById(R.id.picture_edit);	
		mPicEditView.setOnEditListener(new PictureEditView.OnEditListener() {

			@Override
			public void onItemAcitve(int state) {
				if (state == PictureEditView.FOCUS_NONE) {
					mBtnFontSizeInc.setVisibility(View.GONE);
					mBtnFontSizeDes.setVisibility(View.GONE);
					mBtnModify.setVisibility(View.GONE);
					mBtnColor.setVisibility(View.GONE);
					mBtnDel.setVisibility(View.GONE);
					mBtnRotate.setVisibility(View.GONE);
				} else if (state == PictureEditView.FOCUS_ON_TEXT) {
					mBtnFontSizeInc.setVisibility(View.VISIBLE);
					mBtnFontSizeDes.setVisibility(View.VISIBLE);
					mBtnModify.setVisibility(View.VISIBLE);
					mBtnColor.setVisibility(View.VISIBLE);
					mBtnDel.setVisibility(View.VISIBLE);
					mBtnRotate.setVisibility(View.GONE);
				} else if (state == PictureEditView.FOCUS_ON_IMAGE) {

					mBtnFontSizeInc.setVisibility(View.VISIBLE);
					mBtnFontSizeDes.setVisibility(View.VISIBLE);
					mBtnModify.setVisibility(View.GONE);
					mBtnColor.setVisibility(View.GONE);
					mBtnDel.setVisibility(View.VISIBLE);
					mBtnRotate.setVisibility(View.VISIBLE);
				}
			}
		});
		// 右滑不退出
		this.setMoveBackEnable(false);
		// 模板编辑完成
		this.setTitleNextImageBtnClickListener(R.drawable.ok, mBtnClickListener);
		this.setTitleBackBtnClickListener(mBtnClickListener);
		findViewById(R.id.btn_add).setOnClickListener(mBtnClickListener);
		findViewById(R.id.btn_add_pic).setOnClickListener(mBtnClickListener);
		findViewById(R.id.btn_add_bgpic).setOnClickListener(mBtnClickListener);
		mBtnModify = findViewById(R.id.btn_modify);
		mBtnModify.setOnClickListener(mBtnClickListener);
		mBtnDel = findViewById(R.id.btn_del);
		mBtnDel.setOnClickListener(mBtnClickListener);
		mBtnFontSizeInc = findViewById(R.id.btn_fontsize_inc);
		mBtnFontSizeInc.setOnClickListener(mBtnClickListener);
		mBtnFontSizeDes = findViewById(R.id.btn_fontsize_des);
		mBtnFontSizeDes.setOnClickListener(mBtnClickListener);
		mBtnColor = findViewById(R.id.btn_color);
		mBtnColor.setOnClickListener(mBtnClickListener);
		mBtnFontSizeInc.setOnTouchListener(mOnFontBtnTouchListener);
		mBtnFontSizeDes.setOnTouchListener(mOnFontBtnTouchListener);

		mBtnRotate = findViewById(R.id.btn_rotate);
		mBtnRotate.setOnClickListener(mBtnClickListener);

		mBtnFontSizeInc.setVisibility(View.GONE);
		mBtnFontSizeDes.setVisibility(View.GONE);
		mBtnModify.setVisibility(View.GONE);
		mBtnColor.setVisibility(View.GONE);
		mBtnDel.setVisibility(View.GONE);
		mBtnRotate.setVisibility(View.GONE);
	}

	// 显示对应url的图片
	private void loadImage(String url) {
		Log.d(TAG,"loadImage URL:"+url);
		File file = null;
		String path = url;
		if( url.startsWith("/") == true ){//本地文件，直接加载
			file = new File(url);
		}else{//网上图片，先下载
			String name = StringUtils.getFileName(url);
			file = new File(getCacheDir(), name);
			path =  getCacheDir()+"/"+name;
		}
		
		if (file.exists()) {
			mTemplateUrl = path;
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			Message msg = Message.obtain(mHandler, MSG_UPDATE_IMAGE, bitmap);
			mHandler.sendMessage(msg);
			return;
		}
		File templateFile = new File(url);
		String templateFilePath = templateFile.getPath();
		if(templateFile.exists())
		{
			Bitmap bitmap = BitmapFactory.decodeFile(templateFilePath);
			Message msg = Message.obtain(mHandler, MSG_UPDATE_IMAGE, bitmap);
			mHandler.sendMessage(msg);
			return;
		}
		FileGetTask task = new FileGetTask(url, path, url);
		Log.d(TAG,"loadImage path:"+path);
		task.setOnHttpGetTaskListener(new HttpGetTask.OnHttpGetTaskListener() {

			@Override
			public void onTaskStarted(HttpGetTask task) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTaskFinished(HttpGetTask task, int code) {
				Bitmap bitmap = null;
				if (code == HttpGetTask.ERR_NONE) {
					FileGetTask t = (FileGetTask) task;
					String path = t.getFilePath();
					Log.d(TAG,"loadImage path:"+path);
					mTemplateUrl = path;
					bitmap = BitmapFactory.decodeFile(path);
				}
				Message msg = Message.obtain(mHandler, MSG_UPDATE_IMAGE, bitmap);
				mHandler.sendMessage(msg);
			}
		});
		new Thread(task).start();
	}
	
	private void onUpdateImage(Bitmap bitmap) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		if (bitmap == null) {
			return;
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Log.v(TAG, "image w: " + w + " h: " + h);
		if (w > h) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		mPicEditView.setImage(bitmap);
		//有模板编辑包，加载模板编辑包到界面上
		if( mTemplateInfo != null && mTemplateInfo.mTemplateEditInfos != null 
				&& mTemplateInfo.mTemplateEditInfos.size() > 0 ){
			new Timer().schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Message msg=new Message();
					msg.what = MSG_UPDATE_EDIT_INFO;
					mHandler.sendMessage(msg);
				}
			},100);
		}
	}

	private static final int MSG_UPDATE_IMAGE = 0;
	private static final int MSG_LONG_PRESS = 1;
	private static final int MSG_LONG_PRESS_CONTINUE = 2;
	private static final int MSG_UPDATE_EDIT_INFO=3;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_IMAGE:
				onUpdateImage((Bitmap) msg.obj);
				break;

			case MSG_LONG_PRESS:
			case MSG_LONG_PRESS_CONTINUE: {
				View v = (View) msg.obj;
				v.performClick();
				Message msg_continue = Message.obtain(mHandler,MSG_LONG_PRESS_CONTINUE, v);
				mHandler.sendMessageDelayed(msg_continue, 100);
				break;
			}
			case MSG_UPDATE_EDIT_INFO:
				loadTemplateEditImg(mTemplateInfo.mTemplateEditInfos);
				break;
			}
		}
	};

	private View.OnClickListener mBtnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.btn_add) {
				onAdd();
			} else if (v.getId() == R.id.btn_modify) {
				onModify();
			} else if (v.getId() == R.id.btn_del) {
				mPicEditView.deleteActive();
			} else if (v.getId() == R.id.btn_fontsize_inc) {
				int state = mPicEditView.getActiveState();
				if (state == PictureEditView.FOCUS_ON_TEXT) {
					mPicEditView.setTextSize(mPicEditView.getTextSize() + 1);
				} else if (state == PictureEditView.FOCUS_ON_IMAGE) {
					mPicEditView.enlargeImage();
				}
			} else if (v.getId() == R.id.btn_fontsize_des) {
				int state = mPicEditView.getActiveState();
				Log.d(TAG, "state:" + state);
				if (state == PictureEditView.FOCUS_ON_TEXT) {
					mPicEditView.setTextSize(mPicEditView.getTextSize() - 1);
				} else if (state == PictureEditView.FOCUS_ON_IMAGE) {
					mPicEditView.reduceImage();
				}
			} else if (v.getId() == R.id.btn_color) {
				showColorPickDialog();
			}
			// 模板编辑完成
			else if (v.getId() == R.id.next_step) {
				onDone();
			} else if (v.getId() == R.id.btn_add_pic) {
				mAddBgPic = false;
				mPicSelHelper.showAddPicDialog();
			} else if (v.getId() == R.id.btn_rotate) {
				mPicEditView.rotateImage();
			} 
			else if (v.getId()==R.id.back_btn) {
				deleteTmpTemplateDir();
				finish();
			}else if( v.getId() == R.id.btn_add_bgpic ){
				mAddBgPic = true;
				mPicSelHelper.showAddPicDialog();
			}
			
		}
	};
	private boolean mAddBgPic = false;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult requestCode:" + requestCode
				+ ",resultCode:" + resultCode);
		if( mAddBgPic == false ){
			if( true == mPicSelHelper.handleActivityResult(requestCode,resultCode,data) ){
				return;
			}
			switch (requestCode) {
			case PicSelHelper.PHOTO_CUT:
				if (resultCode == RESULT_OK) {
					boolean ret = addImage(mPicSelHelper.getImageUri());
					if (ret == false) {
						reportToast("添加图片失败");
					}
				}
				break;
			default:
				break;
			}
		}else{
			switch (requestCode) {
			case PicSelHelper.PHOTO_CARMERA:
			case PicSelHelper.PHOTO_PICK:
				if (resultCode == RESULT_OK) {
					boolean ret = false;
					if( requestCode == PicSelHelper.PHOTO_PICK ){ 
						ret = addBgImage(data.getData());
					}else{
						ret = addBgImage(mPicSelHelper.getImageUri());
					}			
					if (ret == false) {
						reportToast("添加底图失败");
					}
				}
				break;
			default:
				break;
			}
		}
	}
	
	private boolean addImage(Uri imageUri){
		if( imageUri == null ){
			
			return false;
		}
		Log.d(TAG,"addImage imageUri:"+imageUri);
		PicInfo picInfo = mPicEditView.addImage(imageUri);
		if( picInfo == null ) return false;
		String fileName = picInfo.content;
		Bitmap bitmap = picInfo.bitmap;
		if( fileName == null  ){
			return false;
		}
		File file = new File(mTemplateInfo.mEditDirPath,fileName);
		if(file.exists())
			file.delete();
		try {
			FileOutputStream out=new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private boolean addBgImage(Uri imageUri){
		if( imageUri == null ){		
			return false;
		}
		Log.d(TAG,"addBgImage imageUri:"+imageUri);
		boolean ret = mPicEditView.addBgImage(imageUri);
		if( ret == false ) return false;
		Log.d(TAG,"addBgImage ret:"+ret);
	
		//覆盖原图
		File file = new File(mTemplateInfo.mEditDirPath,mTemplateInfo.mTemplate);
		if(file.exists())
			file.delete();
		try {
			FileOutputStream out=new FileOutputStream(file);
			mPicEditView.getBgBitmap().compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG,"addBgImage end");
		return true;
	}
	
	private void addImage(TemplateEditInfo templateEditInfo) {
		mPicEditView.addImage(templateEditInfo,mTemplateInfo.mEditDirPath);
	}

	private void onDone() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyyMMddHHmmss);
		String time = sdf.format(calendar.getTime());
		File file = new File(getCacheDir(), "picture_generate"+"_"+time+".jpg");
		if (!mPicEditView.generatePicture(file.getAbsolutePath())) {
			reportToast("合成图片失败");
			return;
		}
		
		//保存模板编辑文件
		//url 模板原型文件地址
		TemplatePkgTool.saveTmplateEditInfoFile(mTemplateInfo.mEditDirPath,mPicEditView.gettInfos(),mTemplateInfo.mTemplate);
		TemplatePkgTool.saveTemplateOriginalPic(mTemplateUrl, mTemplateInfo.mEditDirPath,mTemplateInfo.mTemplate);

		//删除多余的小图片
		TemplatePkgTool.deleteUnUsedPic(mTemplateInfo.mEditDirPath, mPicEditView.gettInfos());
		if (InfoReleaseApplication.isEduPlatform == true ) {// 教育模版编辑，上传图片，编辑内容
			sendImage(file.getAbsolutePath());
		} else {// 广告模版编辑
			if( mOffLineEdit == true ){
				editOfflinePlanTemplate(file.getAbsolutePath());
			}else{
				sendPlanImage(file.getAbsolutePath());
			}
		}
	}

	//离线模板计划编辑
	private void editOfflinePlanTemplate(String path) {
		
		Intent intent = new Intent(this, PlanInfoEditActivity.class);	
		intent.putExtra(CommonBundleName.TEMPLATE_ID_TAG, mTemplateId);
		intent.putExtra(CommonBundleName.PIC_PATH_TAG, path);
		intent.putExtra(CommonBundleName.TEMPLATE_DIR_PATH_TAG, mTemplateInfo.mEditDirPath);	
		//data数据直接传到下一级界面
		intent.putExtra(CommonBundleName.DATA_TAG, this.getIntent().getSerializableExtra(CommonBundleName.DATA_TAG));
		if( this.isFromEdit() == true ){
			intent.putExtra(PlanInfoEditActivity.PLAN_TYPE, PlanInfoEditActivity.EDIT_TEMPLATE_PLAN);
		}else{
			intent.putExtra(PlanInfoEditActivity.PLAN_TYPE, PlanInfoEditActivity.ADD_TEMPLATE_PLAN);
		}
		this.startActivity(intent);

		finish();
	}
	
	private void startPlanEditActivity(String localPath,String resId){
		Intent intent = new Intent(ClassPictureEditActivity.this,PlanInfoEditActivity.class);						
		intent.putExtra(CommonBundleName.OFFLINE_TAG, false);
		if( this.isFromEdit() == true ){
			intent.putExtra(PlanInfoEditActivity.PLAN_TYPE, PlanInfoEditActivity.EDIT_TEMPLATE_PLAN);
		}else{
			intent.putExtra(PlanInfoEditActivity.PLAN_TYPE, PlanInfoEditActivity.ADD_TEMPLATE_PLAN);
		}
		intent.putExtra(CommonBundleName.TEMPLATE_ID_TAG, mTemplateId);
		intent.putExtra(CommonBundleName.TEMPLATE_DIR_PATH_TAG, mTemplateInfo.mEditDirPath);	
		//素材的本地路径,没有修改可以传null
		intent.putExtra(CommonBundleName.PIC_PATH_TAG, localPath);	
		//素材的id,没有修改可以传null
		intent.putExtra(CommonBundleName.RES_ID_TAG, resId);
		//data数据直接传到下一级界面
		intent.putExtra(CommonBundleName.DATA_TAG, ClassPictureEditActivity.this.getIntent().getSerializableExtra(CommonBundleName.DATA_TAG));
		startActivity(intent);
		finish();
	}
	
	// 节目在线模板素材图片提交
	private void sendPlanImage(final String path) {
		//path为本地文件路径
		if( mPicEditView.getModifiedFlag() == false ){//文件未修改，不重新上传素材		
			startPlanEditActivity(null,null);
			return;
		}
		String urlString = UrlUtils.getResourceUploadUrl();
		Log.i(TAG, "URL:" + urlString);
		Map<String, File> files = new HashMap<String, File>();

		Map<String, String> params = new HashMap<String, String>();
		int fileType = MaterialParams.TYPE_AD_PICTURE;
		
		// 上传模板分类
		int species = this.getIntent().getIntExtra(CommonBundleName.SPECIES_TAG,0);
		params.put("species", Integer.toString(species));
		params.put("fileType", Integer.toString(fileType));
		files.put("file1", new File(path));

		UploadFiles(urlString, files, params, new Listener<String>() {
			@Override
			public void onResponse(String response) {
				if (progressDialog.isShowing() && progressDialog != null) {
					progressDialog.dismiss();
				}
				ResourceUploadBean bean = ResourceUploadBeanParser
						.parseResourceUploadBean(response);
				if (bean == null) {
					reportToast("上传素材失败!");
					return;
				}
				if (bean.code == 0) {
					if (bean.obj != null && bean.obj.size() > 0) {
						Log.d(TAG, response);
						ArrayList<String> resIds = new ArrayList<String>();
						for (ResourceUploadobjBean fileInfo : bean.obj) {
							if (fileInfo.status == 0) {
								resIds.add(Integer.toString(fileInfo.fileId));
							}
						}
						if (resIds.size() == 0) {
							reportToast("上传成功素材个数为0");
							return;
						}
						startPlanEditActivity(path,resIds.get(0));
					}
				} else if (bean.code == -2) {
					returnToLogin();
				} else {
					reportToast(bean.msg);
				}
			}

		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				if (progressDialog.isShowing() && progressDialog != null) {
					progressDialog.dismiss();
				}
				reportToast("上传素材失败!");
			}

		}, null);
	}
	
	private void startClassEditActivity(String localPath,String resId){	
		Intent intent = new Intent(ClassPictureEditActivity.this,ClassInfoEditActivity.class);
		//素材的本地路径,没有修改可以传null
		intent.putExtra(CommonBundleName.PIC_PATH_TAG, localPath);	
		//素材的id,没有修改可以传null
		intent.putExtra(CommonBundleName.RES_ID_TAG, resId);
		intent.putExtra("title", "");
		intent.putExtra("title1", "");
		intent.putExtra("title2", "");
		intent.putExtra("desc", "");
		intent.putExtra(CommonBundleName.TEMPLATE_DIR_PATH_TAG,mTemplateInfo.mEditDirPath);
		intent.putExtra(CommonBundleName.CLASSINFO_TYPE_TAG,classInfoType);
		intent.putExtra(CommonBundleName.OFFLINE_TAG,false);
		intent.putExtra(CommonBundleName.AuditClassInfoAuthority, 
				getIntent().getBooleanExtra(CommonBundleName.AuditClassInfoAuthority, false));
		//data数据直接传到下一级界面
		intent.putExtra(CommonBundleName.DATA_TAG, ClassPictureEditActivity.this.getIntent().getSerializableExtra(CommonBundleName.DATA_TAG));
		startActivity(intent);
		finish();
	}

	// 在线模板班牌素材上传
	private void sendImage(final String path) {
		if( mPicEditView.getModifiedFlag() == false ){//文件未修改，不重新上传素材
			startClassEditActivity(path,null);
			return;
		}
		String urlString = UrlUtils.getResourceUploadUrl();
		Log.i(TAG, "URL:" + urlString);
		Map<String, File> files = new HashMap<String, File>();

		Map<String, String> params = new HashMap<String, String>();

		int fileType = MaterialParams.TYPE_CLASS_PICTURE;
		int orientation = getResources().getConfiguration().orientation;
		if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
			fileType = MaterialParams.TYPE_CLASS_PICTURE_SPECIAL;
		}
		// 上传模板分类
		int species = this.getIntent().getIntExtra(CommonBundleName.SPECIES_TAG,0);
		params.put("species", Integer.toString(species));
		params.put("fileType", Integer.toString(fileType));
		files.put("file1", new File(path));

		UploadFiles(urlString, files, params, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				if (progressDialog.isShowing() && progressDialog != null) {
					progressDialog.dismiss();
				}

				ResourceUploadBean bean = ResourceUploadBeanParser
						.parseResourceUploadBean(response);
				if (bean == null) {
					reportToast("上传素材失败!");
					return;
				}
				if (bean.code == 0) {
					if (bean.obj != null && bean.obj.size() > 0) {
						Log.d(TAG, response);
						ArrayList<String> resIds = new ArrayList<String>();
						for (ResourceUploadobjBean fileInfo : bean.obj) {
							if (fileInfo.status == 0) {
								resIds.add(Integer.toString(fileInfo.fileId));
							}
						}
						if (resIds.size() == 0) {
							reportToast("上传成功素材个数为0");
							return;
						}
						startClassEditActivity(path,resIds.get(0));						
					}
				} else if (bean.code == -2) {
					returnToLogin();
				} else {
					reportToast(bean.msg);
				}
			}

		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				if (progressDialog.isShowing() && progressDialog != null) {
					progressDialog.dismiss();
				}
				reportToast("上传素材失败!");
			}

		}, null);
	}

	public  void UploadFiles(final String url, final Map<String, File> files,
			final Map<String, String> params,
			final Listener<String> responseListener,
			final ErrorListener errorListener, final Object tag) {
		if (null == url || null == responseListener) {
			return;
		}

		progressDialog = ProgressDialog.show(this, "", "...Loading...");
		RequestQueue mSingleQueue = Volley.newRequestQueue(this,
				new MultiPartStack());
		MultiPartStringRequest multiPartRequest = new MultiPartStringRequest(
				Request.Method.POST, url, responseListener, errorListener) {

			@Override
			public Map<String, File> getFileUploads() {
				return files;
			}

			@Override
			public Map<String, String> getStringUploads() {
				return params;
			}

		};

		multiPartRequest.setRetryPolicy(new RetryPolicy() {

			@Override
			public void retry(VolleyError error) throws VolleyError {
				Toast.makeText(ClassPictureEditActivity.this, "网络连接失败!",
						Toast.LENGTH_LONG).show();
			}

			@Override
			public int getCurrentTimeout() {
				// TODO Auto-generated method stub
				return 20 * 1000;
			}

			@Override
			public int getCurrentRetryCount() {
				// TODO Auto-generated method stub
				return 2;
			}
		});
		multiPartRequest.setCookie(HttpClientDownloader.getInstance()
				.getCookie());
		mSingleQueue.add(multiPartRequest);
	}

	private View.OnTouchListener mOnFontBtnTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			switch (action) {
			case MotionEvent.ACTION_DOWN: {
				Message msg = Message.obtain(mHandler, MSG_LONG_PRESS, v);
				mHandler.sendMessageDelayed(msg, 500);
				v.setPressed(true);
				break;
			}

			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				if (mHandler.hasMessages(MSG_LONG_PRESS)) {
					v.performClick();
				}
				mHandler.removeMessages(MSG_LONG_PRESS_CONTINUE);
				mHandler.removeMessages(MSG_LONG_PRESS);
				break;
			}
			return true;
		}
	};

	private void onAdd() {
		showTextEditDialog(null);
	}

	private void onModify() {
		CharSequence s = mPicEditView.getActiveText();
		if (s != null) {
			showTextEditDialog(s);
		}
	}

	protected void showColorPickDialog() {
		// int initalColor = Color.parseColor(color);
		ColorPickerDialog dlg = new ColorPickerDialog(this, "颜色选择",
				new ColorPickerDialog.OnColorChangedListener() {

					@Override
					public void colorChanged(int color) {
						mPicEditView.setTextColor(color);
					}
				});
		dlg.show();
	}

	protected void showFontPickDialog() {
		String[] items = new String[] { "8", "9", "10", "11", "12", "13", "14",
				"15", "16", "17", "18", "19", "20", "21", "22", "23", "24",
				"25", "26", "27", "28", "29", "30", "31", "32", "33", "34",
				"35", "36", "37", "38", "39", "40", "41", "42", "43", "44",
				"45", "46", "47", "48", "49", "50", "51", "52", "53", "54",
				"55", "56", "57", "58", "59", "60", "61", "62", "63", "64",
				"65", "66", "67", "68", "69", "70", "71", "72", "73", "74",
				"75", "76", "77", "78", "79", "80", };
		AlertDialog dlg = new AlertDialog.Builder(this)
				.setTitle("选择字体")
				.setSingleChoiceItems(items, 20,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								mPicEditView.setTextSize(which + 8);
							}
						}).create();
		dlg.show();
	}

	// 添加文字或修改文字
	private void showTextEditDialog(final CharSequence text) {
		View contentView = LayoutInflater.from(this).inflate(R.layout.popwin_multiedit, null);
		
		
		WindowManager manager = (WindowManager) getSystemService(this.WINDOW_SERVICE);
		
		int width = manager.getDefaultDisplay().getWidth()*3/4;
		final PopupWindow popupWindow = new PopupWindow(contentView,
				width, ViewGroup.LayoutParams.WRAP_CONTENT, true);

		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setFocusable(true);
	      // 不带参的方法已经deprecated
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.showAtLocation(findViewById(R.id.main), Gravity.CENTER, 0, 0);
        
        WindowManager.LayoutParams params = getWindow().getAttributes();
		// 当弹出Popupwindow时，背景变半透明
		params.alpha = 0.5f;
		getWindow().setAttributes(params);
		
		final EditText edit = (EditText) (contentView.findViewById(R.id.editText));
		edit.setText(text);
		
		TextView okTv = (TextView) (contentView.findViewById(R.id.okTv));
		okTv.setOnClickListener(new View.OnClickListener() {//完成
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String data = edit.getText().toString();
				if (data == null || data.isEmpty()) {// 输入数据为空
					Toast.makeText(ClassPictureEditActivity.this,R.string.data_is_null, Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (text != null) {
					mPicEditView.modifyText(data);
				} else {
					mPicEditView.addText(data);
				}

				popupWindow.dismiss();
			}
		});
		
		// 设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				WindowManager.LayoutParams params = getWindow().getAttributes();
				params.alpha = 1f;
				getWindow().setAttributes(params);

			}
		});
		
		edit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
	}
	
	//加载模板编辑信息
	public void loadTemplateEditImg(List<TemplateEditInfo> editInfos)
	{
		if ( editInfos != null && editInfos.size() > 0) {
			for (int i = 0; i < editInfos.size(); i++) {
				if(editInfos.get(i).getType().equals("text"))
				{
					mPicEditView.addText(editInfos.get(i));
				}
				else addImage(editInfos.get(i));
			}
		}
	}
	//删除模板编辑临时信息目录
	private void deleteTmpTemplateDir()
	{
		TemplatePkgTool.deleteTmpTemplateDir(this);
	}
	
}
