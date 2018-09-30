package com.routon.smartcampus.schoolcompare;

import java.io.Serializable;

import org.json.JSONObject;

/**
 * @author sj
 * @version 2018年4月28日 上午10:29:06
 */
public class ClassPeroidItemScoreBean implements Serializable{
	public double itemTotalScore;
	public double avgTotalScore;
	public int ratingItemId;
	public ClassPeroidItemScoreBean(JSONObject jsonObject) {
		itemTotalScore=jsonObject.optDouble("itemTotalScore");
		avgTotalScore=jsonObject.optDouble("avgTotalScore");
		ratingItemId=jsonObject.optInt("ratingItemId");
	}
}
