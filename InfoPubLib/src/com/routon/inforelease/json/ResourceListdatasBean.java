package com.routon.inforelease.json;




public class ResourceListdatasBean {
	public String content;
	public int resid;
	public int filetypeid;
	public String createtime;
	
	public ResourceListdatasBean() {
		
	}
	
	public ResourceListdatasBean(String content, int resid, int filetypeid) {
		this.content = content;
		this.resid = resid;
		this.filetypeid = filetypeid;
	}
 };
