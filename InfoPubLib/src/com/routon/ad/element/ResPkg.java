package com.routon.ad.element;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ResPkg extends AdNode {
	public int id;
	public String title;
	public String url;
	
	public List<ResPkgFile> files = new ArrayList<ResPkgFile>();
	
	public String version;
	public String file_url;

	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("pkg");
		setAttribute(elm, "id", Integer.toString(id));
		setAttribute(elm, "title", title);
		setAttribute(elm, "url", url);
		
		for (ResPkgFile file : files) {
			elm.appendChild(file.toElement(doc));
		}
		
		return elm;
	}

}
