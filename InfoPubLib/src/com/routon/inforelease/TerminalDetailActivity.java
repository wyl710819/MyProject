package com.routon.inforelease;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.classinfo.ClassInfoListActivity;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.inforelease.plan.PlanListActivity;
import com.routon.inforelease.snotice.SNoticeListActivity;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.widget.SettingItem;

/**
 * 终端详情界面
 * 
 * @author xiaolp
 * 
 */
public class TerminalDetailActivity extends CustomTitleActivity {
	private TerminalListdatasBean mBean = null;
	public static final String DETAIL_TAG = "detail";
	private boolean mShowNextBtn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_terminaldetail);

		Parcelable p = getIntent().getParcelableExtra(TerminalDetailActivity.DETAIL_TAG);
		mBean = (TerminalListdatasBean) (p);
		
		//默认显示按钮
		mShowNextBtn = this.getIntent().getBooleanExtra(CommonBundleName.TITLEBAR_NEXT_BTN_VISIBLE_TAG, true);

		this.initTitleBar(R.string.termodedetail);
		if( mShowNextBtn == true ){
			this.setTitleNextImageBtnClickListener(R.drawable.detail, new View.OnClickListener() {
	
				@Override
				public void onClick(View v) {
					if( InfoReleaseApplication.isEduPlatform == false ){//信息发布，显示终端节目列表
						showTerminalPlanList();
					}else{//泛在教育，显示班牌和信息选择界面
						showPopupMenu(v);
					}
				}
			});
		}

		updateDetail();
	}
	
	private void showPopupMenu(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.terminal_detail_list_menu, popup.getMenu());
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if( item.getItemId() == R.id.btn_terminal_class_list ){
					TerminalListdatasBean data = mBean;

					Intent intent = new Intent(TerminalDetailActivity.this, ClassInfoListActivity.class);
					intent.putExtra("id", String.valueOf(data.archiveid));
					Log.d("terminal detail","data.archiveid:"+data.archiveid);
					startActivity(intent);
				}else if( item.getItemId() == R.id.btn_terminal_class_info_list ){
					TerminalListdatasBean data = mBean;

					Intent intent = new Intent(TerminalDetailActivity.this, SNoticeListActivity.class);
					intent.putExtra("id", String.valueOf(data.archiveid));
					startActivity(intent);
				}
				return false;
			}
		});
		popup.show();
	}

	private void updateDetail() {
		TerminalListdatasBean bean = mBean;

		SettingItem item = (SettingItem) (findViewById(R.id.terminalid));
		item.setName(R.string.terminal_id);
		if (bean.terminalid != null) {
			item.setInfo(bean.terminalid);
		}

		item = (SettingItem) (findViewById(R.id.installplace));
		item.setName(R.string.install_space);
		if (bean.installplace != null) {
			item.setInfo(bean.installplace);
		}

		item = (SettingItem) (findViewById(R.id.terplace));
		item.setName(R.string.install_position);
		if (bean.terplace2 != null) {
			item.setInfo(bean.terplace2);
		}

		item = (SettingItem) (findViewById(R.id.bsgroup));
		item.setName(R.string.group_name);
		if (bean.bsgroup != null) {
			item.setInfo(bean.bsgroup);
		}

		item = (SettingItem) (findViewById(R.id.olstate));
		item.setName(R.string.olsstate);
		if (bean.olstate != null) {
			item.setInfo(bean.olstate);
		}

		item = (SettingItem) (findViewById(R.id.softver));
		item.setName(R.string.termodesoftver);
		item.setInfoMaxWidth((int)(this.getResources().getDimension(R.dimen.setting_item_day_info_width)));
		if (bean.softver != null) {
			item.setInfo(bean.softver);
		}

		item = (SettingItem) (findViewById(R.id.logintime));
		item.setName(R.string.logintime);
		item.setInfoMaxWidth((int)(this.getResources().getDimension(R.dimen.setting_item_day_info_width)));
		if (bean.logintime != null) {
			item.setInfo(bean.logintime);
		}

		item = (SettingItem) (findViewById(R.id.lastcomutime));
		item.setName(R.string.lastcomutime);
		item.setInfoMaxWidth((int)(this.getResources().getDimension(R.dimen.setting_item_day_info_width)));
		if (bean.lastcomutime != null) {
			item.setInfo(bean.lastcomutime);
		}

		item = (SettingItem) (findViewById(R.id.firstcommtime));
		item.setName(R.string.firstcommtime);
		item.setInfoMaxWidth((int)(this.getResources().getDimension(R.dimen.setting_item_day_info_width)));
		if (bean.firstcommtime != null) {
			item.setInfo(bean.firstcommtime);
		}

		item = (SettingItem) (findViewById(R.id.onofftime));
		item.setName(R.string.onofftime);
		item.setInfoMaxWidth((int)(this.getResources().getDimension(R.dimen.setting_item_day_info_width)));
		if (bean.onofftime != null) {
			item.setInfo(bean.onofftime);
		}

		item = (SettingItem) (findViewById(R.id.holidayonofftime));
		item.setName(R.string.holidayonofftime);
		item.setInfoMaxWidth((int)(this.getResources().getDimension(R.dimen.setting_item_day_info_width)));
		if (bean.holidayonofftime != null) {
			item.setInfo(bean.holidayonofftime);
		}
	}

//	public void setTerminalDetail(TerminalListdatasBean bean) {
//		mBean = bean;
//	}

	private void showTerminalPlanList() {
		TerminalListdatasBean data = mBean;

		Intent intent = new Intent(this, PlanListActivity.class);
		intent.putExtra("id", data.terminalid);
		startActivity(intent);
	}
}
