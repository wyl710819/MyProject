package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Style extends AdNode {
	public String FontName;
	public int FontSize;
	public String color = "#042398";
	public String bgcolor = "#FFFFFF";
	public String alpha = "80";
	public String background;
	
	public Title title;
	
	public String fly;
	
	public int rate = 10;
	public int scroll = 2;
	public String effect;

	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("style");
		createNode(doc, elm, "FontName", FontName);
		createNode(doc, elm, "FontSize", Integer.toString(FontSize));
		createNode(doc, elm, "color", color);
		createNode(doc, elm, "bgcolor", bgcolor);
		createNode(doc, elm, "alpha", alpha);
		createNode(doc, elm, "background", background);
		
		if (title != null) {
			elm.appendChild(title.toElement(doc));
		}
		
		createNode(doc, elm, "fly", fly);
		
		createNode(doc, elm, "rate", Integer.toString(rate));
		createNode(doc, elm, "scroll", Integer.toString(scroll));
		createNode(doc, elm, "effect", effect);
		
		return elm;
	}

}
