package com.routon.smartcampus.homework;



import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class FeedbackWorkBean implements Parcelable{
	
	private static final long serialVersionUID = -6887105457672943897L;
	
	public String fileUrl;
	public boolean isLocal=true;
	public int fileType;
	public int audioLength=0;
	public String parent_remark=null;
	public String fileId=null;
	public FeedbackWorkBean(){
		
	}
	
	public FeedbackWorkBean(String _fileUrl,boolean _isLocal,int _fileType){
		fileUrl=_fileUrl;
		isLocal=_isLocal;
		fileType=_fileType;
	}
	FeedbackWorkBean(Parcel par){
		this.fileType=par.readInt();
		this.fileUrl=par.readString();
		this.audioLength=par.readInt();
		this.isLocal=par.readByte() != 0;
		this.fileId=par.readString();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(fileType);
		dest.writeString(fileUrl);
		dest.writeInt(audioLength);
		dest.writeByte((byte)(isLocal?1:0));
		dest.writeString(fileId);
	}
	
	public static final Creator<FeedbackWorkBean> CREATOR = new Creator<FeedbackWorkBean>() {  
		 @Override  
	        public FeedbackWorkBean createFromParcel(Parcel par) {
        	
	        	return new FeedbackWorkBean(par);      
	            
	        }  
	  
	        @Override  
	        public FeedbackWorkBean[] newArray(int size) {  
	            return new FeedbackWorkBean[size];  
	        }  
	};
}
