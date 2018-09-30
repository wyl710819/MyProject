package com.routon.ad.element;

import java.io.Serializable;
import java.util.ArrayList;

public class TemplateInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -80520686075075612L;
	public ArrayList<TemplateEditInfo> mTemplateEditInfos;
	//模板原型图片名
	public String mTemplate;
	
	//编辑目录路径
	//ends with "/"
	public String mEditDirPath;
}
