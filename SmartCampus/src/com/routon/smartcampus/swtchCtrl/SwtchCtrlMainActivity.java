package com.routon.smartcampus.swtchCtrl;

import java.util.ArrayList;
import java.util.List;

import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.json.TerminalListSwtchBean;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.smartcampus.swtchCtrl.ListGradeAdapter.ListGradeListener;
import com.routon.smartcampus.swtchCtrl.SwtchCtrlDataRequest.DataRequestListener;
import com.routon.smartcampus.swtchCtrl.treeAdapter.DataBean;
import com.routon.smartcampus.swtchCtrl.treeAdapter.RecyclerAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;



public class SwtchCtrlMainActivity extends BaseActivity implements DataRequestListener, ListGradeListener, OnClickListener {


	
	private String Tag = "SwtchCtrlMainActivity";
	private RecyclerView classRev;
	private RecyclerAdapter adapter;
//	private List<TerminalGroup> mGroups = new  ArrayList<TerminalGroup>();
	private ImageView backMenu;
	private ArrayList<DataBean> dataBeanList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
	
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_swtch_ctrl_main);
		initView();
		
		mBackListener = this;
		mDownListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SwtchCtrlDataRequest.getAllTerminals();	
			}
		};
		SwtchCtrlDataRequest.delegate = this;
		SwtchCtrlDataRequest.baseAcitivity = this;
		SwtchCtrlDataRequest.getAllTerminals();	
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SwtchCtrlDataRequest.delegate = this;
		SwtchCtrlDataRequest.baseAcitivity = this;
		
	}
	private void initView() {
		// TODO Auto-generated method stub

		backMenu = (ImageView) findViewById(R.id.img_back);
		classRev = (RecyclerView)findViewById(R.id.listView);
//		classRev.setLayoutManager(new LinearLayoutManager(this));
//		adapter = new ListGradeAdapter(this, null);
//		adapter.setListGradeListener(this);
//		classRev.setAdapter(adapter);
		
		
		initListView();
		
		backMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				finish();
				overridePendingTransition(R.animator.slide_in_left,
						R.animator.slide_out_right);
			}
		});
	}

	

  
    private void initData(){

    	
    	List<String> listExpandIds = new ArrayList<String>();
        
    	for (DataBean item:dataBeanList)
    	{
    		if (item.type == DataBean.PARENT_ITEM && item.isExpand)
    		{
    			listExpandIds.add(item.ID);
    		}
    	}
    	dataBeanList.clear();
    	
        List<TerminalGroup> list = SwtchCtrlDataRequest.divTerminalGroup();
        int size = list.size();
        for (int index = 0 ; index < size; index++)
        {
        	TerminalGroup item  = list.get(index);
        	 DataBean dataBean = new DataBean();
             dataBean.ID = (index+"");
             if (listExpandIds.contains(dataBean.ID))
             {
            	 dataBean.isExpand = true;
             }
             dataBean.type = (DataBean.PARENT_ITEM);
             dataBean.pName = item.pName;
             List<DataBean> childList = new  ArrayList<DataBean>();
             
             for (TerminalListdatasBean ter: item.terminals)
             {
            	 DataBean dataBean1 = new DataBean();              
                 dataBean1.type = (DataBean.CHILD_ITEM);
                 dataBean1.mTerminaldata = ter;
                 childList.add(dataBean1);
             }
             if (childList.size() >= 1)
             {
            	 int lastIndex = childList.size() - 1;
            	 childList.get(lastIndex).isLast = true;
             }
             dataBean.childBean = childList ;
             dataBeanList.add(dataBean);
             
             if (dataBean.isExpand)
             {
            	 
            	 dataBeanList.addAll(dataBeanList.size(), dataBean.childBean);
             }
        }
        
        adapter.notifyDataSetChanged();
//        setData();
    }

    
    
    private void initListView(){
    	
    	if (dataBeanList == null)
        {
    		dataBeanList = new ArrayList<DataBean>();
        }
    	classRev.setLayoutManager(new LinearLayoutManager(this));
    	adapter = new RecyclerAdapter(this,dataBeanList);
    	classRev.setAdapter(adapter);
        //滚动监听
    	adapter.setOnScrollListener(new RecyclerAdapter.OnScrollListener() {
            @Override
            public void scrollTo(int pos) {
            	classRev.scrollToPosition(pos);
            }
        });
    }
	@Override
	public void onAllTerminalsObtain() {
		// TODO Auto-generated method stub
		SwtchCtrlDataRequest.initGroupList();
	}

	@Override
	public void onAllGroupList() {
		// TODO Auto-generated method stub
//		List<TerminalGroup> list = SwtchCtrlDataRequest.divTerminalGroup();

//		adapter.setListGradeData(list);
//		adapter.notifyDataSetChanged();
		initData();
	}


	@Override
	public void onItemClick(View view, int position) {
		// TODO Auto-generated method stub
		int index = position;
		SwtchCtrlDataRequest.selIndex = index;
		
		Intent intent=new Intent(this,SwtchCtrlDetailActivity.class);
		startActivityForResult(intent, 0);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	
		
//		List<TerminalGroup> list = SwtchCtrlDataRequest.mTerminalsGroup;
//		adapter.setListGradeData(list);
//		adapter.notifyDataSetChanged();
		
	}
	@Override
	public void onItemSwitchClick(View view, int position, boolean isChecked) {
		// TODO Auto-generated method stub
		TerminalGroup groupData = SwtchCtrlDataRequest.mTerminalsGroup.get(position);
		
		for (TerminalListdatasBean terminal :groupData.terminals)
		{
			for (TerminalListSwtchBean swtch:terminal.mswtchs)
			{
				swtch.status = isChecked == true ? 1 : 0;
				
			}
		}
		SwtchCtrlDataRequest.sendSwtchForGroup(groupData, isChecked);
		SwtchCtrlDataRequest.shouldRefresh = true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//back click
		onBackPressed();
	}



}

