package com.routon.inforelease.plan;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.routon.ad.pkg.PkgTools;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.FindAdPeriodsperiodsBean;
import com.routon.inforelease.json.PlanListBeanParser;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.json.PlanMaterialparamsBean;
import com.routon.inforelease.json.PlanMaterialrowsBean;
import com.routon.inforelease.net.NetWorkRequest;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.offline.OfflineReleaseTools;
import com.routon.inforelease.plan.create.OfflinePicSelectActivity;
import com.routon.inforelease.plan.create.PictureSelectActivity;
import com.routon.inforelease.plan.create.TextSelectActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.inforelease.util.TemplatePkgTool;
import com.routon.inforelease.widget.GroupSelActivity;
import com.routon.inforelease.widget.PlanPreviewView;
import com.routon.inforelease.widget.PlanSubtitleView;
import com.routon.inforelease.widget.PopupList;
import com.routon.inforelease.widget.SettingItem;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class PlanInfoEditActivity extends CustomTitleActivity implements PlanPreviewView.OnPreviewClickListener{
	private final String TAG = PlanInfoEditActivity.this.getClass().getName();
	//计划数据
	private PlanListrowsBean mPlanListrowsBean;
	//图片素材列表
	private List<PlanMaterialrowsBean> mMaterialList;
	//文字素材列表
	private List<PlanMaterialrowsBean> mSubtitleList;
	private PlanPreviewView planVp;
	private LinearLayout editSubtitle;
	private PlanSubtitleView item;
	private RelativeLayout subtitleAdd;
	
	public final static int ADD_PLAN = 0;
	public final static int EDIT_PLAN = 1;
	public final static int ADD_TEMPLATE_PLAN = 2;
	public final static int EDIT_TEMPLATE_PLAN = 3;
	private int mType = ADD_PLAN;
	
	public static final String PLAN_TYPE = "plan_type";
	
	private String mTemplateDirPath = null;
	private boolean mOffLinePlan = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		setContentView(R.layout.layout_plan_info_edit);
		
		//取数据
		initData();
		
		//刷新界面
		initView();
	}

	private void initView() {
		editSubtitle = (LinearLayout) findViewById(R.id.plan_edit_subtitle);
		planVp = (PlanPreviewView) findViewById(R.id.plan_edit_view);
		subtitleAdd = (RelativeLayout) findViewById(R.id.subtitle_add);
		
		if( mOffLinePlan == false ){
			SettingItem item = (SettingItem) findViewById(R.id.groupitem);
			item.setVisibility(View.VISIBLE);
			item.setMoreClicked(true);
			item.setName(Html.fromHtml(getResources().getString(R.string.publish_group_must)));
			item.setInfo("0");
			// 选择分组
			item.setOnClickListener(new View.OnClickListener() {
	
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.setClass(PlanInfoEditActivity.this,GroupSelActivity.class);
					intent.putExtra("select_param", mPlanListrowsBean.groups);
					PlanInfoEditActivity.this.startActivityForResult(intent, REQUEST_CODE_GROUP_SELECT);
				}
			});
	
			updateGroupTip();
		}
		
		addPicView = (RelativeLayout) findViewById(R.id.add_pic);
		addPicView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AddPicMaterial();
			}
		});
		subtitleAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if( mOffLinePlan == false ){
					Intent intent = new Intent(PlanInfoEditActivity.this, TextSelectActivity.class);
					intent.putExtra("start_by", "edit");
					PlanInfoEditActivity.this.startActivityForResult(intent, REQUEST_CODE_ONLINE_TEXT_SELECT);
				}else{
					showInputTitleDialog(false,null,0);
				}
			}
		});
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, screenWidth * 9 / 16);
		params.setMargins(0, 160, 0, 0);
		planVp.setLayoutParams(params);
		addPicView.setLayoutParams(params);
		
		this.setTouchUnDealView(planVp);

		if ( mType == EDIT_PLAN ) {
			this.initTitleBar("编辑内容");
		} else if ( mType == ADD_PLAN ) {		
			this.initTitleBar("新建内容");		
		}else if ( mType == ADD_TEMPLATE_PLAN ) {
			this.initTitleBar("新建模板内容");		
		}else if ( mType == EDIT_TEMPLATE_PLAN) {
			this.initTitleBar("编辑内容");
		}
		
		//更新滚动文字显示
		updateSubtitleView();		
		//更新图片显示
		updatePicView();
		//更新图片上文字显示
		this.updatePreviewText();
		
		PlanInfoEditActivity.this.setTitleNextBtnClickListener(getResources().getString(R.string.menu_finish),
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if( mSubtitleList.size() == 0 && mMaterialList.size() == 0 ){
							PlanInfoEditActivity.this.reportToast("请至少选择一张图片素材或者一条文字素材");
							return;
						}
						mPlanListrowsBean.terminalIDs = "";
						if( mOffLinePlan == true ){
							saveOfflinePlan();
						}else{
							saveOnlinePlan();
						}
						
					}
				});
		
	}
	
	private void saveOfflinePlan(){
		new OfflineReleaseTools(PlanInfoEditActivity.this,false).saveOfflinePlanMaterial(mPlanListrowsBean, mMaterialList, 
				mSubtitleList, new OfflineReleaseTools.OnTaskFinishListener() {
					@Override
					public void onFinished(int errcode) {
						if( mPlanListrowsBean != null && mTemplateDirPath != null ){
							TemplatePkgTool.mvOldDirToNew(mTemplateDirPath,
									TemplatePkgTool.getNormalTmeplateDir(PlanInfoEditActivity.this, mPlanListrowsBean.templateId, mPlanListrowsBean.name));				
						}
						notifyPlanListChanged();
						finish();	
					}
		});	
	}
	
	//生成在线计划
	public void saveOnlinePlan(){
		if (mPlanListrowsBean.groups == null || mPlanListrowsBean.groups.trim().equals("")) {
			reportToast("请选择发布分组");
			return;
		}
		//修改所有广告的时间
		long time = Calendar.getInstance().getTimeInMillis();
		if( mPlanListrowsBean.materialList != null ){
			for( int i = 0 ; i < mPlanListrowsBean.materialList.size(); i++ ){
				mPlanListrowsBean.materialList.get(i).modify = time;
			}
		}
		if( mPlanListrowsBean.subTitleList != null ){
			for( int i = 0 ; i < mPlanListrowsBean.subTitleList.size(); i++ ){
				mPlanListrowsBean.subTitleList.get(i).modify = time;
			}
		}
		JSONObject obj = PlanListBeanParser.getJSONObject(mPlanListrowsBean);
		Log.d(TAG,"json:"+obj.toString());
		if( mType == ADD_TEMPLATE_PLAN || mType == EDIT_TEMPLATE_PLAN ){
			saveOnlineTemplateZipAndPlan(obj.toString());
		}else{
			saveOnlineNormalPlan(obj.toString());
		}
	}
	
	public void saveOnlineNormalPlan(String content){
		final ProgressDialog progressDialog = ProgressDialog.show(this, "","...Loading...");

		String urlString = UrlUtils.getSinglePlanSaveUrl();// +"&startTimeStr="+mStartTimeItem.getInfo()+":00";
		Log.i(TAG, "URL:" + urlString);
		Log.d(TAG, "Content="+content);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("content", content));
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (progressDialog.isShowing()&& progressDialog != null) {
							progressDialog.dismiss();
						}
						try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if (bean.code == 1) {
								if (mPlanListrowsBean != null) {
									PublishStateUtils.removeData(PlanInfoEditActivity.this,getDir("isPublishClass.txt",
													Context.MODE_PRIVATE).getPath(), String.valueOf(mPlanListrowsBean.contractId));
								}
								notifyPlanListChanged();
								finish();
							} else if (bean.code == -2) {
								returnToLogin();
							} else {
								if( bean.msg != null ){
									reportToast(bean.msg);
								}else{
									reportToast("添加班牌实时信息失败");
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
						Toast.makeText(PlanInfoEditActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						if (progressDialog.isShowing()&& progressDialog != null) {
							progressDialog.dismiss();
						}
					}
				});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	//上传模板编辑信息ZIP
	private void saveOnlineTemplateZipAndPlan(String content) {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, File> files = new HashMap<String, File>();
		final File file =  TemplatePkgTool.fileToZip(mTemplateDirPath, mTemplateDirPath, new File(mTemplateDirPath).getName());
		files.put("file", file);
		params.put("content", content);

		final ProgressDialog progressDialog = ProgressDialog.show(this, "","...Loading...");

		String urlString = UrlUtils.getSinglePlanSaveUrl();
		Log.i(TAG, "URL:" + urlString);
		NetWorkRequest.UploadFiles(PlanInfoEditActivity.this,urlString, files, params, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				// TODO Auto-generated method stub
				if (progressDialog.isShowing() && progressDialog != null)
					progressDialog.dismiss();
				Log.d(TAG, response);
				try {
					BaseBean bean = BaseBeanParser.parseBaseBean(response);
					if (bean.code == 1) {
						if (mPlanListrowsBean != null) {
							PublishStateUtils.removeData(PlanInfoEditActivity.this,getDir("isPublishClass.txt",
											Context.MODE_PRIVATE).getPath(), String.valueOf(mPlanListrowsBean.contractId));
						}
						String picPath = mMaterialList.get(0).thumbnail;
						if( picPath != null)
						{
							File picFile=new File(picPath);
							if(picFile.exists())
								picFile.delete();
						}
						file.delete();					
						notifyPlanListChanged();
						finish();
					} else if (bean.code == -2) {
						returnToLogin();
					} else {
						if( bean.msg != null ){
							reportToast(bean.msg);
						}else{
							reportToast("添加内容模板失败");
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
				Toast.makeText(PlanInfoEditActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
				if (progressDialog.isShowing() && progressDialog != null) {
					progressDialog.dismiss();
				}
			}
		}, null);
	}
	
	private void updateGroupTip() {
		if (mPlanListrowsBean.groups != null) {
			String[] groups = mPlanListrowsBean.groups.split(",");
			((SettingItem) findViewById(R.id.groupitem)).setInfo(String.valueOf(groups.length));

		}
	}

	private void initData() {
		Bundle bundle = PlanInfoEditActivity.this.getIntent().getExtras();
		String picPath = null;
		String resId = null;
		String templateId = null;
		if (bundle != null) {
			mPlanListrowsBean = (PlanListrowsBean) bundle.getSerializable(CommonBundleName.DATA_TAG);
			
			//模板原型id
			templateId = bundle.getString(CommonBundleName.TEMPLATE_ID_TAG);
			
			mType = bundle.getInt(PLAN_TYPE);
			
			//模板编辑目录
			mTemplateDirPath = bundle.getString(CommonBundleName.TEMPLATE_DIR_PATH_TAG);
			
			//模板编辑后生成的图片资源id
			resId = bundle.getString(CommonBundleName.RES_ID_TAG);		
			//模板编辑后生成的图片的文件路径
			picPath = bundle.getString(CommonBundleName.PIC_PATH_TAG);
			Log.d(TAG, "picPath="+picPath);
			
			mOffLinePlan = bundle.getBoolean(CommonBundleName.OFFLINE_TAG,true);
		}
		
		if ( mType == EDIT_PLAN ) {//进入计划编辑界面
			mMaterialList = mPlanListrowsBean.materialList;
			mSubtitleList = mPlanListrowsBean.subTitleList;
			if(mMaterialList.size()>0)
				Picasso.with(this).invalidate(mPlanListrowsBean.materialList.get(0).thumbnail);
		}else if ( mType == ADD_PLAN ){//新建计划
			if( mOffLinePlan == true ){//离线计划
				mPlanListrowsBean = PkgTools.makePlanListrowsBean(mOffLinePlan);
				mPlanListrowsBean.materialList = new ArrayList<PlanMaterialrowsBean>();
				mPlanListrowsBean.subTitleList = new ArrayList<PlanMaterialrowsBean>();
			}else{//在线计划
				mPlanListrowsBean = PkgTools.makePlanListrowsBean(mOffLinePlan);
				mPlanListrowsBean.materialList=new ArrayList<PlanMaterialrowsBean>();
				mPlanListrowsBean.subTitleList=new ArrayList<PlanMaterialrowsBean>();
			}
			mMaterialList = mPlanListrowsBean.materialList;
			mSubtitleList = mPlanListrowsBean.subTitleList;
		}else if ( mType == ADD_TEMPLATE_PLAN ){//新建模板内容
			//生成新的计划
			if( mOffLinePlan == true ){//离线计划
				mPlanListrowsBean = PkgTools.makePlanListrowsBean(templateId,mOffLinePlan);					
			}else{//在线计划
				mPlanListrowsBean = PkgTools.makePlanListrowsBean(templateId,mOffLinePlan);	
			}	
			mPlanListrowsBean.templateId = templateId;
			mPlanListrowsBean.materialList = new ArrayList<PlanMaterialrowsBean>();
			mPlanListrowsBean.subTitleList = new ArrayList<PlanMaterialrowsBean>();
			mMaterialList = mPlanListrowsBean.materialList;
			mSubtitleList = mPlanListrowsBean.subTitleList;
			addImageData(picPath,resId);
			Picasso.with(this).invalidate(mPlanListrowsBean.materialList.get(0).thumbnail);			
		}
		else if ( mType == EDIT_TEMPLATE_PLAN ) //编辑模板计划
		{	
			mMaterialList = mPlanListrowsBean.materialList;
			mSubtitleList = mPlanListrowsBean.subTitleList;
			if( picPath != null ){//模板图片有修改
				if( mOffLinePlan == true ){//离线计划
					mMaterialList.get(0).thumbnail="file://"+picPath;
				}else{
					mMaterialList.get(0).thumbnail = picPath;
					mMaterialList.get(0).fileID = Integer.parseInt(resId);
					mMaterialList.get(0).adId = -1;//新增时adId为-１
				}
			}
			Picasso.with(this).invalidate(mPlanListrowsBean.materialList.get(0).thumbnail);
		}
	}
	
	void addImageData(String path,String resId){
		Log.d("plan info edit activty","add image data path:"+path+",resId:"+resId);
		if( path == null ){
			return;
		}
		if( resId == null ){
			if( path != null && path.startsWith("file://") == false ){
				path = "file://"+path;
			}
		}
		PlanMaterialrowsBean imageBean;
		//生成新的图片素材
			imageBean=PkgTools.makeImageAd(path,mOffLinePlan);
		if( resId != null ){
			imageBean.type = PlanMaterialrowsBean.TYPE_PIC;
			imageBean.adId = -1;
			imageBean.fileID = Integer.parseInt(resId);
		}
		mMaterialList.add(imageBean);
	}
	
	void addTextData(String text,String resId){
		//生成新的文字素材
		PlanMaterialrowsBean textBean = PkgTools.makeTextAd(text,mOffLinePlan);
		if( resId != null ){
			textBean.adId = -1;
			textBean.fileID = Integer.parseInt(resId);
		}
		mSubtitleList.add(textBean);
		//更新图片文字显示
		mSelectTextIndex = mSubtitleList.size() - 1;
		this.updatePreviewText();
	}
	
	void updatePicView(){
		ArrayList<String> picUrlList = new ArrayList<String>();
		if (mMaterialList != null && mMaterialList.size() > 0) {
			for (int i = 0; i < mMaterialList.size(); i++) {
				picUrlList.add(mMaterialList.get(i).thumbnail);
			}
		}
		//设置图片编辑界面图片显示
		if( picUrlList.size() > 0 ){
			planVp.setItems(picUrlList, picUrlList.get(mSelectPicIndex),this,PlanPreviewView.TYPE_PLANEDIT);
			planVp.setVisibility(View.VISIBLE);
		}else{
			planVp.setVisibility(View.GONE);
		}
	}
	
	private void startPreviewActivity(int picturePosition,int textPosition){
		Intent intent = new Intent(PlanInfoEditActivity.this, PlanPreviewActivity.class);
		 
	    Bundle bundle = new Bundle();    
	    bundle.putString("startBy", "plan_info_edit");
	    ArrayList<String> picUrlList = new ArrayList<String>();
		if (mMaterialList != null && mMaterialList.size() > 0) {
			for (int i = 0; i < mMaterialList.size(); i++) {
				picUrlList.add(mMaterialList.get(i).thumbnail);
			}
		}if (picUrlList!=null && picUrlList.size()>0) {
			bundle.putStringArrayList(PlanPreviewActivity.INTENTDATA_PICLIST, picUrlList);
		    bundle.putString(PlanPreviewActivity.INTENTDATA_IMAGE_PATH, picUrlList.get(picturePosition));
		}
	    
	    
	    if (mSubtitleList!=null && mSubtitleList.size() > 0) {
	    	if( textPosition > mSubtitleList.size() - 1 ){
	    		textPosition = mSubtitleList.size() - 1;
	    	}
	    	PlanMaterialrowsBean titleBean = mSubtitleList.get(textPosition);
			bundle.putString(PlanPreviewActivity.INTENTDATA_SUBTITLE, titleBean.thumbnail);
			for (PlanMaterialparamsBean param : titleBean.params) {
				bundle.putString(Integer.toString(param.adParamId), param.adParamValue);
			}
		}
	    
	    intent.putExtras(bundle);
	    PlanInfoEditActivity.this.startActivity(intent);
	}
	
	
	private List<String> popupSubtitleMenuItemList = new ArrayList<String>();
	
	private void showSubtitlePopupList(View anchorView,int contextPosition) {
		popupSubtitleMenuItemList.clear();
		popupSubtitleMenuItemList.add(this.getString(R.string.menu_preview)); 
		if( mOffLinePlan == true ){
			popupSubtitleMenuItemList.add(this.getString(R.string.menu_edit)); 
		}
		popupSubtitleMenuItemList.add(getString(R.string.menu_delete)); 
		popupSubtitleMenuItemList.add(getString(R.string.menu_attribute)); 
		popupSubtitleMenuItemList.add(getString(R.string.menu_timesetting)); 
		
		if( contextPosition > 0 ){
			if (mSubtitleList.size()>=3 && contextPosition!=0 && contextPosition!=mSubtitleList.size()-1) {
				popupSubtitleMenuItemList.add(getString(R.string.menu_more));
			}else{
				popupSubtitleMenuItemList.add(getString(R.string.menu_frontmove));
			}
			
		}
		if( mSubtitleList != null && contextPosition < mSubtitleList.size() - 1  ){
			if (mSubtitleList.size()>=3 && contextPosition!=0 && contextPosition!=mSubtitleList.size()-1) {
				
			}else{
				popupSubtitleMenuItemList.add(getString(R.string.menu_backmove)); 	
			}
				
		}	
		
		PopupList popupList = new PopupList(this); 
		popupList.setTextPaddingLeft(popupList.dp2px(12));
		popupList.setTextPaddingRight(popupList.dp2px(12));
		popupList.setTextSize(popupList.sp2px(15));
		popupList.showPopupListWindowAtCenter(anchorView, contextPosition, popupSubtitleMenuItemList, new PopupList.PopupListListener() {
			 @Override
			 public void onPopupListClick(View contextView, int contextPosition, int position) { 
				 mSelectTextIndex = contextPosition;
				 updatePreviewText();
				 String clickStr = popupSubtitleMenuItemList.get(position);
				 if( clickStr.equals(getString(R.string.menu_preview)) ) {//预览		
					 startPreviewActivity(mSelectPicIndex,mSelectTextIndex);
				 }else  if( clickStr.equals(getString(R.string.menu_edit)) ) {//预览		
					 showInputTitleDialog(true,mSubtitleList.get(contextPosition).thumbnail,contextPosition);
				 }else  if( clickStr.equals(getString(R.string.menu_delete)) ) {//删除	
					 onDelSubtitle(contextPosition);
				 }else  if( clickStr.equals(getString(R.string.menu_attribute)) ) {//文字属性设置
					 startTextAttributeSetting(contextPosition);
				 }else  if( clickStr.equals(getString(R.string.menu_timesetting)) ) {//文字时段设置
					 startTimeSetting(false,contextPosition);
				 }else  if( clickStr.equals(getString(R.string.menu_backmove)) ) {//文字属性设置
					 backMove(contextView,contextPosition);
				 }else  if( clickStr.equals(getString(R.string.menu_frontmove)) ) {//文字属性设置
					 frontMove(contextView, contextPosition);
				 }else{//more
					 showMovePopupList(contextView,contextPosition,0);
				 }
			 }
		
			@Override
			public boolean showPopupList(View adapterView, View contextView,
					int contextPosition) {
				// TODO Auto-generated method stub
				return true;
			} 
		});
	}
	
	private void backMove(View contextView, int contextPosition) {
		//滚动文字某一个向后移动
		PlanMaterialrowsBean bean = mSubtitleList.remove(contextPosition);
		mSubtitleList.add(contextPosition+1,bean);
		updateSubtitleView();
		mSelectTextIndex++;
		if( mSelectTextIndex >= mSubtitleList.size() ){
			mSelectTextIndex = mSubtitleList.size() - 1;
		}
	}

	private void frontMove(View contextView, int contextPosition) {
		//滚动文字某一个向前移动
		PlanMaterialrowsBean bean = mSubtitleList.remove(contextPosition);
		mSubtitleList.add(contextPosition-1,bean);
		updateSubtitleView();		
		mSelectTextIndex--;
		if( mSelectTextIndex < 0 ){
			mSelectTextIndex = 0;
		}
	}
	
	public void showPopupList(View v, int position) {
		final List<String> popupPlanMenuItemList = new ArrayList<String>();
		popupPlanMenuItemList.add(this.getString(R.string.menu_preview)); 
		if(  mType != EDIT_TEMPLATE_PLAN &&  mType != ADD_TEMPLATE_PLAN )
		{
			popupPlanMenuItemList.add(getString(R.string.menu_add));
			popupPlanMenuItemList.add(getString(R.string.menu_delete));
		}
		popupPlanMenuItemList.add(getString(R.string.menu_attribute)); 
		popupPlanMenuItemList.add(getString(R.string.menu_timesetting)); 
		if( position > 0 ){
			if (mMaterialList.size()>=3 && position!=0 && position!=mMaterialList.size()-1) {
				popupPlanMenuItemList.add(getString(R.string.menu_more));
			}else{
				popupPlanMenuItemList.add(getString(R.string.menu_frontmove));
			}
			
		}
		if( mMaterialList != null && position < mMaterialList.size() - 1  ){
			if (mMaterialList.size()>=3 && position!=0 && position!=mMaterialList.size()-1) {
				
			}else{
				popupPlanMenuItemList.add(getString(R.string.menu_backmove)); 	
			}
				
		}			
		
		PopupList popupList = new PopupList(this); 
		popupList.setTextPaddingLeft(popupList.dp2px(12));
		popupList.setTextPaddingRight(popupList.dp2px(12));
		popupList.setTextSize(popupList.sp2px(15));
		popupList.showPopupListWindowAtCenter(v, position, popupPlanMenuItemList, new PopupList.PopupListListener() {
			 @Override
			 public void onPopupListClick(View contextView, int contextPosition, int position) { 
				 mSelectPicIndex = contextPosition;
				 String selStr = popupPlanMenuItemList.get(position);
				 if( selStr.equals(getString(R.string.menu_preview))){
					 startPreviewActivity(mSelectPicIndex,mSelectTextIndex);
				 }else if( selStr.equals(getString(R.string.menu_attribute))){
					 startImageAttributeSetting(contextPosition);
				 }else if( selStr.equals(getString(R.string.menu_timesetting))){
					 startTimeSetting(true,contextPosition);
				 }else if( selStr.equals(getString(R.string.menu_backmove))){
					 backPicMove(contextView,contextPosition);
				 }else if( selStr.equals(getString(R.string.menu_frontmove))){
					 frontPicMove(contextView, contextPosition);
				 }else if( selStr.equals(getString(R.string.menu_more))){
					 showMovePopupList(contextView,contextPosition,1);
				 }else if( selStr.equals(getString(R.string.menu_add))){
					 AddPicMaterial();
				 }else if( selStr.equals(getString(R.string.menu_delete))){
					 delPicMaterial(contextPosition);
				 }
			 }	

			@Override
			public boolean showPopupList(View adapterView, View contextView,
					int contextPosition) {
				// TODO Auto-generated method stub
				return true;
			} 
		});
		
	}
	
	private List<String> popupMenuItemList = new ArrayList<String>();
//	private boolean isMoveSubtitle=false;
	
	public void showMovePopupList(View v, int position, final int type) {
		popupMenuItemList.clear();
		popupMenuItemList.add("<"); 
		popupMenuItemList.add("前移"); 
		popupMenuItemList.add("后移");
		
		PopupList popupList = new PopupList(this); 
		popupList.setTextPaddingLeft(popupList.dp2px(12));
		popupList.setTextPaddingRight(popupList.dp2px(12));
		popupList.setTextSize(popupList.sp2px(15));
		popupList.showPopupListWindowAtCenter(v, position, popupMenuItemList, new PopupList.PopupListListener() {
			 @Override
			 public void onPopupListClick(View contextView, int contextPosition, int position) { 
				 switch (position) {
				 case 0:
					 
					 if (type == 0) {
						 PlanInfoEditActivity.this.showSubtitlePopupList(contextView,contextPosition);
						}else if (type == 1) {
					            PlanInfoEditActivity.this.showPopupList(contextView,contextPosition);
						}
				
					break;
					case 1:
						if (type == 0) {
							if (contextPosition==0) {
								backMove(contextView,contextPosition);
							}else {
								frontMove(contextView, contextPosition);
							}
						}else if (type == 1) {
							if (contextPosition==0) {
								backPicMove(contextView,contextPosition);
							}else {
								frontPicMove(contextView, contextPosition);
							}
						}
							
					
						break;
					case 2:
						if (type == 0) {
							
							backMove(contextView,contextPosition);
							
						}else if (type == 1) {
						     backPicMove(contextView,contextPosition);
						}
						break;
					}
			 }
	


			@Override
			public boolean showPopupList(View adapterView, View contextView,
					int contextPosition) {
				// TODO Auto-generated method stub
				return true;
			} 
		});
		
	}
	
	
	private void backPicMove(View contextView, int contextPosition) {
		PlanMaterialrowsBean bean = mMaterialList.remove(contextPosition);
		mMaterialList.add(contextPosition+1,bean);
		mSelectPicIndex++;
		if( mSelectPicIndex >  mMaterialList.size() - 1){
			mSelectPicIndex = mMaterialList.size() - 1;
		}
		updatePicView();
	}

	private void frontPicMove(View contextView, int contextPosition) {
		PlanMaterialrowsBean bean = mMaterialList.remove(contextPosition);
		mMaterialList.add(contextPosition-1,bean);
		mSelectPicIndex--;
		if( mSelectPicIndex < 0 ){
			mSelectPicIndex = 0;
		}
		updatePicView();		
	}

	//删除图片素材
	private void delPicMaterial(int position) {
		if( mMaterialList.size() > position ){
			mMaterialList.remove(position);
			mSelectPicIndex--;
			if( mSelectPicIndex < 0 ){
				mSelectPicIndex = 0;
			}
			updatePicView();
		}
		
	}
	
	//添加图片素材
	private void AddPicMaterial() {
		if( mOffLinePlan == true ){
			Intent intent = null;		
			intent = new Intent(this, OfflinePicSelectActivity.class);
			intent.putExtra("start_by", "edit");
			intent.putExtra("fileType", MaterialParams.TYPE_AD_PICTURE);
			startActivityForResult(intent, REQUEST_CODE_OFFLINE_IMAGE_SELECT);	
		}else{	
			Intent intent = new Intent(this, PictureSelectActivity.class);
			intent.putExtra("start_by", "edit");
			intent.putExtra("fileType", MaterialParams.TYPE_AD_PICTURE);
			startActivityForResult(intent, REQUEST_CODE_ONLINE_IMAGE_SELECT);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("PlanInfoEdit","requestCode:"+requestCode);
		if (requestCode == REQUEST_CODE_IMAGE_ATTR_SETTING) {
			if (resultCode == Activity.RESULT_OK) {
			    boolean modifyAll = data.getBooleanExtra("modifyAll", false);
				
			    if( modifyAll == true ){//修改当前广告所有图片参数
					for (PlanMaterialrowsBean ad : mMaterialList) {
						for (PlanMaterialparamsBean param : ad.params) {
							param.adParamValue = data.getStringExtra(Integer.toString(param.adParamId));
						}
					}
			    }else{
			    	PlanMaterialrowsBean ad = mMaterialList.get(mSelectPicIndex);
			    	for (PlanMaterialparamsBean param : ad.params) {
						param.adParamValue = data.getStringExtra(Integer.toString(param.adParamId));
					}
			    }
//				mOfflinePkgMgr.savePlanMaterial(mPlanMaterialBean, planListrowsBean.name, TYPE_PICS);
			}
		}else if( requestCode == REQUEST_CODE_TEXT_ATTR_SETTING ){
			
			if (resultCode == Activity.RESULT_OK) {
			    boolean modifyAll = data.getBooleanExtra("modifyAll", false);
				
			    if( modifyAll == true ){//修改当前广告所有文字属性
					for (PlanMaterialrowsBean ad : mSubtitleList) {
						for (PlanMaterialparamsBean param : ad.params) {
							param.adParamValue = data.getStringExtra(Integer.toString(param.adParamId));
						}
					}
			    }else{
			    	PlanMaterialrowsBean ad = mSubtitleList.get(mSelectTextIndex);
			    	for (PlanMaterialparamsBean param : ad.params) {
						param.adParamValue = data.getStringExtra(Integer.toString(param.adParamId));
					}
			    }
			    this.updatePreviewText();
			}
		}else if( requestCode == REQUEST_CODE_TEXT_TIME_SETTING ){
			if (resultCode == Activity.RESULT_OK) {
				boolean modifyAll = data.getBooleanExtra("modifyAll", false);
				ArrayList<FindAdPeriodsperiodsBean> periods = data.getParcelableArrayListExtra("periods");
				
			    if( modifyAll == true ){//修改当前广告所有文字属性
					for (PlanMaterialrowsBean ad : mSubtitleList) {
						ad.periods = periods;
					}
			    }else{
			    	PlanMaterialrowsBean ad = mSubtitleList.get(mSelectTextIndex);
			    	ad.periods = periods;
			    }
//				mOfflinePkgMgr.savePlanMaterial(mPlanMaterialBean, planListrowsBean.name, TYPE_TEXT);
			}
		}else if( requestCode == REQUEST_CODE_IMAGE_TIME_SETTING ){
			if (resultCode == Activity.RESULT_OK) {
				boolean modifyAll = data.getBooleanExtra("modifyAll", false);
				ArrayList<FindAdPeriodsperiodsBean> periods = data.getParcelableArrayListExtra("periods");
				
				  if( modifyAll == true ){//修改当前广告所有文字属性
						for (PlanMaterialrowsBean ad : mMaterialList) {
							ad.periods = periods;
						}
				  }else{
					  PlanMaterialrowsBean ad = mMaterialList.get(mSelectPicIndex);
					  ad.periods = periods;
				  }
//				  mOfflinePkgMgr.savePlanMaterial(mPlanMaterialBean, planListrowsBean.name, TYPE_PICS);
			}
		}else if (requestCode == REQUEST_CODE_OFFLINE_IMAGE_SELECT) {//从图片选择界面返回
			if (resultCode == Activity.RESULT_OK) {
				ArrayList<String> selPics = data.getStringArrayListExtra("select_pic_url");
				if (selPics != null && selPics.size() > 0) {
					
					for(String url:selPics){
						addImageData(url,null);
					}	
					mSelectPicIndex = mMaterialList.size() - 1;
					if( mSelectPicIndex < 0 ){
						mSelectPicIndex = 0;
					}
					updatePicView();
				}		
				
			}
		}else if (requestCode == REQUEST_CODE_ONLINE_IMAGE_SELECT) {//从在线图片选择界面返回
			if (resultCode == Activity.RESULT_OK) {
				ArrayList<String> selPics = data.getStringArrayListExtra("select_pic_url");
				ArrayList<String> selIds = data.getStringArrayListExtra("select_pic_param");
				if (selPics != null && selPics.size() > 0) {
					
					for(int i = 0; i< selPics.size(); i++ ){
						addImageData(selPics.get(i),selIds.get(i));
					}	
					mSelectPicIndex = mMaterialList.size() - 1;
					if( mSelectPicIndex < 0 ){
						mSelectPicIndex = 0;
					}
					updatePicView();
				}		
				
			}
		}else if (requestCode == REQUEST_CODE_ONLINE_TEXT_SELECT) {//从在线图片选择界面返回
			if (resultCode == Activity.RESULT_OK) {
				ArrayList<String> selTextIds = data.getStringArrayListExtra("select_pic_param");
				ArrayList<String> selTextContents = data.getStringArrayListExtra("select_text_contents");
				if (selTextIds != null && selTextIds.size() > 0) {
					
					for(int i = 0; i< selTextIds.size(); i++ ){
						addTextData(selTextContents.get(i),selTextIds.get(i));
					}	
					updateSubtitleView();
				}		
				
			}
		}else if( requestCode == REQUEST_CODE_GROUP_SELECT ){
			if (resultCode == Activity.RESULT_OK) {
				mPlanListrowsBean.groups = data.getStringExtra("groupids");
				updateGroupTip();	
			}
		}
	}
	
		
    public static final int REQUEST_CODE_IMAGE_ATTR_SETTING = 0;
    public static final int REQUEST_CODE_TEXT_ATTR_SETTING = 1;
    public static final int REQUEST_CODE_TEXT_TIME_SETTING = 2;
    public static final int REQUEST_CODE_IMAGE_TIME_SETTING = 3;
    public static final int REQUEST_CODE_OFFLINE_IMAGE_SELECT = 4;
    public static final int REQUEST_CODE_ONLINE_IMAGE_SELECT = 5;
    public static final int REQUEST_CODE_ONLINE_TEXT_SELECT = 6;
    public static final int REQUEST_CODE_GROUP_SELECT = 7;
    
    private int mSelectPicIndex = 0;
    private int mSelectTextIndex = 0;
	private RelativeLayout addPicView;
    
    private void startTimeSetting(boolean pic,int index) {
		Intent intent = new Intent(this, TimeSettingActivity.class);
		intent.putExtra("offline_mode", true);
		intent.putExtra(CommonBundleName.OFFLINE_TAG, mOffLinePlan);
		if( pic == false ){
			intent.putParcelableArrayListExtra("periods", mSubtitleList.get(index).periods);
		}else{
			intent.putParcelableArrayListExtra("periods", mMaterialList.get(index).periods);
		}
		intent.putExtra("is_pic", pic);
		if( pic == true ){
			startActivityForResult(intent, REQUEST_CODE_IMAGE_TIME_SETTING);
		}else{
			startActivityForResult(intent, REQUEST_CODE_TEXT_TIME_SETTING);
		}
	}
	
	private void startImageAttributeSetting(int index) {
		Intent intent = null;
		intent = new Intent(this, ImageAttributeSettingActivity.class);
		PlanMaterialrowsBean ad = mMaterialList.get(index);
		for (PlanMaterialparamsBean param : ad.params) {
			intent.putExtra(Integer.toString(param.adParamId), param.adParamValue);
		}
		startActivityForResult(intent, REQUEST_CODE_IMAGE_ATTR_SETTING);
	}
	
	private void startTextAttributeSetting(int index) {
		Intent intent = new Intent(this, TextAttributeSettingActivity.class);
		PlanMaterialrowsBean ad = mSubtitleList.get(index);
		for (PlanMaterialparamsBean param : ad.params) {
			intent.putExtra(Integer.toString(param.adParamId), param.adParamValue);
		}
		startActivityForResult(intent, REQUEST_CODE_TEXT_ATTR_SETTING);
	}	

	private void showInputTitleDialog(final boolean editText,String text,final int contextPosition) {
        final EditText inputServer = new EditText(this);
        inputServer.setFocusable(true);
        if( text != null ){
        	inputServer.setText(text);
        }
        inputServer.setPadding(10, 50, 10, 30);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
		builder.setView(inputServer);
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

			

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text=inputServer.getText().toString();
				text=text.replaceAll("\r|\n", "");
				if (text.trim().length() == 0 ) {
					reportToast("输入文字不能为空！");
					hideSoftInput(inputServer);
				}else{
					editSubtitle.setVisibility(View.VISIBLE);
					if( editText == false ){//增加滚动文字
						addTextData(text,null);
					}else{//编辑滚动文字
						editTextData(text,contextPosition);
					}
					updateSubtitleView();
					hideSoftInput(inputServer);
					dialog.dismiss();
				}
			}	
		});
		builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				hideSoftInput(inputServer);
				
				dialog.dismiss();
			}
			
			
		});
		
		final AlertDialog dlg = builder.create();
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View view=layoutInflater.inflate(R.layout.dialog_title_view, null);
		TextView dialogTitle=(TextView)view.findViewById(R.id.dialog_title);
		dialogTitle.setText("编辑滚动字幕:");
		dialogTitle.setTextSize(20);
		dlg.setCustomTitle(view);
		dlg.setOnShowListener(new OnShowListener() {
			   private Button neutralBtn ;
			   private Button positiveBtn;
			   @Override
			   public void onShow(DialogInterface dialogInterface) {
			    //设置button文本大小
			    positiveBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
			    neutralBtn = dlg.getButton(DialogInterface.BUTTON_NEUTRAL);
			    positiveBtn.setTextSize(18);
			    neutralBtn.setTextSize(18);
			    dlg.getActionBar();
			   }
			  });
		dlg.show();
    } 

	private void hideSoftInput(EditText inputServer) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);    
        imm.hideSoftInputFromWindow(inputServer.getWindowToken(), 0) ;
		
	}
	//删除滚动字幕
	private void onDelSubtitle(int position) {
		if( mSubtitleList.size() >= position ){
			mSubtitleList.remove(position);
		}
		updateSubtitleView();
		mSelectTextIndex--;
		if( mSelectTextIndex < 0 ){
			mSelectTextIndex = 0;
		}
		this.updatePreviewText();
	}
	
	//更新滚动字幕列表
	private void updateSubtitleView() {		
		editSubtitle.removeAllViews();	
		if( mSubtitleList == null ){
			return;
		}
		for ( int i = 0; i < mSubtitleList.size(); i++) {
			item = new PlanSubtitleView(PlanInfoEditActivity.this);
			TextView subtitleText = (TextView) item.findViewById(R.id.subtitle_text);
			subtitleText.setText(mSubtitleList.get(i).thumbnail);
			subtitleText.setTextSize(18);
			item.setId(i);
			editSubtitle.addView(item);
			item.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showSubtitlePopupList(v,v.getId() );

				}
			});
		}
	}
	
	private void updatePreviewText(){
		if( mSubtitleList == null ){
			return;
		}
		PlanMaterialrowsBean showTextBean = null;
		if( mSubtitleList.size() > 0 ){
			if( mSubtitleList.size() > mSelectTextIndex ){
				showTextBean = this.mSubtitleList.get(mSelectTextIndex);
			}else{
				showTextBean = this.mSubtitleList.get(0);
			}
		}
		if( showTextBean == null ){
			planVp.setText(null, false, null, null, null, 16);
		}else{
			String textcolor = "#042398";
			String textBgColor = "#ffffff";
			String textBgAlpha = "80";
			for (PlanMaterialparamsBean param : showTextBean.params) {
				if( param.adParamId == AdParams.TEXT_BG_COLOR ){
					textBgColor = param.adParamValue;
				}else if( param.adParamId == AdParams.TEXT_COLOR ){
					textcolor = param.adParamValue;
				}else if( param.adParamId == AdParams.TEXT_BG_ALPHA ){
					textBgAlpha = param.adParamValue;
				}				
			}
			planVp.setText(showTextBean.thumbnail, false, textcolor, textBgColor, textBgAlpha, 16);
		}
	}

	private void notifyPlanListChanged() {
		Intent intent = new Intent(PlanListFragment.ACTION_PLAN_LIST_CHANGED);
		sendBroadcast(intent);
	}

	@Override
	public void onPreviewClickListener(View v, int position) {
		// TODO Auto-generated method stub
		this.showPopupList(v, position);
	}
	
	private void editTextData(String text, int contextPosition) {
		
		        mSubtitleList.remove(contextPosition);
				PlanMaterialrowsBean textBean = PkgTools.makeTextAd(text,mOffLinePlan);
				mSubtitleList.add(contextPosition,textBean);
				//更新图片文字显示
				mSelectTextIndex = mSubtitleList.size() - 1;
				this.updatePreviewText();
	}
}
