package com.routon.inforelease.json;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.routon.inforelease.plan.AdParams;
import com.routon.inforelease.util.TimeUtils;



public class PlanMaterialrowsBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1821923606572134198L;
	public int fileID;
	public String thumbnail;
	public int adId;
	public String name;
	public List<PlanMaterialparamsBean> params;
	public int type;//图片还是文字 1 文字 3图片
	
	public long modify;
	
	public final static int TYPE_PIC = 3;
	public final static int TYPE_TEXT = 1;
	
	public ArrayList<FindAdPeriodsperiodsBean> periods;  // for offline cache
	
	public static PlanMaterialrowsBean makeNewPlanMaterialrowsBean(){
		PlanMaterialrowsBean bean = new PlanMaterialrowsBean();
		bean.adId = -1;
		return bean;
	}
 };
