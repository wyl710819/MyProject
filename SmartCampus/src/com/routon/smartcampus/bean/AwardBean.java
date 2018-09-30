package com.routon.smartcampus.bean;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class AwardBean implements Parcelable {

	public int id;
	public String name;
	public int badgenum;
	public int bonuspoint;
	public String imgUrl;
	public String createtime;
	public String modifytime;
	public int status;
	public int stock;
	
	
	public AwardBean(JSONObject obj) {
		try {
			id = obj.getInt("id");
			name = obj.getString("name");
			badgenum = obj.getInt("badgenum");
			bonuspoint = obj.getInt("bonuspoint");
			imgUrl = obj.getString("imgUrl");
			createtime = obj.getString("createtime");
			modifytime = obj.getString("modifytime");
			status = obj.getInt("status");
			stock = obj.getInt("stock");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public AwardBean() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

}
