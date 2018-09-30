package com.routon.inforelease.plan.create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.R;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class CreateProgramActivity extends CustomTitleActivity {

	private static final String TAG = "CreateProgram";
	
	private int titleIds[] = {
			R.string.add_pic_plan,
			R.string.add_text_plan,
			R.string.add_notice			
	};
		
	private ListView listView;
	private SimpleAdapter listAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_program);
	
		initTitleBar(R.string.add_plan);
		
		listView = (ListView) findViewById(R.id.listview);
		
		Resources res = this.getResources();
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
		for(int i = 0; i< titleIds.length; i++){
	        Map<String, Object> map = new HashMap<String, Object>();
	        map.put("title", res.getString(titleIds[i]));
	        datas.add(map);
        }
		
		listAdapter = new SimpleAdapter(this, datas, R.layout.create_program_list_item, 
				new String[] {"title"},
				new int[]{R.id.title});
		
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:{
					addPicturePlan();
					break;
				}
				case 1:{
					addTextPlan();
					break;
				}
				case 2:
					
					break;
				default:
					break;
				}
				
			}
		});
	}
	
	private void addPicturePlan(){
		Intent intent = new Intent(this, PictureSelectActivity.class);
		this.startActivity(intent);
	}
	
	private void addTextPlan(){

		Intent intent = new Intent(this, TextSelectActivity.class);
		this.startActivity(intent);
	}
}
