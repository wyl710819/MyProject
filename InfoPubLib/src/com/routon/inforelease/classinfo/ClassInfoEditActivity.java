package com.routon.inforelease.classinfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.routon.ad.pkg.EClassPkgTools;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.json.ClassInfoListfilesBean;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.net.NetWorkRequest;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.offline.OfflineReleaseTools;
import com.routon.inforelease.plan.MaterialParams;
import com.routon.inforelease.plan.StringUtils;
import com.routon.inforelease.plan.adapter.ClassInfoEditListAdapter;
import com.routon.inforelease.plan.create.ClassPreviewActivity;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.OfflinePicSelectActivity;
import com.routon.inforelease.plan.create.PictureSelectActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.inforelease.util.TemplatePkgTool;
import com.routon.inforelease.util.TimeUtils;
import com.routon.inforelease.widget.ClassPreviewView;
import com.routon.inforelease.widget.ClassPreviewView.OnPreviewClickListener;
import com.routon.inforelease.widget.DateTimePickerHelper;
import com.routon.inforelease.widget.GroupSelActivity;
import com.routon.inforelease.widget.PopupList;
import com.routon.inforelease.widget.SettingItem;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

public class ClassInfoEditActivity extends CustomTitleActivity implements OnPreviewClickListener {
	private static final String TAG = "ClassInfoEditActivity";
	private EditText mTitle;
	private EditText mTitle1;
	private EditText mTitle2;
	private EditText mDesc;
	// private GridView mGridView;
	private Context mContext;
//	private Button mBtnSelectPicture;
	
	public static final int TOP_SCREEN_PRIORITY = 10;
	
	private ArrayList<ClassInfoListfilesBean> mResIds = new ArrayList<ClassInfoListfilesBean>();

	private ClassInfoListdatasBean mClassInfoData;

	private boolean mEdit; // 编辑 or 新建

	private int classInfoType;
	String startTime = null;
	String endTime = null;
	int mPriority = 0;
	private SimpleDateFormat mShowSdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm);
//	private ArrayList<String> files;
	private boolean mOffLineFlag = false;
	private String mTemplateDirPath = null;
	private boolean mAuditAuthority;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.layout_class_info_edit);
		Bundle bundle = this.getIntent().getExtras();
		String resId = null;
		String picPath = null;
		if (bundle != null) {
			classInfoType = bundle.getInt(CommonBundleName.CLASSINFO_TYPE_TAG, 1);
			mOffLineFlag = bundle.getBoolean(CommonBundleName.OFFLINE_TAG, false);	
			
			mAuditAuthority = bundle.getBoolean(CommonBundleName.AuditClassInfoAuthority);
			
			//模板编辑目录
			mTemplateDirPath = bundle.getString(CommonBundleName.TEMPLATE_DIR_PATH_TAG);		
			//模板编辑后生成的图片资源id
			resId = bundle.getString(CommonBundleName.RES_ID_TAG);		
			//模板编辑后生成的图片的文件路径
			picPath = bundle.getString(CommonBundleName.PIC_PATH_TAG);
			mClassInfoData = (ClassInfoListdatasBean) this.getIntent().getSerializableExtra(CommonBundleName.DATA_TAG);
			
			Log.d(TAG,"mClassInfoData:"+mClassInfoData);
			mResIds.clear();
			if( mClassInfoData != null ){
				if( resId != null ){
					mClassInfoData.files.get(0).resid = Integer.parseInt(resId);
				}
				if( picPath != null ){
					mClassInfoData.files.get(0).content = picPath;
				}
				if( mTemplateDirPath != null ){
					mResIds.add(mClassInfoData.files.get(0));
				}else{
					mResIds.addAll(mClassInfoData.files);
				}
			}else{
				ArrayList<String> selectResIds = bundle.getStringArrayList(CommonBundleName.SELECT_PIC_PARAM_TAG);
				ArrayList<String> selectResUrls = bundle.getStringArrayList(CommonBundleName.SELECT_PIC_URL_TAG);
				if (selectResIds != null && selectResUrls != null) {
					for (int i = 0; i < selectResIds.size(); i++) {
						ClassInfoListfilesBean bean = new ClassInfoListfilesBean();
						bean.content = selectResUrls.get(i);
						bean.resid = StringUtils.toInteger(selectResIds.get(i), 0);
						mResIds.add(bean);
					}
				}
				
				if( resId != null && picPath != null ){
					ClassInfoListfilesBean bean = new ClassInfoListfilesBean();
					bean.content = picPath;
					bean.resid = Integer.parseInt(resId);
					mResIds.add(bean);
				}
			}
		}
		
		if (mClassInfoData != null) {
			groupsId = mClassInfoData.groupIds;
			startTime = mClassInfoData.startTime;
			endTime = mClassInfoData.endTime;
			mEdit = true;
			classInfoType = mClassInfoData.type;
			mPriority = mClassInfoData.priority;
		}
		
		GroupListData.getClassListData(this, new DataResponse.Listener<ArrayList<GroupInfo>>() {

			@Override
			public void onResponse(ArrayList<GroupInfo> response) {
				// TODO Auto-generated method stub
				if( groupsId == null ){
					if( InfoReleaseApplication.authenobjData.headTeacherClasses != null 
							&& InfoReleaseApplication.authenobjData.headTeacherClasses.length == 1 ){
						//一个班级班主任，默认选中作为班主任的班级
						groupsId = InfoReleaseApplication.authenobjData.headTeacherClasses[0];
					}else if( response != null && response.size() == 1 ){
						groupsId = String.valueOf(response.get(0).getId());
					}
				}
			}
		}, null, null);

		mContext = this;

		initView();

		if ( mTemplateDirPath == null) {//普通内容编辑界面
			imagePathList = new ArrayList<String>();
			for (int i = 0; i < mResIds.size(); i++) {
				imagePathList.add(mResIds.get(i).content);
			}
			classInfoVp.setItems(imagePathList, imagePathList.get(0),
					mClassInfoData, ClassInfoEditActivity.this,
					ClassPreviewView.TYPE_CLASSEDIT);
		} else {//模板内容编辑界面
			if( picPath == null ){
				picPath = mClassInfoData.files.get(0).content;
			}
			Bitmap bmp = BitmapFactory.decodeFile(picPath);
			editImg.setImageBitmap(bmp);
		}

		this.setTouchUnDealView(classInfoVp);
	}

	private void initView() {
		initStartAndEndTime(startTime, endTime);
		mTitle = (EditText) findViewById(R.id.edit_class_info_title);
		mTitle1 = (EditText) findViewById(R.id.edit_class_info_title1);
		mTitle2 = (EditText) findViewById(R.id.edit_class_info_title2);
		mDesc = (EditText) findViewById(R.id.edit_class_info_desc);
		ImageButton topScreenSwitch = (ImageButton)findViewById(R.id.topscreen_switch);
		topScreenSwitch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				arg0.setSelected(!arg0.isSelected());
				if( arg0.isSelected() == false ){
					mPriority = 0; 
				}else{
					mPriority = TOP_SCREEN_PRIORITY; 
				}
			}
		});
			
		
		if( mPriority == 0 ){
			topScreenSwitch.setSelected(false);
		}else{
			topScreenSwitch.setSelected(true);
		}		

		if ( mTemplateDirPath != null ) {//模板编辑界面
			editImg = (ImageView) findViewById(R.id.class_info_edit_img);
			editImg.setVisibility(View.VISIBLE);
			LinearLayout descLayout = (LinearLayout) findViewById(R.id.desc_llayout);
			LinearLayout titleLayout = (LinearLayout) findViewById(R.id.title_llayout);
			LinearLayout title1Layout = (LinearLayout) findViewById(R.id.title1_llayout);
			LinearLayout title2Layout = (LinearLayout) findViewById(R.id.title2_llayout);
			descLayout.setVisibility(View.GONE);
			titleLayout.setVisibility(View.GONE);
			title1Layout.setVisibility(View.GONE);
			title2Layout.setVisibility(View.GONE);
		}
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;

		classInfoVp = (ClassPreviewView) findViewById(R.id.class_info_edit_vp);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				screenWidth, screenWidth * 9 / 16);
		classInfoVp.setLayoutParams(params);
		picVp = (ViewPager) classInfoVp.findViewById(R.id.pic_vp);

		SettingItem item = (SettingItem) findViewById(R.id.groupitem);

		if( mAuditAuthority == false ){
			item.setMoreClicked(true);
		}	
		item.setName(Html.fromHtml(getResources().getString(R.string.publish_group_must)));
		item.setInfo("0");
		// 选择分组
		item.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub	
				if( mAuditAuthority == false ){
					Intent intent = new Intent();
					intent.setClass(ClassInfoEditActivity.this, GroupSelActivity.class);
					intent.putExtra("select_param", groupsId);
					intent.putExtra(CommonBundleName.GROUP_SEL_HEADTEACHERS, true);
					ClassInfoEditActivity.this.startActivityForResult(intent, 0);
				}
			}
		});
		

		updateGroupTip();

		if (mClassInfoData != null) {
			mTitle.setText(mClassInfoData.title);
			mTitle1.setText(mClassInfoData.subtitle1);
			mTitle2.setText(mClassInfoData.subtitle2);
			mDesc.setText(mClassInfoData.desc);
		}
		ClassInfoEditListAdapter adapter = new ClassInfoEditListAdapter(mContext, mResIds);
		
		adapter.updateText(mTitle.getText().toString(), mTitle1.getText().toString(), 
		mTitle2.getText().toString(), mDesc.getText().toString());
		mTitle.addTextChangedListener(mOnTextChangedListener);
		mTitle1.addTextChangedListener(mOnTextChangedListener);
		mTitle2.addTextChangedListener(mOnTextChangedListener);
		mDesc.addTextChangedListener(mOnTextChangedListener);

		// mGridView.setAdapter(adapter);

//		updatePictureSelectButton();

		if (mEdit) {
			this.initTitleBar("编辑内容");
		} else {
			this.initTitleBar("新建内容");
		}
		// 新建内容完成
		this.setTitleNextBtnClickListener(getResources().getString(R.string.menu_finish),
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (groupsId == null || groupsId.equals("")) {
							reportToast("请选择发布分组");
							return;
						}
						if ( mTemplateDirPath != null ) {//模板编辑信息包					
							if (false == checkTime()) {
								return;
							}
							sendTemplateZipFile();
						} else {
							confirmEdit();
						}
					}
				});
		
		findViewById(R.id.group_bottom).setVisibility(View.GONE);

	}

	private void confirmEdit() {

		if (mResIds.size() == 0) {
			reportToast("请至少添加一张图片");
			return;
		}
		if (mResIds.size() > 9) {
			reportToast("最多不能超过9张图片");
			return;
		}
		String title = mTitle.getText().toString();
		String title1 = mTitle1.getText().toString();
		String title2 = mTitle2.getText().toString();
		String desc = mDesc.getText().toString();

		if ( mOffLineFlag == true ) {
			ClassInfoListdatasBean bean = mClassInfoData;
			if (bean == null) {
				bean = EClassPkgTools.makeNewOfflineClassInfo(1);
			}
			bean.files = mResIds;
			bean.title = title;
			bean.subtitle1 = title1;
			bean.subtitle2 = title2;
			bean.desc = desc;
			new OfflineReleaseTools(ClassInfoEditActivity.this).saveOfflineClassInfo(bean,
							new OfflineReleaseTools.OnTaskFinishListener() {
								@Override
								public void onFinished(int errcode) {
									if (errcode == 0) {
										finish();
									}
								}
							});

		} else {
			// Intent intent = new Intent(ClassInfoEditActivity.this,
			// ClassInfoGroupSelectActivity.class);
			// intent.putExtra("title", title);
			// intent.putExtra("title1", title1);
			// intent.putExtra("title2", title2);
			// intent.putExtra("desc", desc);
			// intent.putExtra("classInfoType", classInfoType);
			// ArrayList<String> resIds = new ArrayList<String>();
			// for (ClassInfoListfilesBean bean : mResIds) {
			// resIds.add(Integer.toString(bean.resid));
			// }
			// intent.putStringArrayListExtra("files", resIds);
			// if (mClassInfoData != null) {
			// intent.putExtra("id", mClassInfoData.id);
			// intent.putExtra("select_param", mClassInfoData.groupIds);
			// }
			// startActivity(intent);
			// finish();
			if (false == checkTime()) {
				return;
			}

			sendNormalClassInfo();

		}

	}
	
	private void sendPublishRequest(final String ids){
		Log.v(TAG, "class info publish: " + ids); 
        String urlString = UrlUtils.getClassInfoPublishUrl(ids);
        Log.i(TAG, "URL:" + urlString);
        
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if( bean == null ){
								return;
							}
							if (bean.code == 0) {
							} else if( bean.code == -2 ){
								InfoReleaseApplication.returnToLogin(ClassInfoEditActivity.this);
								return;
							} else {
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {
                    }  
                });
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}

	private void sendNormalClassInfo() {
		Bundle bundle = this.getIntent().getExtras();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if ( mClassInfoData != null && mClassInfoData.id != 0 ) {
			params.add(new BasicNameValuePair("id", Integer.toString(mClassInfoData.id)));
		}
		params.add(new BasicNameValuePair("title", mTitle.getText().toString()));
		params.add(new BasicNameValuePair("subTitle1", mTitle1.getText().toString()));
		params.add(new BasicNameValuePair("subTitle2", mTitle2.getText().toString()));
		params.add(new BasicNameValuePair("desc", mDesc.getText().toString()));
		params.add(new BasicNameValuePair("type", Integer.toString(classInfoType)));
		params.add(new BasicNameValuePair("groupIds", groupsId));
		params.add(new BasicNameValuePair("priority",  Integer.toString(mPriority)));
		if (mStartTimeItem.getInfo() != null&& mStartTimeItem.getInfo().isEmpty() == false) {
			params.add(new BasicNameValuePair("startTimeStr", mStartTimeItem.getInfo() + ":00"));
		}
		if (mEndTimeItem.getInfo() != null&& mEndTimeItem.getInfo().isEmpty() == false) {
			params.add(new BasicNameValuePair("endTimeStr", mEndTimeItem.getInfo()+ ":00"));
		}
		Log.v(TAG, "saveClassInfo startTime: " + startTime + ",endTime:"+ endTime);
		String fileIds = "";
		for (ClassInfoListfilesBean bean : mResIds) {
			fileIds += bean.resid;
			fileIds += ",";
		}

		params.add(new BasicNameValuePair("fileIds", fileIds));
		Log.v(TAG, "fileIds: " + fileIds);

		this.showProgressDialog();

		String urlString = UrlUtils.getClassInfoSaveUrl();// +"&startTimeStr="+mStartTimeItem.getInfo()+":00";
		Log.i(TAG, "URL:" + urlString+",params:"+params);

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideProgressDialog();
						try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if (bean.code == 0) {
								if (mClassInfoData != null) {
									PublishStateUtils.removeData(ClassInfoEditActivity.this,getDir("isPublishClass.txt",
													Context.MODE_PRIVATE).getPath(), String.valueOf(mClassInfoData.id));
								}
								if( mAuditAuthority == true ){
									sendPublishRequest(Integer.toString(mClassInfoData.id));
								}
								notifyClassInfoListChanged();
								
								finish();
							} else if (bean.code == -2) {
								returnToLogin();
							} else {
								if( bean.msg == null || bean.msg.isEmpty() ){
									reportToast("添加班牌实时信息失败");
								}else{
									reportToast(bean.msg);
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if( InfoReleaseApplication.showNetWorkFailed(mContext) == true ){
							reportToast("获取数据失败!");
						}
						hideProgressDialog();
					}
				});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	

//	private void updatePictureSelectButton() {
//		String text = "添加图片";
//		// if (mResIds.size() > 0) {
//		// text = "重新选择图片";
//		// }
//		mBtnSelectPicture.setText(text);
//	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 0) {
			if (resultCode == Activity.RESULT_OK) {

				groupsId = data.getStringExtra("groupids");
				if( groupsId.endsWith(",") ){
					groupsId = groupsId.substring(0, groupsId.length()-1);
				}
				Log.d(TAG,"onActivityResult groupsId:"+groupsId);
				if (groupsId != null) {
					updateGroupTip();
				} else {
					if (mClassInfoData != null) {
						groupsId = mClassInfoData.groupIds;
						updateGroupTip();
					}

				}
			}
		} else if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
				ArrayList<String> selectResIds = data.getStringArrayListExtra("select_pic_param");
				ArrayList<String> selectResUrls = data.getStringArrayListExtra("select_pic_url");
				if (selectResIds != null && selectResUrls != null) {
					for (int i = 0; i < selectResIds.size(); i++) {
						Log.v(TAG, "select id: " + selectResIds.get(i)
								+ " url: " + selectResUrls.get(i));
						ClassInfoListfilesBean bean = new ClassInfoListfilesBean();
						bean.content = selectResUrls.get(i);
						bean.resid = StringUtils.toInteger(selectResIds.get(i),0);
						mResIds.add(bean);
					}

					for (int i = 0; i < selectResUrls.size(); i++) {
						imagePathList.add(selectResUrls.get(i));
					}
					classInfoVp.setItems(imagePathList, imagePathList.get(0),
							mClassInfoData, ClassInfoEditActivity.this,
							ClassPreviewView.TYPE_CLASSEDIT);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private TextWatcher mOnTextChangedListener = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,int count) {
			// ClassInfoEditListAdapter adapter = (ClassInfoEditListAdapter)
			// mGridView.getAdapter();
			// adapter.updateText(mTitle.getText().toString(),
			// mTitle1.getText().toString(), mTitle2.getText().toString(),
			// mDesc.getText().toString());

			classInfoVp.updateText(mTitle.getText().toString(), mTitle1
					.getText().toString(), mTitle2.getText().toString(), mDesc
					.getText().toString());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	private ClassPreviewView classInfoVp;
	private String groupsId;

	private List<String> popupMenuItemList = new ArrayList<String>();

	public void showPopupList(View anchorView, int contextPosition) {
		popupMenuItemList.clear();
		popupMenuItemList.add("预览");
		popupMenuItemList.add("添加");
		popupMenuItemList.add("删除");
		if (contextPosition > 0) {
			popupMenuItemList.add("前移");
		}
		if (mResIds != null && contextPosition < mResIds.size() - 1) {
			popupMenuItemList.add("后移");
		}

		PopupList popupList = new PopupList(this);
		popupList.setTextPaddingLeft(popupList.dp2px(14));
		popupList.setTextPaddingRight(popupList.dp2px(14));
		popupList.showPopupListWindowAtCenter(anchorView, contextPosition,
				popupMenuItemList, new PopupList.PopupListListener() {
					@Override
					public void onPopupListClick(View contextView,int contextPosition, int position) {
						switch (position) {
						case 0:
							ClassInfoListfilesBean bean = mResIds.get(contextPosition);
							ArrayList<String> contentList = new ArrayList<String>();
							for (int i = 0; i < mResIds.size(); i++) {
								contentList.add(mResIds.get(i).content);
							}
							Intent previewIntent = new Intent(ClassInfoEditActivity.this,ClassPreviewActivity.class);
							previewIntent.putExtra("path", bean.content);
							previewIntent.putExtra("startBy", "class_info_edit");
							previewIntent.putExtra("title", mTitle.getText().toString());
							previewIntent.putExtra("subtitle1", mTitle1.getText().toString());
							previewIntent.putExtra("subtitle2", mTitle2.getText().toString());
							previewIntent.putExtra("desc", mDesc.getText().toString());
							previewIntent.putStringArrayListExtra("content_list", contentList);
							Bundle bundle = new Bundle();
							bundle.putSerializable("class_info_datas",mClassInfoData);
							previewIntent.putExtras(bundle);
							ClassInfoEditActivity.this.startActivity(previewIntent);

							break;
						case 1://添加
							if( mResIds.size() == ImageUtils.MAX_CLASS_PICTURE_NUM ){
								reportToast("当前图片数目为"+ImageUtils.MAX_CLASS_PICTURE_NUM+"，不可再添加图片");
								return;
							}
							Intent intent;
							if (mClassInfoData != null) {
								if (mClassInfoData.isOffLine()) {// 离线班牌编辑
									intent = new Intent(ClassInfoEditActivity.this,OfflinePicSelectActivity.class);
								} else {// 在线班牌编辑
									intent = new Intent(ClassInfoEditActivity.this,PictureSelectActivity.class);
								}
							} else {
								if ( mOffLineFlag == true ) {//
									intent = new Intent(ClassInfoEditActivity.this,OfflinePicSelectActivity.class);
								} else {
									intent = new Intent(ClassInfoEditActivity.this,PictureSelectActivity.class);

								}
							}
							intent.putExtra(CommonBundleName.MAX_SEL_PIC_NUM, ImageUtils.MAX_CLASS_PICTURE_NUM - mResIds.size());
							intent.putExtra("start_by", "edit");
							if (classInfoType == 2) // 侧位班牌
								intent.putExtra("fileType",MaterialParams.TYPE_CLASS_PICTURE_SPECIAL);
							else
								intent.putExtra("fileType",MaterialParams.TYPE_CLASS_PICTURE);
							startActivityForResult(intent, 1);
							break;

						case 2:
							if (mResIds.size() <= 1) {
								reportToast("至少保留一张图片!");
								return;
							}
							ClassInfoListfilesBean bean2 = mResIds.get(contextPosition);
							mResIds.remove(bean2);
							for (int i = 0; i < imagePathList.size(); i++) {
								if (imagePathList.get(i).equals(bean2.content)) {
									imagePathList.remove(i);
								}
							}
							classInfoVp.setItems(imagePathList,
									imagePathList.get(0), mClassInfoData,ClassInfoEditActivity.this,
									ClassPreviewView.TYPE_CLASSEDIT);
							break;

						case 3:
							if (contextPosition > 0) {
								if (imagePathList.size() > 1&& contextPosition != 0) {
									String imgPath = imagePathList.get(contextPosition);
									imagePathList.remove(contextPosition);
									imagePathList.add(contextPosition - 1,imgPath);
									ClassInfoListfilesBean classInfoListfilesBean = mResIds.get(contextPosition);
									mResIds.remove(contextPosition);
									mResIds.add(contextPosition - 1,classInfoListfilesBean);
									classInfoVp.setItems(imagePathList,imagePathList.get(contextPosition - 1),mClassInfoData,
											ClassInfoEditActivity.this,ClassPreviewView.TYPE_CLASSEDIT);
								}
							} else if (mResIds != null&& contextPosition < mResIds.size() - 1) {
								String imgPath = imagePathList.get(contextPosition);
								imagePathList.remove(contextPosition);
								imagePathList.add(contextPosition + 1, imgPath);
								ClassInfoListfilesBean classInfoListfilesBean = mResIds.get(contextPosition);
								mResIds.remove(contextPosition);
								mResIds.add(contextPosition + 1,classInfoListfilesBean);
								classInfoVp.setItems(imagePathList,imagePathList.get(contextPosition + 1),mClassInfoData,
										ClassInfoEditActivity.this,ClassPreviewView.TYPE_CLASSEDIT);
							}

							break;
						case 4:
							if (imagePathList.size() > 1&& contextPosition != imagePathList.size() - 1) {
								String imgPath = imagePathList.get(contextPosition);
								imagePathList.remove(contextPosition);
								imagePathList.add(contextPosition + 1, imgPath);
								ClassInfoListfilesBean classInfoListfilesBean = mResIds.get(contextPosition);
								mResIds.remove(contextPosition);
								mResIds.add(contextPosition + 1,classInfoListfilesBean);
								classInfoVp.setItems(imagePathList,imagePathList.get(contextPosition + 1),mClassInfoData,
										ClassInfoEditActivity.this,
										ClassPreviewView.TYPE_CLASSEDIT);
							}

							break;
						}
					}

					@Override
					public boolean showPopupList(View adapterView,
							View contextView, int contextPosition) {
						// TODO Auto-generated method stub
						return true;
					}
				});
	}

//	public void showExtraActionDlg(final int position) {
//		int array = R.array.class_info_edit_action_array;
//		AlertDialog dlg = new AlertDialog.Builder(this).setItems(array,
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						switch (which) {
//						case 0:
//							ClassInfoListfilesBean bean = mResIds.get(position);
//							ArrayList<String> contentList = new ArrayList<String>();
//							for (int i = 0; i < mResIds.size(); i++) {
//								contentList.add(mResIds.get(i).content);
//							}
//							Intent previewIntent = new Intent(ClassInfoEditActivity.this,ClassPreviewActivity.class);
//							previewIntent.putExtra("path", bean.content);
//							previewIntent.putStringArrayListExtra("content_list", contentList);
//							Bundle bundle = new Bundle();
//							bundle.putSerializable("class_info_datas",mClassInfoData);
//							previewIntent.putExtras(bundle);
//							ClassInfoEditActivity.this.startActivity(previewIntent);
//							break;
//
//						case 1:
//							if( mResIds.size() == ImageUtils.MAX_CLASS_PICTURE_NUM ){
//								reportToast("当前图片数目为"+ImageUtils.MAX_CLASS_PICTURE_NUM+"，不可再添加图片");
//								return;
//							}
//							Intent intent;
//							if (mClassInfoData != null) {
//								if (mClassInfoData.isOffLine()) {// 离线班牌编辑
//									intent = new Intent(ClassInfoEditActivity.this,OfflinePicSelectActivity.class);
//								} else {// 在线班牌编辑
//									intent = new Intent(ClassInfoEditActivity.this,PictureSelectActivity.class);
//								}
//							} else {
//								if ( mOffLineFlag == true ) {//
//									intent = new Intent(ClassInfoEditActivity.this,OfflinePicSelectActivity.class);
//								} else {
//									intent = new Intent(ClassInfoEditActivity.this,PictureSelectActivity.class);
//
//								}
//							}
//							intent.putExtra(CommonBundleName.MAX_SEL_PIC_NUM, ImageUtils.MAX_CLASS_PICTURE_NUM - mResIds.size());
//							intent.putExtra("start_by", "edit");
//							if (classInfoType == 2) // 侧位班牌
//								intent.putExtra("fileType",MaterialParams.TYPE_CLASS_PICTURE_SPECIAL);
//							else
//								intent.putExtra("fileType",MaterialParams.TYPE_CLASS_PICTURE);
//							startActivityForResult(intent, 0);
//
//							break;
//						case 2:
//							ClassInfoListfilesBean bean2 = mResIds.get(position);
//							mResIds.remove(bean2);
//							for (int i = 0; i < imagePathList.size(); i++) {
//								if (imagePathList.get(i).equals(bean2.content)) {
//									imagePathList.remove(i);
//								}
//							}
//							classInfoVp.setItems(imagePathList,
//									imagePathList.get(0), mClassInfoData,
//									ClassInfoEditActivity.this,
//									ClassPreviewView.TYPE_CLASSEDIT);
//							break;
//						case 3:// 前移
//
//							if (imagePathList.size() > 1 && position != 0) {
//								String imgPath = imagePathList.get(position);
//								imagePathList.remove(position);
//								imagePathList.add(position - 1, imgPath);
//								classInfoVp.setItems(imagePathList,
//										imagePathList.get(position - 1),
//										mClassInfoData,
//										ClassInfoEditActivity.this,
//										ClassPreviewView.TYPE_CLASSEDIT);
//							}
//
//							break;
//
//						case 4:// 后移
//							if (imagePathList.size() > 1&& position != imagePathList.size() - 1) {
//								Toast.makeText(mContext, "后移",Toast.LENGTH_SHORT).show();
//								String imgPath = imagePathList.get(position);
//								imagePathList.remove(position);
//								imagePathList.add(position + 1, imgPath);
//								classInfoVp.setItems(imagePathList,
//										imagePathList.get(position + 1),
//										mClassInfoData,
//										ClassInfoEditActivity.this,
//										ClassPreviewView.TYPE_CLASSEDIT);
//							}
//
//							break;
//						}
//					}
//				}).create();
//		dlg.show();
//	}

	private void updateGroupTip() {
		if (groupsId != null) {
			String[] groups = groupsId.split(",");
			((SettingItem) findViewById(R.id.groupitem)).setInfo(String.valueOf(groups.length));
		}
	}

	private SettingItem mStartTimeItem = null;
	private SettingItem mEndTimeItem = null;
	private Calendar mStartTime = null;
	private Calendar mEndTime = null;
//	private int class_id;
	private List<String> imagePathList;
	private ViewPager picVp;

	private void initStartAndEndTime(String startTime, String endTime) {
		Log.d(TAG, "initStartAndEndTime startTime:" + startTime + ",endTime:"+ endTime);
		mStartTimeItem = ((SettingItem) findViewById(R.id.starttime_item));
		mStartTimeItem.setMoreClicked(true);
		mStartTimeItem.setName(Html.fromHtml(getResources().getString(R.string.starttime_must)));
		mStartTime = TimeUtils.getFormatCalendar(startTime,TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		mEndTime = TimeUtils.getFormatCalendar(endTime,TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		
		if( mStartTime == null ){
			mStartTime = Calendar.getInstance();
			mEndTime = Calendar.getInstance();
			mEndTime.add(Calendar.DATE, 1);
		}

		if (mStartTime != null) {
			mStartTimeItem.setInfo(mShowSdf.format(mStartTime.getTime()));
		}
		mStartTimeItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showDatePicker(true);
			}
		});
		mEndTimeItem = ((SettingItem) findViewById(R.id.endtime_item));
		mEndTimeItem.setMoreClicked(true);
		mEndTimeItem.setName(Html.fromHtml(getResources().getString(R.string.endtime_must)));
		if (mEndTime != null) {
			mEndTimeItem.setInfo(mShowSdf.format(mEndTime.getTime()));
		}
		mEndTimeItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showDatePicker(false);
			}
		});
		// starttimeItem.setInfo(String.valueOf(groups.length));
	}
	
	
	private void showDatePicker(final boolean startTime) {
		//不能设置小于当前时间的时间
		 Calendar curTime = Calendar.getInstance();
		 Calendar initTime = null;
		 long maxTime = 0;
		 if( startTime == true ){
			 initTime = mStartTime;
		 }else{
			 initTime = mEndTime;
		 }		
		 Calendar showTime = curTime;
		 if( initTime != null ){
			 if( TimeUtils.isTimeBeforeTilMinute(curTime, initTime)  ){
				 showTime = initTime;
			 }
		 }
           
         // Build DateTimeDialog  
		 DateTimePickerHelper.showDateTimePicker(this,showTime,curTime.getTimeInMillis(),0, new DateTimePickerHelper.OnClickListener() {  
             @Override  
             public void onClick(Calendar time) {  

 				 if( startTime == true ){//开始时间设置
// 					 if( checkTime(time,mEndTime) == false ){
// 						 return;
// 					 }
 					 mStartTime = time;
 					 mStartTimeItem.setInfo(mShowSdf.format(mStartTime.getTime()));
// 					//设置起始时间时，默认结束时间为起始时间后一天
// 					 if( mEndTime == null ){
	 					 mEndTime = Calendar.getInstance();
	 					 mEndTime.setTime(mStartTime.getTime());
	 					 if( TOP_SCREEN_PRIORITY == mPriority ){
	 						mEndTime.add(Calendar.DATE,1);
	 					 }else{
	 						mEndTime.add(Calendar.DATE,1);
	 					 }
	 					//修改结束时间
	 					 mEndTimeItem.setInfo(mShowSdf.format(mEndTime.getTime()));
// 					 }
 				 }else{//结束时间设置
 					//设置结束时间时，如果没有设置起始时间，默认起始时间为当前时间
 					 if( mStartTime == null ){
	 					//修改起始时间
	 					mStartTime = Calendar.getInstance();
	 					mStartTimeItem.setInfo(mShowSdf.format(mStartTime.getTime()));
 					 }
 					 if( checkTime(mStartTime,time) == false ){
 						 return;
 					 }
 					 //修改结束时间
 					 mEndTime = time;
 					 mEndTimeItem.setInfo(mShowSdf.format(mEndTime.getTime()));
 				 }
             }  
         });    
	}
	
	private boolean checkTime(Calendar startTime,Calendar endTime){
		if (startTime == null || endTime == null) {
			return false;
		}
		if (TimeUtils.isTimeBeforeTilMinute(endTime, startTime)) {
			reportToast("结束时间不能早于开始时间");
			return false;
		}
		if( TOP_SCREEN_PRIORITY == mPriority ){
			 if ( TimeUtils.getDayCount(endTime, startTime) > 3 ){
				 reportToast("插播节目时间间隔不能超过３天");
				 return false;
			 }
		}else{
			if ( TimeUtils.getDayCount(endTime, startTime) > 7 ){
				 reportToast("普通节目时间间隔不能超过７天");
				 return false;
			 }
		}
		
		return true;
	}

	private boolean checkTime() {
		return checkTime(mStartTime,mEndTime);
	}

	// 发送广播通知内容列表刷新
	private void notifyClassInfoListChanged() {
		Intent intent = new Intent(
				ClassInfoListFragment.ACTION_CLASS_INFO_LIST_CHANGED);
		sendBroadcast(intent);
	}

//	private String startBy;
//	private String path;
	private ImageView editImg;

//	// 上传新建内容
//	private void saveClassInfo(List<Integer> groupIds) {
//
//		Bundle bundle = this.getIntent().getExtras();
//		List<NameValuePair> params = new ArrayList<NameValuePair>();
//		int class_id = bundle.getInt("id", 0);
//		if (class_id != 0) {
//			params.add(new BasicNameValuePair("id", Integer.toString(class_id)));
//		}
//		params.add(new BasicNameValuePair("title", ""));
//		params.add(new BasicNameValuePair("subTitle1", ""));
//		params.add(new BasicNameValuePair("subTitle2", ""));
//		params.add(new BasicNameValuePair("desc", ""));
//		int classInfoType = bundle.getInt("classInfoType", 3);
//		params.add(new BasicNameValuePair("type", Integer.toString(classInfoType)));
//		if (mStartTimeItem.getInfo() != null&& mStartTimeItem.getInfo().isEmpty() == false) {
//			params.add(new BasicNameValuePair("startTime", mStartTimeItem.getInfo() + " 00:00:00"));
//		}
//		if (mEndTimeItem.getInfo() != null&& mEndTimeItem.getInfo().isEmpty() == false) {
//			params.add(new BasicNameValuePair("endTime", mEndTimeItem.getInfo()+ " 23:59:59"));
//		}
//		String ids = "";
//		int index = 0;
//		for (int id : groupIds) {
//			ids += id;
//			index++;
//			if( index < groupIds.size()){
//				ids += ",";
//			}
//		}
//		Log.v(TAG, "startTime: " + startTime + ",endTime:" + endTime);
//		params.add(new BasicNameValuePair("groupIds", ids));
//		ArrayList<String> resIds = bundle.getStringArrayList("files");
//		String fileIds = "";
//		for (String fileId : resIds) {
//			fileIds += fileId;
//			fileIds += ",";
//		}
//		params.add(new BasicNameValuePair("fileIds", fileIds));
//		Log.v(TAG, "fileIds: " + fileIds+",ids:"+ids);
//
//		final ProgressDialog progressDialog = ProgressDialog.show(this, "","...Loading...");
//
//		String urlString = UrlUtils.getClassInfoSaveUrl();
//		Log.i(TAG, "URL:" + urlString);
//
//		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
//				Request.Method.POST, urlString, params,
//				new Response.Listener<JSONObject>() {
//					@Override
//					public void onResponse(JSONObject response) {
//						Log.d(TAG, "response=" + response);
//						if (progressDialog.isShowing()&& progressDialog != null) {
//							progressDialog.dismiss();
//						}
//						try {
//							BaseBean bean = BaseBeanParser.parseBaseBean(response);
//							if (bean.code == 0) {
//								notifyClassInfoListChanged();
//								finish();
//							} else if (bean.code == -2) {
//								returnToLogin();
//							} else {
//								reportToast("添加内容模板失败");
//							}
//						} catch (JSONException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//
//					}
//				}, new Response.ErrorListener() {
//					@Override
//					public void onErrorResponse(VolleyError arg0) {
//						Toast.makeText(mContext, "网络连接失败!", Toast.LENGTH_LONG).show();
//						if (progressDialog.isShowing()&& progressDialog != null) {
//							progressDialog.dismiss();
//						}
//					}
//				});
//		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
//		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
//	}
	
	private File zipFile = null;
	
	//上传模板编辑信息ZIP
	private void sendTemplateZipFile() {
		Bundle bundle = this.getIntent().getExtras();
		Map<String, String> params = new HashMap<String, String>();
		Map<String, File> files = new HashMap<String, File>();
		if( zipFile == null ){//文件压缩后，源文件已经删除，直接使用压缩后的zip文件
			zipFile =  TemplatePkgTool.fileToZip(mTemplateDirPath, mTemplateDirPath, new File(mTemplateDirPath).getName());
		}
		files.put("editPkgFile", zipFile);
		Log.d(TAG,"sendTemplateZipFile file:"+zipFile+",exits:"+zipFile.exists());
		if ( mClassInfoData != null && mClassInfoData.id != 0 ) {
			params.put("id", Integer.toString(mClassInfoData.id));
		}
		params.put("title", "");
		params.put("subTitle1", "");
		params.put("subTitle2", "");
		params.put("desc", "");
		params.put("priority", Integer.toString(mPriority));		
		int classInfoType = bundle.getInt(CommonBundleName.CLASSINFO_TYPE_TAG, 3);
		params.put("type", Integer.toString(classInfoType));
		if (mStartTimeItem.getInfo() != null&& mStartTimeItem.getInfo().isEmpty() == false) {
			params.put("startTimeStr", mStartTimeItem.getInfo() + ":00");
		}
		if (mEndTimeItem.getInfo() != null&& mEndTimeItem.getInfo().isEmpty() == false) {
			params.put("endTimeStr", mEndTimeItem.getInfo() + ":00");
		}
//		String ids = "";
//		for (int id : groupIds) {
//			ids += id;
//			ids += ",";
//		}
		Log.v(TAG, "startTime: " + startTime + ",endTime:" + endTime);
		params.put("groupIds", groupsId);
		String fileIds = "";
		
		for (ClassInfoListfilesBean bean : mResIds) {
			fileIds += bean.resid;
			fileIds += ",";
		}
		params.put("fileIds", fileIds);
		Log.v(TAG, "fileIds: " + fileIds);

		showProgressDialog();

		String urlString = UrlUtils.getClassInfoSaveUrl();
		Log.i(TAG, "URL:" + urlString+",params:"+params);
		NetWorkRequest.UploadFiles(ClassInfoEditActivity.this,urlString, files, params, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				// TODO Auto-generated method stub
				hideProgressDialog();
				Log.d(TAG, response);
				try {
					BaseBean bean = BaseBeanParser.parseBaseBean(response);
					if (bean.code == 0) {
						if (mClassInfoData != null) {
							PublishStateUtils.removeData(ClassInfoEditActivity.this,getDir("isPublishClass.txt",
											Context.MODE_PRIVATE).getPath(), String.valueOf(mClassInfoData.id));
						}
						if( mAuditAuthority == true ){
							sendPublishRequest(Integer.toString(mClassInfoData.id));
						}
						notifyClassInfoListChanged();
						finish();
					} else if (bean.code == -2) {
						returnToLogin();
					} else {
						if( bean.msg == null || bean.msg.isEmpty() ){
							reportToast("添加班牌实时信息失败");
						}else{
							reportToast(bean.msg);
						}						
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				reportToast(error.getMessage());
				hideProgressDialog();
			}
		}, null);
	}

	// class preview view click listener
	@Override
	public void onPreviewClickListener(View v, int position) {
		// TODO Auto-generated method stub
		this.showPopupList(v, position);
	}
	
//	public void renameFile()
//	{
//		String resId = getIntent().getStringExtra("resId");
//		if( dirPath == null ){
//			return;
//		}
//		File file = new File(dirPath);	
//		File newFile = new File(file.getParent()+"/"+"template_"+resId);
//		newDirPath = newFile.getAbsolutePath();
//		if(file.exists()&&file.isDirectory()&&!newFile.exists())
//		{
//			file.renameTo(newFile);
//		}
//	}

}
