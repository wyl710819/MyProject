package com.routon.smartcampus.gradetrack;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class StudentGrade implements Parcelable{

	public String time;//周
	public String course;//小红花种类
	public int grade;//小红花数量
	public int studentId;//id
	public int average;//平均数量
	public String studentName;
	public int rank;
	
	public StudentGrade(String time,String course,int grade,int studentId,String studentName)
	{
		this.time = time;
		this.course = course;
		this.grade = grade;
		this.studentId = studentId;
		this.studentName = studentName;
	}
	
	public StudentGrade(int studentId,String studentName,int rank,int grade)
	{
		this.studentId = studentId;
		this.studentName = studentName;
		this.rank = rank;
		this.grade = grade;
	}
	
	public StudentGrade(int grade,String time,String course)
	{
		this.grade = grade;
		this.time = time;
		this.course = course;
	}
	
	public StudentGrade()
	{
		
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeString(time);
		dest.writeString(course);
		dest.writeInt(grade);
		dest.writeInt(studentId);
		dest.writeInt(average);
		dest.writeString(studentName);
		dest.writeInt(rank);
	}
	
	public static final Creator<StudentGrade> CREATOR = new Creator<StudentGrade>() {
		@Override
		public StudentGrade createFromParcel(Parcel source) {
			StudentGrade studentGrade = new StudentGrade();
			studentGrade.time = source.readString();
			studentGrade.course = source.readString();
			studentGrade.grade = source.readInt();
			studentGrade.studentId = source.readInt();
			studentGrade.average = source.readInt();
			studentGrade.studentName = source.readString();
			studentGrade.rank = source.readInt();
			return studentGrade;
		}
		
		public StudentGrade[] newArray(int arg0) {
			return new StudentGrade[arg0];
		};
	};
	
	public static void swap(StudentGrade a,StudentGrade b)
	{
		Log.d("MainActivity", a.grade+"---"+b.grade);
		StudentGrade c = new StudentGrade(a.studentId, a.studentName, a.rank, a.grade);
		a = new StudentGrade(b.studentId, b.studentName, b.rank, b.grade);
		b = c;
		Log.d("MainActivity", a.grade+"---"+b.grade);
	}
	
}
