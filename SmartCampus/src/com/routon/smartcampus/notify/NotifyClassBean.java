package com.routon.smartcampus.notify;

public class NotifyClassBean {
	
	public String className;
	public int classId;
	public boolean isChecked;
	
	public NotifyClassBean(String _className,int _classId,boolean _isChecked){
		className=_className;
		classId=_classId;
		isChecked=_isChecked;
	}

}
