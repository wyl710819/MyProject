package com.routon.ad.element;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Ad extends AdNode {
	public int id;
	public String url;
	public String name;
	public int pri;
	public String length = "-1";
	public int volume;
	public String tone;
	public int max = 65535;
	
	public Style style;
	public List<Period> periods = new ArrayList<Period>() {
		{
			add(new Period());
		}
	};
	
	public Ad() {
		
	}
	
	public Ad(int id, String url, String name) {
		this.id = id;
		this.url = url;
		this.name = name;
	}
	
	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("ad");
		
		createNode(doc, elm, "id", Integer.toString(id));
		createNode(doc, elm, "url", url);
		createNode(doc, elm, "name", name);
		createNode(doc, elm, "pri", Integer.toString(pri));
		createNode(doc, elm, "length", length);
		createNode(doc, elm, "volume", Integer.toString(volume));
		createNode(doc, elm, "tone", tone);
		createNode(doc, elm, "max", Integer.toString(max));
		
		if (style != null) {
			elm.appendChild(style.toElement(doc));
		}
		
		Element periods_elm = doc.createElement("periods");
		elm.appendChild(periods_elm);
		for (Period period : periods) {
			periods_elm.appendChild(period.toElement(doc));
		}
		return elm;
	}
	
}
