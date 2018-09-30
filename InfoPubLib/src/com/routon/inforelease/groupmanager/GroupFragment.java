package com.routon.inforelease.groupmanager;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import com.routon.widgets.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseFragment;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.GroupInfoBean;
import com.routon.inforelease.json.GroupInfoBeanParser;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonArrayRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.widget.treeView.Node;
import com.routon.inforelease.widget.treeView.TreeListViewAdapter.OnTreeNodeClickListener;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

/**
 * 终端列表界面
 * @author xiaolp
 *
 */
public class GroupFragment extends BaseFragment{
	private ListView groupListView;
	private GroupTreeAdapter<GroupInfo> listAdapter = null;	
	
	private static final int MSG_DEL_GROUP = 0;
	private static final int MSG_ADD_GROUP = 1;
	private static final int MSG_RENAME_GROUP = 2;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_group, container, false);
	}
	
	@Override  
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		this.initTitleBar(R.string.group_title);
		
		groupListView = (ListView)getView().findViewById(R.id.group_list);		
		initGroupList();
		
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	void updateGroupList(ArrayList<GroupInfo> groups){
		try {
			if(listAdapter == null){
				listAdapter = new GroupTreeAdapter(groupListView, this.getContext(), groups, 9);
				groupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
//						Log.d(TAG,"onItemLongClick position:"+position);
						showExtraActionDlg(position);
						return true;
					}
				});
				
				listAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {

					@Override
					public void onClick(Node node, int position) {
						// TODO Auto-generated method stub
//						Log.d(TAG,"OnTreeNodeClickListener position:"+position);
						int childCount = node.getChildrenCout();
						if( childCount == 0 ){//叶子节点，点击显示当前分组所包含终端
							Intent intent = new Intent();
							intent.putExtra(GroupTerminalActivity.GROUPID_TAG, String.valueOf(node.getId()));
							intent.setClass(GroupFragment.this.getActivity(), GroupTerminalActivity.class);						
							startActivity(intent);
						}
					}
				});
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
		if( handler != null ){
			handler.removeCallbacksAndMessages(null);
		}
		hideProgressDialog();
		
		//重新获取数据
		GroupListData.getGroupListData(getOwnActivity(), null, null, null);
	}

	Handler handler = new Handler(){
		 @Override
      public void handleMessage(Message msg) {
//	      Log.d(TAG,"handleMessage");
	      hideProgressDialog();   
	      final Activity activity = GroupFragment.this.getActivity();
	      if( activity == null ) return;
	      
	      if( msg.arg2 != 0 ){//获取网络数据过程中出错    	  
	    	  if( InfoReleaseApplication.showNetWorkFailed(activity) == false ){
	    		  return;
	    	  }
	      }else{
	         if( msg.arg1 == MSG_DEL_GROUP ){//del group       
	        	  if( msg.obj != null  && msg.obj instanceof String ){
           	  		BaseBean bean = BaseBeanParser.parseBaseBean(msg.obj.toString());
           	  		if( bean == null ){//删除分组失败 原因未知
           	  			Toast.makeText(activity, R.string.delete_group_failed, Toast.LENGTH_LONG).show();
           	  			return;
           	  		}else if( bean.code == 0 ){//删除分组成功 
           	  			listAdapter.removeNode(mSelNode);
           	  			listAdapter.notifyDataSetChanged();
           	  			Toast.makeText(activity, R.string.delete_group_success, Toast.LENGTH_LONG).show();
           	  			return;
           	  		}else if( bean.code == -2 ){//登陆会话失效，返回登陆界面
           	  			InfoReleaseApplication.returnToLogin(activity);
           	  			return;
           	  		}else{//删除分组失败提示信息
           	  			Toast.makeText(activity, bean.msg, Toast.LENGTH_LONG).show();
           	  			return;
           	  		}
           	  	} 	  
	          }else if( msg.arg1 == MSG_ADD_GROUP ){//add group
	        	  	hideModifyDataDialog();
	        	  	GroupInfoBean bean = GroupInfoBeanParser.parseGroupInfoBean(msg.obj.toString());
           	  		if( bean == null ){//新建分组失败
           	  			Toast.makeText(activity, R.string.add_group_failed, Toast.LENGTH_LONG).show();
           	  			return;
           	  		}else if( bean.code == 0 ){//新建分组成功 
           	  			if( bean.obj != null ){
           	  				listAdapter.addNode(bean.obj.getName(),bean.obj.getId(),mSelNode);
           	  				listAdapter.notifyDataSetChanged();
           	  			}
           	  			Toast.makeText(activity, R.string.add_group_success, Toast.LENGTH_LONG).show();
           	  			return;
           	  		}else if( bean.code == -2 ){//登陆会话失效，返回登陆界面
           	  			InfoReleaseApplication.returnToLogin(activity);
           	  			return;
           	  		}else{//新建用户失败提示信息
           	  			Toast.makeText(activity, bean.msg, Toast.LENGTH_LONG).show();
           	  			return;
           	  		}
	          }else if( msg.arg1 == MSG_RENAME_GROUP ){//rename group
	        	  	hideModifyDataDialog();
	        		BaseBean bean = BaseBeanParser.parseBaseBean(msg.obj.toString());
           	  		if( bean == null ){//修改分组名称失败
           	  			Toast.makeText(activity, R.string.rename_group_failed, Toast.LENGTH_LONG).show();
           	  			return;
           	  		}else if( bean.code == 0 ){//修改分组名称成功 
           	  			mSelNode.setName(mInputName);
           	  			listAdapter.notifyDataSetChanged();
           	  			Toast.makeText(activity, R.string.rename_group_success, Toast.LENGTH_LONG).show();
           	  			return;
           	  		}else if( bean.code == -2 ){//登陆会话失效，返回登陆界面
           	  			InfoReleaseApplication.returnToLogin(activity);
           	  			return;
           	  		}else{//修改分组名称失败提示信息
           	  			Toast.makeText(activity, bean.msg, Toast.LENGTH_LONG).show();
           	  			return;
           	  		}
	          }
	      }
	      InfoReleaseApplication.showNetDataFailedTip(GroupFragment.this.getContext());
		}
	};
	
	private AlertDialog mModifyDataDialog = null;
	
	private void hideModifyDataDialog(){
		if( mModifyDataDialog == null ) return;
		try 
		{
			Field field = mModifyDataDialog.getClass()
			.getSuperclass().getDeclaredField(
			"mShowing" );
			field.setAccessible( true );
			// 将mShowing变量设为false，表示对话框已关闭 
			field.set(mModifyDataDialog, true );
			mModifyDataDialog.dismiss();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mModifyDataDialog.dismiss();
		mModifyDataDialog = null;
	}
	
	private String mInputName = null;
	private Node mSelNode = null;
	
	//新建分组和修改分组名
	private void showModifyDataDialog(final int type,final int groupId,String defaultName){
		AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
	      
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View layout = inflater.inflate(R.layout.dialog_modify_data, null);
		EditText edit = (EditText)(layout.findViewById(R.id.edit));
		edit.setText(defaultName);
		builder.setView(layout);
		if( type == 0 ){//新建分组
			builder.setTitle(R.string.create_childgroup);
		}else{
			builder.setTitle(R.string.rename_group);			
		}
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				try 
				{
					Field field = mModifyDataDialog.getClass()
					.getSuperclass().getDeclaredField(
					"mShowing" );
					field.setAccessible( true );
					// 将mShowing变量设为false，表示对话框已关闭 
					field.set(mModifyDataDialog, false );
					mModifyDataDialog.dismiss();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				EditText edit = (EditText)(layout.findViewById(R.id.edit));
				String name = edit.getText().toString();
				if( name == null || name.isEmpty() ){//输入数据为空
					Toast.makeText(GroupFragment.this.getActivity(), R.string.data_is_null, Toast.LENGTH_LONG).show();				
					return;
				}
				
				showProgressDialog();
				
				mInputName = name;
				if( type == 0 ){//新建分组
					String url = UrlUtils.getGroupAddUrl(groupId,name);
					HttpClientDownloader.getInstance().getResultFromUrlWithSession(url,handler,MSG_ADD_GROUP);
				}else{//修改分组名称
					String url = UrlUtils.getGroupRenameUrl(groupId,name);
					HttpClientDownloader.getInstance().getResultFromUrlWithSession(url,handler,MSG_RENAME_GROUP);
				}	
				
    	  	}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				hideModifyDataDialog();
    	  	}
		});
		mModifyDataDialog = builder.create();
		mModifyDataDialog.show();
	}
	
	private void showExtraActionDlg(final int position) {	
		final Node node = (Node) listAdapter.getItem(position);
		mSelNode = node;
		int childCount = node.getChildrenCout();
		DialogInterface.OnClickListener listener =  new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0://新建子分组	
					showModifyDataDialog(0,node.getId(),null);
					break;
				case 1://重命名
					showModifyDataDialog(1,node.getId(),node.getName());
					break;
				case 2: {//删除确认对话框
					String msg = GroupFragment.this.getContext().getResources().getString(R.string.delete_confirm_group_msg)
							+" "+node.getName()+" ?";
					new AlertDialog.Builder(GroupFragment.this.getContext()).setMessage(msg)
					.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {//确认删除
							// TODO Auto-generated method stub
							String url = UrlUtils.getGroupDelUrl(node.getId());
//							Log.d(TAG,"delete group");
							HttpClientDownloader.getInstance().getResultFromUrlWithSession(url, handler, MSG_DEL_GROUP);
						}
					}).setNegativeButton(R.string.cancel,null).create().show();
					}
					break;
					
				
				}
			}
		};
		if( childCount == 0 ){//叶子节点,增加删除功能
			String[] options = {this.getContext().getString(R.string.create_childgroup),					
					this.getContext().getString(R.string.rename_group),
					this.getContext().getString(R.string.delete_group)};  
			AlertDialog dlg = new AlertDialog.Builder(this.getContext()).setItems(options,listener).create();
			dlg.show();
		}else{
			String[] options = {this.getContext().getString(R.string.create_childgroup)
					,this.getContext().getString(R.string.rename_group)};  
			AlertDialog dlg = new AlertDialog.Builder(this.getContext()).setItems(options,listener).create();
			dlg.show();
		}
	}
	
	void initGroupList(){
		showProgressDialog();
		GroupListData.getGroupListData(getOwnActivity(), new DataResponse.Listener<ArrayList<GroupInfo>>() {

			@Override
			public void onResponse(ArrayList<GroupInfo> response) {
				// TODO Auto-generated method stub
				hideProgressDialog();
				updateGroupList(response);
			}
		},  new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				 hideProgressDialog();
				Toast.makeText(getOwnActivity(), "获取分组数据失败", Toast.LENGTH_SHORT).show();
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideProgressDialog();
			}
			
		});
	}
  	
}
