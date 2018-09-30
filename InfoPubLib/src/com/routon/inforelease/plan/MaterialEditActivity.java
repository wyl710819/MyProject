package com.routon.inforelease.plan;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.R;
import com.routon.inforelease.classinfo.ClassInfoEditActivity;
import com.routon.inforelease.json.FindAdPeriodsperiodsBean;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.json.PlanMaterialBean;
import com.routon.inforelease.json.PlanMaterialBeanParser;
import com.routon.inforelease.json.PlanMaterialparamsBean;
import com.routon.inforelease.json.PlanMaterialrowsBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.offline.OfflinePackageMgr;
import com.routon.inforelease.offline.OfflineReleaseTools;
import com.routon.inforelease.plan.adapter.ImageMaterialListAdapter;
import com.routon.inforelease.plan.adapter.MaterialListAdapter;
import com.routon.inforelease.plan.adapter.TextMaterialListAdapter;
import com.routon.inforelease.plan.create.OfflinePicSelectActivity;
import com.routon.inforelease.plan.create.PictureSelectActivity;
import com.routon.inforelease.plan.create.TextSelectActivity;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

public class MaterialEditActivity extends CustomTitleActivity {
	private static final String TAG = "PicsEdit";

	private AdapterView mAdapterView;
	
	private PlanListrowsBean mPlanBean;
	private int mContractId;
	private String mContractName;
	
	private static final int TYPE_PICS = 0;
	private static final int TYPE_TEXT = 1;
	private int mType = 0;
	
	private boolean isFromTerminal;
	
	// for offline cache
	private boolean mIsOfflineMode = false;
	private OfflinePackageMgr mOfflinePkgMgr;
	private PlanMaterialBean mPlanMaterialBean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mPlanBean = (PlanListrowsBean) bundle.getSerializable("plan");
			mContractId = mPlanBean.contractId; // bundle.getInt("contractId");
			mContractName = mPlanBean.contractName; // bundle.getString("contractName");
			mType = bundle.getInt("type");
			isFromTerminal = bundle.getBoolean("is_from_terminal");
			
			if (mPlanBean.name != null && mPlanBean.name.length() > 0)
				mIsOfflineMode = true;
		}
		Log.v(TAG, "isFromTerminal: " + isFromTerminal);
		
		setContentView(R.layout.activity_plan_make_material_edit);
		
		initViews();
		
		if (mIsOfflineMode) {
			mOfflinePkgMgr = new OfflinePackageMgr(this);
			mPlanMaterialBean = mOfflinePkgMgr.getPlanMaterial(mPlanBean.name, mType);

			mMaterialList = mPlanMaterialBean.rows;
			MaterialListAdapter adapter = null;
			if (mType == TYPE_PICS) {
				adapter = new ImageMaterialListAdapter(this, mMaterialList);
			} else {
				adapter = new TextMaterialListAdapter(this, mMaterialList);
			}
			adapter.setEditable(!isFromTerminal);
			mAdapterView.setAdapter(adapter);
		} else {
			if (mType == TYPE_PICS)
				HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getImageListUrl(null, 1, 100, mContractId), mHandler, MSG_GET_MATERIAL_LIST);
			else
				HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getTextListUrl(null, 1, 100, mContractId), mHandler, MSG_GET_MATERIAL_LIST);
		}
	}

	private void initViews() {
		
		findViewById(R.id.btn_attribute_setting).setOnClickListener(mOnBtnClickedListener);
		findViewById(R.id.btn_time_setting).setOnClickListener(mOnBtnClickedListener);
		findViewById(R.id.btn_delete_ad).setOnClickListener(mOnBtnClickedListener);
		findViewById(R.id.btn_add_material).setOnClickListener(mOnBtnClickedListener);
//		if (mIsOfflineMode) {
//			findViewById(R.id.btn_add_material).setVisibility(View.GONE);
//		}
		
		if (isFromTerminal) {
			findViewById(R.id.material_bottom).setVisibility(View.GONE);
			if (mType == TYPE_PICS) {
				initTitleBar(R.string.pics_ad_view);
			} else {
				initTitleBar(R.string.text_ad_view);
			}
		}else{
			// title bar	
			if (mType == TYPE_PICS) {
				mAdapterView = (AdapterView) findViewById(R.id.material_adapter_view_grid);
				this.initTitleBar(R.string.pics_ad_edit);
			} else {
				mAdapterView = (AdapterView) findViewById(R.id.material_adapter_view_list);
				this.initTitleBar(R.string.text_ad_edit);
			}
			this.setTitleNextImageBtnClickListener(R.drawable.detail, mOnBtnClickedListener);
		}
		this.setTitleBackBtnClickListener(mOnBtnClickedListener);
		
		mAdapterView.setVisibility(View.VISIBLE);
	}
	
	private void startAttributeSetting() {
		MaterialListAdapter adapter = (MaterialListAdapter) mAdapterView.getAdapter();
		List<PlanMaterialrowsBean> select = adapter.getSelectMaterial();
		if (select.size() == 0) {
			Toast.makeText(this, R.string.prompt_no_select_ads, Toast.LENGTH_SHORT).show();
			return;
		}
	
		Intent intent = null;
		if (mType == TYPE_PICS)
			intent = new Intent(this, ImageAttributeSettingActivity.class);
		else
			intent = new Intent(this, TextAttributeSettingActivity.class);
		
		intent.putExtra("offline_mode", mIsOfflineMode);
		if (mIsOfflineMode) {
			
		} else {
			ArrayList<String> adIds = new ArrayList<String>();
			for (int i = 0; i < select.size(); i++) {
				PlanMaterialrowsBean bean = select.get(i);
				Log.v(TAG, "get select: " + bean);
				adIds.add(Integer.toString(bean.adId));
			}			
			intent.putStringArrayListExtra("ids", adIds);
		}
		PlanMaterialrowsBean ad = select.get(0);
		for (PlanMaterialparamsBean param : ad.params) {
			intent.putExtra(Integer.toString(param.adParamId), param.adParamValue);
		}
		startActivityForResult(intent, 0);
	}
	
	private void startTimeSetting() {
		MaterialListAdapter adapter = (MaterialListAdapter) mAdapterView.getAdapter();
		List<PlanMaterialrowsBean> select = adapter.getSelectMaterial();
		if (select.size() == 0) {
			Toast.makeText(this, R.string.prompt_no_select_ads, Toast.LENGTH_SHORT).show();
			return;
		}

		Intent intent = new Intent(this, TimeSettingActivity.class);
		intent.putExtra("offline_mode", mIsOfflineMode);
		
		if (mIsOfflineMode) {
			intent.putParcelableArrayListExtra("periods", select.get(0).periods);
		} else {
			ArrayList<String> adIds = new ArrayList<String>();
			for (int i = 0; i < select.size(); i++) {
				PlanMaterialrowsBean bean = select.get(i);
				adIds.add(Integer.toString(bean.adId));
			}
			intent.putStringArrayListExtra("ids", adIds);
		}
		startActivityForResult(intent, 2);
	}
	
	private void onDelAd() {
		MaterialListAdapter adapter = (MaterialListAdapter) mAdapterView.getAdapter();
		List<PlanMaterialrowsBean> select = adapter.getSelectMaterial();
		if (select.size() == 0) {
			Toast.makeText(this, R.string.prompt_no_select_ads, Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (mIsOfflineMode) {
			for (PlanMaterialrowsBean bean1 : select) {
				mMaterialList.remove(bean1);
			}
			mOfflinePkgMgr.savePlanMaterial(mPlanMaterialBean, mPlanBean.name, mType);
			isChange = true;
			adapter.clearSelect();
			adapter.notifyDataSetChanged();			
		} else {
			ArrayList<String> adIds = new ArrayList<String>();
			for (int i = 0; i < select.size(); i++) {
				PlanMaterialrowsBean bean = select.get(i);
				adIds.add(Integer.toString(bean.adId));
			}
			
			HttpClientDownloader.getInstance().adDel(adIds, mHandler, MSG_DEL_AD);
		}
	}
	
	void startAddMaterial() {
		Intent intent = null;
		if (mType == TYPE_PICS) {
			if (mIsOfflineMode) {
				intent = new Intent(this, OfflinePicSelectActivity.class);
				intent.putExtra("fileType", MaterialParams.TYPE_AD_PICTURE);
			} else {
				intent = new Intent(this, PictureSelectActivity.class);
			}
		} else {
			intent = new Intent(this, TextSelectActivity.class);
		}
		intent.putExtra("start_by", "edit");
		startActivityForResult(intent, 1);
	}
	
	@Override
	public void onBackPressed() {
		Intent data = new Intent();
		int count = mMaterialList != null ? mMaterialList.size() : 0;
		data.putExtra("count", count);
		setResult(RESULT_OK, data);
		super.onBackPressed();
	}
	
	private void showPopupMenu(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.material_edit, popup.getMenu());
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if( item.getItemId() == R.id.select_all ){
					MaterialListAdapter adapter = (MaterialListAdapter) mAdapterView.getAdapter();
					adapter.selectAll();
					adapter.notifyDataSetChanged();
				}else if( item.getItemId() == R.id.unselect ){
					MaterialListAdapter adapter = (MaterialListAdapter) mAdapterView.getAdapter();
					//adapter.selectReverse();
					adapter.clearSelect();
					adapter.notifyDataSetChanged();
				}
				return false;
			}
		});
		popup.show();
	}

	private View.OnClickListener mOnBtnClickedListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if( v.getId() == R.id.next_step ){
				showPopupMenu(v);
			}else if( v.getId() == R.id.back_btn ){
				Intent data = new Intent();
				int count = mMaterialList != null ? mMaterialList.size() : 0;
				data.putExtra("count", count);
				data.putExtra("isChange", isChange);
				setResult(RESULT_OK, data);
				finish();
			}else if( v.getId() == R.id.btn_add_material ){
				startAddMaterial();
			}else if( v.getId() == R.id.btn_attribute_setting ){
				startAttributeSetting();
			}else if( v.getId() == R.id.btn_time_setting ){
				startTimeSetting();
			}else if( v.getId() == R.id.btn_delete_ad ){
				onDelAd();
			}
		}
	};
	
	private List<PlanMaterialrowsBean> mMaterialList;
	
	private void onGetMaterialList(String text) {
		if (text == null) {
			return;
		}
		
		PlanMaterialBean bean = PlanMaterialBeanParser.parsePlanMaterialBean(text);
		if (bean == null) {
			return;
		}
		
		mMaterialList = bean.rows;
		MaterialListAdapter adapter = null;
		if (mType == TYPE_PICS) {
			adapter = new ImageMaterialListAdapter(this, mMaterialList);
		} else {
			adapter = new TextMaterialListAdapter(this, mMaterialList);
		}
		adapter.setEditable(!isFromTerminal);
		mAdapterView.setAdapter(adapter);
	}
	
	private void onDelAd(String text) {
		Log.v(TAG, "onDelAd: " + text);
		if (text == null) {
			return;
		}
		
		BaseBean bean = BaseBeanParser.parseBaseBean(text);
		if (bean == null) {
			return;
		}
		
		if (bean.code == 1) {
			MaterialListAdapter adapter = (MaterialListAdapter) mAdapterView.getAdapter();
			List<PlanMaterialrowsBean> select = adapter.getSelectMaterial();
			for (PlanMaterialrowsBean bean1 : select) {
				mMaterialList.remove(bean1);
			}
			isChange = true;
			adapter.clearSelect();
			adapter.notifyDataSetChanged();			
		} else if (bean.code == -2) {
			returnToLogin();
		} else {
			reportToast(bean.msg);
		}
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
			Toast.makeText(this, "添加素材成功", Toast.LENGTH_SHORT).show();
			if (mType == TYPE_PICS)
				HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getImageListUrl(null, 1, 100, mContractId), mHandler, MSG_GET_MATERIAL_LIST);
			else
				HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getTextListUrl(null, 1, 100, mContractId), mHandler, MSG_GET_MATERIAL_LIST);

		} else if (bean.code == -2) {
			returnToLogin();
		} else {
			reportToast(bean.msg);
		}
	}
	
	private static final int MSG_GET_MATERIAL_LIST = 0;
	private static final int MSG_DEL_AD = 1;
	private static final int MSG_EDIT_CONTRACT = 2;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
			{
				switch (msg.arg1) {
				case MSG_GET_MATERIAL_LIST:
					onGetMaterialList((String) msg.obj);
					break;

				case MSG_DEL_AD:
					onDelAd((String) msg.obj);
					break;
					
				case MSG_EDIT_CONTRACT:
					onEditContract((String) msg.obj);
					break;

				}
			}
			break;
		}
		}
	};

	private ImageButton btnBack;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 0) {
			if (resultCode == Activity.RESULT_OK) {
				isChange = data.getBooleanExtra("isChange", false);
				MaterialListAdapter adapter = (MaterialListAdapter) mAdapterView.getAdapter();
				List<PlanMaterialrowsBean> select = adapter.getSelectMaterial();
				for (PlanMaterialrowsBean ad : select) {
					for (PlanMaterialparamsBean param : ad.params) {
						param.adParamValue = data.getStringExtra(Integer.toString(param.adParamId));
					}
				}
				if (mIsOfflineMode) {
					mOfflinePkgMgr.savePlanMaterial(mPlanMaterialBean, mPlanBean.name, mType);
				}
			}
		} else if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
				isChange = data.getBooleanExtra("isChange", false);
				if (mType == TYPE_PICS) {
					if (mIsOfflineMode) {
//						ArrayList<String> selectResIds = data.getStringArrayListExtra("select_pic_param");
						ArrayList<String> selectResUrls = data.getStringArrayListExtra("select_pic_url");
						new OfflineReleaseTools(MaterialEditActivity.this).addOfflinePlanMaterial(mPlanBean, mPlanMaterialBean, selectResUrls, TYPE_PICS,
								new OfflineReleaseTools.OnTaskFinishListener() {
									@Override
									public void onFinished(int errcode) {
										if (errcode == 0) {
											MaterialListAdapter adapter = (MaterialListAdapter) mAdapterView.getAdapter();
											adapter.notifyDataSetChanged();
										}
									}
								});
					} else {
						ArrayList<String> resIds = data.getStringArrayListExtra("select_pic_param");
						Log.v(TAG, "add : " + resIds);
						HttpClientDownloader.getInstance().planEdit(mContractId, mContractName, resIds, mHandler, MSG_EDIT_CONTRACT);
					}
				} else {
					if (mIsOfflineMode) {
						ArrayList<String> contentsList = data.getStringArrayListExtra("select_text_contents");
						new OfflineReleaseTools(MaterialEditActivity.this).addOfflinePlanMaterial(mPlanBean, mPlanMaterialBean, contentsList, TYPE_TEXT,
								new OfflineReleaseTools.OnTaskFinishListener() {
									@Override
									public void onFinished(int errcode) {
										if (errcode == 0) {
											MaterialListAdapter adapter = (MaterialListAdapter) mAdapterView.getAdapter();
											adapter.notifyDataSetChanged();
										}
									}
								});
					} else {
						ArrayList<String> resIds = data.getStringArrayListExtra("select_pic_param");
						Log.v(TAG, "add : " + resIds);
						HttpClientDownloader.getInstance().planEdit(mContractId, mContractName, resIds, mHandler, MSG_EDIT_CONTRACT);											
					}
				}
			}
		} else if (requestCode == 2) {
			
			if (resultCode == Activity.RESULT_OK) {
				isChange = data.getBooleanExtra("isChange", false);
				ArrayList<FindAdPeriodsperiodsBean> periods = data.getParcelableArrayListExtra("periods");
				MaterialListAdapter adapter = (MaterialListAdapter) mAdapterView.getAdapter();
				List<PlanMaterialrowsBean> select = adapter.getSelectMaterial();
				for (PlanMaterialrowsBean ad : select) {
					ad.periods = periods;
				}
				if (mIsOfflineMode) {
					mOfflinePkgMgr.savePlanMaterial(mPlanMaterialBean, mPlanBean.name, mType);
				}
			}
		}
	}
	
	//此界面返回时需要传递数据，所以滑动返回时不能直接finish
    private static final int XDISTANCE_MIN = 80;
    private static final int YDISTANCE_MIN = 100;
    private float xDown;
    private float yDown;
    private float xMove;
    private float yMove;
    private boolean isBack;

	private boolean isChange;
    
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
              xDown = event.getRawX();
              yDown = event.getRawY();
              break;
        case MotionEvent.ACTION_MOVE:
              xMove = event.getRawX();
              yMove= event.getRawY();
              //滑动的距离
              int distanceX = (int) (xMove - xDown);
              int distanceY= (int) (yMove - yDown);
              //关闭Activity需满足以下条件：
              if(distanceX > XDISTANCE_MIN &&(distanceY<YDISTANCE_MIN&&distanceY>-YDISTANCE_MIN)) {
            	  isBack = true;
            	  btnBack.performClick();
              }
              break;
        case MotionEvent.ACTION_UP:
              break;
        default:
              break;
        }
		 return isBack ? false : super.dispatchTouchEvent(event);
	}
}
