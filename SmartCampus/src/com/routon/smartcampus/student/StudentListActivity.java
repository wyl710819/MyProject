package com.routon.smartcampus.student;

import java.util.List;

import com.routon.common.BaseFragmentActivity;
import com.routon.edurelease.R;
import com.routon.smartcampus.attendance.AttendanceRankingFragment;
import com.routon.smartcampus.attendance.AttendanceStatisticsFragment;
import com.routon.smartcampus.flower.RecentIssuedFragment;
import com.routon.smartcampus.utils.MyBundleName;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StudentListActivity extends BaseFragmentActivity implements OnClickListener,StudentListFragment.FragmentInteraction {

	private Fragment studentFragment = null;
//	private Fragment prizeFragment = null;
	private Fragment statisticsFragment = null;
	private Fragment attendanceRankingFragment = null;
	private Fragment attendanceStatisticsFragment = null;
	private Fragment mRecentIssuedFragment = null;
    public boolean isCanShow=true;
    public boolean isCanClick;
	private boolean mFamilyVersion = false;
	
	private int mAppType = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_student);
		
		mAppType = this.getIntent().getIntExtra(MyBundleName.STUDENT_APP_TYPE, MyBundleName.TYPE_STUDENT_FLOWER);
		mFamilyVersion = this.getIntent().getBooleanExtra(MyBundleName.FAMILY_VERSION,false);

		if( mAppType == MyBundleName.TYPE_STUDENT_SCORE || mAppType == MyBundleName.TYPE_STUDENT_OPINION){
			 FragmentManager fragmentManager = getSupportFragmentManager();
			 // 开启事务
			 FragmentTransaction transaction = fragmentManager.beginTransaction();
			 StudentListFragment fragment = new StudentListFragment();
			 ((StudentListFragment)fragment).setFamilyVersion(mFamilyVersion);
			 ((StudentListFragment)fragment).mAppType = mAppType;
			 ((StudentListFragment)fragment).setReturnEnable(true);
			 transaction.add(R.id.fl_content, fragment);
			 transaction.commit();
			 this.findViewById(R.id.bottom_bar).setVisibility(View.GONE);
			 this.setMoveBackEnable(true);
		}else{
			init();
			setMenuItemSelect(R.id.bottom_ll_student, true);
			initFragment(R.id.bottom_ll_student);
		}
	}
	
//	/**  
//     * 监听Back键按下事件,方法1:  
//     * 注意:  
//     * super.onBackPressed()会自动调用finish()方法,关闭  
//     * 当前Activity.  
//     * 若要屏蔽Back键盘,注释该行代码即可  
//     */    
//    @Override    
//    public void onBackPressed() {    
//        super.onBackPressed();    
//        overridePendingTransition(0, R.anim.activity_slide_out);         
//    }  

	private void init() {
		if( mAppType == MyBundleName.TYPE_STUDENT_ATTENDANCE ){
			TextView bottomStatisticsView = (TextView) this.findViewById(R.id.bottom_tv_statistics);
			TextView bottomQueryView = (TextView) this.findViewById(R.id.bottom_tv_recentissued);
			TextView bottomStudentView = (TextView) this.findViewById(R.id.bottom_tv_student);
			bottomStudentView.setText(this.getString(R.string.attendance_student_text));
			bottomQueryView.setText(this.getString(R.string.attendance_query_text));
			bottomStatisticsView.setText(this.getString(R.string.attendance_ranking_title_text));
			
			ImageView imageView=(ImageView) this.findViewById(R.id.bottom_iv_recentissued);
			imageView.setImageResource(R.drawable.ic_absence_query);
		}
		
		this.findViewById(R.id.bottom_ll_student).setOnClickListener(this);
//		this.findViewById(R.id.bottom_ll_prize).setOnClickListener(this);
		this.findViewById(R.id.bottom_ll_statistics).setOnClickListener(this);
		this.findViewById(R.id.bottom_ll_exit).setOnClickListener(this);
		this.findViewById(R.id.bottom_ll_recentissued).setOnClickListener(this);
		
		if( mFamilyVersion == true ){
			findViewById(R.id.bottom_ll_recentissued).setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		if (!isCanClick) {
			this.setMenuItemSelect(R.id.bottom_ll_student, false);
//			this.setMenuItemSelect(R.id.bottom_ll_prize, false);
			this.setMenuItemSelect(R.id.bottom_ll_statistics, false);
			this.setMenuItemSelect(R.id.bottom_ll_exit, false);
			this.setMenuItemSelect(R.id.bottom_ll_recentissued, false);
			this.setMenuItemSelect(v.getId(), true);
			initFragment(v.getId());
		}
	}
	
	private void setMenuItemSelect(int resId, boolean selected) {
		View view = findViewById(resId);
		if (view == null)
			return;
		LinearLayout layout = (LinearLayout) view;
		ImageView imageview = (ImageView) (layout.getChildAt(0));
		TextView textview = (TextView) (layout.getChildAt(1));
		if (resId == R.id.bottom_ll_student) {
			if (selected == false) {
				imageview.setImageResource(R.drawable.menu_student);
			} else {
				imageview.setImageResource(R.drawable.menu_student_sel);
			}
		} 
		else if (resId == R.id.bottom_ll_statistics) {
			if (selected == false) {
				imageview.setImageResource(R.drawable.ic_statistics);
			} else {
				imageview.setImageResource(R.drawable.ic_statistics_sel);
			}
		} 
		else if (resId == R.id.bottom_ll_exit) {
			if (selected == false) {
				imageview.setImageResource(R.drawable.menu_exit);
			} else {
				imageview.setImageResource(R.drawable.menu_exit_sel);
			}
		}else if( resId == R.id.bottom_ll_recentissued ){
			if( mAppType == MyBundleName.TYPE_STUDENT_ATTENDANCE ){
				if (selected == false) {
					imageview.setImageResource(R.drawable.ic_absence_query);
				} else {
					imageview.setImageResource(R.drawable.ic_absence_query_sel);
				}
			}else {
				if (selected == false) {
					imageview.setImageResource(R.drawable.ic_recent_issued);
				} else {
					imageview.setImageResource(R.drawable.ic_recent_issued_sel);
				}
			}
		}

		if (selected == true) {
			textview.setTextColor(this.getResources().getColor(R.color.student_bottom_sel_color));
		} else {
			textview.setTextColor(this.getResources().getColor(R.color.student_bottom_color));
		}
	}


	private void initFragment(int id) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (studentFragment != null) {
			transaction.hide(studentFragment);
		}
		if (statisticsFragment != null) {
			transaction.hide(statisticsFragment);
		}
		 
		if( attendanceRankingFragment != null ){
			transaction.hide(attendanceRankingFragment);
		}
		 
		if( mRecentIssuedFragment != null ){
			transaction.hide(mRecentIssuedFragment);
		}
		if( attendanceStatisticsFragment != null ){
			transaction.hide(attendanceStatisticsFragment);
		}

		if (id == R.id.bottom_ll_student) {
			if (studentFragment == null) {
				studentFragment = new StudentListFragment();
				transaction.add(R.id.fl_content, studentFragment, "home");
			} else {
				transaction.show(studentFragment);
			}
			((StudentListFragment)studentFragment).setFamilyVersion(mFamilyVersion);
			((StudentListFragment)studentFragment).mAppType = mAppType;
			isCanShow=true;
		}else if (id == R.id.bottom_ll_exit) {
				 this.finish();							
		} else if (id == R.id.bottom_ll_statistics) {
			 if( mAppType == MyBundleName.TYPE_STUDENT_FLOWER ){
				 if (statisticsFragment == null) {
					 statisticsFragment = new StatisticsFragment();
					 transaction.add(R.id.fl_content, statisticsFragment,
							 "statistics");
				 } else {
					 transaction.show(statisticsFragment);
				 }
			 }else {
				 if (attendanceStatisticsFragment == null) {
					 attendanceStatisticsFragment = new AttendanceStatisticsFragment();
					 transaction.add(R.id.fl_content, attendanceStatisticsFragment,
							 "attendance");
				 } else {
					 transaction.show(attendanceStatisticsFragment);
				 }
			}
		}else if( id == R.id.bottom_ll_recentissued ){
			if (mAppType == MyBundleName.TYPE_STUDENT_FLOWER) {
				if ( mRecentIssuedFragment == null ) {
					mRecentIssuedFragment = new RecentIssuedFragment();
					transaction.add(R.id.fl_content, mRecentIssuedFragment, "home");
				} else {
					transaction.show(mRecentIssuedFragment);
				}
			}else {
				if (attendanceRankingFragment == null) {
					attendanceRankingFragment = new AttendanceRankingFragment();
					transaction.add(R.id.fl_content, attendanceRankingFragment, "me");
				} else {
						 transaction.show(attendanceRankingFragment);
				}
			}
			isCanShow=false;
		}else{
			if (attendanceRankingFragment == null) {
				attendanceRankingFragment = new AttendanceRankingFragment();
				transaction.add(R.id.fl_content, attendanceRankingFragment, "me");
			} else {
					 transaction.show(attendanceRankingFragment);
			}
			isCanShow=false;
		}
		
		transaction.commit();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FragmentManager fm = getSupportFragmentManager();
		Log.d("studentlistactivity","requestCode:"+requestCode);
		List<Fragment> frags = fm.getFragments();

		for (Fragment f : frags) {
			if (f != null && f.isVisible() ) {
				f.onActivityResult(requestCode, resultCode, data);
			}
		}
		// super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void process(Boolean bool) {
		isCanClick=bool;
	}
	
}
