package com.routon.inforelease.json;

import java.io.Serializable;




public class PlanMaterialparamsBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1639654034737953870L;
	public int adParamId;
	public String adParamValue;
	
	public PlanMaterialparamsBean() {
		
	}
	
	public PlanMaterialparamsBean(int id, String value) {
		adParamId = id;
		adParamValue = value;
	}
 };
