package com.routon.smartcampus.swtchCtrl.treeAdapter;

import java.util.List;

import com.routon.inforelease.json.TerminalListdatasBean;



public class DataBean {

    public static final int PARENT_ITEM = 0;//父布局
    public static final int CHILD_ITEM = 1;//子布局

    public int type;// 显示类型
    public boolean isExpand;// 是否展开
    public List<DataBean> childBean;

    //parent
    public String ID;
	public int pid;
	public String pName;
	
	//child
	public TerminalListdatasBean mTerminaldata;
	public boolean isLast;
  
}
