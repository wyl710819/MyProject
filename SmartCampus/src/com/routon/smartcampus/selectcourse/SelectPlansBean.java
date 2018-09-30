package com.routon.smartcampus.selectcourse;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;



public class SelectPlansBean implements Serializable{
	
	String typeId ;
	String typeName;
	int subjectCount; 
	ArrayList<SelectSubjectsBean> subjectsList;
	public SelectPlansBean (JSONObject object){
		try {
			typeId=object.optString("typeId");
			typeName=object.optString("typeName");
			subjectCount=object.optInt("subjectCount");
			JSONArray jsonArray=object.optJSONArray("subjects");
			if (jsonArray!=null) {
				Log.d("subjects", "jsonArray!=null" );
				subjectsList=new ArrayList<SelectSubjectsBean>();
				for(int i=0;i<jsonArray.length();i++)
				{
					SelectSubjectsBean bean = new SelectSubjectsBean(jsonArray.getJSONObject(i));
					subjectsList.add(bean);
				}
			}
		}catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
