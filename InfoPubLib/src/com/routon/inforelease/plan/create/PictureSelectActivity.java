package com.routon.inforelease.plan.create;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.classinfo.ClassInfoEditActivity;
import com.routon.inforelease.classinfo.ClassPictureEditActivity;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.MaterialParams;
import com.routon.inforelease.plan.create.PictureListAdapter.OnImageClickedListener;
import com.routon.inforelease.plan.create.pictureAdd.PictureAddActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.routon.widgets.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

//在线图片选择界面
public class PictureSelectActivity extends CustomTitleActivity{
	private static final String TAG = "PictureSelectFragment";
	private PullToRefreshGridView picsGridView;
	private Button minePicsBtn;
	private Button otherPicsBtn;
	private PictureListAdapter picturesAdapter = null;
	
	private ArrayList<MaterialItem> allMaterialDatas = new ArrayList<MaterialItem>();
	private ArrayList<MaterialItem> myMaterialDatas = new ArrayList<MaterialItem>();
	
	private int materialType;//0:mine 1:all
	private int currentAllPage = -1;
	private int currentMyPage = -1;

	
	private int defaultPageSize = 20;
	private int picFileType = MaterialParams.TYPE_AD_PICTURE;
	private int classInfoType = 1;
	private int ON_TEXT_SELECT_ACTIVITY_FINISH = 0;
	private int ON_PICTURE_ADD_FINISH = 1;
	private int ON_GROUP_SELECT_ACTIVITY_FINISH = 2;
	
	private RadioGroup switchGroup;
	
	private String mStartBy;
	
	//默认可选择最多图片数目，为0则不限制图片数目
	//add　by xiaolp 20171117
	private int mMaxSelPicNum = 0 ;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pictures_fragment);
		
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mStartBy = bundle.getString("start_by");
			picFileType = bundle.getInt(CommonBundleName.FILE_TYPE_TAG, MaterialParams.TYPE_AD_PICTURE);
			classInfoType = bundle.getInt(CommonBundleName.CLASSINFO_TYPE_TAG, 1);
			mMaxSelPicNum = bundle.getInt(CommonBundleName.MAX_SEL_PIC_NUM, 0);		
		}
		
		initTitleBar(R.string.pictures_select_title);

        setTitleNextBtnClickListener(this.getResources().getString(R.string.menu_next_step),new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int len = getSelectMaterialList().size();
				if( mMaxSelPicNum > 0 ){
					if( len > mMaxSelPicNum ){
						reportToast("选择图片不能超过"+mMaxSelPicNum+"张");
						return;
					}
				}
				if (mStartBy != null) {					
					ArrayList<String> paramStr = new ArrayList<String>();
					ArrayList<String> urls = new ArrayList<String>();
					//模板species属性
					ArrayList<Integer> speciesArrayList = new ArrayList<Integer>();
					
					for(int i = 0; i< len; i++){
						MaterialItem item = getSelectMaterialList().get(i);	
						paramStr.add(Integer.toString(item.getId()));
						urls.add(item.getContent());
						speciesArrayList.add(Integer.valueOf(item.getSpecies()));
					}
					//新建班牌流程（普通班牌和模板班牌）
					//信息发布使用模板新建广告流程
					if (mStartBy.equals("class_info_add")) {
						//使用模板,选择图片
						if (picFileType == MaterialParams.TYPE_CLASS_TEMPLATE) {
							if (urls.size() < 1) {
								reportToast("请选择一个模板");
								return;
							}
							startTemplatePictureEdit(urls.get(0),speciesArrayList.get(0).intValue(),paramStr.get(0));
							return;
						}
						
						//新建班牌流程
						//新增内容，选择图片
						if (urls.size() == 0) {
							reportToast("请至少选择一张图片");
							return;
						}
						Intent data = new Intent(PictureSelectActivity.this, ClassInfoEditActivity.class);
						data.putStringArrayListExtra(CommonBundleName.SELECT_PIC_PARAM_TAG, paramStr);
						data.putStringArrayListExtra(CommonBundleName.SELECT_PIC_URL_TAG, urls);
						data.putExtra(CommonBundleName.CLASSINFO_TYPE_TAG, classInfoType);
						startActivity(data);
						finish();
					} else if (mStartBy.equals("edit")) {
						Intent data = new Intent();
						data.putStringArrayListExtra(CommonBundleName.SELECT_PIC_PARAM_TAG, paramStr);
						data.putStringArrayListExtra(CommonBundleName.SELECT_PIC_URL_TAG, urls);
						if (len >0) {
							data.putExtra("isChange", true);
						}
						setResult(Activity.RESULT_OK, data);
						finish();						
					}
				} else {
					String paramStr = new String();
					for(int i = 0; i< len; i++){
						MaterialItem item = getSelectMaterialList().get(i);

						paramStr += "&";
						paramStr += "resIds="+Integer.toString(item.getId());
					}
					
					if(InfoReleaseApplication.getClassInfoPrivilege()){
						addNewPlan();
					}else{
						Intent intent = new Intent(PictureSelectActivity.this, TextSelectActivity.class);
						intent.putExtra("select_pic_param", paramStr);
						PictureSelectActivity.this.startActivityForResult(intent, ON_TEXT_SELECT_ACTIVITY_FINISH);
					}
				}
			}
		});

    	currentAllPage = -1;
    	currentMyPage = -1;
        initView();
        
        if (picFileType == MaterialParams.TYPE_CLASS_PICTURE) {
        	switchGroup.setEnabled(false);
        }
	}
	
	private void startTemplatePictureEdit(String templateUrl,int species,String templateId){
		Intent data = new Intent(PictureSelectActivity.this, ClassPictureEditActivity.class);
		data.putExtra(CommonBundleName.TEMPLATE_URL_TAG, templateUrl);
		data.putExtra(CommonBundleName.OFFLINE_TAG, 
				PictureSelectActivity.this.getIntent().getBooleanExtra(CommonBundleName.OFFLINE_TAG, false));
		//将species属性保存到下一级
		data.putExtra(CommonBundleName.SPECIES_TAG,species);
		//模板原型ID
		data.putExtra(CommonBundleName.TEMPLATE_ID_TAG, templateId);
		startActivity(data);
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == ON_TEXT_SELECT_ACTIVITY_FINISH){
			finish();
		}
		
		if(requestCode == ON_PICTURE_ADD_FINISH){
			
			reUpdateMaterialsList();
		}
		
		if(requestCode == ON_GROUP_SELECT_ACTIVITY_FINISH){
			finish();
		}
	}
	
	private void reUpdateMaterialsList(){
		if(materialType == 0){
			currentMyPage = 1;
			currentAllPage = -1;
			myMaterialDatas.clear();
			getPictureMaterials(0, currentMyPage,true);
			switchGroup.setBackgroundResource(R.drawable.switch_l);
			minePicsBtn.setTextColor(Color.WHITE);
			otherPicsBtn.setTextColor(Color.rgb(44, 145, 225));
		}else{
			currentAllPage = 1;
			currentMyPage = -1;
			allMaterialDatas.clear();
			getPictureMaterials(1, currentAllPage,true);
			switchGroup.setBackgroundResource(R.drawable.switch_r);
			otherPicsBtn.setTextColor(Color.WHITE);
			minePicsBtn.setTextColor(Color.rgb(44, 145, 225));
		}
	}

	private void initView(){
		picturesAdapter = new PictureListAdapter(this, allMaterialDatas);
		picsGridView = (PullToRefreshGridView) findViewById(R.id.pics_grid_view);
		setPushRefreshListener();
		setImageClickListener();
		
//		if (mStartBy != null && mStartBy.equals("edit")) {
//			setTitleNextButtonImageResource(R.drawable.ok);
//		}
		if (mStartBy!=null&&mStartBy.equals("class_info_add")&&picFileType==MaterialParams.TYPE_CLASS_TEMPLATE) {
			RelativeLayout bottom=(RelativeLayout) findViewById(R.id.material_select_bottom);
			bottom.setVisibility(View.INVISIBLE);
			
			RelativeLayout.LayoutParams linearParams=(RelativeLayout.LayoutParams)picsGridView.getLayoutParams();
			linearParams.bottomMargin=0;
			picsGridView.setLayoutParams(linearParams);
		}
		
		minePicsBtn = (RadioButton) findViewById(R.id.mine_pics);
		otherPicsBtn = (RadioButton) findViewById(R.id.other_pics);
		
		Button addBtn = (Button) findViewById(R.id.add);
		
		addBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 Intent intent = new Intent(PictureSelectActivity.this, PictureAddActivity.class);
				 intent.putExtra(CommonBundleName.FILE_TYPE_TAG, picFileType);
				 PictureSelectActivity.this.startActivityForResult(intent, ON_PICTURE_ADD_FINISH);
			}
		});
		
		Button delBtn = (Button) findViewById(R.id.del);
		delBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				delSelectMaterials();				
			}
		});
		
		switchGroup = (RadioGroup) findViewById(R.id.switch_group);
		switchGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if( checkedId == R.id.mine_pics ){
					Log.d(TAG, "checkedId:  R.id.mine_pics");

					materialType = 0;
					if(currentMyPage == -1){
						currentMyPage = 1;
						myMaterialDatas.clear();
						getPictureMaterials(0, currentMyPage,true);
					}else{
						picturesAdapter.setDatas(myMaterialDatas);
						picsGridView.setAdapter(picturesAdapter);
					}
					switchGroup.setBackgroundResource(R.drawable.switch_l);
					minePicsBtn.setTextColor(Color.WHITE);
					otherPicsBtn.setTextColor(Color.rgb(44, 145, 225));
				}else if( checkedId == R.id.other_pics ){
					Log.d(TAG, "checkedId:  R.id.other_pics");

					materialType = 1;
					if(currentAllPage == -1){
						currentAllPage = 1;
						allMaterialDatas.clear();
						getPictureMaterials(1, currentAllPage,true);
					}else{

						picturesAdapter.setDatas(allMaterialDatas);
						picsGridView.setAdapter(picturesAdapter);
					}
					switchGroup.setBackgroundResource(R.drawable.switch_r);
					otherPicsBtn.setTextColor(Color.WHITE);
					minePicsBtn.setTextColor(Color.rgb(44, 145, 225));
				}
				
			}
		});
		if (picFileType==MaterialParams.TYPE_CLASS_TEMPLATE) {
			switchGroup.check(R.id.other_pics);
		}else {
			switchGroup.setVisibility(View.INVISIBLE);
			switchGroup.check(R.id.mine_pics);
		}
		
	}

	private void delSelectMaterials(){
		if( mBusyState == true ){
			return;
		}
		String paramStr = new String();
		int len = getSelectMaterialList().size();
		if(len == 0){
			Toast.makeText(PictureSelectActivity.this, R.string.del_materials_1, Toast.LENGTH_LONG).show();
			return;
		}
		
		for(int i = 0; i< len; i++){
			if(i == 0){
				paramStr += "resIds=";
			}
			if(i != 0){
				paramStr += ",";
			}
			MaterialItem item = getSelectMaterialList().get(i);
			paramStr += Integer.toString(item.getId());
		}
		
		String urlString = UrlUtils.getDelMaterialUrl();
    	
		urlString += "?"+paramStr;
		Log.i(TAG, "URL:" + urlString);
  
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "...Loading...");  
        mBusyState = true;
		
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.POST, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                    	mBusyState = false; 
                        Log.d(TAG, "response="+response);  
                        if (progressDialog!=null && progressDialog.isShowing()) {  
                            progressDialog.dismiss();  
                        }
                        
						int code = response.optInt("code");
						if( code == 0){
							Toast.makeText(PictureSelectActivity.this, R.string.del_materials_susccess, Toast.LENGTH_LONG).show();

							clearSelectMaterialList();
							reUpdateMaterialsList();
						}else if(code == -2){
							InfoReleaseApplication.returnToLogin(PictureSelectActivity.this);
						}else{//失败

							clearSelectMaterialList();
							reUpdateMaterialsList();
							Log.e(TAG, response.optString("msg"));  
							Toast.makeText(PictureSelectActivity.this, response.optString("msg"), Toast.LENGTH_LONG).show();
//	                        	
						}
                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	mBusyState = false;
                    	Toast.makeText(PictureSelectActivity.this, "连接平台超时!", Toast.LENGTH_LONG).show();
                    	if (progressDialog!=null && progressDialog.isShowing()) {  
                            progressDialog.dismiss();  
                        }  
                    }  
                });  
        
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	private void setPushRefreshListener(){
		picsGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<GridView> refreshView) {
				Log.i(TAG, "------onPullDownToRefresh-------");
				
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<GridView> refreshView) {

				if(materialType == 0){
					getPictureMaterials(0, ++currentMyPage,false);
				}
				
				if(materialType == 1){
					getPictureMaterials(1, ++currentAllPage,false);
				}
			}
		});
	}
	private void setImageClickListener(){
		picturesAdapter.setListener(new OnImageClickedListener() {
			
			@Override
			public void onImageClicked(int position) {
				// TODO Auto-generated method stub
				MaterialItem item = null;
				if(materialType == 0){
					item = myMaterialDatas.get(position);
				}
				
				if(materialType == 1){
					item = allMaterialDatas.get(position);
				}

				if(item != null){
					String name = item.getContent(); 
//					Toast.makeText(PictureSelectActivity.this, name, Toast.LENGTH_LONG).show();
					Intent previewIntent = new Intent(PictureSelectActivity.this, PicPreviewActivity.class);
					previewIntent.putExtra("path", name);
					startActivity(previewIntent);
				}
			}
		});
	}
	
	private void updateGridView(final int type){
		switch(type){
		case 0://mine
			picturesAdapter.setDatas(myMaterialDatas);
			picturesAdapter.notifyDataSetChanged();	
			break;
		case 1://other
			picturesAdapter.setDatas(allMaterialDatas);
			picturesAdapter.notifyDataSetChanged();	
			break;
		}
	}
	
private void addNewPlan(){	
		
		if(getSelectMaterialList().size() == 0){
			Toast.makeText(this, "没有选中任何素材!", Toast.LENGTH_LONG).show();
			return;
		}
		
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		final String name = df.format(new Date());//+"_mobile";
		
		String paramStr = new String();
		paramStr += "name="+name+"_mobile";
		
		int len = getSelectMaterialList().size();
		for(int i = 0; i< len; i++){
			MaterialItem item = getSelectMaterialList().get(i);

			paramStr += "&";
			paramStr += "resIds="+Integer.toString(item.getId());
		}
		
		String urlString = UrlUtils.getPlanAddUrl();
    	
		urlString += "?"+paramStr;
		Log.i(TAG, "URL:" + urlString);
  
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "...Loading...");  
		
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.POST, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                        if (progressDialog!=null && progressDialog.isShowing()) {  
                            progressDialog.dismiss();  
                        }
                        
						int code = response.optInt("code");
						if( code == 1){
							
							int currentPlanId = response.optInt("id");
							
							Intent intent = new Intent(PictureSelectActivity.this, GroupSelectActivity.class);
							intent.putExtra("plan_id", currentPlanId);
							intent.putExtra("plan_name", name);
							intent.putExtra("startBy", "textSelect");
							PictureSelectActivity.this.startActivityForResult(intent, ON_GROUP_SELECT_ACTIVITY_FINISH);
							
						}else if( code == -2){
							InfoReleaseApplication.returnToLogin(PictureSelectActivity.this);
						}else{//失败
							Log.e(TAG, response.optString("msg"));  
							Toast.makeText(PictureSelectActivity.this, response.optString("msg"), Toast.LENGTH_LONG).show();
//	                        	
						}
                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(PictureSelectActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
                    	if (progressDialog!=null && progressDialog.isShowing()) {  
                            progressDialog.dismiss();  
                        }  
                    }  
                });  
        
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
    }

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	public void getPictureMaterials(int type, int page, boolean isShowDialog ){
		materialType = type;
		String lastResId = null;
		switch(type){
		case 0://mine
			if(myMaterialDatas.size() != 0){
				MaterialItem item = myMaterialDatas.get(myMaterialDatas.size()-1);
				lastResId = Integer.toString(item.getId());
			}
			
			getMaterialLists(page, defaultPageSize, picFileType, "my", lastResId,isShowDialog);
			break;
		case 1:
			if(allMaterialDatas.size() != 0){
				MaterialItem item = allMaterialDatas.get(allMaterialDatas.size()-1);
				lastResId = Integer.toString(item.getId());
			}
			getMaterialLists(page, defaultPageSize, picFileType, "all", lastResId,isShowDialog);
			break;
		}
		
	}
	private ProgressDialog LoadProgressDialog ;
	//获得模板图片
    private void getMaterialLists(final int page, int pageSize, int FileType, String flag, String flagId,boolean isShowDialog) {  
		if(pageSize == -1)
			pageSize = 10;
    	
		String urlString = UrlUtils.getResourceListUrl(page, pageSize, FileType, flag, flagId, null, null, null);
		Log.d(TAG, "URL:" + urlString);
		
		
		if (isShowDialog) {
			LoadProgressDialog = ProgressDialog.show(PictureSelectActivity.this, "", "...Loading...");  
		}else {
			LoadProgressDialog=new ProgressDialog(this);
		}
  
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.POST, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                        if (LoadProgressDialog!=null && LoadProgressDialog.isShowing()) {  
                        	LoadProgressDialog.dismiss();  
                        }
//                        JSONObject infoObject;
						int code = response.optInt("code");
						if( code == 0){ //返回成功
							MaterialRequestResult requestResult = new MaterialRequestResult();
							requestResult.info.code = response.optInt("code");
							requestResult.info.msg = response.optString("msg");
							requestResult.info.fullListSize = response.optInt("fullListSize");
							requestResult.info.page = response.optInt("page");
							requestResult.info.pageSize = response.optInt("pageSize");
								                        	
							JSONArray jsonArray = response.optJSONArray("datas");
							if( jsonArray == null || jsonArray.length() == 0){
						    	
						    	picsGridView.onRefreshComplete();
						    	
						    	if(page == 1){
						        	updateGridView(materialType);
						    	}else{
						    		if(materialType == 0){
						            	currentMyPage--;
						        	}else{
						        		currentAllPage--;
						        	}
						    		Toast.makeText(PictureSelectActivity.this, "没有了!", Toast.LENGTH_LONG).show();
						    	}
						    	return;
							}
							for(int i = 0; i< jsonArray.length(); i++){
								JSONObject obj = jsonArray.optJSONObject(i);
								if( obj == null )
									continue;
								MaterialItem item = new MaterialItem();
								item.setId(obj.optInt("resid"));
								item.setType(obj.optInt("filetypeid"));
								item.setContent(obj.optString("content"));
								item.setCreatetime(obj.optString("createtime"));
								item.setSpecies(obj.optInt("species"));
								if(materialType == 0){
									myMaterialDatas.add(item);
						    	}else{
						    		allMaterialDatas.add(item);
						    	}
								
								Log.d(TAG, "resid:"+item.getId()+"  filetype:"+item.getType() +
										"  content:"+item.getContent()+" createtime:"+item.getCreatetime()+" species:"+item.getSpecies());
							}
							
							updateGridView(materialType);
						
						}else if( code == -2){
							InfoReleaseApplication.returnToLogin(PictureSelectActivity.this);
						}else{//失败
							Log.e(TAG, response.optString("msg"));  
							Toast.makeText(PictureSelectActivity.this, response.optString("msg"), Toast.LENGTH_LONG).show();
							
						}

						picsGridView.onRefreshComplete();
                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(PictureSelectActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
                    	if (LoadProgressDialog!=null && LoadProgressDialog.isShowing()) {  
                    		LoadProgressDialog.dismiss();  
                        }  

                    	picsGridView.onRefreshComplete();
                    }  
                });  
        
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
    }  
	
    public ArrayList<MaterialItem> getSelectMaterialList(){
    	
    	return picturesAdapter.selectMaterails;
    }
    
    private void clearSelectMaterialList(){
    	picturesAdapter.selectMaterails.clear();
    }
}