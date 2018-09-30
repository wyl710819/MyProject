package com.routon.inforelease.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;




public class PlanListrowsBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8030143932516727807L;
	
	public String status;
	public String beginTime;
	public boolean published;
	public int contractId;
	public String endTime;
	public String contractName;
	public String name;
	public String terminalIDs;
	public String templateId;
	
	public String groups;
	public String editPkgUrl;
	public int fileId;
	
	//add by xiaolp 20170821
	
	//图片广告
	public List<PlanMaterialrowsBean> materialList;
	//滚动文字广告
	public List<PlanMaterialrowsBean> subTitleList;
	
	public boolean isOffLine(){
		if (name != null && !name.isEmpty()) {
			return true;
		}
		return false;
	}
	
	public static PlanListrowsBean makeNewPlanListrowsBean(){
		PlanListrowsBean bean = new PlanListrowsBean();
		bean.contractId = -1;//新建时计划id为-1
		bean.materialList  = new ArrayList<PlanMaterialrowsBean>();
		bean.subTitleList  = new ArrayList<PlanMaterialrowsBean>();
		return bean;
	}
	
//	private boolean checked;
//	
//	public boolean isChecked() {
//		return checked;
//	}
//
//	public void setChecked(boolean isChecked) {
//		this.checked = isChecked;
//	}
 };
