package com.routon.smartcampus.flower;

import java.io.Serializable;


public class BadgeInfo implements Serializable {

	private static final long serialVersionUID = 6111348961881721564L;
	//常用小红花type
	public static final int TYPE_CUSTOMFLOWER = 0;
	//自定义评语type
	public static final int TYPE_DEFINEEVAS = 1;

	public int id;// id
	public int bonuspoint = 0;// 积分
	public String badgeTitle = "";// 评语标题
	public String badgeRemark = "";// 评语内容
	public int prop = 0;// 徽章性质 0正面 1负面
	public String imgUrl = "";// 一级徽章图片url
	public int type;//徽章类型 0常用 1自定义
	public int badgeId;
	public int badgeTitleId;//标题id
	
	public boolean isSelect = false;// 是否为选中状态
	public String[] imgList;
	

	public BadgeInfo() {

	}

	/*public BadgeInfo(JSONObject jsonObject) {
		if( jsonObject == null ){
			return;
		}
//		id = obj.optInt("id");
		bonuspoint = jsonObject.optInt("bonuspoint");
		badgeTitle = jsonObject.optString("title");
//		prop = obj.optInt("prop");
		badgeRemark = jsonObject.optString("remark");
//		createtime = obj.optString("createtime");
	}*/

}
