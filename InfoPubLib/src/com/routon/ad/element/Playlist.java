package com.routon.ad.element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Playlist extends AdNode {
	public String datebegin;
	public String dateend;
	public int orderno;
	
	List<Ad> ad_list = new ArrayList<Ad>();
	
	public Playlist() {
	}

	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("playlist");		
		setAttribute(elm, "datebegin", datebegin);
		setAttribute(elm, "dateend", dateend);
		setAttribute(elm, "orderno", Integer.toString(orderno));
		if (ad_list != null) {
			for (Ad ad : ad_list) {
				elm.appendChild(ad.toElement(doc));
			}
		}
		return elm;
	}
	
	public void addAd(Ad ad) {
		ad_list.add(ad);
	}
	
	public List<Ad> getAdList() {
		return ad_list;
	}
}
