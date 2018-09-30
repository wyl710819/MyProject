package com.routon.inforelease;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.routon.common.BaseFragment;
import com.routon.inforelease.json.TerminalListBean;
import com.routon.inforelease.json.TerminalListBeanParser;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;

/**
 * 终端列表界面
 * @author xiaolp
 *
 */
public class TerminalListFragment extends BaseFragment{
	
	private static final int REQUEST_QRCODE_SCAN = 2;
	
	private TextView tv_all;
	private TextView tv_online;
	private TextView tv_offline;
	private PullToRefreshListView lv_terminal;
	
	private TerminalListHelper mTerminalHelper;

	private static final String TAG = "terminalList";
	
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//		Log.d(TAG,"onCreateView");
		return inflater.inflate(R.layout.fragment_terminallist, container, false);
	}
	
	private void initView(int index){
		tv_all.setTextColor(this.getResources().getColor(R.color.gray));
		tv_online.setTextColor(this.getResources().getColor(R.color.gray));
		tv_offline.setTextColor(this.getResources().getColor(R.color.gray));
		if( index == 0 ){
			tv_all.setTextColor(this.getResources().getColor(R.color.blue));
		}else if( index == 1 ){
			tv_online.setTextColor(this.getResources().getColor(R.color.blue));
		}else{
			tv_offline.setTextColor(this.getResources().getColor(R.color.blue));
		}	
	}

	@Override  
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		tv_all =(TextView) getView().findViewById(R.id.tv_all);
		tv_online =(TextView) getView().findViewById(R.id.tv_online);
		tv_offline =(TextView) getView().findViewById(R.id.tv_offline);
		

		this.setTitleNextImageBtnClickListener(R.drawable.ic_scan, new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(getOwnActivity(), ScannerActivity.class);
				startActivityForResult(intent, REQUEST_QRCODE_SCAN);
			}
		});

		this.initTitleBar(R.string.terminal_list_title);
		initView(0);
		
		View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( v == tv_all ){
					mTerminalHelper.setTerminalStateType(TerminalListHelper.ALL_TERMINAL);
					initView(0);
				}else if( v == tv_online ){
					mTerminalHelper.setTerminalStateType(TerminalListHelper.ONLINE_TERMINAL);
					initView(1);
				}else{
					mTerminalHelper.setTerminalStateType(TerminalListHelper.OFFLINE_TERMINAL);
					initView(2);
				}
				
				mTerminalHelper.reload();
			}
		}; 
		tv_all.setOnClickListener(listener);
		tv_online.setOnClickListener(listener);
		tv_offline.setOnClickListener(listener);
			
		lv_terminal = (PullToRefreshListView)getView().findViewById(R.id.lv_terminal);
		TerminalAdapter adapter = new TerminalAdapter(this.getActivity());
		lv_terminal.setAdapter(adapter);
		
		mTerminalHelper = new TerminalListHelper(lv_terminal,adapter,this.getActivity());
		mTerminalHelper.setOnTerminalListItemClickListener(new TerminalListHelper.onTerminalListItemClickListener() {
			
			@Override
			public void onTerminalListItemClick(int position, TerminalListdatasBean data) {
				showTerminalDetail(data);
			}
		});
	    
	    boolean netWorkState = InfoReleaseApplication.showNetWorkFailed(this.getContext());
	    if( netWorkState == true ){
	    	mTerminalHelper.reload();
	    }	 
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_QRCODE_SCAN) {
        	if (resultCode == Activity.RESULT_OK) {
        		String mac = data.getStringExtra("blueMac");
        		String terminalId = data.getStringExtra("termId");
        		

        		Log.v(TAG, "onQRCodeScan: " + mac+",terminalId:"+terminalId);

        		requestTerminalInfoWidthBtMac(mac,terminalId);
        	}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if( mTerminalHelper != null ){
			mTerminalHelper.cancelAllHandlerMsg();
		}
	}
	
	//在外面先定义，ViewHolder静态类
  	static class ViewHolder
  	{
  	    public TextView tv_id;
  	    public TextView tv_team;
  	    public TextView tv_area;
  	    public TextView tv_online;
  	}
	
	public class TerminalAdapter extends BaseAdapter
    {   
	    private LayoutInflater mInflater = null;
	    private Context mContext = null;    	      
	    	
        public TerminalAdapter(Context context)
        {
            this.mInflater = LayoutInflater.from(context);
            mContext = context;
        }
	      
	    @Override
	    public int getCount() {
	       //How many items are in the data set represented by this Adapter.
	       //在此适配器中所代表的数据集中的条目数
	    	if( mTerminalHelper == null ) return 0;
      		return mTerminalHelper.getDataSize();
	    }
	    
	    @Override
	    public Object getItem(int position) {
	          // Get the data item associated with the specified position in the data set.
	          //获取数据集中与指定索引对应的数据项
	        return position;
	    }
	    
	    @Override
	    public long getItemId(int position) {
	          //Get the row id associated with the specified position in the list.
	          //获取在列表中与指定索引对应的行id
	          return position;
	    }
	                                                      
	      //Get a View that displays the data at the specified position in the data set.
	      //获取一个在数据集中指定索引的视图来显示数据
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ViewHolder holder = null;
//	        Log.d(TAG,"getView position:"+position);
	        //如果缓存convertView为空，则需要创建View
	        if( mTerminalHelper == null || mTerminalHelper.getDataSize() == 0 ){
	        	return null;
	        }
	        if(convertView == null)
	        {
	            holder = new ViewHolder();
	            //根据自定义的Item布局加载布局
	            convertView = mInflater.inflate(R.layout.item_terminal, null);
	            holder.tv_id = (TextView)convertView.findViewById(R.id.tv_terminal_id);
	            holder.tv_team = (TextView)convertView.findViewById(R.id.tv_terminal_team);
	            holder.tv_area = (TextView)convertView.findViewById(R.id.tv_terminal_area);
	            holder.tv_online = (TextView)convertView.findViewById(R.id.tv_terminal_linestate);
	              
	            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
	            convertView.setTag(holder);
	        }else{
	            holder = (ViewHolder)convertView.getTag();
	        }

//        	Log.d(TAG,"getView position:"+position);
          	TerminalListdatasBean itemData = mTerminalHelper.getTerminalData(position);
          	holder.tv_id.setText(mContext.getResources().getString(R.string.terminal_id)+":"
          			+itemData.terminalid);
   	        holder.tv_team.setText(mContext.getResources().getString(R.string.group_name)+":"
   	        		+itemData.bsgroup);
   	        holder.tv_area.setText(mContext.getResources().getString(R.string.install_space)+":"
   	        		+itemData.installplace);  
   	        holder.tv_online.setText( itemData.olstate );
   	        
          	if(  itemData.txtTerminalState == 1 ){//在线
    	        holder.tv_online.setTextColor(mContext.getResources().getColor(R.color.black));
	        }else{//离线
	        	holder.tv_online.setTextColor(mContext.getResources().getColor(R.color.red));
	        	
	        }

	        return convertView;
	    }                                                     
    }
	
	private void requestTerminalInfoWidthBtMac(String btMac,String terminalId) {
		String urlString = null;
		//优先查询terminalId
		if ( terminalId != null && terminalId.isEmpty() == false ) {
			urlString = UrlUtils.getTerminalListUrl(1, 1, 10, null, terminalId, null, null, null, null);
		}else if ( btMac != null && btMac.isEmpty() == false ){
			urlString = UrlUtils.getTerminalListUrl(1, 1, 10, null, null, null, null, btMac, null);
		}
		Log.d(TAG, "URL:" + urlString);
  
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", "...Loading...");  
  
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
//                        Log.d(TAG, "response="+response);  
                        if (progressDialog.isShowing() && progressDialog != null) {  
                            progressDialog.dismiss();  
                        }
						try {
							TerminalListBean bean = TerminalListBeanParser.parseTerminalListBean(response);
							if (bean == null || bean.datas == null || bean.datas.size() == 0) {								
								String text = "未查询到终端，可能您无权管理这台终端";
								if (bean == null) {
									text = "查询终端失败";
								}
								Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
								return;
							}
							
							if (bean.datas.size() > 1) {
								Toast.makeText(getContext(), "查询到" + bean.datas.size() + "个终端", Toast.LENGTH_SHORT).show();
							}
							showTerminalDetail(bean.datas.get(0));
						} catch (JSONException e) {
							e.printStackTrace();
						}                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
//                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(getContext(), "网络连接失败!", Toast.LENGTH_LONG).show();
                    	if (progressDialog.isShowing() && progressDialog != null) {  
                            progressDialog.dismiss();  
                        }  
                    }  
                });  
        
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest); 
	}
	
	public void showTerminalDetail(TerminalListdatasBean data) {
		Intent intent = new Intent();
		intent.setClass(getOwnActivity(), TerminalDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable(TerminalDetailActivity.DETAIL_TAG,data);  
		intent.putExtras(bundle);  
		startActivity(intent);
	}
}
