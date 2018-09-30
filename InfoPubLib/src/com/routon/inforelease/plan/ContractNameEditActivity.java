package com.routon.inforelease.plan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.R;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

public class ContractNameEditActivity extends CustomTitleActivity {

	private int mContractId;
	private String mContractName;
	private String mResIds;
	
	private EditText mEditContractName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mContractId = bundle.getInt("contractId");
			mContractName = bundle.getString("contractName");
			mResIds = bundle.getString("resIds");
		}
		
		setContentView(R.layout.activity_plan_make_contract_name_edit);
		
		initViews();
	}

	private void initViews() {
		// title bar
		this.initTitleBar(R.string.change_plan_name);
		
		this.setTitleNextImageBtnClickListener(R.drawable.ok, mOnBtnClickedListener);

		mEditContractName = (EditText) findViewById(R.id.edit_contract_name);
		mEditContractName.setText(mContractName);
	}
	
	private void changeContractName() {
		if (mContractName != null && mContractName.length() > 0) {
			String name = mEditContractName.getText().toString();
			if (!name.equals(mContractName)) {
				HttpClientDownloader.getInstance().planEdit(mContractId, name, null, mHandler, MSG_EDIT_CONTRACT);				
			} else {
				finish();
			}
		} else {
			finish();
		}
	}
	
	private View.OnClickListener mOnBtnClickedListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if( v.getId() == R.id.next_step ){
				changeContractName();	
			}
		}
	};
	
	private void onEditContract(String text) {
		Log.v("NameEdit", "plan edit: " + text);
		if (text == null) {
			reportToast("通信错误!");
			return;
		}
		
		BaseBean bean = BaseBeanParser.parseBaseBean(text);
		if (bean == null) {
			reportToast("数据错误!");
			return;
		}
		
		if (bean.code == 1) {
			Toast.makeText(this, "change name sucess", Toast.LENGTH_SHORT).show();
			Intent data = new Intent();
			data.putExtra("contractName", mEditContractName.getText().toString());
			setResult(1, data);
			finish();
		} else if (bean.code == -2) {
			returnToLogin();
		} else {
			reportToast(bean.msg);
		}
	}
	
	private static final int MSG_EDIT_CONTRACT = 3;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
			{
				switch (msg.arg1) {
				case MSG_EDIT_CONTRACT:
					onEditContract((String) msg.obj);
					break;
				}
				break;
				
			}
			}
		}
	};
}
