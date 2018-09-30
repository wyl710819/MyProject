package com.routon.smartcampus.swtchCtrl;

import java.util.ArrayList;
import java.util.List;

import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.json.TerminalListSwtchBean;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.smartcampus.answerrelease.AnswerMainActivity;
import com.routon.smartcampus.swtchCtrl.ListDetailAdapter.ListDetailListener;
import com.routon.smartcampus.swtchCtrl.SwtchCtrlDataRequest.DataRequestListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;



public class SwtchCtrlDetailActivity extends BaseActivity implements ListDetailListener, DataRequestListener  {


	
	private String Tag = "SwtchCtrlDetailActivity";
	private RecyclerView classRev;
	private ListDetailAdapter adapter;
	private ImageView backMenu;
	private TextView mTitleView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
	
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_swtch_ctrl_detail);
		initView();
		
		
		SwtchCtrlDataRequest.delegate = this;
		SwtchCtrlDataRequest.baseAcitivity = this;
		
		mBackListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		};
		mDownListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SwtchCtrlDataRequest.getAllTerminals();	
			}
		};
		if (SwtchCtrlDataRequest.shouldRefresh)
		{
			SwtchCtrlDataRequest.getAllTerminals();	
			SwtchCtrlDataRequest.shouldRefresh = false;
		}
//		SwtchCtrlDataRequest.delegate = this;
//		SwtchCtrlDataRequest.getAllTerminals(getApplicationContext());
	
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	
	}
	
	private void initView() {
		// TODO Auto-generated method stub

		backMenu = (ImageView) findViewById(R.id.img_back);
		mTitleView = (TextView) findViewById(R.id.title);
		
		classRev = (RecyclerView)findViewById(R.id.listView);
		classRev.setLayoutManager(new LinearLayoutManager(this));
		TerminalGroup terminalInfo = SwtchCtrlDataRequest.mTerminalsGroup.get(SwtchCtrlDataRequest.selIndex);
		adapter = new ListDetailAdapter(this, terminalInfo);
		adapter.setListGradeListener(this);
		classRev.setAdapter(adapter);
		mTitleView.setText(terminalInfo.pName);
//		adapter.notifyDataSetChanged();
		
		backMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				finish();
				overridePendingTransition(R.animator.slide_in_left,
						R.animator.slide_out_right);
			}
		});
	}


	@Override
	public void onItemClick(View view, int position) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSwtchClick(TerminalListdatasBean terminal, int swtchValue) {
		// TODO Auto-generated method stub
		List<SwtchParm> swtchs = new ArrayList<SwtchParm>();
		String id = terminal.terminalid;
		int swtch = swtchValue;
		int status = 0;
		for (TerminalListSwtchBean item:terminal.mswtchs)
		{
			if (swtchValue == item.swtch)
			{
				status = item.status;
				
			}
		}
		SwtchParm parm =  new SwtchParm(id, swtch, status);
		swtchs.add(parm);
		SwtchCtrlDataRequest.baseAcitivity = this;
		SwtchCtrlDataRequest.sendSwtchCtrl(swtchs);
	}

	@Override
	public void onAllTerminalsObtain() {
		// TODO Auto-generated method stub
		SwtchCtrlDataRequest.initGroupList();
	}

	@Override
	public void onAllGroupList() {
		// TODO Auto-generated method stub
		List<TerminalGroup> list = SwtchCtrlDataRequest.divTerminalGroup();
		TerminalGroup terminalInfo = SwtchCtrlDataRequest.mTerminalsGroup.get(SwtchCtrlDataRequest.selIndex);
		adapter.setTerminalGroup(terminalInfo);
		adapter.notifyDataSetChanged();
	}


}

