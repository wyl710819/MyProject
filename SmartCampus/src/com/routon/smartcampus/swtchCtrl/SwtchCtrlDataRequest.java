package com.routon.smartcampus.swtchCtrl;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.TerminalListBean;
import com.routon.inforelease.json.TerminalListBeanParser;
import com.routon.inforelease.json.TerminalListSwtchBean;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonArrayRequest;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.schoolcompare.ClassCompareBean;
import com.routon.smartcampus.schoolcompare.ClassMarkActivity;
import com.routon.smartcampus.schoolcompare.SubprojectBean;
import com.routon.widgets.Toast;

class TerminalGroup
{
	int pid;
	String pName;
	ArrayList<TerminalListdatasBean> terminals = new  ArrayList<TerminalListdatasBean>();	

}

public class SwtchCtrlDataRequest {

	
    public interface DataRequestListener {
  
        public void onAllTerminalsObtain();
        public void onAllGroupList();
   }
    
    public static DataRequestListener delegate;
	private static String TAG = "SwtchCtrlDataRequest";
	private static ArrayList<GroupInfo> groups = new ArrayList<GroupInfo>();
	private static List<TerminalListdatasBean> mTerminalListdatasBean = new ArrayList<TerminalListdatasBean>();
	public static BaseActivity baseAcitivity;
	
	//
	public static ArrayList<TerminalGroup> mTerminalsGroup;
	public static int selIndex = 0;
	public static boolean shouldRefresh = false;
	public static List<TerminalGroup> divTerminalGroup()
	{
		
		List<GroupInfo> groups = SwtchCtrlDataRequest.groups;
		List<TerminalListdatasBean> terminals =  SwtchCtrlDataRequest.mTerminalListdatasBean;
		
		List<TerminalGroup> mgroups = new  ArrayList<TerminalGroup>();
		
		
		for (TerminalListdatasBean terminal:terminals)
		{
			GroupInfo node =  findTerminalInGroupTree(groups,terminal);
			if ( node != null)
			{
//				String pName  = node.getpName();
//				int pId = node.getPid();
			
				addTerminalToTerminalGroups(terminal, node, mgroups);
					
			}
		}
	
		SwtchCtrlDataRequest.mTerminalsGroup = (ArrayList<TerminalGroup>) mgroups;
		return mgroups;
		
	}
	private static void addTerminalToTerminalGroups(TerminalListdatasBean terminal,
			GroupInfo node,List<TerminalGroup> mgroups)
	{
		for (TerminalGroup group:mgroups)
		{
			if (group.pid == node.getPid())
			{
				group.terminals.add(terminal);
				return ;
			}
		}
		
		TerminalGroup groupnew = new TerminalGroup();
		
		groupnew.pid = node.getPid();
		groupnew.pName = node.getpName();
		groupnew.terminals.add(terminal);
		
		mgroups.add(groupnew);
	}
	private static GroupInfo findTerminalInGroupTree(List<GroupInfo> mgroups,TerminalListdatasBean terminal)
	{
		
		for (GroupInfo group:mgroups)
		{
			if (terminal.groupid == group.getId())
			{
				return  group;
			}
		}
		
		return null;
	
		
		
	}
	public static void getAllTerminals()
	{
		
		//优先查询terminalId
		String urlString = null;
		urlString = UrlUtils.getTerminalListUrlTerMode(2, 1, 100, null, null, null, null, null, null, "S1810");

		Log.d(TAG , "URL:" + urlString);
  
       baseAcitivity.showProgressDialog();
       
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
//                        Log.d(TAG, "response="+response);  
        
                        baseAcitivity.hideProgressDialog();
						try {
							TerminalListBean bean = TerminalListBeanParser.parseTerminalListBean(response);
							if (bean == null || bean.datas == null || bean.datas.size() == 0) {								
								String text = "未查询到终端，可能您无权管理这台终端";
								if (bean == null) {
									text = "查询终端失败";
								}
								Toast.makeText(baseAcitivity, text, Toast.LENGTH_SHORT).show();
								return;
							}
							
//							if (bean.datas.size() > 1) {
//								Toast.makeText(baseAcitivity, "查询到" + bean.datas.size() + "个终端", Toast.LENGTH_SHORT).show();
//							}
//							showTerminalDetail(bean.datas.get(0));
							mTerminalListdatasBean = bean.datas;
							if (delegate != null)
							{
								delegate.onAllTerminalsObtain();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
//                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(baseAcitivity, "网络连接失败!", Toast.LENGTH_LONG).show();
                    	baseAcitivity.hideProgressDialog();
                    }  
                });  
        
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest); 
	
	}
	
	
	public static void initGroupList(){
		String urlString = UrlUtils.getGroupListUrl();
		 
     
		baseAcitivity.showProgressDialog();
        CookieJsonArrayRequest jsonObjectRequest = new CookieJsonArrayRequest(urlString, 
        		new Response.Listener<JSONArray>() {  
                    @Override  
                    public void onResponse(JSONArray response) {  
                        Log.d(TAG, "response="+response);  

						baseAcitivity.hideProgressDialog();
                        parseGroupList(response);
                		
						if (delegate != null)
						{
							delegate.onAllGroupList();
						}
//                    	updateGroupList();
                        
                    }
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(baseAcitivity, "网络连接失败!", Toast.LENGTH_LONG).show();
                    	baseAcitivity.hideProgressDialog();
                    }  
                });  
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	static void parseGroupList(JSONArray jsonArray){
		
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
			info.setpName(parentJsonObj.optString("pName"));
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
	
	public static boolean isSwtcherAllDown(TerminalGroup terGroup)
	{
		boolean allDown = true;
		
		for (TerminalListdatasBean item:terGroup.terminals)
		{
			for (TerminalListSwtchBean swtch:item.mswtchs)
			{
				if (swtch.status == 1)
				{
					allDown = false;
					return allDown;
				}
			}
		}
		return allDown;
	}
	public static void sendSwtchForGroup(TerminalGroup terminalGroup,boolean allOn)
	{
		List<SwtchParm> swtchs = new ArrayList<SwtchParm>();
		
		for (TerminalListdatasBean item:terminalGroup.terminals)
		{
			String id = item.terminalid;
			int status = allOn == true ? 1 : 0;
			
			SwtchParm parm =  new SwtchParm(id, 0, status);
			swtchs.add(parm);					
		}
		sendSwtchCtrl(swtchs);

	}
	
	public static void sendSwtchCtrl(List<SwtchParm> swtchs)
	{
		String urlString = UrlUtils.getSwitchCtrlUrl();
		List<NameValuePair> params = new ArrayList<NameValuePair>();		

        if (baseAcitivity != null)
        {
        	baseAcitivity.showProgressDialog();
        }
        for (SwtchParm item:swtchs)
        {
        	params.add(new BasicNameValuePair("s1810", String.valueOf(item.id)));
        	params.add(new BasicNameValuePair("swtch", String.valueOf(item.swtch)));
        	params.add(new BasicNameValuePair("status", String.valueOf(item.status)));
        }
        		
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
				        if (baseAcitivity != null)
				        {
				        	baseAcitivity.hideProgressDialog();
				        }
						try {
							if (response.getInt("code") == 0) {
								
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(baseAcitivity, response.getString("msg"), Toast.LENGTH_LONG)
										.show();
								
								
							} else if (response.getInt("code") == -2) {
//								InfoReleaseApplication.returnToLogin(ClassMarkActivity.this);
								Toast.makeText(baseAcitivity, "认证失败,请稍后再试!", Toast.LENGTH_LONG).show();
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(baseAcitivity, response.getString("msg"), Toast.LENGTH_LONG)
										.show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(baseAcitivity, "网络连接失败!", Toast.LENGTH_LONG).show();
						baseAcitivity.hideProgressDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);		
	}
}
