package com.routon.smartcampus.homework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.pictureAdd.PictureAddActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.flower.BadgePicEditActivity;
import com.routon.smartcampus.homework.FamilyRecyclerAdapter.FamilyRecyclerAddHolder;
import com.routon.smartcampus.homework.FamilyRecyclerAdapter.FamilyRecyclerImgHolder;
import com.routon.smartcampus.homework.FamilyRecyclerAdapter.OnDelClickListener;
import com.routon.smartcampus.homework.FamilyRecyclerAdapter.OnItemClickListener;
import com.routon.smartcampus.homework.FamilyRecyclerAdapter.OnItemLongClickListener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.ImgUploadUtil;
import com.routon.smartcampus.utils.MyConstant;
import com.routon.smartcampus.utils.RecordUploadUtil;
import com.routon.smartcampus.utils.UploadImgListener;
import com.squareup.picasso.Picasso;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//老师版批改作业界面
public class HomeworkFeedbackActivity extends BaseActivity implements OnClickListener {

	private static String TAG = "HomeworkFeedbackActivity";
	private TextView frontBtn;
	private TextView nextBtn;
	private ImageView studentImg;
	private TextView studentName;
	private StudentBean studentBean;
	private int studentPosition;
	private List<StudentBean> studentBeanList;
	private Bitmap bitmap;
	private LinearLayout rankLayout;
	private LinearLayout.LayoutParams textParams;

	private RecyclerView familyRecyclerView;
	private RecyclerView teacherRecyclerView;
	private FamilyRecyclerAdapter tAdapter;
	private List<FeedbackHomeworkFileBean> tacherDataList;
	private EditText teacherRemarkEditText;
	private FamilyRecyclerImgHolder mImgHolder;
	private ArrayList<FeedbackHomeworkFileBean> imgBeanList = new ArrayList<FeedbackHomeworkFileBean>();
	private String[] rateStrs = new String[0];
	
	private int[] shapeImgs=new int[]{R.drawable.shape_rank1,R.drawable.shape_rank2,R.drawable.shape_rank3,R.drawable.shape_rank4,R.drawable.shape_rank};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_homework_feedback);
		this.setMoveBackEnable(false);

		initView();
		initData();
	}

	private void initView() {
		ImageView backBtn = (ImageView) findViewById(R.id.homework_back_btn);
		frontBtn = (TextView) findViewById(R.id.front_b);
		nextBtn = (TextView) findViewById(R.id.next_b);

		studentImg = (ImageView) findViewById(R.id.student_img);
		studentName = (TextView) findViewById(R.id.student_name);
		timeView = (TextView) findViewById(R.id.time_view);

		rankLayout = (LinearLayout) findViewById(R.id.correct_rank_ll);
		rankLayout2 = (LinearLayout) findViewById(R.id.correct_rank2_ll);
		scrollView = (ScrollView) findViewById(R.id.scroll_layout);
		

		teacherRemarkEditText = (EditText) findViewById(R.id.teacher_correct_remark);
		familyRemarkEditText = (EditText) findViewById(R.id.family_homework_remark);
		familyRemarkEditText.setFocusable(false);

		familyRecyclerView = (RecyclerView) findViewById(R.id.family_recyclerView);
		teacherRecyclerView = (RecyclerView) findViewById(R.id.teacher_recyclerView);
		LinearLayoutManager familyLayoutManager = new LinearLayoutManager(HomeworkFeedbackActivity.this);
		familyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		familyRecyclerView.setLayoutManager(familyLayoutManager);

		LinearLayoutManager teacherLayoutManager = new LinearLayoutManager(HomeworkFeedbackActivity.this);
		teacherLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		teacherRecyclerView.setLayoutManager(teacherLayoutManager);

		
		

		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				toNextStudent(2);
				

			}
		};
		backBtn.setOnClickListener(mBackListener);
		frontBtn.setOnClickListener(this);
		nextBtn.setOnClickListener(this);

	}

	private void initData() {
		studentPosition = getIntent().getIntExtra("position", 0);
		homeworkId = getIntent().getStringExtra("homeworkId");
		classId = getIntent().getStringExtra("classId");
		isFeedback = getIntent().getBooleanExtra("is_feedback", false);
		Bundle bundle = getIntent().getExtras();
		studentBeanList = (List<StudentBean>) bundle.getSerializable("studentBeanList");
		
		
		
		textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				dp2px(34));
		textParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				dp2px(34));
		textParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		textParams4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		
		SharedPreferences sharedPrefrences = this.getSharedPreferences("homewprkRate", Context.MODE_PRIVATE);
		String rateString=sharedPrefrences.getString("rateStr", "A、B、C、D、未完成");
		
		if (rateString!=null && rateString.contains("、")) {
			rateStrs=rateString.split("、");
		}
		
		
		setStudentInfo(0);

		
		relativeLayoutList = new ArrayList<RelativeLayout>();
		textViewList = new ArrayList<TextView>();
		int textSize = 18;
//		if (rateString.length() > 7 && rateString.length() <= 10) {
//			textSize = 16;
//		} else if (rateString.length() > 10) {
//			textSize = 14;
//		}
		
//		textParams.leftMargin = dp2px(5);
//		textParams.rightMargin = dp2px(5);
		textParams3.leftMargin = dp2px(5);
		textParams3.rightMargin = dp2px(5);
		textParams4.leftMargin = dp2px(5);
		textParams4.rightMargin = dp2px(5);
//		textParams3.setMargins(dp2px(5),dp2px(5),dp2px(5),dp2px(5));
		if (rateString.length()>=14) {
			textParams.width=dp2px(60);
		}else {
			textParams.width=dp2px(40);
		}
		textParams3.width=textParams.width+dp2px(6);
//		textParams2.leftMargin = dp2px(5);
//		textParams2.rightMargin = dp2px(5);
		textParams2.width=dp2px(80);
		textParams4.width=textParams2.width+dp2px(6);
		for (int j = 0; j <  rateStrs.length; j++) {
			final int textViewTag = j;
			final RelativeLayout relativeLayout=new RelativeLayout(this);
			
			relativeLayout.setGravity(Gravity.CENTER);
			final TextView textView = new TextView(this);
			textView.setText(rateStrs[j]);
			textView.setBackgroundResource(shapeImgs[j]);
			textView.setTextSize(textSize);
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(getResources().getColor(R.color.white));
			
			if (j!=4) {
				textView.setLayoutParams(textParams);
				relativeLayout.setLayoutParams(textParams3);
//				textParams3.width=textParams.width+dp2px(10);
			}else {
				textView.setLayoutParams(textParams2);
				relativeLayout.setLayoutParams(textParams4);
//				textParams3.width=textParams2.width+dp2px(10);
			}
			
			textView.setClickable(true);
			
			if (studentBean.homeworkRate == j + 1) {
				relativeLayout.setBackgroundResource(R.drawable.shape_rank_stroke);
			} else {
				relativeLayout.setBackgroundResource(R.drawable.shape_rank_null);
			}

			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					relativeLayout.setBackgroundResource(R.drawable.shape_rank_stroke);
					if (studentBean.homeworkRate > 0) {
						relativeLayoutList.get(studentBean.homeworkRate - 1).setBackgroundResource(R.drawable.shape_rank_null);
					}
					studentBean.homeworkRate = textViewTag + 1;

					studentBean.rateStr=rateStrs[textViewTag];
				}
			});
			textViewList.add(textView);
			relativeLayout.addView(textView);
			relativeLayoutList.add(relativeLayout);
            if (rateString.length()>=14) {
            	if (j==3 ||j==4) {
    				rankLayout2.addView(relativeLayout);
    			}else {
    				rankLayout.addView(relativeLayout);
    			}
			}else {
				if (j==4) {
					rankLayout2.addView(relativeLayout);
				}else {
					rankLayout.addView(relativeLayout);
				}
			}
			
			
		}

		setFamilyData();
		setTeacherData();
	}

	public String listToString(List<String> list, char separator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {

			sb.append(list.get(i)).append(separator);

		}
		return sb.toString().substring(0, sb.toString().length() - 1);
	}

	// 批改作业
	private void correctHomework() {

		String fileIds = null;
		String  fileIdParams=null;
		if (studentBean.correctHomeworkMaterialIds.size() > 0) {
			fileIds = listToString(studentBean.correctHomeworkMaterialIds, ',');
			
			List<String> fileIdParamsList=new ArrayList<String>();
			for (int i = 0; i < tacherDataList.size(); i++) {
				if (tacherDataList.get(i).isLocal && tacherDataList.get(i).fileType == 2) {
					fileIdParamsList.add(tacherDataList.get(i).fileId+"_"+tacherDataList.get(i).fileIdparams);
				}else if (!tacherDataList.get(i).isLocal && tacherDataList.get(i).fileType == 2) {
					fileIdParamsList.add(tacherDataList.get(i).fileId+"_"+tacherDataList.get(i).fileIdparams);
				}
			}
			if (fileIdParamsList.size()>0) {
				fileIdParams=listToString(fileIdParamsList, ',');
			}
			
		}
		
		
		String urlString = SmartCampusUrlUtils.getCorrectHomeworkUrl(classId, String.valueOf(studentBean.sid),
				homeworkId, String.valueOf(studentBean.homeworkRate), fileIds,fileIdParams);

		List<NameValuePair> params = new ArrayList<NameValuePair>();

		if (studentBean.remark != null && !studentBean.remark.equals("")) {
			params.add(new BasicNameValuePair("remark", studentBean.remark));
		}
		if (studentBean.rateStr!= null && !studentBean.rateStr.equals("")) {
			params.add(new BasicNameValuePair("rateStr", studentBean.rateStr));
		}

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
								dataIsChange = true;
//								studentBeanList.remove(studentPosition);

								if (toStudentType == 0) {
									if (studentPosition > 0) {
										studentPosition -= 1;
										setStudentInfo(1);
									}
								} else if (toStudentType == 1) {
									if (studentPosition < studentBeanList.size() - 1) {
										studentPosition += 1;
										setStudentInfo(1);
									}
								} else if (toStudentType == 2) {
									Toast.makeText(HomeworkFeedbackActivity.this, "批改成功",
											Toast.LENGTH_SHORT).show();
									finishThis();
									
								}

							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(HomeworkFeedbackActivity.this);
								finishThis();
							} else {

								Log.e(TAG, response.getString("msg"));
								Toast.makeText(HomeworkFeedbackActivity.this, response.getString("msg"),
										Toast.LENGTH_LONG).show();
								finishThis();

							}

						} catch (JSONException e) {
							e.printStackTrace();
							finishThis();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(HomeworkFeedbackActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();
						finishThis();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	protected void finishThis() {
		
		if (toStudentType == 2) {
			endPlay();
			if (dataIsChange) {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				finish();
			}else {
				HomeworkFeedbackActivity.this.finish();
			}
			
			overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
		}
	}

	private boolean isRecord;
	private AnimationDrawable animDrawable;

	private void setTeacherData() {
		
		if (studentBean.remark != null && !studentBean.remark.equals("null")) {
			teacherRemarkEditText.setText(studentBean.remark);
		}else {
			teacherRemarkEditText.setText("");
		}
		
		tacherDataList = new ArrayList<FeedbackHomeworkFileBean>();

		
		if (studentBean.correctResList.size() > 0) {
			for (int i = 0; i < studentBean.correctResList.size(); i++) {
				tacherDataList.add(studentBean.correctResList.get(i));
			}
		}

		FeedbackHomeworkFileBean bean1 = new FeedbackHomeworkFileBean("pic", false, 3);
		FeedbackHomeworkFileBean bean2 = new FeedbackHomeworkFileBean("audio", false, 3);
		tacherDataList.add(bean1);
		tacherDataList.add(bean2);

		imgBeanList.clear();
		for (int i = 0; i < tacherDataList.size(); i++) {
			if (tacherDataList.get(i).fileType == 1) {
				imgBeanList.add(tacherDataList.get(i));
			}
		}

		tAdapter = new FamilyRecyclerAdapter(HomeworkFeedbackActivity.this, tacherDataList, true,"tacher");

		tAdapter.setItemClickListener(new OnItemClickListener() {

			@Override
			public void itemClick(View v, int position, String type, FamilyRecyclerImgHolder imgHolder) {
				if (type.equals("pic_preview")) {// 图片预览
					endPlay();

					/*Intent intent = new Intent(HomeworkFeedbackActivity.this, BadgePicEditActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST,
							tacherDataList.get(position));
					intent.putExtras(bundle);
					startActivityForResult(intent, 2);*/

					Intent intent = new Intent(HomeworkFeedbackActivity.this, HomeworkPicActivity.class);
					Bundle bundle = new Bundle();
					bundle.putBoolean(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_ADD_PIC, false);
					bundle.putInt(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_POSITION,
							imgBeanList.indexOf(tacherDataList.get(position)));
					bundle.putSerializable(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST,
							imgBeanList);
					bundle.putString("homework", "homework");
					intent.putExtras(bundle);
					startActivity(intent);
				} else if (type.equals("audio_play")) {// 录音播放
					endPlay();
					mImgHolder = imgHolder;
					if (imgHolder != null) {
						animDrawable = (AnimationDrawable) imgHolder.playRecordImg.getBackground();
						if (animDrawable != null) {
							animDrawable.start();
						}
						startPlayRecord(tacherDataList.get(position).fileUrl, tacherDataList.get(position).audioLength,
								imgHolder.playRecordImg);
					}

				} else if (type.equals("add_pic")) {
					if (tacherDataList.size()>=11) {
						Toast.makeText(HomeworkFeedbackActivity.this, "最多添加9个文件资源！",
								Toast.LENGTH_SHORT).show();
						return;
					}
					
					
					endPlay();
					Intent intent = new Intent(HomeworkFeedbackActivity.this, PictureAddActivity.class);
					intent.putExtra("img_count", tacherDataList.size());
					intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 14);
					HomeworkFeedbackActivity.this.startActivityForResult(intent, 1);
				} 

			}
		});

		teacherRecyclerView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					break;
				case MotionEvent.ACTION_MOVE:
//					if (isRecord) {
//					float x = event.getRawX();
//					float y = event.getRawY();
//					Log.e("坐标2", x+"="+y);
//					
//					if (x>viewRX ||x<viewLX ||y>viewBY||y<viewTY) {
//						stopRecord();
//					}
//					}
					
					break;
				case MotionEvent.ACTION_UP:
					if (isRecord) {
						mAddHolder.tvRecordTime.setVisibility(View.GONE);
						mAddHolder.addRecordImg.setImageResource(R.drawable.record_normal);

						isRecord = false;
						stopRecord();
					}

					break;
				default:
					break;
				}

				return isRecord;
			}
		});

		tAdapter.setItemLongClickListener(new OnItemLongClickListener() {

			

			@Override
			public void itemLongClick(View v, MotionEvent event, final FamilyRecyclerAddHolder addHolder) {
				mAddHolder = addHolder;
				endPlay();
//				int[] location = new int[2];
//				v.getLocationOnScreen(location);
//				viewRX = location[0]+(v.getHeight()/2);
//				viewLX = location[0]-(v.getHeight()/2);
//				viewBY = location[1]+(v.getWidth()/2);
//				viewTY = location[1]-(v.getWidth()/2);

				
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (tacherDataList.size()>=11) {
						Toast.makeText(HomeworkFeedbackActivity.this, "最多添加9个文件资源！",
								Toast.LENGTH_SHORT).show();
						return;
					}
					isRecord = true;
					addHolder.tvRecordTime.setVisibility(View.VISIBLE);
					addHolder.addRecordImg.setImageResource(R.drawable.record_pressed);
					startRecord();
					break;
				case MotionEvent.ACTION_MOVE:
					
					
					
					break;
				case MotionEvent.ACTION_UP:
					addHolder.tvRecordTime.setVisibility(View.GONE);
					addHolder.addRecordImg.setImageResource(R.drawable.record_normal);
					isRecord = false;
					stopRecord();
					break;
				default:
					break;
				}

			}
		});
		
		scrollView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					
					break;
				case MotionEvent.ACTION_MOVE:
					
					break;
				case MotionEvent.ACTION_UP:
					if (isRecord) {
						mAddHolder.tvRecordTime.setVisibility(View.GONE);
						mAddHolder.addRecordImg.setImageResource(R.drawable.record_normal);

						isRecord = false;
						stopRecord();
					}
					break;
				default:
					break;
				}
				
				
				
				return false;
			}
		});

		tAdapter.setDelClickListener(new OnDelClickListener() {

			@Override
			public void delClick(View v, int position) {

				endPlay();
				if (!tacherDataList.get(position).fileId.equals("")) {
					studentBean.correctHomeworkMaterialIds.remove(tacherDataList.get(position).fileTypeId);
					
					if (!tacherDataList.get(position).isLocal) {
						studentBean.correctResList.remove(position);
					}
					
					if (tacherDataList.get(position).fileType==1) {
						imgBeanList.remove(tacherDataList.get(position));
					}
					
				}
				tacherDataList.remove(position);

				tAdapter.notifyDataSetChanged();
			}
		});

		teacherRecyclerView.setAdapter(tAdapter);
	}

	
	
	private FamilyRecyclerAddHolder mAddHolder;

	private void setFamilyData() {
		
		if (studentBean.parentRemark!=null && !studentBean.parentRemark.equals("null")) {
			familyRemarkEditText.setText(studentBean.parentRemark);
		}else {
			familyRemarkEditText.setText("");
		}
		
		final ArrayList<FeedbackHomeworkFileBean> familyImgs = new ArrayList<FeedbackHomeworkFileBean>();
		final ArrayList<FeedbackHomeworkFileBean> familyDatas = new ArrayList<FeedbackHomeworkFileBean>();
		for (int i = 0; i < studentBean.checkResList.size(); i++) {
			familyDatas.add(studentBean.checkResList.get(i));
			if (studentBean.checkResList.get(i).fileType == 1) {
				familyImgs.add(studentBean.checkResList.get(i));
			}
		}

		FamilyRecyclerAdapter fAdapter = new FamilyRecyclerAdapter(HomeworkFeedbackActivity.this, studentBean.checkResList,
				false,"family");
		fAdapter.setItemClickListener(new OnItemClickListener() {

			@Override
			public void itemClick(View v, int position, String type, FamilyRecyclerImgHolder audioHolder) {
				if (type.equals("pic_preview")) {
					Intent intent = new Intent(HomeworkFeedbackActivity.this, BadgePicEditActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST,
							studentBean.checkResList.get(position));
					intent.putExtras(bundle);
					startActivityForResult(intent, 2);
				} else if (type.equals("audio_play")) {
					endPlay();
					mImgHolder = audioHolder;
					if (audioHolder != null) {
						animDrawable = (AnimationDrawable) audioHolder.playRecordImg.getBackground();
						if (animDrawable != null) {
							animDrawable.start();
						}
						Log.e("tacherDataList", tacherDataList.size()+"----"+position);
						startPlayRecord(familyDatas.get(position).fileUrl, familyDatas.get(position).audioLength,
								audioHolder.playRecordImg);
					}

				}

			}
		});

		familyRecyclerView.setAdapter(fAdapter);
	}

	private ArrayList<String> imgDatas = new ArrayList<String>();
	private EditText familyRemarkEditText;

	/*
	 * 上传批改作业资源
	 */
	private void uploadCorrectHomeworkFile(final List<String> addPicList, final List<String> addAudioList) {
		
		showLoadDialog();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if (addPicList.size() > 0 && addAudioList.size() > 0) {
					ImgUploadUtil.uploadImgs(HomeworkFeedbackActivity.this, addPicList, new UploadImgListener() {

						@Override
						public void uploadImgSuccessListener(List<Integer> imgFileIdList) {
							if (imgFileIdList != null && imgFileIdList.size() > 0) {
								imgFileIds = new ArrayList<String>();
								for (int j = 0; j < imgFileIdList.size(); j++) {
									imgFileIds.add(imgFileIdList.get(j) + "_166");
								}
							}

							RecordUploadUtil.uploadRecord(HomeworkFeedbackActivity.this, addAudioList,
									new UploadImgListener() {

										@Override
										public void uploadImgSuccessListener(List<Integer> imgFileIdList) {

											if (imgFileIdList != null && imgFileIdList.size() > 0) {
												audioFileIds = new ArrayList<String>();
												for (int j = 0; j < imgFileIdList.size(); j++) {
													audioFileIds.add(imgFileIdList.get(j) + "_172");
												}
											}

											Message msg = new Message();
											msg.what = UPLOAD_PIC_AUDIO;
											handler.sendMessage(msg);

										}

										@Override
										public void uploadImgErrorListener(String errorStr) {
											Toast.makeText(HomeworkFeedbackActivity.this, errorStr, Toast.LENGTH_SHORT)
													.show();
											hideLoadDialog();
											finishThis();
										}

									});

						}

						@Override
						public void uploadImgErrorListener(String errorStr) {
							Toast.makeText(HomeworkFeedbackActivity.this, errorStr, Toast.LENGTH_SHORT).show();
							hideLoadDialog();
							finishThis();
						}
					});
				} else if (addAudioList.size() > 0) {
					RecordUploadUtil.uploadRecord(HomeworkFeedbackActivity.this, addAudioList, new UploadImgListener() {

						@Override
						public void uploadImgSuccessListener(List<Integer> imgFileIdList) {
							
							if (imgFileIdList != null && imgFileIdList.size() > 0) {
								audioFileIds = new ArrayList<String>();
								for (int j = 0; j < imgFileIdList.size(); j++) {
									audioFileIds.add(imgFileIdList.get(j) + "_172");

								}
							}
							Message msg = new Message();
							msg.what = UPLOAD_PIC_AUDIO;
							handler.sendMessage(msg);
							
						}

						@Override
						public void uploadImgErrorListener(String errorStr) {
							Toast.makeText(HomeworkFeedbackActivity.this, errorStr, Toast.LENGTH_SHORT).show();
							hideLoadDialog();
							finishThis();
						}

					});
				} else if (addPicList.size() > 0) {
					ImgUploadUtil.uploadImgs(HomeworkFeedbackActivity.this, addPicList, new UploadImgListener() {

						@Override
						public void uploadImgSuccessListener(List<Integer> imgFileIdList) {
							
							if (imgFileIdList != null && imgFileIdList.size() > 0) {
								imgFileIds = new ArrayList<String>();
								for (int j = 0; j < imgFileIdList.size(); j++) {
									imgFileIds.add(imgFileIdList.get(j) + "_166");
								}
							}
							
							Message msg = new Message();
							msg.what = UPLOAD_PIC_AUDIO;
							handler.sendMessage(msg);
							

						}

						@Override
						public void uploadImgErrorListener(String errorStr) {
							Toast.makeText(HomeworkFeedbackActivity.this, errorStr, Toast.LENGTH_SHORT).show();
							hideLoadDialog();
							finishThis();
						}
					});
				}else {
					Message msg = new Message();
					msg.what = FILEID_CHANGE;
					handler.sendMessage(msg);
				}

			}
		}, 200);
	}

	public static final int UPLOADSTATUS = 0;
	public static final int UPLOAD_PIC_AUDIO = 1;
	public static final int FILEID_CHANGE = 2;
	public static final int UPLOAD_AUDIO_TEXT = 3;
	public static final int RECORD_AUDIO_TEXT = 4;
	private List<String> imgFileIds;
	private List<String> audioFileIds;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case UPLOADSTATUS:
				correctHomework();
				break;
			case UPLOAD_PIC_AUDIO:
				// 上传两种类型文件需要对资源id重新按照编辑时顺序排序
				for (int i = 0; i < tacherDataList.size(); i++) {
					if (tacherDataList.get(i).isLocal && tacherDataList.get(i).fileType != 3) {
						if (tacherDataList.get(i).fileType == 1&&tacherDataList.get(i).fileId.equals("")) {
							studentBean.correctHomeworkMaterialIds.add(imgFileIds.get(0));
							tacherDataList.get(i).fileId = imgFileIds.get(0).substring(0, imgFileIds.get(0).length()-4);
							imgFileIds.remove(0);
						} else if (tacherDataList.get(i).fileType == 2&&tacherDataList.get(i).fileId.equals("")) {
							studentBean.correctHomeworkMaterialIds.add(audioFileIds.get(0));
							tacherDataList.get(i).fileId = audioFileIds.get(0).substring(0, audioFileIds.get(0).length()-4);
							audioFileIds.remove(0);
						}
					}
				}

				correctHomework();
				break;
			case FILEID_CHANGE:
				correctHomework();
				break;
			case UPLOAD_AUDIO_TEXT:
				mImgHolder.recordTimeView.setText(playerTime + "s");
				break;
			case RECORD_AUDIO_TEXT:
				mAddHolder.tvRecordTime.setText(recordTime + "s");
				if (recordTime==MyConstant.HOMEWORK_RECORD_MAX_LENGTH) {
					mTimer.cancel();
					mTimer.purge();
					mTimer = null;
					stopRecord();
					return ;
				}
				break;
			default:
				break;
			}

		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				imgDatas = data.getStringArrayListExtra("img_data");
				if (imgDatas != null && imgDatas.size() > 0) {
					for (int i = 0; i < imgDatas.size(); i++) {
						FeedbackHomeworkFileBean bean = new FeedbackHomeworkFileBean(imgDatas.get(i), true, 1);
						tacherDataList.add(tacherDataList.size() - 2, bean);
					}
					imgBeanList.clear();
					for (int i = 0; i < tacherDataList.size(); i++) {
						if (tacherDataList.get(i).fileType == 1) {
							imgBeanList.add(tacherDataList.get(i));
						}
					}

					tAdapter.notifyDataSetChanged();
				}

			}else if (requestCode == 2) {
				String imagePath = data.getStringExtra("key_image_path");
				FeedbackHomeworkFileBean bean = new FeedbackHomeworkFileBean(imagePath, true, 1);
				tacherDataList.add(tacherDataList.size() - 2, bean);
				
				imgBeanList.clear();
				for (int i = 0; i < tacherDataList.size(); i++) {
					if (tacherDataList.get(i).fileType == 1) {
						imgBeanList.add(tacherDataList.get(i));
					}
				}
				tAdapter.notifyDataSetChanged();
			}
		}
	}

	private void setStudentInfo(int type) {
		if (studentBeanList != null && studentBeanList.size() > 0) {
			studentBean = studentBeanList.get(studentPosition);
			
			studentBeanTag = new StudentBean();
			studentBeanTag.sid = studentBean.sid;
			studentBeanTag.homeworkRate = studentBean.homeworkRate;
			studentBeanTag.rateStr = studentBean.rateStr;
			if (studentBean.remark.equals("null")) {
				studentBean.remark = "";
			}
			studentBeanTag.remark = studentBean.remark;
			studentBeanTag.correctHomeworkAudioList.addAll(studentBean.correctHomeworkAudioList);
			studentBeanTag.correctHomeworkPicList.addAll(studentBean.correctHomeworkPicList);
			studentBeanTag.correctHomeworkMaterialIds.addAll(studentBean.correctHomeworkMaterialIds);
			
		}
		

		if (type == 1) {
			for (int i = 0; i < textViewList.size(); i++) {
				if (studentBean.homeworkRate == i + 1) {
					relativeLayoutList.get(i).setBackgroundResource(R.drawable.shape_rank_stroke);
				} else {
					relativeLayoutList.get(i).setBackgroundResource(R.drawable.shape_rank_null);
				}

			}
		}
		
		
		if (studentBean.checkTime!=null && !studentBean.checkTime.equals("") && !studentBean.checkTime.equals("null")) {
			timeView.setText(studentBean.checkTime.substring(0, studentBean.checkTime.length()-3)+" 完成");
		}else {
			timeView.setText("未检查");
		}
		

		String studentImgUrl = studentBean.imgSavePath;
		if (studentImgUrl == null || studentImgUrl.isEmpty()) {
			studentImgUrl = studentBean.imgUrl;
		}

		if (studentImgUrl != null && studentImgUrl.startsWith("/")) {
			bitmap = BitmapFactory.decodeFile(studentImgUrl);
			if (bitmap != null) {
				studentImg.setImageBitmap(bitmap);
			} else {
				studentImg.setImageResource(R.drawable.default_student);
			}
		} else if (studentImgUrl != null ) {
			Picasso.with(this).load(studentImgUrl).error(R.drawable.default_student)
					.placeholder(R.drawable.default_student).fit().into(studentImg);
		} else {
			studentImg.setImageResource(R.drawable.default_student);
		}

		if (studentBean.empName != null) {
			studentName.setText(studentBean.empName);
		}
		
		if (studentPosition==0) {
			frontBtn.setVisibility(View.GONE);
			if (studentBeanList.size()==1) {
				nextBtn.setVisibility(View.GONE);
			}else {
				nextBtn.setVisibility(View.VISIBLE);
			}
			
		}else if (studentPosition==studentBeanList.size()-1) {
			nextBtn.setVisibility(View.GONE);
			if (studentBeanList.size()==1) {
				frontBtn.setVisibility(View.GONE);
			}else {
				frontBtn.setVisibility(View.VISIBLE);
			}
		}else {
			if (frontBtn.getVisibility()==View.GONE) {
				frontBtn.setVisibility(View.VISIBLE);
			}
			if (nextBtn.getVisibility()==View.GONE) {
				nextBtn.setVisibility(View.VISIBLE);
			}
		}
		if (!isFeedback) {
			frontBtn.setVisibility(View.GONE);
			nextBtn.setVisibility(View.GONE);
		}

		setTeacherData();
		setFamilyData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.front_b:
			
			toNextStudent(0);
			break;
		case R.id.next_b:
			toNextStudent(1);

			break;

		default:
			break;
		}

	}


	private void toNextStudent(int type) {
		if (isSoftShowing()) {
			closeKeyboard(teacherRemarkEditText);
		}

		toStudentType=type;
		
		studentBean.correctHomeworkPicList.clear();
		studentBean.correctHomeworkAudioList.clear();
		
		List<FeedbackHomeworkFileBean> tagBeanList=new ArrayList<FeedbackHomeworkFileBean>();
		for (int i = 0; i < studentBean.correctResList.size(); i++) {
			if (studentBean.correctResList.get(i).isLocal) {
				tagBeanList.add(studentBean.correctResList.get(i));
			}
		}
		
		if (tagBeanList.size()>0) {
			for (int i = 0; i < tagBeanList.size(); i++) {
				studentBean.correctResList.remove(tagBeanList.get(i));
			}
		}
		
		List<String> newPicList = new ArrayList<String>();
		List<String> newAudioList = new ArrayList<String>();

		if (tacherDataList.size() > 2) {
			for (int i = 0; i < tacherDataList.size(); i++) {
				if (tacherDataList.get(i).fileType == 1 && tacherDataList.get(i).isLocal) {
					studentBean.correctHomeworkPicList.add(tacherDataList.get(i));
					newPicList.add(tacherDataList.get(i).fileUrl);
				} else if (tacherDataList.get(i).fileType == 2 && tacherDataList.get(i).isLocal) {
					studentBean.correctHomeworkAudioList.add(tacherDataList.get(i));
					newAudioList.add(tacherDataList.get(i).fileUrl);
				}
				
				
				if (tacherDataList.get(i).isLocal && tacherDataList.get(i).fileType != 3) {
					studentBean.correctResList.add(tacherDataList.get(i));
				}

			}
		}

		studentBean.remark = teacherRemarkEditText.getText().toString();

		List<String> addPicList = new ArrayList<String>();
		List<String> addAudioList = new ArrayList<String>();

		List<String> tagPicList = new ArrayList<String>();
		List<String> tagAudioList = new ArrayList<String>();

		for (int i = 0; i < studentBeanTag.correctHomeworkPicList.size(); i++) {
			tagPicList.add(studentBeanTag.correctHomeworkPicList.get(i).fileUrl);
		}
		for (int i = 0; i < studentBeanTag.correctHomeworkAudioList.size(); i++) {
			tagAudioList.add(studentBeanTag.correctHomeworkAudioList.get(i).fileUrl);
		}
		

		if (!compare(newPicList, tagPicList)||!compare(newAudioList, tagAudioList) || !compare(studentBean.correctHomeworkMaterialIds, studentBeanTag.correctHomeworkMaterialIds)) {
			for (int i = 0; i < studentBean.correctHomeworkPicList.size(); i++) {
				if (studentBean.correctHomeworkPicList.get(i).fileId.equals("")) {
					addPicList.add(studentBean.correctHomeworkPicList.get(i).fileUrl);
				}
				
			}
			for (int i = 0; i < studentBean.correctHomeworkAudioList.size(); i++) {
				if (studentBean.correctHomeworkAudioList.get(i).fileId.equals("")) {
					addAudioList.add(studentBean.correctHomeworkAudioList.get(i).fileUrl);
				}
			}

			isEqual = true;
		} else {
			isEqual = false;
		}

		if (isEqual) {
			uploadCorrectHomeworkFile(addPicList, addAudioList);
		} else if (studentBean.homeworkRate != studentBeanTag.homeworkRate
				|| !studentBean.remark.equals(studentBeanTag.remark)) {
			correctHomework();
		} else {
			if (type==1) {//下一位
				if (studentPosition < studentBeanList.size() - 1) {
					studentPosition += 1;
					setStudentInfo(1);
				}
			}else if (type==0){//上一位
				if (studentPosition > 0) {
					studentPosition -= 1;
					setStudentInfo(1);
					
				}
			}else if (type==2){//返回键
				
				finishThis();
			}
			
		}
	}


	private Timer mTimer;
	private int recordTime;
	private MediaRecorder mMediaRecorder = null;
	// 录音所保存的文件
	private File mAudioFile;
	// 录音文件保存位置
	private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";
	private String recordPath;

	public void startRecord() {
		recordTime = 0;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				recordTime++;

				Message msg = new Message();
				msg.what = RECORD_AUDIO_TEXT;
				handler.sendMessage(msg);
				
			}
		}, 0, 1000);

		// TODO 录音
		
		mMediaRecorder = new MediaRecorder();
		
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		mMediaRecorder.setAudioSamplingRate(44100);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mMediaRecorder.setAudioEncodingBitRate(96000);
		recordPath = mFilePath + System.currentTimeMillis() + ".amr";
		Log.d("recordPath   :", recordPath);
		mAudioFile = new File(recordPath);
		// 创建父文件夹
		mAudioFile.getParentFile().mkdirs();

		try {
			mAudioFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 设置录音保存的文件
		mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());
		// 开始录音
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mMediaRecorder.start();
		// KeyPoint：setOnLongClickListener中return的值决定是否在长按后再加一个短按动作，true为不加短按,false为加入短按

	}

	//
	public void stopRecord() {
		
		
		if (recordTime < MyConstant.HOMEWORK_RECORD_MIN_LENGTH) {
			if (mAudioFile.exists()) {
				mAudioFile.delete();
			}
			Toast.makeText(HomeworkFeedbackActivity.this, "录音时间太短,请长按录音!", Toast.LENGTH_SHORT).show();
		} else {
			FeedbackHomeworkFileBean bean = new FeedbackHomeworkFileBean(recordPath, true, 2);
			bean.audioLength = recordTime;
			bean.fileIdparams="audioDuration="+recordTime;
			tacherDataList.add(tacherDataList.size() - 2, bean);
			tAdapter.notifyDataSetChanged();
		}

		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
			recordTime = 0;
		}

		if (mMediaRecorder != null) {
			try {
				// mMediaRecorder.stop();
				mMediaRecorder.reset();
			} catch (IllegalStateException e) {
				mMediaRecorder = null;
				mMediaRecorder = new MediaRecorder();
			}
			mMediaRecorder.release();
			mMediaRecorder = null;
		}

	}

	private MediaPlayer mediaPlayer;
	private Timer mPlayTimer;
	private int playerTime;
	private int audioItemTime;
	private boolean isPlaying;
	private ImageView audioPlayImgView;

	// 播放录音
	private void startPlayRecord(String path, int audioTime, final ImageView imgView) {
		this.audioPlayImgView=imgView;
		isPlaying=true;
		Log.d("path:", path);
		File file = null;
		if (path != null) {
			file = new File(path);
			mediaPlayer = new MediaPlayer();
			// 设置播放音频数据文件
			try {
				mediaPlayer.setDataSource(path);
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						

						playEndOrFail();
					}
				});
				mediaPlayer.setOnErrorListener(new OnErrorListener() {

					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						
						playEndOrFail();

						return true;
					}
				});
				mediaPlayer.setVolume(1, 1);
				// 是否循环播放
				mediaPlayer.setLooping(false);
				// 准备及播放
				mediaPlayer.prepare();
				mediaPlayer.start();
				updatePlayerTime(audioTime);
			} catch (IllegalArgumentException e) {
				playEndOrFail();
				e.printStackTrace();
			} catch (SecurityException e) {
				playEndOrFail();
				e.printStackTrace();
			} catch (IllegalStateException e) {
				playEndOrFail();
				e.printStackTrace();
			} catch (IOException e) {
				playEndOrFail();
				e.printStackTrace();
			}
		}
	}

	private void updatePlayerTime(int audioTime) {
		audioItemTime = audioTime;
		playerTime = audioTime;
		mPlayTimer = new Timer();
		mPlayTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (playerTime==0) {
					return;
				}
				playerTime--;

				Message msg = new Message();
				msg.what = UPLOAD_AUDIO_TEXT;
				handler.sendMessage(msg);
				
			}
		}, 0, 1000);

	}
	
	private void endPlay(){
		if (isPlaying) {
			playEndOrFail();
		}
	}

	// 停止播放或者播放异常处理
	private void playEndOrFail() {
		isPlaying=false;
		if (null != mediaPlayer) {
			mediaPlayer.setOnCompletionListener(null);
			mediaPlayer.setOnErrorListener(null);
			mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;

		}
		if (mPlayTimer != null) {
			mPlayTimer.cancel();
			mPlayTimer.purge();
			mPlayTimer = null;
			mImgHolder.recordTimeView.setText(audioItemTime + "s");
		}
		
		if (animDrawable!=null) {
			animDrawable.selectDrawable(0);
			animDrawable.stop();

			audioPlayImgView.setBackgroundResource(R.drawable.play_record_anim);
		}
		
	}

	private ProgressDialog progressDialog;
	private String homeworkId = "";
	private String classId = "";
	private StudentBean studentBeanTag;
	private List<TextView> textViewList;
	private int toStudentType;
	private boolean isEqual = true;
	private TextView timeView;
	private boolean dataIsChange;
	private LinearLayout rankLayout2;
	private LinearLayout.LayoutParams textParams2;
	private LinearLayout.LayoutParams textParams3;
	private List<RelativeLayout> relativeLayoutList;
	private LinearLayout.LayoutParams textParams4;
	private boolean isFeedback;
	private ScrollView scrollView;

	private void showLoadDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(HomeworkFeedbackActivity.this, "", "...loading...");
		}
	}

	private void hideLoadDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	public int dp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
	}

	public int sp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
	}

	public static <T extends Comparable<T>> boolean compare(List<T> a, List<T> b) {
		if (a.size() != b.size())
			return false;
		Collections.sort(a);
		Collections.sort(b);
		for (int i = 0; i < a.size(); i++) {
			if (!a.get(i).equals(b.get(i)))
				return false;
		}
		return true;
	}
	private boolean isSoftShowing() {
        //获取当前屏幕内容的高度
        int screenHeight = getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
 
        return screenHeight - rect.bottom != 0;
    }
	
	public void closeKeyboard(View view) {
	    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	@Override  
    public void onBackPressed() {  
//        super.onBackPressed();  
		toNextStudent(2);
    }  

	@Override
	protected void onDestroy() {
		endPlay();
		super.onDestroy();
	}
	
}
