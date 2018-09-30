package com.routon.inforelease.usercontrol;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.routon.common.BaseFragment;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.R.array;
import com.routon.inforelease.R.drawable;
import com.routon.inforelease.R.id;
import com.routon.inforelease.R.layout;
import com.routon.inforelease.R.string;
import com.routon.inforelease.R.style;
import com.routon.inforelease.json.UserListBean;
import com.routon.inforelease.json.UserListBeanParser;
import com.routon.inforelease.json.UserListdatasBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

/**
 * 用户列表
 * @author xiaolp
 *
 */
public class UserListFragment extends BaseFragment implements AbsListView.OnScrollListener{
	private PullToRefreshListView mListView;
	//底部加载更多布局
	private View footer;
	//ListView总共显示多少条
	private int totalItemCount;
 
	//ListView最后的item项
	private int lastItem;
 
	//用于判断当前是否在加载
	private boolean isLoading;
	private int m_iPage;
	private List<UserListdatasBean> mData;
	private UserAdapter mUserAdapter;
//	private static final String TAG = "UserList";
	private static final int MSG_GET_USERLIST = 0;
	private static final int MSG_DEL_USER = 1;

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		hideProgressDialog();
		if( handler != null ){
			handler.removeCallbacksAndMessages(null);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_userlist, container, false);
	}
	
	Handler handler = new Handler(){
		 @Override
       public void handleMessage(Message msg) {
	      
	      if( UserListFragment.this.getContext() == null ) return;
	      hideProgressDialog();
	      mListView.onRefreshComplete();
	      if( m_iPage == 0 ){//获取到的第一页数据
			  mData.clear();
			  mUserAdapter.notifyDataSetChanged();
		  }
	      
	      if( msg.arg2 != 0 ){//获取网络数据过程中出错    	  
	    	  if( InfoReleaseApplication.showNetWorkFailed(UserListFragment.this.getContext()) == true ){
    			  InfoReleaseApplication.showNetDataFailedTip(UserListFragment.this.getContext()); 
    		  }
	    	  return;
	      }else{
	          if( msg.arg1 == MSG_GET_USERLIST ){//get user lisUserListdatasBeant answer	        	 
	        	  if( msg.obj != null && msg.obj instanceof String ){
		        	  UserListBean bean = UserListBeanParser.parseUserListBean(msg.obj.toString());
		        	  Log.d("User list fragment","get terminal list "+msg.obj.toString());
		        	  if( bean != null ){    
//		        		  if( bean.info != null ){
		        			  if( bean.code == 0  ){//获取数据成功  
		        				  if( bean.datas != null && bean.datas.size() > 0 ){
						        	  m_iPage = bean.page;
//						        	  Log.d(TAG,"terminal list fragment handleMessage bean size:"+bean.datas.size()+",m_iPage:"+m_iPage);
						        	  mData.addAll(bean.datas);
						        	  mUserAdapter.notifyDataSetChanged();
						        	  return;
		        				  }else{
		        					  return;
		        				  }
		        			  }else if( bean.code == -2 ){
		        				  InfoReleaseApplication.returnToLogin(UserListFragment.this.getActivity());
		              	  		  return;
		              	  	  }else{
		        				  Toast.makeText(UserListFragment.this.getActivity(), bean.msg, Toast.LENGTH_LONG).show();
		        				  return;
		        			  }
//		        		  }
		        	  }
	        	  }
	          }else if( msg.arg1 == MSG_DEL_USER ){
	        	  if( msg.obj != null  && msg.obj instanceof String ){
            	  		BaseBean bean = BaseBeanParser.parseBaseBean(msg.obj.toString());
            	  		if( bean == null ){
            	  			Toast.makeText(UserListFragment.this.getActivity(), R.string.del_user_failed, Toast.LENGTH_LONG).show();
            	  			return;
            	  		}else if( bean.code == 0 ){//删除用户成功    
            	  			mData.remove(mSelIndex);
            	  			mUserAdapter.notifyDataSetChanged();
            	  			Toast.makeText(UserListFragment.this.getActivity(), R.string.del_user_success, Toast.LENGTH_LONG).show();
            	  			return;
            	  		}else if( bean.code == -2 ){
            	  			InfoReleaseApplication.returnToLogin(UserListFragment.this.getActivity());
            	  			return;
            	  		}else{//删除用户失败提示信息
            	  			Toast.makeText(UserListFragment.this.getActivity(), bean.msg, Toast.LENGTH_LONG).show();
            	  			return;
            	  		}
            	  	} 
	          }
	      }
	      InfoReleaseApplication.showNetDataFailedTip(UserListFragment.this.getContext()); 
		}
	};
	
	private void getUserList(int page){
//		Log.d(TAG,"getTerminalList page:"+page);
		String flagId = null;
		//为了防止分页列表重复查到前一页的数据，特此加上此参数。
		//当客户端请求分页列表数据时，该参数传上一次请求数据中最后一个数据的createtime.(取第一页数据不需要此参数)
		if( page > 1 ){
			flagId = String.valueOf(mData.get(mData.size()-1).userid);
//			Log.d(TAG,"flagId:"+flagId);
		}
		HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getUserListUrl(page, 100, flagId), handler, MSG_GET_USERLIST);
	}
	
	private void reload(){
		//获取第一页数据
		m_iPage = 0;
		getUserList(1);
	}
	
	private int mSelIndex = -1;

	
	@Override  
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		Log.d(TAG,"on activity created");
			
		this.setTitleNextImageBtnClickListener(R.drawable.ic_add, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//create new user 
				Intent intent = new Intent();
				intent.setClass(UserListFragment.this.getActivity(), UserEditActivity.class);
				startActivity(intent);
			}
		});
		this.initTitleBar(R.string.user_list_title);
		
		initListView();
	    m_iPage = 0;
	    
	    boolean netWorkState = InfoReleaseApplication.showNetWorkFailed(this.getContext());
	    if( netWorkState == true ){
	    	showProgressDialog();
		    //获取第一页数据
		    getUserList(1);
	    }
	    
	    mData = new ArrayList<UserListdatasBean>();
	    
	    registerUserBroadcast();
	   
	}
	
	private void initListView(){
		//下拉刷新，上拉加载
		mListView = (PullToRefreshListView)getView().findViewById(R.id.listview);
		
		// 下拉刷新时的提示文本设置  
		mListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");  
		mListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在更新...");  
		mListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");  
		// 上拉加载更多时的提示文本设置  
		mListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载");  
		mListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");  
		mListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");  
		

		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {  
            @Override  
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {  
                String label = DateUtils.formatDateTime(UserListFragment.this.getActivity().getApplicationContext(), System.currentTimeMillis(),  
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);  
  
                // Update the LastUpdatedLabel  
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);  
                reload();
            }  
        });  
		
		mListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener(){
			@Override
			public void onLastItemVisible() {
				// TODO Auto-generated method stub
				getUserList(m_iPage+1);
//				Log.d(TAG,"pull and refresh listview last item visible listener");
			}
		});
		mData = new ArrayList<UserListdatasBean>();
		mUserAdapter = new UserAdapter(this.getContext());
		mListView.setAdapter(mUserAdapter);
		ListView actualListView = mListView.getRefreshableView();  
        actualListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(UserListFragment.this.getActivity(), UserEditActivity.class);
				intent.putExtra("detail",mData.get(position-1));
				mSelIndex = position - 1;
				startActivity(intent);
			}		
		});
        
        actualListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				showExtraActionDlg(position);
				mSelIndex = position-1;
				return true;
			}
		});
	}
	
	
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		if( mReceiver != null ){
			this.getActivity().unregisterReceiver(mReceiver);  
		}
	}
	
	private BroadcastReceiver mReceiver = null;

	private void registerUserBroadcast(){
		IntentFilter intentFilter = new IntentFilter();  
	    intentFilter.addAction("addUser");  
	    intentFilter.addAction("modifyUser");
	    mReceiver = new BroadcastReceiver() {  
	      
	        @Override  
	        public void onReceive(Context context, Intent intent) {  
	        	String action = intent.getAction();
	        	if( action.equals("addUser")){
		            UserListdatasBean bean = (UserListdatasBean) (intent.getSerializableExtra("user"));
		            mData.add(0, bean);
		            mUserAdapter.notifyDataSetChanged();
	        	}else if( action.equals("modifyUser")){
	        		UserListdatasBean beforeBean = mData.get(mSelIndex);
	        		UserListdatasBean afterbean = (UserListdatasBean) (intent.getSerializableExtra("user"));
	        		beforeBean.username = afterbean.username;
	        		beforeBean.realname = afterbean.realname;
	        		beforeBean.phonenum = afterbean.phonenum;
	        		beforeBean.email = afterbean.email;
	        		beforeBean.address = afterbean.address;
	        		beforeBean.groupids = afterbean.groupids;
	        		mUserAdapter.notifyDataSetChanged();
	        		if( InfoReleaseApplication.authenobjData.userId == afterbean.userid ){
	        			InfoReleaseApplication.authenobjData.userName =  afterbean.username;
	        			InfoReleaseApplication.authenobjData.realName = afterbean.realname;
	        			InfoReleaseApplication.authenobjData.phoneNum = afterbean.phonenum;
	        			InfoReleaseApplication.authenobjData.email = afterbean.email;
	        			InfoReleaseApplication.authenobjData.address = afterbean.address;
	        		}
	        	}
	        }  
	      
	    };  
	    this.getActivity().registerReceiver(mReceiver, intentFilter);  
	}
	
	 public void loadComplete() {
	    isLoading = false;
	    footer.setVisibility(View.GONE);	 
	 }
	 
	  /**
	   * 监听可见界面的情况
	   *
	   * @param view       ListView
	   * @param firstVisibleItem 第一个可见的 item 的索引
	   * @param visibleItemCount 可以显示的 item的条数
	   * @param totalItemCount  总共有多少个 item
	   */
	  @Override
	  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	    //当可见界面的第一个item + 当前界面多有可见的界面个数就可以得到最后一个item项了
	    lastItem = firstVisibleItem + visibleItemCount;
	    //总listView的item个数
	    this.totalItemCount = totalItemCount;
	  }
	
	 /**
	   * 滑动状态变化
	   *
	   * @param view
	   * @param scrollState 1 SCROLL_STATE_TOUCH_SCROLL是拖动  2 SCROLL_STATE_FLING是惯性滑动 0SCROLL_STATE_IDLE是停止 , 只有当在不同状态间切换的时候才会执行
	   */
	  @Override
	  public void onScrollStateChanged(AbsListView view, int scrollState) {
	    //如果数据没有加载，并且滑动状态是停止的，而且到达了最后一个item项
	    if (!isLoading && lastItem == totalItemCount && scrollState == SCROLL_STATE_IDLE) {
	      //显示加载更多
	      footer.setVisibility(View.VISIBLE);
	    }
	  }
	  
	//在外面先定义，ViewHolder静态类
  	static class ViewHolder
  	{
  	    public TextView tv_name;
  	    public TextView tv_realname;
  	    public TextView tv_phone;
  	}
	  
  	public class UserAdapter extends BaseAdapter
    {   
	    private LayoutInflater mInflater = null;
	    private Context mContext = null;
	    private static final boolean mTest = false;	      	      
	    	
        public UserAdapter(Context context)
        {
            this.mInflater = LayoutInflater.from(context);
            mContext = context;
        }
	      
	    @Override
	    public int getCount() {
	       //How many items are in the data set represented by this Adapter.
	       //在此适配器中所代表的数据集中的条目数
	       
//	    	    Log.d(TAG,"getCount mData:"+mData.size());
      		if( mData == null ) return 0;
      	
      		return mData.size();
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
	        if( mData.size() == 0 ){
	        	return null;
	        }
	        if(convertView == null)
	        {
	            holder = new ViewHolder();
	            //根据自定义的Item布局加载布局
	            convertView = mInflater.inflate(R.layout.item_terminal, null);
	            holder.tv_name = (TextView)convertView.findViewById(R.id.tv_terminal_id);
	            holder.tv_realname = (TextView)convertView.findViewById(R.id.tv_terminal_team);
	            holder.tv_phone = (TextView)convertView.findViewById(R.id.tv_terminal_area);
	              
	            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
	            convertView.setTag(holder);
	        }else{
	            holder = (ViewHolder)convertView.getTag();
	        }
	      
//        	Log.d(TAG,"getView position:"+position);
          	UserListdatasBean itemData = mData.get(position);
          	holder.tv_name.setText(mContext.getResources().getString(R.string.username)+":"
          			+itemData.username);
   	        holder.tv_realname.setText(mContext.getResources().getString(R.string.realname)+":"
   	        		+itemData.realname);
   	        holder.tv_phone.setText(mContext.getResources().getString(R.string.phonenum)+":"
   	        		+itemData.phonenum);     
	        return convertView;
	    }                                                     
    }
  	
  	private void delUser(){
  		String url = UrlUtils.getUserDelUrl(String.valueOf(mData.get(mSelIndex).userid));
  		showProgressDialog();
  		HttpClientDownloader.getInstance().getResultFromUrlWithSession(url,handler,MSG_DEL_USER);
  	}
	
  	private void showExtraActionDlg(final int position) {
		AlertDialog dlg = new AlertDialog.Builder(this.getContext()).setItems(R.array.userlist_extra_action_array, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: {//删除
					new AlertDialog.Builder(UserListFragment.this.getContext()).setMessage(R.string.delete_confirm_user_msg)
					.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {//确认删除
							// TODO Auto-generated method stub
							delUser();
						}
					}).setNegativeButton(R.string.cancel,null).create().show();
					}
					break;
					
				case 1:
					break;
					
				case 2:
					
					break;
				}
			}
		}).create();
		dlg.show();
	}
  	
}
