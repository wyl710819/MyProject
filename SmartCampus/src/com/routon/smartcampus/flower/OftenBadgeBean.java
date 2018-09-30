package com.routon.smartcampus.flower;

import java.io.Serializable;
/**
 * @author sj
 * @version 2017年6月28日 上午10:33:18
 */
import org.json.JSONObject;


//常用徽章
public class OftenBadgeBean extends BadgeInfo implements Serializable {

	private static final long serialVersionUID = -48552365006907293L;
	
	public OftenBadgeBean(JSONObject obj) {
		if( obj == null ){
			return;
		}
		
		badgeTitle = obj.optString("resTitle");
		badgeRemark = obj.optString("resRemark");
		id = obj.optInt("resId");
		badgeId = obj.optInt("badgeId");
		type = obj.optInt("type");
		bonuspoint = obj.optInt("resBounsPoint");
		badgeTitleId = obj.optInt("resId");
		
	}

	public OftenBadgeBean(String _name,String _badgeRemark,String _imgUrl,int _bonuspoint,int _prop) {
		badgeTitle=_name;
		badgeRemark=_badgeRemark;
		imgUrl=_imgUrl;
		bonuspoint=_bonuspoint;
		prop=_prop;
	}
	
	public OftenBadgeBean() {

	}

}
