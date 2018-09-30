package com.routon.inforelease.plan.create;

import java.io.Serializable;

public class GroupInfo {

	@com.routon.inforelease.widget.treeView.TreeNodeId
	private int id;
	@com.routon.inforelease.widget.treeView.TreeNodeLabel
	private String name;
	@com.routon.inforelease.widget.treeView.TreeNodePid
	private int pId;
	private String pName;
	private boolean isChecked;
	private boolean open;
	private String text;
	private int childrencount;
	private String showName;
//	private boolean enable;
//	private String state; 
	
//	private ArrayList<GroupInfo> childList = new ArrayList<GroupInfo>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public int getPid() {
		return pId;
	}
	public void setPid(int pid) {
		this.pId = pid;
	}
	
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getChildrencount() {
		return childrencount;
	}
	public void setChildrencount(int childrencount) {
		this.childrencount = childrencount;
	}
//	public ArrayList<GroupInfo> getChildList() {
//		return childList;
//	}
//	public void setChildList(ArrayList<GroupInfo> childList) {
//		this.childList = childList;
//	}
//
//	public void addChild(GroupInfo child){
//		childList.add(child);
//	}
	public String getpName() {
		return pName;
	}
	public void setpName(String pName) {
		this.pName = pName;
	}
}
