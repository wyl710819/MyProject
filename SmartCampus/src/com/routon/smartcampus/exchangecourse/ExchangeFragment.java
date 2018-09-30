package com.routon.smartcampus.exchangecourse;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.exchangecourse.ExchangeCourseView.LessonCell;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.ClassSelListViewAdapter;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.widgets.Toast;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnScrollChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

//老师换课界面
public class ExchangeFragment extends Fragment implements OnClickListener{

	private static final String TAG = "ExchangeFragment";
	private ExchangeCourseView exchangeCourseView;
	private ExchangeCourseView exchangeCourseView1;
	
	private TextView title_view;
	private FrameLayout mClassDropdown;
	private ListView classListView;
	private TextView classTextView;
	private View dropdownMask;
	private LinearLayout classCoursetableLL;
	private ProgressDialog progressDialog;
	private ImageView arrow_left_view;
	private ImageView arrow_right_view;
	
	private ArrayList<Integer> classGroupIdList;
	private ArrayList<String> mClassList;
	private int classGroupId;
	private String teacherId;
	private int userId;
	private boolean mPopViewShow;
	protected OnClickListener mBackListener = null;
	private RelativeLayout mContentView = null;

	private boolean ifHasTwoWeek;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_exchange_layout, container, false);
		return view;
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	
		super.onActivityCreated(savedInstanceState);
		mPopViewShow = true;	
		initView(getView());
	}
	
	private ArrayList<LessonCell> mSelectedCells = new ArrayList<LessonCell>();
	
	private void clearLessonCells(){
		for(LessonCell cell:mSelectedCells){
			LessonCell.clear(cell);
		}
		mSelectedCells.clear();
	}

	private void startExchangeAni(){
		SwapHelper.swapViews(mContentView,mSelectedCells.get(0).view, mSelectedCells.get(1).view,new SwapHelper.swapAniListener() {
			
			@Override
			public void swapStart() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void swapFinish() {
				// TODO Auto-generated method stub
				String tempCourse = mSelectedCells.get(0).course.course;
				String tempTeacher = mSelectedCells.get(0).course.teacherName;
				String sid = mSelectedCells.get(0).course.sid;
				//交换数据
				mSelectedCells.get(0).course.course = mSelectedCells.get(1).course.course;
				mSelectedCells.get(0).course.teacherName = mSelectedCells.get(1).course.teacherName;
				mSelectedCells.get(0).course.sid = mSelectedCells.get(1).course.sid;
				mSelectedCells.get(1).course.course = tempCourse;
				mSelectedCells.get(1).course.teacherName = tempTeacher;
				mSelectedCells.get(1).course.sid = sid;
				((ExchangeCourseAdapter)(mSelectedCells.get(0).listview.getAdapter())).notifyDataSetChanged();
				((ExchangeCourseAdapter)(mSelectedCells.get(1).listview.getAdapter())).notifyDataSetChanged();
				clearLessonCells();
			}
		});
	}
	
	private void handleExchangeRequest(){
		int[] classGroupIds = new int[2];
		classGroupIds[0] = classGroupId;
		classGroupIds[1] = classGroupId;
		int[] lessons = new int[2];
		lessons[1] = mSelectedCells.get(1).course.lesson;
		lessons[0] = mSelectedCells.get(0).course.lesson;
		int[] weeks = new int[2];
		weeks[1] = mSelectedCells.get(1).week;
		weeks[0] = mSelectedCells.get(0).week;
		String[] courseNames = new String[2];
		courseNames[1] = mSelectedCells.get(0).course.course;
		courseNames[0] = mSelectedCells.get(1).course.course;
		String[] teacherSids = new String[2];

		teacherSids[1] = mSelectedCells.get(0).course.sid;

		teacherSids[0] = mSelectedCells.get(1).course.sid;
		int[] weekevens = new int[2];
		weekevens[1] = mSelectedCells.get(1).weekeven;
		weekevens[0] = mSelectedCells.get(0).weekeven;
		String urlString = SmartCampusUrlUtils.getExchangeCourseUrl(classGroupIds, 
				lessons, weeks, courseNames,
				teacherSids, weekevens);
		Log.d(TAG,"handleExchangeRequest urlString:"+urlString);
		showMyProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.e(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {	
								startExchangeAni();
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(getActivity());
								clearLessonCells();
							} else {
								Log.e(TAG, "####"+response.getString("msg"));
								clearLessonCells();
								
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}						
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");

						if (InfoReleaseApplication.showNetWorkFailed(getActivity()) == true) {
							// showToast("按日期查询作业失败!");
						}
						hideMyProgressDialog();
						
						clearLessonCells();
					}
					
				});
		
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void confirmToExchangeDialog(){
		AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
		builder.setMessage("\n确认要换课吗？\n").setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				clearLessonCells();
			}
		}).setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				handleExchangeRequest();//处理确认换课事件
			}
		});
		AlertDialog alertDialog=builder.create();
		alertDialog.setCancelable(false);
		alertDialog.show();
	}
	
	public void initView(View view){
		Log.e("#####", "initView begin");
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		mContentView = (RelativeLayout) view.findViewById(R.id.fragment_main);
		LinearLayout coursell = (LinearLayout)view.findViewById(R.id.coursell);
		
		DisplayMetrics dm = new DisplayMetrics();
	    wm.getDefaultDisplay().getMetrics(dm);
	    int width = dm.widthPixels;         // 屏幕宽度（像素）
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,LayoutParams.WRAP_CONTENT);
		exchangeCourseView = new ExchangeCourseView(this.getActivity());
		exchangeCourseView1 = new ExchangeCourseView(this.getActivity());
		exchangeCourseView.setLayoutParams(params);
		exchangeCourseView.setVisibility(View.VISIBLE);
		exchangeCourseView1.setLayoutParams(params);
		exchangeCourseView1.setVisibility(View.GONE);
		ExchangeCourseView.onCellClick cellListener = new ExchangeCourseView.onCellClick() {
			
			@Override
			public void onCellClick(LessonCell cell) {
				// TODO Auto-generated method stub
				mSelectedCells.add(cell);
				if( mSelectedCells.size() == 2 ){
					if( (mSelectedCells.get(0).course.course == null || mSelectedCells.get(0).course.course.isEmpty() == true )
							&& (mSelectedCells.get(1).course.course == null || mSelectedCells.get(1).course.course.isEmpty() == true ) ){
						Toast.makeText(getContext(), "无课程信息，不能换课", Toast.LENGTH_LONG).show();
						clearLessonCells();
						return;
					}
					confirmToExchangeDialog();
				}
			}
			
			public void cancelClick(View view){
				for(LessonCell cell:mSelectedCells){
					if( cell.view == view ){
						mSelectedCells.remove(cell);
					}
				}
			}
		};
		exchangeCourseView.setOnCellClick(cellListener);
		exchangeCourseView1.setOnCellClick(cellListener);
		
		final HorizontalScrollView courseScrollview = (HorizontalScrollView)view.findViewById(R.id.coursescrollview);
		courseScrollview.setOnScrollChangeListener(new OnScrollChangeListener(){
			@Override
			public void onScrollChange(View arg0, int l, int t, int oldl, int oldt){
				// TODO Auto-generated method stub
				int  screenW =  ((WindowManager)getContext()  
		                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()  
		                .getWidth();  
		             
				if( exchangeCourseView1.getVisibility() == View.VISIBLE ){
					if( l > 0 ){//左边箭头可见
						arrow_left_view.setVisibility(View.VISIBLE);		
					} else{
						arrow_left_view.setVisibility(View.GONE);
					}
					if( l < screenW ){//右边箭头可见
						arrow_right_view.setVisibility(View.VISIBLE);
					}else{
						arrow_right_view.setVisibility(View.GONE);
					}
				}
			}
			
		});	
		coursell.addView(exchangeCourseView);
		coursell.addView(exchangeCourseView1);
		title_view = (TextView)view.findViewById(R.id.title_view);
		mClassDropdown=(FrameLayout)view.findViewById(R.id.dropdown_coursetable_class);
		classListView=(ListView)view.findViewById(R.id.dropdown_listview_coursetable_class);
		classTextView=(TextView)view.findViewById(R.id.coursetable_class_tv);
		dropdownMask = view.findViewById(R.id.dropdown_mask_coursetable);
		classCoursetableLL=(LinearLayout)view.findViewById(R.id.tv_class_linear);
		arrow_right_view=(ImageView)view.findViewById(R.id.arrow_right_view);
		arrow_left_view=(ImageView)view.findViewById(R.id.arrow_left_view);
		title_view.setText("我要换课");
		dropdownMask.setOnClickListener(this);
		userId = InfoReleaseApplication.authenobjData.userId;
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
				Log.e(TAG, "班级id:" + classGroupId);
				classTextView.setText(mClassList.get(position));
				getThisWeekClassCourse(classGroupId);
			}
		});	
		
		getTeacherId(userId,2);
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
//						Log.d(TAG, "response=" + response);
						if( getContext() == null ){
							return;
						}
						try {
							if(response.getInt("code")==0){
								JSONObject  obj = response.getJSONObject("datas");
								teacherId = String.valueOf(obj.optInt("sid"));
								exchangeCourseView.setTeacherId(teacherId);
								exchangeCourseView1.setTeacherId(teacherId);
								Log.e(TAG,"getTeacherId  #### teacher id:"+teacherId);
								getClassListData();
							}else if (response.getInt("code") == -2) {
								hideMyProgressDialog();
								InfoReleaseApplication.returnToLogin(getActivity());
							} else {
								Log.e(TAG, "getTeacherId"+response.getString("msg"));
								hideMyProgressDialog();
								Toast.makeText(getActivity(), "查询老师信息失败!", Toast.LENGTH_SHORT).show();
							}
						}catch(Exception e){
							hideMyProgressDialog();
							e.printStackTrace();
							Toast.makeText(getActivity(), "查询老师信息失败!", Toast.LENGTH_SHORT).show();
						}
					}
			
		},  new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				if( getContext() == null ){
					return;
				}
				Log.e(TAG, "getTeacherId  "+"sorry,Error");
				if( InfoReleaseApplication.showNetWorkFailed(getActivity()) == true ){
					Toast.makeText(getActivity(), "查询老师信息失败!", Toast.LENGTH_SHORT).show();
				}
				hideMyProgressDialog();
			}
		});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void getClassListData() {
		showMyProgressDialog();
		GroupListData.getClassListData(getActivity(), new DataResponse.Listener<ArrayList<GroupInfo>>() {
			
			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				// TODO Auto-generated method stub
				hideMyProgressDialog();
				classGroupIdList = new ArrayList<Integer>();
				mClassList = new ArrayList<String>();
				/*
				mClassList.add("本周课表");
				classGroupIdList.add(0);
				*/
				for (int i = 0; i < classGroups.size(); i++) {
					mClassList.add(classGroups.get(i).getName());
					classGroupIdList.add(classGroups.get(i).getId());
					Log.e(TAG, "getClassListData :"+classGroups.get(i).getName()+":"+classGroups.get(i).getId());
				}
				if (classGroups.size() > 0) {
					if (mClassList.size()>0) {
						classTextView.setText(mClassList.get(0));
						classGroupId = classGroupIdList.get(0);
						Log.e(TAG,"getClassListData   #### class Id:"+classGroupId);
						getThisWeekClassCourse(classGroupId);
					}					
					classListView.setAdapter(new ClassSelListViewAdapter(getActivity(), mClassList));
				}else{
					hideMyProgressDialog();
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
			progressDialog = ProgressDialog.show(getActivity(), "", "...loading...");
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
				classListView.setAdapter(new ClassSelListViewAdapter(getActivity(), mClassList));
				listViewOutAnim();
			}
		} else {
			listViewInAnim();
		}
	}
	
	private void listViewOutAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.VISIBLE);
		classListView.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.dd_menu_in));
		dropdownMask.setVisibility(View.VISIBLE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.dd_mask_in));
		mClassDropdown.setVisibility(View.VISIBLE);
		mPopViewShow = false;
	}
	
	private void listViewInAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.GONE);
		classListView.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.dd_menu_out));
		dropdownMask.setVisibility(View.GONE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.dd_mask_out));
		mClassDropdown.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.dd_mask_out));
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
			}	
	}
    
	@Override
	public void onStop(){
		// TODO Auto-generated method stub
		super.onStop();
		hideMyProgressDialog();
	}
	
	//获取当前周数据
	public void getThisWeekClassCourse(final int classGroupId) {
		this.clearLessonCells();
		
		this.exchangeCourseView.setVisibility(View.VISIBLE);
		this.exchangeCourseView1.setVisibility(View.GONE);
		arrow_left_view.setVisibility(View.GONE);
		arrow_right_view.setVisibility(View.GONE);	
		Log.e(TAG, "showTeacherCourseFragment: classGroupId = "+classGroupId+",teacherId = "+teacherId+"\n ifHasTwoWeek = "+ifHasTwoWeek);
		listViewInAnim();
		
		exchangeCourseView.getTeacherTimetableLists(classGroupId,true,new ExchangeCourseView.ResultListener() {	
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				if( exchangeCourseView.hasTwoWeekCourses() == true ){//有双周课表
					
					exchangeCourseView1.getTeacherTimetableLists(classGroupId,false,new ExchangeCourseView.ResultListener() {
						
						@Override
						public void onSuccess() {
							// TODO Auto-generated method stub
							exchangeCourseView1.setVisibility(View.VISIBLE);
							arrow_left_view.setVisibility(View.GONE);
							arrow_right_view.setVisibility(View.VISIBLE);
						}
					});
				}
			}
		});
	}
}
