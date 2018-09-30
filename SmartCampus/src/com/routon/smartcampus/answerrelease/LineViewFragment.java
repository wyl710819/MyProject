package com.routon.smartcampus.answerrelease;

import com.routon.edurelease.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LineViewFragment extends Fragment{
	private View view;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState)  
    {
		view=inflater.inflate(R.layout.fragment_answer_line_view, container,false);
		return view;
    }
}
