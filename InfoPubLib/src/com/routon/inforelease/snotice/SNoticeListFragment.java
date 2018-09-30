package com.routon.inforelease.snotice;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.routon.common.BaseFragment;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.classinfo.ClassInfoListFragment;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.json.SNoticeListBean;
import com.routon.inforelease.json.SNoticeListBeanParser;
import com.routon.inforelease.json.SNoticeListrowsBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.offline.OfflinePackageMgr;
import com.routon.inforelease.plan.adapter.SNoticeListAdapter;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.ContentCheckHelper;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.inforelease.widget.PopupList;
import com.routon.inforelease.widget.SelectTabWidget;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

public class SNoticeListFragment extends BaseFragment {
	private static final String TAG = "SNoticeListActivity";

	private PullToRefreshListView mNoticeListView;

	private List<SNoticeListrowsBean> mSNoticeListDatas = new ArrayList<SNoticeListrowsBean>();
	private List<SNoticeListrowsBean> mSNoticeListOfflineDatas = new ArrayList<SNoticeListrowsBean>();
	private List<SNoticeListrowsBean> mSNoticeListOnlineDatas = new ArrayList<SNoticeListrowsBean>();

	private SNoticeListAdapter mAdapter;

	private int mCurPage = 0;
	private SelectTabWidget mSelWidget;

	private String mTerminalId;
	private boolean mIsTerminalSNoticeList;
	private List<String> mPublishIds = null;
	private boolean mAuditAuthority;

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {		
		View view = inflater.inflate(R.layout.fragment_snotice_list, container, false);
		return view;
	}
	
	@Override  
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle args = this.getArguments();
		if (args != null) {
			mTerminalId = args.getString("id");
			if (mTerminalId != null && mTerminalId.length() > 0) {
				mIsTerminalSNoticeList = true;
			}
			mAuditAuthority = args.getBoolean(CommonBundleName.AuditSchoolNoticeAuthority);
		}
		Log.v(TAG, "mTerminalId: " + mTerminalId);
		mSNoticeListDatas = mSNoticeListOnlineDatas;

		initView(getView());

		mCurPage = 1;
		getSNoticeList(mCurPage, 10, ProgressDialog.show(getContext(), "", "...Loading..."));

		mSNoticeListOfflineDatas.addAll(new OfflinePackageMgr(getContext()).getSNoticeList());
		
		mPublishIds = PublishStateUtils.getSavedNoticeIDs(this.getContext());
//		if( mAdapter != null ){
//			mAdapter.setPublishIds(mPublishIds);
//		}
	}
	
	private int mSelListIndex = 0;
	private void showPopupList(View anchorView,int contextPosition) {
		final List<String> popupMenuItemList = new ArrayList<String>();
		popupMenuItemList.add(getString(R.string.snotice_edit)); 
		if( this.mAuditAuthority == false ){
			popupMenuItemList.add(getString(R.string.plan_throw)); 
		}else{
			popupMenuItemList.add(getString(R.string.pass));
			popupMenuItemList.add(getString(R.string.passnot));
		}
		popupMenuItemList.add(getString(R.string.delete)); 
		
		PopupList popupList = new PopupList(this.getContext()); 
		popupList.showPopupListWindowAtCenter(anchorView, contextPosition, popupMenuItemList, new PopupList.PopupListListener() {
			 @Override
			 public void onPopupListClick(View contextView, int contextPosition, int position) { 
				 String selMenuStr = popupMenuItemList.get(position);
				 if( selMenuStr.equals(getString(R.string.snotice_edit)) ){
						//edit notice
						Intent intent=new Intent(getContext(), SNoticeAddActivity.class);
						SNoticeListrowsBean noticeListrowsbean = mSNoticeListDatas.get(contextPosition-1);
						intent.putExtra("noticeId", noticeListrowsbean.id);
						intent.putExtra(CommonBundleName.AuditSchoolNoticeAuthority, mAuditAuthority);
						intent.putExtra("groupsId", noticeListrowsbean.groups);
						intent.putExtra("noticeText", noticeListrowsbean.notice);
						intent.putExtra("isEditable", mEditable);
						intent.putExtra("startTime", noticeListrowsbean.startTime);
						intent.putExtra("endTime", noticeListrowsbean.endTime);
						startActivity(intent);
				 }else if( selMenuStr.equals(getString(R.string.delete)) ){
						//delete notice
						SNoticeListrowsBean item = mSNoticeListDatas.get(contextPosition-1);
						onDelSelected(String.valueOf(item.id));
				 }else if( selMenuStr.equals(getString(R.string.plan_throw)) ){
						//publish notice
						SNoticeListrowsBean item2 = mSNoticeListDatas.get(contextPosition-1);
						onPublishSelected(String.valueOf(item2.id));
		
				 }else if( selMenuStr.equals(getString(R.string.pass)) ){
					 SNoticeListrowsBean item = mSNoticeListDatas.get(contextPosition-1);
					 auditSNoticeInfo(item);
				 }else if( selMenuStr.equals(getString(R.string.passnot)) ){
					 showProgressDialog();
					 showContentList();
//					 SNoticeListrowsBean item = mSNoticeListDatas.get(contextPosition-1);
//					 auditNoSNotice(item);
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
	
	public void showContentList(){
		ContentCheckHelper.getContentCheckList(this.getOwnActivity(), new DataResponse.Listener<ArrayList<String>>(){

			@Override
			public void onResponse(final ArrayList<String> response) {
				// TODO Auto-generated method stub
				hideProgressDialog();
				ContentCheckHelper.showContentListDialog(getOwnActivity(), response,new DialogInterface.OnClickListener() {
			        @Override
			        public void onClick(DialogInterface dialog, int which) {
			            // which 下标从0开始
			            // ...To-do		
						 SNoticeListrowsBean item = mSNoticeListDatas.get(mSelListIndex);
						 auditNoSNotice(item,response.get(which));
			        }
			    });
			}
			
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				hideProgressDialog();
			}
		});
	}
	
	public void auditNoSNotice(final SNoticeListrowsBean item,String content){
		String urlString = UrlUtils.getAuditSNoticeUrl(String.valueOf(item.id),0,content);
		this.showProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                       SNoticeListFragment.this.hideProgressDialog();
                        try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if( bean.code == 0 ){//审核通过
								mSNoticeListDatas.remove(item);
								Toast.makeText(getOwnActivity(), "滚动字幕审核不通过", Toast.LENGTH_LONG).show();
								mAdapter.setDatas(mSNoticeListDatas);
							}else{
								Toast.makeText(getOwnActivity(), bean.msg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Toast.makeText(getOwnActivity(), "提交审核申请失败", Toast.LENGTH_LONG).show();
						}
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(getOwnActivity(), "提交审核申请失败", Toast.LENGTH_LONG).show();
                    	SNoticeListFragment.this.hideProgressDialog();
                    }  
                });         
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	public void auditSNoticeInfo(final SNoticeListrowsBean item){
		String urlString = UrlUtils.getAuditSNoticeUrl(String.valueOf(item.id),1,null);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                       Log.d(TAG, "response="+response);  
                       SNoticeListFragment.this.hideProgressDialog();
                       try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if( bean.code == 0 ){//审核通过
								mSNoticeListDatas.remove(item);
								mAdapter.setDatas(mSNoticeListDatas);
								Toast.makeText(getOwnActivity(), "滚动字幕审核通过", Toast.LENGTH_LONG).show();
								sendPublishRequestWithoutAnswer(String.valueOf(item.id));
							}else{
								Toast.makeText(getOwnActivity(), bean.msg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Toast.makeText(getOwnActivity(), "提交审核申请失败", Toast.LENGTH_LONG).show();
						}
                                       
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(getOwnActivity(), "提交审核申请失败", Toast.LENGTH_LONG).show();
                    	SNoticeListFragment.this.hideProgressDialog();
                    }  
                });         
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}

	private void initView(View view) {

		mSelWidget = (SelectTabWidget) view.findViewById(R.id.sel_widget);

		mSelWidget.setVisibility(View.INVISIBLE);
		ViewGroup.LayoutParams widgetParams = (ViewGroup.LayoutParams) mSelWidget.getLayoutParams();
		widgetParams.height = 0;
		mSelWidget.setLayoutParams(widgetParams);

		mSelWidget.setOnSelClickListener(new SelectTabWidget.SelClickListener() {

			@Override
			public void onClick(int index) {
				// TODO Auto-generated method stub
				if (index == SelectTabWidget.ONLINE_INDEX) {
					mSNoticeListDatas = mSNoticeListOnlineDatas;
				} else if (index == 1) {
					mSNoticeListOfflineDatas.clear();
					mSNoticeListOfflineDatas.addAll(new OfflinePackageMgr(getContext()).getSNoticeList());
					mSNoticeListDatas = mSNoticeListOfflineDatas;
				}
				mAdapter.clearSelect();
				mAdapter.setDatas(mSNoticeListDatas);
			}
		});

		mNoticeListView = (PullToRefreshListView) view.findViewById(R.id.list_snotice);

		mNoticeListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				if (mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX) {
					mCurPage = 1;
					mSNoticeListDatas.clear();
					mAdapter.clearSelect();
					getSNoticeList(mCurPage, 10, null);
				} else {

					mSNoticeListOfflineDatas.clear();
					mAdapter.clearSelect();
					mSNoticeListOfflineDatas.addAll(new OfflinePackageMgr(getContext()).getSNoticeList());
					mAdapter.setDatas(mSNoticeListOfflineDatas);
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mNoticeListView.onRefreshComplete();
						}
					});
				}
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				if (mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX) {
					getSNoticeList(++mCurPage, 10, null);
				} else {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mNoticeListView.onRefreshComplete();
						}
					});
				}

			}

		});
		mNoticeListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mIsTerminalSNoticeList == false) {
					mSelListIndex = position-1;
					showPopupList(view,position);
					SNoticeListrowsBean item = mSNoticeListDatas.get(mSelListIndex);
					//审核不通过
					if( item != null && item.status == ClassInfoListdatasBean.STATUS_AUDIT_NOTTHROUGH ){
						if( item.attitude != null && item.attitude.isEmpty() == false ){
							String ToastStr = "<font color='#FF0000'>该内容上次发布失败!\n失败原因:"+item.attitude+"</font>";
							Toast.makeText(getOwnActivity(), Html.fromHtml(ToastStr),Toast.LENGTH_LONG).show();//"该内容上次发布失败!\n失败原因:"+item.attitude, Toast.LENGTH_LONG).show();
						}else{
							String ToastStr = "<font color='#FF0000'>该内容上次发布失败!</font>";
							Toast.makeText(getOwnActivity(), Html.fromHtml(ToastStr), Toast.LENGTH_LONG).show();
						}
					}
				}
				
			}
		});
		
		mAdapter = new SNoticeListAdapter(getContext(), mSNoticeListDatas);
		if (mIsTerminalSNoticeList == true) {
			mAdapter.setEditable(false);
			mEditable = false;
		}
		if( mAuditAuthority == true ){
			mAdapter.setIsShowPublishFlag(false);
		}
		mNoticeListView.setAdapter(mAdapter);

		view.findViewById(R.id.btn_snotice_add).setOnClickListener(mOnClickListener);
		view.findViewById(R.id.btn_snotice_edit).setOnClickListener(mOnClickListener);
		view.findViewById(R.id.btn_snotice_del).setOnClickListener(mOnClickListener);

		// init title bar		
		this.initTitleBar("滚动字幕列表");

		if (mIsTerminalSNoticeList) {
			this.setTitleBackBtnClickListener(mOnClickListener);
			mSelWidget.setVisibility(View.GONE);
		} else {
			if( mAuditAuthority == false ){
				this.setTitleNextImageBtnClickListener(R.drawable.ic_add,new OnClickListener() {
	
					@Override
					public void onClick(View v) {
	//					showPopupMenu(v);
						Intent intent = new Intent(getContext(), SNoticeAddActivity.class);
						intent.putExtra("start_by_type", "online_plan");
						startActivity(intent);
	
					}
				});
			}
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (SNoticeListFragment.this.getContext() == null)
				return;

		}

	};

//	private void showExtraActionDlg(final int position) {
//		int array = R.array.class_info_action_array;
//		AlertDialog dlg = new AlertDialog.Builder(getContext()).setItems(array, new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				switch (which) {
//				case 0:
//					
//					Intent intent=new Intent(getContext(), SNoticeAddActivity.class);
//					SNoticeListrowsBean noticeListrowsbean=mSNoticeListDatas.get(position-1);
//					intent.putExtra("noticeId", noticeListrowsbean.id);
//					intent.putExtra("groupsId", noticeListrowsbean.groups);
//					intent.putExtra("noticeText", noticeListrowsbean.notice);
//					intent.putExtra("isEditable", mEditable);
//					intent.putExtra("startTime", noticeListrowsbean.startTime);
//					intent.putExtra("endTime", noticeListrowsbean.endTime);
//					getContext().startActivity(intent);
//
//					break;
//
//				case 1:
//					SNoticeListrowsBean item = mSNoticeListDatas.get(position-1);
//					onDelSelected(String.valueOf(item.id));
//					break;
//
//				case 2:
//					SNoticeListrowsBean item2 = mSNoticeListDatas.get(position-1);
//					onPublishSelected(String.valueOf(item2.id));
//					break;
//				}
//			}
//		}).create();
//		dlg.show();
//	}

	private void getSNoticeList(int page, int pageSize, final ProgressDialog progressDialog) {
		if (pageSize == -1)
			pageSize = 10;

		String urlString = "";
		if (mIsTerminalSNoticeList) {
			urlString = UrlUtils.getSNoticeListUrl(mTerminalId, null, page, pageSize);
		} else {
			if( mAuditAuthority == true ){//获取待审核内容列表
				urlString = UrlUtils.getSNoticeListUrl(null, page, pageSize,2);
			}else{
				String[] classes = InfoReleaseApplication.authenobjData.headTeacherClasses;
				String groupIds = "";
				for(int index = 0; index< classes.length;index++){
					groupIds += classes[index];
					if( index < classes.length - 1 ){
						groupIds += ",";
					}
				}
				urlString = UrlUtils.getSNoticeListUrl(null, page, pageSize,groupIds);
			}
		}

		Log.d(TAG, "URL:" + urlString);

		// final ProgressDialog progressDialog =
		// ProgressDialog.show(getContext(), "", "...Loading...");

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						mNoticeListView.onRefreshComplete();
						SNoticeListBean bean;
						try {
							bean = SNoticeListBeanParser.parseSNoticeListBean(response);
							if (bean == null || bean.rows == null || bean.rows.size() == 0) {
								mNoticeListView.onRefreshComplete();
							}
							if( bean != null && bean.code == -2 ){
								InfoReleaseApplication.returnToLogin(SNoticeListFragment.this.getActivity());
								return;
							}
							if( bean.code != 0 ){
								if( bean.msg != null && bean.msg.isEmpty() == false ){
									Toast.makeText(getOwnActivity(), bean.msg,Toast.LENGTH_LONG).show();
								}else{
									Toast.makeText(getOwnActivity(), "获取数据失败",Toast.LENGTH_LONG).show();
								}
								return;
							}
							mAdapter.notifyDataSetChanged();
							if( bean != null && bean.rows != null ){
								mSNoticeListOnlineDatas.addAll(bean.rows);
							}
							
						} catch (JSONException e) {
							e.printStackTrace();
						}

						
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						if( InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) == true ){
							InfoReleaseApplication.showNetDataFailedTip(getOwnActivity());
						}
						
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}

						mNoticeListView.onRefreshComplete();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void onPublishSelected(final String ids) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("请选择:");
		builder.setMessage("您确认发布选择的内容吗?");
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				sendPublishRequest(ids);
				dialog.dismiss();
			}

		});
		builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}

		});

		AlertDialog dlg = builder.create();
		dlg.show();
	}

	private void sendPublishRequest(final String ids) {
		Log.v(TAG, "class info publish: " + ids);

		final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", "...Loading...");

		String urlString = UrlUtils.getSNoticePublishUrl(ids);
		Log.i(TAG, "URL:" + urlString);

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if (bean.code == 0) {
								if( mSelListIndex >= 0 && mSelListIndex < mSNoticeListDatas.size() ){
									mSNoticeListDatas.get(mSelListIndex).status = 2;
								}
								//发布成功后，保存文件
								mPublishIds.add(ids);
								mAdapter.notifyDataSetChanged();
								PublishStateUtils.writeData(SNoticeListFragment.this.getContext(),PublishStateUtils.NOTICE_SAVE_IDS_FILE, ids);
								reportToast("发布滚动字幕成功");
							} else if (bean.code == -2) {
								 returnToLogin();
							} else {
								if( bean.msg == null || bean.msg.isEmpty() ){
									reportToast("发布滚动字幕失败");
								}else{
									reportToast(bean.msg);
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if( InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) == true ){
							reportToast("发布滚动字幕失败");
						}
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
					}
				});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void sendPublishRequestWithoutAnswer(final String ids) {
		String urlString = UrlUtils.getSNoticePublishUrl(ids);
		Log.i(TAG, "URL:" + urlString);

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
					}
				});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void onDelSelected(final String ids) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("请选择:");
		builder.setMessage("您确认删除选择的内容并取消投放吗?");
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX) {
					sendDeleteRequest(ids);
				} else {

					mSNoticeListOfflineDatas.clear();
					mAdapter.clearSelect();
					mSNoticeListOfflineDatas.addAll(new OfflinePackageMgr(getContext()).getSNoticeList());
					mAdapter.setDatas(mSNoticeListOfflineDatas);
					mNoticeListView.onRefreshComplete();

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
		
		AlertDialog dlg = builder.create();
		dlg.show();

	}

	private void sendDeleteRequest(final String ids) {
		Log.v(TAG, "snoticedel: " + ids);

		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("ids", ids));

		final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", "...Loading...");

		String urlString = UrlUtils.getSNoticeDelUrl();
		Log.i(TAG, "URL:" + urlString);

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if (bean.code == 1) {
								PublishStateUtils.removeData(SNoticeListFragment.this.getContext(), PublishStateUtils.NOTICE_SAVE_IDS_FILE, ids);
								onDeleteSelectedSucess(ids);
								reportToast("删除滚动字幕成功");
							} else if (bean.code == -2) {
								returnToLogin();
							} else {
								if( bean.msg == null || bean.msg.isEmpty() ){
									reportToast("删除滚动字幕失败");
								}else{
									reportToast(bean.msg);
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if( InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) == true ){
							reportToast("删除滚动字幕失败");
						}
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
					}
				});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void onDeleteSelectedSucess(String id) {
		
		//界面上由删除多个改为删除单个，删除多个时可能出现某一张被删除，另外一张不能被删除的情况，需要reload数据
		//删除单个不存在这种情况，删除成功后直接更新界面数据即可
		for (SNoticeListrowsBean bean : mSNoticeListDatas) {
			if( String.valueOf(bean.id).equals(id) ){
				mSNoticeListDatas.remove(bean);
				break;
			}
		}
		mAdapter.notifyDataSetChanged();
		
	}

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.next_step) {
//				showPopupMenu(v);
			} else if (v.getId() == R.id.btn_snotice_add) {
				Intent intent = new Intent(getContext(), SNoticeAddActivity.class);
				if (mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX) {
					intent.putExtra("start_by_type", "online_plan");
				} else {
					intent.putExtra("start_by_type", "offline_plan");
				}
				startActivity(intent);
			} else if (v.getId() == R.id.btn_snotice_del) {
				// onDelSelected();
			} else if (v.getId() == R.id.back_btn) {
				getOwnActivity().finish();
				getOwnActivity().overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		}
	};

	protected void reportToast(String text) {
		Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
	}

	protected void returnToLogin() {
		InfoReleaseApplication.returnToLogin(getActivity());
	}

//	private void showPopupMenu(View v) {
//		PopupMenu popup = new PopupMenu(getContext(), v);
//		MenuInflater inflater = popup.getMenuInflater();
//		if (mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX) {
//			inflater.inflate(R.menu.snotice_list_menu, popup.getMenu());
//		} else {
//			inflater.inflate(R.menu.offline_snotice_list_menu, popup.getMenu());
//		}
//
//		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//
//			@Override
//			public boolean onMenuItemClick(MenuItem item) {
//				if (item.getItemId() == R.id.btn_snotice_add) {
//					Intent intent = new Intent(getContext(), SNoticeAddActivity.class);
//					if (mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX) {
//						intent.putExtra("start_by_type", "online_plan");
//					} else {
//						intent.putExtra("start_by_type", "offline_plan");
//					}
//					startActivity(intent);
//				} else if (item.getItemId() == R.id.btn_snotice_publish) {
//					// onPublishSelected();
//				} else if (item.getItemId() == R.id.btn_snotice_del) {
//					// onDelSelected();
//				} else if (item.getItemId() == R.id.btn_offline_release) {
//					onOfflineReleaseSelected();
//				} else if (item.getItemId() == R.id.btn_offline_cache) {
//					onOfflineCacheSelected();
//				} else if (item.getItemId() == R.id.select_all) {
//					SNoticeListAdapter adapter = mAdapter;
//					adapter.selectAll();
//					adapter.notifyDataSetChanged();
//				} else if (item.getItemId() == R.id.unselect) {
//					SNoticeListAdapter adapter = mAdapter;
//					adapter.selectAll();
//					adapter.notifyDataSetChanged();
//				}
//				return false;
//			}
//		});
//		popup.show();
//	}

	private void reload() {

		if (mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX) {
			mCurPage = 1;
			mSNoticeListDatas.clear();
			mAdapter.clearSelect();
			getSNoticeList(mCurPage, 10, ProgressDialog.show(getContext(), "", "...Loading..."));
		} else {

			mSNoticeListOfflineDatas.clear();
			mAdapter.clearSelect();
			mSNoticeListOfflineDatas.addAll(new OfflinePackageMgr(getContext()).getSNoticeList());
			mAdapter.setDatas(mSNoticeListOfflineDatas);
			mNoticeListView.onRefreshComplete();

		}
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

	public static final String ACTION_SNOTICE_LIST_CHANGED = "com.routon.inforelease.class.info.list.changed";

	private void registerRefreshListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SNOTICE_LIST_CHANGED);
		getContext().registerReceiver(mContentChangedListener, filter);
	}

	private void unregisterRefreshListener() {
		getContext().unregisterReceiver(mContentChangedListener);
	}

	private BroadcastReceiver mContentChangedListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_SNOTICE_LIST_CHANGED)) {
				mPublishIds = PublishStateUtils.getSavedNoticeIDs(SNoticeListFragment.this.getContext());
//				if( mAdapter != null ){
//					mAdapter.setPublishIds(mPublishIds);
//				}
				reload();
			}
		}
	};

	private boolean mEditable=true;
}
