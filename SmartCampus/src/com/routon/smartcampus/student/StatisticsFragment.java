package com.routon.smartcampus.student;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseFragment;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.edurelease.R;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.smartcampus.flower.Badge;
import com.routon.smartcampus.flower.BadgeType;
import com.routon.smartcampus.flower.ListViewAnimationFactory;
import com.routon.smartcampus.leave.TeacherLeaveActivity;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.schoolcompare.ClassCompareBean;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class StatisticsFragment extends BaseFragment implements OnClickListener {

	private String TAG = "StatisticsFragment";

	private ArrayList<GroupInfo> classGroups = new ArrayList<GroupInfo>();
	private ArrayList<String> mClassList;
	private ListView nameListView;
	private boolean isListViewShow = true;
	private boolean isClassListShow = true;
	private boolean isBadgeListViewShow = true;
	private boolean isTimeListShow=true;
	private boolean isBadgeTypeListViewShow = true;
	private ListView classListView;
	private FrameLayout classDropdownFl;
	private View dropdownMask;
	private TextView classText;
	private TextView badgeText;
	private ListView badgeListView;
	private FrameLayout badgeDropdownFl;
	private String firstWeekDay;
	private String lastWeekDay;
	private String lastMounthDay;
	private String firstMounthDay;
	private String beginDate=null;
	private String endDate=null;
	private TextView classBadgeText;
	
	private TextView badgeTypeText;

	private ListView badgeTypeListView;

	private ListView timeListView;
	private TextView timeText;
	private FrameLayout timeDropdownFl;
	private FrameLayout badgeTypeDropdownFl;
	private ArrayList<BadgeType> flowerTypes;
	private ArrayList<String> badgeTypeNameList;
	private ArrayList<Integer> badgeTypeIdList;
	private ArrayList<Badge> badges;
	private ArrayList<String> badgeNameList;
	private ArrayList<String> timeList;
	private boolean isbadgeTypeListOpen=false;
	private ArrayList<StudentBean> mStudentBadgeCountBeanList;
	
	private int classId;
	private int badgeTypeId;
	
	//颁发人id
	private int mTeacherUserId = 0;
	private BadgeType badgeType;
	private Badge badge;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_student_statistics, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initViews(getView());

		initData();
	}

	private void initViews(View view) {
		classText = (TextView) view.findViewById(R.id.student_class_edit);
		badgeText = (TextView) view.findViewById(R.id.student_badge_edit);
		timeText=(TextView) view.findViewById(R.id.time_type);
		timeListView=(ListView) view.findViewById(R.id.dropdown_listview_time_type);
		timeDropdownFl=(FrameLayout) view.findViewById(R.id.dropdown_fl_time_type);
		badgeText.setTextColor(Color.GRAY);
		badgeTypeText = (TextView) view.findViewById(R.id.student_badge_edit_type);
		nameListView = (ListView) view.findViewById(R.id.student_name_listview);
		classBadgeText = (TextView) view.findViewById(R.id.student_class_badge);
		
		dropdownMask = view.findViewById(R.id.dropdown_mask);
		classListView = (ListView) view.findViewById(R.id.dropdown_listview_class);
		classDropdownFl = (FrameLayout) view.findViewById(R.id.dropdown_fl_class);

		badgeListView = (ListView) view.findViewById(R.id.dropdown_listview_badge);
		
		badgeDropdownFl = (FrameLayout) view.findViewById(R.id.dropdown_fl_badge);
		
		badgeTypeListView = (ListView) view.findViewById(R.id.dropdown_listview_badge_type);
		
		badgeTypeNameList = new ArrayList<String>();
		badgeTypeNameList.add("全部");
		badgeTypeNameList.add("本人");
		
		badgeTypeListView.setAdapter(new ClassSelListViewAdapter(getContext(), badgeTypeNameList));
		
		badgeTypeDropdownFl = (FrameLayout) view.findViewById(R.id.dropdown_fl_badge_type);
		
		classView = (TextView) view.findViewById(R.id.student_class);
		timeList=new ArrayList<String>();
		timeList.add("本学期");
		timeList.add("本月");
		timeList.add("本周");
		timeList.add("今天");
		timeListView.setAdapter(new ClassSelListViewAdapter(getContext(), timeList));
		classText.setOnClickListener(this);
		badgeText.setOnClickListener(this);
		badgeTypeText.setOnClickListener(this);
		timeText.setOnClickListener(this);
		dropdownMask.setOnClickListener(this);
		badgeTypeText.setClickable(false);
		badgeTypeText.setTextColor(Color.GRAY);
		badgeText.setClickable(false);
		ListViewAnimationFactory.setListViewLayoutAnim(nameListView, getContext(), R.anim.listview_item_slid_right);
		
		classListView.setOnItemClickListener(new OnItemClickListener() {
			

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				badgeText.setText("全部");
//				badgeText.setTextColor(Color.GRAY);
//				badgeText.setClickable(false);
				badgeTypeText.setText("全部");
				isBadgeListViewShow=false;
//				badgeListView.setAdapter(new ClassSelListViewAdapter(getContext(), fullBadgeNameList));
				
				classText.setText(mClassList.get(position));
				classId = classGroups.get(position).getId();
				mTeacherUserId = 0;
				getStudentListData(classId,0,0,0,beginDate,endDate);
//				setClassBadgeText(mClassList.get(position),200,"",40);
				if( nameListView != null ){
					nameListView.setAdapter(null);
				}
				dropdownClick(0);
			}
		});
		badgeListView.setOnItemClickListener(new OnItemClickListener() {
			

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
//					for(Badge badge:badges){
//						badgeNameList.add(badge.name);
//					}
				if( mStudentBadgeCountBeanList == null || mStudentBadgeCountBeanList.size() == 0 ){
					Toast.makeText(StatisticsFragment.this.getContext(), "该班级未录入学生数据!", Toast.LENGTH_LONG).show();
					return;
				}
				if (position==0) {
					badgeText.setText("全部");
					getStudentListData(classId,badgeTypeId,0,mTeacherUserId,beginDate,endDate);
					
				}else {
					 badge = badges.get(position-1);
					 badgeText.setText(badge.name);
					 getStudentListData(classId,badgeTypeId,badge.id,mTeacherUserId,beginDate,endDate);
				}
				   
					
					
//					setClassBadgeText(classText.getText().toString(),200,badgeNameList.get(position)+"类",40);
				if( nameListView != null ){
					nameListView.setAdapter(null);
				}
				dropdownClick(1);
			}
		});
		badgeTypeListView.setOnItemClickListener(new OnItemClickListener() {
			

			

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if( mStudentBadgeCountBeanList == null || mStudentBadgeCountBeanList.size() == 0 ){
					Toast.makeText(StatisticsFragment.this.getContext(), "该班级未录入学生数据!", Toast.LENGTH_LONG).show();
					return;
				}
			    isbadgeTypeListOpen = true;
				
				badgeText.setText("全部");
				
				badgeTypeText.setText(badgeTypeNameList.get(position));
//				badgeTypeId = badgeTypeIdList.get(position);
				
				if ( position == 0 ) {
//					badgeText.setClickable(false);
//					badgeText.setTextColor(Color.GRAY);
					
					mTeacherUserId = 0;
				}else {
					badgeText.setClickable(true);
					
//					badgeType = flowerTypes.get(position-1);
//					badgeType.imgUrl=badgeType.badges.get(0).imgUrl;
//					badges = badgeType.badges;
//					badgeNameList = new ArrayList<String>();
//					badgeNameList.add("全部");
//					badgeText.setTextColor(Color.BLACK);
//					for(Badge badge:badges){
//						badgeNameList.add(badge.name);
//					}
//					badgeListView.setAdapter(new ClassSelListViewAdapter(getContext(), badgeNameList));
//					
					mTeacherUserId = InfoReleaseApplication.authenobjData.userId;
					
				}
				if( nameListView != null ){
					nameListView.setAdapter(null);
				}
				getStudentListData(classId,badgeTypeId,0,mTeacherUserId,beginDate,endDate);
				
//				setClassBadgeText(classText.getText().toString(),200,badgeTypeNameList.get(position)+"类",40);
				dropdownClick(2);
			}
		});
		timeListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				timeText.setText("本学期");
				timeText.setText(timeList.get(position));
				switch(position){
				case 0:
					beginDate=null;
					endDate=null;
					if(badge!=null){
						getStudentListData(classId,badgeTypeId,badge.id,mTeacherUserId,beginDate,endDate);
					}else{
						getStudentListData(classId,badgeTypeId,0,mTeacherUserId,beginDate,endDate);
					}
					
					break;
				case 1:
					beginDate=firstMounthDay;
					endDate=lastMounthDay;
					if(badge!=null){
						getStudentListData(classId,badgeTypeId,badge.id,mTeacherUserId,beginDate,endDate);
					}else{
						getStudentListData(classId,badgeTypeId,0,mTeacherUserId,beginDate,endDate);
					}
					break;
				case 2:
					beginDate=firstWeekDay;
					endDate=lastWeekDay;
					if(badge!=null){
						getStudentListData(classId,badgeTypeId,badge.id,mTeacherUserId,beginDate,endDate);
					}else{
						getStudentListData(classId,badgeTypeId,0,mTeacherUserId,beginDate,endDate);
					}
					break;
				case 3:
					beginDate=today;
					endDate=null;
					if(badge!=null){
						getStudentListData(classId,badgeTypeId,badge.id,mTeacherUserId,beginDate,endDate);
					}else{
						getStudentListData(classId,badgeTypeId,0,mTeacherUserId,beginDate,endDate);
					}
					break;
				default:
					break;
				}
				
					
				dropdownClick(3);
			}
		});
	}

	private void initData() {
		
		Date day=new Date();    
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
		today = df.format(day);
		Calendar cal = Calendar.getInstance();  
		getWeekDay(today,cal);
		getMounthDay(cal,df);
		getClassListData();	
	}
	private void getWeekDay(String day,Calendar cal){
		
	      
        try {
			cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(day));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
  
        int d = 0;  
        if(cal.get(Calendar.DAY_OF_WEEK)==1){  
            d = -6;  
        }else{  
            d = 2-cal.get(Calendar.DAY_OF_WEEK);  
        }  
        cal.add(Calendar.DAY_OF_WEEK, d);  
        firstWeekDay = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        cal.add(Calendar.DAY_OF_WEEK, 6);  
        lastWeekDay = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
	}
	private void getMounthDay(Calendar cal,SimpleDateFormat sdf){
		//获取当前一个月第一天
//		Calendar calendar1 = Calendar.getInstance();
		cal.add(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH,1);
        firstMounthDay = sdf.format(cal.getTime());
        //获取前一个月最后一天
//        Calendar calendar2 = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        lastMounthDay = sdf.format(cal.getTime());
	}
	private ProgressDialog progressDialog;

	private TextView classView;

	private String today=null;

	

	

	
	private void getStudentListData(final Integer groupId,final Integer badgeTypeId,final Integer badgeId,final int teacherUserId,final String beginDate,final String endDate) {//
		showMyProgressDialog();
			String urlString = SmartCampusUrlUtils.getStudentBadgeCountListURl()+"?groupId=" + groupId;
			if (badgeTypeId!=0) {
				urlString+="&badgeTypeId=" + badgeTypeId;
			}
			if (badgeId!=0) {
				urlString+="&badgeId=" + badgeId;
			}
			
			if (teacherUserId!=0) {
				urlString+="&teacherUserId=" + teacherUserId;
			}
			if(beginDate!=null){
				urlString+="&beginDate=" + beginDate;
			}
			if(endDate!=null){
				urlString+="&endDate=" + endDate;
			}
			Log.d(TAG,"getStudentListData");
			
			CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
					new Response.Listener<JSONObject>() {
						
						@Override
						public void onResponse(JSONObject response) {
							Log.d(TAG, "response=" + response);
							hideMyProgressDialog();
							int code = response.optInt("code");
							if (code == 0) {							
								mStudentBadgeCountBeanList = new ArrayList<StudentBean>();
								JSONArray array = response.optJSONArray("datas");
								if( array == null ){
									Toast.makeText(getActivity(), R.string.get_student_data_failed, Toast.LENGTH_LONG).show();
									return;
								}
								for (int i = 0; i < array.length(); i++) {
									JSONObject obj = (JSONObject) array.opt(i);
									StudentBean bean = new StudentBean(obj);
									if (badgeId!=0) {
										bean.subclassPoint=bean.badgeCount*badge.bonuspoint;
									}else {
										bean.subclassPoint=bean.bonuspoints;
									}
									//根据班级名称获取对应本地学生照片文件
									bean.imgSavePath = getStudentImgList(bean.sid, bean.grade, bean.staffGroup);
									mStudentBadgeCountBeanList.add(bean);
								}
								//根据积分排序
								ArrayList<StudentBean> taxisList=new ArrayList<StudentBean>();
								if (mStudentBadgeCountBeanList!=null) {
									taxisList=getTaxisList(mStudentBadgeCountBeanList);
								}else {
									return;
								}
								
								if (mStudentBadgeCountBeanList.size()>0) {
//									setClassBadgeText(mClassList.get(0),badgeCount,"",badgeCount/mStudentBadgeCountBeanList.size());
									if (badgeId!=0) {
										nameListView.setAdapter(new StudentNameListAdpter(getContext(), taxisList,badge));
									}else {
										nameListView.setAdapter(new StudentNameListAdpter(getContext(), taxisList));
									}
									badgeListView.setEnabled(true);
									badgeTypeText.setClickable(true);
									badgeTypeText.setTextColor(Color.BLACK);
								}else{
									badgeTypeText.setClickable(false);
									badgeTypeText.setTextColor(Color.GRAY);
									Toast.makeText(StatisticsFragment.this.getContext(), "该班级未录入学生数据!", Toast.LENGTH_LONG).show();	
								}
								
								

							} else if (code == -2) {
								InfoReleaseApplication.returnToLogin(getActivity());
							} else {
								Toast.makeText(getActivity(), response.optString("msg"), Toast.LENGTH_LONG).show();
							}

						}

						
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
							hideMyProgressDialog();
							//先判断网络状况
							if( true == InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) ){
								Toast.makeText(getActivity(), R.string.get_student_data_failed, Toast.LENGTH_LONG).show();
							}
							
						}
					});

			jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
			InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
	}

	
	private ArrayList<StudentBean> getTaxisList(ArrayList<StudentBean> badgeCountBeanList) {
		Collections.sort(badgeCountBeanList, new Comparator<StudentBean>() {
			@Override
			public int compare(StudentBean lhs, StudentBean rhs) {
				if (lhs.subclassPoint > rhs.subclassPoint) {
					return -1;
				}
				if (lhs.subclassPoint == rhs.subclassPoint) {
					return 0;
				}
				return 1;
			}
		});
		int taxisTag=0;
		for (int i = 0; i < badgeCountBeanList.size(); i++) {
			if (i==0) {
				badgeCountBeanList.get(i).ranking=i+1;
			}else if (badgeCountBeanList.get(i).subclassPoint==badgeCountBeanList.get(i-1).subclassPoint) {
				taxisTag+=1;
				badgeCountBeanList.get(i).ranking=badgeCountBeanList.get(i-1).ranking;
			} else {
				badgeCountBeanList.get(i).ranking=badgeCountBeanList.get(i-1).ranking+1+taxisTag;
				taxisTag=0;
			}
		}
		
		
		return badgeCountBeanList;
	}
	
	private void setClassBadgeText(String className, int totalNum, String badgeType, int averageNum) {
		String info = getResources().getString(R.string.class_badge_text);
		String infotext = String.format(info, className, totalNum,badgeType,averageNum,badgeType);
		int index[] = new int[5];
		index[0] = infotext.indexOf(className);
		String stSizeS = String.valueOf(totalNum);
		index[1] = infotext.indexOf(stSizeS);
		index[2] = infotext.indexOf(badgeType);
		String stSize = String.valueOf(averageNum);
		index[3] = infotext.indexOf(stSize);
		index[4] = infotext.indexOf(badgeType);
		
//		int markTextSize = getResources().getDimensionPixelSize(R.dimen.student_badge_mark_text_size);
		SpannableStringBuilder style=new SpannableStringBuilder(infotext);     	
		
        style.setSpan(new ForegroundColorSpan(Color.RED),index[0],index[0]+className.length(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);      
        style.setSpan(new ForegroundColorSpan(Color.RED),index[1],index[1]+stSizeS.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.RED),index[2],index[2]+badgeType.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        style.setSpan(new ForegroundColorSpan(Color.RED),index[3],index[3]+stSize.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.RED),index[4],index[4]+badgeType.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        
//        style.setSpan(new AbsoluteSizeSpan(markTextSize),0,index[0], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
//        style.setSpan(new AbsoluteSizeSpan(markTextSize),0,index[1],Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
        classBadgeText.setText(style);
	}

	private void dropdownClick(int type) {
		if (isListViewShow) {
			listViewInAnim(type);
		} else {
			listViewOutAnim(type);
		}
	}

	private void listViewInAnim(int type) {
		if (type == 0) {
			classListView.clearAnimation();
			classListView.setVisibility(View.VISIBLE);
			classListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
			classDropdownFl.setVisibility(View.VISIBLE);
			setListViewPosition(classDropdownFl,classView);
			isClassListShow = false;
		} else  if (type == 1) {
			badgeListView.clearAnimation();
			badgeListView.setVisibility(View.VISIBLE);
			badgeListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
			badgeDropdownFl.setVisibility(View.VISIBLE);
			setListViewPosition(badgeDropdownFl,classView);
			isBadgeListViewShow = false;
		}else  if (type == 2) {
			badgeTypeListView.clearAnimation();
			badgeTypeListView.setVisibility(View.VISIBLE);
			badgeTypeListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
			badgeTypeDropdownFl.setVisibility(View.VISIBLE);
			setListViewPosition(badgeTypeDropdownFl,classView);
			isBadgeTypeListViewShow = false;
		}else if(type==3){
			timeListView.clearAnimation();
			timeListView.setVisibility(View.VISIBLE);
			timeListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
			timeDropdownFl.setVisibility(View.VISIBLE);
			setListViewPosition(timeDropdownFl,classView);
			isTimeListShow = false;
		}
		

		dropdownMask.setVisibility(View.VISIBLE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_in));
		isListViewShow = false;

	}

	private void setListViewPosition(FrameLayout classDropdownView, TextView textView) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) classDropdownView.getLayoutParams();
		
		
		int[] location = new int[2];  
		textView.getLocationOnScreen(location);  
        int x = location[0];  
        int y = location[1];
        
        params.setMargins(textView.getWidth(), 0, 0, 0);
        
        classDropdownView.setLayoutParams(params);
		
	}

	private void listViewOutAnim(int type) {
		if (type == 0) {
			classListView.clearAnimation();
			classListView.setVisibility(View.GONE);
			classListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
			classDropdownFl.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
			classDropdownFl.setVisibility(View.GONE);
			isClassListShow = true;
		} else if (type == 1){
			badgeListView.clearAnimation();
			badgeListView.setVisibility(View.GONE);
			badgeListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
			badgeDropdownFl.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
			badgeDropdownFl.setVisibility(View.GONE);
			isBadgeListViewShow = true;
		}else if (type == 2){
			badgeTypeListView.clearAnimation();
			badgeTypeListView.setVisibility(View.GONE);
			badgeTypeListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
			badgeTypeDropdownFl.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
			badgeTypeDropdownFl.setVisibility(View.GONE);
			isBadgeTypeListViewShow = true;
		}else if(type==3){
			timeListView.clearAnimation();
			timeListView.setVisibility(View.GONE);
			timeListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
			timeDropdownFl.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
			timeDropdownFl.setVisibility(View.GONE);
			isTimeListShow = true;
		}

		dropdownMask.setVisibility(View.GONE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
		isListViewShow = true;

	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		if (hidden) {
			if (!isListViewShow) {
				if (!isClassListShow) {
					listViewOutAnim(0);
				} else if (!isBadgeTypeListViewShow) {
					listViewOutAnim(2);
				}else if(!isTimeListShow){
					listViewOutAnim(3);
				}else {
					listViewOutAnim(1);
				}

			}
		}else{
			if( classGroups == null || classGroups.size() == 0 ){
				initData();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dropdown_mask:
			if (!isListViewShow) {
				if (!isClassListShow) {
					listViewOutAnim(0);
				} else if (!isBadgeListViewShow){
					listViewOutAnim(1);
				}else if (!isBadgeTypeListViewShow){
					listViewOutAnim(2);
				}else if(!isTimeListShow){
					listViewOutAnim(3);
				}
			}
			break;
		case R.id.student_badge_edit:
			dropdownClick(1);
			break;
		case R.id.student_class_edit:
			dropdownClick(0);
			break;
		case R.id.student_badge_edit_type:
			dropdownClick(2);
			break;
		case R.id.time_type:
			dropdownClick(3);
			break;
		default:
			break;
		}
	}

	private void getClassListData() {
    	GroupListData.getClassListData(getOwnActivity(), new DataResponse.Listener<ArrayList<GroupInfo>>() {
			@Override
			public void onResponse(ArrayList<GroupInfo> response) {
				// TODO Auto-generated method stub
				classGroups = response;
				mClassList = new ArrayList<String>();
				for (int i = 0; i < classGroups.size(); i++) {
					mClassList.add(classGroups.get(i).getName());
				}
				if (mClassList!=null && mClassList.size()>0) {
					classText.setText(mClassList.get(0));
					classId = classGroups.get(0).getId();
					classListView.setAdapter(new ClassSelListViewAdapter(getContext(), mClassList));	
					badgeTypeText.setClickable(true);
					badgeTypeText.setTextColor(Color.BLACK);
					getBadgeListData();  
					//获取到有效的班级列表,不需要重新
				}
			}
		},new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
			}
		},new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
			}
		});  
	}	
	
	private void getBadgeListData(){
		showMyProgressDialog();
		BadgeListData.getBadgesListData(getOwnActivity(), new DataResponse.Listener<ArrayList<Badge>>() {

			@Override
			public void onResponse(ArrayList<Badge> flowersList) {
				// TODO Auto-generated method stub
//				flowerTypes = new ArrayList<BadgeType>();
				badges = flowersList;
				if (flowersList.size() > 0 ) {
					badgeText.setClickable(true);
					badgeText.setTextColor(Color.BLACK);
					badgeNameList = new ArrayList<String>();
					badgeNameList.add("全部");
					badgeText.setTextColor(Color.BLACK);
					for(Badge badge:flowersList){
						badgeNameList.add(badge.name);
					}
					badgeListView.setAdapter(new ClassSelListViewAdapter(getContext(), badgeNameList));
					
//					badgeTypeNameList = new ArrayList<String>();
//					badgeTypeNameList.add("全部");
//					badgeTypeIdList = new ArrayList<Integer>();
//					badgeTypeIdList.add(0);
//					flowerTypes = BadgeType.filterBadgeTypesFromBadges(flowersList);
//					for(BadgeType badgeType: flowerTypes){
//						badgeTypeIdList.add(badgeType.id);
//						badgeTypeNameList.add(badgeType.name);
//					}			
//					badgeTypeListView.setAdapter(new ClassSelListViewAdapter(getContext(), badgeTypeNameList));
					badgeText.setText("全部");
				}		
				getStudentListData(classGroups.get(0).getId(),0,0,mTeacherUserId,null,null);
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
	
	private String getStudentImgList(Integer studentId,String gradeName,String className) {
		// TODO Auto-generated method stub
		
		ArrayList<String> imagePathList=new ArrayList<String>();
		String imgPath = FaceRecognizeMgr.getImageDir(gradeName, className);
		File imgFile = new File(imgPath);
		File[] files = imgFile.listFiles();
		if (files!=null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];  
	            if (checkIsImageFile(file.getPath())) {  
	                imagePathList.add(file.getPath());  
	            }
			}
		}else{
			return null;
		}
		
		//截取图片路径中的学生id
//		ArrayList<Integer> studentIdList=new ArrayList<Integer>();
		String imgFilePath = null;
		for (int i = 0; i < imagePathList.size(); i++) {
			String str=imagePathList.get(i);
			int id=Integer.valueOf(str.substring(str.lastIndexOf("/")+1, str.lastIndexOf("_")));
			if (id==studentId) {
				imgFilePath=imagePathList.get(i);
			}
//			studentIdList.add(Integer.valueOf(str.substring(str.lastIndexOf("/")+1, str.lastIndexOf("_"))));
		}
		return imgFilePath;
		
		
	}

	
	private boolean checkIsImageFile(String fName) {  
        boolean isImageFile = false;  
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,  
                fName.length()).toLowerCase();  
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")  
                || FileEnd.equals("jpeg")|| FileEnd.equals("bmp") ) {  
            isImageFile = true;  
        } else {  
            isImageFile = false;  
        }  
        return isImageFile;  
    }  
	
	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(getActivity(), "",
					"...loading...");
		}
	}

	private void hideMyProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	

}
