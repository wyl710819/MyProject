package com.routon.inforelease.plan.create;

import java.util.ArrayList;


public class MaterialRequestResult{
	ResultInfo info;
	ArrayList<MaterialItem>  materialLists;
	
	public MaterialRequestResult(){
		info = new ResultInfo();
		materialLists = new ArrayList<MaterialItem>();
	}
	
	public class ResultInfo{
		int code;           // -1失败;0成功
	    String msg;         //结果描述
	    int fullListSize;   //总记录数
	    int page;           //当前页
	    int pageSize;       //每页记录数，默认为10

	}

}