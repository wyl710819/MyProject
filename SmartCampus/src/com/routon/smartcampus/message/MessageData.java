package com.routon.smartcampus.message;
import android.os.Parcel;
import android.os.Parcelable;



public class MessageData implements Parcelable{
	public int buzId;//业务id
	public int type;//1: 账号推送； 2：分组推送
	public String title;//推送标题
	public String content;//推送内容
	public String channel;//账号或分组
	public String time;//推送时间 2017-08-09 01:02:03
	
	public int isNew;//用于判断是否是未读消息 0 未读　１　已读

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Creator<MessageData> CREATOR = new Creator<MessageData>() {
        @Override
        public MessageData createFromParcel(Parcel in) {
            return new MessageData(in);
        }

        @Override
        public MessageData[] newArray(int size) {
            return new MessageData[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(buzId);
        out.writeInt(type);
        out.writeString(title);
        out.writeString(content);
        out.writeString(channel);
        out.writeString(time);
        out.writeInt(isNew);
    }
    
    public MessageData() {
    	
    }

    protected MessageData(Parcel in) {
    	buzId = in.readInt();
    	type = in.readInt();
    	title = in.readString();
    	content = in.readString();
    	channel = in.readString();
    	time = in.readString();
    	isNew = in.readInt();
    }
}
