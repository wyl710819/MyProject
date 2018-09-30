package com.routon.smartcampus.student;

import com.routon.smartcampus.bean.StudentBean;

import android.view.View;

public interface OnStudentBeanChangeListener {
	public void onSelect(StudentBean bean, int position);
	public void onClicked(StudentBean bean, int position, View view);
}
