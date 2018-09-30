package com.routon.inforelease.json;

import java.io.Serializable;




public class UserListdatasBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8133594302487049685L;
	public int userid;
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRealname() {
		return realname;
	}
	public void setRealname(String realname) {
		this.realname = realname;
	}
	public String getPhonenum() {
		return phonenum;
	}
	public void setPhonenum(String phonenum) {
		this.phonenum = phonenum;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getPwdchangetime() {
		return pwdchangetime;
	}
	public void setPwdchangetime(String pwdchangetime) {
		this.pwdchangetime = pwdchangetime;
	}
	public String getLoggingtime() {
		return loggingtime;
	}
	public void setLoggingtime(String loggingtime) {
		this.loggingtime = loggingtime;
	}
	public String getLoggingip() {
		return loggingip;
	}
	public void setLoggingip(String loggingip) {
		this.loggingip = loggingip;
	}
	public String getGroupids() {
		return groupids;
	}
	public void setGroupids(String groupids) {
		this.groupids = groupids;
	}
	public String getGroupnames() {
		return groupnames;
	}
	public void setGroupnames(String groupnames) {
		this.groupnames = groupnames;
	}
	public String username;
	public String realname;
	public String phonenum;
	public String email;
	public String address;
	public String createtime;
	public String state;
	public String pwdchangetime;
	public String loggingtime;
	public String loggingip;
	public String groupids;
	public String groupnames;   
 };
