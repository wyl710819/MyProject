package com.routon.smartcampus.bean;

public class HistoryAwardBean {
	
	private int id;
	private String createtime;
	private Award award;
	private String teacherName;
	private int teacherId;
	private int usebonuspoint;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public Award getAward() {
		return award;
	}

	public void setAward(Award award) {
		this.award = award;
	}

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}

	public int getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(int teacherId) {
		this.teacherId = teacherId;
	}

	public int getUsebonuspoint() {
		return usebonuspoint;
	}

	public void setUsebonuspoint(int usebonuspoint) {
		this.usebonuspoint = usebonuspoint;
	}

	public static class Award{
		private int id;
		private String name;
		private String imgUrl;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getImgUrl() {
			return imgUrl;
		}
		public void setImgUrl(String imgUrl) {
			this.imgUrl = imgUrl;
		}
	}
}
