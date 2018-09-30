package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Teacher extends AdNode {
	public int id;
	public String title;
	public String subTitle1;
	public String subTitle2;
	public String desc;
	public String photo;
	public int duration = 10;
	public int type;

	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("teacher");
		setAttribute(elm, "id", Integer.toString(id));
		setAttribute(elm, "name", title);
		setAttribute(elm, "lesson", subTitle1);
		setAttribute(elm, "title", subTitle2);
		setAttribute(elm, "photo", photo);
		setAttribute(elm, "duration", Integer.toString(duration));
		setAttribute(elm, "type", Integer.toString(type));
		elm.setTextContent(desc);
		
		return elm;
	}

}
