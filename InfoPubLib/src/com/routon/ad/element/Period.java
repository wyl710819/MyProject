package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Period extends AdNode {
	public int loop = 1;								
	public String begintime = "000000";
	public String endtime = "240000";
	public int maxcount = 65535;
	
	public Period() {
		
	}
	
	public Period(int loop, int maxloop, String begintime, String endtime) {
		this.loop = loop;
		this.maxcount = maxloop;
		this.begintime = begintime;
		
		if (endtime.equals("000000")) {
			this.endtime = "240000";
		} else {
			this.endtime = endtime;
		}
	}
	
	@Override
	public Element toElement(Document doc) {
		Element p = doc.createElement("p");
		setAttribute(p, "loop", Integer.toString(loop));
		
		String time = begintime.substring(0, 2) + endtime.substring(0, 2)
				+ begintime.substring(2, 4) + endtime.substring(2, 4)
				+ begintime.substring(4, 6) + endtime.substring(4, 6);
		setAttribute(p, "time", time);
		p.setTextContent(Integer.toString(maxcount));
		return p;
	}

}
