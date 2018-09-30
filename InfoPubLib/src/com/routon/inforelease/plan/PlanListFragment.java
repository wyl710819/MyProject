package com.routon.inforelease.plan;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.routon.ad.element.TemplateInfo;
import com.routon.common.BaseFragment;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.classinfo.ClassInfoListFragment;
import com.routon.inforelease.classinfo.ClassInfoZipDownListener;
import com.routon.inforelease.classinfo.ClassPictureEditActivity;
import com.routon.inforelease.json.PlanListBean;
import com.routon.inforelease.json.PlanListBeanParser;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.json.PlanMaterialparamsBean;
import com.routon.inforelease.json.PlanMaterialrowsBean;
import com.routon.inforelease.json.SendResultBean;
import com.routon.inforelease.json.SendResultBeanParser;
import com.routon.inforelease.json.SendplayBean;
import com.routon.inforelease.json.SendplayBeanParser;
import com.routon.inforelease.net.NetWorkRequest;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.offline.OfflinePackageMgr;
import com.routon.inforelease.offline.OfflineReleaseTools;
import com.routon.inforelease.plan.adapter.PlanListAdapter;
import com.routon.inforelease.plan.create.PictureSelectActivity;
import com.routon.inforelease.plan.create.PublishActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.inforelease.util.TemplatePkgTool;
import com.routon.inforelease.widget.PopupList;
import com.routon.inforelease.widget.SelectTabWidget;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;


public class PlanListFragment extends BaseFragment {

	private static final String TAG = "PlanListFragment";
	
	private PullToRefreshListView mPlanListView;
	//在线节目
	private List<PlanListrowsBean> mPlanListrowsBeanOnlineList = new ArrayList<PlanListrowsBean>();
	//离线节目
	private List<PlanListrowsBean> mPlanListrowsBeanOfflineList = new ArrayList<PlanListrowsBean>();
	//显示节目
	private List<PlanListrowsBean> mPlanListrowsBeanList = new ArrayList<PlanListrowsBean>();
	
	//是否是终端内容列表界面
	private String mTerminalId;
	private boolean mIsTerminalPlanList;
	
	//add by xiaolp 20170816 
	//兼容显示内容的多个版本
	//只显示离线内容
	public static final int TYPE_SHOW_OFFLINE_CONTENT = 0;
	//只显示在线内容
	public static final int TYPE_SHOW_ONLINE_CONTENT = 1;
	//显示在线内容和离线内容
	public static final int TYPE_SHOW_ALL_CONTENT = 2;
	//内容显示类型
	private int mContentShowType = TYPE_SHOW_ALL_CONTENT;
	
	//starts from 1
	private int mCurPage = 1;
	private int mPageCount = 0;
	private PlanListAdapter mAdapter;
//	private PlanMaterialBean mPlanSubtitleBean;
//	private List<PlanMaterialrowsBean> mSubtitleList;
//	private ArrayList<String> testList;

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_planlist, container, false);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle args = this.getArguments();
		if (args != null) {
			mTerminalId = args.getString("id");
			if (mTerminalId != null && mTerminalId.length() > 0) {
				mIsTerminalPlanList = true;
			}
		}
		Log.v(TAG, "mTerminalId: " + mTerminalId);
		
		if( mContentShowType == TYPE_SHOW_OFFLINE_CONTENT ){
			getOfflineData();
		}else if( mContentShowType == TYPE_SHOW_ONLINE_CONTENT ){
			getOnlineData();
		}else{
			getOnlineData();
		}	
		initViews(getView());		
	}
	
	private void getOnlineData(){
		mPlanListrowsBeanList = mPlanListrowsBeanOnlineList;
		mCurPage = 1;
		getPlanList(mCurPage,ProgressDialog.show(getContext(), "", "...Loading..."));	
	}
	
	private void getOfflineData(){
		mPlanListrowsBeanOfflineList.clear();
		List<PlanListrowsBean> beans = new OfflinePackageMgr(getContext()).getPlanList();
		if( mTerminalId != null ){
			for( PlanListrowsBean bean:beans){
				Log.v(TAG, "bean.terminalIDs: " + bean.terminalIDs);
				if( bean.terminalIDs != null && bean.terminalIDs.contains(mTerminalId) == true ){
					mPlanListrowsBeanOfflineList.add(bean);
				}
			}
		}else{			
			mPlanListrowsBeanOfflineList.addAll(beans);
		}
		
		OfflinePackageMgr offlinePkgMgr = new OfflinePackageMgr(this.getContext());
		for( int i = 0; i < mPlanListrowsBeanOfflineList.size(); i++ ){
			PlanListrowsBean bean = mPlanListrowsBeanOfflineList.get(i);
			bean.materialList = offlinePkgMgr.getPlanMaterial(bean.name, 0).rows;
			bean.subTitleList = offlinePkgMgr.getPlanMaterial(bean.name, 1).rows;
		}
		
		if( mPlanListrowsBeanOfflineList.size() == 0 ){
			this.reportToast(R.string.no_ads);
		}
		mPlanListrowsBeanList = mPlanListrowsBeanOfflineList;
	}
	
	
	private void reload() {
		
		if( mPlanListrowsBeanList ==  mPlanListrowsBeanOnlineList ){//在线数据加载
			mCurPage = 1;
			getPlanList(mCurPage,ProgressDialog.show(getContext(), "", "...Loading..."));
		}else{//离线数据加载
			mPlanListrowsBeanList.clear();
			mPlanListrowsBeanList.addAll(new OfflinePackageMgr(getContext()).getPlanList());
			OfflinePackageMgr offlinePkgMgr = new OfflinePackageMgr(this.getContext());
			for( int i = 0; i < mPlanListrowsBeanOfflineList.size(); i++ ){
				PlanListrowsBean bean = mPlanListrowsBeanOfflineList.get(i);
				bean.materialList = offlinePkgMgr.getPlanMaterial(bean.name, 0).rows;
				bean.subTitleList = offlinePkgMgr.getPlanMaterial(bean.name, 1).rows;
			}
			mAdapter.setDatas(mPlanListrowsBeanList);
			mPlanListView.onRefreshComplete();	
		}
	}
	
	private void initAdapter(){
		mAdapter = new PlanListAdapter(getContext(), mPlanListrowsBeanList);
		mAdapter.setOnItemClickClickListener(new PlanListAdapter.OnItemClickListener() {
			
			@Override
			public void onItemClickListener(View v, int position) {
				// TODO Auto-generated method stub
				if (mIsTerminalPlanList) {//节目内容列表界面点击，直接跳转到预览界面
					startPlanPreviewActivity(position);	
				}else{
					showPopupList(v,position);
				}
			}
		});
		
		mPlanListView.setAdapter(mAdapter);
	}

	private SelectTabWidget mSelWidget;
	private void initViews(View view) {
		mPlanListView = (PullToRefreshListView) view.findViewById(R.id.list_plan);
		mSelWidget= (SelectTabWidget) view.findViewById(R.id.sel_widget);
		if( mContentShowType == TYPE_SHOW_OFFLINE_CONTENT || mContentShowType == TYPE_SHOW_ONLINE_CONTENT ){
			mSelWidget.setVisibility(View.GONE);
		}else{
			mSelWidget.setVisibility(View.VISIBLE);
			TextView tvOnline=(TextView) mSelWidget.findViewById(R.id.tv_online);
			TextView tvOffline=(TextView) mSelWidget.findViewById(R.id.tv_offline);
			tvOnline.setText("在线内容");
			tvOffline.setText("本地内容");
			mSelWidget.setOnSelClickListener(new SelectTabWidget.SelClickListener() {
				
				@Override
				public void onClick(int index) {
					// TODO Auto-generated method stub
					if( index == SelectTabWidget.ONLINE_INDEX ){
						//在线		
						if ( mIsTerminalPlanList == false ){
							setTitleNextImageBtnClickListener(R.drawable.add_online, mOnBtnClickedListener);
						}
						getOnlineData();
						mPlanListView.setMode(Mode.BOTH);
					}else if( index == 1 ){
						//离线
						if ( mIsTerminalPlanList == false ){
							setTitleNextImageBtnClickListener(R.drawable.add_offline, mOnBtnClickedListener);
						}
						mPlanListView.onRefreshComplete();
						mPlanListView.setMode(Mode.DISABLED);
						getOfflineData();
					}
					mPlanListView.onRefreshComplete();
					
					mAdapter.setDatas(mPlanListrowsBeanList);
				}
			});
		}
		initAdapter();
		
		// title		
		this.initTitleBar(R.string.plan_list);
		
		
		if (mIsTerminalPlanList) {//终端内容列表界面不可添加，可返回
			this.setTitleBackBtnClickListener(mOnBtnClickedListener);
		} else {
			if( mPlanListrowsBeanList  == mPlanListrowsBeanOnlineList  ){//在线节目，重新刷新数据，离线节目，不用重新取数据
				this.setTitleNextImageBtnClickListener(R.drawable.add_online, mOnBtnClickedListener);
			}else{
				this.setTitleNextImageBtnClickListener(R.drawable.add_offline, mOnBtnClickedListener);
			}
		}

		if( mPlanListrowsBeanList  == mPlanListrowsBeanOnlineList  ){//在线节目，重新刷新数据，离线节目，不用重新取数据
			mPlanListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
	
				@Override
				public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
					Log.d("planlist","onPullDownToRefresh");
					if( mPlanListrowsBeanList  == mPlanListrowsBeanOnlineList  ){//在线节目，重新刷新数据
							mCurPage = 1;
							getPlanList(mCurPage,null);
							return;
					}	
					Log.d("planlist","onPullDownToRefresh 111");
					//不需要刷新
					mPlanListView.onRefreshComplete();
				}
	
				@Override
				public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
					if( mPlanListrowsBeanList  == mPlanListrowsBeanOnlineList  ){//在线节目，获取下一页数据
						//cur page starts form 1
						if( mCurPage+1 <= mPageCount ){
							getPlanList(++mCurPage,null);
							return;
						}
						else {
							mPlanListView.postDelayed(new Runnable() {
								public void run() {
									mPlanListView.onRefreshComplete();
								}
							},500);
							return;
						}
					}		
					//不需要刷新
					mPlanListView.onRefreshComplete();
				}
				
			});
		}else{
			mPlanListView.setMode(Mode.DISABLED);
		}
		
		mPlanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.v(TAG, "position: " + position);
				showPlanDetail(position - 1);
			}
		});
	}
	
	private void showPlanDetail(int position) {
		PlanListrowsBean bean = mPlanListrowsBeanOfflineList.get(position);
		
		Intent intent = new Intent(this.getContext(), PublishActivity.class);
		intent.putExtra("plan", bean);
		intent.putExtra("start_by", "plan_list");
		if( mSelWidget.getSelIndex() != SelectTabWidget.ONLINE_INDEX ){
			intent.putExtra("start_type", "plan_offline_list");
		}
		
		intent.putExtra("is_from_terminal", mIsTerminalPlanList);
		startActivityForResult(intent, 0);
		
//		 FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
//		 // 开启事务
//		 FragmentTransaction transaction = fragmentManager.beginTransaction();
//		 transaction.hide(this);
//		 PublishFragment fragment = new PublishFragment();
//		 Bundle args = new Bundle();
//		 args.putSerializable("plan", bean);
//		 fragment.setArguments(args);
//		 transaction.setCustomAnimations(R.animator.slide_in_right,R.animator.slide_out_left,R.animator.slide_in_left,
//				 R.animator.slide_out_right); 
//		 transaction.add(R.id.fl_content,fragment);				
//		 transaction.addToBackStack(null);
//		 transaction.commit();
	}
	
//	private void showExtraActionDlg(final int position) {
//		int array = mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX ? R.array.plan_extra_action_array : R.array.plan_extra_action_array_offline;
//		AlertDialog dlg = new AlertDialog.Builder(this.getContext()).setItems(array, new OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				switch (which) {
//				case 0:
//					onDeletePlan(position);
//					break;
//					
//				case 1:
//					generateOfflinePkg(position);					
//					break;
//					
//				case 2:
//					startSNotice(position);
//					break;
//					
//				case 3:
//					startClassInfo(position);
//					break;
//					
////				case 2:
////					onDeletePlan(position);
////					break;
//				}
//			}
//		}).create();
//		dlg.show();
//	}
	
	//add normal plan,enter plan info edit activity
	void onAddPlan() {
		Intent intent  = new Intent(this.getContext(), PlanInfoEditActivity.class);
		if( mPlanListrowsBeanList == mPlanListrowsBeanOnlineList ){
			intent.putExtra(CommonBundleName.OFFLINE_TAG, false);
		}
		intent.putExtra("startBy", PlanInfoEditActivity.ADD_PLAN);
		
		startActivity(intent);
	}
	
	void onPublishPlan() {
		if (!checkPlanSelect(mOnCheckPlanCountIsEqualOne)) {
			Toast.makeText(this.getContext(), "please just select one plan to publish", Toast.LENGTH_SHORT).show();
			return;
		}		
	}
	
	void onThrowPlan() {
		if (!checkPlanSelect(mOnCheckPlanCountIsEqualOne)) {
			Toast.makeText(this.getContext(), "please just select one plan to throw", Toast.LENGTH_SHORT).show();
			return;
		}
	}
	
	private int mOpPosition = -1;
	
	void onDeletePlan(final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("请选择:");
		builder.setMessage("您确认删除选择的内容吗?");
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				mOpPosition = position;		
				PlanListrowsBean data = mPlanListrowsBeanList.get(position);
				if( mPlanListrowsBeanList == mPlanListrowsBeanOfflineList ){//离线数据删除
					new OfflinePackageMgr(getContext()).delete(data.name, OfflinePackageMgr.TYPE_AD);
					deleteTemplateEditInfo(data);
					mPlanListrowsBeanOfflineList.remove(position);
					mAdapter.setDatas(mPlanListrowsBeanOfflineList);
				}else{//在线数据删除		
					//HttpClientDownloader.getInstance().planDel(data.contractId, mHandler, MSG_PLAN_DEL);
					sendDeleteRequest(data.contractId);
				}
				dialog.dismiss();
			}
			
		});
		builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}		
			
		});
		
		final AlertDialog dlg = builder.create();
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		View view = layoutInflater.inflate(R.layout.dialog_title_view, null);
		dlg.setCustomTitle(view);
		dlg.setOnShowListener(new OnShowListener() {
			   private Button neutralBtn ;
			   private Button positiveBtn;
			   @Override
			   public void onShow(DialogInterface dialogInterface) {
			    //设置button文本大小
			    positiveBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
			    neutralBtn = dlg.getButton(DialogInterface.BUTTON_NEUTRAL);
			    positiveBtn.setTextSize(18);
			    neutralBtn.setTextSize(18);
			    dlg.getActionBar();
			   }
			  });
		
		dlg.show();
		
		
	}
	
	private boolean checkPlanSelect(OnCheckPlanSelectListener listener) {
		int check_count = 0;
//		for (PlanListrowsBean data : mPlanListrowsBeanList) {
//			if (data.isChecked()) {
//				check_count++;
//			}
//		}
		Toast.makeText(this.getContext(), "check_count: " + check_count, Toast.LENGTH_SHORT).show();
		
		return listener.checkPlanSelect(check_count);
	}
	
	private interface OnCheckPlanSelectListener {
		boolean checkPlanSelect(int check_count);
	}
	
	private OnCheckPlanSelectListener mOnCheckPlanCountIsEqualOne = new OnCheckPlanSelectListener() {

		@Override
		public boolean checkPlanSelect(int check_count) {
			if (check_count == 1)
				return true;
			return false;
		}
		
	};

	private View.OnClickListener mOnBtnClickedListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if( v.getId() == R.id.next_step ){
				showPopupMenu(v);
				
			}else if( v.getId() == R.id.back_btn ){
				getActivity().finish();
			}
		}
	};
		
	private void onMsgPlanDel(String text) {
		Log.v(TAG, "plan del result: " + text);
		if (text == null) {
			Log.v(TAG, "msg del plan null");
			return;
		}		
		
		BaseBean bean = BaseBeanParser.parseBaseBean(text);
		if( bean == null ){
			Toast.makeText(getContext(), "计划删除失败", Toast.LENGTH_SHORT).show();
			return;
		}
		if ( bean.code != 1 ) {			
			if (bean.code == -2) {
				returnToLogin();
			} else {
				Toast.makeText(getContext(), bean.msg, Toast.LENGTH_SHORT).show();
			}
			return;
		}
		if (mOpPosition >= 0&&mPlanListrowsBeanList==mPlanListrowsBeanOfflineList) {
			mPlanListrowsBeanOfflineList.remove(mOpPosition);
			mOpPosition = -1;
		}
		//mPlanListView.setAdapter(new PlanListAdapter(this.getContext(), mPlanListrowsBeanList));
		mAdapter.notifyDataSetChanged();
		reload();
	}
	
	private static final int MSG_PLAN_DEL = 1;
	private static final int MSG_SEND_RESULT = 2;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if( PlanListFragment.this.getContext() == null ) return;
			switch (msg.what) {
				case MSG_PLAN_DEL:
					onMsgPlanDel((String) msg.obj);
					break;	
				case MSG_SEND_RESULT:
					sendPublishResult((String) msg.obj);
					break;
				}
			}		
	};
	
	protected void returnToLogin() {		
		InfoReleaseApplication.returnToLogin(getActivity());
	}
	
	private void generateOfflinePkg(int position) {
		PlanListrowsBean data = mPlanListrowsBeanOfflineList.get(position);
		if (data.isOffLine()) {
			new OfflineReleaseTools(this.getContext()).startPkgAd(data, InfoReleaseApplication.getClassInfoPrivilege());			
		} else {
			new OfflineReleaseTools(this.getContext(),false).startOfflineCache(data);
		}
	}
	
//	private void startSNotice(int position) {
//		Intent intent = new Intent(this.getContext(), SNoticeListActivity.class);
//		PlanListrowsBean data = mPlanListrowsBeanOfflineList.get(position);
//		if (data.isOffLine()) {
//			intent.putExtra("start_by_type", "offline_plan");
//		} else {
//			intent.putExtra("start_by_type", "online_plan");
//		}
//		startActivity(intent);
//	}
	
//	private void startClassInfo(int position) {
//		Intent intent = new Intent(this.getContext(), ClassInfoListActivity.class);
//		PlanListrowsBean data = mPlanListrowsBeanOfflineList.get(position);
//		if (data.isOffLine()) {
//			intent.putExtra("start_by_type", "offline_plan");
//		} else {
//			intent.putExtra("start_by_type", "online_plan");
//			
//		}
//		startActivity(intent);
//	}
		
	private void getPlanList(final int page,final ProgressDialog progressDialog) {  
		final int pageSize = 100;
		
		String url = "";
		if (mIsTerminalPlanList) {
			url = UrlUtils.getTerminalPlanListUrl(mTerminalId, page, pageSize);
		} else {
			url = UrlUtils.getPlanListUrl(null, page, pageSize);
		}
		
		Log.v(TAG, "plan list url: " + url);
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                        if ( progressDialog != null && progressDialog.isShowing() ) {  
                            progressDialog.dismiss();  
                        }
                		PlanListBean bean = null;
						try {
							bean = PlanListBeanParser.parsePlanListBean(response);
							//获取到第一页数据，在线数据清空，重新处理
							if( page == 1 ){
								mPlanListrowsBeanOnlineList.clear();
								mPageCount = bean.total / pageSize;
								if (bean.total % pageSize != 0) {
									mPageCount += 1;
								}
							}					
							mPlanListrowsBeanOnlineList.addAll(bean.rows);
							//update adapter data
							mAdapter.notifyDataSetChanged();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						//结束界面上拉或者下拉的转圈
						mPlanListView.onRefreshComplete();                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(getContext(), "网络连接失败!", Toast.LENGTH_LONG).show();
                    	if ( progressDialog != null && progressDialog.isShowing() ) {  
                            progressDialog.dismiss();  
                        }  
                    	if( page == 1 ){
							mPlanListrowsBeanOnlineList.clear();
							mAdapter.notifyDataSetChanged();
						}
                    	mPlanListView.onRefreshComplete();
                    }  
                });  
        
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
    }
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		registerRefreshListener();
	}

	@Override
	public void onDestroy() {
		unregisterRefreshListener();
		super.onDestroy();
	}

	public static final String ACTION_PLAN_LIST_CHANGED = "com.routon.inforelease.plan.list.changed";
	public static final String ACTION_PLAN_OFF_CHANGED = "com.routon.inforelease.plan.list.offchanged";
	
	private void registerRefreshListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_PLAN_LIST_CHANGED);
		filter.addAction(ACTION_PLAN_OFF_CHANGED);
		getContext().registerReceiver(mContentChangedListener, filter);
	}
	
	private void unregisterRefreshListener() {
		getContext().unregisterReceiver(mContentChangedListener);
	}
	
	private BroadcastReceiver mContentChangedListener = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_PLAN_LIST_CHANGED)) {
				reload();
			}else if (action.equals(ACTION_PLAN_OFF_CHANGED)) {
				mAdapter.notifyDataSetChanged();
			}
			
		}
	};
	
	public void showPopupList(View anchorView,int contextPosition) {
		mOpPosition = contextPosition;
		ArrayList<String>popupMenuItemList = new ArrayList<String>();
		popupMenuItemList.add("预览"); 
		popupMenuItemList.add(getString(R.string.snotice_edit)); 
		popupMenuItemList.add(getString(R.string.plan_throw)); 
		popupMenuItemList.add(getString(R.string.delete)); 

		PopupList popupList = new PopupList(this.getContext()); 
		popupList.showPopupListWindowAtCenter(anchorView, contextPosition, popupMenuItemList, new PopupList.PopupListListener() {
			 @Override
			 public void onPopupListClick(View contextView, int contextPosition, int position) { 
				 switch (position) {
				 	case 0://预览
				 		startPlanPreviewActivity(contextPosition);
						break;
					case 1://编辑
						startPlanEditActivity(contextView,contextPosition);
						break;						
					case 2://发布
						onRelease(contextPosition);
						break;
					case 3://删除
						onDeletePlan(contextPosition);
						break;
					}
			 }
	
			@Override
			public boolean showPopupList(View adapterView, View contextView,
					int contextPosition) {
				// TODO Auto-generated method stub
				return true;
			} 
		});		
	}
	
	public void showProgressDialog(View anchorView, int contextPosition){
		mWaitDialog = new Dialog(getActivity(), R.style.new_circle_progress);
		int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
		int x = anchorView.getWidth()/2;
		int y = location[1];
        Window window = mWaitDialog.getWindow();
        window.setGravity(Gravity.TOP);
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = y;
        window.setAttributes(param);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mWaitDialog.setContentView(R.layout.dialog_wait);
        mWaitDialog.setCancelable(false);
        mWaitDialog.show();
	}
	
	private void startPlanEditActivity(View contextView,int position){
		final PlanListrowsBean planbean = mPlanListrowsBeanList.get(position);
		if( planbean == null ){
			return;
		}
		
		File templateDir = new File(TemplatePkgTool.getNormalTmeplateDir(PlanListFragment.this.getContext(),
				planbean.templateId, planbean.name));
		Log.d("TemplatePkgTool","templateDir:"+templateDir+",templateDir exits:"+templateDir.exists());
		
		if( mPlanListrowsBeanList == mPlanListrowsBeanOnlineList){
			if( planbean.editPkgUrl.equals("") ){//在线普通内容编辑界面
				startOnLineNormalEdit(planbean);
			}else{//在线模板内容编辑界面
				showProgressDialog(contextView, position);
				final String fileName = "template_"+planbean.contractId+".zip";
				NetWorkRequest.downloadZip(PlanListFragment.this.getContext(),planbean.editPkgUrl, fileName, new ClassInfoZipDownListener() {
					
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						hideProgressDialog();
						startOnLineTemplateEdit(fileName,planbean);
					}
					
					@Override
					public void onError() {
						// TODO Auto-generated method stub
						reportToast("Zip包下载失败");
					}
				});
			}
		}else if( templateDir != null && templateDir.exists() && templateDir.isDirectory() ){//离线模板编辑界面
			startOffLineTemplateEdit(templateDir.getAbsolutePath(),planbean);							
		}else {//离线普通内容编辑界面
			startOffLineNormalEdit(planbean);
		}
	}
	
	private void startOffLineNormalEdit(PlanListrowsBean planbean){
		Intent intent = new Intent(getContext(),PlanInfoEditActivity.class);
		Bundle bundleEdit = new Bundle();
		bundleEdit.putSerializable(CommonBundleName.DATA_TAG, planbean);
		intent.putExtra(PlanInfoEditActivity.PLAN_TYPE, PlanInfoEditActivity.EDIT_PLAN);
		intent.putExtra(CommonBundleName.OFFLINE_TAG, true);
		intent.putExtras(bundleEdit);
		startActivity(intent);
	}
	
	private void startOnLineNormalEdit(PlanListrowsBean planbean){
		Intent intent = new Intent(getContext(),PlanInfoEditActivity.class);
		Bundle bundleEdit = new Bundle();
		bundleEdit.putSerializable(CommonBundleName.DATA_TAG, planbean);
		intent.putExtra(PlanInfoEditActivity.PLAN_TYPE, PlanInfoEditActivity.EDIT_PLAN);	
		intent.putExtra(CommonBundleName.OFFLINE_TAG, false);
		intent.putExtras(bundleEdit);
		startActivity(intent);
	}
	
	private void startOffLineTemplateEdit(String templateDir,PlanListrowsBean planbean){
		TemplateInfo templateinfo = TemplatePkgTool.parseTemplateEditXml(templateDir+"/"+TemplatePkgTool.TEMPLATE_EDIT_INFO_FILE_NAME);
		Intent intent = new Intent(getActivity(),ClassPictureEditActivity.class);
		Bundle editBundle = new Bundle();
		templateinfo.mEditDirPath = templateDir;
		editBundle.putString(CommonBundleName.TEMPLATE_URL_TAG, templateDir+"/"+templateinfo.mTemplate);
		editBundle.putBoolean(CommonBundleName.OFFLINE_TAG, true);
		editBundle.putSerializable(CommonBundleName.TEMPLATE_INFO_TAG, (Serializable)templateinfo);
		editBundle.putSerializable(CommonBundleName.DATA_TAG, planbean);
		intent.putExtras(editBundle);
		startActivity(intent);
	}
	
	//启动在线模板内容编辑界面
	private void startOnLineTemplateEdit(String fileName,PlanListrowsBean planbean){
		String templatePath = getActivity().getFilesDir().getAbsolutePath()+"/"+TemplatePkgTool.TEMPLATE_EDIT_DIR_NAME+"/"+fileName.substring(0, fileName.lastIndexOf("."))+"/";
		File zipFile = new File(getActivity().getFilesDir()+"/"+fileName);
		TemplatePkgTool.unzip(PlanListFragment.this.getContext(),zipFile.getAbsolutePath());
		TemplateInfo templateinfo = TemplatePkgTool.parseTemplateEditXml(templatePath+TemplatePkgTool.TEMPLATE_EDIT_INFO_FILE_NAME);
		templateinfo.mEditDirPath = templatePath;
		Intent intent = new Intent(getActivity(),ClassPictureEditActivity.class);
		Bundle editBundle = new Bundle();
		editBundle.putBoolean(CommonBundleName.OFFLINE_TAG, false);
		editBundle.putSerializable(CommonBundleName.DATA_TAG, planbean);
		editBundle.putInt(CommonBundleName.APP_TYPE_TAG, CommonBundleName.PLAN_APP_TYPE);
		editBundle.putSerializable(CommonBundleName.TEMPLATE_INFO_TAG, (Serializable)templateinfo);
		intent.putExtras(editBundle);
		startActivity(intent);
	}
	
	//启动预览界面
	private void startPlanPreviewActivity(int position){
		 PlanListrowsBean bean = mPlanListrowsBeanList.get(position);	 
		  	
		 List<PlanMaterialrowsBean> materialList = bean.materialList;
		 List<PlanMaterialrowsBean> subtitleList = bean.subTitleList;

		 ArrayList<String> picUrlList = new ArrayList<String>();
		 if ( materialList != null && materialList.size() > 0 ) {
			for (int i = 0; i < materialList.size(); i++) {
					picUrlList.add(materialList.get(i).thumbnail);
			}
		 }	
			
		Intent previewIntent = new Intent(getContext(), PlanPreviewActivity.class);
		Bundle bundle = new Bundle();
		if ( picUrlList.size() == 0 ) {
			picUrlList.add("0");
		}
		bundle.putString(PlanPreviewActivity.INTENTDATA_IMAGE_PATH,picUrlList.get(0) );
		bundle.putStringArrayList(PlanPreviewActivity.INTENTDATA_PICLIST, picUrlList);
		
		//只传第一条滚动文字
		if( subtitleList != null && subtitleList.size() > 0 ){
			PlanMaterialrowsBean titleBean = subtitleList.get(0);
			bundle.putString(PlanPreviewActivity.INTENTDATA_SUBTITLE, titleBean.thumbnail);
			for (PlanMaterialparamsBean param : titleBean.params) {
				bundle.putString(Integer.toString(param.adParamId), param.adParamValue);
			}
		}
		previewIntent.putExtras(bundle);		
		startActivity(previewIntent);
	}
	
	ProgressDialog mProgressDlg;
	private void showSendProgress() {
		if (mProgressDlg == null) {
			mProgressDlg = new ProgressDialog(this.getContext());
			mProgressDlg.setTitle("广告下发");
			mProgressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDlg.setButton(ProgressDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		}
		new Handler().postDelayed(new Runnable() {  
            public void run() {  
            	Button btn = mProgressDlg.getButton(ProgressDialog.BUTTON_POSITIVE);
            	if( btn != null ){
            		btn.setVisibility(View.GONE);
            	}
            }  
        }, 30);  
		
		mProgressDlg.setMessage("正在下发广告");
		mProgressDlg.setProgress(0);
		mProgressDlg.show();
	}
	
	private void updateProgress(int complete, int sum) {
		if (sum <= 0)
			return;
		if (mProgressDlg != null) {
			mProgressDlg.setProgress(complete * 100 / sum);
			mProgressDlg.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
		}
	}
	
	private void sendPublishResult(String sendIdStr){
		int sendId = Integer.parseInt(sendIdStr);
		String urlString = UrlUtils.getSendResultUrl(sendId);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);             
                        try {
                        	SendResultBean bean = SendResultBeanParser.parseSendResultBean(response);
							if( bean == null ){
								return;
							}
							if (bean.code == 0) {
								Log.v(TAG, "sum: " + bean.obj.sum);
								Log.v(TAG, "complete: " + bean.obj.completecount);
								Log.v(TAG, "cancel: " + bean.obj.cancelcount);
								Log.v(TAG, "exception: " + bean.obj.exceptioncount);
								int sum = Integer.parseInt(bean.obj.completecount) + Integer.parseInt(bean.obj.cancelcount) + Integer.parseInt(bean.obj.exceptioncount);
								Log.d(TAG, "sum="+sum);
								updateProgress(Integer.parseInt(bean.obj.completecount), bean.obj.sum);
								if (sum != bean.obj.sum || bean.obj.sum == 0) {
									Message msg = Message.obtain(mHandler, MSG_SEND_RESULT, bean.obj.sendId);
									mHandler.sendMessageDelayed(msg, 1000);
								} else {
									if (mProgressDlg != null) {
										mProgressDlg.setMessage(PlanListFragment.this.getContext().getResources().getString(R.string.publish_finished));
										mProgressDlg.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
									}
									mPlanListrowsBeanOnlineList.get(mOpPosition).published = true;
									mAdapter.notifyDataSetChanged();
								}
								
							} else if( bean.code == -2 ){
								//重新登录，发布过程取消
								InfoReleaseApplication.returnToLogin(PlanListFragment.this.getActivity());
								if (mProgressDlg != null) {
									mProgressDlg.setMessage(PlanListFragment.this.getContext().getResources().getString(R.string.publish_finished));
									mProgressDlg.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
								}
								return;
							} else {
								Toast.makeText(getContext(), "查询发布进度失败", Toast.LENGTH_SHORT).show();
								if (mProgressDlg != null) {
									mProgressDlg.setMessage(PlanListFragment.this.getContext().getResources().getString(R.string.publish_finished));
									mProgressDlg.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {
                    	Toast.makeText(getContext(), "网络连接失败!", Toast.LENGTH_LONG).show();
                    	 
                    }  
                });
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	private void sendDeleteRequest(int id)
	{
		String urlString = UrlUtils.getPlanDelUrl();
//		Log.v(TAG, "planDel url: " + urlStr);
		final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", "...Loading..."); 
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("id", String.valueOf(id)));
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, params, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						if(progressDialog.isShowing())
							progressDialog.dismiss();
						Message msg = Message.obtain(mHandler, MSG_PLAN_DEL,response.toString());
						mHandler.sendMessage(msg);
					}
				}, 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						if(progressDialog.isShowing())
							progressDialog.dismiss();
						reportToast("节目删除失败");
					}
				});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  	
	}
	
	private void sendPublishRequest(final int id){
		Log.v(TAG, "sendPublishRequest: " + id);
		
		showSendProgress();
		
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", "...Loading...");  
  
        String urlString = UrlUtils.getSendPlayUrl(id);
        Log.i(TAG, "URL:" + urlString);
        
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                        if (progressDialog!=null && progressDialog.isShowing()) {  
                            progressDialog.dismiss();  
                        }
                        try {
                        	SendplayBean bean = SendplayBeanParser.parseSendplayBean(response);
							if( bean == null ){
								return;
							}
							if (bean.code == 0) {
								sendPublishResult(bean.obj.sendId);
							} else if( bean.code == -2 ){
								InfoReleaseApplication.returnToLogin(PlanListFragment.this.getActivity());
								return;
							} else {
								Toast.makeText(getContext(), "发布节目信息失败", Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {
                    	Toast.makeText(getContext(), "网络连接失败!", Toast.LENGTH_LONG).show();
                    	if (progressDialog!=null && progressDialog.isShowing()) {  
                            progressDialog.dismiss();  
                        }  
                    }  
                });
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	private void onRelease(final int contextPosition){
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("请选择:");
		builder.setMessage("您确认发布选择的内容吗?");
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if( mPlanListrowsBeanList == mPlanListrowsBeanOnlineList ){//在线发布
					sendPublishRequest(mPlanListrowsBeanOnlineList.get(contextPosition).contractId);
				}else{//离线发布
					new OfflineReleaseTools(getContext()).startPkgAd(mPlanListrowsBeanOfflineList.get(contextPosition), 
						InfoReleaseApplication.getClassInfoPrivilege());
				}
				dialog.dismiss();
			}
			
		});
		builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}		
		});
		
		final AlertDialog dlg = builder.create();
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		View view=layoutInflater.inflate(R.layout.dialog_title_view, null);
		dlg.setCustomTitle(view);
		dlg.setOnShowListener(new OnShowListener() {
			   private Button neutralBtn ;
			   private Button positiveBtn;
			   @Override
			   public void onShow(DialogInterface dialogInterface) {
			    //设置button文本大小
			    positiveBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
			    neutralBtn = dlg.getButton(DialogInterface.BUTTON_NEUTRAL);
			    positiveBtn.setTextSize(18);
			    neutralBtn.setTextSize(18);
			    dlg.getActionBar();
			   }
			  });
		
		dlg.show();
	}
	
	private void showPopupMenu(View v) {
		// TODO Auto-generated method stub
		PopupMenu popup = new PopupMenu(getContext(), v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.class_info_list_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if( item.getItemId() == R.id.btn_class_info_add ){
					onAddPlan();
					
				}else if( item.getItemId() == R.id.btn_class_info_add_template ){
		
					onAddTemplatePlan();
				}
				return false;
			}
		});
		popup.show();
	}
	
	private void onAddTemplatePlan(){
		//使用模版
		Intent intent ;
		intent = new Intent(getContext(), PictureSelectActivity.class);
		intent.putExtra("start_by", "class_info_add");
		if( mPlanListrowsBeanList == mPlanListrowsBeanOnlineList ){//在线内容编辑
			intent.putExtra(CommonBundleName.OFFLINE_TAG, false);
		}else{//离线内容编辑
			intent.putExtra(CommonBundleName.OFFLINE_TAG, true);
		}
		intent.putExtra(CommonBundleName.FILE_TYPE_TAG, MaterialParams.TYPE_CLASS_TEMPLATE);	
		startActivityForResult(intent, 0);
	}
	
	//删除节目时删除对应的模板编辑信息
	private void deleteTemplateEditInfo(PlanListrowsBean data)
	{
		File templateEdtFile=new File(getActivity().getFilesDir().getAbsolutePath()+"/template_edit");
		if(templateEdtFile.exists()&&templateEdtFile.isDirectory())
		{
			File[] templates = templateEdtFile.listFiles();
			for(int i=0;i<templates.length;i++)
			{
				if(templates[i].getName().equals("template_"+data.templateId+"_"+data.name)||templates[i].getName().equals("template_"+data.templateId))
					if(templates[i].isDirectory())
					{
						File[] files=templates[i].listFiles();
						for(int j=0;j<files.length;j++)
						{
							files[j].delete();
						}
						templates[i].delete();
					}
			}
		}
	}
	
}
