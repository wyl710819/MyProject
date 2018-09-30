package com.routon.inforelease.json;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;




public class TerminalListdatasBean implements Parcelable{
	/**
	 * 
	 */
//	private static final long serialVersionUID = -951629495635078537L;
	public String terplace2;
	public String terip;
	public String bsgroup;
	public String holidayonofftime;
	public String lastcomutime;
	public String onofftime;
	public String terminalid;
	public int type;
	public String typename;
	public String ipaddress;
	public String firstcommtime;
	public String createtime;
	public String installplace;
	public String logintime;
	public String olstate;
	public int txtTerminalState;
	public String disksize;
	public String softver;
	public int groupid;
	public int archiveid;
	public String termodealias;  
	public String areastr;
	public String organization;
	public String btmac;
	public List<TerminalListSwtchBean> mswtchs;
	
	@Override
    public int describeContents()
    {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(terplace2);
        out.writeString(terip);
        out.writeString(bsgroup);
        out.writeString(holidayonofftime);
        out.writeString(lastcomutime);
        out.writeString(onofftime);
        out.writeString(terminalid);
        
        out.writeInt(type);
        
        out.writeString(typename);
        out.writeString(ipaddress);
        out.writeString(firstcommtime);
        out.writeString(createtime);
        out.writeString(installplace);
        out.writeString(logintime);
        out.writeString(olstate);
        
        out.writeInt(txtTerminalState);
        
        out.writeString(disksize);
        out.writeString(softver);
        
        out.writeInt(groupid);
        out.writeInt(archiveid);
        
        out.writeString(termodealias);    
        
        out.writeString(areastr);
        out.writeString(organization);
        
        out.writeString(btmac);
        
    }
    
    public static final Parcelable.Creator<TerminalListdatasBean> CREATOR = new Creator<TerminalListdatasBean>()
    {
        @Override
        public TerminalListdatasBean[] newArray(int size)
        {
            return new TerminalListdatasBean[size];
        }
        
        @Override
        public TerminalListdatasBean createFromParcel(Parcel in)
        {
            return new TerminalListdatasBean(in);
        }
    };
    
    TerminalListdatasBean(){
    	
    }
    
    TerminalListdatasBean(Parcel in)
    {
    	if( in == null ) return;
    	terplace2 = in.readString();
    	terip = in.readString();
    	bsgroup = in.readString();
    	holidayonofftime = in.readString();
    	lastcomutime = in.readString();
    	onofftime = in.readString();
    	terminalid = in.readString();
    	
    	type = in.readInt();
    	
    	typename = in.readString();
    	ipaddress = in.readString();
    	firstcommtime = in.readString();
    	createtime = in.readString();
    	installplace = in.readString();
    	logintime = in.readString();
    	olstate = in.readString();
    	
    	txtTerminalState = in.readInt();
    	
    	disksize = in.readString();
    	softver = in.readString();
    	
    	groupid = in.readInt();
    	archiveid = in.readInt();
    	
    	termodealias = in.readString();
    	areastr = in.readString();
    	organization = in.readString();
    	
    	btmac = in.readString();
    }
 };
