package com.routon.smartcampus.user;

import java.util.ArrayList;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ListView;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.groupmanager.GroupTreeAdapter;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.widget.treeView.Node;
import com.routon.inforelease.widget.treeView.TreeListViewAdapter.OnTreeNodeClickListener;
import com.routon.smartcampus.homework.HomeworkActivity;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.widgets.Toast;

public class ScreenGroupSelectActivity extends CustomTitleActivity{
	private ListView listView;
	private String tid;
	private GroupTreeAdapter<GroupInfo> listAdapter;
	public Dialog mWaitDialog = null;
	
	private final String TAG = "ScreenGroupSelectActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_group_select);
		init();
		initGroupList();
	}
	
	public void init(){
		initTitleBar(R.string.menu_change_group);
		setTitleBackground(ContextCompat.getDrawable(this, R.drawable.student_title_bg));
		listView = (ListView)findViewById(R.id.list_group_select);
		tid = getIntent().getStringExtra("tid");
	}
	
	//取分组数据
	public void initGroupList(){
		showProgressDialog();
		GroupListData.getSchoolGroupListData(ScreenGroupSelectActivity.this,InfoReleaseApplication.authenobjData.schoolIds[0], new DataResponse.Listener<ArrayList<GroupInfo>>() {

			@Override
			public void onResponse(ArrayList<GroupInfo> response) {
				// TODO Auto-generated method stub
				Log.d(TAG, response.toString());
				hideProgressDialog();
				updateGroupList(response);
			}
		},  new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				 hideProgressDialog();
				Toast.makeText(ScreenGroupSelectActivity.this, "获取分组数据失败", Toast.LENGTH_SHORT).show();
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideProgressDialog();
			}
			
		},false);
	}
	
	public void updateGroupList(final ArrayList<GroupInfo> groups){
		try {
			if(listAdapter == null){
				listAdapter = new GroupTreeAdapter<GroupInfo>(listView, ScreenGroupSelectActivity.this, groups, 9);			
				listAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {

					@Override
					public void onClick(Node node, int position) {
						int childCount = node.getChildrenCout();
						if( childCount == 0 ){//叶子节点，点击显示当前分组所包含终端
							GroupInfo group = groups.get(position);
							String assignGroupId = String.valueOf(group.getId());
							String archiveIds = tid;
							String assignName = group.getName();
							assignGroup(assignGroupId, archiveIds, assignName);
						}
					}
				});
			}
			listView.setAdapter(listAdapter);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param assignGroupId 单个分组Id
	 * @param archiveIds    终端Id
	 */
	public void assignGroup(String assignGroupId, final String archiveIds, final String assignName){
		String url = SmartCampusUrlUtils.getAssignGroup(assignGroupId, archiveIds);
		Log.d(TAG, "url="+url);
		CookieJsonRequest request = new CookieJsonRequest(Method.GET, url, null, 
				new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response="+response);
						int code = response.optInt("code");
						String msg = response.optString("msg");
						if(code == 0){
//							Toast.makeText(ScreenGroupSelectActivity.this, "重新分配到"+assignName, Toast.LENGTH_SHORT).show();
							
							final AlertDialog.Builder normalDialog = 
						            new AlertDialog.Builder(ScreenGroupSelectActivity.this);
						        
						        normalDialog.setMessage("已成功将智慧屏"+archiveIds+"切换到"+assignName);
						        normalDialog.setPositiveButton("确定", 
						            new DialogInterface.OnClickListener() {
						            @Override
						            public void onClick(DialogInterface dialog, int which) {
						            	ScreenGroupSelectActivity.this.finish();
						            }
						        });
						        normalDialog.show();
							
						}
						else {
							Toast.makeText(ScreenGroupSelectActivity.this, msg, Toast.LENGTH_SHORT).show();
						}
					}
				}, 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(ScreenGroupSelectActivity.this, "重新分配班级失败", Toast.LENGTH_SHORT).show();
					}
				});
		request.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(request);
	}

	public void hideProgressDialog(){
		if( mWaitDialog != null ){
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}
	}
	
	public void showProgressDialog(){
		if( mWaitDialog == null ){
			mWaitDialog = new Dialog(ScreenGroupSelectActivity.this,R.style.new_circle_progress);    
			mWaitDialog.setContentView(R.layout.dialog_wait);    
			mWaitDialog.setCancelable(true);
			mWaitDialog.show();
		}
	}
}
