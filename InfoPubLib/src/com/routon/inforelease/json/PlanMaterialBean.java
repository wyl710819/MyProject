package com.routon.inforelease.json;

import java.util.List;



public class PlanMaterialBean {
	public int total;
	public List<PlanMaterialrowsBean> rows;
	
	public List<PlanMaterialrowsBean> getRows() {
		return rows;
	}
	public void setRows(List<PlanMaterialrowsBean> rows) {
		this.rows = rows;
	}
	
 };
