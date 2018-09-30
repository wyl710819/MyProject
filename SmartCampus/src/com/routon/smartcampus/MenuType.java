package com.routon.smartcampus;

import java.util.ArrayList;

import com.routon.edurelease.R;

public class MenuType{
	public static final int MENU_SCHOOL_DYNAMIC = 0;//校园动态
	public static final int MENU_AUDIT = 1;//老师版内容审核
	public static final int MENU_FLOWER = 2;//小红花
	public static final int MENU_HOMEWORK = 3;//家庭作业
	public static final int MENU_COURSE = 4;//课表
	public static final int MENU_GRADETRACK = 5;//学情追踪
	public static final int MENU_COLLIGATE_OPINION = 6;//综合评价
	public static final int MENU_SCHOOL_COMPARE = 7;//老师版校务评比
	public static final int MENU_ANSWER = 8;//老师版答题
	public static final int MENU_ATTENDANCE = 9;//考勤
	public static final int MENU_CARDMANAGER = 10;//校园卡管理
	public static final int MENU_GUESTBOOK = 11;//家长版留言簿
	public static final int MENU_SWTCH_CTRL = 12;//老师版智能开关
	public static final int MENU_LEAVE = 13;//老师版请假换课
	public static final int MENU_SELECT_COURSE = 14;//家长版在线选课
	public static final int MENU_TEACHER_LEAVE = 15;//老师版我要请假
	public static final int MENU_STUDENT_LEAVE = 16;//老师版学生请假
	public static final int MENU_STUDENT_ADD_LEAVE = 17;//家长版我要请假
	public static final int MENU_NOTIFY = 18;//通知
	
	public static void getOrders(String menulistorder,ArrayList<Integer> orders){
		if( menulistorder != null ){
			orders.clear();
			String[] orderstrs = menulistorder.split("&");
			if( orderstrs.length > 0 ){
				for( int i = 0; i < orderstrs.length; i++ ){
					try{
						orders.add(Integer.parseInt(orderstrs[i]));
					}catch(NumberFormatException e){
						
					}
				}
			}
		}
		return;
	}
	
	public static String formatMenuListOrder(ArrayList<Integer> orders){
		String orderStr = "";
		for( int i = 0; i < orders.size(); i++ ){
			orderStr += orders.get(i);
			if( i != orders.size() - 1 ){
				orderStr += "&";
			}		
		}
		return orderStr;
	}
	
	public static int getName(int type){
		switch(type){
		case MENU_SCHOOL_DYNAMIC:
			return R.string.menu_school_dynamic;
		case MENU_AUDIT:
			return R.string.menu_audit;
		case MENU_FLOWER:
			return R.string.menu_flower;
		case MENU_HOMEWORK:
			return R.string.menu_homework;
		case MENU_COURSE:
			return R.string.menu_course;
		case MENU_GRADETRACK:
			return R.string.menu_gradetrack;
		case MENU_COLLIGATE_OPINION:
			return R.string.menu_colligate_opinion;
		case MENU_SCHOOL_COMPARE:
			return R.string.menu_school_compare;
		case MENU_ANSWER:
			return R.string.menu_answer;
		case MENU_ATTENDANCE:
			return R.string.menu_attendance;
		case MENU_CARDMANAGER:
			return R.string.menu_cardmanager;
		case MENU_GUESTBOOK:
			return R.string.menu_guestbook;
		case MENU_SWTCH_CTRL:
			return R.string.menu_swtch;
		case MENU_LEAVE://请假换课
			return R.string.menu_leave;
		case MENU_SELECT_COURSE://在线选课
			return R.string.menu_select_course;
		case MENU_TEACHER_LEAVE://我要请假
			return R.string.menu_teacher_leave;
		case MENU_STUDENT_LEAVE://学生请假
			return R.string.menu_student_leave;
		case MENU_STUDENT_ADD_LEAVE://我要请假
			return R.string.menu_teacher_leave;
		case MENU_NOTIFY://通知
			return R.string.menu_notify;
		}
		return -1;
	}
	
	public static int getIcon(int type){
		switch(type){
		case MENU_SCHOOL_DYNAMIC:
			return R.drawable.menu_school_dynamic;
		case MENU_AUDIT:
			return R.drawable.edu_icon;
		case MENU_FLOWER:
			return R.drawable.star;
		case MENU_HOMEWORK:
			return R.drawable.menu_work;
		case MENU_COURSE:
			return R.drawable.menu_course;
		case MENU_GRADETRACK:
			return R.drawable.menu_grade;
		case MENU_COLLIGATE_OPINION:
			return R.drawable.menu_colligate_opinion;
		case MENU_SCHOOL_COMPARE:
			return R.drawable.menu_school_affairs_compare;
		case MENU_ANSWER:
			return R.drawable.menu_answer;
		case MENU_ATTENDANCE:
			return R.drawable.menu_attendance;
		case MENU_CARDMANAGER:
			return R.drawable.menu_cardmanager;
		case MENU_GUESTBOOK:
			return R.drawable.menu_guestbook;
		case MENU_SWTCH_CTRL:
			return R.drawable.menu_swtch;
		case MENU_LEAVE:
			return R.drawable.menu_leave;
		case MENU_SELECT_COURSE:
			return R.drawable.menu_selectcourse;
		case MENU_TEACHER_LEAVE:
			return R.drawable.menu_teacher_leave;
		case MENU_STUDENT_LEAVE:
			return R.drawable.menu_student_leave;
		case MENU_STUDENT_ADD_LEAVE:
			return R.drawable.menu_student_add_leave;
		case MENU_NOTIFY:
			return R.drawable.menu_notify;
		}
		return -1;
	}
}