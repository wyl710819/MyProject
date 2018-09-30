package com.routon.ad.element;

import java.io.Serializable;

public class TemplateEditInfo implements Serializable {
	private String type;
	private float x;
	private float y;
	private String content;
	private float scale;
	private String color;
	private float rotate;
	public String getType() {
		return type;
	}
	
	public TemplateEditInfo(String type,float x,float y,String content,float scale,String color)
	{
		this.type=type;
		this.x=x;
		this.y=y;
		this.content=content;
		this.scale=scale;
		this.color=color;
	}
	public TemplateEditInfo(String type,float x,float y,String content,float scale)
	{
		this.type=type;
		this.x=x;
		this.y=y;
		this.content=content;
		this.scale=scale;
	}
	public TemplateEditInfo()
	{
		
	}
	public void setType(String type) {
		this.type = type;
	}
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public float getScale() {
		return scale;
	}
	public void setScale(float scale) {
		this.scale = scale;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public float getRotate() {
		return rotate;
	}
	public void setRotate(float rotate) {
		this.rotate = rotate;
	}
	public void changeX(float x)
	{
		this.x+=x;
	}
	public void changeY(float y)
	{
		this.y+=y;
	}
}
