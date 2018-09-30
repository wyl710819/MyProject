package com.routon.ad.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AdElement extends AdNode {
	public int id;
	public int left;
	public int top;
	public int width;
	public int height;
	
	public AdElement(int id, int width, int height) {
		this.id = id;
		this.width = width;
		this.height = height;
	}
	
	public AdElement(int id, int left, int top, int width, int height) {
		this.id = id;
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
	}
	
	protected void setAttribute(Element elm) {
		setAttribute(elm, "id", Integer.toString(id));
		setAttribute(elm, "width", Integer.toString(width));
		setAttribute(elm, "height", Integer.toString(height));
		setAttribute(elm, "left", Integer.toString(left));
		setAttribute(elm, "top", Integer.toString(top));
	}
}
