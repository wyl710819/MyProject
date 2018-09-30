package com.routon.inforelease.json;




public class SNoticeListrowsBean {
	public static final int STATUS_AUDIT_THROUGH = 1;//审核通过
	public static final int STATUS_AUDIT_TOBE = 2;//待审核
	public static final int STATUS_AUDIT_NOTTHROUGH = 3;//审核不通过
	public int id;
	public String createTime;
	public String fontColor;
	public int status;
	public int modifyUser;
	public String font;
	public int sorting;
	public String notice;
	public String startTime;
	public String endTime;
	public boolean isPublish;
	public int fontSize;
	public String modifyTime;
	public String groups;
	public String bgcolor;
	public String name;
	public String attitude;
	public boolean isOffLine(){
		if (name != null && !name.isEmpty()) {
			return true;
		}
		return false;
	}
 };
