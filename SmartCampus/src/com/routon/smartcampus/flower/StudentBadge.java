package com.routon.smartcampus.flower;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.routon.smartcampus.bean.StudentBean;

import android.util.Log;

//学生徽章
public class StudentBadge implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5233013333583183801L;
	public int id;
	public String createTime = " ";
	public String undoTime = " ";
	public int teacherId = 1;
	public String teacherName = " ";
//	public int undoTeacherId = 1;
//	public String undoTeacherName = " ";
	public int exchangeId = 0;
	public int status = 0;
	
	public String teacheImgUrl = null;
	public String undoTeachImgUrl = null;
	
	public int bonusPoint;
	
	public int origin = 0;//app: 0; s1701: 1; 平台: 2
	public Badge badge;
	public StudentBean student;
	
	public BadgeRemarkBean badgeRemarkBean = null;
	
	StudentBadge(JSONObject obj){
		if( obj == null ){
			return;
		}
		id = obj.optInt("id");
		createTime = obj.optString("createtime");
		undoTime = obj.optString("undotime");
		teacherId = obj.optInt("teacherId");
		teacherName = obj.optString("teacherName");
//			undoTeacherId = obj.getInt("undoTeacherId");
//		undoTeacherName = obj.optString("undoTeacherName");
		exchangeId = obj.optInt("exchangeId");
		status = obj.optInt("status");
		
		teacheImgUrl = obj.optString("teacherImgUrl");
		undoTeachImgUrl = obj.optString("undoTeacherImgUrl");
		bonusPoint = obj.optInt("bonuspoint");
		
		origin = obj.optInt("origin");
//		Log.d("StudentBadge","teacherName"+teacherName);
		badge = new Badge(obj.optJSONObject("badge"));
		
		badgeRemarkBean = new BadgeRemarkBean();
		badgeRemarkBean.badgeTitle = obj.optString("title");	
		badgeRemarkBean.badgeRemark = obj.optString("remark");
		
		JSONObject studentObj = obj.optJSONObject("student");
		if( studentObj != null ){
			student = new StudentBean(studentObj);
		}
		
		//关联图片
		JSONArray resJsonArray = obj.optJSONArray("res");
		if( resJsonArray != null ){
			badgeRemarkBean.imgList = new String[resJsonArray.length()];
			for( int i = 0; i < resJsonArray.length(); i++ ){
				JSONObject picres = resJsonArray.optJSONObject(i);
				badgeRemarkBean.imgList[i] = picres.optString("imgUrl");
			}
		}
	}
	
	StudentBadge(){
		
	}
	
	public void dump(){
		
		Log.i("StudentBadge:", createTime+" "+ teacherName + " " + badge.name);
	}
}
