package com.routon.inforelease.offline;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import com.routon.widgets.Toast;

import com.routon.ad.pkg.EClassPkgTools;
import com.routon.ad.pkg.PkgTools;
import com.routon.ad.pkg.ResPkgTools;
import com.routon.ad.pkg.SNoticePkgTools;
import com.routon.inforelease.classinfo.ClassInfoListFragment;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.json.PlanMaterialBean;
import com.routon.inforelease.json.PlanMaterialrowsBean;
import com.routon.inforelease.json.SNoticeListrowsBean;
import com.routon.inforelease.plan.PlanListFragment;

public class OfflineReleaseTools {
	public static final int FILE_TYPE_AD = 0;
	public static final int FILE_TYPE_ECLASS = 1;
	public static final int FILE_TYPE_RESPKG = 2;
	public static final int FILE_TYPE_SNOTICE = 3;

	private Context mContext;
	
	private String mPath;
	private int mType;
	
	public OfflineReleaseTools(Context context) {
		mContext = context;
		
		enableBluetooth();
	}
	public OfflineReleaseTools(Context context,boolean isOpenBluetooth) {
		mContext = context;
		
	}
	
	private void enableBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
	        if (!bluetoothAdapter.isEnabled()) {
	        	bluetoothAdapter.enable();
	        }
        }
	}
	
	/*
	public void startPkgAd(int contractId, boolean hasEClassPriviledge) {	
		mPath = new File(mContext.getCacheDir(), "ad.zip").getAbsolutePath();
		mType = FILE_TYPE_AD;
//		if (new File(mContext.getCacheDir(), "ad.zip").exists()) {
//			uploadOffline();
//			return;
//		}
		
		PkgTools pt = new PkgTools();
		pt.setOnPackListener(new PkgTools.OnPackListener() {
			
			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}
			
			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Toast.makeText(mContext, "制作离线包完成", Toast.LENGTH_SHORT).show();
					//showUploadDlg();
					uploadOffline();
				} else {
					Toast.makeText(mContext, "制作离线包失败", Toast.LENGTH_SHORT).show();
				}
			}
		});
		pt.startMakePkg(contractId, new File(mContext.getCacheDir(), "ad"), mPath, hasEClassPriviledge);
		showProg(0);
	}
	*/
	
	public void startPkgAd(final PlanListrowsBean bean, boolean hasEClassPriviledge) {	
		mPath = new File(mContext.getCacheDir(), "ad.zip").getAbsolutePath();
		mType = FILE_TYPE_AD;
		
		PkgTools pt = new PkgTools();
		pt.setOnPackListener(new PkgTools.OnPackListener() {
			
			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}
			
			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Toast.makeText(mContext, "制作离线包完成", Toast.LENGTH_SHORT).show();
					//showUploadDlg();
					uploadOffline(bean);
				} else {
					Toast.makeText(mContext, "制作离线包失败", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		if (bean.name != null && bean.name.length() > 0) {
			File ad_cache_dir = OfflinePackageMgr.getOfflinePkgDir(mContext, OfflinePackageMgr.TYPE_AD);
			File ad_dir = new File(ad_cache_dir, bean.name);
			pt.startMakePkg(ad_dir, mPath, hasEClassPriviledge);			
		} else {
			pt.startMakePkg(bean.contractId, new File(mContext.getCacheDir(), "ad"), mPath, hasEClassPriviledge);
		}
		showProg(0);
	}

	public void startOfflineCache(PlanListrowsBean bean) {	
		PkgTools pt = new PkgTools();
		pt.setOnPackListener(new PkgTools.OnPackListener() {
			
			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}
			
			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Toast.makeText(mContext, "离线缓存完成", Toast.LENGTH_SHORT).show();
					//showUploadDlg();
					//uploadOffline();
				} else {
					Toast.makeText(mContext, "离线缓存失败", Toast.LENGTH_SHORT).show();
				}
			}
		});
		pt.startOfflineCache(bean, OfflinePackageMgr.getOfflinePkgDir(mContext, OfflinePackageMgr.TYPE_AD));
		showProg(0);
	}
	
	public interface OnTaskFinishListener {
		void onFinished(int errcode);
	}
	
	public PlanListrowsBean startMakeOfflinePlan(List<String> imageList, List<String> textList, final OnTaskFinishListener listener) {	
		PkgTools pt = new PkgTools();
		pt.setOnPackListener(new PkgTools.OnPackListener() {
			
			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}
			
			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Toast.makeText(mContext, "离线广告制作完成", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(PlanListFragment.ACTION_PLAN_LIST_CHANGED);
					mContext.sendBroadcast(intent);
					//showUploadDlg();
					//uploadOffline();
				} else {
					Toast.makeText(mContext, "离线广告制作失败", Toast.LENGTH_SHORT).show();
				}
				
				if (listener != null) {
					listener.onFinished(errcode);
				}
			}
		});
		showProg(0);
		return pt.makeNewOfflinePlan(imageList, textList, OfflinePackageMgr.getOfflinePkgDir(mContext, OfflinePackageMgr.TYPE_AD));
	}
	
	// 离线节目添加素材
	public void addOfflinePlanMaterial(PlanListrowsBean bean, PlanMaterialBean materialBean, List<String> materialList, int type, final OnTaskFinishListener listener) {
		PkgTools pt = new PkgTools();
		pt.setOnPackListener(new PkgTools.OnPackListener() {
			
			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}
			
			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
//					Toast.makeText(mContext, "广告素材添加完成", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(PlanListFragment.ACTION_PLAN_LIST_CHANGED);
					mContext.sendBroadcast(intent);
					//showUploadDlg();
					//uploadOffline();
				} else {
//					Toast.makeText(mContext, "广告素材添加失败", Toast.LENGTH_SHORT).show();
				}
				
				if (listener != null) {
					listener.onFinished(errcode);
				}
			}
		});
		showProg(0);
		
		File ad_cache_dir = OfflinePackageMgr.getOfflinePkgDir(mContext, OfflinePackageMgr.TYPE_AD);
		File ad_dir = new File(ad_cache_dir, bean.name);
		pt.addOfflinePlanMaterial(materialBean, materialList, type, ad_dir);
	}
	
	public void saveOfflinePlanMaterial(PlanListrowsBean planBean, List<PlanMaterialrowsBean> imageMaterials, List<PlanMaterialrowsBean> textMaterials, final OnTaskFinishListener listener) {
		PkgTools pt = new PkgTools();
		pt.setOnPackListener(new PkgTools.OnPackListener() {
			
			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}
			
			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Intent intent = new Intent(PlanListFragment.ACTION_PLAN_LIST_CHANGED);
					mContext.sendBroadcast(intent);
				} 
				
				if (listener != null) {
					listener.onFinished(errcode);
				}
			}
		});
		showProg(0);
		
		File ad_cache_dir = OfflinePackageMgr.getOfflinePkgDir(mContext, OfflinePackageMgr.TYPE_AD);
		File ad_dir = new File(ad_cache_dir, planBean.name);
		pt.saveOfflinePlanMaterial(planBean, imageMaterials, textMaterials, ad_dir);
	}
	
	public ClassInfoListdatasBean saveOfflineClassInfo(ClassInfoListdatasBean bean, final OnTaskFinishListener listener) {	
		EClassPkgTools pt = new EClassPkgTools();
		pt.setOnPackListener(new EClassPkgTools.OnPackListener() {
			
			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}
			
			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Toast.makeText(mContext, "离线班牌保存成功", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(ClassInfoListFragment.ACTION_CLASS_INFO_LIST_CHANGED);
					mContext.sendBroadcast(intent);
					//showUploadDlg();
					//uploadOffline();
				} else {
					Toast.makeText(mContext, "离线班牌保存失败", Toast.LENGTH_SHORT).show();
				}
				
				if (listener != null) {
					listener.onFinished(errcode);
				}
			}
		});
		showProg(0);
		return pt.saveClassInfo(bean, OfflinePackageMgr.getOfflinePkgDir(mContext, OfflinePackageMgr.TYPE_CLASS_INFO));
	}
	
	public void startPkgEClass(List<ClassInfoListdatasBean> dataList) {
		mPath = new File(mContext.getCacheDir(), "eclass.zip").getAbsolutePath();
		mType = FILE_TYPE_ECLASS;
		
		EClassPkgTools pt = new EClassPkgTools();
		pt.setOnPackListener(new EClassPkgTools.OnPackListener() {

			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}

			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Toast.makeText(mContext, "制作班牌离线包完成", Toast.LENGTH_SHORT).show();
					uploadOffline();
				} else {
					Toast.makeText(mContext, "制作班牌离线包失败", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		pt.startMakePkg(dataList, new File(mContext.getCacheDir(), "eclass"), mPath);			
		showProg(0);
	}
	
	public void startCacheEClass(List<ClassInfoListdatasBean> dataList) {
		EClassPkgTools pt = new EClassPkgTools();
		pt.setOnPackListener(new EClassPkgTools.OnPackListener() {

			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}

			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Toast.makeText(mContext, "班牌离线缓存完成", Toast.LENGTH_SHORT).show();
					//showUploadDlg();
				} else {
					Toast.makeText(mContext, "班牌离线缓存失败", Toast.LENGTH_SHORT).show();
				}
			}
			
		});

		pt.startOfflineCache(dataList, OfflinePackageMgr.getOfflinePkgDir(mContext, OfflinePackageMgr.TYPE_CLASS_INFO).getAbsolutePath());
		showProg(0);
	}
	
	public void startPkgSNotice(List<String> dataList) {
		mPath = new File(mContext.getCacheDir(), "snotice.zip").getAbsolutePath();
		mType = FILE_TYPE_SNOTICE;
		
		SNoticePkgTools pt = new SNoticePkgTools();
		pt.setOnPackListener(new SNoticePkgTools.OnPackListener() {

			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}

			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Toast.makeText(mContext, "制作班牌离线包完成", Toast.LENGTH_SHORT).show();
					//showUploadDlg();
					uploadOffline();
				} else {
					Toast.makeText(mContext, "制作班牌离线包失败", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		pt.startMakePkg(dataList, new File(mContext.getCacheDir(), "eclass"), mPath);
		showProg(0);
	}
	
	public void startCacheSNotice(List<SNoticeListrowsBean> dataList) {
		SNoticePkgTools pt = new SNoticePkgTools();
		pt.setOnPackListener(new SNoticePkgTools.OnPackListener() {

			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}

			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Toast.makeText(mContext, "制作实时信息离线包完成", Toast.LENGTH_SHORT).show();
					//showUploadDlg();
//					uploadOffline();
				} else {
					Toast.makeText(mContext, "制作实时信息离线包失败", Toast.LENGTH_SHORT).show();
				}
			}
			
		});

		pt.startOfflineCache(dataList, OfflinePackageMgr.getOfflinePkgDir(mContext, OfflinePackageMgr.TYPE_SNOTICE).getAbsolutePath());
	}
	
	public void startPkgResPkg(List<ClassInfoListdatasBean> dataList) {
		mPath = new File(mContext.getCacheDir(), "respkg.zip").getAbsolutePath();
		mType = FILE_TYPE_RESPKG;
		
		ResPkgTools pt = new ResPkgTools(mContext.getCacheDir());
		pt.setOnPackListener(new ResPkgTools.OnPackListener() {

			@Override
			public void onProgress(int progress) {
				showProg(progress);
			}

			@Override
			public void onFinished(int errcode) {
				if (dlg != null) {
					dlg.dismiss();
				}
				
				if (errcode == 0) {
					Toast.makeText(mContext, "制作专题离线包完成", Toast.LENGTH_SHORT).show();
					//showUploadDlg();
					uploadOffline();
				} else {
					Toast.makeText(mContext, "制作专题离线包失败", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		pt.startMakePkg(dataList);
		showProg(0);
	}
	
	private ProgressDialog dlg;
	
	private void showProg(int progress) {
		if (dlg == null) {
			 dlg = new ProgressDialog(mContext);
			 dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			 dlg.setCancelable(false);
		}
		
		dlg.setProgress(progress);
		dlg.show();
	}
	
	private void showUploadDlg() {
		Dialog dlg = new AlertDialog.Builder(mContext)
			.setTitle("发布离线计划")
			.setMessage("制作离线包成功")
			.setPositiveButton("发布", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				uploadOffline();
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).create();
		dlg.show();
	}
	
	private void uploadOffline() {
		Intent intent = new Intent(mContext, DeviceSelectActivity.class);
		intent.putExtra("path", mPath);
		intent.putExtra("type", mType);
		mContext.startActivity(intent);
	}
	
	private void uploadOffline(PlanListrowsBean bean) {
		Intent intent = new Intent(mContext, DeviceSelectActivity.class);
		intent.putExtra("path", mPath);
		Log.d("offlinerelease","mBean"+bean);
		intent.putExtra("data", bean);
		intent.putExtra("type", mType);
		intent.putExtra("plan_id", String.valueOf(bean.contractId));
		intent.putExtra("plan_name", bean.name);
		mContext.startActivity(intent);
	}
}
