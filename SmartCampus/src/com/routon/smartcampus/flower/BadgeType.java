package com.routon.smartcampus.flower;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.widget.RadioButton;

public class BadgeType {

	public int id;
	public String name;
	public String imgUrl;
	public RadioButton radioButton;
	
	public ArrayList<Badge> badges = new ArrayList<Badge>();
	
	BadgeType(JSONObject obj){
		try {
			name = obj.getString("name");
			id = obj.getInt("");
			imgUrl = obj.getString("imgUrl");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	BadgeType(Badge badge){
//		id = badge.type;
//		name = badge.typeName;
	}

	BadgeType(){
		
	}
	
	static public ArrayList<BadgeType> filterBadgeTypesFromBadges(ArrayList<Badge> badges){
		
		ArrayList<BadgeType> types = new ArrayList<BadgeType>();
//		
//		for(Badge badge : badges){
//			
//			BadgeType type = isTypeExistIn(badge.type, types);
//			if(type == null){
//				type = new BadgeType(badge);
//				types.add(type);
//			}
//			type.badges.add(badge);
//		}
//		
		
		return types;
	}

	static public BadgeType isTypeExistIn(int typeId, ArrayList<BadgeType> types){
		
		for(BadgeType badgeType:types){
			if(badgeType.id == typeId){
				return badgeType;
			}
		}
		return null;
	}
}
