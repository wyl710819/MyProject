package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Title extends AdNode {
	public int color;
	public int bgcolor;
	public String background;
	public int alpha;
	public String text;

	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("title");
		setAttribute(elm, "color", StringUtils.color2String(color));
		setAttribute(elm, "bgcolor", StringUtils.color2String(bgcolor));
		setAttribute(elm, "background", background);
		setAttribute(elm, "alpha", Integer.toString(alpha));
		
		elm.setTextContent(text);
		
		return elm;
	}

}
