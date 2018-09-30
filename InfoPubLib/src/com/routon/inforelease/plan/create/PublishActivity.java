package com.routon.inforelease.plan.create;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.routon.ad.pkg.SNoticePkgTools;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.FindPlanAssignBean;
import com.routon.inforelease.json.FindPlanAssignBeanParser;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.json.PlanMaterialBean;
import com.routon.inforelease.json.PlanMaterialBeanParser;
import com.routon.inforelease.json.SendResultBean;
import com.routon.inforelease.json.SendResultBeanParser;
import com.routon.inforelease.json.SendplayBean;
import com.routon.inforelease.json.SendplayBeanParser;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.offline.OfflinePackageMgr;
import com.routon.inforelease.offline.OfflineReleaseTools;
import com.routon.inforelease.plan.MaterialEditActivity;
import com.routon.inforelease.plan.PlanListFragment;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

public class PublishActivity extends CustomTitleActivity{
	private static final String TAG = "PublishActivity";

	private Context mContext;
	
//	private ListView listView;
	
	private TextView mContractNameTextView;

	private TextView mPicsCountTextView;
	private TextView mTextCountTextView;
	private TextView mGroupCountTextView;
	private String selectIds = null;
	
	private static final int ON_NAME_EDIT_FINISH = 1;
	private static final int ON_PICS_EDIT_FINISH = 2;
	private static final int ON_TEXT_EDIT_FINISH = 3;
	private static final int ON_GROUP_SELECT_FINISH = 4;
	private static final int GROUP_SELECT_RESULT=5;
	
	private String startBy = "create_plan";
	private boolean isFromTerminal = false;
	
	private boolean mContractNameChanged;
	
	// for offline
	private boolean mIsOfflineMode = false;
	private PlanMaterialBean mImageMaterial;
	private PlanMaterialBean mTextMaterial;
	private OfflinePackageMgr mOfflinePkgMgr;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getIntent().getExtras();
		if (args != null) {
			startBy = args.getString("start_by", "create_plan");
			isFromTerminal = args.getBoolean("is_from_terminal");
			startType = args.getString("start_type");
			mPlanBean = (PlanListrowsBean) args.getSerializable("plan");
			mContractId = mPlanBean.contractId; //args.getInt("plan_id");
			mContractName = mPlanBean.contractName; // args.getString("plan_name");
			
			if (mPlanBean.name != null && mPlanBean.name.length() > 0) {
				mIsOfflineMode = true;
			}
		}
		Log.v(TAG, "isFromTerminal: " + isFromTerminal);

		setContentView(R.layout.publish_fragment);

		mContext = this;
		if (startBy.equals("plan_list")) {
			initTitleBar(R.string.plan_detail);
			TextView status = (TextView) findViewById(R.id.plan_status);
			status.setVisibility(View.GONE);
		} else if (startBy.equals("offline_plan")){
			initTitleBar(R.string.offline_publish_title);
			findViewById(R.id.plan_status).setVisibility(View.GONE);
			findViewById(R.id.publish_btn_layout).setVisibility(View.GONE);
			findViewById(R.id.line_group_count).setVisibility(View.GONE);
		}else{
			initTitleBar(R.string.complate_and_publish_title);
		}

		setTitleBackBtnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mContractNameChanged || startBy.equals("create_plan")) {
					notifyPlanListChanged();
				} 
				finish();
			}
		});
		
		Button publishBtn = (Button)findViewById(R.id.button_publish_plan);
		publishBtn.setOnClickListener(mOnClickListener);
		if (isFromTerminal) {
			publishBtn.setVisibility(View.GONE);
			findViewById(R.id.more_edit_contract_name).setVisibility(View.INVISIBLE);
			findViewById(R.id.more_group_count).setVisibility(View.INVISIBLE);
			findViewById(R.id.btn_delete_contract).setVisibility(View.GONE);
		}
		// 有班牌权限无文字广告，不显示可编辑提示
		if (InfoReleaseApplication.getClassInfoPrivilege()) {
			findViewById(R.id.more_text_count).setVisibility(View.INVISIBLE);
		}
		if (mIsOfflineMode) {
			publishBtn.setVisibility(View.GONE);
			findViewById(R.id.more_group_count).setVisibility(View.INVISIBLE);
		}
		
		mContractNameTextView = (TextView) findViewById(R.id.edit_contract_name);
		mPicsCountTextView = (TextView) findViewById(R.id.text_pics_count);
		mTextCountTextView = (TextView) findViewById(R.id.text_count);
		mGroupCountTextView = (TextView) findViewById(R.id.text_group_count);
		
		mContractNameTextView.setOnClickListener(mOnClickListener);
		mPicsCountTextView.setOnClickListener(mOnClickListener);
		mTextCountTextView.setOnClickListener(mOnClickListener);
		mGroupCountTextView.setOnClickListener(mOnClickListener);
		
		findViewById(R.id.button_offline_release).setOnClickListener(mOnClickListener);
		findViewById(R.id.btn_delete_contract).setOnClickListener(mOnClickListener);
		
//		listView = (ListView) findViewById(R.id.list_view);
//		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				switch(position){
//				case 0:	{ //name
//					onEditContractName();
//				}
//					break;
//				case 1://pics
//					onEditGroups();
//					break;
//				case 2://texts
//					onEditText();
//					break;
//				case 3: { //groups
//					onEditGroups();
//				}
//					break;
//				}
//				
//			}
//		});

		if (mIsOfflineMode) {
			mOfflinePkgMgr = new OfflinePackageMgr(this);
			mImageMaterial = mOfflinePkgMgr.getPlanMaterial(mPlanBean.name, 0);
			mTextMaterial = mOfflinePkgMgr.getPlanMaterial(mPlanBean.name, 1);
			
			mImageCount = mImageMaterial.total;
			mTextCount = mTextMaterial.total;
			setProgram();
		} else {
			HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getImageListUrl(null, 1, 100, mContractId), mHandler, MSG_GET_IMAGE_LIST);
			HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getTextListUrl(null, 1, 100, mContractId), mHandler, MSG_GET_TEXT_LIST);
			HttpClientDownloader.getInstance().findPlanAssign(mContractId, mHandler, MSG_GET_GROUP_LIST);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mContractNameChanged || startBy.equals("create_plan")) {
			notifyPlanListChanged();
		} 
		super.onBackPressed();
	}

	private PlanListrowsBean mPlanBean;
	private int mContractId;
	private String mContractName;
	private int mImageCount = 0;
	private int mTextCount = 0;
	private int mGroupCount = 0;

	public void setProgram(){
		Resources res = mContext.getResources();
		
		if (mContractNameChanged) {
			if (mIsOfflineMode) {
				PublishStateUtils.removeData(this,getDir("isPublishOffPlan.txt", Context.MODE_PRIVATE).getPath(),String.valueOf(mPlanBean.name));
				Intent intent = new Intent(PlanListFragment.ACTION_PLAN_OFF_CHANGED);
				this.sendBroadcast(intent);
				
			}else{
				PublishStateUtils.removeData(this,getDir("isPublishPlan.txt", Context.MODE_PRIVATE).getPath(),String.valueOf(mContractId));
			}
			
		}
		
		String name = null;
		if (mContractName != null && mContractName.length() > 0) {
			name = mContractName;
		} else {
			DateFormat df = new SimpleDateFormat("'我的计划'yyyyMMddHHmmss");
			name = df.format(new Date());
		}
		mContractNameTextView.setText(name);
		
		mPicsCountTextView.setText("" + mImageCount + res.getString(R.string.pices1));
		mTextCountTextView.setText("" + mTextCount + res.getString(R.string.pices2));
		mGroupCountTextView.setText("" + mGroupCount + res.getString(R.string.pices3));

//		int titles[] = {R.string.program_name_title, R.string.pics_title, R.string.text_title, R.string.group_title};
//		String contents[] = {
//				name, 
//				"" + mImageCount + res.getString(R.string.pices1), 
//				"" + mTextCount + res.getString(R.string.pices2),
//				"" + mGroupCount + res.getString(R.string.pices3)
//		};
//		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
//		for(int i = 0; i< 4; i++){
//	        Map<String, Object> map = new HashMap<String, Object>();
//	        map.put("title", res.getString(titles[i]));
//	        map.put("content", contents[i]);
//	        datas.add(map);
//        }
//        
//		SimpleAdapter listAdapter = new SimpleAdapter(this,
//					datas, 
//					R.layout.publish_program_list_item, 
//					new String[] {"title", "content"},
//					new int[]{R.id.title, R.id.content});
//		
//		listView.setAdapter(listAdapter);
	}
	
	private void onPublishPlan() {
		showSendProgress();
		HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getSendPlayUrl(mContractId), mHandler, MSG_SEND_PLAY);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	private void onGetImageList(String text) {
		Log.v(TAG, "imglist: " + text);
		if (text == null) {
			return;
		}
		
		PlanMaterialBean bean = PlanMaterialBeanParser.parsePlanMaterialBean(text);
		if (bean == null) {
			return;
		}
		mImageCount = bean.total;
		
		setProgram();
	}
	
	private void onGetTextList(String text) {
		Log.v(TAG, "textlist: " + text);
		if (text == null) {
			return;
		}
		
		PlanMaterialBean bean = PlanMaterialBeanParser.parsePlanMaterialBean(text);
		if (bean == null) {
			return;
		}
		
		mTextCount = bean.total;
		
		setProgram();
	}
	
	String mGroups;
	
	private void onGetGroupList(String text) {
		Log.v(TAG, "groups: " + text);
		if (text == null) {
			return;
		}
		FindPlanAssignBean bean = FindPlanAssignBeanParser.parseFindPlanAssignBean(text);
		if (bean == null) {
			return;
		}
		if (bean.code == 1) {
			if (bean.groups == null || bean.groups.isEmpty()) {
				mGroupCount = 0;
			} else {
				parseGroupSelectIdsString(bean.groups);
			}
			
		} else if (bean.code == -2) {
			returnToLogin();
		} else {
			reportToast("获取分组失败"); //(bean.msg);
			mGroupCount = 0;
		}

		mGroups = bean.groups;
			
		setProgram();
	}
	
	private void parseGroupSelectIdsString(String selectIdString){
		selectIds = selectIdString;
		String[] splits = selectIdString.split(",");
		mGroupCount = splits.length;
	}
		
	private void onEditContract(String text) {
		Log.v(TAG, "plan edit: " + text);
		if (text == null) {
			reportToast("通信错误!");
			return;
		}
		
		BaseBean bean = BaseBeanParser.parseBaseBean(text);
		if (bean == null) {
			reportToast("数据错误!");
			return;
		}
		
		if (bean.code == 1) {
			Toast.makeText(this, "change name sucess", Toast.LENGTH_SHORT).show();
			mContractName = mEditContractName;
			mContractNameChanged = true;
			mContractNameTextView.setText(mContractName);
			mPlanBean.contractName = mContractName;
			PublishStateUtils.removeData(this,getDir("isPublishPlan.txt", Context.MODE_PRIVATE).getPath(),String.valueOf(mContractId));
		} else if (bean.code == -2) {
			returnToLogin();
		} else {
			reportToast(bean.msg);
		}
	}
	
	private void onPlanAssign(String text) {
		if (text == null) {
			return;
		}
		
		BaseBean bean = BaseBeanParser.parseBaseBean(text);
		if (bean == null) {
			return;
		}
		
		if (bean.code == 1) {
			Toast.makeText(mContext, "plan assign sucess", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, "plan assign failed", Toast.LENGTH_SHORT).show();
		}		
	}
	
	private void onSendPlay(String text) {
		Log.v(TAG, "send play: " + text);
		if (text == null) {
			return;
		}
		
		SendplayBean bean = SendplayBeanParser.parseSendplayBean(text);
		if (bean == null) {
			return;
		}
		
		if (bean.code == 0) {
			//Toast.makeText(getContext(), "plan assign sucess", Toast.LENGTH_SHORT).show();
			HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getSendResultUrl(Integer.parseInt(bean.obj.sendId)), mHandler, MSG_SEND_RESULT);
		} else {
			//Toast.makeText(getContext(), "plan assign failed", Toast.LENGTH_SHORT).show();
		}		
	}
	
	private void onSendResult(String text) {
		Log.v(TAG, "send play: " + text);
		if (text == null) {
			return;
		}
		
		SendResultBean bean = SendResultBeanParser.parseSendResultBean(text);
		if (bean == null) {
			return;
		}
		
		if (bean.code == 0) {
			Log.v(TAG, "sum: " + bean.obj.sum);
			Log.v(TAG, "complete: " + bean.obj.completecount);
			Log.v(TAG, "cancel: " + bean.obj.cancelcount);
			Log.v(TAG, "exception: " + bean.obj.exceptioncount);
			int sum = Integer.parseInt(bean.obj.completecount) + Integer.parseInt(bean.obj.cancelcount) + Integer.parseInt(bean.obj.exceptioncount);
			updateProgress(Integer.parseInt(bean.obj.completecount), bean.obj.sum);
			if (sum != bean.obj.sum || bean.obj.sum == 0) {
				Message msg = Message.obtain(mHandler, 1, bean);
				mHandler.sendMessageDelayed(msg, 1000);
			} else {
				if (mProgressDlg != null) {
					mProgressDlg.setMessage(mContext.getResources().getString(R.string.publish_finished));
					mProgressDlg.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
				}
			}
			PublishStateUtils.writeData(this,getDir("isPublishPlan.txt", Context.MODE_PRIVATE).getPath(),String.valueOf(mContractId));
			Intent intent = new Intent(PlanListFragment.ACTION_PLAN_LIST_CHANGED);
			mContext.sendBroadcast(intent);
		} else {
			
		}
	}
	
	ProgressDialog mProgressDlg;
	private void showSendProgress() {
		if (mProgressDlg == null) {
			mProgressDlg = new ProgressDialog(mContext);
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

	private static final int MSG_GET_IMAGE_LIST = 0;
	private static final int MSG_GET_TEXT_LIST = 1;
	private static final int MSG_GET_GROUP_LIST = 2;
	private static final int MSG_EDIT_CONTRACT = 3;
	private static final int MSG_PLAN_ASSIGN = 4;
	private static final int MSG_SEND_PLAY = 5;
	private static final int MSG_SEND_RESULT = 6;
	private static final int MSG_PLAN_DEL = 7;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
			{
				switch (msg.arg1) {
				case MSG_GET_IMAGE_LIST:
					onGetImageList((String) msg.obj);
					break;
					
				case MSG_GET_TEXT_LIST:
					onGetTextList((String) msg.obj);
					break;
					
				case MSG_GET_GROUP_LIST:
					onGetGroupList((String) msg.obj);
					break;
					
				case MSG_EDIT_CONTRACT:
					onEditContract((String) msg.obj);
					break;
					
				case MSG_PLAN_ASSIGN:
					onPlanAssign((String) msg.obj);
					break;
					
				case MSG_SEND_PLAY:
					onSendPlay((String) msg.obj);
					break;
					
				case MSG_SEND_RESULT:
					onSendResult((String) msg.obj);
					break;
					
				case MSG_PLAN_DEL:
					onMsgPlanDel((String) msg.obj);
					break;				
				}
			}
				break;
				
			case 1: {
				SendResultBean bean = (SendResultBean) msg.obj;
				HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getSendResultUrl(Integer.parseInt(bean.obj.sendId)), mHandler, MSG_SEND_RESULT);
			}
				break;
			}
		}
		
	};
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if( v.getId() == R.id.button_publish_plan ){
				onPublishPlan();
			}else if( v.getId() == R.id.edit_contract_name ){
				onEditContractName();
			}else if( v.getId() == R.id.text_pics_count ){
				onEditPics();
			}else if( v.getId() == R.id.text_count ){
				onEditText();
			}else if( v.getId() == R.id.text_group_count ){
				onEditGroups();
			}else if( v.getId() == R.id.button_offline_release ){
				onOfflineRelease();
			}else if( v.getId() == R.id.btn_delete_contract ){
				onDeleteContract();
			}
		}
	};
	
	private void onEditContractName() {
		if (isFromTerminal)
			return;
//		Intent intent = new Intent(mContext, ContractNameEditActivity.class);
//		intent.putExtra("contractId", mContractId);
//		intent.putExtra("contractName", mContractName);
//		startActivityForResult(intent, ON_NAME_EDIT_FINISH);
		
		showModifyContractNameDialog(mContractName);
	}
	
	private String mEditContractName;

	private String startType;
	
	private void showModifyContractNameDialog(String text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	      
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.dialog_modify_data, null);
		EditText edit = (EditText)(layout.findViewById(R.id.edit));
		edit.setInputType(InputType.TYPE_CLASS_TEXT);
		edit.setText(text);
		edit.selectAll();
		builder.setView(layout);
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int arg1) {
				EditText edit = (EditText)(layout.findViewById(R.id.edit));
				String data = edit.getText().toString();
				
				if( data == null || data.isEmpty() ){//输入数据为空
					Toast.makeText(mContext, R.string.data_is_null, Toast.LENGTH_SHORT).show();				
					return;
				}
				
				if (mContractName != null && mContractName.length() > 0) {
					if (!data.equals(mContractName)) {
						mEditContractName = data;
						
						if (mIsOfflineMode) {
							mContractName = mEditContractName;
							mContractNameChanged = true;
							mContractNameTextView.setText(mContractName);
							mPlanBean.contractName = mContractName;
							mOfflinePkgMgr.savePlanInfo(mPlanBean);
							
							PublishStateUtils.removeData(PublishActivity.this,getDir("isPublishOffPlan.txt", Context.MODE_PRIVATE).getPath(),String.valueOf(mPlanBean.name));
							Intent intent = new Intent(PlanListFragment.ACTION_PLAN_OFF_CHANGED);
							PublishActivity.this.sendBroadcast(intent);
						} else {
							HttpClientDownloader.getInstance().planEdit(mContractId, data, null, mHandler, MSG_EDIT_CONTRACT);
						}
					}
				}
				
				dlg.dismiss();
    	  	}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int arg1) {
				dlg.dismiss();
    	  	}
		});
		AlertDialog dlg = builder.create();
		dlg.show();
	}
	
	private void onEditPics() {
		Intent intent = new Intent(this, MaterialEditActivity.class);
		intent.putExtra("plan", mPlanBean);
		intent.putExtra("type", 0);
		intent.putExtra("is_from_terminal", isFromTerminal);
		startActivityForResult(intent, ON_PICS_EDIT_FINISH);
	}
	
	private void onEditText() {
		// 有班牌权限无文字广告，禁止编辑
		if (InfoReleaseApplication.getClassInfoPrivilege())
			return;
		
		Intent intent = new Intent(this, MaterialEditActivity.class);
		intent.putExtra("plan", mPlanBean);
		intent.putExtra("type", 1);
		intent.putExtra("is_from_terminal", isFromTerminal);
		startActivityForResult(intent, ON_TEXT_EDIT_FINISH);
	}
	
	private void onEditGroups() {
		Log.v(TAG, "onEditGroups");
		if (isFromTerminal)
			return;
		if (startType!=null&&startType.equals("plan_offline_list")) {
			return;
		}

		Intent intent = new Intent(this, GroupSelectActivity.class);		
		intent.putExtra("plan_id", mContractId);
		intent.putExtra("plan_name", mContractName);
		intent.putExtra("start_by", "publish");
		if(selectIds != null){
			Log.i(TAG, "------------selectIds:"+selectIds);
			intent.putExtra("select_param", selectIds);
		}
		intent.putExtra("is_from_terminal", isFromTerminal);
		startActivityForResult(intent, ON_GROUP_SELECT_FINISH);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == ON_NAME_EDIT_FINISH) {
			if (resultCode == 1) {
				String name = data.getStringExtra("contractName");
				mContractName = name;
				setProgram();
				mContractNameChanged = true;
			}
		} else if(requestCode == ON_GROUP_SELECT_FINISH){
//			if(resultCode == 0){
//				String selectIdsString = data.getStringExtra("select_ids");
//				if(selectIdsString != null && selectIdsString.length() != 0){
//					parseGroupSelectIdsString(selectIdsString);
//					setProgram();
//				}
//			}
			if (resultCode == RESULT_OK) {
				mContractNameChanged=data.getBooleanExtra("isChange", false);
				PublishStateUtils.removeData(this,getDir("isPublishPlan.txt", Context.MODE_PRIVATE).getPath(),String.valueOf(mContractId));
			}
			HttpClientDownloader.getInstance().findPlanAssign(mContractId, mHandler, MSG_GET_GROUP_LIST);
		} else if(requestCode == ON_PICS_EDIT_FINISH){
			if (resultCode == RESULT_OK) {
				int count = data.getIntExtra("count", mImageCount);
				mContractNameChanged=data.getBooleanExtra("isChange", false);
				mImageCount = count;
				setProgram();
			}
		} else if(requestCode == ON_TEXT_EDIT_FINISH){
			if (resultCode == RESULT_OK) {
				mContractNameChanged=data.getBooleanExtra("isChange", false);
				int count = data.getIntExtra("count", mImageCount);
				mTextCount = count;
				setProgram();
			}
		}
	}

	private void onOfflineRelease() {
//		if (mIsOfflineMode) {
			new OfflineReleaseTools(this).startPkgAd(mPlanBean, InfoReleaseApplication.getClassInfoPrivilege());
//		}
	}
	
	private void onDeleteContract() {
		if (mIsOfflineMode) {
			mOfflinePkgMgr.delete(mPlanBean.name, OfflinePackageMgr.TYPE_AD);
			notifyPlanListChanged();			
			finish();
		} else {
			HttpClientDownloader.getInstance().planDel(mContractId, mHandler, MSG_PLAN_DEL);
		}
	}
	
	private void onMsgPlanDel(String text) {
		Log.v(TAG, "plan del result: " + text);
		if (text == null) {
			Log.v(TAG, "msg del plan null");
			return;
		}		
		
		BaseBean bean = BaseBeanParser.parseBaseBean(text);
		if( bean == null ){
			Toast.makeText(this, "计划删除失败", Toast.LENGTH_SHORT).show();
			return;
		}
		if ( bean.code != 1 ) {			
			if (bean.code == -2) {
				returnToLogin();
			} else {
				Toast.makeText(this, bean.msg, Toast.LENGTH_SHORT).show();
			}
			return;
		}
		notifyPlanListChanged();
		
		finish();
	}
	
	private void notifyPlanListChanged() {
		Intent intent = new Intent(PlanListFragment.ACTION_PLAN_LIST_CHANGED);
		sendBroadcast(intent);
	}
}
