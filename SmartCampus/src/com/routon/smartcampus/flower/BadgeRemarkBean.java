package com.routon.smartcampus.flower;

import java.io.Serializable;

import org.json.JSONObject;

public class BadgeRemarkBean extends BadgeInfo implements Serializable {

	private static final long serialVersionUID = 4758814943631470203L;


	public BadgeRemarkBean(String _badgeTitle, int _badgeCount, String _badgeRemark) {
		badgeTitle = _badgeTitle;
		// badgeBonuspoint=_badgeCount;
		badgeRemark = _badgeRemark;
	}

	public BadgeRemarkBean(JSONObject jsonObject) {
		if (jsonObject == null) {
			return;
		}
		badgeTitle = jsonObject.optString("title");
		bonuspoint = jsonObject.optInt("bonuspoint");
//		if (bonuspoint<0) {
//			prop=1;
//		}
		badgeRemark = jsonObject.optString("remark");
		badgeTitleId= jsonObject.optInt("id");
	}

	BadgeRemarkBean() {

	}

}
