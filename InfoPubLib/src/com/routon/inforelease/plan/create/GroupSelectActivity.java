package com.routon.inforelease.plan.create;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonArrayRequest;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.widget.treeView.Node;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ListView;
import com.routon.widgets.Toast;

public class GroupSelectActivity extends CustomTitleActivity {

	private final String TAG = "GroupSelectFragment";
	private ListView groupListView;
	private SimpleTreeAdapter<GroupInfo> listAdapter = null;
	
	private ArrayList<GroupInfo> groups = new ArrayList<GroupInfo>();
	
	private int mPlanId;
	private String mPlanName = null;
	
	private String startBy;
	private String selectParams = null;
	
	private int GROUP_SELECT_RESULT = 5;
	private boolean mSelOnly = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_select_fragment);

		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			mPlanId = bundle.getInt("plan_id");
			mPlanName = bundle.getString("plan_name");
			selectParams = bundle.getString("select_param");
			startBy = bundle.getString("start_by");
			
			
			Log.i(TAG, "---id:"+mPlanId+"  name:"+mPlanName+"  selectParams:"+selectParams);
		}	
		
		mSelOnly = getIntent().getBooleanExtra("sel_only",false);
		
		initTitleBar(R.string.publish_team_select_title);
        this.setTitleNextImageBtnClickListener(R.drawable.ok, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( mSelOnly == true ){
					String groupids = getSelectIdString();
					if( groupids == null || groupids.trim().length() == 0 ){
						Toast.makeText(GroupSelectActivity.this, R.string.sel_empty_group, Toast.LENGTH_SHORT).show();
						return;
					}
					Log.d("group sel activity","finish sel groups groupids:"+groupids);
					Intent intent = new Intent();
					intent.putExtra("groupids", groupids); 
//					intent.putExtra("groupnum", selGroups.size()); 
					GroupSelectActivity.this.setResult(RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
					finish();//此处一定要调用finish()方法
				}else{
					addPlanGroup();
				}
			}
		});
        
        groupListView = (ListView)findViewById(R.id.group_list);
		
		initGroupList();
	}
	
	private String getSelectIdString(){
		int len = listAdapter.selectList.size();
		String selectIds = new String();
		for(int i=0; i< len; i++){
			Node node = listAdapter.selectList.get(i);
			if( node.getParent() == null || listAdapter.selectList.contains(node.getParent()) == false ){//此节点父节点未被选中
				selectIds += node.getId();
				selectIds += ",";
			}		
		}
		
		return selectIds;
	}
	
	void initGroupList(){
		String urlString = UrlUtils.getGroupListUrl();
		 
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "...Loading...");  
  
        CookieJsonArrayRequest jsonObjectRequest = new CookieJsonArrayRequest(urlString, 
        		new Response.Listener<JSONArray>() {  
                    @Override  
                    public void onResponse(JSONArray response) {  
                        Log.d(TAG, "response="+response);  
                        if (progressDialog.isShowing()&&progressDialog!=null) {  
                            progressDialog.dismiss();  
                        }
                        parseGroupList(response);
                		
                    	updateGroupList();
                        
                    }
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(getApplicationContext(), "网络连接失败!", Toast.LENGTH_LONG).show();
                    	if (progressDialog.isShowing()&&progressDialog!=null) {  
                            progressDialog.dismiss();  
                        }  
                    }  
                });  
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	void parseGroupList(JSONArray jsonArray){
		
		int length = jsonArray.length();
		
		for(int i = 0; i<length; i++){
			JSONObject parentJsonObj = jsonArray.optJSONObject(i);
			if( parentJsonObj == null ){
				continue;
			}
			GroupInfo info = new GroupInfo();
			info.setId(parentJsonObj.optInt("id"));
			info.setName(parentJsonObj.optString("name"));
			info.setPid(parentJsonObj.optInt("pid"));
			
			Log.i(TAG, "---------"+info.getId() + " "+info.getPid()+" "+info.getName());
			groups.add(info);
			
			JSONArray childJsonArray = parentJsonObj.optJSONArray("children");
			if(childJsonArray == null || childJsonArray.length() == 0){
				continue;
			}else{
				parseGroupList(childJsonArray);
			}
		}
		
	}
	
	void updateGroupList(){
		try {
			if(listAdapter == null){
	
				listAdapter = new SimpleTreeAdapter(groupListView, this, groups, 9);
				if(selectParams != null){
					setSelectGroup(selectParams);
				}
			}
	
			groupListView.setAdapter(listAdapter);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	private ArrayList<GroupInfo> getSelectGroup(){
		ArrayList<GroupInfo> infos = new ArrayList<GroupInfo>();
		int len = groups.size();
		for(int i= 0; i<len; i++){
			GroupInfo info = groups.get(i);
			if(isSelectListContains(listAdapter.selectList, info)){
				infos.add(info);
			}
		}
		
		return infos;
	}
	
	private void setSelectGroup(String idString){
		Log.i(TAG, "-----selectId:"+idString);
		listAdapter.setSelectList(idString);
	}
	
	private boolean isSelectListContains(ArrayList<Node>selectList, GroupInfo info){
		int selectSize = selectList.size();
		for(int i= 0; i<selectSize; i++){
			Node selectItem = selectList.get(i);
			if(selectItem.getId() == info.getId()){
				return true;
			}
		}
		return false;
	}

	public void addPlanGroup(){
		String paramStr = new String();
		paramStr += "id="+mPlanId;
		
		if(getSelectGroup() == null || getSelectGroup().size() == 0){
			Toast.makeText(getApplicationContext(), "未选择投放分组!", Toast.LENGTH_LONG).show();
			return;
		}
		
		int len = getSelectGroup().size();
		for(int i = 0; i< len; i++){
			GroupInfo info = getSelectGroup().get(i);
			paramStr += "&";
			paramStr += "groupids="+info.getId();
		}
		
		String urlString = UrlUtils.getPlanAssignUrl();
		urlString += "?"+paramStr;
		Log.i(TAG, "URL:" + urlString);
		
//        RequestQueue requestQueue = Volley.newRequestQueue(this);   
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "...Loading...");  
        
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.POST, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                        if (progressDialog.isShowing()&&progressDialog!=null) {  
                            progressDialog.dismiss();  
                        }
                        
						int code =  response.optInt("code");
						if( code == 1){
							Toast.makeText(GroupSelectActivity.this, "分组投放成功!", Toast.LENGTH_LONG).show();

				        	
				        	if(startBy != null && startBy.equals("publish")){

								Intent resultIntent = new Intent();			
								resultIntent.putExtra("isChange", true);
								setResult(RESULT_OK, resultIntent);
				        		finish();
				        	}else{
								Intent intent = new Intent(GroupSelectActivity.this, PublishActivity.class);
								PlanListrowsBean bean = new PlanListrowsBean();
								bean.contractId = mPlanId;
								bean.contractName = mPlanName;
								intent.putExtra("plan", bean);
					        	GroupSelectActivity.this.startActivity(intent);
					        	finish();
				        	}
				        	
                        }else if( code == -2){
                        	InfoReleaseApplication.returnToLogin(GroupSelectActivity.this);
                        }else{//失败
                        	Log.e(TAG, response.optString("msg"));  
                        	Toast.makeText(GroupSelectActivity.this, response.optString("msg"), Toast.LENGTH_LONG).show();
//		                        	
                        }		
                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(getApplicationContext(), "网络连接失败!", Toast.LENGTH_LONG).show();
                    	if (progressDialog.isShowing()&&progressDialog!=null) {  
                            progressDialog.dismiss();  
                        }  
                    }  
                });  
        
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
		
	}
}
