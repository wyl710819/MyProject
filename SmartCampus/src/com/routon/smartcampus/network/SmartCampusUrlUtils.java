package com.routon.smartcampus.network;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.R.integer;
import android.util.Log;

import com.routon.utils.BaiscUrlUtils;
import com.routon.utils.Md5Util;

public class SmartCampusUrlUtils extends BaiscUrlUtils{
	
	//获取徽章列表
	public static String getBadgeListUrl(){	
		Map<String, String> params = new HashMap<String, String>();
		params.put("page", "1");
		params.put("pageSize", "9999");
		return makeUrl("/badge/list.htm", params);
	}
	
	public static String getStudenCardUpdateUrl(){
		return makeUrl("/appversion.htm?app=studentcard", null);
	}
	
	public static String getPushMsgUrl(String channel,int type){
		Map<String, String> params = new HashMap<String, String>();
		params.put("page", "1");
		params.put("channel", channel);
		params.put("pageSize", "9999");
		if( type > 0 ){
			params.put("type", String.valueOf(type));
		}
		return makeUrl("/pushmsg/list.htm", params);
	}
	
	public static String getTeacherCardUpdateUrl(){
		return makeUrl("/appversion.htm?app=teachercard", null);
	}
	
	public static String getFaceVersionUrl(){
		StringBuilder sb = new StringBuilder();
		sb.append("http://edu.wanlogin.com:8086/edu");
		sb.append(BaiscUrlUtils.easyad_client_address);
		sb.append("/appversion.htm");
		sb.append("?app=face");
		return sb.toString();
	}
	
	//学生徽章信息明细
	public static String getStudentBadgeDetailListUrl(int studentId){
		Map<String, String> params = new HashMap<String, String>();
		params.put("studentId", String.valueOf(studentId));
		params.put("status", "0,3");
		params.put("sort", "1");
		return makeUrl("/studentbadge/detaillist.htm", params);
	}
	
	//学生徽章信息明细
	public static String getStudentBadgeDetailListCmdUrl(int studentId){
		Map<String, String> params = new HashMap<String, String>();
		params.put("studentId", String.valueOf(studentId));
		params.put("status", "0,3");
		params.put("sort", "1");
		return makeCmdUrl("/studentbadge/detaillist.htm", params);
	}
	
	//教师颁发徽章信息明细
	public static String getTeacherIssuedBadgeDetailListUrl(int teacherUserId,int page,int pageSize,int flagId){
		Map<String, String> params = new HashMap<String, String>();
		params.put("teacherUserId", String.valueOf(teacherUserId));
		params.put("page", String.valueOf(page));
		if( flagId > 0 ){
			params.put("flagId", String.valueOf(flagId));
		}
		params.put("status", "0,3");
		params.put("pageSize", String.valueOf(pageSize));
		return makeUrl("/teacherbadge/detaillist.htm", params);
	}
	
	// 本学期班级颁发的各类小红花平均数
		public static String getSessionClassBadgeUrl(String groupId, int badgeId) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("groupId", groupId);
			if (badgeId > 0) {
				params.put("page", String.valueOf(badgeId));
			}

			return makeCmdUrl("/badge/stats/class_badge.htm", params);
		}

		// 本学期每周班级人均积分
		public static String getWeeklyClassBadgeScoreUrl(String groupId) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("groupId", groupId);

			return makeCmdUrl("/badge/stats/week/class_bonuspoint.htm", params);
		}

		// 本学期每周学生获取的积分数
		public static String getWeeklyStudentBadgeScoreUrl(int sid) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("sid", String.valueOf(sid));

			return makeCmdUrl("/badge/stats/week/student_bonuspoint.htm", params);
		}

		// 本学期每周学生获取的各类小红花数
		public static String getWeeklyStudentBadgeUrl(int sid, int badgeId) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("sid", String.valueOf(sid));
			if (badgeId > 0) {
				params.put("badgeId", String.valueOf(badgeId));
			}

			return makeCmdUrl("/badge/stats/week/student_badge.htm", params);
		}
	
//	public static String getUndoBadgeUrl(int studentId, int badgeId){
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("studentId", String.valueOf(studentId));
//		params.put("badgeId", String.valueOf(badgeId));
//		return makeUrl("/badge/undo.htm", params);
//	}
	public static String getUndoBadgeUrl(int issueId){
		Map<String, String> params = new HashMap<String, String>();
		params.put("issueId", String.valueOf(issueId));
		return makeUrl("/badge/undo.htm", params);
	}
	
	public static String getBadugeIssueURl(){
		return makeUrl("/badge/issue.htm", null);
	}
	
	public static String getBadugeIssueAuditURl(){
		return makeUrl("/badge/issueaudit.htm", null);
	}
	
	//获取学生列表
	public static String getStudentListUrl(){	
		Map<String, String> params = new HashMap<String, String>();
		params.put("page", "1");
		params.put("pageSize", "9999");
		return makeUrl("/student/list.htm", params);
	}
	
	//获取奖品列表
		public static String getAwardListUrl(){	
			Map<String, String> params = new HashMap<String, String>();
			params.put("page", "1");
			params.put("pageSize", "9999");
			return makeUrl("/award/list.htm", params);
		}
		
		public static String getExchangeAwardURl(){
			return makeUrl("/badge/exchange.htm", null);
		}
		
		public static String getClassListURl(){
			return makeUrl("/group/list.htm", null);
		}
		
		public static String getStudentAttendanceUrl(String sid,String beginTime,String endTime){
			Map<String, String> params = new HashMap<String, String>();
			params.put("sid", sid);
			params.put("beginTime", beginTime);
			params.put("endTim", endTime);
			return makeUrl("/att/studentattd.htm", params);
		}
		
		public static String getCmdUpdateLoginTimeUrl(String phone){
			Map<String, String> params = new HashMap<String, String>();
			params.put("phone", phone);
			return makeCmdUrl("/parent/updatelogintime.htm", params);
		}
		
		public static String getCmdStudentAttendanceUrl(String sid,String beginTime,String endTime){
			Map<String, String> params = new HashMap<String, String>();
			params.put("sid", sid);
			params.put("beginTime", beginTime);
			params.put("endTim", endTime);
			return makeCmdUrl("/att/studentattd.htm", params);
		}
		
		public static String getSchoolAttendanceUrl(int groupId,String terminalId){
//			Map<String, String> params = new HashMap<String, String>();
//			params.put("groupId", String.valueOf(groupId));
			StringBuilder sb = new StringBuilder();
			sb.append("http://");
			sb.append(server_address);
			sb.append("/ad/cmd/schoolattence.htm");
			sb.append("?app="+app);
			if( groupId > 0 ){
				sb.append("&groupId="+groupId);
			}
			if( terminalId != null ){
				sb.append("&TerminalID="+terminalId);
			}
			return sb.toString();
		}
		
		//查询某次考勤结果
		public static String getAttendResultUrl(String groupId,String day,String lesson){
			StringBuilder sb = new StringBuilder();
			sb.append("http://");
			sb.append(server_address);
			sb.append("/easyad/cmd/client/att/attdresult.htm");
			sb.append("?app="+app);
			if( groupId != null){
				sb.append("&groupId="+groupId);
			}
			if( day != null ){
				sb.append("&day="+day);
			}
			if( lesson != null ){
				sb.append("&lesson="+lesson);
			}
			return sb.toString();
		}
		
		public static String getClassAttendaceUrl(String groupId,String course,String sort,String beginTime,String endTime){
			Map<String, String> params = new HashMap<String, String>();
			if( groupId != null ){
				params.put("groupId", groupId);
			}
			if( course != null ){
				params.put("course", course);
			}
			if( sort != null ){
				params.put("sort", sort);
			}
			if( beginTime != null ){
				params.put("beginTime", beginTime);
			}
			if( endTime != null ){
				params.put("endTime", endTime);
			}
			return makeUrl("/att/classattd.htm", params); 
		}
		
		public static String getStudentBadgeCountListURl(){
			return makeUrl("/student/badgecount.htm", null);
		}
		
		public static String getFtpURl(){
			return makeUrl("/app/ftp.htm", null);
		}
		
		public static String getCmdStudentBadgeCountListURl(){
			return makeCmdUrl("/student/badgecount.htm", null);
		}
		
		public static String getCurrentClass(){
			return makeUrl("/teacher/currentClass.htm", null);
		}
		
		public static String getAgentListUrl(String groupId){
			Map<String, String> params = new HashMap<String, String>();
			if( groupId != null ){
				params.put("groupId", groupId);
			}
			return makeUrl("/agent/list.htm", params);
		}
		
		public static String getAgentAddUrl(String studentId){
			Map<String, String> params = new HashMap<String, String>();
			if( studentId != null ){
				params.put("studentId", studentId);
			}
			return makeUrl("/agent/add.htm", params);
		}
		
		public static String getBadgeUndoUrl(String studentId,String badgeId,String issueId){
			Map<String, String> params = new HashMap<String, String>();
			if( studentId != null ){
				params.put("studentId", studentId);
			}
			if( badgeId != null ){
				params.put("badgeId", studentId);
			}
			if( issueId != null ){
				params.put("issueId", issueId);
			}
			return makeUrl("/badge/undo.htm", params);
		}
		
		public static String getAgentCancelUrl(String staffUserAgentId){
			Map<String, String> params = new HashMap<String, String>();
			if( staffUserAgentId != null ){
				params.put("staffUserAgentId", staffUserAgentId);
			}
			return makeUrl("/agent/cancel.htm", params);
		}
		public static String getCommitAttecnceInfoUrl(String teacherId,String lesson,String sid){
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("teacherId", teacherId);
			params.put("lesson", lesson);
			params.put("sid", sid);
			return makeUrl("/att/attdmanul.htm", params);
			
		}
		public static String getCommitAllAttecnceInfoUrl(String lesson, String groupId){
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("lesson", lesson);
			params.put("groupId", groupId);
			return makeCmdUrl("/att/attd.htm", params);
			
		}
		
		/*
		 * 作业接口地址
		 * */
		
		//教师班级作业批改接口
		public static String getCorrectHomeworkUrl(String classId,String studentId,String homeworkId, String rate, String fileIds, String fileIdParams){
			Map<String, String> params = new HashMap<String, String>();
			params.put("classId", classId);
			params.put("studentIds", studentId);
			params.put("homeworkIds", homeworkId);
			params.put("rate", rate);
			if (fileIds!=null) {
				params.put("fileId", fileIds);
			}
			if (fileIdParams!=null && !fileIdParams.equals("") && !fileIdParams.equals("null")) {
				params.put("fileIdparams", fileIdParams);
			}
			/*if (rateStr!=null && !rateStr.equals("") && !rateStr.equals("null")) {
				params.put("rateStr", rateStr);
			}*/
			return makeUrl("/teacher/homework/correct.htm", params);
		}
		
		//教师班级作业批改查询接口
		public static String getCorrectHomeworkQueryUrl(String classId,String homeworkId){
			Map<String, String> params = new HashMap<String, String>();
			params.put("classId", classId);
			params.put("homeworkId", homeworkId);
			return makeUrl("/teacher/homework/correct/query.htm", params);
		}
		
		//家长查询学生作业接口
		public static String getFamilyHomeworkListUrl(String dateTime,String studentId){
			Map<String, String> params = new HashMap<String, String>();
			params.put("dateTime", dateTime);
			params.put("studentId", studentId);
			return makeCmdUrl("/parents/homework/query.htm", params);
		}
		//家长检查作业接口
		public static String getFamilyCheckHomeworkUrl(String studentId,String homeworkId,String isCheck,String fileId_type,String fileIdparams){
			Map<String, String> params = new HashMap<String, String>();
			params.put("studentId", studentId);
			params.put("homeworkId", homeworkId);
			params.put("isCheck", isCheck);
			if(fileId_type!=null){
				params.put("fileId_type", fileId_type);
			} 
			if(fileIdparams!=null){
				params.put("fileIdparams", fileIdparams);
			}
			return makeCmdUrl("/parents/homework/check.htm", params);
		}
		
		//家长查询学生成绩接口
		public static String getAllExamsUrl(String studentId)
		{
			Map<String, String> params = new HashMap<String, String>();
			params.put("studentId", studentId);
			return makeCmdUrl("/student/grades/query.htm", params);
		}

		//增加作业接口
		public static String getAddHomeworkUrl(String teacherId,String courseName,String description,String fileId,String fileIdparams){
			Map<String, String> params = new HashMap<String, String>();
			params.put("teacherId", teacherId);
			params.put("courseName", courseName);
			if(fileId!=null){
				params.put("fileId", fileId);
			} 
			if(fileIdparams!=null){
				params.put("fileIdparams", fileIdparams);
			}
			return makeUrl("/teacher/homework/create.htm", params);
		}
		//布置作业接口
		public static String getAssingHomeworkUrl(String classId,String homeworkId,String dateTime){
			Map<String, String> params = new HashMap<String, String>();
			
			params.put("classId", classId);
			params.put("homeworkId", homeworkId);
			
			
			return makeUrl("/teacher/homework/assign.htm", params);
		}
		//同年级近期作业查询接口
		public static String getHomeworkQueryUrl(String teacherId,String gradeId,String classId){
			Map<String, String> params = new HashMap<String, String>();
			params.put("teacherId", teacherId);
			params.put("gradeId", gradeId);
			params.put("classId", classId);
			return makeUrl("/teacher/homework/grade/query.htm", params);
		}
		//根据日期查询当前老师的班级列表以及作业情况
		public static String getClassHomeworkQueryUrl(String teacherId,String dateTime){
			Map<String, String> params = new HashMap<String, String>();
			params.put("teacherId", teacherId);
			params.put("dateTime", dateTime);
			
			return makeUrl("/teacher/homework/date/query.htm", params);
		}

		public static String ModifyHomeworkUrl(String homeworkId,String description,String fileId){
			Map<String, String> params = new HashMap<String, String>();
			params.put("homeworkId", homeworkId);
			params.put("description", description);
			params.put("fileId", fileId);
			
			return makeUrl("/teacher/homework/date/modify.htm", params);
			
		}

		//家长查询学生单科成绩接口
		public static String getCourseExamUrl(String studentId,String course)
		{
			Map<String, String> params = new HashMap<String, String>();
			params.put("studentId", studentId);
			params.put("course", course);
			return makeCmdUrl("/student/grades/query.htm", params);
		}
		

		
		//修改作业接口
		public static String getModifyHomeworkUrl(String homeworkId,String description){
			Map<String, String> params = new HashMap<String, String>();
			params.put("homeworkId", homeworkId);
			params.put("description", description);
			return makeUrl("/teacher/homework/modify.htm", params);
		}		
		//查询老师对应的课程名信息
		public static String getCourseNameUrl(String terUserId,String type){
			Map<String, String> params = new HashMap<String, String>();
			params.put("terUserId", terUserId);
			params.put("type", type);
			return makeUrl("/staff/info.htm", params);
			
		}
		
		//不需要登录会话获取学生列表
		public static String getStudentListCmdUrl(String groupIds,String parentPhone){
			Map<String, String> params = new HashMap<String, String>();
			params.put("page", "1");
			params.put("pageSize", "9999");
			if( groupIds != null ){
				params.put("groupIds", groupIds);
			}
			if( parentPhone != null ){
				params.put("parentPhone", parentPhone);
			}
			return makeCmdUrl("/student/list.htm", params);
		}
		
		public static String getUpdatePhoneCmdUrl(int sid,String phone){
			Map<String, String> params = new HashMap<String, String>();
			params.put("sid", String.valueOf(sid));
			params.put("phone", phone);
			return makeCmdUrl("/staff/updatephone.htm", params);
		}
		
		public static String getParentAuthenCmdUrl(){
			return makeCmdUrl("/user/parentauthen.htm", null);
		}
		//删除布置作业接口
		public static String getDeleteAssignHomeworkUrl(String classId,
				String homeworkId, String dateTime) {
			// TODO Auto-generated method stub
			Map<String,String>params=new HashMap<String,String>();
			params.put("classId",classId);
			params.put("homeworkId", homeworkId);
			params.put("dateTime", dateTime);
			return makeUrl("/teacher/homework/delete.htm", params);
		}

		public static String getUpdatePhotoCmdUrl(){	
			return makeCmdUrl("/staff/updatephoto.htm", null);
		}
		
		public static String getUpdatePhotoUrl(){	
			return makeUrl("/staff/updatephoto.htm", null);
		}
		
		// 老师查询学生成绩接口
		public static String getTeacherAllExamsUrl(String groupId) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("groupId", groupId);
			return makeUrl("/class/grades/query.htm", params);
		}
		
		// 老师查询学生单科成绩接口
		public static String getTeacherCourseExamUrl(String groupId, String course) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("groupId", groupId);
			params.put("course", course);
			return makeUrl("/class/grades/query.htm", params);
		}
		
		// 学生综合评价接口
				public static String getStudentOpinionWebUrl(String studentId) {
					Map<String, String> params = new HashMap<String, String>();
					params.put("studentId", studentId);
					return makeCmdUrl("/query/class/getstudentremark.htm", params);
				}
				
				
		// 评比项目列表接口
		public static String getSchoolRatingListUrl(String schoolGroupId) {
			Map<String, String> params = new HashMap<String, String>();
			if (schoolGroupId!=null && !schoolGroupId.equals("")) {
				params.put("schoolGroupId", schoolGroupId);
			}
			return makeUrl("/school/rating/list.htm", params);
		}
		
		// 评比子项目打分接口
		public static String getSchoolRatingScoreUrl(String date) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("date", date);
			return makeUrl("/school/rating/score.htm", params);
		}
		
		// 评比完成接口
		public static String getSchoolRatingFinishUrl(String ratingId) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("ratingId", ratingId);
			return makeUrl("/school/rating/finish.htm", params);
		}
		
		// 评比历史成绩查询接口
		public static String getSchoolRatingHistoryUrl(String ratingId ,String groupId) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("groupId", groupId);
			params.put("ratingId", ratingId);
			return makeUrl("/school/rating/class/history.htm", params);
		}
		
		// 评比单日成绩查询接口
		public static String getSchoolRatingQueryUrl(String ratingId,String date) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("ratingId", ratingId);
			params.put("date", date);
			return makeUrl("/school/rating/class/query.htm", params);
		}
		
		//评比周期列表查询接口
		public static String getSchoolRatingCycleQueryUrl(String ratingId) {
			Map<String, String> params = new HashMap<String, String>();
				params.put("ratingId", ratingId);
			return makeUrl("/school/rating/peroid/query.htm", params);
		}
		
		//某周期评比班级列表查询接口
		public static String getRatingCycleClassQueryUrl(String ratingId,int peroidId) {
			Map<String, String> params = new HashMap<String, String>();
				params.put("ratingId", ratingId);
				if (peroidId!=0) {
					params.put("peroidId", String.valueOf(peroidId));
				}
				
			return makeUrl("/school/rating/peroid/class/query.htm", params);
		}
		
		public static String getRatingClassPeroidScoreUrl(String ratingId,String peroidId, String groupId) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("ratingId", ratingId);
			params.put("recordId", peroidId);
			params.put("groupId", groupId);
			
			return makeUrl("/school/rating/peroid/itemTotalScore/query.htm", params);
		}
		
		//提交留言接口
		public static String getAddMsgboardUrl(String sid ,String msg) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("sid", sid);
//			params.put("msg", msg);
			String KEY = "bK6b6lGpPsCN^S&7";
			String v = Md5Util.getMd5(sid + KEY + msg);
			addParam(params, "v", v);
			return makeCmdUrl("/school/msgboard/add.htm", params);
		}
		
		//留言列表
		public static String getMsgboardListUrl(String sid) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("sid", sid);
//			params.put("type", "1");
			return makeCmdUrl("/school/msgboard/studentmsglist.htm", params);
		}
		//答题上报数据
		public  static String getAnswereleaseUrl(int questionId,String course,String rightAnswer,String groupId,String uploadTime,String studentAnswerInfo, int teacherId){
			Map<String, String> params = new HashMap<String, String>();
			
			params.put("questionId", String.valueOf(questionId));
			params.put("course",String.valueOf(course));
			params.put("uploadTime", String.valueOf(uploadTime));
			params.put("rightAnswer", String.valueOf(rightAnswer));
			params.put("groupId",String.valueOf(groupId));
			params.put("teacherId",String.valueOf(teacherId));
			
			params.put("studentAnswerInfo",studentAnswerInfo);
			
			
			return makeCmdUrl("/student/answer/save.htm", params);
			
		}
		//根据终端id
		public  static String getClassIdUrl(String terminalId){
			Map<String, String> params = new HashMap<String, String>();
			params.put("terminalId", String.valueOf(terminalId));
			return makeCmdUrl("/getgroup.htm", params);
		}
		
		public static String getAddStepNumber(int sid,String date,int count,String v){
			Map<String, String> params = new HashMap<String, String>();
			params.put("sid", String.valueOf(sid));
			params.put("date", date);
			params.put("count", String.valueOf(count));
			params.put("v", v);
			return makeCmdUrl("/school/stepNumber/add.htm", params);
		}
		
		//获取当前时间单双周
		public static String getWeekeven(int groupId,String date){
			Map<String, String> params = new HashMap<String, String>();
			params.put("groupId", String.valueOf(groupId));
			params.put("date", date);
			return makeUrl("/att/weekeven.htm", params);
		}
		
		//智慧屏分配分组
		public static String getAssignGroup(String assignGroupId, String archiveIds){
			Map<String, String> params = new HashMap<String, String>();
			params.put("assignGroupId", assignGroupId);
			params.put("terminalIds", archiveIds);
			return makeUrl("/group/assign.htm", params);
		}
		//查询老师一周课表
		public static String getTeacherTimetablesUrl(String teacherUserId){
			Map<String, String> params = new HashMap<String, String>();
			params.put("teacherUserId", teacherUserId);
			return makeUrl("/timetable/teacher.htm", params);
		}
		//班级课程查询
		public static String getClassCoursesUrl(String classGroupId){
			Map<String, String> params = new HashMap<String, String>();
			params.put("classGroupId", classGroupId);
			return makeUrl("/timetable/class.htm", params);
		}

		// 班级课程查询,以日期，默认是下一周
		public static String getClassCoursesUrlByDate(String classGroupId) {
			 String mDateFormat = "yyyy-MM-dd";
		  	 SimpleDateFormat format = new SimpleDateFormat(mDateFormat);
		  	 Calendar nextWeek = Calendar.getInstance();
		     nextWeek.setTime(new Date());
		     nextWeek.add(Calendar.DAY_OF_WEEK, 7);
		     Date nextWeekDate = nextWeek.getTime();
		     String nextWeekString=format.format(nextWeekDate);
			Map<String, String> params = new HashMap<String, String>();
			params.put("classGroupId", classGroupId);
			Log.e("nextWeekdate",nextWeekString );
			params.put("date", nextWeekString);
			return makeUrl("/timetable/class.htm", params);
		}
		
		public static String getExchangeCourseUrl(int[] classGroupId,int[] lesson,int[] week,String[] courseName
				,String[] teacherSid,int[] weekeven){
			StringBuilder stringBuilder=new StringBuilder();
			stringBuilder.append("http://");
			stringBuilder.append(BaiscUrlUtils.server_address);
			stringBuilder.append(BaiscUrlUtils.easyad_client_address);
			stringBuilder.append("/timetable/modify.htm?");
			stringBuilder.append("classGroupId="+classGroupId[0]+"&lesson="+lesson[0]+"&week="+week[0]+"&courseName="+courseName[0]
					+"&teacherSid="+teacherSid[0]+"&weekeven="+weekeven[0]);
			stringBuilder.append("&classGroupId="+classGroupId[1]+"&lesson="+lesson[1]+"&week="+week[1]+"&courseName="+courseName[1]
					+"&teacherSid="+teacherSid[1]+"&weekeven="+weekeven[1]);
			return stringBuilder.toString();
		}
		
		//家长登录
		public static String getParentLoginUrl(String phone,String pwd){
			Map<String, String> params = new HashMap<String, String>();
			params.put("phone", phone);
			params.put("pwd", pwd);
			return makeCmdUrl("/user/parentlogin.htm", params);
		}
		//家长更换密码
		public static String getParentChangePwdUrl(String userId, String userName, String oldPwd, 
				String newPwd, String newPwdConfirm){
			Map<String, String> params = new HashMap<String, String>();
			params.put("userId", userId);
			params.put("userName", userName);
			params.put("oldPwd", oldPwd);
			params.put("newPwd", newPwd);
			params.put("newPwdConfirm", newPwdConfirm);
			return makeUrl("/changekey.htm", params);
		}
		
		public static String getExchangeHistoryUrl(String studentId){
			Map<String, String> params = new HashMap<String, String>();
			params.put("studentId", studentId);
			return makeUrl("/studentbadge/exchangelist.htm", params);
		}
		
		//查询多天班级考勤结果
		public static String getClassWeekAttenceUrl(String groupId, String startDay, String endDay){
			Map<String, String> params = new HashMap<>();
			params.put("groupId", groupId);
			params.put("startDay", startDay);
			params.put("endDay", endDay);
			return makeUrl("/att/attdresultbydays.htm", params);
		}
		
		public static String getSelectCourseUrl(String studentId)
		{
			Map<String, String> params = new HashMap<String, String>();
			params.put("studentId", studentId);
			return makeUrlCourses("/api/openCourse/selectPlan", params);
		}
		public static String getSelectSubjectsUrl(String planId)
		{
			Map<String, String> params = new HashMap<String, String>();
			params.put("planId", planId);
			return makeUrlCourses("/api/openCourse/selectItems", params);
			
		}
		public static String getStudentCoursesUrl(String planId,String studentId)
		{
			Map<String, String> params = new HashMap<String, String>();
			params.put("planId", planId);
			params.put("studentId", studentId);
			return makeUrlCourses("/api/openCourse/selectResult", params);
		}
		public static String getSelectSubjectSubmitUrl(String planId,String studentId,String studentName,String subjectIds,String subjectNames,String itemIds,String itemNames)
		{
			Map<String, String> params = new HashMap<>();
			params.put("planId", planId);
			params.put("studentId", studentId);
			params.put("studentName", studentName);
			params.put("subjectIds", subjectIds);
			params.put("subjectNames", subjectNames);
			params.put("itemIds", itemIds);
			params.put("itemNames", itemNames);
			return makeUrlCourses("/api/openCourse/saveSelect", params);
		}
		
		public static String getNotifyAttenceUrlString(String groupId){
			Map<String, String> params = new HashMap<>();
			params.put("groupId", groupId);
			params.put("status", "1");
			return makeUrl("/att/ctrl.htm", params);
		}
		
		public static String getErrorMsgUrl(){
			return makeCmdUrl("/terminal/taskstatus/errcode.htm", null);
		}

		// 17.7
		public static String getStudentLeaveInfo(String groupId){
			Map<String, String> params = new HashMap<>();
			params.put("groupId", groupId);
			return makeUrl("/class/leave/query.htm", params);
		}
		// 17.9
		public static String setStudentLeaveReview(String leaveId, String type){
			Map<String, String> params = new HashMap<>();
			params.put("leaveId", leaveId);
			params.put("type", type);
			return makeUrl("/student/leave/review.htm", params);
		}
		// 17.10
		public static String getStudentLeaveRanking(String groupId){
			Map<String, String> params = new HashMap<>();
			params.put("groupId", groupId);
			return makeUrl("/student/leave/ranking.htm", params);
		}
		// 21.2
		public static String getSchoolNotifyList(String flagId){
			Map<String, String> params = new HashMap<>();
			params.put("page", "1");
			params.put("pageSize", "255");
			if (flagId != null) {
                params.put("flagId", flagId);
			}
			return makeUrl("/schoolinform/list.htm", params);
		}

		//查询当前学生请假情况
		public static String getFamilyLeaveData(int sid){
			Map<String, String> params = new HashMap<>();
			params.put("sid", String.valueOf(sid));
			return makeCmdUrl("/student/leave/query.htm", params);
		}
		
		//学生提交请假
		public static String getFamilyleaveAddUrl(int sid, String reason){
			Map<String, String> params = new HashMap<>();
			params.put("sid", String.valueOf(sid));
			params.put("reason", reason);
			return makeCmdUrl("/student/leave/apply.htm", params);
		}

		//查询当前老师的请假情况
		public static String getTeacherLeaveUrl(String startTime, String endTime, String groupid){
			Map<String, String> params = new HashMap<>();
			if (startTime!=null) {
				params.put("startTime", startTime);
			}
			
			if (endTime!=null) {
				params.put("endTime", endTime);
			}
			
			if (groupid!=null) {
				params.put("groupid", groupid);
			}
			return makeUrl("/school/leave/course/queryschoolleave.htm", params);
		}
		
		//查询请假
		public static String getQueryLeaveUrl(String leaveId){
			Map<String, String> params = new HashMap<>();
				params.put("leaveId", leaveId);
			return makeUrl("/school/leave/query.htm", params);
		}
		
		//查询某节课教师的上课情况
		public static String getQueryTeacherStatusUrl(String lesson,String date,String groupId){
			Map<String, String> params = new HashMap<>();
				params.put("lesson", lesson);
				params.put("date", date);
				params.put("groupId", groupId);
			return makeUrl("/school/leave/course/queryteacherstatus.htm", params);
		}
		
		//查询教师某个时段内所有需要上的课的接口
		public static String getQueryUserTeacherClassUrl(String sid,String startTime,String endTime){
			Map<String, String> params = new HashMap<>();
				params.put("sid", sid);
				params.put("startTime", startTime);
				params.put("endTime", endTime);
			return makeUrl("/school/leave/course/query.htm", params);
		}
		
		//提交请假接口
		public static String getCommitLeaveUrl(String sid,String startTime,String endTime,String catalog,String reason){
			Map<String, String> params = new HashMap<>();
				params.put("sid", sid);
				if (startTime!=null) {
					params.put("startTime", startTime);
				}
				if (endTime!=null) {
					params.put("endTime", endTime);
				}
				
				params.put("catalog", catalog);
				
				if (reason!=null) {
					params.put("reason", reason);
				}
				
			return makeUrl("/school/leave/apply.htm", params);
		}
		
		//请假审批接口
		public static String getLeaveReviewUrl(String leaveId,String type){
			Map<String, String> params = new HashMap<>();
				params.put("leaveId", leaveId);
				params.put("type", type);
			return makeUrl("/student/leave/review.htm", params);
		}
		
		//发布通知接口
		public static String getSchoolInformUrl(String id,String title,String content,String confirm,String endTime,String groupIds,String fileIds){
			Map<String, String> params = new HashMap<>();
			if (id!=null) {
				params.put("id", id);
			}
			if (title!=null) {
				params.put("title", title);
			}
			if (content!=null) {
				params.put("content", content);
			}
			if (confirm!=null) {
				params.put("confirm", confirm);
			}
			if (endTime!=null) {
				params.put("endTime", endTime);
			}
			if (groupIds!=null && !groupIds.isEmpty()) {
				params.put("groupIds", groupIds);
			}
			if (fileIds!=null && !fileIds.isEmpty()) {
				params.put("fileIds", fileIds);
			}
				
			return makeUrl("/schoolinform/save.htm", params);
		}
		
		
		
}
