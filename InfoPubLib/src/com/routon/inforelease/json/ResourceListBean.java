package com.routon.inforelease.json;

import java.util.List;

import com.routon.json.BaseBean;



public class ResourceListBean extends BaseBean {
	public String page;
	public int pageSize;
	public int fullListSize;

	public List<ResourceListdatasBean> datas;
 };
