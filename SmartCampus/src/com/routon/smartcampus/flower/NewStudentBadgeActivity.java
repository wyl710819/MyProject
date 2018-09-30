package com.routon.smartcampus.flower;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.pictureAdd.PictureAddActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.widget.PopupList;
import com.routon.edurelease.R;
import com.routon.smartcampus.SmartCampusApplication;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.gradetrack.HistogramRevealActivity;
import com.routon.smartcampus.gradetrack.StudentGrade;
import com.routon.smartcampus.gradetrack.ZigzagRevealActivity;
import com.routon.smartcampus.guideview.MyMaskView;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.StudentListFragment;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.TakePhotoPopWin;
import com.squareup.picasso.Picasso;

//新版的学生的小红花显示界面，兼容教师版本学生小红花显示界面（可撤销，可备注）和家长版本的小红花显示界面（不可撤销，不可备注）
public class NewStudentBadgeActivity extends CustomTitleActivity {

	private final String TAG = "NewStudentBadgeActivity";

	private ListView mListView;
	private TextView studentInfoView1;
	private TextView studentInfoView2;
	private TextView studentInfoView3;

	// 积分数目
	private int mBonusPoints = 0;
	private String studentName;
	private int totalPoints = 0;

	private ArrayList<StudentBadge> studentBadges = new ArrayList<StudentBadge>();
	private ArrayList<STBadgeCategroy> stCategorys = new ArrayList<STBadgeCategroy>();
	int studentId;

	private Context mContext;

	private ArrayList<Badge> flowersList=new ArrayList<Badge>();

	private String groupId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_student_badge);

		mContext = this;
		Bundle bundle = getIntent().getExtras();
		userId = bundle.getInt(MyBundleName.USER_ID);
		studentName = bundle.getString(MyBundleName.STUDENT_NAME);
		studentId = bundle.getInt(MyBundleName.STUDENT_ID);
		String studentImgUrl = bundle.getString(MyBundleName.STUDENT_IMG_URL);
		mBonusPoints = bundle.getInt(MyBundleName.STUDENT_BONUS_POINTS);
		totalPoints = bundle.getInt("TOTALCOUNT");
		studentBean = (StudentBean) bundle.getSerializable(MyBundleName.STUDENT_BEAN);
		groupId = bundle.getString("group_id");
		if (groupId==null) {
			groupId=String.valueOf(studentBean.groupId);
		}
		
		
		if (studentId == 0) {
			studentId = studentBean.sid;
		}
		if (studentImgUrl == null || studentImgUrl.isEmpty()) {
			studentImgUrl = studentBean.imgUrl;
		}
		if (studentName == null || studentName.isEmpty()) {
			studentName = studentBean.empName;
		}
		if (mBonusPoints == 0) {
			mBonusPoints = studentBean.availableBonusPoints;
		}
		if (totalPoints == 0) {
			totalPoints = studentBean.bonuspoints;
		}

		// Log.i(TAG,
		// "studentImgUrl:"+studentImgUrl+",id:"+studentBean.sid+",userId:"+userId);
		if (SmartCampusApplication.mFamilyVersion == false) {
			initTitleBar(studentName + "的小红花");
		} else {
			initTitleBar("小红花");
		}
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));

		mListView = (ListView) findViewById(R.id.list);

		View v = LayoutInflater.from(this).inflate(R.layout.student_info_layout, null, true);
		mListView.addHeaderView(v);

		popView = getLayoutInflater().inflate(R.layout.badge_count_pop_layout, null);
		mPopupWindow = new PopupWindow(popView, dip2px(140), RelativeLayout.LayoutParams.WRAP_CONTENT);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

		studentInfoView1 = (TextView) v.findViewById(R.id.student_info1);
		studentInfoView2 = (TextView) v.findViewById(R.id.student_info2);
		studentInfoView3 = (TextView) v.findViewById(R.id.student_info3);
		ImageView imageview = (ImageView) v.findViewById(R.id.image);
		if (studentImgUrl != null && studentImgUrl.startsWith("/")) {
			Bitmap bitmap = BitmapFactory.decodeFile(studentImgUrl);
			if (bitmap != null) {
				imageview.setImageBitmap(bitmap);
			}
		} else if (studentImgUrl != null && studentImgUrl.startsWith("http")) {
			Picasso.with(this).load(studentImgUrl).placeholder(R.drawable.default_student).fit().into(imageview);
		} else {
			imageview.setImageResource(R.drawable.default_student);
		}

		if (SmartCampusApplication.mFamilyVersion == false) {
			flowersList = BadgeInfoUtil.getFlowerList();
			getStudentBadgeInfo(studentId);
		}else {//家长版需要获取小红花列表
			getFlowersListData();
		}
		

		if (SmartCampusApplication.mFamilyVersion == false) {// 家长版不需要兑奖按钮

			setTitleNextImageBtnClickListener(R.drawable.badge_query_icon, new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mPopupWindow.isShowing()) {
						return;
					} else {
						showPopupMenu(popView, v);
					}

				}
			});

		} else {
			setTitleNextImageBtnClickListener(R.drawable.hornorroll_btn, new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					if (mPopupWindow.isShowing()) {
						return;
					} else {
						showPopupMenu(popView, v);
					}
					
					
				}
			});
		}
	}

	

	ProgressDialog progressDialog;

	private void getStudentBadgeInfo(int studentId) {
		String urlString = SmartCampusUrlUtils.getStudentBadgeDetailListUrl(studentId);
		if (SmartCampusApplication.mFamilyVersion == true) {
			urlString = SmartCampusUrlUtils.getStudentBadgeDetailListCmdUrl(studentId);
		}
		Log.d(TAG, "urlString=" + urlString);
		showLoadDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						
						int code = response.optInt("code");

						if (code == 0) {
							studentBadges.clear();
							JSONArray array = response.optJSONArray("datas");
							if (array != null) {
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.opt(i);
									if (obj != null) {
										StudentBadge studentB = new StudentBadge(obj);
										studentBadges.add(studentB);
									}
								}

								updateView();
							}
							
							getSessionClassBadgeData();

						} else if (code == -2) {
							hideLoadDialog();
							InfoReleaseApplication.returnToLogin(NewStudentBadgeActivity.this);
						} else {// 失败
							hideLoadDialog();
							Log.e(TAG, response.optString("msg"));
							Toast.makeText(NewStudentBadgeActivity.this, response.optString("msg"), Toast.LENGTH_LONG)
									.show();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						if (InfoReleaseApplication.showNetWorkFailed(NewStudentBadgeActivity.this) == true) {
							reportToast("获取徽章数据失败");
						}
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private List<BadgeAvgBean> badgeAvgBeans=new ArrayList<BadgeAvgBean>();
	private List<BadgeAvgBean> studentBadgeBeans=new ArrayList<BadgeAvgBean>();
	//本学期班级颁发的各类小红花平均数
	private void getSessionClassBadgeData() {
		String urlString = SmartCampusUrlUtils.getSessionClassBadgeUrl(groupId,0);
		Log.d(TAG, "urlString=" + urlString);
		showLoadDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						int code = response.optInt("code");

						if (code == 0) {//badges
							
							badgeAvgBeans = new ArrayList<BadgeAvgBean>();
							JSONObject jsonObject = response.optJSONObject("datas");
							JSONArray array = jsonObject.optJSONArray("badges");
							if (array != null) {
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.opt(i);
									BadgeAvgBean badgeAvgBean=new BadgeAvgBean(obj,"is_avg");
									badgeAvgBeans.add(badgeAvgBean);
								}
							}
							getStudentDetailBadgeData();
							
						} else if (code == -2) {
							hideLoadDialog();
							InfoReleaseApplication.returnToLogin(NewStudentBadgeActivity.this);
						} else {// 失败
							hideLoadDialog();
							Log.e(TAG, response.optString("msg"));
							Toast.makeText(NewStudentBadgeActivity.this, response.optString("msg"), Toast.LENGTH_LONG)
									.show();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
	}

	// 本学期每周学生获取的各类小红花数
	private void getStudentDetailBadgeData() {
		String urlString = SmartCampusUrlUtils.getWeeklyStudentBadgeUrl(studentId,0);
		Log.d(TAG, "urlString=" + urlString);
		showLoadDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
//						hideLoadDialog();
						int code = response.optInt("code");

						if (code == 0) {//badges
							
							studentBadgeBeans = new ArrayList<BadgeAvgBean>();
							JSONObject jsonObject = response.optJSONObject("datas");
							JSONArray array = jsonObject.optJSONArray("badges");
							if (array != null) {
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.opt(i);
									BadgeAvgBean badgeAvgBean=new BadgeAvgBean(obj,"is_student");
									
									studentBadgeBeans.add(badgeAvgBean);
								}
							}
							getWeeklyClassBadgeScore();
//							setRadoData();
							
						} else if (code == -2) {
							hideLoadDialog();
							InfoReleaseApplication.returnToLogin(NewStudentBadgeActivity.this);
						} else {// 失败
							hideLoadDialog();
							Log.e(TAG, response.optString("msg"));
							Toast.makeText(NewStudentBadgeActivity.this, response.optString("msg"), Toast.LENGTH_LONG)
									.show();
						}
					}

					
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
	}
	private List<Integer> studentWeeklyScoreDatas=new ArrayList<Integer>();//学生周平均分数据
	private List<Integer> classWeeklyScoreDatas=new ArrayList<Integer>();//班级周平均分数据
	// 本学期每周班级人均积分
	private void getWeeklyClassBadgeScore() {
		String urlString = SmartCampusUrlUtils.getWeeklyClassBadgeScoreUrl(groupId);
		Log.d(TAG, "urlString=" + urlString);
		showLoadDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						int code = response.optInt("code");

						if (code == 0) {//badges
							
							classWeeklyScoreDatas = new ArrayList<Integer>();
							try {
							JSONObject jsonObject = response.optJSONObject("datas");
							JSONArray array = jsonObject.optJSONArray("avg_bonuspoints");
							if (array != null) {
								if(array!=null && array.length()>0){
									for (int i = 0; i < array.length(); i++) {
										
										classWeeklyScoreDatas.add(Integer.parseInt(new DecimalFormat("0").format(array.get(i))));
										
									}
								}
							}
							} catch (JSONException e) {
								e.printStackTrace();
							}
							
							
							getWeeklyStudentBadgeScore();
							
						} else if (code == -2) {
							hideLoadDialog();
							InfoReleaseApplication.returnToLogin(NewStudentBadgeActivity.this);
						} else {// 失败
							hideLoadDialog();
							Log.e(TAG, response.optString("msg"));
							Toast.makeText(NewStudentBadgeActivity.this, response.optString("msg"), Toast.LENGTH_LONG)
									.show();
						}
					}

					
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}



	// 本学期每周学生获取的积分数
	private void getWeeklyStudentBadgeScore() {
		// TODO Auto-generated method stub
		String urlString = SmartCampusUrlUtils.getWeeklyStudentBadgeScoreUrl(studentId);
		Log.d(TAG, "urlString=" + urlString);
		showLoadDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideLoadDialog();
						int code = response.optInt("code");

						if (code == 0) {//badges
							studentWeeklyScoreDatas = new ArrayList<Integer>();
							try {
							JSONObject jsonObject = response.optJSONObject("datas");
							JSONArray array = jsonObject.optJSONArray("bonuspoints");
							if (array != null) {
								if(array!=null && array.length()>0){
									for (int i = 0; i < array.length(); i++) {
										
										studentWeeklyScoreDatas.add(Integer.parseInt(new DecimalFormat("0").format(array.get(i))));
										
									}
								}
							}
							} catch (JSONException e) {
								e.printStackTrace();
							}
							setRadoData();
							setZigzagData();
							
						} else if (code == -2) {
							hideLoadDialog();
							InfoReleaseApplication.returnToLogin(NewStudentBadgeActivity.this);
						} else {// 失败
							hideLoadDialog();
							Log.e(TAG, response.optString("msg"));
							Toast.makeText(NewStudentBadgeActivity.this, response.optString("msg"), Toast.LENGTH_LONG)
									.show();
						}
					}

					
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	



	NewStudentBadgeListAdapter adapter;
	private StudentBean studentBean;

	private void updateView() {

		// for test
		// for(int i = 0; i< studentBadges.size(); i++){
		//
		// StudentBadge studentB = studentBadges.get(i);
		// studentB.teacherName = "朱老师";
		// studentB.badgeRemarkBean = new BadgeRemarkBean();
		// if( i == 0 ){
		// studentB.badgeRemarkBean.badgeRemark =
		// "能发现自己学习上的不足，想办法弥补，改进学习方法，克服学习上的困难";
		//
		// }else if( i == 1 ){
		// studentB.badgeRemarkBean.badgeRemark =
		// "在文艺方面有一或多项特长，在艺术活动中表现优异，获得大家的认可";
		// studentB.badgeRemarkBean.imgList = new String[2];
		// studentB.badgeRemarkBean.imgList[0] = "/sdcard/1.jpg";
		// studentB.badgeRemarkBean.imgList[1] = "/sdcard/2.jpg";
		// }else if( i == 2 ){
		// studentB.badgeRemarkBean.badgeRemark = "学习有计划，主动预习，主动复习";
		// }
		// }

		// //data------------
		// for (int i = 0; i< 60; i++){
		// StudentBadge stB = new StudentBadge();
		// stB.id = 5;
		// stB.createTime = "2017-07-06 12:45:00";
		// stB.exchangeId = i%2;
		// stB.status = 0;
		// stB.teacherName = "王老师";
		// stB.teacherId = (int) (Math.random()%3);
		//
		// Badge badge = new Badge();
		// badge.id = i%6;
		// badge.name = "小红花"+i%6;
		// badge.type = i%5;
		// badge.typeName = "type"+i%5;
		// badge.imgUrl = "http://123";
		// stB.badge = badge;
		//
		// studentBadges.add(stB);
		// }
		//
		// //end data-------

		// filterBadgesFromStudentBadges();

		String info1 = getResources().getString(R.string.student_tip1);
		String infotext1 = String.format(info1, studentName, studentBadges.size());
		int index[] = new int[2];
		index[0] = infotext1.indexOf(studentName);
		String stSizeS = String.valueOf(studentBadges.size());
		index[1] = infotext1.indexOf(stSizeS);

		int markTextSize = getResources().getDimensionPixelSize(R.dimen.student_badge_mark_text_size);
		SpannableStringBuilder style = new SpannableStringBuilder(infotext1);
		style.setSpan(new ForegroundColorSpan(Color.RED), index[0], index[0] + studentName.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		style.setSpan(new ForegroundColorSpan(Color.RED), index[1], index[1] + stSizeS.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		style.setSpan(new AbsoluteSizeSpan(markTextSize), 0, index[0], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		style.setSpan(new AbsoluteSizeSpan(markTextSize), index[1], index[1] + stSizeS.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置字体大小
		// studentInfoView1.setText(infotext1);
		studentInfoView1.setText(style);

		String info2 = getResources().getString(R.string.student_tip2);
		String info3 = getResources().getString(R.string.student_tip3);
		String infoText2 = String.format(info2, mBonusPoints);
		String infoText3 = String.format(info3, totalPoints);
		String markStr = String.valueOf(mBonusPoints);
		String markStr2 = String.valueOf(totalPoints);
		SpannableStringBuilder style2 = new SpannableStringBuilder(infoText2);
		int markIndex = infoText2.indexOf(markStr);
		style2.setSpan(new ForegroundColorSpan(Color.rgb(82, 224, 20)), markIndex, markIndex + markStr.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		style2.setSpan(new AbsoluteSizeSpan(markTextSize), markIndex, markIndex + markStr.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		SpannableStringBuilder style3 = new SpannableStringBuilder(infoText3);
		int markIndex2 = infoText3.indexOf(markStr2);
		style3.setSpan(new ForegroundColorSpan(Color.rgb(82, 224, 20)), markIndex2, markIndex2 + markStr2.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		style3.setSpan(new AbsoluteSizeSpan(markTextSize), markIndex2, markIndex2 + markStr2.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		studentInfoView2.setText(style2);
		studentInfoView3.setText(style3);
		initListView();

		// int length = stCategorys.size();
		// for(int i = 0 ; i < length; i++){
		// badges.add(stCategorys.get(i).badge);
		// }
		// DisplayMetrics dm = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(dm);

		// final int screenWidth = dm.widthPixels;
		// final int screenHeight = dm.heightPixels;
		// final int titleBarHeight = this.getTitleBarHeight();
		// View v = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
		// final int contentTop = v.getTop();

		// //视图的高度
		// View v = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
		// int height = v.getHeight();
		// int top = v.getTop();

		// badgeGridView.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// final int position, long id) {
		// if( mFamilyVersion == true ){//家长版不需要撤销功能
		// return;
		// }
		// Log.d(TAG,"onItemClick position:"+position);
		// if( position >= 3 ){
		// int[] location = new int[2];
		// view.getLocationOnScreen(location);
		//// Log.d(TAG,"onItemClick viewHeight:"+viewHeight
		//// +",view.getHeight():"+view.getHeight()+",location[1]:"+location[1]+",contentTop:"+contentTop);
		// if ( location[1] + view.getHeight()/4 > contentTop && location[1] +
		// view.getHeight() / 2 < screenHeight ) {
		// if (studentBadges.get(position-3).exchangeId==0) {
		// setPopupListAndHightLight(view,position-3);
		// }
		//
		// }
		// }
		// return;
		// }
		//
		// });

	}

	private NewStudentBadgeListAdapter mAdapter = null;
	private TakePhotoPopWin takePhotoPopWin;
	private LayoutParams params;
	private ArrayList<FlowerBean> mDataList;
	private int mAddPosition = 0;

	private void initListView() {
		
		mAdapter = new NewStudentBadgeListAdapter(mContext, studentBadges);
		mAdapter.setEditable(!SmartCampusApplication.mFamilyVersion);
		mListView.setAdapter(mAdapter);

		mAdapter.setOnClickListener(new NewStudentBadgeListAdapter.onClickListener() {

			@Override
			public void onClick(int position, int type) {
				mAddPosition = position;
				if (type == NewStudentBadgeListAdapter.onClickListener.ADD_TYPE) {
					Intent intent = new Intent();
					intent.setClass(NewStudentBadgeActivity.this, BadgeRemarkActivity.class);
					NewStudentBadgeActivity.this.startActivityForResult(intent, BADGE_REMARK_REQUEST_CODE);
					// takePhotoPopWin = new
					// TakePhotoPopWin(NewStudentBadgeActivity.this,
					// mPopWinListener,null);
					//// 设置Popupwindow显示位置（从底部弹出）
					// takePhotoPopWin.showAtLocation(NewStudentBadgeActivity.this.findViewById(R.id.main),
					// Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
					// params =
					// NewStudentBadgeActivity.this.getWindow().getAttributes();
					// //当弹出Popupwindow时，背景变半透明
					// params.alpha=0.7f;
					// NewStudentBadgeActivity.this.getWindow().setAttributes(params);
					// //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
					// takePhotoPopWin.setOnDismissListener(new
					// PopupWindow.OnDismissListener() {
					// @Override
					// public void onDismiss() {
					// params =
					// NewStudentBadgeActivity.this.getWindow().getAttributes();
					// params.alpha = 1f;
					// NewStudentBadgeActivity.this.getWindow().setAttributes(params);
					// }
					// });
				} else if (type == NewStudentBadgeListAdapter.onClickListener.CANCEL_TYPE) {
					showCancelDialog(position);
				}
			}
		});
	}

	public static int STUDENT_BADGE_DETAIL_REQUEST_CODE = 1;
	public static int BADGE_REMARK_REQUEST_CODE = 7;
	public static final int PICTUER_ADD_REQUEST_CODE = 11;
	public static final int REMARK_IMAGE_PREVIEW_REQUEST_CODE = 12;

	private PopOnClickListener mPopWinListener = new PopOnClickListener() {

		@Override
		public void itemClick(int position) {
			Intent intent = new Intent(NewStudentBadgeActivity.this, RemarkImagePreviewActivity.class);
			intent.putExtra("position", position);
			intent.putStringArrayListExtra("img_list", takePhotoPopWin.imgList);
			NewStudentBadgeActivity.this.startActivityForResult(intent, REMARK_IMAGE_PREVIEW_REQUEST_CODE);
		}

		@Override
		public void lastItemtemClick() {
			Intent intent = new Intent(NewStudentBadgeActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(com.routon.inforelease.util.CommonBundleName.FILE_TYPE_TAG, 14);
			NewStudentBadgeActivity.this.startActivityForResult(intent, PICTUER_ADD_REQUEST_CODE);
		}

		@Override
		public void addImgClick(int position) {
			Intent intent = new Intent(NewStudentBadgeActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(com.routon.inforelease.util.CommonBundleName.FILE_TYPE_TAG, 14);
			NewStudentBadgeActivity.this.startActivityForResult(intent, PICTUER_ADD_REQUEST_CODE);
		}

		@Override
		public void awardClick() {
			takePhotoPopWin.dismiss();
		}

		@Override
		public void saveRemark(View v) {

		}

	};

	private void showCancelDialog(final int position) {
		new AlertDialog.Builder(this).setTitle("是否撤消当前小红花！").setNegativeButton("取消", null)
				.setPositiveButton("撤消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mDataList.remove(position);
						mAdapter.notifyDataSetChanged();
					}
				}).show();
	}

	private void remove(final int position) {
		StudentBadge stB = studentBadges.get(position);
		if (stB.exchangeId != 0) {// 已兑奖，不能撤消
			Toast.makeText(mContext, "该小红花已兑奖，不能撤消！", 2000).show();
			return;
		}

		if (userId != stB.teacherId) {
			Toast.makeText(mContext, "该小红花是" + stB.teacherName + "颁发，您无权撤消！", 2000).show();
			return;
		}

		new AlertDialog.Builder(NewStudentBadgeActivity.this).setTitle("是否撤消当前小红花！").setNegativeButton("取消", null)
				.setPositiveButton("撤消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						retractBadge(studentId, studentBadges.get(position).id, position);
					}
				}).show();
	}

	private MyMaskView showHightLight(View view) {
		MyMaskView maskView = new MyMaskView(this);
		maskView.setTargetView(view);
		maskView.setAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
		maskView.show(this);
		return maskView;
	}

	void setPopupListAndHightLight(View view, int position) {
		final MyMaskView maskView = showHightLight(view);
		PopupList poplist = showPopupList(view, position, new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				Log.d("studentListFragment", "onDismiss");
				maskView.dismiss();
			}
		});

	}

	private List<String> popupMenuItemList = new ArrayList<String>();
	private int userId;

	private View popView;

	private PopupList showPopupList(View anchorView, int contextPosition, PopupWindow.OnDismissListener listener) {

		if (popupMenuItemList.size() == 0) {
			popupMenuItemList.add("撤销");
		}

		int[] location = new int[2];
		anchorView.getLocationOnScreen(location);
		final float viewWidth = anchorView.getWidth() / 2;
		final float x = location[0] + anchorView.getWidth() / 2;
		final float y = location[1] + anchorView.getHeight() / 3;
		PopupList popupList = new PopupList(this);
		popupList.setOnDismissListener(listener);
		Log.d(TAG, "showPopupList contextPosition:" + contextPosition);
		popupList.showPopupListWindow(anchorView, contextPosition, x, y, popupMenuItemList,
				new PopupList.PopupListListener() {
					@Override
					public void onPopupListClick(View contextView, int contextPosition, int position) {
						switch (position) {

						case 0:// 撤销
							remove(contextPosition);
							break;
						}
					}

					@Override
					public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
						return true;
					}
				});
		return popupList;
	}

	private void getFlowersListData() {
		String urlString = SmartCampusUrlUtils.getBadgeListUrl();
		showLoadDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						
						try {
							if (response.getInt("code") == 0) {
								ArrayList<Badge> flowers = new ArrayList<Badge>();
								JSONArray array = response.getJSONArray("datas");
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.get(i);
									Badge flower = new Badge(obj);
									flowers.add(flower);
								}
								flowersList = flowers;
								BadgeInfoUtil.setFlowerList(flowers);					
								getStudentBadgeInfo(studentId);
								
							} else if (response.getInt("code") == -2) {
								hideLoadDialog();
								InfoReleaseApplication.returnToLogin(NewStudentBadgeActivity.this);
							} else {// 失败
								hideLoadDialog();
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(NewStudentBadgeActivity.this, response.getString("msg"),
										Toast.LENGTH_LONG).show();

							}

						} catch (JSONException e) {
							hideLoadDialog();
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(NewStudentBadgeActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
	}
	
	
	
	private void retractBadge(final int studentId, int badgeId, final int position) {
		String urlString = SmartCampusUrlUtils.getUndoBadgeUrl(badgeId);
        showLoadDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						
						try {
							if (response.getInt("code") == 0) {
								studentBadges.clear();
								getStudentBadgeInfo(studentId);
								Intent intent = new Intent(StudentListFragment.ACTION_STUDENT_BADGE_RETRACT);
								NewStudentBadgeActivity.this.sendBroadcast(intent);
							} else if (response.getInt("code") == -2) {
								hideLoadDialog();
								InfoReleaseApplication.returnToLogin(NewStudentBadgeActivity.this);
							} else {// 失败
								hideLoadDialog();
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(NewStudentBadgeActivity.this, response.getString("msg"),
										Toast.LENGTH_LONG).show();

							}

						} catch (JSONException e) {
							hideLoadDialog();
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(NewStudentBadgeActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void showPopupMenu(View layout, View v) {
		mPopupWindow.showAsDropDown(v);

		layout.findViewById(R.id.pop_rado_item).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {// 图表
				startChartActivity("rado_chart");
				mPopupWindow.dismiss();
			}
		});
		layout.findViewById(R.id.pop_zigzag_item).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startChartActivity("zigzag_chart");
				mPopupWindow.dismiss();
			}
		});
		layout.findViewById(R.id.pop_histogram_item).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startChartActivity("histogram_chart");
				mPopupWindow.dismiss();
			}
		});
		layout.findViewById(R.id.pop_count_item).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startChartActivity("count_chart");
				mPopupWindow.dismiss();
			}
		});

	}
	
	String countTagName="";
	/*
	 * 设置雷达图数据
	 * */
	private void setRadoData() {
		
		//测试数据
		/*flowersList=SmartCampusApplication.flowersList;
		Badge badge=new Badge();
		badge.name="";
		flowersList.add(badge);
		for (int i = 0; i < flowersList.size(); i++) {
			allFlowers.add(flowersList.get(i).name);
			studentFlowers.add((i+1)*5);
			averageFlowers.add((i+1)*7);
		}
		*/
		
		// TODO Auto-generated method stub
		//平台数据
		Badge badge=new Badge();
		badge.name="";
		badge.id=0;
		flowersList.add(badge);
		
		countTag = 0;
		
		for (int i = 0; i < flowersList.size(); i++) {
			if (flowersList.get(i).prop==1) {
				propTags.add(i);
			}
			
			allFlowers.add(flowersList.get(i).name);
			studentFlowers.add(0);
			averageFlowers.add(0);
		}
		
		List<BadgeAvgBean> studentBadgesTag=new ArrayList<BadgeAvgBean>();
		for (int i = 0; i < flowersList.size()-1; i++) {
			BadgeAvgBean bean=new BadgeAvgBean();
			int count=0;
			for (int j = 0; j < studentBadges.size(); j++) {
				if (flowersList.get(i).id==studentBadges.get(j).badge.id) {
					count+=1;
				}
			}
			
			bean.badgeId=flowersList.get(i).id;
			bean.studentBadgeCount=count;
			bean.badgeName=flowersList.get(i).name;
			
			studentBadgesTag.add(bean);
		}
		
		
		
		for (int i = 0; i < flowersList.size(); i++) {
			for (int j = 0; j < badgeAvgBeans.size(); j++) {
				if (flowersList.get(i).id==badgeAvgBeans.get(j).badgeId) {
					averageFlowers.set(i, (int)badgeAvgBeans.get(j).avg_badgeCounts);
					if (countTag < (int)badgeAvgBeans.get(j).avg_badgeCounts) {
						countTagName=flowersList.get(i).name;
					}
					countTag=countTag < (int)badgeAvgBeans.get(j).avg_badgeCounts ? (int)badgeAvgBeans.get(j).avg_badgeCounts : countTag;
					
				}
			}
			
			
			
			
			for (int j = 0; j < studentBadgesTag.size(); j++) {
                if (flowersList.get(i).id==studentBadgesTag.get(j).badgeId) {
                	
                	studentFlowers.set(i, (int)studentBadgesTag.get(j).studentBadgeCount);
                	if (countTag < (int)studentBadgesTag.get(j).studentBadgeCount) {
						countTagName=flowersList.get(i).name;
					}
                	countTag=countTag < (int)studentBadgesTag.get(j).studentBadgeCount ? (int)studentBadgesTag.get(j).studentBadgeCount : countTag;
                	
				}
				
			}
			
			
		}
		
		
		
		flowersList.remove(flowersList.size()-1);
		
	}
	private ArrayList<Integer> propTags=new ArrayList<>();
	
	private ArrayList<String> allZigzagFlowers = new ArrayList<String>();
	private void setZigzagData() {
		allZigzagFlowers.clear();
		weeklyList.clear();
		
				for (int i = 0; i < flowersList.size(); i++) {
					allZigzagFlowers.add(flowersList.get(i).name);
					
					for (int j = 0; j < studentBadgeBeans.size(); j++) {
						if (studentBadgeBeans.get(j).badgeId==flowersList.get(i).id) {
							studentBadgeBeans.get(j).badgeName=flowersList.get(i).name;
						}
					}
					
					
				}
				
				allZigzagFlowers.add("班级积分");
				allZigzagFlowers.add("学生积分");
				
				
				for (int i = 0; i < studentBadgeBeans.size(); i++) {
					
					List<Integer> integers=studentBadgeBeans.get(i).studentBadgeWeeklyCounts;
					
					for (int j = 0; j < integers.size(); j++) {
						
						studentAllGrades.add(new StudentGrade(integers.get(j), j+1+"", studentBadgeBeans.get(i).badgeName));
					}
				}
				
				
				
				for (int i = 0; i < classWeeklyScoreDatas.size(); i++) {
					
					weeklyList.add(i+1+"");
					studentAllGrades.add(new StudentGrade(classWeeklyScoreDatas.get(i), i+1+"", "班级积分"));
                    if (studentWeeklyScoreDatas.size()-1>=i) {
                    	studentAllGrades.add(new StudentGrade(studentWeeklyScoreDatas.get(i), i+1+"", "学生积分"));
					}
				}
				
				
				
		
	}
	
	private ArrayList<StudentGrade> studentAllGrades = new ArrayList<StudentGrade>();
	
	private ArrayList<String> allFlowers = new ArrayList<String>();//徽章种类
	private ArrayList<Integer> studentFlowers = new ArrayList<Integer>();//当前学生本学期获得小红花数据
	private ArrayList<Integer> averageFlowers = new ArrayList<Integer>();//当前班级本学期获得小红花数据
	
	private ArrayList<String> weeklyList = new ArrayList<String>();//周

	private void startChartActivity(String type) {// 跳转图表界面
		
		if (type.equals("rado_chart")) {//雷达图
			Intent radoIntent = new Intent(NewStudentBadgeActivity.this, BadgeRadarRevealActivity.class);
			radoIntent.putStringArrayListExtra("examAllCourses", allFlowers);
			radoIntent.putIntegerArrayListExtra("studentGrades", studentFlowers);
			radoIntent.putIntegerArrayListExtra("averageGrades", averageFlowers);
			radoIntent.putExtra("picTitle", "小红花雷达图");
			radoIntent.putExtra(MyBundleName.STUDENT_BEAN, studentBean.empName);
			radoIntent.putExtra("is_badge", true);
			radoIntent.putExtra("max_count", countTag);
			radoIntent.putExtra("max_count_index", allFlowers.lastIndexOf(countTagName));
			radoIntent.putIntegerArrayListExtra("prop_tag", propTags);
			Log.e("countTag", "=="+countTag);
			startActivity(radoIntent);
		}else if (type.equals("zigzag_chart")) {//折线图
			
			Intent zigIntent = new Intent(NewStudentBadgeActivity.this,ZigzagRevealActivity.class);
			zigIntent.putStringArrayListExtra("examAllCourses", allZigzagFlowers);//小红花种类
			zigIntent.putStringArrayListExtra("examTimes", weeklyList);//时间段
			zigIntent.putParcelableArrayListExtra("studentAllGrades", studentAllGrades);//全部分数
			zigIntent.putExtra("examType", 1);
			zigIntent.putExtra("picTitle", "小红花折线图");
			zigIntent.putExtra(MyBundleName.STUDENT_BEAN,studentBean.empName);
			zigIntent.putExtra("is_badge", true);
			startActivity(zigIntent);
			
			
		}else if (type.equals("histogram_chart")) {//柱状图
			Intent hisIntent = new Intent(NewStudentBadgeActivity.this,HistogramRevealActivity.class);
			hisIntent.putStringArrayListExtra("examAllCourses", allFlowers);
			hisIntent.putIntegerArrayListExtra("studentGrades", studentFlowers);
			hisIntent.putIntegerArrayListExtra("averageGrades", averageFlowers);
			hisIntent.putExtra("picTitle", "小红花柱状图");
			hisIntent.putExtra(MyBundleName.STUDENT_BEAN, studentBean.empName);
			hisIntent.putExtra("is_badge", true);
			hisIntent.putExtra("max_count", countTag);
			startActivity(hisIntent);
		}else if (type.equals("count_chart")) {//统计排行
			
				Intent intent = new Intent(NewStudentBadgeActivity.this, StudentHornorRollForParentActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt(MyBundleName.STUDENT_ID, studentId);
				bundle.putLong(MyBundleName.STUDENT_GROUP_ID, studentBean.groupId);
				intent.putExtras(bundle);
				NewStudentBadgeActivity.this.startActivity(intent);
			
		}

	}

	private int dip2px(float dpValue) {

		final float scale = getResources().getDisplayMetrics().density;

		return (int) (dpValue * scale + 0.5f);

	}

	private PopupWindow mPopupWindow;

	private int countTag;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stCategorys.clear();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == STUDENT_BADGE_DETAIL_REQUEST_CODE) {
			if (data != null) {
				boolean needRefresh = data.getBooleanExtra(MyBundleName.STUDENT_BADGE_IS_RETRACT, false);
				if (needRefresh) {
					getStudentBadgeInfo(studentId);
				}
			}
		} else if (requestCode == PICTUER_ADD_REQUEST_CODE) {
			ArrayList<String> imgDatas = data.getStringArrayListExtra("img_data");
			takePhotoPopWin.addImgList(imgDatas);
			Log.d(TAG, "resultCode:  " + resultCode);
		} else if (requestCode == REMARK_IMAGE_PREVIEW_REQUEST_CODE) {
			ArrayList<String> imgDatas = data.getStringArrayListExtra("img_data");
			takePhotoPopWin.updateImgList(imgDatas);
		} else if (requestCode == BADGE_REMARK_REQUEST_CODE) {
			String text = data.getStringExtra(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_TEXT);
			ArrayList<String> images = data
					.getStringArrayListExtra(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_IMAGES);
			FlowerBean bean = mDataList.get(mAddPosition);
			bean.content = text;
			if (images != null) {
				bean.urls = new String[images.size()];
				images.toArray(bean.urls);
			}
			mAdapter.notifyDataSetChanged();
		}
	}

	public STBadgeCategroy isBadgeExistIn(int badgeId, ArrayList<STBadgeCategroy> categorys) {

		for (STBadgeCategroy stc : categorys) {
			if (stc.badge.id == badgeId) {
				return stc;
			}
		}
		return null;
	}

	public class STBadgeCategroy {

		public Badge badge;
		public ArrayList<StudentBadge> stBadgeList = new ArrayList<StudentBadge>();

	}
	
	
	
	private void showLoadDialog(){
		if (progressDialog == null) {
			progressDialog = ProgressDialog.show(this, "", "...loading...");
			progressDialog.show();
		}else {
			progressDialog.show();
		}
	}
	
	private void hideLoadDialog(){
		if (progressDialog != null) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			
		}
	}
	

}
