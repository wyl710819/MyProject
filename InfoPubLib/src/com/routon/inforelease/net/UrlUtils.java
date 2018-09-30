/*
 * 构造请求url
 */
package com.routon.inforelease.net;

import java.util.HashMap;
import java.util.Map;
import com.routon.utils.BaiscUrlUtils;

public class UrlUtils extends BaiscUrlUtils{
	
	//更换用户手机号和密码，需要获取验证码更换手机号和密码
	public static String getUpdatePhoneUrl(String phone,String verifyCode,String pwd){
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "phone", phone);
		addParam(params, "pwd", pwd);
		addParam(params, "verifyCode", verifyCode);
		return makeUrl("/user/updatephone.htm", params);
	}
	
	//学生绑定家长手机号
	public static String getUpdateParentPhoneUrl(String sid,String phone,String verifyCode){
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "sid", sid);
		addParam(params, "phone", phone);
		addParam(params, "verifyCode", verifyCode);
		return makeUrl("/staff/updatephone.htm", params);
	}
	
	//新建分组
	public static String getGroupAddUrl(int groupPId,String groupName){	
		Map<String, String> params = new HashMap<String, String>();
		params.put("groupId", Integer.toString(groupPId));
		params.put("forceAdd", Integer.toString(-2));
		addParam(params, "name", groupName);
		return makeUrl("/group/add.htm", params);
	}
	
	//修改分组名
	public static String getGroupRenameUrl(int groupPId,String groupName){	
		Map<String, String> params = new HashMap<String, String>();
		params.put("groupId", Integer.toString(groupPId));
		addParam(params, "name", groupName);
		return makeUrl("/group/rename.htm", params);
	}
	
	// 删除分组
	public static String getGroupDelUrl(int groupId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("groupId", Integer.toString(groupId));
		return makeUrl("/group/del.htm", params);
	}
	
	//用户指派分组
	public static String getUserAssignGroup(String userIds,String groupIds){
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "userIds", userIds);
		addParam(params, "groupIds", groupIds);
		return makeUrl("/userassigngroup.htm", params);		
	}
	
	public static String getUserSaveUrl(String userName,String realName,String email,String phoneNum,String address,String GroupIDs){
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "userName", userName);
		addParam(params, "realName", realName);
		addParam(params, "phoneNum", phoneNum);
		addParam(params, "email", email);
		addParam(params, "address", address);
		addParam(params, "groupIds", GroupIDs);
		return makeUrl("/usersave.htm", params);
	}
	
	public static String getUserDelUrl(String userIds){
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "userIds", userIds);
		return makeUrl("/userdel.htm", params);
	}
	
	public static String getUserResetKeyUrl(int userId,String userName){
		Map<String, String> params = new HashMap<String, String>();
		params.put("userId", Integer.toString(userId));
		addParam(params, "userName", userName);
		return makeUrl("/userresetkey.htm", params);
	}
	
	public static String getAssignTerminalUrl(String assignGroupId, String archiveId) {
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "assignGroupId", assignGroupId);
		if( archiveId.endsWith(",") ){
			archiveId = archiveId.substring(0, archiveId.length()-1);
		}
		addParam(params, "archiveIds", archiveId);
		return makeUrl("/group/assign.htm", params);
	}
	
	
	public static String getUserListUrl(int page, int pageSize,String flagId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("page", Integer.toString(page));
		params.put("pageSize", Integer.toString(pageSize));
		addParam(params, "flagId", flagId);
		return makeUrl("/userlist.htm", params);
	}
	
	// 分组树接口
	public static String getGroupListUrl() {
		return makeUrl("/group/list.htm", null);
	}
	
	public static String getGroupListCmdUrl(String parent){
		Map<String, String> params = new HashMap<String, String>();
		params.put("parent", parent);
		return makeCmdUrl("/group/list.htm", params);
	}
	
	// 素材列表接口
	public static String getResourceListUrl(int page, int pageSize, int FileType, String flag, String flagId, String reslabel, String rescatogryId, String flagTime) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("page", Integer.toString(page));
		if(pageSize == -1)
			pageSize = 10;
		params.put("pageSize", Integer.toString(pageSize));
		params.put("FileType", Integer.toString(FileType));
		addParam(params, "flag", flag);
		addParam(params, "reslabel", reslabel);
		addParam(params, "rescatogryId", rescatogryId);
		addParam(params, "flagTime", flagTime);
		addParam(params, "flagId", flagId);
		return makeUrl("/resource/list.htm", params);
	}
	
	// 获取素材分类接口
	public static String getResourceResCatUrl() {
		return makeUrl("/resource/rescat.htm", null);
	}
	
	// 获取素材标签接口
	public static String getResourceResTagUrl(int page, int pageSize, String labelname) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("page", Integer.toString(page));
		params.put("pageSize", Integer.toString(pageSize));
		addParam(params, "labelname", labelname);
		return makeUrl("/resource/restag.htm", params);
	}

	// 上传文字素材接口
	public static String getResourceAddTxtUrl(/*String txtResourceName, String txtFlytextContent*/) {
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("txtResourceName", txtResourceName);
//		params.put("txtFlytextContent", txtFlytextContent);
		return makeUrl("/resource/addtxt.htm", null);//params);
	}
	
	// 上传素材接口
	public static String getResourceUploadUrl() {
		return makeUrl("/resource/upload.htm", null);
	}
	
	// 节目下发
	public static String getSendPlayUrl(int contractId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("contractId", Integer.toString(contractId));
		return makeUrl("/sendplay.htm", params);
	}
	
	// 节目下发进度查询 
	public static String getSendResultUrl(int sendId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("sendId", Integer.toString(sendId));
		return makeUrl("/sendresult.htm", params);
	}
	
	// 节目列表查询
	public static String getPlanListUrl(String name, int page, int rows) {
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "name", name);
		params.put("page", Integer.toString(page));
		params.put("rows", Integer.toString(rows));
		return makeUrl("/planlist.htm", params);
	}
	
	// 终端节目列表查询
	public static String getTerminalPlanListUrl(String id, int page, int rows) {
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "id", id);
		params.put("page", Integer.toString(page));
		params.put("rows", Integer.toString(rows));
		return makeUrl("/terplanlist.htm", params);
	}
	
	// 节目新增 
	public static String getPlanAddUrl() {
		return makeUrl("/planadd.htm", null);
	}
	
	//删除素材接口
	public static String getDelMaterialUrl(){
		return makeUrl("/resource/del.htm", null);
	}
	
	// 节目编辑
	public static String getPlanEditUrl() {
		return makeUrl("/planedit.htm", null);
	}
	
	// 节目删除
	public static String getPlanDelUrl() {
		return makeUrl("/plandel.htm", null);
	}
	
	// 节目投放
	public static String getPlanAssignUrl() {
		return makeUrl("/planassign.htm", null);
	}
	
	// 节目的投放分组查询
	public static String getFindPlanAssignUrl() {
		return makeUrl("/findplanassign.htm", null);
	}
	
	// 节目的图片广告查询
	public static String getImageListUrl(String name, int page, int rows, int id) {
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "name", name);
		params.put("page", Integer.toString(page));
		params.put("rows", Integer.toString(rows));
		params.put("id", Integer.toString(id));
		return makeUrl("/imglist.htm", params);
	}

	// 节目的文字广告查询
	public static String getTextListUrl(String name, int page, int rows, int id) {
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "name", name);
		params.put("page", Integer.toString(page));
		params.put("rows", Integer.toString(rows));
		params.put("id", Integer.toString(id));
		return makeUrl("/txtlist.htm", params);
	}
	
	// 广告播放属性设置
	public static String getAdParamUrl() {
		return makeUrl("/adparam.htm", null);
	}
	
	// 广告播放时段查询
	public static String getFindAdPeriodsUrl() {
		return makeUrl("/findadperiods.htm", null);
	}

	// 广告播放时段设置
	public static String getAdPeriodsUrl() {
		return makeUrl("/adperiods.htm", null);
	}
	
	// 广告删除
	public static String getAdDelUrl() {
		return makeUrl("/addel.htm", null);
	}
	
	// 图片显示效果
	public static String getImgEffectsUrl() {
		return makeUrl("/imgeffects.htm", null);
	}
	
	public static String getSNoticeListUrl(String id,String notice,int page, int rows) {
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "archiveIds", id);
		addParam(params, "notice", notice);
		params.put("page", Integer.toString(page));
		params.put("rows", Integer.toString(rows));
		return makeUrl("/snoticelist.htm", params);
	}
	
	public static String getSNoticeListUrl(String notice, int page, int rows,int status) {
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "notice", notice);
		params.put("page", Integer.toString(page));
		params.put("rows", Integer.toString(rows));
		params.put("status", Integer.toString(status));
		return makeUrl("/snoticelist.htm", params);
	}
	
	public static String getSNoticeListUrl(String notice, int page, int rows,String groupIds) {
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "notice", notice);
		params.put("page", Integer.toString(page));
		params.put("rows", Integer.toString(rows));
		if( groupIds != null ){
			params.put("groupIds", groupIds);
		}
		return makeUrl("/snoticelist.htm", params);
	}
	
	
	public static String getSNoticeAddUrl() {
		return makeUrl("/snoticeadd.htm", null);
	}
	
	public static String getSNoticeEditUrl() {
		return makeUrl("/snoticeedit.htm", null);
	}
	
	public static String getSNoticeDelUrl() {
		return makeUrl("/snoticedel.htm", null);
	}
	
	public static String getSNoticePublishUrl(String ids) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("ids", ids);
		return makeUrl("/snotice/publish.htm", params);
	}
	
	public static String getClassInfoListUrl(int page, int pageSize,int status,String groupIds) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("page", Integer.toString(page));
		params.put("pageSize", Integer.toString(pageSize));
		params.put("status", Integer.toString(status));
		if( groupIds != null ){
			params.put("groupIds", groupIds);
		}
		return makeUrl("/classinfo/list.htm", params);
	}
	
	public static String getClassInfoListUrl(int page, int pageSize,String groupIds) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("page", Integer.toString(page));
		params.put("pageSize", Integer.toString(pageSize));
		if( groupIds != null ){
			params.put("groupIds", groupIds);
		}
//		params.put("types", "1,3");
		return makeUrl("/classinfo/list.htm", params);
	}
	
	public static String getTerminalClassInfoListUrl(String id,int page, int pageSize) {
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "archiveIds", id);
		params.put("page", Integer.toString(page));
//		params.put("types", "1,3");
		params.put("pageSize", Integer.toString(pageSize));
		return makeUrl("/classinfo/list.htm", params);
	}
	
	public static String getParentClassInfoListUrl(String groupID, int page, int pageSize){
		Map<String, String> params = new HashMap<String, String>();
		addParam(params, "groupIds", groupID);
		params.put("page", Integer.toString(page));
//		params.put("types", "1,3");
		params.put("pageSize", Integer.toString(pageSize));
		params.put("publishStatus", "1");
		return makeCmdUrl("/classinfo/list.htm", params);
	}
	
	public static String getContentCheckListUrl(){
		return makeCmdUrl("/school/contentcheck/list.htm",null);
	}
	
	public static String getClassInfoSaveUrl() {
		return makeUrl("/classinfo/save.htm", null);
	}
	
	public static String getClassInfoDelUrl(String ids) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("ids", ids);
		return makeUrl("/classinfo/del.htm", params);
	}
	
	public static String getClassInfoPublishUrl(String ids) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("ids", ids);
		return makeUrl("/classinfo/publish.htm", params);
	}
	
	public static String getSinglePlanSaveUrl()
	{
		return makeUrl("/singleplan.htm", null);
	}
	
	public static String getAuditUrl(String buzids,int state,String attitude){
		Map<String, String> params = new HashMap<String, String>();
		params.put("buzids", buzids);
		params.put("state", String.valueOf(state));
		if( attitude != null ){
			params.put("attitude", attitude);	
		}
		return makeUrl("/audit212.htm", params);
	}
	
	public static String getAuditSNoticeUrl(String buzids,int state,String attitude){
		Map<String, String> params = new HashMap<String, String>();
		params.put("buzids", buzids);
		params.put("state", String.valueOf(state));
		if( attitude != null ){
			params.put("attitude", attitude);	
		}
		return makeUrl("/audit213.htm", params);
	}
	
	public static String getSwitchCtrlUrl() {
		return makeUrl("/s1810/ctrl.htm", null);
	}
}
