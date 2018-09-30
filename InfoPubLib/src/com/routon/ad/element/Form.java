package com.routon.ad.element;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Form extends AdElement {
	String align;
	
	private List<AdElement> elements = new ArrayList<AdElement>();

	public Form(int id, int width, int height) {
		super(id, width, height);
	}
	
	public void addAdElement(AdElement element) {
		elements.add(element);
	}
	
	public Document toXml() {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element form = toElement(doc);
			doc.appendChild(form);
			
			return doc;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("form");
		setAttribute(elm);
		
		for (AdElement adElm : elements) {
			elm.appendChild(adElm.toElement(doc));
		}

		return elm;
	}
}
