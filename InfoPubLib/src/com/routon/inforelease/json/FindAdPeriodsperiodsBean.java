package com.routon.inforelease.json;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class FindAdPeriodsperiodsBean implements Parcelable,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5924887648062769547L;
	public int loops;
	public int periodId;
	public int max;
	public int adId;
	public String beginTime;
	public String endTime;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(loops);
		dest.writeInt(periodId);
		dest.writeInt(max);
		dest.writeInt(adId);
		dest.writeString(beginTime);
		dest.writeString(endTime);
	}

	public static final Parcelable.Creator<FindAdPeriodsperiodsBean> CREATOR = new Parcelable.Creator<FindAdPeriodsperiodsBean>() {
		public FindAdPeriodsperiodsBean createFromParcel(Parcel in) {
			return new FindAdPeriodsperiodsBean(in);
		}

		public FindAdPeriodsperiodsBean[] newArray(int size) {
			return new FindAdPeriodsperiodsBean[size];
		}
	};

	private FindAdPeriodsperiodsBean(Parcel in) {
		loops = in.readInt();
		periodId = in.readInt();
		max = in.readInt();
		adId = in.readInt();
		beginTime = in.readString();
		endTime = in.readString();
	}
	
	public FindAdPeriodsperiodsBean() {
		
	}
};
