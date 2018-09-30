package com.routon.inforelease.json;

import java.io.Serializable;
import java.util.List;



public class ClassInfoListdatasBean implements Serializable {
	public static final int STATUS_AUDIT_THROUGH = 1;//审核通过
	public static final int STATUS_AUDIT_TOBE = 2;//待审核
	public static final int STATUS_AUDIT_NOTTHROUGH = 3;//审核不通过
	/**
	 * 
	 */
	private static final long serialVersionUID = 6908671180085371487L;
	public int id;
	public List<ClassInfoListfilesBean> files;
	public String duration;
	public String title;
	public String groupIds;
	public String desc;
	public String subtitle2;
	public int status;
	public String subtitle1;
	public String groupNames;
	public int type;
	public String name;
	public String startTime;
	public String endTime;
	public int editPkg;
	public String editPkgUrl;
	public int priority;
	public int publishStatus;
	public String attitude;//审核意见
	
	public boolean isOffLine(){
		if (name != null && !name.isEmpty()) {
			return true;
		}
		return false;
	}
 };
