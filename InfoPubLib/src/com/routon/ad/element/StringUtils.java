package com.routon.ad.element;

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

	public static String getFileName(String url) {
		int i = url.lastIndexOf("/");
		if (i >= 0) {
			return url.substring(i + 1);
		}
		
		return url;
	}
}
