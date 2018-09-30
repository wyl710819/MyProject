package com.routon.inforelease;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.routon.widgets.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.routon.inforelease.json.TerminalListBean;
import com.routon.inforelease.json.TerminalListBeanParser;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

public class TerminalListHelper{
//	private static final String TAG = "TerminalListHelper";
	public static final String TERMINAL_IDS = "terminalids";
	public static final String TERMINALS = "terminals";
	private PullToRefreshListView lv_terminal;
	private Activity mActivity;
	private int m_iPage;
	private List<TerminalListdatasBean> mData;
	private BaseAdapter mTerminalAdapter;
	private int mPageSize = 10;
	private Handler handler = new Handler(){
		 @Override
	      public void handleMessage(Message msg) {
//		      Log.d(TAG,"terminal list helper handleMessage");
		      hideProgressDialog();
		      lv_terminal.onRefreshComplete();
		      
		      if( msg.arg2 != 0 ){
		    	  //网络连接失败   	  
		    	  if( InfoReleaseApplication.showNetWorkFailed(mActivity) == false ){
		    		  return;
		    	  }
		      }else{
		          if( msg.arg1 == 0 ){//get terminal list answer	        	 
		        	  if( msg.obj != null && msg.obj instanceof String ){
			        	  TerminalListBean bean = TerminalListBeanParser.parseTerminalListBean(msg.obj.toString());
//			        	  Log.d(TAG,"get terminal list "+msg.obj.toString());
			        	  if( bean != null ){    
		        			  if( bean.code == 0  ){//获取数据成功  
		        				  if( bean.datas != null && bean.datas.size() > 0 ){
						        	  m_iPage = bean.page;
						        	  if( m_iPage == 1 ){//获取到的第一页数据
										  mData.clear();
									  }
						        	  if( bean.fullListSize > 0 && bean.pageSize > 0 ){
						        		  mPageSize = bean.fullListSize/bean.pageSize +(bean.fullListSize%bean.pageSize>0 ?1:0);
						        	  }
//						        	  Log.d(TAG,"terminal list fragment handleMessage bean size:"+bean.datas.size()+",m_iPage:"+m_iPage);
						        	  removeUnSelTerminals(bean.datas);
						        	  mData.addAll(bean.datas);
						        	  if( mTerminalAdapter != null ){
						        		  mTerminalAdapter.notifyDataSetChanged();
						        	  }
						        	  return;
		        				  }else{//获取到数据为空
		        					  return;
		        				  }
		        			  }else if( bean.code == -2 ){//登陆会话失效
		              	  			InfoReleaseApplication.returnToLogin(mActivity);
		              	  			return;
		              	  	  }else{//提示返回错误信息
		        				  Toast.makeText(mActivity, bean.msg, Toast.LENGTH_LONG).show();
		        				  return;
		        			  }
			        	  }
		        	  }   	  
		          }else if( msg.arg1 == 1 ){//add terminal     	 
		        	  if( msg.obj != null && msg.obj instanceof String ){
			        	  BaseBean bean = BaseBeanParser.parseBaseBean(msg.obj.toString());
//			        	  Log.d(TAG,"add terminal "+msg.obj.toString());
			        	  if( bean != null ){    
		        			  if( bean.code == 0  ){//添加终端成功
		        				  Toast.makeText(mActivity, R.string.terminal_assign_success, Toast.LENGTH_SHORT).show();
		        				  if( mOnTerminalListNetWorkReply != null ){
		        					  mOnTerminalListNetWorkReply.addTerminal(true);
		        				  }
		        				  return;
		        			  }else if( bean.code == -2 ){//登陆会话失效
		              	  			InfoReleaseApplication.returnToLogin(mActivity);
		              	  			return;
		              	  	  }else{//提示返回错误信息
		        				  Toast.makeText(mActivity, bean.msg, Toast.LENGTH_LONG).show();
		        				  return;
		        			  }
			        	  }else{
			        		  Toast.makeText(mActivity, R.string.terminal_assign_failed, Toast.LENGTH_SHORT).show();
			        		  return;
			        	  }
		        	  }else{
		        		  Toast.makeText(mActivity, R.string.terminal_assign_failed, Toast.LENGTH_SHORT).show();
		        		  return;
		        	  }    	  
		          }	         
		      }
		      InfoReleaseApplication.showNetDataFailedTip(mActivity); 
			}
		};;

	public TerminalListHelper(PullToRefreshListView listview,BaseAdapter adapter,Activity activity) {
		// TODO Auto-generated constructor stub
		lv_terminal = listview;
		mActivity = activity;
		mTerminalAdapter = adapter;
		initTerminalList();
		
		
	}
	
	public int getDataSize(){
		if( mData == null ) return 0;
		return mData.size();
	}
	
	public TerminalListdatasBean getTerminalData(int position){
		if( mData == null ) return null;
		TerminalListdatasBean data = mData.get(position);
		return data;
	}
	
	public void reload(){
		mData.clear();
		if( mTerminalAdapter != null ){
			mTerminalAdapter.notifyDataSetChanged();
		}
		showProgressDialog();
		reloadData();
	}
	
	private void reloadData(){
		//获取第一页数据
		m_iPage = 0;
		getTerminalList(1);
	}
	
	public void addData(List<TerminalListdatasBean> datas){
		mData.addAll(datas);
		if( mTerminalAdapter != null ){
			mTerminalAdapter.notifyDataSetChanged();
		}
	}
	
	public void initTerminalList(){
	 	// 下拉刷新时的提示文本设置  
		lv_terminal.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");  
		lv_terminal.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在更新...");  
		lv_terminal.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");  
		// 上拉加载更多时的提示文本设置  
		lv_terminal.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载");  
		lv_terminal.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");  
		lv_terminal.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");  

		lv_terminal.setOnRefreshListener(new OnRefreshListener<ListView>() {  
            @Override  
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {  
                String label = DateUtils.formatDateTime(mActivity.getApplicationContext(), System.currentTimeMillis(),  
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);  
  
                // Update the LastUpdatedLabel  
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);  
                reload();
            }  
        });  
		
		lv_terminal.setOnLastItemVisibleListener(new OnLastItemVisibleListener(){
			@Override
			public void onLastItemVisible() {
				// TODO Auto-generated method stub
				getTerminalList(m_iPage+1);
//				Log.d(TAG,"pull and refresh listview last item visible listener");
			}
		});
		
		ListView actualListView = lv_terminal.getRefreshableView();  
        actualListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if( mOnTerminalListItemClickListener != null ){
					mOnTerminalListItemClickListener.onTerminalListItemClick(position, mData.get(position-1));
				}
			}		
		});
		
	    mData = new ArrayList<TerminalListdatasBean>();
	}
	
	public ArrayList<Integer> getTerminalArchiveids(){
		if( mData == null ) return null;
		ArrayList<Integer> archiveids = new ArrayList<Integer>();
		for( int i = 0; i < mData.size(); i++ ){
			archiveids.add(mData.get(i).archiveid);
		}
		return archiveids;
	}
	
	private ArrayList<Integer> mUnSelTerminalIds = null;
	public void setUnSelTerminalIds(ArrayList<Integer> ids){
		if( ids == null || ids.size() == 0){
			return;
		}
		mUnSelTerminalIds = ids;
	}
	
	private void removeUnSelTerminals(List<TerminalListdatasBean> datas){
		if( mUnSelTerminalIds == null || mUnSelTerminalIds.size() == 0 ) return;
//		Log.d(TAG,"removeUnSelTerminals before datas size:"+datas.size());
		List<TerminalListdatasBean> unselDatas = new ArrayList<TerminalListdatasBean>();
		for( int i = 0; i < datas.size(); i++ ){
			for( int j = 0; j < mUnSelTerminalIds.size(); j++ ){
				TerminalListdatasBean itemdata = datas.get(i);
				if( (itemdata.archiveid == mUnSelTerminalIds.get(j).intValue()) == true ){
					unselDatas.add(itemdata);
					break;
				}
			}
		}
		datas.removeAll(unselDatas);
//		Log.d(TAG,"removeUnSelTerminals after datas size:"+datas.size()+",unselDatas size:"+unselDatas.size());
	}
	
	public interface onTerminalListItemClickListener {
		  void onTerminalListItemClick(int position,TerminalListdatasBean data);		
	}
	
	private onTerminalListItemClickListener mOnTerminalListItemClickListener = null;
	public void setOnTerminalListItemClickListener(onTerminalListItemClickListener listener){
		mOnTerminalListItemClickListener = listener;
	}
	
	public interface onTerminalListNetWorkReply {
		  void addTerminal(boolean success);		
	}
	
	private onTerminalListNetWorkReply mOnTerminalListNetWorkReply = null;
	public void setOnTerminalListNetWorkReply(onTerminalListNetWorkReply listener){
		mOnTerminalListNetWorkReply = listener;
	}
	
	public void cancelAllHandlerMsg(){
		if( handler != null ){
			handler.removeCallbacksAndMessages(null);
		}
		this.hideProgressDialog();
	}
	
	public static final int ALL_TERMINAL = 0;
	public static final int ONLINE_TERMINAL = 1;
	public static final int OFFLINE_TERMINAL = 2;
	private int mTerminalStateType;
	
	public void setTerminalStateType(int type){
		mTerminalStateType = type;
	} 
	
	private String mBtMac;
	public void setBluetoothMac(String btMac) {
		mBtMac = btMac;
	}
	
	private String mGroupIds = null;
	public void setGroups(String groupids){
		mGroupIds = groupids;
	}
	
	public void addTerminal(String archiveId){
//		Log.d(TAG,"add terminal:"+archiveId);
		HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getAssignTerminalUrl(mGroupIds,archiveId), handler, 1);
	}
	
	private void getTerminalList(int page){
//		Log.d(TAG,"getTerminalList page:"+page+",mPageSize:"+mPageSize);
		if( page > mPageSize ) return;//不能超过总页数
		String state = null;
		if( mTerminalStateType == ONLINE_TERMINAL ){
			state = "1";
		}else if( mTerminalStateType == OFFLINE_TERMINAL ){
			state = "0";
		}
		String archiveid = null;
		//为了防止分页列表重复查到前一页的数据，特此加上此参数。
		//当客户端请求分页列表数据时，该参数传上一次请求数据中最后一个数据的createtime.(取第一页数据不需要此参数)
		if( page > 1 ){
			archiveid = String.valueOf(mData.get(mData.size()-1).archiveid);
//			Log.d(TAG,"archiveid:"+archiveid);
	
		}
		HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getTerminalListUrl(1, page, 100,mGroupIds, null, null, state, mBtMac, archiveid), handler, 0);
	}
	
	private Dialog mWaitDialog = null;
	private static ProgressDialog progressDialog;
	
	private void hideProgressDialog(){
		if( mWaitDialog != null ){
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}
	}
	
	public interface Response{
		void onSuccess(String text,TerminalListdatasBean data);
		void onFailed(String text);
		void onError(String text);
	} 
	
	public static void getTerminalAuth(final Context context,String btMac, String terminalId,Boolean isShowDialog,final Response tResponse ) {
		String urlString = null;
		//优先查询terminalId
		if ( terminalId != null && terminalId.isEmpty() == false ) {
			urlString = UrlUtils.getTerminalListUrl(1, 1, 10, null, terminalId, null, null, null, null);
		}else if ( btMac != null && btMac.isEmpty() == false ){
			urlString = UrlUtils.getTerminalListUrl(1, 1, 10, null, null, null, null, btMac, null);
		}
    	Log.d("terminal list helper", "URL:" + urlString);
    	
      if (isShowDialog) {
    	  progressDialog = ProgressDialog.show(context, "", "...Loading...");
	  }else{
		  progressDialog= new ProgressDialog(context);
	  }
      
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                    Request.Method.GET, urlString, null, new Listener<JSONObject>() {  
                        @Override  
                        public void onResponse(JSONObject response) {  
//                            Log.d(TAG, "response="+response);  
                            if (progressDialog != null && progressDialog.isShowing() ) {  
                                progressDialog.dismiss();  
                            }
    						try {
    							TerminalListBean bean = TerminalListBeanParser.parseTerminalListBean(response);
    							if (bean == null || bean.datas == null || bean.datas.size() == 0) {								
    								String text = "未查询到终端，可能您无权管理这台终端";
    								if (bean == null) {
    									text = "查询终端失败";
    								}
    								tResponse.onFailed(text);
//    								Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    								
    							}
    							
    							if (bean.datas.size() >= 1) {
    								String text = "查询到" + bean.datas.size() + "个终端";
    								tResponse.onSuccess(text,bean.datas.get(0));
//    								Toast.makeText(context, "查询到" + bean.datas.size() + "个终端", Toast.LENGTH_SHORT).show();
    							}
    						} catch (JSONException e) {
    							e.printStackTrace();
    						}                        
                        }  
                    },   
                    new ErrorListener() {  
                        @Override  
                        public void onErrorResponse(VolleyError arg0) {  
                        	
//                        	Toast.makeText(context, "网络连接失败!", Toast.LENGTH_LONG).show();
                        	if (progressDialog.isShowing() && progressDialog != null) {  
                                progressDialog.dismiss();  
                            }
                        	tResponse.onError("网络连接失败!");
                        }  
                    });  
            
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest); 
    }
	
	private void showProgressDialog(){
		mWaitDialog = new Dialog(mActivity,R.style.new_circle_progress);    
		mWaitDialog.setContentView(R.layout.dialog_wait);    
		mWaitDialog.show();
	}
}
