package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Panel extends AdElement {

	public Panel(int id, int left, int top, int width, int height) {
		super(id, left, top, width, height);
	}

	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("panel");
		setAttribute(elm);
		
		return elm;
	}

}
