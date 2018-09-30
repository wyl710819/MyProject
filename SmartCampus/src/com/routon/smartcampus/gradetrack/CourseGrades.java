package com.routon.smartcampus.gradetrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.os.Parcel;
import android.os.Parcelable;

public class CourseGrades implements Parcelable {

	public String course;
	
	public int rank;
	
	public int position;
	
	public List<StudentGrade> grades = new ArrayList<StudentGrade>();
	
	public CourseGrades(String course)
	{
		this.course = course;
	}

	public CourseGrades()
	{

	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeString(course);
		arg0.writeInt(rank);
		arg0.writeInt(position);
		arg0.writeList(grades);
	}
	
	public static final Creator<CourseGrades> CREATOR = new Creator<CourseGrades>() {

		@SuppressWarnings("unchecked")
		@Override
		public CourseGrades createFromParcel(Parcel arg0) {
			CourseGrades courseGrades = new CourseGrades();
			courseGrades.course = arg0.readString();
			courseGrades.rank = arg0.readInt();
			courseGrades.position = arg0.readInt();
			courseGrades.grades = arg0.readArrayList(StudentGrade.class.getClassLoader());
			return courseGrades;
		}

		@Override
		public CourseGrades[] newArray(int arg0) {
			return new CourseGrades[arg0];
		}
	};
	
}
