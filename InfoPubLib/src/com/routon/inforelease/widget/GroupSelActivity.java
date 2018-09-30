package com.routon.inforelease.widget;

import java.util.ArrayList;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import com.routon.widgets.Toast;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.SimpleTreeAdapter;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.widget.treeView.Node;

//分组选择控件，返回选择的分组，不实现数据上传
public class GroupSelActivity extends CustomTitleActivity {

	private final String TAG = "GroupSelectFragment";
	private ListView groupListView;
	private SimpleTreeAdapter<GroupInfo> listAdapter = null;
	
	private ArrayList<GroupInfo> groups = new ArrayList<GroupInfo>();

	private String selectParams = null;
	private boolean mGroupSelHeadTeachers = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_select_fragment);

		Bundle bundle = getIntent().getExtras();
		if(bundle != null){			
			selectParams = bundle.getString("select_param");	
			mGroupSelHeadTeachers = bundle.getBoolean(CommonBundleName.GROUP_SEL_HEADTEACHERS);
			
			Log.i(TAG, "---selectParams:"+selectParams);
		}	
		
		initTitleBar(R.string.publish_team_select_title);
        setTitleNextImageBtnClickListener(R.drawable.ok,new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if( mSelOnly == true ){
					String groupids = getSelectIdString();
					if( groupids == null || groupids.trim().length() == 0 ){
						Toast.makeText(GroupSelActivity.this, R.string.sel_empty_group, Toast.LENGTH_SHORT).show();
						return;
					}
//					Log.d(TAG,"finish sel groups groupids:"+groupids+",groupnames:"+groupnames);
					Intent intent = new Intent();
					intent.putExtra("groupids", groupids); 
//					intent.putExtra("groupnum", selGroups.size()); 
					GroupSelActivity.this.setResult(RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
					finish();//此处一定要调用finish()方法
//				}else{
//					addPlanGroup();
//				}
			}
		});
        
        groupListView = (ListView)findViewById(R.id.group_list);
		
		initGroupList();
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
	
	private String getSelectIdString(){
//		int len = listAdapter.selectList.size();
		String selectIds = new String();
//		for(int i=0; i< len; i++){
//			Node node = listAdapter.selectList.get(i);
//			selectIds += node.getId();
//			selectIds += ",";
//		}
		int len = groups.size();
		for(int i= 0; i<len; i++){
			GroupInfo info = groups.get(i);
			if(isSelectListContains(listAdapter.selectList, info)){
				boolean isChildFlag = true;
				for(int j = 0; j< len; j++){
					GroupInfo item = groups.get(j);
					if(item.getPid() == info.getId()){
						isChildFlag = false;
						break;
					}
				}
				if(isChildFlag){
					selectIds += info.getId();
					selectIds += ",";
				}
			}
		}
		
		return selectIds;
	}
	
	void initGroupList(){
		
		showProgressDialog();
		GroupListData.getGroupListData(this, new DataResponse.Listener<ArrayList<GroupInfo>>() {

			@Override
			public void onResponse(ArrayList<GroupInfo> response) {
				// TODO Auto-generated method stub
				hideProgressDialog();
				groups.clear();
				if( mGroupSelHeadTeachers == true && InfoReleaseApplication.authenobjData.headTeacherClasses != null 
						&& InfoReleaseApplication.authenobjData.headTeacherClasses.length > 0 ){
					for( GroupInfo group : response){
						for( int i = 0; i < InfoReleaseApplication.authenobjData.headTeacherClasses.length; i++){
							if(InfoReleaseApplication.authenobjData.headTeacherClasses[i].equals(String.valueOf(group.getId())) ){
								groups.add(group);
								break;
							}
						}
					}
				}else{
					groups = response;
				}
				updateGroupList();
			}
		},  new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				 hideProgressDialog();
				Toast.makeText(GroupSelActivity.this, "获取分组数据失败", Toast.LENGTH_SHORT).show();
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideProgressDialog();
			}
			
		});
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
	
	private void setSelectGroup(String idString){
		Log.i(TAG, "-----selectId:"+idString);
		listAdapter.setSelectList(idString);
	}
}
