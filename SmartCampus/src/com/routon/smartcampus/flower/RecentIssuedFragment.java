package com.routon.smartcampus.flower;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.routon.common.BaseFragment;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.pictureAdd.PictureAddActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.edurelease.R;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.TakePhotoPopWin;
import com.routon.widgets.Toast;

public class RecentIssuedFragment extends BaseFragment{
	private static final String TAG = "RecentIssuedFragment";

	private RecentIssuedListAdapter mAdapter;
	private PullToRefreshListView mListView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_recent_issued, container, false);
		mListView = (PullToRefreshListView) view.findViewById(R.id.list);
		return view;
	}

	private int mAddPosition = 0;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getTeachIssuedBadgeInfo(InfoReleaseApplication.authenobjData.userId,1,true);
		mListView.setMode(PullToRefreshBase.Mode.BOTH);
		mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				getTeachIssuedBadgeInfo(InfoReleaseApplication.authenobjData.userId,1,true);
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				
				getTeachIssuedBadgeInfo(InfoReleaseApplication.authenobjData.userId,m_iPage+1,false);
			}
		});
		initListView();
	}
	
	private void initListView(){	
		mAdapter = new RecentIssuedListAdapter(this.getActivity(), mStudentBadges);
		mAdapter.setOnClickListener(new RecentIssuedListAdapter.onClickListener() {
			
			@Override
			public void onClick(int position, int type) {
				// TODO Auto-generated method stub
				if( type == RecentIssuedListAdapter.onClickListener.ADD_TYPE ){
					mAddPosition = position;
					Intent intent = new Intent();
					intent.setClass(getOwnActivity(), BadgeRemarkActivity.class);
					intent.putExtra(MyBundleName.STUDENT_BADGE_INFO, mStudentBadges.get(position));
					getOwnActivity().startActivityForResult(intent,BADGE_REMARK_REQUEST_CODE);
				}else if( type == RecentIssuedListAdapter.onClickListener.CANCEL_TYPE ){
					showCancelDialog(position);
				}
			}
		});
		mListView.setAdapter(mAdapter);
	}
	
	private ArrayList<StudentBadge> mStudentBadges = new ArrayList<StudentBadge>();
	ProgressDialog progressDialog;
	private int m_iPage;
	private void getTeachIssuedBadgeInfo(int teacherUserId,int page,boolean showWait){
		int pageSize = 10;
		int flagId = 0;
		if( page > 1 ){
			if(mStudentBadges.size()!=0){
				flagId = mStudentBadges.get(mStudentBadges.size()-1).id;
			}else{
				flagId=0;
				page=1;
			}
			
		}
		String urlString = SmartCampusUrlUtils.getTeacherIssuedBadgeDetailListUrl(teacherUserId,page,pageSize,flagId);
		if( showWait == true ){
			progressDialog = ProgressDialog.show(getOwnActivity(), "", "...正在获取数据...");
		}
		m_iPage = page;
		Log.d(TAG, "getTeachIssuedBadgeInfo urlString="+urlString);  
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
	                Request.Method.POST, urlString, null, new Response.Listener<JSONObject>() {  
	                    @Override  
	                    public void onResponse(JSONObject response) {  
	                        Log.d(TAG, "response="+response);  
	                        if (progressDialog!=null && progressDialog.isShowing()) {  
	                        	progressDialog.dismiss();  
	                        }
	                        mListView.onRefreshComplete();
	                        int code = response.optInt("code"); 
	                        Log.d(TAG,"getTeachIssuedBadgeInfo m_iPage:"+m_iPage);
	                        if( m_iPage == 1 ){
	                        	mStudentBadges.clear();
	                        }
					
							if( code  == 0){
								JSONArray array = response.optJSONArray("datas");
								if( array != null ){
									int len = array.length();
									for(int i = 0; i< len; i++){
										JSONObject obj = (JSONObject) array.opt(i);
										if( obj != null ){
											StudentBadge studentB = new StudentBadge(obj);		
											mStudentBadges.add(studentB);
										}
									}
									mAdapter.notifyDataSetChanged();
//									updateView();
								}
								
	                        }else if(code == -2){
	                        	Toast.makeText(getOwnActivity(), response.optString("msg"), Toast.LENGTH_LONG).show();
	                        	InfoReleaseApplication.returnToLogin(getOwnActivity());
	                        }else{//失败
	                        	Log.e(TAG, response.optString("msg"));  
	                        	Toast.makeText(getOwnActivity(), response.optString("msg"), Toast.LENGTH_LONG).show();
	                        }
	                    }  
	                },   
	                new Response.ErrorListener() {  
	                    @Override  
	                    public void onErrorResponse(VolleyError arg0) {  
	                    	Log.e(TAG, "sorry,Error"); 
	                    	if( true == InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) ){
	                    		Toast.makeText(getOwnActivity(), "获取数据失败!", Toast.LENGTH_LONG).show();
	                    	}
	                    	if (progressDialog!=null && progressDialog.isShowing()) {  
	                    		progressDialog.dismiss();  
	                        } 
	                    	if( mListView != null ){
	                    		mListView.onRefreshComplete();
	                    	}
	                    }  
	                });  
	        
	        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
	        InfoReleaseApplication.requestQueue.add(jsonObjectRequest); 		
	}
	
	private void cancelBadge(final int position){
		StudentBadge studentbadge = mStudentBadges.get(position);
		int id = studentbadge.id;
		String urlString = SmartCampusUrlUtils.getBadgeUndoUrl(null,null,String.valueOf(id));
		Log.d(TAG,"cancelBadge id:"+id);
	
		progressDialog = ProgressDialog.show(getOwnActivity(), "", null);
		Log.d(TAG, "cancelBadge urlString="+urlString);  
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
	                Request.Method.POST, urlString, null, new Response.Listener<JSONObject>() {  
	                    @Override  
	                    public void onResponse(JSONObject response) {  
	                        Log.d(TAG, "response="+response);  
	                        if (progressDialog!=null && progressDialog.isShowing()) {  
	                        	progressDialog.dismiss();  
	                        }	   
	                        int code = response.optInt("code");                    
					
							if( code  == 0){
								mStudentBadges.remove(position);
								mAdapter.notifyDataSetChanged();								
	                        }else if(code == -2){
	                        	Toast.makeText(getOwnActivity(), response.optString("msg"), Toast.LENGTH_LONG).show();
	                        	InfoReleaseApplication.returnToLogin(getOwnActivity());
	                        }else{//失败
	                        	Log.e(TAG, response.optString("msg"));  
	                        	Toast.makeText(getOwnActivity(), response.optString("msg"), Toast.LENGTH_LONG).show();
	                        	
	                        }				
	                        
	                    }  
	                },   
	                new Response.ErrorListener() {  
	                    @Override  
	                    public void onErrorResponse(VolleyError arg0) {  
	                    	Log.e(TAG, "sorry,Error"); 
	                    	if( true == InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) ){
	                    		Toast.makeText(getOwnActivity(), "撤销徽章失败!", Toast.LENGTH_LONG).show();
	                    	}
	                    	if (progressDialog!=null && progressDialog.isShowing()) {  
	                    		progressDialog.dismiss();  
	                        }  

	                    }  
	                });  
	        
	        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
	        InfoReleaseApplication.requestQueue.add(jsonObjectRequest); 	
	}
	
	private void showCancelDialog(final int position){
		new AlertDialog.Builder(getOwnActivity())
		.setTitle("是否撤消当前小红花！")
		.setNegativeButton("取消", null)
		.setPositiveButton("撤消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				cancelBadge(position);
			}
		}).show();
	}
	
	public static final int PICTUER_ADD_REQUEST_CODE = 11;
	public static final int REMARK_IMAGE_PREVIEW_REQUEST_CODE = 12;
	public static final int BADGE_REMARK_REQUEST_CODE = 13;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ( requestCode == PICTUER_ADD_REQUEST_CODE ) {
			ArrayList<String> imgDatas = data.getStringArrayListExtra("img_data");
			takePhotoPopWin.addImgList(imgDatas);
			Log.d(TAG, "resultCode:  "+resultCode);
		}else if ( requestCode == REMARK_IMAGE_PREVIEW_REQUEST_CODE ) {
			ArrayList<String> imgDatas=data.getStringArrayListExtra("img_data");
			takePhotoPopWin.updateImgList(imgDatas);		
		}else if ( requestCode == BADGE_REMARK_REQUEST_CODE ) {
			if( data != null ){
				String text = data.getStringExtra(MyBundleName.BADGE_REMARK_TEXT);
				String title = data.getStringExtra(MyBundleName.BADGE_REMARK_TITLE);
				ArrayList<String> images = data.getStringArrayListExtra(MyBundleName.BADGE_REMARK_IMAGES);
				int score = data.getIntExtra(MyBundleName.BADGE_REMARK_SCORE, 0);
				StudentBadge bean = mStudentBadges.get(mAddPosition);
				if( bean.badgeRemarkBean == null ){
					bean.badgeRemarkBean = new BadgeRemarkBean();
				}
				Log.d(TAG,"onactivity result text:"+text);
				bean.badgeRemarkBean.badgeRemark = text;
				bean.badgeRemarkBean.badgeTitle = title;
				bean.bonusPoint = score;
				
				if(  images != null ){
					bean.badgeRemarkBean.imgList = new String[images.size()];
					images.toArray(bean.badgeRemarkBean.imgList);
				}
				mAdapter.notifyDataSetChanged();
			}
		}
	}
    
    private PopOnClickListener  mPopWinListener = new PopOnClickListener(){

		@Override
		public void itemClick(int position) {
			Intent intent = new Intent(getOwnActivity(), RemarkImagePreviewActivity.class);
			intent.putExtra("position", position);
			intent.putStringArrayListExtra("img_list", takePhotoPopWin.imgList);
			getOwnActivity().startActivityForResult(intent, REMARK_IMAGE_PREVIEW_REQUEST_CODE);
		}

		@Override
		public void lastItemtemClick() {
			Intent intent = new Intent(getOwnActivity(), PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(com.routon.inforelease.util.CommonBundleName.FILE_TYPE_TAG, 14);
			getOwnActivity().startActivityForResult(intent, PICTUER_ADD_REQUEST_CODE);
		}


		@Override
		public void addImgClick(int position) {
			Intent intent = new Intent(getOwnActivity(), PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(com.routon.inforelease.util.CommonBundleName.FILE_TYPE_TAG, 14);
			getOwnActivity().startActivityForResult(intent, PICTUER_ADD_REQUEST_CODE);
		}

		@Override
		public void awardClick() {
			takePhotoPopWin.dismiss();
		}

		@Override
		public void saveRemark(View v) {
			// TODO Auto-generated method stub
			
		}

		
    };
    
	private TakePhotoPopWin takePhotoPopWin;
	private LayoutParams params;
}
