package com.routon.inforelease.plan;

public class StringUtils {

	public static int toInteger(String s, int default_value) {
		int val = default_value;
		try {
			val = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			
		}
		return val;
	}
	
	public static String color2String(int color) {
		String strColor = String.format("#%06X", 0xFFFFFF & color);
		return strColor;
	}
}
