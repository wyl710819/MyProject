package com.routon.smartcampus.coursetable;

import java.util.ArrayList;

import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.coursetable.CourseTableHelper;
import com.routon.smartcampus.coursetable.TeacherCourseFragment;
import com.routon.smartcampus.coursetable.calendarview.CalendarView;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.ClassSelListViewAdapter;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.widgets.Toast;

import android.app.ProgressDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ClassCourseActivity extends FragmentActivity implements OnClickListener{
	
	private TeacherCourseFragment teacherCourseFragment;
	private ImageView imgBack;
	private TextView title_view;
	private FrameLayout mClassDropdown;
	private ListView classListView;
	private TextView classTextView;
	private View dropdownMask;
	private LinearLayout calRoot;
	private LinearLayout classCoursetableLL;
	private ProgressDialog progressDialog;
	
	private FragmentManager fm;
	private FragmentTransaction transaction;
	private String appType;
	private ArrayList<Integer> classGroupIdList;
	private ArrayList<String> mClassList;
	private int classGroupId;
	private String teacherId;
	private CalendarView calendarView;
	private String teacherName;
	private int userId;
	private StudentBean mStudent;
	private boolean mPopViewShow;
	private float xDown;
	private float yDown;
	private float xMove;
	private float yMove;
	private boolean isBack;
	protected OnClickListener mBackListener = null;
	 //手指上下滑动时的最小速度
    private static final int YSPEED_MIN = 1000;
    //滑动时的最小距离
    private static final int XDISTANCE_MIN = 80;
    //上滑或下滑时的最小距离
    private static final int YDISTANCE_MIN = 100;
    //用于计算手指滑动的速度。
    private VelocityTracker mVelocityTracker;

	private static final String FRAGMENT_TAG = "teacherCourseFragment";
	private static final String TAG = "ClassCourseActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initView();
	}
	
	public void initData(){
		Bundle bundle = getIntent().getExtras();
		appType=bundle.getString("AppType");
		fm = getSupportFragmentManager();
		mPopViewShow = true;
	}
	
	public void initView(){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_course_calendar);
		imgBack = (ImageView)findViewById(R.id.back_btn);
		imgBack.setOnClickListener(this);
		title_view = (TextView) findViewById(R.id.title_view);
		mClassDropdown=(FrameLayout) findViewById(R.id.dropdown_coursetable_class);
		classListView=(ListView)findViewById(R.id.dropdown_listview_coursetable_class);
		classTextView=(TextView) findViewById(R.id.coursetable_class_tv);
		dropdownMask = findViewById(R.id.dropdown_mask_coursetable);
		calRoot=(LinearLayout) findViewById(R.id.cal_root);
		classCoursetableLL=(LinearLayout) findViewById(R.id.tv_class_linear);
		title_view.setText("课表");
		if(appType!=null&&appType.equals("TeacherCourseTable")){
			dropdownMask.setOnClickListener(this);
		    teacherName=InfoReleaseApplication.authenobjData.realName;
			userId=InfoReleaseApplication.authenobjData.userId;
			getTeacherId(userId,2);
			mStudent = (StudentBean) this.getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
			classTextView.setVisibility(View.VISIBLE);
			mClassDropdown.setVisibility(View.VISIBLE);
			classCoursetableLL.setVisibility(View.VISIBLE);					
			classCoursetableLL.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dropdownClick();
				}
			});
			classListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					classGroupId = classGroupIdList.get(position);
					Log.d(TAG, "班级id:" + classGroupId);
					classTextView.setText(mClassList.get(position));
					showTeacherCourseFragment(classGroupId);
					listViewInAnim();
				}
			});
		}else {
			mStudent = (StudentBean) this.getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
			classGroupId = (int) mStudent.groupId;
			classTextView.setVisibility(View.INVISIBLE);
			mClassDropdown.setVisibility(View.INVISIBLE);
			classCoursetableLL.setVisibility(View.INVISIBLE);
			showTeacherCourseFragment(classGroupId);
		}
	}
	
	public void showTeacherCourseFragment(int classGroupId){
		teacherCourseFragment = (TeacherCourseFragment) fm.findFragmentByTag(FRAGMENT_TAG);
		if(teacherCourseFragment == null){
			Bundle bundle = new Bundle();
			bundle.putInt("classGroupId", classGroupId);
			if(appType!=null&&appType.equals("TeacherCourseTable")){
				bundle.putString("sid", teacherId);
			}
			teacherCourseFragment = new TeacherCourseFragment();
			teacherCourseFragment.setArguments(bundle);
			transaction = fm.beginTransaction();
			transaction.add(R.id.framlayout_teachercourse, teacherCourseFragment, FRAGMENT_TAG);
			transaction.commit();
		}else {
			if(classGroupId != 0){
				teacherCourseFragment.getTeacherTimetableLists(classGroupId);
			}else {
				teacherCourseFragment.getTeacherCourseLists(userId);
			}
		}
	}
	
	private void getTeacherId(int userId, int type) {
		// TODO Auto-generated method stub
		String urlString=SmartCampusUrlUtils.getCourseNameUrl(String.valueOf(userId),String.valueOf(type));
		showMyProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						Log.d(TAG, "response=" + response);
						try {
							if(response.getInt("code")==0){
								JSONObject  obj=response.getJSONObject("datas");
								teacherId=String.valueOf(obj.optInt("sid"));
								Log.d(TAG,"老师id:"+teacherId);
								getClassListData();
							}else if (response.getInt("code") == -2) {
								hideMyProgressDialog();
								InfoReleaseApplication.returnToLogin(ClassCourseActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								hideMyProgressDialog();
								Toast.makeText(ClassCourseActivity.this, "查询老师信息失败!", Toast.LENGTH_SHORT).show();
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
			
		},  new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.e(TAG, "sorry,Error");
				if( InfoReleaseApplication.showNetWorkFailed(ClassCourseActivity.this) == true ){
					Toast.makeText(ClassCourseActivity.this, "查询老师信息失败!", Toast.LENGTH_SHORT).show();
				}
				hideMyProgressDialog();
			}
		});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void getClassListData() {
		showMyProgressDialog();
		GroupListData.getClassListData(ClassCourseActivity.this, new DataResponse.Listener<ArrayList<GroupInfo>>() {
			
			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				// TODO Auto-generated method stub
				hideMyProgressDialog();
				classGroupIdList = new ArrayList<Integer>();
				mClassList = new ArrayList<String>();
				mClassList.add("本周课表");
				classGroupIdList.add(0);
				for (int i = 0; i < classGroups.size(); i++) {
					mClassList.add(classGroups.get(i).getName());
					classGroupIdList.add(classGroups.get(i).getId());
					Log.d(TAG,classGroups.get(i).getName()+":"+classGroups.get(i).getId());
				}
				if (classGroups.size() > 0) {
					if (mClassList.size()>0) {
						classTextView.setText(mClassList.get(0));
						classGroupId=classGroupIdList.get(0);
						Log.d(TAG,"班级Id:"+classGroupId);
						showTeacherCourseFragment(classGroupId);
					}					
					classListView.setAdapter(new ClassSelListViewAdapter(ClassCourseActivity.this, mClassList));
				}
			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				hideMyProgressDialog();
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideMyProgressDialog();
			}
		});
	}
	
	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(ClassCourseActivity.this, "", "...loading...");
		}
	}
	private void hideMyProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	private void dropdownClick() {
		if (mPopViewShow) {
			if (mClassList != null) {
				classListView.setAdapter(new ClassSelListViewAdapter(ClassCourseActivity.this, mClassList));
				listViewOutAnim();
			}
		} else {
			listViewInAnim();
		}
	}
	
	private void listViewOutAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.VISIBLE);
		classListView.startAnimation(AnimationUtils.loadAnimation(ClassCourseActivity.this, R.anim.dd_menu_in));
		dropdownMask.setVisibility(View.VISIBLE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(ClassCourseActivity.this, R.anim.dd_mask_in));
		mClassDropdown.setVisibility(View.VISIBLE);
		mPopViewShow = false;
	}
	
	private void listViewInAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.GONE);
		classListView.startAnimation(AnimationUtils.loadAnimation(ClassCourseActivity.this, R.anim.dd_menu_out));
		dropdownMask.setVisibility(View.GONE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(ClassCourseActivity.this, R.anim.dd_mask_out));
		mClassDropdown.startAnimation(AnimationUtils.loadAnimation(ClassCourseActivity.this, R.anim.dd_mask_out));
		mClassDropdown.setVisibility(View.GONE);
		mPopViewShow = true;

	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.dropdown_mask_coursetable:
				if (!mPopViewShow) {
					listViewInAnim();
				}
				break;
			case R.id.back_btn:
				finish();
				break;
			}	
	}
	
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {	
    	createVelocityTracker(event);
         switch (event.getAction()) {
         case MotionEvent.ACTION_DOWN:
               xDown = event.getRawX();
               yDown = event.getRawY();
               break;
         case MotionEvent.ACTION_MOVE:
               xMove = event.getRawX();
               yMove= event.getRawY();
               //滑动的距离
               int distanceX = (int) (xMove - xDown);
               int distanceY= (int) (yMove - yDown);
               //获取顺时速度
               int ySpeed = getScrollVelocity();
               //关闭Activity需满足以下条件：           
               if(distanceX > XDISTANCE_MIN &&(distanceY<YDISTANCE_MIN&&distanceY>-YDISTANCE_MIN)&& ySpeed < YSPEED_MIN) {
            	   isBack = true;
            	   finish();
               }
               break;
         case MotionEvent.ACTION_UP:
               recycleVelocityTracker();
               break;
         default:
               break;
         }
         return isBack ? false : super.dispatchTouchEvent(event);
    }
	
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getYVelocity();
        return Math.abs(velocity);
    }
    
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }
    
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
               mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
   }
    
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		hideMyProgressDialog();
	}

}
