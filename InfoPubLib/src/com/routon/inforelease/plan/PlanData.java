package com.routon.inforelease.plan;

public class PlanData {
	public String name;
	public String start_time;
	public String end_time;
	public String status;
	private boolean checked;
	
	public PlanData(String name, String start_time, String end_time, String status) {
		this.name = name;
		this.start_time = start_time;
		this.end_time = end_time;
		this.status = status;
	}
	
	public void setChecked(boolean flag) {
		checked = flag;
	}
	
	public boolean isChecked() {
		return checked;
	}
}
