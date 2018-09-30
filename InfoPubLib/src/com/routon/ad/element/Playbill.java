package com.routon.ad.element;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Playbill extends AdNode {
	List<Playlist> playlist = new ArrayList<Playlist>();

	@Override
	public Element toElement(Document doc) {
		Element elm = doc.createElement("playbill");
		if (playlist != null) {
			for (Playlist pl : playlist) {
				elm.appendChild(pl.toElement(doc));
			}
		}
		return elm;
	}
	
	public void addPlaylist(Playlist pl) {
		playlist.add(pl);
	}
	
	public Document toXml() {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("root");
			doc.appendChild(root);

			Element playbill = toElement(doc);
			root.appendChild(playbill);
		
			return doc;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public List<Playlist> getPlaylist() {
		return playlist;
	}
}
