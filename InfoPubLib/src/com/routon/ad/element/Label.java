package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Label extends AdElement {

	public String color;
	public String bgcolor;
	public String background;
	public int alpha = 255;

	public String fontname;
	public int fontsize;
	
	String align;
	
	String fly;
	int rate;
	int scroll;	
	
	String playbill;

	public Label(int id, int left, int top, int width, int height, String playbill) {
		super(id, left, top, width, height);
		
		this.playbill = playbill;
	}
	
	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("label");
		setAttribute(elm);
		setAttribute(elm, "color", color);
		setAttribute(elm, "bgcolor", bgcolor);
		setAttribute(elm, "alpha", Integer.toString(alpha));
		setAttribute(elm, "background", background);
		setAttribute(elm, "fontname", fontname);
		setAttribute(elm, "fontsize", Integer.toString(fontsize));
		setAttribute(elm, "align", align);
		setAttribute(elm, "fly", fly);
		setAttribute(elm, "rate", Integer.toString(rate));
		setAttribute(elm, "scroll", Integer.toString(scroll));
		setAttribute(elm, "playbill", playbill);

		return elm;
	}

}
