package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Image extends AdElement {

	String color;
	String bgcolor;
	String background;

	String align;
	
	String fly;
	
	String playbill;

	public Image(int id, int left, int top, int width, int height, String playbill) {
		super(id, left, top, width, height);
		
		this.playbill = playbill;
	}
	
	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("image");
		setAttribute(elm);
		
		setAttribute(elm, "color", color);
		setAttribute(elm, "bgcolor", bgcolor);
		setAttribute(elm, "background", background);
		setAttribute(elm, "align", align);
		setAttribute(elm, "fly", fly);
		setAttribute(elm, "playbill", playbill);

		return elm;
	}

}
