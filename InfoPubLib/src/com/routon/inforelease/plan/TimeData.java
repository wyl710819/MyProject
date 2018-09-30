package com.routon.inforelease.plan;

public class TimeData {
	public String name;
	public String start_time;
	public String end_time;
	public int repeat_time;
	public int max_repeat_time;
	
	public TimeData() {
		
	}
	
	public TimeData(String name, String start_time, String end_time, int repeat_time, int max_repeat_time) {
		this.name = name;
		this.start_time = start_time;
		this.end_time = end_time;
		this.repeat_time = repeat_time;
		this.max_repeat_time = max_repeat_time;
	}
}
