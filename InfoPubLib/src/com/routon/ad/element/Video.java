package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Video extends AdElement {

	String playbill;
	
	public Video(int id, int left, int top, int width, int height) {
		super(id, left, top, width, height);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("video");
		setAttribute(elm);
		
		setAttribute(elm, "playbill", playbill);
		
		return elm;
	}

}
