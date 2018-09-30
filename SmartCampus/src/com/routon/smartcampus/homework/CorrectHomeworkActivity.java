package com.routon.smartcampus.homework;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.pictureAdd.PictureAddActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.widget.PopupList;
import com.routon.edurelease.R;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.smartcampus.flower.PopOnClickListener;
import com.routon.smartcampus.flower.RemarkImagePreviewActivity;
import com.routon.smartcampus.guideview.Guide;
import com.routon.smartcampus.guideview.GuideBuilder;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.ClassSelListViewAdapter;
import com.routon.smartcampus.student.OnStudentBeanChangeListener;
import com.routon.smartcampus.student.StudentItemAdapter;
import com.routon.smartcampus.student.PullToRefreshInitialIndexGridView;
import com.routon.smartcampus.utils.ImgUploadUtil;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.utils.UploadImgListener;
import com.routon.smartcampus.view.IndexBarView;
import com.routon.smartcampus.view.InitialIndexGridView;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.routon.widgets.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CorrectHomeworkActivity extends BaseActivity implements OnClickListener {

	private static String TAG = "CorrectHomeworkActivity";

	private InitialIndexGridView mGridView;
	private StudentItemAdapter mAdaptor;
	private RelativeLayout correctTitleRl;
	private RelativeLayout homeworkContentRl;
//	private GridView imgGridView;
	private TextView dateClassView;
	private TextView homeworkContentView;
	private TextView unSubmitText;
	private TextView wholeText;
	// private LayoutParams params;
	// private TakePhotoPopWin takePhotoPopWin;

	private ArrayList<Integer> mListSectionPos = new ArrayList<Integer>();
	private ArrayList<String> mListItems = new ArrayList<String>();
	private ArrayList<String> mNameInitialList;
	private ArrayList<StudentBean> mDataList;
	private ArrayList<StudentBean> mStudentDataList;
	private ArrayList<FeedbackWorkBean> imgDatas = new ArrayList<FeedbackWorkBean>();

	private ProgressDialog progressDialog;

	// private ArrayList<String> imgDatas = new ArrayList<String>();
	private StudentHomeWorkBean studentHomeWorkBean;
	private ArrayList<CorrectStudentBean> correctStudentBeanList;
	private ArrayList<StudentBean> studentdatalist;
	private ArrayList<StudentBean> ratestudentlist=new ArrayList<StudentBean>();
	
	private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";
	private List<String> remarkList;
	private ListView remarkListView;
	private View dropdownMask;
	private FrameLayout mCorrectDropdown;
	private boolean mPopViewShow = true;
	private LinearLayout  mCorrectLL;
	private TextView mCorrectRemark;
	private int mPosition;
	private String[] rateStrs = new String[0];
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.activity_correct_homework);
		this.setMoveBackEnable(true);

		Intent intent = getIntent();
		classId = intent.getStringExtra(MyBundleName.HOMEWORK_CLASS_ID);
		className = intent.getStringExtra(MyBundleName.HOMEWORK_CLASS_NAME);
		homeworkId = String.valueOf(intent.getIntExtra(MyBundleName.HOMEWORK_ID, 0));
		mFaceRecongnizeMgr = FaceRecognizeMgr.getInstance(this);
		mFaceRecongnizeMgr.init(this);

		initView();
		initData();
	}
	
	@Override
	public void onDestroy() {
		if( mFaceRecongnizeMgr != null ){
			mFaceRecongnizeMgr.deinit();
		}
		super.onDestroy();
	}

	private void initData() {
		getStudentListData(classId);
		
		SharedPreferences sharedPrefrences = this.getSharedPreferences("homewprkRate", Context.MODE_PRIVATE);
		rateString = sharedPrefrences.getString("rateStr", "A、B、C、D、未完成");
		
		if (rateString!=null && rateString.contains("、")) {
			rateStrs=rateString.split("、");
		}
		
		
		remarkList=new ArrayList<String>();
		remarkList.add("所有");
		remarkList.add("作业反馈");
		remarkList.add("未批改");
		remarkList.add(rateStrs[0]);
		remarkList.add(rateStrs[1]);
		remarkList.add(rateStrs[2]);
		remarkList.add(rateStrs[3]);
		remarkList.add(rateStrs[4]);
		
		
	}

	private void initView() {

		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				CorrectHomeworkActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};

		correctTitleRl = (RelativeLayout) findViewById(R.id.correct_title_rl);
		homeworkContentRl = (RelativeLayout) findViewById(R.id.homework_content_rl);
		ImageView backBut = (ImageView) findViewById(R.id.back_btn);
		TextView titleView = (TextView) findViewById(R.id.title_view);
		titleView.setText("批改作业");
		remarkListView=(ListView) findViewById(R.id.dropdown_listview_correct_remark);
		dropdownMask=findViewById(R.id.dropdown_mask_correct);
		mCorrectDropdown=(FrameLayout) findViewById(R.id.dropdown_correct_remark);
		mCorrectLL=(LinearLayout) findViewById(R.id.tv_correct_linear);
		mCorrectRemark=(TextView) findViewById(R.id.correct_remark_tv);
		dropdownMask.setOnClickListener(this);
		
		scrollView = (ScrollView) findViewById(R.id.scroll_view);
		
		mCorrectLL.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dropdownClick();
			}
		});
		remarkListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mCorrectRemark.setText(remarkList.get(position));
				listViewInAnim();
				mPosition=position;
				showStudentList(mPosition);
				
			}
		});
//		TextView nextBut = (TextView) findViewById(R.id.next_but);

//		unSubmitText = (TextView) findViewById(R.id.un_submit_view);
//		wholeText = (TextView) findViewById(R.id.submit_state_whole);

		homeworkImgView = (ImageView) findViewById(R.id.homework_img);
		dateClassView = (TextView) findViewById(R.id.homework_date_class);
		homeworkContentView = (TextView) findViewById(R.id.homework_content);
		
//		imgGridView = (GridView) findViewById(R.id.homework_img_gridview);

		PullToRefreshInitialIndexGridView pullToRefreshInitialIndexGridView = (PullToRefreshInitialIndexGridView) findViewById(
				R.id.student_list_view);
		mGridView = pullToRefreshInitialIndexGridView.getRefreshableView();
		mGridView.setNumColumns(3);
		mGridView.setHorizontalSpacing((int) this.getResources().getDimension(R.dimen.student_grid_horizontal_space));
		mGridView.setVerticalSpacing((int) this.getResources().getDimension(R.dimen.student_grid_vertical_space));
		mGridView.setPadding((int) this.getResources().getDimension(R.dimen.student_grid_padding_left),
				(int) this.getResources().getDimension(R.dimen.student_grid_padding_left),
				(int) this.getResources().getDimension(R.dimen.student_grid_padding_right), 0);

		backBut.setOnClickListener(mBackListener);
//		nextBut.setOnClickListener(this);
//		unSubmitText.setOnClickListener(this);
//		wholeText.setOnClickListener(this);
		/*modifyHomework.setOnClickListener(this);*/

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		final int screenHeight = dm.heightPixels;

		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//				int[] location = new int[2];
//				view.getLocationOnScreen(location);
//				float viewHeight = location[1] + view.getHeight() / 2;
//				if (viewHeight > (correctTitleRl.getHeight() + homeworkContentRl.getHeight() + view.getHeight() / 4)
//						&& viewHeight < screenHeight) {
//					setPopupListAndHightLight(view, position);
//				}
				

			}

		});

//		imgGridView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				Intent intent = new Intent(CorrectHomeworkActivity.this, RemarkImagePreviewActivity.class);
//				Bundle bundle = new Bundle();
//				bundle.putBoolean(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_ADD_PIC, false);
//				bundle.putInt(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_POSITION, position);
//				bundle.putStringArrayList(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST,
//						studentHomeWorkBean.fileUrls);
//				intent.putExtras(bundle);
//				startActivity(intent);
//			}
//		});
		homeworkImgView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CorrectHomeworkActivity.this, RemarkImagePreviewActivity.class);
				Bundle bundle = new Bundle();
				bundle.putBoolean(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_ADD_PIC, false);
				bundle.putInt(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_POSITION, 0);
				bundle.putStringArrayList(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST,
						studentHomeWorkBean.fileUrls);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	protected void showStudentList(int position) {
		switch(position){
		case 0:
			getStudentListData(classId);
			break;
		case 1:
			getRateStudentList(9);
			break;
		case 2:
			getRateStudentList(-1);
			break;
		case 3:
			getRateStudentList(1);
			break;
		case 4:
			getRateStudentList(2);
			break;
		case 5:
			getRateStudentList(3);
			break;
		case 6:
			getRateStudentList(4);
			break;
		case 7:
			getRateStudentList(5);
			break;
		}
	}

	public void getRateStudentList(int pos){
		
		ratestudentlist.clear();
		if( correctStudentBeanList != null && studentdatalist != null ){
			for (int j = 0; j < correctStudentBeanList.size(); j++) {
				for (int i = 0; i < studentdatalist.size(); i++) {
					
					if (studentdatalist.get(i).sid == correctStudentBeanList.get(j).studentId) {
						studentdatalist.get(i).rate=correctStudentBeanList.get(j).rate;
						
					}
					                                                                                                                                         
				}
			}
		}
		if( studentdatalist != null ){
			for(int k=0;k<studentdatalist.size();k++){
				if (pos==9) {
					if (!studentdatalist.get(k).parentRemark.equals("") && !studentdatalist.get(k).parentRemark.equals("null")) {
						ratestudentlist.add(studentdatalist.get(k));
					}else if (studentdatalist.get(k).checkResList !=null && studentdatalist.get(k).checkResList.size()>0) {
						ratestudentlist.add(studentdatalist.get(k));
					}
				}else {
					if(studentdatalist.get(k).rate==pos){
						ratestudentlist.add(studentdatalist.get(k));
					}
				}
				
			}
		}
		initStudentList(ratestudentlist, null);
		mAdaptor.notifyDataSetChanged();
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.next_but:// 完成
//			CorrectHomeworkActivity.this.finish();
//			break;
//		case R.id.un_submit_view:// 未交
//			wholeText.setTextColor(getResources().getColor(R.color.homework_text_grey));
//			unSubmitText.setTextColor(getResources().getColor(R.color.homework_text_color));
//
//			for (int j = 0; j < correctStudentBeanList.size(); j++) {
//				for (int i = 0; i < studentdatalist.size(); i++) {
//					if (studentdatalist.get(i).sid == correctStudentBeanList.get(j).studentId) {
//						studentdatalist.remove(i);
//					}
//
//				}
//			}
//			initStudentList(studentdatalist, null);
			// mAdaptor.notifyDataSetChanged();
//			break;
//		case R.id.submit_state_whole:// 所有
//			wholeText.setTextColor(getResources().getColor(R.color.homework_text_color));
//			unSubmitText.setTextColor(getResources().getColor(R.color.homework_text_grey));
//			getStudentListData(classId);
//			break;

		/*case R.id.modify_homework:// 修改作业
			ArrayList<String> imgFileUrlList=new ArrayList<String>();
			imgFileUrlList.add("null");
			imgFileUrlList.addAll(0,studentHomeWorkBean.fileUrls);
			showPopWin(imgFileUrlList,studentHomeWorkBean.description);
			
			break;*/
		
		case R.id.dropdown_mask_correct:
		if (!mPopViewShow) {
			listViewInAnim();
		}
		
		break;
		default:
			break;
		}
	}

	
	
	private void dropdownClick() {
		if (mPopViewShow) {
			if (remarkList != null) {
				remarkListView.setAdapter(new ClassSelListViewAdapter(CorrectHomeworkActivity.this, remarkList));
				listViewOutAnim();
			}
		} else {
			listViewInAnim();
		}
	}


	private void listViewOutAnim() {
		remarkListView.clearAnimation();
		remarkListView.setVisibility(View.VISIBLE);
		remarkListView.startAnimation(AnimationUtils.loadAnimation(CorrectHomeworkActivity.this, R.anim.dd_menu_in));
		dropdownMask.setVisibility(View.VISIBLE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(CorrectHomeworkActivity.this, R.anim.dd_mask_in));
		mCorrectDropdown.setVisibility(View.VISIBLE);
		mPopViewShow = false;
	}
	private void listViewInAnim() {
		
		remarkListView.clearAnimation();
		remarkListView.setVisibility(View.GONE);
		remarkListView.startAnimation(AnimationUtils.loadAnimation(CorrectHomeworkActivity.this, R.anim.dd_menu_out));
		dropdownMask.setVisibility(View.GONE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(CorrectHomeworkActivity.this, R.anim.dd_mask_out));
		mCorrectDropdown.startAnimation(AnimationUtils.loadAnimation(CorrectHomeworkActivity.this, R.anim.dd_mask_out));
		mCorrectDropdown.setVisibility(View.GONE);
	
		mPopViewShow = true;

	}
	
	private void getStudentListData(final String groupIds) {// 获取学生列表
		String urlString = SmartCampusUrlUtils.getStudentListUrl() + "&groupIds=" + groupIds;
		showLoadDialog();
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);

						try {
							if (response.getInt("code") == 0) {
								studentdatalist = new ArrayList<StudentBean>();
								JSONArray array = response.optJSONArray("datas");
								if (array != null) {
									int len = array.length();
									for (int i = 0; i < len; i++) {
										JSONObject obj = (JSONObject) array.get(i);

										StudentBean bean = new StudentBean(obj);
										studentdatalist.add(bean);

									}
								}

								downloadStudentImage(studentdatalist);
								initStudentList(studentdatalist, null);

								getCorrectHomeworkStudentList(classId, homeworkId);

							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(CorrectHomeworkActivity.this);
								hideLoadDialog();
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(CorrectHomeworkActivity.this, response.getString("msg"),
										Toast.LENGTH_LONG).show();
								hideLoadDialog();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideLoadDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	private boolean feedbackToastIsShow;
	/**
	 * 获取批改作业学生列表
	 * 
	 * @param classId
	 * @param homeworkId
	 */
	private void getCorrectHomeworkStudentList(String classId, String homeworkId) {

		String urlString = SmartCampusUrlUtils.getCorrectHomeworkQueryUrl(classId, homeworkId);

		showLoadDialog();
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {


					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						
						try {
							if (response.getInt("code") == 0) {
								correctStudentBeanList = new ArrayList<CorrectStudentBean>();
								ArrayList<CorrectStudentBean> correctStudentList = new ArrayList<CorrectStudentBean>();
								JSONArray jsonArray = response.getJSONArray("datas");
								int len = jsonArray.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) jsonArray.get(i);
									studentHomeWorkBean = new StudentHomeWorkBean(obj.getJSONObject("homework"));
									// 学生列表
									for (int j = 0; j < obj.getJSONArray("students").length(); j++) {
										JSONObject obj2 = (JSONObject) obj.getJSONArray("students").get(j);
										CorrectStudentBean correctStudentBean = new CorrectStudentBean(obj2);
										
										if (correctStudentBean.rate>0) {
											correctStudentBeanList.add(correctStudentBean);
											
										}
										correctStudentList.add(correctStudentBean);
									}

								}

								if (mDataList==null) {
									Toast.makeText(CorrectHomeworkActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
									return;
								}
								int feedbackCount = 0;
								
								for (int i = 0; i < mDataList.size(); i++) {
									for (int j = 0; j < correctStudentList.size(); j++) {
										if (mDataList.get(i).sid == correctStudentList.get(j).studentId) {
											mDataList.get(i).homeworkRate = correctStudentList.get(j).rate;
											mDataList.get(i).remark = correctStudentList.get(j).remark;
											mDataList.get(i).isCheck=correctStudentList.get(j).isCheck;
											mDataList.get(i).correctResList=correctStudentList.get(j).correctResList;
											mDataList.get(i).checkResList=correctStudentList.get(j).checkResList;
											mDataList.get(i).correctHomeworkMaterialIds=correctStudentList.get(j).correctHomeworkMaterialIds;
											mDataList.get(i).checkHomeworkMaterialIds=correctStudentList.get(j).checkHomeworkMaterialIds;
											mDataList.get(i).rateStr = correctStudentList.get(j).rateStr;
											mDataList.get(i).checkTime = correctStudentList.get(j).checkTime;
											mDataList.get(i).isFeedback = correctStudentList.get(j).isFeedback;
											mDataList.get(i).parentRemark = correctStudentList.get(j).parentRemark;
											
											
											
										}
									}

									
									if (!mDataList.get(i).parentRemark.equals("") && !mDataList.get(i).parentRemark.equals("null")) {
										feedbackCount +=1;
									}else if (mDataList.get(i).checkResList !=null && mDataList.get(i).checkResList.size()>0) {
										feedbackCount +=1;
									}
								}
								
								
								
								
								mAdaptor.notifyDataSetChanged();

								dateClassView.setText(
										getTimeStr(studentHomeWorkBean.assignmentTime) + "　" + className );
								homeworkContentView.setText(studentHomeWorkBean.description);
								
//								imgGridView.setAdapter(new FamilyHomeworkGridImgAdapter(CorrectHomeworkActivity.this,
//										studentHomeWorkBean.fileUrls));
								if (studentHomeWorkBean.fileUrls.size()>0) {
									if (studentHomeWorkBean.fileUrls.size()>1) {
										homeworkImgView.setBackgroundResource(R.drawable.homework_img_bag);
									}
									
									homeworkImgView.setVisibility(View.VISIBLE);
									Picasso.with(CorrectHomeworkActivity.this).load(studentHomeWorkBean.fileUrls.get(0)).placeholder(R.drawable.empty_photo).error(R.drawable.empty_photo).into(homeworkImgView);
								}
								
								if (homeworkContentView.getLineCount()>6) {
									RelativeLayout.LayoutParams scrollParams=
										    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dip2px(CorrectHomeworkActivity.this,130));
									scrollParams.setMargins(0, dip2px(CorrectHomeworkActivity.this,50), 0, 0);
											
									scrollView.setLayoutParams(scrollParams);
								}
								hideLoadDialog();

								if (!feedbackToastIsShow) {
									feedbackToastIsShow = true;
									Toast.makeText(CorrectHomeworkActivity.this, "有"+feedbackCount+"位同学提交了作业反馈，您可在右上方选择作业反馈查看并批改", Toast.LENGTH_LONG).show();
								}
								
								} else if (response.getInt("code") == -2) {
								hideLoadDialog();
								InfoReleaseApplication.returnToLogin(CorrectHomeworkActivity.this);
							} else {
								hideLoadDialog();
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(CorrectHomeworkActivity.this, response.getString("msg"),
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
						Toast.makeText(CorrectHomeworkActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	public String listToString(List<String> list, char separator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {

			sb.append(list.get(i)).append(separator);

		}
		return sb.toString().substring(0, sb.toString().length() - 1);
	}

	/**
	 * 批改作业
	 * 
	 * @param classId
	 * @param student
	 * @param homeworkId
	 * @param position
	 */
	private void correctHomework(final String classId, final StudentBean student, final String homeworkId,
			final int position) {
		
		String fileIds = null;
		String  fileIdParams=null;
		if (studentBean.correctHomeworkMaterialIds.size() > 0) {
			fileIds = listToString(studentBean.correctHomeworkMaterialIds, ',');
			
			List<String> fileIdParamsList=new ArrayList<String>();
			for (int i = 0; i < studentBean.correctResList.size(); i++) {
				if (studentBean.correctResList.get(i).isLocal && studentBean.correctResList.get(i).fileType == 2) {
					fileIdParamsList.add(studentBean.correctResList.get(i).fileId+"_"+studentBean.correctResList.get(i).fileIdparams);
				}else if (!studentBean.correctResList.get(i).isLocal && studentBean.correctResList.get(i).fileType == 2) {
					fileIdParamsList.add(studentBean.correctResList.get(i).fileId+"_"+studentBean.correctResList.get(i).fileIdparams);
				}
			}
			if (fileIdParamsList.size()>0) {
				fileIdParams=listToString(fileIdParamsList, ',');
			}
			
		}
		

		String urlString = SmartCampusUrlUtils.getCorrectHomeworkUrl(classId, String.valueOf(student.sid), homeworkId,
				String.valueOf(position),fileIds,fileIdParams);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		if (studentBean.remark != null && !studentBean.remark.equals("")) {
			params.add(new BasicNameValuePair("remark", studentBean.remark));
		}
		if (rateStrs[position-1] != null && !rateStrs[position-1].equals("")) {
			params.add(new BasicNameValuePair("rateStr", rateStrs[position-1]));
		}
		// String
		// urlString=SmartCampusUrlUtils.getCorrectHomeworkUrl(String.valueOf(classId));
		// urlString+="&studentId="+(new long[]{Long.valueOf(student.sid)});
		// urlString+="&homeworkId="+(new long[]{Long.valueOf(homeworkId)});

		// String materialIdSrc = "";

		showLoadDialog();
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideLoadDialog();
						try {
							if (response.getInt("code") == 0) {
								Toast.makeText(CorrectHomeworkActivity.this, "批改作业成功!", Toast.LENGTH_SHORT).show();

								if (dialog != null && dialog.isShowing()) {
									dialog.dismiss();
								}
								CorrectStudentBean bean = new CorrectStudentBean();
								bean.studentId = student.sid;
								bean.name = student.empName;
								bean.rate = position;
								bean.rateStr=rateStrs[position-1];
								
								studentBean.homeworkRate = position;
								
								studentBean.rateStr=rateStrs[position-1];
									
								if (correctStudentBeanList != null && correctStudentBeanList.size() > 0) {
									boolean isContains = false;
									for (int i = 0; i < correctStudentBeanList.size(); i++) {
										if (correctStudentBeanList.get(i).studentId == bean.studentId) {
											correctStudentBeanList.set(i, bean);
											isContains = true;
										}
									}
									if (!isContains) {
										correctStudentBeanList.add(bean);
									}
								}

								correctStudentBeanList.add(bean);
								mAdaptor.notifyDataSetChanged();
								
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(CorrectHomeworkActivity.this);
							} else {

								Log.e(TAG, response.getString("msg"));
								Toast.makeText(CorrectHomeworkActivity.this, response.getString("msg"),
										Toast.LENGTH_LONG).show();

							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(CorrectHomeworkActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	/**
	 * 修改作业
	 * 
	 * @param homeworkId
	 * @param position
	 */
/*	private void modifyHomework(String homeworkId, String description) {

		String urlString = SmartCampusUrlUtils.getModifyHomeworkUrl(homeworkId, description);

		showLoadDialog();
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideLoadDialog();
						try {
							if (response.getInt("code") == 0) {
								Toast.makeText(CorrectHomeworkActivity.this, "修改作业成功",
										Toast.LENGTH_LONG).show();
							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(CorrectHomeworkActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(CorrectHomeworkActivity.this, response.getString("msg"),
										Toast.LENGTH_LONG).show();

							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(CorrectHomeworkActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}*/

	private String getTimeStr(String time) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dstr = time.substring(0, time.indexOf(" "));
		Date date = null;
		try {
			date = sdf.parse(dstr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String str = formatter.format(date);
		return str;
	}

	private FaceRecognizeMgr mFaceRecongnizeMgr = null;

	public void downloadStudentImage(ArrayList<StudentBean> datalist) {
		if (datalist == null)
			return;
		ArrayList<String> savepaths = new ArrayList<String>();
		ArrayList<String> urls = new ArrayList<String>();
		for (int i = 0; i < datalist.size(); i++) {
			StudentBean bean = datalist.get(i);
			if (bean.imgUrl != null && bean.imgUrl.isEmpty() == false) {
				urls.add(bean.imgUrl);
				savepaths.add(bean.imgSavePath);
			}
		}
		Response.Listener<String> listener = new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				if (mAdaptor != null) {
					mAdaptor.notifyDataSetChanged();
				}

			}

		};
		mFaceRecongnizeMgr.startDownloadClassImagesAndGetDatas(savepaths, urls, listener, null);
	}

	void setPopupListAndHightLight(View view, int position) {
		final Guide guide = showHightLight(view);
		PopupList poplist = showPopupList(view, position, new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				guide.dismiss();
			}
		});

	}

	private int positionTag;

	private PopupList showPopupList(View anchorView, int contextPosition, PopupWindow.OnDismissListener listener) {
		List<String> popupMenuItemList = new ArrayList<String>();
		studentBean = mDataList.get(contextPosition);
		popupMenuItemList.add(rateStrs[0]);
		popupMenuItemList.add(rateStrs[1]);
		popupMenuItemList.add(rateStrs[2]);
		popupMenuItemList.add(rateStrs[3]);
		popupMenuItemList.add(rateStrs[4]);
//		if (studentBean.homeworkRate != 0) {
			popupMenuItemList.add("附注");
//		}
		

		int[] location = new int[2];
		anchorView.getLocationOnScreen(location);
		final float viewWidth = anchorView.getWidth() / 2;
		final float x = location[0] + anchorView.getWidth() / 2;
		final float y = location[1] + anchorView.getHeight() / 3;
		PopupList popupList = new PopupList(this);
		popupList.setTextPaddingLeft(popupList.dp2px(14));
		popupList.setTextPaddingRight(popupList.dp2px(14));
		popupList.setOnDismissListener(listener);
		if (rateString.length()>13) {
			popupList.setTextSize(dip2px(this,14));
		}
		
		popupList.showPopupListWindow(anchorView, contextPosition, x, y, popupMenuItemList,
				new PopupList.PopupListListener() {

					@Override
					public void onPopupListClick(View contextView, int contextPosition, int position) {
						if (position == 5) {// 附注
							positionTag = studentBean.homeworkRate;
							// TODO 6-20批改作业流程修改
							Intent intent = new Intent(CorrectHomeworkActivity.this, HomeworkFeedbackActivity.class);
							
							if ((!mDataList.get(contextPosition).parentRemark.equals("null") && !mDataList.get(contextPosition).parentRemark.equals("")) ||mDataList.get(contextPosition).checkResList.size()>0 ) {
								ArrayList<StudentBean> intentList=getFeedbackStudentLset();
								intent.putExtra("is_feedback", true);
								intent.putExtra("position", intentList.indexOf(mDataList.get(contextPosition)));
								intent.putExtra("homeworkId", homeworkId);
								intent.putExtra("classId", classId);
								Bundle  mBundle = new Bundle();
					            mBundle.putSerializable("studentBeanList",intentList); 
					            intent.putExtras(mBundle);
							}else {
								intent.putExtra("is_feedback", false);
								intent.putExtra("position", contextPosition);
								intent.putExtra("homeworkId", homeworkId);
								intent.putExtra("classId", classId);
								Bundle  mBundle = new Bundle();
					            mBundle.putSerializable("studentBeanList",mDataList); 
					            intent.putExtras(mBundle);
							}
							
				            CorrectHomeworkActivity.this.startActivityForResult(intent, 3);
//							startActivity(intent);
						} else {
							correctHomework(classId, studentBean, homeworkId, position+1);

						}

					}

					@Override
					public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
						return true;
					}
				});
		return popupList;
	}

	private ArrayList<StudentBean> getFeedbackStudentLset() {
		
		ArrayList<StudentBean> list=new ArrayList<StudentBean>();
		
		
		if( correctStudentBeanList != null && mDataList != null ){
			for (int j = 0; j < correctStudentBeanList.size(); j++) {
				for (int i = 0; i < mDataList.size(); i++) {
					
					if (mDataList.get(i).sid == correctStudentBeanList.get(j).studentId) {
						mDataList.get(i).rate=correctStudentBeanList.get(j).rate;
						
					}
					                                                                                                                                         
				}
			}
		}
		if( mDataList != null ){
			for(int k=0;k<mDataList.size();k++){
					if (!mDataList.get(k).parentRemark.equals("") && !mDataList.get(k).parentRemark.equals("null")) {
						list.add(mDataList.get(k));
					}else if (mDataList.get(k).checkResList !=null && mDataList.get(k).checkResList.size()>0) {
						list.add(mDataList.get(k));
					}
			}
		}
		
	/*	if( correctStudentBeanList != null && studentdatalist != null ){
			
			for (int j = 0; j < correctStudentBeanList.size(); j++) {
				
				for (int i = 0; i < studentdatalist.size(); i++) {
					if (studentdatalist.get(i).sid == correctStudentBeanList.get(j).studentId) {
						studentdatalist.get(i).rate=correctStudentBeanList.get(j).rate;
						
					}
					                                                                                                                                         
				}
			}
		}
		if( studentdatalist != null ){
			for(int k=studentdatalist.size()-1;k>=0;k--){
				
					if ((!studentdatalist.get(k).parentRemark.equals("") && !studentdatalist.get(k).parentRemark.equals("null") )|| studentdatalist.get(k).checkResList.size()>0) {
						list.add(studentdatalist.get(k));
					}
				
				
			}
		}*/
		return list;
		
	}

	private Guide showHightLight(View view) {
		final GuideBuilder builder = new GuideBuilder();
		builder.setTargetView(view).setAlpha(150).setOverlayTarget(false).setEnterAnimationId(android.R.anim.fade_in)
				.setExitAnimationId(android.R.anim.fade_out).setOutsideTouchable(false);
		builder.setOnVisibilityChangedListener(new GuideBuilder.OnVisibilityChangedListener() {
			@Override
			public void onShown() {

			}

			@Override
			public void onDismiss() {

			}
		});

		Guide guide = builder.createGuide();
		guide.setShouldCheckLocInWindow(false);
		guide.show(CorrectHomeworkActivity.this);
		return guide;
	}

	/*
	 * 作业编辑窗口
	 */
	/*private  void showPopWin(ArrayList<String> savemages, String homeworkString) {
		RelativeLayout homework_main=(RelativeLayout) findViewById(R.id.main);
		if (takePhotoPopWin!=null && takePhotoPopWin.isShowing()) {
			takePhotoPopWin.dismiss();
		}
		takePhotoPopWin = new AddHomeworkPopWin(this, popOnClickListener, "请输入", "修改",savemages,homeworkString);
		takePhotoPopWin.showAtLocation(homework_main, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		params = getWindow().getAttributes();
		params.alpha = 0.7f;
		getWindow().setAttributes(params);
		takePhotoPopWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				params = getWindow().getAttributes();
				params.alpha = 1f;
				getWindow().setAttributes(params);
			}
		});
	}*/
	
	/*private PopOnClickListener popOnClickListener = new PopOnClickListener() {

		@Override
		public void itemClick(int position) {// 图片预览
			Intent intent = new Intent(CorrectHomeworkActivity.this, RemarkImagePreviewActivity.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_ADD_PIC, true);
			bundle.putInt(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_POSITION, position);
			bundle.putSerializable(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST,
					takePhotoPopWin.imgList);
			intent.putExtras(bundle);
			CorrectHomeworkActivity.this.startActivityForResult(intent, 2);
		}

		@Override
		public void lastItemtemClick() {// 添加图片
			Intent intent = new Intent(CorrectHomeworkActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 15);
			CorrectHomeworkActivity.this.startActivityForResult(intent, 1);
		}

		@Override
		public void addImgClick(int position) {
			Intent intent = new Intent(CorrectHomeworkActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 15);
			CorrectHomeworkActivity.this.startActivityForResult(intent, 1);
		}

		@Override
		public void awardClick() {// 修改

		}

		@Override
		public void saveRemark(View v) {
			
		}

	};*/
	
	/*
	 * 附注编辑窗口
	 */
	/*private void showRemarkDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		final View layout = getLayoutInflater().inflate(R.layout.dialog_text_edit_layout, null);
		final EditText editView = (EditText) layout.findViewById(R.id.text_edit_view);
		TextView negativeBut = (TextView) layout.findViewById(R.id.edit_negative_but);
		TextView positiveBut = (TextView) layout.findViewById(R.id.edit_positive_but);

		if (!studentBean.remark.equals("null")) {
			editView.setText(studentBean.remark);
			editView.setSelection(studentBean.remark.length());
		}

		negativeBut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		positiveBut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editView.getText() != null
						&& !editView.getText().toString().replace(" ", "").replace("\n", "").equals("")) {
					remarkText = editView.getText().toString();
					addRemarkTag = true;
					correctHomework(classId, studentBean, homeworkId, positionTag);
				} else {
					Toast.makeText(CorrectHomeworkActivity.this, "附注信息为空！", Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.setView(layout);

		dialog = builder.create();
		dialog.show();
		Message message = new Message();
		message.obj = editView;
		mHandler.sendMessage(message);

	}*/

	/*private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.showSoftInput((View) msg.obj, 0);
		};
	};*/

//	private boolean addRemarkTag = false;
	private String remarkText = "";

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
//				imgDatas = data.getSerializableExtra("img_data");
				takePhotoPopWin.addImgList(imgDatas);
			} else if (requestCode == 2) {
//				imgDatas = data.getParcelableArrayListExtra("img_data");
				takePhotoPopWin.updateImgList(imgDatas);
			}else if (requestCode == 3) {
				getCorrectHomeworkStudentList(classId, homeworkId);
				if(mFilePath!=null){
					deleteAllFiles(new File(mFilePath));
				}
			}
		}
	}
	
	//删除录制的本地音频
		 public static void deleteAllFiles(File root) {  
		       File files[] = root.listFiles();  
		       if (files != null)  
		           for (File f : files) {  
		               if (f.isDirectory()) { // 判断是否为文件夹  
		                   deleteAllFiles(f);  
		                   try {  
		                       f.delete();  
		                   } catch (Exception e) {  
		                   }  
		               } else {  
		                   if (f.exists()) { // 判断是否存在  
		                       try {  
		                           f.delete();  
		                       } catch (Exception e) {  
		                       }  
		                   }  
		               }  
		           }  
		   } 

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mListItems != null && mListItems.size() > 0) {
			outState.putStringArrayList("mListItems", mListItems);
		}
		if (mListSectionPos != null && mListSectionPos.size() > 0) {
			outState.putIntegerArrayList("mListSectionPos", mListSectionPos);
		}
		super.onSaveInstanceState(outState);
	}

	private class Poplulate extends AsyncTask<ArrayList<String>, Void, Void> {
		PoplulateCompleteCB mCompleteCB = null;

		public Poplulate() {

		}

		public Poplulate(PoplulateCompleteCB cb) {
			mCompleteCB = cb;
		}

		private void showContent(View contentView) {
			contentView.setVisibility(View.VISIBLE);

		}

		private void showEmptyText(View contentView) {
			contentView.setVisibility(View.GONE);

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			initListAdaptor();
		}

		@Override
		protected Void doInBackground(ArrayList<String>... params) {
			mListItems.clear();
			mListSectionPos.clear();
			ArrayList<String> items = params[0];
			mNameInitialList = new ArrayList<String>();
			if (items.size() > 0) {

				// 获取汉字首字母
				for (String nameStr : items) {
					String initial = getInitial(nameStr.substring(0, 1));
					mNameInitialList.add(initial + nameStr);
				}

				// 重新排序
				Collections.sort(mNameInitialList, new SortIgnoreCase());

				Log.d(TAG, "mNameInitialList size:" + mNameInitialList.size() + ",mListItems:" + mListItems.size());

				String prev_section = "";
				for (String current_item : mNameInitialList) {
					String current_section = current_item.substring(0, 1).toUpperCase(Locale.getDefault());

					if (!prev_section.equals(current_section)) {
						// mListItems.add(current_section);
						mListItems.add(current_item.substring(1, current_item.length()));
						mListSectionPos.add(mListItems.indexOf(current_item.substring(1, current_item.length())));
						prev_section = current_section;
					} else {
						mListItems.add(current_item.substring(1, current_item.length()));
					}
				}

				Log.d(TAG, "mListItems size:" + mListItems.size());

				for (Integer i : mListSectionPos) {
					Log.d(TAG, "mListSectionPos i:" + i);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
//			hideLoadDialog();
			if (!isCancelled()) {
				if (mListItems.size() <= 0) {
					showEmptyText(mGridView);
				} else {
					setListAdaptor();
					showContent(mGridView);
				}
			}
			if (mCompleteCB != null) {
				mCompleteCB.complete();
			}
			super.onPostExecute(result);
		}
	}

	private class SortIgnoreCase implements Comparator<String> {
		public int compare(String s1, String s2) {
			return s1.compareToIgnoreCase(s2);
		}
	}

	private final static int GB_SP_DIFF = 160;
	private final static int[] secPosValueList = { 1601, 1637, 1833, 2078, 2274, 2302, 2433, 2594, 2787, 3106, 3212,
			3472, 3635, 3722, 3730, 3858, 4027, 4086, 4390, 4558, 4684, 4925, 5249, 5600 };
	private final static char[] firstLetter = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'o',
			'p', 'q', 'r', 's', 't', 'w', 'x', 'y', 'z' };

	private StudentBean studentBean;

	private String classId;

	private String className;

	private String homeworkId;

	private AlertDialog dialog;

	private AddHomeworkPopWin takePhotoPopWin;

	private LayoutParams params;

	private ImageView homeworkImgView;

	private ScrollView scrollView;

	private String getInitial(String nameStr) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < nameStr.length(); i++) {

			char ch = nameStr.charAt(i);
			// 对英文字母的处理：小写字母转换为大写，大写的直接返回
			if (ch >= 'a' && ch <= 'z') {
				buffer.append(String.valueOf(ch));
			} else if (ch >= 'A' && ch <= 'Z') {
				buffer.append(String.valueOf(ch));
			} else {
				if ((ch >> 7) == 0) {
					buffer.append("z");
				} else {
					char spell = getFirstLetter(ch);
					buffer.append(String.valueOf(spell));
				}
			}
		}
		return buffer.toString();

	}

	public static Character getFirstLetter(char ch) {

		byte[] uniCode = null;
		try {
			uniCode = String.valueOf(ch).getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		if (uniCode[0] < 128 && uniCode[0] > 0) {
			return null;
		} else {
			return convert(uniCode);
		}
	}

	static char convert(byte[] bytes) {
		char result = '-';
		int secPosValue = 0;
		int i;
		for (i = 0; i < bytes.length; i++) {
			bytes[i] -= GB_SP_DIFF;
		}
		secPosValue = bytes[0] * 100 + bytes[1];
		for (i = 0; i < 23; i++) {
			if (secPosValue >= secPosValueList[i] && secPosValue < secPosValueList[i + 1]) {
				result = firstLetter[i];
				break;
			}
		}
		return result;
	}

	private void initStudentList(ArrayList<StudentBean> datalist, PoplulateCompleteCB cb) {
//		showLoadDialog();
		mStudentDataList = datalist;
		ArrayList<String> items = new ArrayList<String>();
		if (mStudentDataList != null) {
			for (int i = 0; i < mStudentDataList.size(); i++) {
				items.add(mStudentDataList.get(i).empName);
			}
		}
		new Poplulate(cb).execute(items);
//		if (datalist.size()==0) {
//			hideLoadDialog();
//		}
		
	}

	private void setListAdaptor() {
		mDataList = new ArrayList<StudentBean>();

		// 学生数据数组排序
		for (int i = 0; i < mListItems.size(); i++) {
			for (int j = 0; j < mStudentDataList.size(); j++) {
				StudentBean studentBean2 = mStudentDataList.get(j);
				if (mListItems.get(i).equals(studentBean2.empName)) {
					//modified by xiaolp 20180314 学生名字重复时，根据名字判断会重复添加
					if( mDataList.contains(studentBean2) == false ){//去掉重复处理，重名数据会添加多次
						mDataList.add(studentBean2);
					}
				}
			}
		}

		mAdaptor = new StudentItemAdapter(CorrectHomeworkActivity.this, mListItems, mListSectionPos, new ListFilter(),
				mDataList, true, studentChangeListener);
		mGridView.setAdapter(mAdaptor);

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		IndexBarView indexBarView = (IndexBarView) inflater.inflate(R.layout.index_bar_view, mGridView, false);
		indexBarView.setData(mGridView, mNameInitialList, mListSectionPos);

		mGridView.setIndexBarView(indexBarView);

		mGridView.setOnScrollListener(mAdaptor);
	}

	private void initListAdaptor() {
		mAdaptor = new StudentItemAdapter(this, null, null, new ListFilter(), null, true, studentChangeListener);
		mGridView.setAdapter(mAdaptor);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		IndexBarView indexBarView = (IndexBarView) inflater.inflate(R.layout.index_bar_view, mGridView, false);
		indexBarView.setData(mGridView, null, null);

		mGridView.setIndexBarView(indexBarView);

		mGridView.setOnScrollListener(mAdaptor);
	}

	OnStudentBeanChangeListener studentChangeListener = new OnStudentBeanChangeListener() {
		
		@Override
		public void onSelect(StudentBean bean, int position) {
			
		}
		
		@Override
		public void onClicked(StudentBean bean, int position, View view) {
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			float viewHeight = location[1] + view.getHeight() / 2;
			
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			final int screenHeight = dm.heightPixels;
			
			if (viewHeight > (correctTitleRl.getHeight() + homeworkContentRl.getHeight() + view.getHeight() / 4)
					&& viewHeight < screenHeight) {
				setPopupListAndHightLight(view, position);
			}
			
		}
	};

	private String rateString;
	
	private class ListFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			if (mListItems == null) {
				return null;
			}

			String constraintStr = constraint.toString().toLowerCase(Locale.getDefault());
			FilterResults result = new FilterResults();

			if (constraint != null && constraint.toString().length() > 0) {
				ArrayList<String> filterItems = new ArrayList<String>();

				synchronized (this) {
					for (String item : mListItems) {
						if (item.toLowerCase(Locale.getDefault()).startsWith(constraintStr)) {
							filterItems.add(item);
						}
					}
					result.count = filterItems.size();
					result.values = filterItems;
				}
			} else {
				synchronized (this) {
					result.count = mListItems.size();
					result.values = mListItems;
				}
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			ArrayList<String> filtered = (ArrayList<String>) results.values;
			setIndexBarViewVisibility(constraint.toString());
			new Poplulate().execute(filtered);
		}

	}

	private void setIndexBarViewVisibility(String constraint) {
		if (constraint != null && constraint.length() > 0) {
			mGridView.setIndexBarVisibility(false);
		} else {
			mGridView.setIndexBarVisibility(true);
		}
	}

	interface PoplulateCompleteCB {
		void complete();
	}
	

	// ------------------------------
	public static int dip2px(Context context, float dpValue) {

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (dpValue * scale + 0.5f);

	}

	private void showLoadDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(CorrectHomeworkActivity.this, "", "...loading...");
		}
	}

	public void hideLoadDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

}
