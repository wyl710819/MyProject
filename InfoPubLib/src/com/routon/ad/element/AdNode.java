package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AdNode {
	public abstract Element toElement(Document doc);
	
	protected Element createNode(Document doc, Element parent, String node, String content) {
		Element elm = doc.createElement(node);
		elm.setTextContent(content);
		parent.appendChild(elm);
		
		return elm;
	}

	protected void setAttribute(Element elm, String name, String value) {
		if (name != null && value != null && elm != null)
			elm.setAttribute(name, value);
	}
}
