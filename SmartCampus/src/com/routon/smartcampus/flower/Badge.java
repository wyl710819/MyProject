package com.routon.smartcampus.flower;

import java.io.Serializable;
/**
 * @author sj
 * @version 2017年6月28日 上午10:33:18
 */
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

//徽章数据
public class Badge implements Serializable {

	public int id;//徽章id
	public int bonuspoint;//徽章积分
//	public int type;
//	public String typeName;
	public String name;//徽章名称
	public String imgUrl = " ";//徽章图片
//	public int count;
	public int prop;//徽章性质，正面0还是负面1
	public String modifytime;
	public String createtime;
//	public int unExchangeCount = 0;
//	public int totalStBadgeCount = 0;
	// 徽章的标题,备注及对应积分,0个或者多个，获取徽章列表时带此数据
	public List<BadgeRemarkBean> badgeRemarkList;

	// public ArrayList<StudentBadge> stBadgeList = new
	// ArrayList<StudentBadge>();

	public Badge(JSONObject obj) {
//		Log.d("Badge","obj:"+obj);
		if( obj == null ){
			return;
		}
		name = obj.optString("name");
		id = obj.optInt("id");
		bonuspoint = obj.optInt("bonuspoint");
		imgUrl = obj.optString("imgUrl");
		prop = obj.optInt("prop");
		modifytime = obj.optString("modifytime");
		createtime = obj.optString("createtime");
//		type = obj.optInt("type");
//		typeName = obj.optString("typeName");
		JSONArray jsonObjects = obj.optJSONArray("res");
		if (jsonObjects != null) {
			int len = jsonObjects.length();
			badgeRemarkList=new ArrayList<BadgeRemarkBean>();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = (JSONObject) jsonObjects.opt(i);
				BadgeRemarkBean badgeRemarkBean = new BadgeRemarkBean(jsonObject);
				badgeRemarkBean.imgUrl=imgUrl;
				badgeRemarkBean.prop=prop;
				badgeRemarkList.add(badgeRemarkBean);
			}

			
		}
	}

	public Badge() {

	}

//	@Override
//	public int describeContents() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeInt(id);
//		dest.writeInt(bonuspoint);
////		dest.writeInt(type);
////		dest.writeString(typeName);
//		dest.writeString(name);
//		dest.writeString(imgUrl);
//		dest.writeInt(prop);
////		dest.writeInt(count);
////		dest.writeInt(unExchangeCount);
////		dest.writeInt(totalStBadgeCount);
//		dest.writeParcelableArray(badgeRemarkList, flags);  
//	}
//
//	public static final Parcelable.Creator<Badge> CREATOR = new Creator<Badge>() {
//
//		@Override
//		public Badge createFromParcel(Parcel source) {
//			Badge badge = new Badge();
//			badge.id = source.readInt();
//			badge.bonuspoint = source.readInt();
//			badge.name = source.readString();
//			badge.imgUrl = source.readString();
//			badge.prop = source.readInt();
//			badge.badgeRemarkList=source.createTypedArray(BadgeRemarkBean.CREATOR);
//			return badge;
//		}
//
//		@Override
//		public Badge[] newArray(int size) {
//			return new Badge[size];
//		}
//	};
}
