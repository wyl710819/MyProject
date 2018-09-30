package com.routon.inforelease;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.routon.common.BaseFragmentActivity;
import com.routon.inforelease.classinfo.ClassInfoListFragment;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.PlanListFragment;
import com.routon.inforelease.snotice.SNoticeListFragment;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.remotecontrol.BluetoothChatService;
import com.routon.update.UpdateManager;

public class MainActivity extends BaseFragmentActivity implements OnClickListener {
	// private static String TAG = "MainActivity";

	private Fragment programFragment = null;
	private Fragment terminalFragment = null;
	private Fragment classinfoFragment = null;
	private Fragment snoticeFragment = null;
//	private Fragment bluetoothFragment = null;
	private Fragment settingFragment = null;

	private static final String program_fragment_tag = "program";
	private static final String terminal_fragment_tag = "terminal";
	private static final String classinfo_fragment_tag = "classinfo";
	private static final String snotice_fragment_tag = "snotice";
//	private static final String bluetooth_fragment_tag = "bluetooth";
	private static final String setting_fragment_tag = "setting";
	private static final String exit_fragment_tag = "exit";
	
	private boolean mAuditClassInfoAuthority;
	private boolean mAuditSchoolNoticeAuthority;

	public class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// do what you want
			Log.d("main activity", "home key event");
			BluetoothChatService.getInstance().stop();
		}
	}

	private HomeKeyEventBroadCastReceiver homeKeyEventReceiver;
	private boolean mShowSetting = true;
	private boolean mShowExit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		mShowSetting = this.getIntent().getBooleanExtra(CommonBundleName.SHOW_SETTING_FRAGMENT, true);
		mShowExit = this.getIntent().getBooleanExtra(CommonBundleName.SHOW_EXIT_FRAGMENT, false);
		
		mAuditClassInfoAuthority = this.getIntent().getBooleanExtra(CommonBundleName.AuditClassInfoAuthority, false);
		mAuditSchoolNoticeAuthority = this.getIntent().getBooleanExtra(CommonBundleName.AuditSchoolNoticeAuthority, false);

		// 监听Home建
		homeKeyEventReceiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(homeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

		// Log.d(TAG,"onCreate");

		FragmentManager fragmentManager = getSupportFragmentManager();
		if (savedInstanceState != null && savedInstanceState.getBoolean("isMainActivityDestroy", false))
		{
			InfoReleaseApplication.isEduPlatform = savedInstanceState.getBoolean("isEduPlatform", false);
			UrlUtils.server_address = savedInstanceState.getString("serverAddress");
			// 当activity被系统销毁，获取到之前的fragment，并且移除之前的fragment的状态
			programFragment = fragmentManager.findFragmentByTag(program_fragment_tag);
			terminalFragment = fragmentManager.findFragmentByTag(terminal_fragment_tag);
			classinfoFragment = fragmentManager.findFragmentByTag(classinfo_fragment_tag);
			snoticeFragment = fragmentManager.findFragmentByTag(snotice_fragment_tag);
			settingFragment = fragmentManager.findFragmentByTag(setting_fragment_tag);
			if (programFragment != null) {
				fragmentManager.beginTransaction().remove(programFragment).commit();
			}
			if (terminalFragment != null) {
				fragmentManager.beginTransaction().remove(terminalFragment).commit();
			}
			if (classinfoFragment != null) {
				fragmentManager.beginTransaction().remove(classinfoFragment).commit();
			}
			if (snoticeFragment != null) {
				fragmentManager.beginTransaction().remove(snoticeFragment).commit();
			}
			if (settingFragment != null) {
				fragmentManager.beginTransaction().remove(settingFragment).commit();
			}
		}

		if (UpdateManager.mUpdateFlag == false) {
			// 检测软件更新
			UpdateManager manager = new UpdateManager(this);
			manager.checkUpdate();
		}

		initView();

		programFragment = null;
		terminalFragment = null;
		classinfoFragment = null;
		snoticeFragment = null;
		settingFragment = null;

		initEvent();
		restartBotton();
		if (InfoReleaseApplication.getClassInfoPrivilege() == false) {
			initFragment(R.id.ll_program);
			setMenuItemSelect(R.id.ll_program, true);
		} else {
			if( mAuditSchoolNoticeAuthority == true && mAuditClassInfoAuthority == false ){			
				initFragment(R.id.ll_snotice);
				setMenuItemSelect(R.id.ll_snotice, true);
			}else{
				initFragment(R.id.ll_classinfo);
				setMenuItemSelect(R.id.ll_classinfo, true);
			}
		}
	}
	
	

	private void setMenuItemSelect(int resId, boolean selected) {
		View view = findViewById(resId);
		if (view == null)
			return;
		LinearLayout layout = (LinearLayout) view;
		ImageView imageview = (ImageView) (layout.getChildAt(0));
		TextView textview = (TextView) (layout.getChildAt(1));
		if (resId == R.id.ll_terminal) {
			if (selected == false) {
				imageview.setImageResource(R.drawable.ic_terminal);
			} else {
				imageview.setImageResource(R.drawable.ic_terminal_sel);
			}
		} else if (resId == R.id.ll_setting) {
			if (selected == false) {
				imageview.setImageResource(R.drawable.ic_setting);
			} else {
				imageview.setImageResource(R.drawable.ic_setting_sel);
			}
		} else if (resId == R.id.ll_program) {
			if (selected == false) {
				imageview.setImageResource(R.drawable.ic_classinfo);
			} else {
				imageview.setImageResource(R.drawable.ic_classinfo_sel);
			}
		} else if (resId == R.id.ll_classinfo) {
			if (selected == false) {
				imageview.setImageResource(R.drawable.ic_classinfo);
			} else {
				imageview.setImageResource(R.drawable.ic_classinfo_sel);
			}
		} else if (resId == R.id.ll_snotice) {
			if (selected == false) {
				imageview.setImageResource(R.drawable.ic_snotice);
			} else {
				imageview.setImageResource(R.drawable.ic_snotice_sel);
			}
		}else if( resId == R.id.ll_exit ){
			if (selected == false) {
				imageview.setImageResource(R.drawable.menu_exit);
			} else {
				imageview.setImageResource(R.drawable.menu_exit_sel);
			}
		}
		if (selected == true) {
			textview.setTextColor(this.getResources().getColor(R.color.blue));
		} else {
			textview.setTextColor(this.getResources().getColor(R.color.gray));
		}
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isEduPlatform", InfoReleaseApplication.isEduPlatform);
		outState.putString("serverAddress", UrlUtils.server_address);
		outState.putBoolean("isMainActivityDestroy", true);
	}

	private void initEvent() {
		// 设置按钮监听
		this.findViewById(R.id.ll_terminal).setOnClickListener(this);
		this.findViewById(R.id.ll_program).setOnClickListener(this);
		this.findViewById(R.id.ll_exit).setOnClickListener(this);
		this.findViewById(R.id.ll_classinfo).setOnClickListener(this);
		this.findViewById(R.id.ll_snotice).setOnClickListener(this);
		this.findViewById(R.id.ll_setting).setOnClickListener(this);
	}

	private void initView() {

		// 泛在教育
		if (InfoReleaseApplication.isEduPlatform == true) {
			this.findViewById(R.id.ll_program).setVisibility(View.GONE);
		
			if( mAuditClassInfoAuthority == false &&  mAuditSchoolNoticeAuthority == false ){		
				this.findViewById(R.id.ll_classinfo).setVisibility(View.VISIBLE);
				this.findViewById(R.id.ll_snotice).setVisibility(View.VISIBLE);
				this.findViewById(R.id.ll_terminal).setVisibility(View.VISIBLE);
			}else {
				if( mAuditSchoolNoticeAuthority == true ){
					this.findViewById(R.id.ll_snotice).setVisibility(View.VISIBLE);				
				} 
				if( mAuditClassInfoAuthority == true ){
					this.findViewById(R.id.ll_classinfo).setVisibility(View.VISIBLE);
				}
				this.findViewById(R.id.ll_terminal).setVisibility(View.GONE);
			}
		} else {
			this.findViewById(R.id.ll_program).setVisibility(View.VISIBLE);
			this.findViewById(R.id.ll_classinfo).setVisibility(View.GONE);
			this.findViewById(R.id.ll_snotice).setVisibility(View.GONE);
		}
		
		if( mShowSetting == false ){
			this.findViewById(R.id.ll_setting).setVisibility(View.GONE);
		}
		
		if( mShowExit == true ){
			this.findViewById(R.id.ll_exit).setVisibility(View.VISIBLE);
		}

	}

	private void restartBotton() {
		this.setMenuItemSelect(R.id.ll_terminal, false);
		this.setMenuItemSelect(R.id.ll_program, false);
		this.setMenuItemSelect(R.id.ll_classinfo, false);
		this.setMenuItemSelect(R.id.ll_snotice, false);
		this.setMenuItemSelect(R.id.ll_exit, false);
		this.setMenuItemSelect(R.id.ll_setting, false);
	}

	private void initFragment(int id) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		// 开启事务
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (programFragment != null) {
			transaction.hide(programFragment);
		}
		if (terminalFragment != null) {
			transaction.hide(terminalFragment);
		}
		if (settingFragment != null) {
			transaction.hide(settingFragment);
		}
		if (classinfoFragment != null) {
			transaction.hide(classinfoFragment);
		}
		if (snoticeFragment != null) {
			transaction.hide(snoticeFragment);
		}
		// Log.d(TAG,"initFragment index:"+index);
		if (id == R.id.ll_program) {
			if (programFragment == null) {
				programFragment = new PlanListFragment();
				transaction.add(R.id.fl_content, programFragment, program_fragment_tag);
			} else {
				transaction.show(programFragment);
			}
		} else if (id == R.id.ll_terminal) {
			if (terminalFragment == null) {
				terminalFragment = new TerminalListFragment();
				transaction.add(R.id.fl_content, terminalFragment, terminal_fragment_tag);
			} else {
				transaction.show(terminalFragment);
			}
		} else if (id == R.id.ll_exit) {
			 this.finish();				
		} else if (id == R.id.ll_setting) {
			if (settingFragment == null) {
				settingFragment = new SettingFragment();
				transaction.add(R.id.fl_content, settingFragment, setting_fragment_tag);
			} else {
				transaction.show(settingFragment);
			}
		} else if (id == R.id.ll_classinfo) {
			if (classinfoFragment == null) {
				classinfoFragment = new ClassInfoListFragment();
				Bundle args = new Bundle();
				args.putBoolean(CommonBundleName.AuditClassInfoAuthority, mAuditClassInfoAuthority);
				classinfoFragment.setArguments(args);
				transaction.add(R.id.fl_content, classinfoFragment, classinfo_fragment_tag);
			} else {
				transaction.show(classinfoFragment);
				// ((ClassInfoListFragment)(classinfoFragment)).reloadData();
			}
		} else if (id == R.id.ll_snotice) {
			if (snoticeFragment == null) {
				snoticeFragment = new SNoticeListFragment();
				Bundle args = new Bundle();
				args.putBoolean(CommonBundleName.AuditSchoolNoticeAuthority, mAuditSchoolNoticeAuthority);
				snoticeFragment.setArguments(args);
				transaction.add(R.id.fl_content, snoticeFragment, snotice_fragment_tag);
			} else {
				transaction.show(snoticeFragment);
			}
		}
		transaction.commit();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 在每次点击后将所有的底部按钮(ImageView,TextView)颜色改为灰色，然后根据点击着色
		restartBotton();

		this.setMenuItemSelect(v.getId(), true);
		initFragment(v.getId());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(homeKeyEventReceiver);
		BluetoothChatService.getInstance().stop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FragmentManager fm = getSupportFragmentManager();
		List<Fragment> frags = fm.getFragments();
		for (Fragment f : frags) {
			if (f != null && f.isVisible() ) {
				f.onActivityResult(requestCode, resultCode, data);
			}
		}
		// super.onActivityResult(requestCode, resultCode, data);
	}
}
