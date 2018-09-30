package com.routon.inforelease.offline;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import com.routon.widgets.Toast;

import com.routon.ad.pkg.BasePkgTools;
import com.routon.ad.pkg.EClassPkgTools;
import com.routon.ad.pkg.PkgTools;
import com.routon.ad.pkg.SNoticePkgTools;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.json.PlanMaterialBean;
import com.routon.inforelease.json.SNoticeListrowsBean;

public class OfflinePackageMgr {
	public static final int TYPE_AD = 0;
	public static final int TYPE_CLASS_INFO = 1;
	public static final int TYPE_SNOTICE = 2;
	
	private Context mContext;
	
	private File mOfflinePkgDir;
	
	private static HashMap<Integer, String> mDirNameMap = new HashMap<Integer, String>() {
		{
			put(TYPE_AD, "ad");
			put(TYPE_CLASS_INFO, "classinfo");
			put(TYPE_SNOTICE, "snotice");			
		}
	};
	
	public OfflinePackageMgr(Context context) {
		mContext = context;
		
		mOfflinePkgDir = context.getDir("offline", Context.MODE_PRIVATE); // new File(mContext.getCacheDir(), "offline");
	}

	public List<PlanListrowsBean> getPlanList() {
		List<PlanListrowsBean> list = new ArrayList<PlanListrowsBean>();
		File[] dirs = listDir(new File(mOfflinePkgDir, getDirNameByType(TYPE_AD)));
		if (dirs == null)
			return list;
		for (File f : dirs) {
			PlanListrowsBean bean = PkgTools.readPlanListrowsBean(f);
			if (bean != null) {
				list.add(0,bean);
			}
		}
		for (int i = 0; i < list.size()-1; i++) {
			for (int j = 0; j < list.size()-i-1; j++) {
				String str=list.get(j).name;
				String str2=list.get(j+1).name;
				long lon=Long.valueOf(str);
				long lon2=Long.valueOf(str2);
				if (lon<=lon2) {
					PlanListrowsBean bean2 =list.get(j);
					list.set(j, list.get(j+1));
					list.set(j+1, bean2);
				}
			}
		}
		return list;
	}
	
	public void savePlanInfo(PlanListrowsBean planBean) {
		File ad_cache_dir = new File(mOfflinePkgDir, getDirNameByType(TYPE_AD));
		File ad_dir = new File(ad_cache_dir, planBean.name);
		BasePkgTools.mkdir(ad_dir);
		PkgTools.writePlanListrowsBean(planBean, ad_dir);
	}
	
	/*
	 * Read material of offline plan
	 * type: 0 - picture  1 - text
	 */
	public PlanMaterialBean getPlanMaterial(String name, int type) {
		File ad_dir = new File(new File(mOfflinePkgDir, getDirNameByType(TYPE_AD)), name);
		File playbill_dir = new File(ad_dir, "playbill");
		File material_file = null;
		if (type == 1) {
			material_file = new File(playbill_dir, BasePkgTools.firstFileName(playbill_dir, "playbill_text.*\\.xml", "playbill_text.xml"));
			return PkgTools.readPlanMaterial(material_file, null);
		} else {
			material_file = new File(playbill_dir, BasePkgTools.firstFileName(playbill_dir, "playbill_picture.*\\.xml", "playbill_picture.xml"));
			return PkgTools.readPlanMaterial(material_file, ad_dir.getAbsolutePath());
		}
	}
	
	public boolean savePlanMaterial(PlanMaterialBean bean, String name, int type) {
		File ad_dir = new File(new File(mOfflinePkgDir, getDirNameByType(TYPE_AD)), name);
		File playbill_dir = new File(ad_dir, "playbill");
		File material_file = null;
		if (type == 1) {
			material_file = new File(playbill_dir, BasePkgTools.firstFileName(playbill_dir, "playbill_text.*\\.xml", "playbill_text.xml"));
		} else {
			material_file = new File(playbill_dir, BasePkgTools.firstFileName(playbill_dir, "playbill_picture.*\\.xml", "playbill_picture.xml"));
		}
		
		return PkgTools.writePlanMaterial(bean, material_file, type);
	}
	
	public List<ClassInfoListdatasBean> getClassInfoList() {
		List<ClassInfoListdatasBean> list = new ArrayList<ClassInfoListdatasBean>();
		File[] dirs = listDir(new File(mOfflinePkgDir, getDirNameByType(TYPE_CLASS_INFO)));
		if (dirs == null)
			return list;
		for (File f : dirs) {
			ClassInfoListdatasBean bean = EClassPkgTools.readClassData(f);
			if (bean != null)
				list.add(0,bean);
		}
		
		for (int i = 0; i < list.size()-1; i++) {
			for (int j = 0; j < list.size()-i-1; j++) {
				String str=list.get(j).name;
				String str2=list.get(j+1).name;
				long lon=Long.valueOf(str.substring(0, str.length()-2));
				long lon2=Long.valueOf(str2.substring(0, str2.length()-2));
				if (lon<=lon2) {
					ClassInfoListdatasBean bean2 =list.get(j);
					list.set(j, list.get(j+1));
					list.set(j+1, bean2);
				}
			}
		}
		
		return list;
		
	}
	
	public boolean saveClassInfo(ClassInfoListdatasBean bean) {
		File eclass_dir = new File(new File(mOfflinePkgDir, getDirNameByType(TYPE_CLASS_INFO)), bean.name);
		return EClassPkgTools.writeClassData(bean, eclass_dir);
	}
	
	public List<SNoticeListrowsBean> getSNoticeList() {
		List<SNoticeListrowsBean> list = new ArrayList<SNoticeListrowsBean>();
		File[] dirs = listDir(new File(mOfflinePkgDir, getDirNameByType(TYPE_SNOTICE)));
		if (dirs == null)
			return list;
		for (File f : dirs) {
			SNoticeListrowsBean bean = SNoticePkgTools.readSNotice(f);
			if (bean != null) {
				list.add(0,bean);
			}
		}
		for (int i = 0; i < list.size()-1; i++) {
			for (int j = 0; j < list.size()-i-1; j++) {
				String str=list.get(j).name;
				String str2=list.get(j+1).name;
				long lon=Long.valueOf(str.substring(0, str.length()-2));
				long lon2=Long.valueOf(str2.substring(0, str2.length()-2));
				if (lon<=lon2) {
					SNoticeListrowsBean bean2 =list.get(j);
					list.set(j, list.get(j+1));
					list.set(j+1, bean2);
				}
			}
		}
		return list;
	}
	
	private File[] listDir(File parent) {
		File[] files = parent.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				
				return false;
			}
			
		});
		
		return files;
	}
	
	public boolean exist(String name, int type) {
		File dir = new File(getOfflinePkgDir(mContext, type), name);
		return dir.exists();
	}
	
	public boolean delete(String name, int type) {
		File dir = new File(getOfflinePkgDir(mContext, type), name);
		
		return EClassPkgTools.deleteDir(dir);
	}
	
	public static File getOfflinePkgDir(Context context, int type) {
		File file = context.getDir("offline", Context.MODE_PRIVATE); // new File(context.getCacheDir(), "offline");
		return new File(file, getDirNameByType(type));
	}
	
	public static String getDirNameByType(int type) {
		String dirName = mDirNameMap.get(type);
		return dirName;
	}

//	public void createClassInfo(String name, List<ClassInfoListdatasBean>)
}
