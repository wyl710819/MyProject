package com.routon.inforelease.classinfo;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
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
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.routon.ad.element.TemplateInfo;
import com.routon.common.BaseFragment;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.ClassInfoListBean;
import com.routon.inforelease.json.ClassInfoListBeanParser;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.json.ClassInfoListfilesBean;
import com.routon.inforelease.net.NetWorkRequest;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.offline.OfflinePackageMgr;
import com.routon.inforelease.offline.OfflineReleaseTools;
import com.routon.inforelease.plan.MaterialParams;
import com.routon.inforelease.plan.adapter.ClassInfoListAdapter;
import com.routon.inforelease.plan.create.ClassPreviewActivity;
import com.routon.inforelease.plan.create.OfflinePicSelectActivity;
import com.routon.inforelease.plan.create.PictureSelectActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.ContentCheckHelper;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.inforelease.util.TemplatePkgTool;
import com.routon.inforelease.widget.PopupList;
import com.routon.inforelease.widget.SelectTabWidget;
import com.routon.inforelease.widget.ClassPreviewView.OnPreviewClickListener;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

public class ClassInfoListFragment extends BaseFragment implements OnPreviewClickListener{
	private static final String TAG = "ClassInfoListActivity";
//	private int species;
	private PullToRefreshListView mClassInfoListView;
	//班牌显示数据
	private List<ClassInfoListdatasBean> mClassInfoListDatas = new ArrayList<ClassInfoListdatasBean>();
	
	//班牌离线数据
	private List<ClassInfoListdatasBean> mClassInfoOfflineDatas = new ArrayList<ClassInfoListdatasBean>();
	
	//班牌在线数据
	private List<ClassInfoListdatasBean> mClassInfoOnlineDatas = new ArrayList<ClassInfoListdatasBean>();
	
	private ClassInfoListAdapter mAdapter;
	
	private int mCurPage = 0;
	
	private String mTerminalId = null;
	private boolean mIsTerminalClassInfoList;
	private boolean mAuditAuthority;
	
	private String groupIDString = null;

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		Bundle args = this.getArguments();
		if (args != null) {
			mTerminalId = args.getString("id");
			if (mTerminalId != null && mTerminalId.length() > 0) {
				mIsTerminalClassInfoList = true;
			}
			
			groupIDString = args.getString("groupIDs");
			Log.i(TAG, "groupIDString:"+groupIDString);
			
			mAuditAuthority = args.getBoolean(CommonBundleName.AuditClassInfoAuthority);
		}
		
		View view = inflater.inflate(R.layout.fragment_classinfo_list, container, false);		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mClassInfoListDatas = mClassInfoOnlineDatas;
		
		initView(getView());
		
		//取班牌在线数据
		mCurPage = 1;
		getClassInfoList(mCurPage, 10,ProgressDialog.show(getContext(), "", "...Loading..."));

		//取班牌离线数据
		mClassInfoOfflineDatas.addAll(new OfflinePackageMgr(getContext()).getClassInfoList());
	}
	
	private SelectTabWidget mSelWidget;
	private void initView(View view) {
		mSelWidget= (SelectTabWidget) view.findViewById(R.id.sel_widget);
		mSelWidget.setVisibility(View.INVISIBLE);
		ViewGroup.LayoutParams widgetParams=(ViewGroup.LayoutParams)mSelWidget.getLayoutParams();
		widgetParams.height=0;
		mSelWidget.setLayoutParams(widgetParams);
		mSelWidget.setOnSelClickListener(new SelectTabWidget.SelClickListener() {
			
			@Override
			public void onClick(int index) {
				// TODO Auto-generated method stub
				if( index == SelectTabWidget.ONLINE_INDEX ){
					mClassInfoListDatas = mClassInfoOnlineDatas;					
				}else if( index == 1 ){
					//重新获取离线数据
					mClassInfoOfflineDatas.clear();
					mClassInfoOfflineDatas.addAll(new OfflinePackageMgr(getContext()).getClassInfoList());
					mClassInfoListDatas = mClassInfoOfflineDatas;
				}
				mAdapter.clearSelect();
				mAdapter.setDatas(mClassInfoListDatas);
			}
		});
		if( mIsTerminalClassInfoList == true ){
			mSelWidget.setVisibility(View.GONE);
		}
		mClassInfoListView = (PullToRefreshListView) view.findViewById(R.id.list_class_info);
		mClassInfoListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				if( mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX ){//重新取在线数据
					mCurPage = 1;
					getClassInfoList(mCurPage, 10,null);
				}else{//重新取离线数据
					mClassInfoOfflineDatas.clear();
					mAdapter.clearSelect();
					mClassInfoOfflineDatas.addAll(new OfflinePackageMgr(getContext()).getClassInfoList());
					mClassInfoListView.onRefreshComplete();
					mAdapter.setDatas(mClassInfoOfflineDatas);
				}
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				if( mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX ){//在线班牌列表
					getClassInfoList(++mCurPage, 10,null);
				}else{
					mClassInfoListView.onRefreshComplete();
				}
			}
			
		});

		mAdapter = new ClassInfoListAdapter(getContext(), mClassInfoListDatas);
		mAdapter.setOnItemClickClickListener(new ClassInfoListAdapter.OnItemClickListener() {
			
			@Override
			public void onItemClickListener(View v, int position) {
				// TODO Auto-generated method stub
				showPopupList(v,position);
				ClassInfoListdatasBean item = mClassInfoListDatas.get(position);
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
		});
		mAdapter.mIsTerminalClassInfoList = mIsTerminalClassInfoList;
		if (mIsTerminalClassInfoList) {
			if(mTerminalId.equals("-1") && groupIDString != null){
				mAdapter.setIsShowPublishFlag(false);
			}
		}else{
			if(this.mAuditAuthority == true ){
				mAdapter.setIsShowPublishFlag(false);
			}
		}
		mClassInfoListView.setAdapter(mAdapter);
		
//		view.findViewById(R.id.btn_snotice_add).setOnClickListener(mOnClickListener);
//		view.findViewById(R.id.btn_class_info_publish).setOnClickListener(mOnClickListener);
//		view.findViewById(R.id.btn_snotice_del).setOnClickListener(mOnClickListener);
		
		// title bar
		if( InfoReleaseApplication.mFamilyVersion == true ){
			this.initTitleBar(this.getString(R.string.menu_school_dynamic));
		}else{
			this.initTitleBar("内容列表");
		}
		
		if (mIsTerminalClassInfoList == false ){
			if( mAuditAuthority == false ){//审核权限不可新建内容
				this.setTitleNextImageBtnClickListener(R.drawable.ic_add, new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						showPopupMenu(arg0);
					}
				});
			}
		}else{
			this.setTitleBackBtnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					getOwnActivity().finish();
					getOwnActivity().overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
				}
			});
		}
	}
	
	private void getClassInfoList(final int page, int pageSize,final ProgressDialog progressDialog) {  
		if(pageSize == -1)
			pageSize = 10;
    	
//		String urlString = UrlUtils.getClassInfoListUrl(page, pageSize);
		String urlString = "";
		if (mIsTerminalClassInfoList) {
			if(mTerminalId.equals("-1") && groupIDString != null){
				urlString =  UrlUtils.getParentClassInfoListUrl(groupIDString, page, pageSize);
			}else{
				urlString = UrlUtils.getTerminalClassInfoListUrl(mTerminalId, page, pageSize);
			}
		}else{
			if( mAuditAuthority == true ){//获取待审核内容列表
				urlString = UrlUtils.getClassInfoListUrl(page, pageSize,2,null);
			}else{
				String[] classes = InfoReleaseApplication.authenobjData.headTeacherClasses;
				String groupIds = "";
				for(int index = 0; index< classes.length;index++){
					groupIds += classes[index];
					if( index < classes.length - 1 ){
						groupIds += ",";
					}
				}
				urlString = UrlUtils.getClassInfoListUrl(page, pageSize,groupIds);
			}
			
		}
		
		Log.d(TAG, "URL:" + urlString);
  
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                        if( ClassInfoListFragment.this.getContext() == null ){
                        	return;
                        }
                        if (progressDialog != null&&progressDialog.isShowing() ) {  
                            progressDialog.dismiss();  
                        }
                    
                        if( page == 1 ){
                        	mClassInfoOnlineDatas.clear();
                        	mAdapter.clearSelect();
                        	mAdapter.setDatas(mClassInfoListDatas);
                        }
                        mClassInfoListView.onRefreshComplete();   
                        ClassInfoListBean bean;
						try {
							bean = ClassInfoListBeanParser.parseClassInfoListBean(response);
							if( bean != null && bean.code == -2 ){
								Activity activity = getOwnActivity();
								if( activity != null ){
									InfoReleaseApplication.returnToLogin(activity);
								}
								return;
							}
							if (bean == null || bean.datas == null || bean.datas.size() == 0) {
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
							mClassInfoOnlineDatas.addAll(bean.datas);
							mAdapter.notifyDataSetChanged();
						} catch (JSONException e) {
							e.printStackTrace();
						}					                     
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	if( page == 1 ){
                         	mClassInfoOnlineDatas.clear();
                         	mAdapter.clearSelect();
                         	mAdapter.setDatas(mClassInfoListDatas);
                        }
                    	
                    	if(InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) == true){
                    		Toast.makeText(getContext(), "获取数据失败!", Toast.LENGTH_LONG).show();
                    	}
                    	
                    	if ( progressDialog != null&&progressDialog.isShowing() ) {  
                            progressDialog.dismiss();  
                        }  

                    	mClassInfoListView.onRefreshComplete();
                    }  
                });  
        
        if(mIsTerminalClassInfoList && mTerminalId.equals("-1") && groupIDString != null){
        	
        }else{
        	jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        }
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
    }
	
	private void onPublishSelected(final String ids) {
//		List<ClassInfoListdatasBean> selected = mAdapter.getSelectMaterial();
//		if (selected.size() == 0) {
//			Toast.makeText(getContext(), "请至少选择一条班牌信息", Toast.LENGTH_SHORT).show();
//			return;
//		}
//		
//		String ids = "";
//		for (ClassInfoListdatasBean bean : selected) {
//			ids += Integer.toString(bean.id);
//			ids += ",";
//		}
//		ids = ids.substring(0, ids.length() - 1);
		
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
	
	private void onOfflineReleaseSelected(int type) {
		List<ClassInfoListdatasBean> selected = mAdapter.getSelectMaterial();
		if (selected.size() == 0) {
			Toast.makeText(getContext(), "请至少选择一条班牌信息", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (type == 0)
			new OfflineReleaseTools(getContext()).startPkgEClass(selected);
		else
			new OfflineReleaseTools(getContext(),false).startCacheEClass(selected);
	}
	
	private void sendPublishRequest(final String ids){
		sendPublishRequest(ids,true);
	}
	
	private void sendPublishRequest(final String ids,final boolean showToast){
		Log.v(TAG, "class info publish: " + ids);
		
        this.showProgressDialog();
  
        String urlString = UrlUtils.getClassInfoPublishUrl(ids);
        Log.i(TAG, "URL:" + urlString);
        
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                        hideProgressDialog();
                        try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if( bean == null ){
								return;
							}
							if (bean.code == 0) {
								if( mSelListIndex >= 0 && mSelListIndex < mClassInfoListDatas.size() ){
									mClassInfoListDatas.get(mSelListIndex).status = 2;
								}
								PublishStateUtils.writeData(getContext() , getContext().getDir("isPublishClass.txt", Context.MODE_PRIVATE).getPath(),ids);
								mAdapter.notifyDataSetChanged();
								if( showToast == true ){
									Toast.makeText(getContext(), "发布班牌信息成功", Toast.LENGTH_SHORT).show();
								}
							} else if( bean.code == -2 ){
								InfoReleaseApplication.returnToLogin(ClassInfoListFragment.this.getActivity());
								return;
							} else {
								if( bean.msg != null && bean.msg.isEmpty() == false ){
									if( showToast == true ){
										Toast.makeText(getContext(), bean.msg, Toast.LENGTH_SHORT).show();
									}
								}else{
									if( showToast == true ){
										Toast.makeText(getContext(), "发布班牌信息失败", Toast.LENGTH_SHORT).show();
									}
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
                    	
                    	if( InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) == true ){
                    		Toast.makeText(getContext(), "发布班牌信息失败!", Toast.LENGTH_LONG).show();
                    	}
                    	hideProgressDialog();
                    }  
                });
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
//	private List<ClassInfoListdatasBean> selectedDel ;
	private void onDelSelected(final String ids) {
//		selectedDel = mAdapter.getSelectMaterial();
//		if (selectedDel.size() == 0) {
//			Toast.makeText(getContext(), "请至少选择一条班牌信息", Toast.LENGTH_SHORT).show();
//			return;
//		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("请选择:");
		builder.setMessage("您确认删除选择的内容并取消投放吗?");
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
//				String ids = "";
//				for (ClassInfoListdatasBean bean : selectedDel) {
//					if (bean.name != null && !bean.name.isEmpty()) {
//						mClassInfoListDatas.remove(bean);
//						new OfflinePackageMgr(getContext()).delete(bean.name, OfflinePackageMgr.TYPE_CLASS_INFO);
//						
//						
//					}
//					ids += Integer.toString(bean.id);
//					ids += ",";
//				}
				if( mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX ){
//					ids = ids.substring(0, ids.length() - 1);
					sendDeleteRequest(ids);
				}else{
					mClassInfoOfflineDatas.clear();
					mAdapter.clearSelect();
					mClassInfoOfflineDatas.addAll(new OfflinePackageMgr(getContext()).getClassInfoList());
					mAdapter.setDatas(mClassInfoOfflineDatas);
					mClassInfoListView.onRefreshComplete();
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
		
//		mAdapter.clearSelect();
//		mAdapter.setDatas(mClassInfoListDatas);
	}
		
	private void sendDeleteRequest(final String ids){
		Log.v(TAG, "class info del: " + ids);
		
		showProgressDialog();
  
        String urlString = UrlUtils.getClassInfoDelUrl(ids);
        Log.i(TAG, "URL:" + urlString);
        
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                        hideProgressDialog();
                        try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if (bean.code == 0) {
								deleteZipAndDir(Integer.valueOf(ids));
								PublishStateUtils.removeData(getContext(), getContext().getDir("isPublishClass.txt", Context.MODE_PRIVATE).getPath(), ids);
								onDeleteSelectedSucess();
							} else if (bean.code == -2) {
								InfoReleaseApplication.returnToLogin(ClassInfoListFragment.this.getActivity());
								return;
							} else {
								if( bean.msg != null && bean.msg.isEmpty() == false ){
									Toast.makeText(getContext(), bean.msg, Toast.LENGTH_SHORT).show();
								}else{
									Toast.makeText(getContext(), "删除班牌信息失败", Toast.LENGTH_SHORT).show();
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
                    	hideProgressDialog();
                    	if( InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) == true ){
                    		Toast.makeText(getContext(), "删除班牌信息失败!", Toast.LENGTH_LONG).show();
                    	}
                    }  
                });
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	private void onDeleteSelectedSucess() {
//		List<ClassInfoListdatasBean> selected = mAdapter.getSelectMaterial();
//		for (ClassInfoListdatasBean bean : selected) {
//			mClassInfoListDatas.remove(bean);
//		}
//		mAdapter.clearSelect();
//		mAdapter.setDatas(mClassInfoListDatas);
		reload();
	}
	
//	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
//		
//		@Override
//		public void onClick(View v) {
//			if( v.getId() == R.id.next_step ){
//				showPopupMenu(v);
//			}else if( v.getId() == R.id.back_btn ){
//				ClassInfoListFragment.this.getActivity().finish();
//			}
//		}
//	};
	
	private void showPopupMenu(View v) {
		PopupMenu popup = new PopupMenu(getContext(), v);
		MenuInflater inflater = popup.getMenuInflater();
		if( mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX ){//在线班牌列表
			inflater.inflate(R.menu.class_info_list_menu, popup.getMenu());
		}else{
			inflater.inflate(R.menu.offline_class_info_list_menu, popup.getMenu());
		}
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				//新增内容
				if( item.getItemId() == R.id.btn_class_info_add ){
					addNormalClass();
				}
				//使用模板
				else if( item.getItemId() == R.id.btn_class_info_add_template ){
					addTemplateClass();
				}else if( item.getItemId() == R.id.btn_class_info_publish ){
//					onPublishSelected();
				}else if( item.getItemId() == R.id.btn_class_info_del ){
//					onDelSelected();
				}else if( item.getItemId() == R.id.btn_offline_release ){
					onOfflineReleaseSelected(0);
				}else if( item.getItemId() == R.id.btn_offline_cache ){
					onOfflineReleaseSelected(1);
				}else if( item.getItemId() == R.id.select_all ){
					ClassInfoListAdapter adapter = mAdapter;
					adapter.selectAll();
					adapter.notifyDataSetChanged();
				}else if( item.getItemId() == R.id.unselect ){
					ClassInfoListAdapter adapter = mAdapter;
					adapter.clearSelect();
					adapter.notifyDataSetChanged();
				}
				return false;
			}
		});
		popup.show();
	}
	
	private void addTemplateClass(){
		Intent intent ;
		intent = new Intent(getContext(), PictureSelectActivity.class);
		intent.putExtra("start_by", "class_info_add");
		intent.putExtra(CommonBundleName.FILE_TYPE_TAG, MaterialParams.TYPE_CLASS_TEMPLATE);	
		startActivityForResult(intent, 0);
	}
	
	private void addNormalClass(){
		Intent intent ;
		if( mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX ){
			intent = new Intent(getContext(), PictureSelectActivity.class);
		}else{
			intent = new Intent(getContext(), OfflinePicSelectActivity.class);
		}
		intent.putExtra("start_by", "class_info_add");
		intent.putExtra(CommonBundleName.FILE_TYPE_TAG, MaterialParams.TYPE_CLASS_PICTURE);
		intent.putExtra(CommonBundleName.CLASSINFO_TYPE_TAG, 1);
		
		startActivityForResult(intent, 0);
	}
	
	private void reload() {
		if( mSelWidget.getSelIndex() == SelectTabWidget.ONLINE_INDEX ){//重新取在线数据
			mCurPage = 1;
			mClassInfoOnlineDatas.clear();
			mAdapter.clearSelect();
			getClassInfoList(mCurPage, 10,ProgressDialog.show(getContext(), "", "...Loading..."));
		}else{//重新取离线数据
			mClassInfoOfflineDatas.clear();
			mAdapter.clearSelect();
			mClassInfoOfflineDatas.addAll(new OfflinePackageMgr(getContext()).getClassInfoList());
			mAdapter.setDatas(mClassInfoOfflineDatas);
			mClassInfoListView.onRefreshComplete();
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

	public static final String ACTION_CLASS_INFO_LIST_CHANGED = "com.routon.inforelease.class.info.list.changed";
	
	private void registerRefreshListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CLASS_INFO_LIST_CHANGED);
		getContext().registerReceiver(mContentChangedListener, filter);
	}
	
	private void unregisterRefreshListener() {
		getContext().unregisterReceiver(mContentChangedListener);
	}
	//注册广播，当接收广播时表明内容列表发生变化，需要重新加载
	private BroadcastReceiver mContentChangedListener = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_CLASS_INFO_LIST_CHANGED)) {
				reload();
			}
		}
	};
	
	private int mSelListIndex = 0;
	public void showPopupList(final View anchorView,int contextPosition) {
		mSelListIndex = contextPosition;
		if (mIsTerminalClassInfoList) {	
				ClassInfoListdatasBean bean = mClassInfoOnlineDatas.get(contextPosition);
			    List<ClassInfoListfilesBean> fListfilesBeans= bean.files;
				ArrayList<String> contentList = new ArrayList<String>();
				for (int i = 0; i < fListfilesBeans.size(); i++) {
					contentList.add(fListfilesBeans.get(i).content);
				}
				Intent previewIntent = new Intent(getContext(), ClassPreviewActivity.class);
				previewIntent.putExtra("path", contentList.get(0));
				previewIntent.putStringArrayListExtra("content_list", contentList);
				Bundle bundle = new Bundle();
				bundle.putSerializable("class_info_datas", bean);
				previewIntent.putExtras(bundle);			
				startActivity(previewIntent);		
		}else{
			final List<String> popupMenuItemList = new ArrayList<String>();
			popupMenuItemList.add(getString(R.string.review)); 
			popupMenuItemList.add(getString(R.string.snotice_edit)); 
			if( mAuditAuthority == false ){
				popupMenuItemList.add(getString(R.string.plan_throw)); 
			}else{
				popupMenuItemList.add(getString(R.string.pass));
				popupMenuItemList.add(getString(R.string.passnot));
			}
			popupMenuItemList.add(getString(R.string.delete)); 

			PopupList popupList = new PopupList(this.getContext()); 
			if( mAuditAuthority == true ){
				popupList.setTextPaddingLeft(popupList.dp2px(10));
				popupList.setTextPaddingRight(popupList.dp2px(10));
			}
			popupList.showPopupListWindowAtCenter(anchorView, contextPosition, popupMenuItemList, new PopupList.PopupListListener() {
				 @Override
				 public void onPopupListClick(View contextView, int contextPosition, int position) { 
					 String selMenuStr = popupMenuItemList.get(position);
					 if( selMenuStr.equals(getString(R.string.review)) ) {
					 		startPreviewActivity(contextPosition);							
					 }else if( selMenuStr.equals(getString(R.string.snotice_edit)) ){
							final ClassInfoListdatasBean dataItem = mClassInfoListDatas.get(contextPosition);
							int editPkg = dataItem.editPkg;
							//如果存在模板编辑信息,编辑时跳转到模板编辑界面
							if( editPkg != 0 )
							{
								showProgressDialog(anchorView, contextPosition);
								final String fileName = "template_"+dataItem.files.get(0).resid+".zip";
								NetWorkRequest.downloadZip(ClassInfoListFragment.this.getContext(),dataItem.editPkgUrl, fileName, new ClassInfoZipDownListener() {
									
									@Override
									public void onFinish() {
										// TODO Auto-generated method stub
										hideProgressDialog();
										startTemplateClassEditActivity(fileName,dataItem);
									}
									
									@Override
									public void onError() {
										// TODO Auto-generated method stub
										reportToast("Zip包下载失败");
									}
								});
								}
							else {
								startNormalClassEditActivity(dataItem);
							}
					 }else if( selMenuStr.equals(getString(R.string.plan_throw)) ){
							ClassInfoListdatasBean item2 = mClassInfoListDatas.get(contextPosition);
							onPublishSelected(String.valueOf(item2.id));

					 }else if( selMenuStr.equals(getString(R.string.delete)) ){
						ClassInfoListdatasBean item = mClassInfoListDatas.get(contextPosition);
						onDelSelected(String.valueOf(item.id));
					 }else if( selMenuStr.equals(getString(R.string.pass)) ){
						 showProgressDialog(anchorView, contextPosition);
						 ClassInfoListdatasBean item = mClassInfoListDatas.get(contextPosition);
						 auditClassInfo(item);
					 }else if( selMenuStr.equals(getString(R.string.passnot)) ){
						 showProgressDialog(anchorView, contextPosition);
						 showContentList();
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
						 ClassInfoListdatasBean item = mClassInfoListDatas.get(mSelListIndex);
						 auditNoClassInfo(item,response.get(which));			        	
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
	
	public void auditNoClassInfo(final ClassInfoListdatasBean item,String content){
		String urlString = UrlUtils.getAuditUrl(String.valueOf(item.id),0,content);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                        ClassInfoListFragment.this.hideProgressDialog();
                        try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if( bean.code == 0 ){//审核通过
								mClassInfoListDatas.remove(item);
								Toast.makeText(getOwnActivity(), "内容审核不通过", Toast.LENGTH_LONG).show();
								mAdapter.setDatas(mClassInfoListDatas);
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
                    	ClassInfoListFragment.this.hideProgressDialog();
                    }  
                });         
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	public void auditClassInfo(final ClassInfoListdatasBean item){
		String urlString = UrlUtils.getAuditUrl(String.valueOf(item.id),1,null);
		Log.d(TAG, "auditClassInfo ="+urlString);  
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                       Log.d(TAG, "response="+response);  
                       ClassInfoListFragment.this.hideProgressDialog();
                       try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if( bean.code == 0 ){//审核通过
								mClassInfoListDatas.remove(item);
								mAdapter.setDatas(mClassInfoListDatas);
								Toast.makeText(getOwnActivity(), "内容审核通过", Toast.LENGTH_LONG).show();
								//审核通过后调用发布接口
								sendPublishRequest(String.valueOf(item.id),false);
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
                    	ClassInfoListFragment.this.hideProgressDialog();
                    }  
                });         
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	public void startNormalClassEditActivity(ClassInfoListdatasBean dataItem){
		Intent intent = new Intent(getContext(), ClassInfoEditActivity.class);
		intent.putExtra(CommonBundleName.DATA_TAG, dataItem);
		intent.putExtra(CommonBundleName.OFFLINE_TAG, false);
		intent.putExtra(CommonBundleName.AuditClassInfoAuthority, this.mAuditAuthority);
		startActivity(intent);
	}
	
	public void startTemplateClassEditActivity(String fileName,ClassInfoListdatasBean dataItem){
		final String templatePath = getActivity().getFilesDir().getAbsolutePath()
				+"/"+TemplatePkgTool.TEMPLATE_EDIT_DIR_NAME+"/template_"+dataItem.files.get(0).resid+"/";
		File zipFile = new File(getActivity().getFilesDir(),fileName);
		TemplatePkgTool.unzip(ClassInfoListFragment.this.getContext(),zipFile.getAbsolutePath());
		TemplateInfo templateinfo = TemplatePkgTool.parseTemplateEditXml(templatePath+TemplatePkgTool.TEMPLATE_EDIT_INFO_FILE_NAME);
		Intent intent = new Intent(getActivity(),ClassPictureEditActivity.class);
		Bundle editBundle = new Bundle();
		
		templateinfo.mEditDirPath = templatePath;
		
		editBundle.putBoolean(CommonBundleName.OFFLINE_TAG, false);
		editBundle.putSerializable(CommonBundleName.DATA_TAG, dataItem);
		editBundle.putInt(CommonBundleName.APP_TYPE_TAG, CommonBundleName.CLASSINFO_APP_TYPE);
		editBundle.putSerializable(CommonBundleName.TEMPLATE_INFO_TAG, (Serializable)templateinfo);
		editBundle.putBoolean(CommonBundleName.AuditClassInfoAuthority, this.mAuditAuthority);

		intent.putExtras(editBundle);
		startActivity(intent);
	}
	
	public void startPreviewActivity(int contextPosition){
		ClassInfoListdatasBean bean = mClassInfoOnlineDatas.get(contextPosition);
	    List<ClassInfoListfilesBean> fListfilesBeans= bean.files;
		ArrayList<String> contentList = new ArrayList<String>();
		for (int i = 0; i < fListfilesBeans.size(); i++) {
			contentList.add(fListfilesBeans.get(i).content);
		}
		Intent previewIntent = new Intent(getContext(), ClassPreviewActivity.class);
		previewIntent.putExtra("path", contentList.get(0));
		previewIntent.putStringArrayListExtra("content_list", contentList);
		Bundle bundle = new Bundle();
		bundle.putSerializable("class_info_datas", bean);
		previewIntent.putExtras(bundle);
		
		startActivity(previewIntent);
	}
	
	public void showExtraActionDlg(final int position) {
		int array = R.array.class_info_action_array;
		AlertDialog dlg = new AlertDialog.Builder(getContext()).setItems(array, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					ClassInfoListdatasBean dataItem = mClassInfoListDatas.get(position);
					Intent intent = new Intent(getContext(), ClassInfoEditActivity.class);
					intent.putExtra("class_info", dataItem);
					if (dataItem!=null&&dataItem.isOffLine()) {
						intent.putExtra("editType", "offline_edit");
					}
					intent.putExtra("is_from_terminal", mIsTerminalClassInfoList);
					intent.putExtra("editType", "online_edit");
					startActivity(intent);
					break;
					
				case 1:
					ClassInfoListdatasBean item = mClassInfoListDatas.get(position);
					onDelSelected(String.valueOf(item.id));
					break;
					
				case 2:
					ClassInfoListdatasBean item2 = mClassInfoListDatas.get(position);
					onPublishSelected(String.valueOf(item2.id));
					break;
					
				case 3:
					break;
				}
			}
		}).create();
		dlg.show();
	}

	@Override
	public void onPreviewClickListener(View v, int position) {
		// TODO Auto-generated method stub
		this.showPopupList(v, position);
	}
	
	//删除班牌时删除对应的模板编辑信息ZIP包
	public void deleteZipAndDir(int id)
	{
		for(int i=0;i<mClassInfoOnlineDatas.size();i++)
		{
			if(mClassInfoOnlineDatas.get(i).id==id)
			{
				String fileName = "template_"+mClassInfoOnlineDatas.get(i).files.get(0).resid+".zip";
				String dirName="template_"+mClassInfoOnlineDatas.get(i).files.get(0).resid;
				File file=new File(getActivity().getFilesDir()+"/"+fileName);
				if(file.exists())
					file.delete();
				File dirFile=new File(getActivity().getFilesDir()+"/template_edit/"+dirName);
				if(dirFile.exists()&&dirFile.isDirectory())
				{
					File[] files=dirFile.listFiles();
					for(int j=0;j<files.length;j++)
					{
						files[j].delete();
					}
					dirFile.delete();
				}
			}
				
		}
	}	

	private void showProgressDialog(View anchorView, int contextPosition){
		mWaitDialog = new Dialog(getOwnActivity(),R.style.new_circle_progress);  
		int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
//		int x = anchorView.getWidth()/2;
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
	
	
}
