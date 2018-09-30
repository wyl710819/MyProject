package com.routon.inforelease.json;

import java.util.List;

import com.routon.json.BaseBean;



public class UserListBean extends BaseBean {
	public int page;
	public int pageSize;
	public int fullListSize;

	public List<UserListdatasBean> datas;
 };
