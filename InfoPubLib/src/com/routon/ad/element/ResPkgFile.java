package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ResPkgFile extends AdNode {
	public int id;
	public String url;
	public int duration = 30;
	public String content;

	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("file");
		setAttribute(elm, "id", Integer.toString(id));
		setAttribute(elm, "url", url);
		setAttribute(elm, "duration", Integer.toString(duration));
		elm.setTextContent(content);
		
		return elm;
	}

}
