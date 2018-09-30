package com.routon.smartcampus.student;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.routon.common.BaseFragment;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.net.NetWorkRequest;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.widget.PicSelHelper;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;
import com.routon.edurelease.R;
import com.routon.smartcampus.SmartCampusApplication;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.smartcampus.family.ColligateOpinionActivity;
import com.routon.smartcampus.attendance.StudentAttendanceActivity;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.ble.BleBroadcastManager;
import com.routon.smartcampus.ble.BleDevice;
import com.routon.smartcampus.ble.BleLog;
import com.routon.smartcampus.ble.BleScanCallback;
import com.routon.smartcampus.ble.BleScanRuleConfig;
import com.routon.smartcampus.ble.HexUtil;
import com.routon.smartcampus.flower.Badge;
import com.routon.smartcampus.flower.BadgeInfo;
import com.routon.smartcampus.flower.BadgeInfoUtil;
import com.routon.smartcampus.flower.BadgeSelectActivity;
import com.routon.smartcampus.flower.NewStudentBadgeActivity;
import com.routon.smartcampus.flower.StudentBadge;
import com.routon.smartcampus.gradetrack.SubjectExamActivity;
import com.routon.smartcampus.guideview.Guide;
import com.routon.smartcampus.guideview.GuideBuilder;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.utils.SpecialCharManage;
import com.routon.smartcampus.view.IndexBarView;
import com.routon.smartcampus.view.InitialIndexGridView;
import com.routon.smartcampus.view.PeriscopeLayout;
import com.routon.smartcampus.view.PeriscopeLayout.OnHeartAnimationUpdateListener;
import com.routon.smartcampus.view.PopupList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import ang.face.recognizer.FrActivity;

//小红花学生列表界面,综合评价学生列表界面
public class StudentListFragment extends BaseFragment implements OnClickListener, BleScanCallback {

	private String TAG = "StudentListFragment";
	// private ArrayList<String> mItems;
	// 存储首字母的起始位置的数组
	private ArrayList<Integer> mListSectionPos;
	// 存储排序后学生姓名的数组
	private ArrayList<String> mListItems;
	// 存储排序后学生首拼加姓名的数组
	private ArrayList<String> mNameInitialList;

	private ArrayList<String> mClassList;
	private int mSelClassIndex = 0;
	private ArrayList<StudentBean> mStudentDataList;
	private ArrayList<Badge> flowersList;

	private InitialIndexGridView mGridView;

	private StudentItemAdapter mAdaptor;

	private FaceRecognizeMgr mFaceRecongnizeMgr = null;
	private boolean isShow = false;

	public int mAppType = 0;
	private boolean mFamilyVersion = false;

	private PicSelHelper mPicSelHelper = null;
	private boolean isChooseShow = true;
	private ListView chooseListView;
	private FrameLayout dropdownF2;
	private List<String> chooseList;
	private View dropdownChooseMask;
	private ImageView chooseBtn;
	private boolean isSelectChooseAll = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_student, container, false);
		return view;

	}

	public void setFamilyVersion(boolean familyversion) {
		mFamilyVersion = familyversion;
	}

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
				// TODO Auto-generated method stub
				// 图片下载完毕后，先处理一遍，获取对应的facedata数据
				if (mAdaptor != null) {
					mAdaptor.notifyDataSetChanged();
				}

			}

		};
		// savepaths.add(mFaceRecongnizeMgr.getImageFilePath("1", "20170703",
		// "四年纪", "1班"));
		// savepaths.add(mFaceRecongnizeMgr.getImageFilePath("2", "20170703",
		// "四年纪", "1班"));
		// savepaths.add(mFaceRecongnizeMgr.getImageFilePath("3", "20170703",
		// "四年纪", "1班"));
		// savepaths.add(mFaceRecongnizeMgr.getImageFilePath("4", "20170703",
		// "四年纪", "1班"));
		// savepaths.add(mFaceRecongnizeMgr.getImageFilePath("5", "20170703",
		// "四年纪", "1班"));
		mFaceRecongnizeMgr.startDownloadClassImagesAndGetDatas(savepaths, urls, listener, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = this.getArguments();
		if (bundle != null) {

			// String groupIds=bundle.getString("class_id");

		}
		userId = InfoReleaseApplication.authenobjData.userId;
		mFaceRecongnizeMgr = FaceRecognizeMgr.getInstance(this.getContext());
		mFaceRecongnizeMgr.init(this.getContext());

		mFaceRecongnizeMgr.setFaceRecoginizeCompleteCb(new FaceRecognizeMgr.FaceRecoginizeCompleteCb() {

			@Override
			public void callback(String modelPath, String imagePath) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra(FrActivity.INTENT_MODEL_DIR_DATA, modelPath);
				intent.putExtra(FrActivity.INTENT_IMAGE_DIR_DATA, imagePath);
				// Log.d(TAG,"imagePath:"+imagePath);
				intent.setComponent(
						new ComponentName(getOwnActivity().getPackageName(), "ang.face.recognizer.FrActivity"));
				startActivityForResult(intent, 0);
			}
		});

		// 清除静态数据
		BadgeInfoUtil.clearDatas();

		initViews(getView());

		initData();

		initPicSel();
	}

	private void initPicSel() {
		mPicSelHelper = new PicSelHelper(this.getOwnActivity());
		// 临时图片保存路径
		File tmpFile = new File(getOwnActivity().getExternalCacheDir(), "temp.png");
		tmpFile.delete();
		mPicSelHelper.setDestUri(Uri.fromFile(tmpFile));

		mPicSelHelper.setCutImageMaxSize(354, 472);
	}

	private void initData() {

		/*
		 * oftenBadgeList = new ArrayList<BadgeInfo>(); OftenBadgeBean badgeBean
		 * = new OftenBadgeBean("答题正确", "课堂上积极回答问题，且回答正确回答正确。",
		 * "http://172.16.51.24:8876/picture/res-163-20180306-135641-s0n16.png",
		 * 10, 1); OftenBadgeBean badgeBean2 = new OftenBadgeBean("正确",
		 * "课堂上积极回答问题，学习成绩优异！",
		 * "http://172.16.51.24:8876/picture/res-163-20180306-135641-s0n16.png",
		 * 10, 1);
		 * 
		 * oftenBadgeList.add(badgeBean); oftenBadgeList.add(badgeBean2);
		 * oftenBadgeList.add(badgeBean); oftenBadgeList.add(badgeBean2);
		 * oftenBadgeList.add(badgeBean);
		 */

		mListSectionPos = new ArrayList<Integer>();
		mListItems = new ArrayList<String>();
		chooseList = new ArrayList<String>();
		chooseList.add("全选");
		chooseList.add("取消");
		getClassListData();

		// getOfflineStudentListData("");
	}

	private Guide showHightLight(View view) {
		final GuideBuilder builder = new GuideBuilder();
		builder.
		// 设置要高亮显示的View
				setTargetView(view)
				// 设置遮罩透明度(0-255)
				.setAlpha(150)
				// 是否覆盖目标View,默认flase
				.setOverlayTarget(false)
				// 设置进出动画
				.setEnterAnimationId(android.R.anim.fade_in).setExitAnimationId(android.R.anim.fade_out)
				// 外部是否可点击取消遮罩,true为不可取消
				.setOutsideTouchable(false);
		// 设置遮罩监听
		builder.setOnVisibilityChangedListener(new GuideBuilder.OnVisibilityChangedListener() {
			@Override
			public void onShown() {
				// 显示遮罩时触发
			}

			@Override
			public void onDismiss() {
				// 显示下一个遮罩

			}
		});

		Guide guide = builder.createGuide();
		// 检测定位非0
		guide.setShouldCheckLocInWindow(false);
		// 显示
		guide.show(StudentListFragment.this.getActivity());
		return guide;
	}

	void setPopupListAndHightLight(View view, int position) {
		final Guide guide = showHightLight(view);
		showPopupList(view, position, BadgeInfoUtil.getCustomFlowers(), new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				Log.d("studentListFragment", "onDismiss");
				isMore = true;
				guide.dismiss();
				isShow = false;
				listterner.process(isShow);
				// window.dismiss();
			}
		});

	}

	private boolean isShowViewTop = false;

	private void initViews(View view) {
		final PullToRefreshInitialIndexGridView pullToRefreshInitialIndexGridView = (PullToRefreshInitialIndexGridView) view
				.findViewById(R.id.list_view);
		mGridView = pullToRefreshInitialIndexGridView.getRefreshableView();
		// mGridView.setNumColumns(3);
		mGridView.setHorizontalSpacing(
				(int) this.getContext().getResources().getDimension(R.dimen.student_grid_horizontal_space));
		mGridView.setVerticalSpacing(
				(int) this.getContext().getResources().getDimension(R.dimen.student_grid_vertical_space));
		mGridView.setPadding((int) this.getContext().getResources().getDimension(R.dimen.student_grid_padding_left), 0,
				(int) this.getContext().getResources().getDimension(R.dimen.student_grid_padding_right), 0);

		classTextView = (TextView) view.findViewById(R.id.tv_class);
		LinearLayout classSelView = (LinearLayout) view.findViewById(R.id.tv_class_ll);
		ImageView faceIdentifyView = (ImageView) view.findViewById(R.id.iv_face_identify);
		ImageView QRScannerView = (ImageView) view.findViewById(R.id.iv_qr_scanner);
		periscopeLayout = (PeriscopeLayout) view.findViewById(R.id.periscope);

		dropdownFl = (FrameLayout) view.findViewById(R.id.dropdown_fl);
		classListView = (ListView) view.findViewById(R.id.dropdown_listview);
		dropdownMask = view.findViewById(R.id.dropdown_mask);
		dropdownF2 = (FrameLayout) view.findViewById(R.id.choose_dropdown_fl);
		chooseListView = (ListView) view.findViewById(R.id.choose_dropdown_listview);
		dropdownChooseMask = view.findViewById(R.id.choose_dropdown_mask);
		chooseBtn = (ImageView) view.findViewById(R.id.iv_choose_btn);
		dropdownChooseMask.setOnClickListener(this);
		dropdownMask.setOnClickListener(this);
		classSelView.setOnClickListener(this);
		faceIdentifyView.setOnClickListener(this);
		QRScannerView.setOnClickListener(this);

		if (mAppType == MyBundleName.TYPE_STUDENT_SCORE || mAppType == MyBundleName.TYPE_STUDENT_OPINION) {
			ImageView backImageView = (ImageView) view.findViewById(R.id.backIv);
			backImageView.setVisibility(View.VISIBLE);
			backImageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					getOwnActivity().finish();
				}
			});
			dropdownFl.setPadding((int) this.getContext().getResources().getDimension(R.dimen.title_bar_btn_w)
					+ dropdownFl.getPaddingLeft(), 0, 0, 0);
		}

		if (mAppType == MyBundleName.TYPE_STUDENT_FLOWER) {

			chooseBtn.setVisibility(View.VISIBLE);
			chooseBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// dropdownChooseClick();
					isSelectChooseAll = !isSelectChooseAll;
					if (isSelectChooseAll) {
						chooseBtn.setImageResource(R.drawable.flower_choose_normal);
						for (int i = 0; i < mDataList.size(); i++) {
							mDataList.get(i).isSelect = false;
							mDataList.get(i).currentIndex = -1;
						}
						selectStudents.clear();
						macList.clear();
						
						if (mAdaptor != null) {
							mAdaptor.notifyDataSetChanged();
						}
					} else {
						chooseBtn.setImageResource(R.drawable.flower_choose_preesed);
						selectStudents.clear();
						macList.clear();
						for (int i = 0; i < mDataList.size(); i++) {
							mDataList.get(i).isSelect = true;
							mDataList.get(i).currentIndex = i;
							selectStudents.add(mDataList.get(i));
							if (getMac(mDataList.get(i).ctrlId)!=null) {
								macList.add(getMac(mDataList.get(i).ctrlId));
							}
							
						}

						if (mAdaptor != null) {
							mAdaptor.notifyDataSetChanged();
						}
					}
				}
			});
		}
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);

		final int screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;

		pullToRefreshInitialIndexGridView.setOnRefreshListener(new OnRefreshListener2<InitialIndexGridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<InitialIndexGridView> refreshView) {
				getStudentListData(mAllStudentDataList[mSelClassIndex].groupId, mSelClassIndex, mSelClassIndex);
				pullToRefreshInitialIndexGridView.onRefreshComplete();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<InitialIndexGridView> refreshView) {
				pullToRefreshInitialIndexGridView.onRefreshComplete();
			}

		});

		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mAppType == MyBundleName.TYPE_STUDENT_FLOWER) {
					if (((StudentListActivity) getActivity()).isCanShow) {

						int[] location = new int[2];
						view.getLocationOnScreen(location);
						float viewHeight = location[1] + view.getHeight() / 2;

						if (viewHeight > screenHeight / 6 && viewHeight < screenHeight - dp2px(95)) {

							isShow = true;
							listterner.process(isShow);
							if (viewHeight > 500) {
								isShowViewTop = true;
							} else {
								isShowViewTop = false;
							}
							setPopupListAndHightLight(view, position);
						}
					}
				} else if (mAppType == MyBundleName.TYPE_STUDENT_ATTENDANCE) {
					Intent intent = new Intent(getContext(), StudentAttendanceActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString(MyBundleName.STUDENT_APP_TYPE, MyBundleName.TYPE_ATTENDANCE);
					bundle.putString(MyBundleName.STUDENT_NAME, mDataList.get(position).empName);
					bundle.putInt(MyBundleName.STUDENT_ID, mDataList.get(position).sid);
					intent.putExtras(bundle);
					startActivity(intent);
				} else if (mAppType == MyBundleName.TYPE_STUDENT_SCORE) {
					Intent intent = new Intent(getActivity(), SubjectExamActivity.class);
					intent.putExtra(MyBundleName.STUDENT_BEAN, mDataList.get(position));
					intent.putExtra(MyBundleName.TYPE, mAppType);
					startActivity(intent);
				} else if (mAppType == MyBundleName.TYPE_STUDENT_OPINION) {
					Intent intent = new Intent(getActivity(), ColligateOpinionActivity.class);
					intent.putExtra(MyBundleName.STUDENT_BEAN, mDataList.get(position));
					intent.putExtra(MyBundleName.TYPE, mAppType);
					startActivity(intent);
				}

			}

		});

		classListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				chooseBtn.setImageResource(R.drawable.flower_choose_normal);
				isSelectChooseAll = true;
				mSelClassIndex = position;
				macList.clear();
				listViewOutAnim();
				classTextView.setText(mClassList.get(position));
				mGridView.setAdapter(null);
				if (mAllStudentDataList != null && mAllStudentDataList.length > position
						&& mAllStudentDataList[position] != null) {
					getStudentListData(mAllStudentDataList[position].groupId, position, position);
				} else {
					initStudentList(null, null);
				}

				// if( mAllStudentDataList[position] != null ){
				// initStudentList(mAllStudentDataList[position].studentdatalist,null);
				// }else{
				// initStudentList(null,null);
				// }

				// getStudentListData(String.valueOf(classGroups.get(position).getId()));
			}
		});
		chooseListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				listViewChooseOutAnim();
				switch (position) {
				case 0:
					selectStudents.clear();
					for (int i = 0; i < mDataList.size(); i++) {
						mDataList.get(i).isSelect = true;
						selectStudents.add(mDataList.get(i));
					}

					if (mAdaptor != null) {
						mAdaptor.notifyDataSetChanged();
					}
					break;
				case 1:
					for (int i = 0; i < mDataList.size(); i++) {
						mDataList.get(i).isSelect = false;
					}
					selectStudents.clear();
					if (mAdaptor != null) {
						mAdaptor.notifyDataSetChanged();
					}
				}
			}
		});
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!isBleStart) {
			startBle();
		}
		
		// getFtpUrl();
		((StudentListActivity) getActivity()).isCanClick = false;

		if (selectStudents != null && selectStudents.size() > 0) {
			for (int i = 0; i < selectStudents.size(); i++) {
				if (selectStudents.get(i).isClick == true) {
					selectStudents.remove(i);
				}
			}
		}
		if (mDataList != null && mDataList.size() > 0) {
			for (int i = 0; i < mDataList.size(); i++) {
				if (mDataList.get(i).isClick == true) {
					mDataList.get(i).isSelect = false;
				}
				mDataList.get(i).isClick = false;
			}
		}
	}

	@Override
	public void onStop() {
		if (isBleStart) {
			stopBle();
		}
		
		super.onStop();
	}
	
	private void initStudentList(ArrayList<StudentBean> datalist, PoplulateCompleteCB cb) {
		// showLoadDialog();
		mStudentDataList = datalist;
		ArrayList<String> items = new ArrayList<String>();
		if (mStudentDataList != null) {
			for (int i = 0; i < mStudentDataList.size(); i++) {
				items.add(mStudentDataList.get(i).empName);
			}
		}
		new Poplulate(cb).execute(items);
	}

	private void getStudentListData(final String groupIds, final int group_index, final int update_index) {// 获取指定班级学生列表
		String urlString = SmartCampusUrlUtils.getStudentListUrl() + "&groupIds=" + groupIds;
		showLoadDialog();
		mGetStudentListDataNum++;
		Log.d(TAG, "getStudentListData urlString:" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						if (getOwnActivity() == null) {
							return;
						}
						Log.d(TAG, "response mGetStudentListDataNum:" + mGetStudentListDataNum + ",group_index:"
								+ group_index + ",update_index：" + update_index);
						mGetStudentListDataNum--;
						try {
							if (response.getInt("code") == 0) {
								ArrayList<StudentBean> studentdatalist = new ArrayList<StudentBean>();
								JSONArray array = response.optJSONArray("datas");
								if (array != null) {
									int len = array.length();
									for (int i = 0; i < len; i++) {
										JSONObject obj = (JSONObject) array.get(i);
										StudentBean bean = new StudentBean(obj);
										studentdatalist.add(bean);
										// Log.d(TAG, "emp name=" +
										// bean.empName+",image save
										// path:"+bean.imgSavePath);

									}
									downloadStudentImage(studentdatalist);
									mAllStudentDataList[group_index].studentdatalist = studentdatalist;
								}
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(getActivity());
								hideLoadDialog();
							} else {
								Log.e(TAG, response.getString("msg"));
								reportToast(response.getString("msg"));
								hideLoadDialog();

							}

							// 全部学生数据获取完毕
							if (mGetStudentListDataNum == 0) {
								if (mAllStudentDataList[update_index] == null
										|| mAllStudentDataList[update_index].studentdatalist == null
										|| mAllStudentDataList[update_index].studentdatalist.size() == 0) {
									initStudentList(null, null);
									reportToast("该班级未录入学生数据!");
								} else {
									initStudentList(mAllStudentDataList[update_index].studentdatalist, null);
									getAgentList(mAllStudentDataList[update_index].groupId);
								}
								if (mAppType != MyBundleName.TYPE_STUDENT_SCORE
										&& mAppType != MyBundleName.TYPE_STUDENT_OPINION) {
									if (isRefreshFlowers) {
										refreshData();
									} else {
										hideLoadDialog();
									}
								} else {
									hideLoadDialog();
								}
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideLoadDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if (getOwnActivity() == null) {
							return;
						}
						hideLoadDialog();
						Log.d(TAG, "onErrorResponse=" + arg0 + ",group_index:" + group_index + ",update_index："
								+ update_index);
						mGetStudentListDataNum--;

						// 全部学生数据获取完毕
						if (mGetStudentListDataNum == 0) {

						}
						if (group_index == update_index) {
							// 先判断网络状况
							if (true == InfoReleaseApplication.showNetWorkFailed(getOwnActivity())) {
								reportToast(R.string.get_student_data_failed);
							}
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	private int getClassIndex(String savepath, int sid) {
		if (sid > 0) {
			for (int i = 0; i < mAllStudentDataList.length; i++) {
				ClassStudentData data = mAllStudentDataList[i];
				if (data != null) {
					if (null != data.studentdatalist) {
						for (StudentBean studentBean : data.studentdatalist) {
							if (sid == studentBean.sid) {
								return i;
							}
						}
					}
				}
			}
			return -1;
		}
		if (savepath == null || savepath.isEmpty() == true) {
			return -1;
		}
		for (int i = 0; i < mAllStudentDataList.length; i++) {
			ClassStudentData data = mAllStudentDataList[i];
			if (data != null) {
				if (null != data.studentdatalist) {
					for (StudentBean studentBean : data.studentdatalist) {
						int it = studentBean.imgSavePath.lastIndexOf("/");
						// Log.d(TAG,"studentBean.imgSavePath:"+studentBean.imgSavePath);
						String str = studentBean.imgSavePath.substring(it + 1, studentBean.imgSavePath.length());
						if (str.equals(savepath)) {
							return i;
						}
					}
				}
			}
		}
		return -1;
	}

	private void scrollToStudent(String path, int sid) {
		for (int i = 0; i < mDataList.size(); i++) {
			StudentBean studentBean = mDataList.get(i);

			int it = studentBean.imgSavePath.lastIndexOf("/");
			String str = studentBean.imgSavePath.substring(it + 1, studentBean.imgSavePath.length());
			boolean ret = false;
			if (path != null) {
				ret = str.equals(path);
			} else {
				ret = (sid == studentBean.sid);
			}
			if (ret) {
				mGridView.setSelection(i);
				final int position = i;
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Log.d("studentlistfragment", " onActivityResult position:" + position);
						View view = mGridView.getChildAt(position - mGridView.getFirstVisiblePosition());
						if (view != null) {
							if (mAppType == MyBundleName.TYPE_STUDENT_ATTENDANCE) {
								Intent intent = new Intent(getContext(), StudentAttendanceActivity.class);
								Bundle bundle = new Bundle();
								bundle.putString(MyBundleName.STUDENT_APP_TYPE, MyBundleName.TYPE_ATTENDANCE);
								bundle.putString(MyBundleName.STUDENT_NAME, mDataList.get(position).empName);
								bundle.putInt(MyBundleName.STUDENT_ID, mDataList.get(position).sid);
								intent.putExtras(bundle);
								startActivity(intent);
							} else if (mAppType == MyBundleName.TYPE_STUDENT_SCORE) {
								Intent intent = new Intent(getActivity(), SubjectExamActivity.class);
								intent.putExtra(MyBundleName.STUDENT_BEAN, mDataList.get(position));
								intent.putExtra(MyBundleName.TYPE, mAppType);
								startActivity(intent);
							} else if (mAppType == MyBundleName.TYPE_STUDENT_OPINION) {
								Intent intent = new Intent(getActivity(), ColligateOpinionActivity.class);
								intent.putExtra(MyBundleName.STUDENT_BEAN, mDataList.get(position));
								intent.putExtra(MyBundleName.TYPE, mAppType);
								startActivity(intent);
							} else {
								// setPopupListAndHightLight(view, position);
								if (((StudentListActivity) getActivity()).isCanShow) {

									int[] location = new int[2];
									view.getLocationOnScreen(location);
									float viewHeight = location[1] + view.getHeight() / 2;

									if (viewHeight > screenHeight / 6 && viewHeight < screenHeight - dp2px(95)) {

										isShow = true;
										listterner.process(isShow);
										if (viewHeight > 500) {
											isShowViewTop = true;
										} else {
											isShowViewTop = false;
										}
										setPopupListAndHightLight(view, position);
									}
								}
							}

						}
					}
				}, 100);
				reportToast("识别成功，找到这个学生");
				break;
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			Log.d("studentlistfragment", " onActivityResult resultCode:" + resultCode);
			if (resultCode == Activity.RESULT_OK) {
				final String path = data.getStringExtra(FrActivity.INTENT_PATH_DATA);
				final int sid = data.getIntExtra(StudentCaptureActivity.INTENT_SID_DATA, -1);
				Log.d("studentlistfragment", " onActivityResult path:" + path);
				int classIndex = getClassIndex(path, sid);
				Log.d("studentlistfragment", " onActivityResult classIndex:" + classIndex);
				if (classIndex >= 0) {
					int curPos = mSelClassIndex;
					if (curPos != classIndex) {// 这个学生不在当前班级中
						classListView.setSelection(classIndex);
						mSelClassIndex = classIndex;
						classTextView.setText(mClassList.get(classIndex));
						mGridView.setAdapter(null);
						if (mAllStudentDataList[classIndex] != null) {
							initStudentList(mAllStudentDataList[classIndex].studentdatalist, new PoplulateCompleteCB() {

								@Override
								public void complete() {
									// TODO Auto-generated method stub
									Handler handler = new Handler();
									handler.postDelayed(new Runnable() {
										@Override
										public void run() {
											scrollToStudent(path, sid);
										}
									}, 100);
								}

							});
						} else {
							initStudentList(null, null);
						}
					} else {
						scrollToStudent(path, sid);
					}
				} else {
					reportToast("识别成功，在当前所有班级中找不到这个学生");
				}
			}
		}

		if (requestCode == MyBundleName.STUDENT_BADGE_SELECT_RESULT) {
			// getStudentBadgeInfo(mDataList.get(mContextPosition));
			if (resultCode == Activity.RESULT_OK) {
				chooseBtn.setImageResource(R.drawable.flower_choose_normal);
				isSelectChooseAll = true;
				dropdownMask.setVisibility(View.VISIBLE);
				final int badgeCount = data.getIntExtra(MyBundleName.STUDENT_BADGE_SELECT_COUNT, 0);
				final ArrayList<String> badgeUrls = data
						.getStringArrayListExtra(MyBundleName.STUDENT_BADGE_SELECT_URLS);
				final ArrayList<String> badgeNames = data
						.getStringArrayListExtra(MyBundleName.STUDENT_BADGE_SELECT_NAME);
				final int badgeIntegral = data.getIntExtra(MyBundleName.STUDENT_BADGE_SELECT_INTEGRAL, 0);
				setBadgeCount(badgeCount, badgeIntegral);

				int[] location = new int[2];
				Handler handler = new Handler();
				for (int j = 0; j < selectStudents.size(); j++) {
					final StudentBean studentBean = selectStudents.get(j);
					if (studentBean.currentIndex < mGridView.getFirstVisiblePosition()
							|| studentBean.currentIndex > mGridView.getLastVisiblePosition()) {
						studentBean.isSelect = false;// 仅设置非选中状态，不删除
						continue;
					}
					selView = mGridView.getChildAt(studentBean.currentIndex - mGridView.getFirstVisiblePosition());
					selView.getLocationOnScreen(location);
					final float viewWidth = selView.getWidth() / 2;
					final float x = location[0] + selView.getWidth() / 2;
					final float y = location[1] + selView.getHeight() - (selView.getHeight() / 3);

					for (int i = 0; i < badgeUrls.size(); i++) {
						final String badgeUrl = badgeUrls.get(i);
						final String badgeName = badgeNames.get(i);
						final int tag = i;
						int timer = 200 * i;
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (tag < badgeUrls.size() - 1) {
									periscopeLayout.addHeart(x, y, viewWidth, getContext(), badgeUrl, badgeCount,
											badgeName, studentBean);
								} else {
									periscopeLayout.addHeart(x, y, viewWidth, getContext(), badgeUrl, badgeCount,
											badgeName, studentBean);
								}

								periscopeLayout.setOnHeartAnimationUpdateListener(new OnHeartAnimationUpdateListener() {

									@Override
									public void onAnimationEnd(StudentBean bean) {
										// TODO Auto-generated method stub
										selectStudents.remove(bean);
										bean.isSelect = false;
										if (selectStudents.size() == 0) {
											dropdownMask.setVisibility(View.GONE);
											resetSelectStudents();
										} else {// 是否颁发完成
											boolean flag = true;
											for (int k = 0; k < selectStudents.size(); k++) {
												StudentBean stb = selectStudents.get(k);
												if (stb.isSelect) {
													flag = false;
													break;
												} else {
													selectStudents.remove(stb);
												}
											}

											if (flag) {
												dropdownMask.setVisibility(View.GONE);
												resetSelectStudents();
											}
										}
									}
								});
							}

						}, timer);
					}
				}
			}

		}

		if (true == mPicSelHelper.handleActivityResult(requestCode, resultCode, data)) {
			return;
		}
		if (requestCode == PicSelHelper.PHOTO_CUT) {
			if (resultCode == Activity.RESULT_OK) {
				if (mPicSelHelper.getImageUri() != null) {
					sendProfileImage(mPicSelHelper.getImageUri().getPath());
				}
			}
		}

		if (requestCode == MyBundleName.STUDENT_BADGE_SELECT_RESULT) {

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	// 头像上传
	private boolean sendProfileImage(final String path) {
		File file = new File(path);
		if (file.exists() == false) {
			this.reportToast("请选择图片");
			return false;
		}
		showLoadDialog();
		String urlString = SmartCampusUrlUtils.getUpdatePhotoUrl();
		Log.i(TAG, "sendProfileImage URL:" + urlString);
		Map<String, File> files = new HashMap<String, File>();
		Map<String, String> params = new HashMap<String, String>();
		params.put("sid", String.valueOf(mDataList.get(mContextPosition).sid));
		files.put("photo", file);
		NetWorkRequest.UploadFiles(getOwnActivity(), urlString, files, params, new Listener<String>() {
			@Override
			public void onResponse(String response) {
				if (getOwnActivity() == null) {
					return;
				}
				hideLoadDialog();

				BaseBean bean = BaseBeanParser.parseBaseBean(response);
				if (bean == null) {
					reportToast("上传照片失败!");
					hideLoadDialog();
					return;
				}
				Log.d(TAG, "sendProfileImage:" + response);
				if (bean.code == 0) {
					reportToast("上传照片成功!");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					String date = sdf.format(new java.util.Date());

					StudentBean studentBean = mDataList.get(mContextPosition);
					studentBean.imgSavePath = FaceRecognizeMgr.getImageFilePath(String.valueOf(studentBean.sid), date,
							studentBean.grade, studentBean.staffGroup);
					ImageUtils.copyFile(path, studentBean.imgSavePath);
					mAdaptor.notifyDataSetChanged();
				} else {
					if (bean.msg != null && bean.msg.isEmpty() == false) {
						reportToast(bean.msg);
					} else {
						reportToast("上传照片失败!");
					}
				}
			}

		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				if (getOwnActivity() == null) {
					return;
				}
				hideLoadDialog();
				if (true == InfoReleaseApplication.showNetWorkFailed(getOwnActivity())) {
					reportToast("上传头像失败!");
				}
			}

		}, null);
		return true;
	}

	private int mStaffUserAgentId = -1;

	void getAgentList(String groupId) {
		String urlString = SmartCampusUrlUtils.getAgentListUrl(groupId);
		Log.d(TAG, "getAgentList urlString:" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "getAgentList response:" + response);
						if (getOwnActivity() == null) {
							return;
						}
						int code = response.optInt("code");
						if (code == 0) {
							JSONArray datas = response.optJSONArray("datas");
							if (datas != null) {
								JSONObject jsonobj = datas.optJSONObject(0);
								if (jsonobj != null) {
									mStaffUserAgentId = jsonobj.optInt("staffUserAgentId");
									int sid = jsonobj.optInt("sid");
									for (int i = 0; i < mStudentDataList.size(); i++) {
										if (mStudentDataList.get(i).sid == sid) {
											mStudentDataList.get(i).isStaffUserAgent = true;
											mAdaptor.notifyDataSetChanged();
											break;
										}
									}
								}
							}
						} else if (code == -2) {
							InfoReleaseApplication.returnToLogin(getOwnActivity());
						} else {
							reportToast("获取代理人列表失败");
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if (getOwnActivity() == null) {
							return;
						}
						Log.d(TAG, "getAgentList arg0:" + arg0);
						if (true == InfoReleaseApplication.showNetWorkFailed(getOwnActivity())) {
							reportToast("获取代理人列表失败");
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	void showNewAgentStudentDialog(String username, String pwd) {
		new AlertDialog.Builder(getOwnActivity()).setMessage("授权代理人用户名:" + username + ",\n登录密码:" + pwd)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				}).show();
	}

	void addAgent(final StudentBean bean) {
		if (bean == null)
			return;
		String studentId = String.valueOf(bean.sid);
		String urlString = SmartCampusUrlUtils.getAgentAddUrl(studentId);
		Log.d(TAG, "addAgent urlString:" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "addAgent response:" + response);
						if (getOwnActivity() == null) {
							return;
						}
						int code = response.optInt("code");
						if (code == 0) {
							JSONObject jsonobj = response.optJSONObject("datas");
							if (jsonobj != null) {
								mStaffUserAgentId = jsonobj.optInt("staffUserAgentId");
							}
							String username = jsonobj.optString("username");
							String pwd = jsonobj.optString("pwd");
							bean.isStaffUserAgent = true;
							mAdaptor.notifyDataSetChanged();
							hideLoadDialog();
							showNewAgentStudentDialog(username, pwd);
						} else if (code == -2) {
							InfoReleaseApplication.returnToLogin(getOwnActivity());
						} else {
							reportToast("新增代理人失败");
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if (getOwnActivity() == null) {
							return;
						}
						Log.d(TAG, "addAgent arg0:" + arg0);
						if (true == InfoReleaseApplication.showNetWorkFailed(getOwnActivity())) {
							reportToast("新增代理人失败");
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	void cancelAgent(final StudentBean bean, final boolean cancelAfterGetData) {
		Log.d(TAG, "cancelAgent mStaffUserAgentId:" + mStaffUserAgentId);
		if (mStaffUserAgentId < 0) {
			return;
		}
		String urlString = SmartCampusUrlUtils.getAgentCancelUrl(String.valueOf(mStaffUserAgentId));
		Log.d(TAG, "cancelAgent urlString:" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "cancelAgent response:" + response);
						if (getOwnActivity() == null) {
							return;
						}
						int code = response.optInt("code");
						if (code == 0) {
							bean.isStaffUserAgent = false;
							mAdaptor.notifyDataSetChanged();
							if (cancelAfterGetData == true) {
								mStaffUserAgentId = -1;
								hideLoadDialog();
							}
							reportToast("取消代理人" + bean.empName);
						} else if (code == -2) {
							InfoReleaseApplication.returnToLogin(getOwnActivity());
						} else {
							reportToast("取消代理人失败");
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if (getOwnActivity() == null) {
							return;
						}
						Log.d(TAG, "cancelAgent arg0:" + arg0);
						if (true == InfoReleaseApplication.showNetWorkFailed(getOwnActivity())) {
							reportToast("取消代理人失败");
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	void startFaceActivity() {
		mFaceRecongnizeMgr.checkModuleFile();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_class_ll:// 班级选择
			dropdownClick();
			break;
		case R.id.iv_face_identify:// 人脸识别
			startFaceActivity();
			// Intent intent=new Intent(getContext(),
			// CorrectHomeworkActivity.class);
			// getActivity().startActivity(intent);
			break;
		case R.id.iv_qr_scanner:// 二维码扫描R.id.dropdown_mask
		{
			Intent intent = new Intent(getContext(), StudentCaptureActivity.class);
			getActivity().startActivityForResult(intent, 0);
		}
			break;
		case R.id.dropdown_mask:
			if (!isClassListShow) {
				listViewOutAnim();
			}

			break;
		case R.id.choose_dropdown_mask:
			if (!isChooseShow) {
				listViewChooseOutAnim();
			}
			break;

		default:
			break;
		}
	}

	private int mContextPosition;
	protected View mContextView;
	private boolean isMore = true;

	private PopupList showPopupList(View anchorView, int contextPosition, ArrayList<BadgeInfo> imgList,
			PopupWindow.OnDismissListener listener) {
		List<String> popupMenuItemList = new ArrayList<String>();
		final StudentBean studentBean = mDataList.get(contextPosition);
		if (mAppType == MyBundleName.TYPE_STUDENT_FLOWER) {
			popupMenuItemList.add("颁发");
			popupMenuItemList.add("查询");
			popupMenuItemList.add("兑奖");
			popupMenuItemList.add("更多");

		}

		int[] location = new int[2];
		anchorView.getLocationOnScreen(location);
		final float viewWidth = anchorView.getWidth() / 2;
		final float x = location[0] + anchorView.getWidth() / 2;
		float y = 0;
		if (isShowViewTop) {
			y = location[1] + anchorView.getHeight() / 2;
		} else {
			y = location[1] + (anchorView.getHeight()) + (anchorView.getHeight() / 6);
		}

		selView = anchorView;
		final PopupList popupList = new PopupList(this.getContext());
		popupList.setTextSize(sp2px(15));
		popupList.setTextPaddingLeft(sp2px(10));
		popupList.setTextPaddingRight(sp2px(10));
		popupList.setOnDismissListener(listener);
		popupList.setStaffUserAgent(studentBean.isStaffUserAgent);
		popupList.showPopupListWindow(anchorView, contextPosition, x, y, imgList, popupMenuItemList, isShowViewTop,
				new PopupList.PopupListListener() {

					@Override
					public void onPopupListClick(View contextView, int contextPosition, int position) {

						StudentListFragment.this.mContextPosition = contextPosition;
						StudentListFragment.this.mContextView = contextView;
						switch (position) {
						// case 0:// 你真棒
						//
						// periscopeLayout.addHeart(x,
						// y,viewWidth,getContext());
						//
						// dropdownMask.setVisibility(View.VISIBLE);
						//
						// issueBadge(mStudentDataList.get(contextPosition).sid);
						//
						// break;
						case 0:
							if (mAppType == MyBundleName.TYPE_STUDENT_FLOWER) {// 颁发

								if (studentBean.isSelect == false) {
									studentBean.isClick = true;
									studentBean.isSelect = true;
									studentBean.currentIndex = contextPosition;
									selectStudents.add(studentBean);
								}
								Intent intent = new Intent(getContext(), BadgeSelectActivity.class);
								Bundle bundle = new Bundle();
								bundle.putSerializable(MyBundleName.MULTI_STUDENT_BEANS, selectStudents);
								bundle.putSerializable("badge_list", flowersList);
								intent.putExtras(bundle);
								startActivityForResult(intent, MyBundleName.STUDENT_BADGE_SELECT_RESULT);
							} else {// 缺勤记录
								Intent intent = new Intent(getContext(), StudentAttendanceActivity.class);
								Bundle bundle = new Bundle();

								bundle.putString(MyBundleName.STUDENT_NAME, mDataList.get(contextPosition).empName);
								bundle.putInt(MyBundleName.STUDENT_ID, mDataList.get(contextPosition).sid);
								bundle.putString(MyBundleName.STUDENT_APP_TYPE, MyBundleName.TYPE_ATTENDANCE);
								intent.putExtras(bundle);
								startActivity(intent);

							}
							break;
						case 1:// 查询
							Intent queryIntent = new Intent(getContext(), NewStudentBadgeActivity.class);
							Bundle queryBundle = new Bundle();
							// queryBundle.putSerializable("student_bean",
							// mDataList.get(contextPosition));
							queryBundle.putInt("userId", userId);
							queryBundle.putBoolean(MyBundleName.FAMILY_VERSION, mFamilyVersion);
							queryBundle.putInt(MyBundleName.STUDENT_ID, mDataList.get(contextPosition).sid);
							queryBundle.putString(MyBundleName.STUDENT_NAME, mDataList.get(contextPosition).empName);
							queryBundle.putString(MyBundleName.STUDENT_IMG_URL,
									mDataList.get(contextPosition).imgSavePath);
							queryBundle.putInt(MyBundleName.STUDENT_BONUS_POINTS,
									mDataList.get(contextPosition).availableBonusPoints);
							queryBundle.putSerializable(MyBundleName.STUDENT_BEAN, mDataList.get(contextPosition));
							queryBundle.putString("group_id", mAllStudentDataList[mSelClassIndex].groupId);
							queryIntent.putExtras(queryBundle);
							startActivity(queryIntent);
							break;
						case 2:// 兑奖
							Intent intentExchange = new Intent(getContext(), ExchangePrizeActivity.class);
							Bundle bundleExchange = new Bundle();

							TextView textView = (TextView) contextView.findViewById(R.id.item_badge_num);
							bundleExchange.putSerializable("student_bean", studentBean);
							bundleExchange.putInt(MyBundleName.STUDENT_ID, studentBean.sid);
							bundleExchange.putString(MyBundleName.STUDENT_NAME, studentBean.empName);
							bundleExchange.putInt(MyBundleName.STUDENT_BONUS_POINTS, studentBean.availableBonusPoints);

							intentExchange.putExtras(bundleExchange);
							startActivity(intentExchange);

							break;
						case 3:// 授权代理和取消代理
							/*
							 * if (studentBean.isStaffUserAgent == true) {
							 * popupMenuItemList.add("取消代理"); } else {
							 * popupMenuItemList.add("授权代理"); }
							 * popupMenuItemList.add("换照片");
							 */

							if (isMore) {
								isMore = false;
							} else {
								if (studentBean.isStaffUserAgent == false) {
									showLoadDialog();
									for (StudentBean bean : mDataList) {
										if (bean.isStaffUserAgent == true) {
											cancelAgent(bean, false);
										}
									}
									addAgent(studentBean);
								} else {
									showLoadDialog();
									cancelAgent(studentBean, true);
								}
							}

							break;
						case 4:// 换照片
							mPicSelHelper.showAddPicDialog();
							break;
						}

					}

					@Override
					public boolean showPopupList(View adapterView, View contextView, int contextPosition) {

						return true;
					}

					@Override
					public void onImgListClick(View contextView, int contextPosition, int position, BadgeInfo badge) {
						StudentListFragment.this.mContextPosition = contextPosition;
						StudentListFragment.this.mContextView = contextView;

						if (studentBean.isSelect == false) {
							studentBean.isSelect = true;
							studentBean.currentIndex = contextPosition;
							selectStudents.add(studentBean);
							if (mAdaptor != null) {
								mAdaptor.notifyDataSetChanged();
							}
						}

						awardBadge(selectStudents, badge);
					}
				});
		return popupList;
	}

	private void setListAdaptor() {
		mDataList = new ArrayList<StudentBean>();

		// 学生数据数组排序
		for (int i = 0; i < mListItems.size(); i++) {
			for (int j = 0; j < mStudentDataList.size(); j++) {
				StudentBean studentBean = mStudentDataList.get(j);
				if (mListItems.get(i).equals(studentBean.empName)) {
					if (mDataList.contains(studentBean) == false) {// 去掉重复处理，重名数据会添加多次
						mDataList.add(studentBean);
					}
				}
			}
		}

		resetSelectStudents();
		mAdaptor = new StudentItemAdapter(this.getContext(), mListItems, mListSectionPos, new ListFilter(), mDataList, false,
				studentBeanChangeListener);
		mAdaptor.enableMultiSelector(false);
		mAdaptor.mContext=this.getContext();
		if (mAppType == MyBundleName.TYPE_STUDENT_ATTENDANCE) {
			mAdaptor.mItemType = StudentItemAdapter.ITEM_TYPE_ATTENDANCE;
		} else if (mAppType == MyBundleName.TYPE_STUDENT_SCORE || mAppType == MyBundleName.TYPE_STUDENT_OPINION) {
			mAdaptor.mItemType = StudentItemAdapter.ITEM_TYPE_GRADE;
		} else if (mAppType == MyBundleName.TYPE_STUDENT_FLOWER) {
			mAdaptor.enableMultiSelector(true);
		}
		mGridView.setAdapter(mAdaptor);

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		IndexBarView indexBarView = (IndexBarView) inflater.inflate(R.layout.index_bar_view, mGridView, false);
		indexBarView.setData(mGridView, mNameInitialList, mListSectionPos);

		mGridView.setIndexBarView(indexBarView);

		mGridView.setOnScrollListener(mAdaptor);


	}

	private ArrayList<StudentBean> selectStudents = new ArrayList<StudentBean>();

	OnStudentBeanChangeListener studentBeanChangeListener = new OnStudentBeanChangeListener() {

		@Override
		public void onSelect(StudentBean bean, int position) {
			
			if (bean.isSelect) {
				bean.currentIndex = position;
				selectStudents.add(bean);
			} else {
				for (int index = 0; index < selectStudents.size(); index++) {
					if (selectStudents.get(index).sid == bean.sid) {
						selectStudents.remove(index);
						break;
					}
				}
				for (int i = 0; i < macList.size(); i++) {
					if (getMac(bean.ctrlId)!=null && macList.get(i).equals(getMac(bean.ctrlId))) {
						macList.remove(i);
						break;
					}
				}
				
			}
		}

		@Override
		public void onClicked(StudentBean bean, int position, View view) {
			if (mAppType == MyBundleName.TYPE_STUDENT_FLOWER) {
				if (((StudentListActivity) getActivity()).isCanShow) {

					int[] location = new int[2];

					if (view == null) {
						view = mGridView.getChildAt(position - mGridView.getFirstVisiblePosition());
					}
					view.getLocationOnScreen(location);
					float viewHeight = location[1] + view.getHeight() / 2;
					if (screenHeight <= 1280) {
						if (viewHeight > screenHeight / 8 - dp2px(10) && viewHeight < screenHeight - dp2px(95)) {

							isShow = true;
							listterner.process(isShow);
							if (viewHeight > 160) {
								isShowViewTop = true;
							} else {
								isShowViewTop = false;
							}
							setPopupListAndHightLight(view, position);
						}
					} else {
						if (viewHeight > screenHeight / 6 - dp2px(12) && viewHeight < screenHeight - dp2px(95)) {

							isShow = true;
							listterner.process(isShow);
							if (viewHeight > 500) {
								isShowViewTop = true;
							} else {
								isShowViewTop = false;
							}
							setPopupListAndHightLight(view, position);
						}
					}
				}
			} else if (mAppType == MyBundleName.TYPE_STUDENT_ATTENDANCE) {
				Intent intent = new Intent(getContext(), StudentAttendanceActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(MyBundleName.STUDENT_APP_TYPE, MyBundleName.TYPE_ATTENDANCE);
				bundle.putString(MyBundleName.STUDENT_NAME, mDataList.get(position).empName);
				bundle.putInt(MyBundleName.STUDENT_ID, mDataList.get(position).sid);
				intent.putExtras(bundle);
				startActivity(intent);
			} else if (mAppType == MyBundleName.TYPE_STUDENT_SCORE) {
				Intent intent = new Intent(getActivity(), SubjectExamActivity.class);
				intent.putExtra(MyBundleName.STUDENT_BEAN, mDataList.get(position));
				intent.putExtra(MyBundleName.TYPE, mAppType);
				startActivity(intent);
			} else if (mAppType == MyBundleName.TYPE_STUDENT_OPINION) {
				Intent intent = new Intent(getActivity(), ColligateOpinionActivity.class);
				intent.putExtra(MyBundleName.STUDENT_BEAN, mDataList.get(position));
				intent.putExtra(MyBundleName.TYPE, mAppType);
				startActivity(intent);
			}
		}
	};

	private void initListAdaptor() {
		resetSelectStudents();
		mAdaptor = new StudentItemAdapter(this.getContext(), null, null, new ListFilter(), null, false,
				studentBeanChangeListener);
		mAdaptor.enableMultiSelector(false);
		mAdaptor.mContext=this.getContext();
		if (mAppType == MyBundleName.TYPE_STUDENT_ATTENDANCE) {
			mAdaptor.mItemType = StudentItemAdapter.ITEM_TYPE_ATTENDANCE;
		}

		if (mAppType == MyBundleName.TYPE_STUDENT_FLOWER) {
			mAdaptor.enableMultiSelector(true);
		}
		mGridView.setAdapter(mAdaptor);
		
		if (this.getActivity()!=null) {
			LayoutInflater inflater = LayoutInflater.from(this.getActivity());

			IndexBarView indexBarView = (IndexBarView) inflater.inflate(R.layout.index_bar_view, mGridView, false);
			indexBarView.setData(mGridView, null, null);

			mGridView.setIndexBarView(indexBarView);
		}
		

		mGridView.setOnScrollListener(mAdaptor);
	}

	private void resetSelectStudents() {
		for (int i = 0; i < selectStudents.size(); i++) {
			selectStudents.get(i).isSelect = false;
		}
		selectStudents.clear();
		if (mAdaptor != null) {
			mAdaptor.notifyDataSetChanged();
		}
	}

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
			if (getContext() == null) {
				return null;
			}
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

				// Log.d(TAG, "mNameInitialList size:" + mNameInitialList.size()
				// + ",mListItems:" + mListItems.size());

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

				// Log.d(TAG, "mListItems size:" + mListItems.size());

				for (Integer i : mListSectionPos) {
					Log.d(TAG, "mListSectionPos i:" + i);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (getContext() == null) {
				return;
			}
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

	private final static int GB_SP_DIFF = 160;
	private final static int[] secPosValueList = { 1601, 1637, 1833, 2078, 2274, 2302, 2433, 2594, 2787, 3106, 3212,
			3472, 3635, 3722, 3730, 3858, 4027, 4086, 4390, 4558, 4684, 4925, 5249, 5600 };
	private final static char[] firstLetter = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'o',
			'p', 'q', 'r', 's', 't', 'w', 'x', 'y', 'z' };

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
					buffer.append(ch);
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
		if (result == '-') {
			result = SpecialCharManage.getSpecialInitial(secPosValue);
		}

		return result;
	}

	private class SortIgnoreCase implements Comparator<String> {
		public int compare(String s1, String s2) {
			return s1.compareToIgnoreCase(s2);
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

	private boolean isClassListShow = true;
	private ListView classListView;
	private FrameLayout dropdownFl;

	private View dropdownMask;

	private PeriscopeLayout periscopeLayout;
	// 排序后的学生数据数组
	private ArrayList<StudentBean> mDataList;

	private void dropdownClick() {
		if (isClassListShow) {
			if (mClassList != null) {
				classListView.setAdapter(new ClassSelListViewAdapter(getContext(), mClassList));
				listViewInAnim();
			}
		} else {
			listViewOutAnim();
		}
	}

	private void listViewInAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.VISIBLE);
		classListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
		dropdownMask.setVisibility(View.VISIBLE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_in));
		dropdownFl.setVisibility(View.VISIBLE);
		isClassListShow = false;
	}

	private void listViewOutAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.GONE);
		classListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
		dropdownMask.setVisibility(View.GONE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
		dropdownFl.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
		dropdownFl.setVisibility(View.GONE);
		isClassListShow = true;
	}

	private void dropdownChooseClick() {
		if (isChooseShow) {
			if (chooseList != null) {
				chooseListView.setAdapter(new ClassSelListViewAdapter(getContext(), chooseList));
				listViewChooseInAnim();
			}
		} else {
			listViewChooseOutAnim();
		}
	}

	private void listViewChooseInAnim() {
		chooseListView.clearAnimation();
		chooseListView.setVisibility(View.VISIBLE);
		chooseListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
		dropdownChooseMask.setVisibility(View.VISIBLE);
		dropdownChooseMask.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_in));
		dropdownF2.setVisibility(View.VISIBLE);
		isChooseShow = false;
	}

	private void listViewChooseOutAnim() {
		chooseListView.clearAnimation();
		chooseListView.setVisibility(View.GONE);
		chooseListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
		dropdownChooseMask.setVisibility(View.GONE);
		dropdownChooseMask.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
		dropdownF2.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
		dropdownF2.setVisibility(View.GONE);
		isChooseShow = true;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {
			if (isBleStart) {
				stopBle();
			}
			if (!isClassListShow) {
				listViewOutAnim();
			}
		} else {
			if (!isBleStart) {
				startBle();
			}
			
			// 当前班级列表为空，重新获取班级列表
			if (mClassList == null || mClassList.size() == 0) {
				initData();
			}
		}
	}

	private ClassStudentData[] mAllStudentDataList = null;
	private int mGetStudentListDataNum = 0;

	private void getAllClassStudentListData(ArrayList<Integer> classGroupIdList, int updateIndex) {
		if (classGroupIdList == null)
			return;
		mAllStudentDataList = new ClassStudentData[classGroupIdList.size()];
		// SmartCampusApplication.mAllStudentDataList=mAllStudentDataList;
		mSelClassIndex = updateIndex;

		classTextView.setText(mClassList.get(updateIndex));

		isRefreshFlowers = true;
		// 一次取下所有学生的数据
		for (int i = 0; i < classGroupIdList.size(); i++) {
			mAllStudentDataList[i] = new ClassStudentData();
			mAllStudentDataList[i].groupId = String.valueOf(classGroupIdList.get(i));
			getStudentListData(classGroupIdList.get(i).toString(), i, updateIndex);
		}
	}

	private void getCurrentClass(final ArrayList<Integer> classGroupIdList) {
		String urlString = SmartCampusUrlUtils.getCurrentClass();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "getCurrentClass response=" + response);
						if (getOwnActivity() == null) {
							return;
						}
						int code = response.optInt("code");
						int updateIndex = 0;
						if (code == 0) {
							JSONObject data = response.optJSONObject("datas");
							if (data != null) {
								int groupId = data.optInt("groupId");
								// Log.d(TAG, "groupId="+groupId);
								for (int i = 0; i < classGroupIdList.size(); i++) {
									if (classGroupIdList.get(i).intValue() == groupId) {
										updateIndex = i;
										break;
									}
								}
							}
							// Log.d(TAG, "updateIndex="+updateIndex);
						} else if (code == -2) {
							InfoReleaseApplication.returnToLogin(StudentListFragment.this.getActivity());
						} else {
							reportToast(response.optString("msg"));
						}
						getAllClassStudentListData(classGroupIdList, updateIndex);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if (getOwnActivity() == null) {
							return;
						}
						Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
						// 获取当前班级失败，获取所有学生数据
						getAllClassStudentListData(classGroupIdList, 0);
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void getClassListData() {
		showLoadDialog();
		GroupListData.getClassListData(getOwnActivity(), new DataResponse.Listener<ArrayList<GroupInfo>>() {

			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				// TODO Auto-generated method stub
				if (getOwnActivity() == null) {
					return;
				}
				ArrayList<Integer> classGroupIdList = new ArrayList<Integer>();
				mClassList = new ArrayList<String>();
				for (int i = 0; i < classGroups.size(); i++) {
					mClassList.add(classGroups.get(i).getName());
					classGroupIdList.add(classGroups.get(i).getId());
				}
				if (classGroups.size() > 0) {
					// 获取当前班级
					getCurrentClass(classGroupIdList);
					if (mClassList.size() > 0) {
						mSelClassIndex = 0;
						classTextView.setText(mClassList.get(0));
					}
					classListView.setAdapter(new ClassSelListViewAdapter(getContext(), mClassList));
				} else {
					hideLoadDialog();
				}

			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				hideLoadDialog();
				if (getOwnActivity() == null) {
					return;
				}

			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideLoadDialog();
				if (getOwnActivity() == null) {
					return;
				}

			}
		});
	}

	public static final String ACTION_STUDENT_CHANGED = "com.routon.smartcampus.student";
	public static final String ACTION_ANIMATION_END = "com.routon.smartcampus.student.end";
	public static final String ACTION_STUDENT_BADGE_RETRACT = "com.routon.smartcampus.student.retract";
	public static final String ACTION_STUDENT_UPDATE = "com.routon.smartcampus.student.update";

	private BroadcastReceiver mContentChangedListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_STUDENT_CHANGED)) {
				// getStudentBadgeInfo(mDataList.get(mContextPosition));
				int count = intent.getIntExtra(MyBundleName.STUDENT_BONUS_POINTS, 0);
				setExchangeCount(count);

			} else if (action.equals(ACTION_ANIMATION_END)) {
				// Handler handler = new Handler();
				// handler.postDelayed(new Runnable() {
				// @Override
				// public void run() {
				// dropdownMask.setVisibility(View.GONE);
				// }
				// }, 500);

			} else if (action.equals(ACTION_STUDENT_BADGE_RETRACT)) {
				// getStudentBadgeInfo(mDataList.get(mContextPosition));
				TextView textView = (TextView) mContextView.findViewById(R.id.item_badge_num);
				int unExchangeCount = Integer.valueOf(String.valueOf(textView.getText()));
				TextView textView2 = (TextView) mContextView.findViewById(R.id.item_badge_num2);
				int badgeCount = Integer
						.valueOf(String.valueOf(textView2.getText().subSequence(1, textView2.getText().length())));

				textView.setText(String.valueOf(unExchangeCount - 1));
				textView2.setText("/" + (badgeCount - 1));

				StudentBean studentBean = mDataList.get(mContextPosition);
				mDataList.remove(mContextPosition);
				studentBean.badgeCount = badgeCount - 1;
				studentBean.unExchangeCount = unExchangeCount - 1;
				mDataList.add(mContextPosition, studentBean);
				if (mAdaptor != null) {
					mAdaptor.notifyDataSetChanged();
				}
			} else if (action.equals(ACTION_STUDENT_UPDATE)) {// 兑换礼物后刷新数据

				getStudentListData(mAllStudentDataList[mSelClassIndex].groupId, mSelClassIndex, mSelClassIndex);

			}

		}

	};

	// 接触校园卡跳转到学生所在班级
	private void toStudentClass(final String mac) {
		int classIndex = getClassTag(mac);
		if (classIndex >= 0) {
			int curPos = mSelClassIndex;
			if (curPos != classIndex) {// 学生不在当前班级
//				macList.clear();
				classListView.setSelection(classIndex);
				mSelClassIndex = classIndex;
				classTextView.setText(mClassList.get(classIndex));
				mGridView.setAdapter(null);

				if (mAllStudentDataList[classIndex] != null) {
					macList.clear();
					initStudentList(mAllStudentDataList[classIndex].studentdatalist, new PoplulateCompleteCB() {

						@Override
						public void complete() {
							// TODO Auto-generated method stub
							Handler handler = new Handler();
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									
									toStudentLocation(mac);
								}
							}, 100);
						}

					});
				} else {
					initStudentList(null, null);
				}

			} else {// 学生在当前班级
				toStudentLocation(mac);
			}
		} else {
			reportToast("未找到这个学生");
		}
	}

	private int getClassTag(String mac) {
		if (mac!=null) {
			for (int i = 0; i < mAllStudentDataList.length; i++) {
				ClassStudentData data = mAllStudentDataList[i];
				if (data != null) {
					if (null != data.studentdatalist) {
						for (StudentBean studentBean : data.studentdatalist) {
							if (getMac(studentBean.ctrlId)!=null &&mac.equals(getMac(studentBean.ctrlId))) {
								Log.d("run", mac+"==="+getMac(studentBean.ctrlId));
								return i;
							}
						}
					}
				}
			}
			return -1;
		}
		return -1;
	}

	// 接触校园卡跳转到学生所在位置并勾选
	private void toStudentLocation(String mac) {
		int location = 0;
		for (int i = 0; i < mDataList.size(); i++) {
			if (getMac(mDataList.get(i).ctrlId)!=null && mac.equals(getMac(mDataList.get(i).ctrlId))) {
				location = i;
				mDataList.get(i).isSelect = true;
				mDataList.get(i).currentIndex = i;
			}

		}
		mGridView.smoothScrollToPosition(location);
		if (!selectStudents.contains(mDataList.get(location))) {
			selectStudents.add(mDataList.get(location));
		}
		
		mAdaptor.notifyDataSetChanged();

		/*
		 * Handler handler = new Handler(); handler.postDelayed(new Runnable() {
		 * 
		 * @Override public void run() {
		 * 
		 * } }, 100);
		 */
	}

	private TextView classTextView;
	private View selView;

	private void registerRefreshListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_STUDENT_CHANGED);
		filter.addAction(ACTION_ANIMATION_END);
		filter.addAction(ACTION_STUDENT_BADGE_RETRACT);
		filter.addAction(ACTION_STUDENT_UPDATE);

		getContext().registerReceiver(mContentChangedListener, filter);
	}

	private void unregisterRefreshListener() {
		getContext().unregisterReceiver(mContentChangedListener);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerRefreshListener();
		BleBroadcastManager.getInstance().setContext(getActivity());
		BleBroadcastManager.getInstance().checkPermissions();
		
		if (!isBleStart) {
			new Timer().schedule(new TimerTask() {
				
				@Override
				public void run() {
					startBle();
				}
			}, 2000);
			
		}
	}

	@Override
	public void onDestroy() {
		unregisterRefreshListener();
		mFaceRecongnizeMgr.deinit();
		if (isBleStart) {
			stopBle();
		}
		super.onDestroy();
	}

	private String ftpUrl;
	private String port;

	private void getFtpUrl() {
		String urlString = SmartCampusUrlUtils.getFtpURl();

		showLoadDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						int code = response.optInt("code");
						if (code == 0) {
							JSONObject object = response.optJSONObject("datas");
							String ftp = object.optString("ftp");

							ftpUrl = ftp.substring(ftp.lastIndexOf("/") + 1, ftp.lastIndexOf(":"));
							port = ftp.substring(ftp.lastIndexOf(":") + 1, ftp.length());
							SmartCampusApplication.ftpUrl = ftpUrl;
							SmartCampusApplication.port = port;
							getFtpBadegData(ftpUrl, port, InfoReleaseApplication.authenobjData.userId + ".json");
						} else if (code == -2) {
							InfoReleaseApplication.returnToLogin(getActivity());
							hideLoadDialog();
						} else {// 失败
							Log.e(TAG, response.optString("msg"));
							Toast.makeText(getContext(), response.optString("msg"), Toast.LENGTH_LONG).show();
							hideLoadDialog();
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

	private void getFtpBadegData(final String ftpUrl, final String port, final String fileName) {
		showLoadDialog();
		BadgeInfoUtil.getFtpBadegData(ftpUrl, port, fileName, new BadgeInfoUtil.DownloadFileListener() {
			@Override
			public void downloadFile(final boolean success) {
				// TODO Auto-generated method stub

				getOwnActivity().runOnUiThread(new Runnable() {
					public void run() {
						// TODO Auto-generated method stub
						hideLoadDialog();
						if (success == false) {
							Toast.makeText(getOwnActivity(), "获取常用小红花失败", Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		});

	}

	private ArrayList<StudentBadge> studentBadges = new ArrayList<StudentBadge>();
	private int userId;

	private void getStudentBadgeInfo(final StudentBean studentBean) {
		String urlString = SmartCampusUrlUtils.getStudentListUrl() + "&groupIds=" + studentBean.groupId + "&name="
				+ studentBean.empName;
		showLoadDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					private StudentBean bean;

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (getOwnActivity() == null) {
							return;
						}
						hideLoadDialog();
						try {
							if (response.getInt("code") == 0) {
								studentBadges.clear();
								JSONArray array = response.getJSONArray("datas");
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.get(i);
									bean = new StudentBean(obj);
								}

								TextView textView = (TextView) mContextView.findViewById(R.id.item_badge_num);
								textView.setText(String.valueOf(bean.unExchangeCount));
								TextView textView2 = (TextView) mContextView.findViewById(R.id.item_badge_num2);
								textView2.setText("/" + bean.badgeCount);
								mDataList.remove(mContextPosition);
								studentBean.badgeCount = bean.badgeCount;
								studentBean.unExchangeCount = bean.unExchangeCount;
								mDataList.add(mContextPosition, studentBean);
								if (mAdaptor != null) {
									mAdaptor.notifyDataSetChanged();
								}
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(getActivity());
							} else {// 失败
								Log.e(TAG, response.getString("msg"));
								reportToast(response.getString("msg"));

							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						if (getOwnActivity() == null) {
							return;
						}
						if (InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) == true) {
							reportToast("获取数据失败");
						}
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void setBadgeCount(int count, int integral) {
		// TextView textView = (TextView)
		// mContextView.findViewById(R.id.item_badge_num);
		// int unExchangeCount = Integer.valueOf(
		// String.valueOf(textView.getText()).substring(0,
		// String.valueOf(textView.getText()).length() - 1));
		// TextView textView2 = (TextView)
		// mContextView.findViewById(R.id.item_badge_num2);
		// int badgeCount = Integer
		// .valueOf(String.valueOf(textView2.getText().subSequence(1,
		// textView2.getText().length())));
		//
		// textView.setText(String.valueOf(unExchangeCount + integral) + "分");
		// textView2.setText("/" + (badgeCount + count));

		for (int i = 0; i < selectStudents.size(); i++) {
			StudentBean studentBean = selectStudents.get(i);
			// mDataList.remove(studentBean.currentIndex);
			studentBean.badgeCount = studentBean.badgeCount + count;
			studentBean.unExchangeCount = studentBean.unExchangeCount + count;
			studentBean.bonuspoints = studentBean.bonuspoints + integral;
			studentBean.availableBonusPoints = studentBean.availableBonusPoints + integral;
			// mDataList.add(studentBean.currentIndex, studentBean);
		}
		mAdaptor.notifyDataSetChanged();

	}

	private void setExchangeCount(int count) {
		TextView textView = (TextView) mContextView.findViewById(R.id.item_badge_num);
		textView.setText(String.valueOf(count));
		StudentBean studentBean = mDataList.get(mContextPosition);
		studentBean.bonuspoints = count;
		mAdaptor.notifyDataSetChanged();
	}

	private FragmentInteraction listterner;
	private PopupWindow window;
	private int screenHeight;
	// private ArrayList<BadgeInfo> oftenBadgeList=new ArrayList<BadgeInfo>();

	public interface FragmentInteraction {
		void process(Boolean bool);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		if (activity instanceof FragmentInteraction) {
			listterner = (FragmentInteraction) activity;
		} else {
			throw new IllegalArgumentException("activity must implements FragmentInteraction");
		}
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		listterner = null;
	}

	private void refreshData() {
		isRefreshFlowers = false;
		String urlString = SmartCampusUrlUtils.getBadgeListUrl();
		showLoadDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (getOwnActivity() == null) {
							return;
						}

						try {
							if (response.getInt("code") == 0) {

								flowersList = new ArrayList<Badge>();
								JSONArray array = response.getJSONArray("datas");
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.get(i);
									Badge flower = new Badge(obj);
									flowersList.add(flower);
								}
								BadgeInfoUtil.setFlowerList(flowersList);
								// 获取常用小红花数据
								getFtpUrl();
								// flowersList.addAll(flowersList.subList(0,
								// 3));
								// refreshFlowerList(flowersList);
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(getActivity());
								hideLoadDialog();
							} else {// 失败
								Log.e(TAG, response.getString("msg"));
								reportToast(response.getString("msg"));
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
						Log.e(TAG, "sorry,Error");
						hideLoadDialog();
						if (getOwnActivity() == null) {
							return;
						}
						if (InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) == true) {
							reportToast("获取数据失败");
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	private void awardBadge(final ArrayList<StudentBean> students, final BadgeInfo badgeBean) {
		String urlString = SmartCampusUrlUtils.getBadugeIssueURl();
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		String studentIds = "";
		if (students.size() > 0) {
			for (int i = 0; i < students.size(); i++) {
				if (i != 0) {
					studentIds += ",";
				}
				studentIds += String.valueOf(students.get(i).sid);
			}
			params.add(new BasicNameValuePair("studentIds", studentIds));
		}
		if (badgeBean.badgeTitleId>0) {
			params.add(new BasicNameValuePair("resId", String.valueOf(badgeBean.badgeTitleId)));
		}
		params.add(new BasicNameValuePair("badgeId", String.valueOf(badgeBean.badgeId)));
		params.add(new BasicNameValuePair("count", String.valueOf(1)));
		params.add(new BasicNameValuePair("title", badgeBean.badgeTitle));
		params.add(new BasicNameValuePair("remark", badgeBean.badgeRemark));
		params.add(new BasicNameValuePair("bonuspoint", "" + badgeBean.bonuspoint));// String.valueOf(bean.badgeBonuspoint)));

		showLoadDialog();

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (getOwnActivity() == null) {
							return;
						}
						hideLoadDialog();
						try {
							if (response.getInt("code") == 0) {
								// sBean.badgeCount=sBean.badgeCount+1;
								// sBean.bonuspoints=sBean.bonuspoints+badgeBean.bonuspoint;
								// sBean.availableBonusPoints=sBean.availableBonusPoints+badgeBean.bonuspoint;
								setFlowerAnimation(badgeBean);
								chooseBtn.setImageResource(R.drawable.flower_choose_normal);
								isSelectChooseAll=true;

							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(getActivity());
							} else {
								Log.e(TAG, response.getString("msg"));
								reportToast(response.getString("msg"));

							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						if (getOwnActivity() == null) {
							return;
						}
						if (InfoReleaseApplication.showNetWorkFailed(getOwnActivity()) == true) {
							reportToast("获取数据失败");
						}
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	protected void setFlowerAnimation(BadgeInfo badgeBean) {
		dropdownMask.setVisibility(View.VISIBLE);
		final int badgeCount = 1;
		final ArrayList<String> badgeUrls = new ArrayList<String>();
		badgeUrls.add(badgeBean.imgUrl);
		final ArrayList<String> badgeNames = new ArrayList<String>();
		badgeNames.add(badgeBean.badgeTitle);
		final int badgeIntegral = badgeBean.bonuspoint;
		setBadgeCount(badgeCount, badgeIntegral);
		int[] location = new int[2];

		Handler handler = new Handler();
		for (int j = 0; j < selectStudents.size(); j++) {

			final StudentBean studentBean = selectStudents.get(j);

			if (studentBean.currentIndex < mGridView.getFirstVisiblePosition()
					|| studentBean.currentIndex > mGridView.getLastVisiblePosition()) {
				studentBean.isSelect = false;// 仅设置非选中状态，不删除
				continue;
			}
			selView = mGridView.getChildAt(studentBean.currentIndex - mGridView.getFirstVisiblePosition());
			selView.getLocationOnScreen(location);
			final float viewWidth = selView.getWidth() / 2;
			final float x = location[0] + selView.getWidth() / 2;
			final float y = location[1] + selView.getHeight() - (selView.getHeight() / 3);

			for (int i = 0; i < badgeUrls.size(); i++) {
				final String badgeUrl = badgeUrls.get(i);
				final String badgeName = badgeNames.get(i);
				final int tag = i;
				int timer = 200 * i;
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (tag < badgeUrls.size() - 1) {
							periscopeLayout.addHeart(x, y, viewWidth, getContext(), badgeUrl, badgeCount, badgeName,
									studentBean);
						} else {
							periscopeLayout.addHeart(x, y, viewWidth, getContext(), badgeUrl, badgeCount, badgeName,
									studentBean);
						}

						periscopeLayout.setOnHeartAnimationUpdateListener(new OnHeartAnimationUpdateListener() {

							@Override
							public void onAnimationEnd(StudentBean bean) {
								// TODO Auto-generated method stub
								selectStudents.remove(bean);
								bean.isSelect = false;

								if (selectStudents.size() == 0) {
									dropdownMask.setVisibility(View.GONE);
									resetSelectStudents();
								} else {// 判断是否颁发完成
									boolean flag = true;
									for (int k = 0; k < selectStudents.size(); k++) {
										StudentBean stb = selectStudents.get(k);
										if (stb.isSelect) {
											flag = false;
											break;
										} else {
											selectStudents.remove(stb);
										}
									}

									if (flag) {
										dropdownMask.setVisibility(View.GONE);
										resetSelectStudents();
									}
								}
							}
						});
					}
				}, timer);

			}
		}
	}

	public int dp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
	}

	public int sp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
	}

	ProgressDialog progressDialog;
	// 是否需要重新获取小红花，只在进入小红花学生列表界面时获取一次
	private boolean isRefreshFlowers = true;

	private void showLoadDialog() {
		Log.d(TAG, "showLoadDialog");
		if (progressDialog == null) {
			progressDialog = ProgressDialog.show(getContext(), "", "...loading...");
			progressDialog.show();
		} else {
			progressDialog.show();
		}
	}

	private void hideLoadDialog() {
		Log.d(TAG, "hideLoadDialog");
		if (progressDialog != null) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
				progressDialog = null;
			}

		}
	}

	
	List<String> macList=new ArrayList<String>();
	private boolean isBleStart;
	public void startBle() {
		Log.d("run", "startBle");
		macList.clear();
		if (getActivity()==null) {
			return;
		}
		BleBroadcastManager.getInstance().init(getActivity(), 999);

		BleBroadcastManager.getInstance().startAdvertising();
		setScanRule();
		BleBroadcastManager.getInstance().scan(this);
		isBleStart = true;
	}

	public void stopBle() {
		Log.d("run", "stopBle");
		BleBroadcastManager.getInstance().cancelScan();
		BleBroadcastManager.getInstance().stopAdvertising();

		isBleStart = false;
	}

	private void setScanRule() {
		String[] uuids;

		boolean isAutoConnect = false;

		BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
				// .setServiceUuids(serviceUuids) // 只扫描指定的服务的设备，可选
				// .setDeviceName(true, names) // 只扫描指定广播名的设备，可选
				// .setDeviceMac(mac) // 只扫描指定mac的设备，可选
				.setAutoConnect(isAutoConnect) // 连接时的autoConnect参数，可选，默认false
				.setScanTimeOut(0) // 扫描超时时间，可选，默认10秒
				.build();
		BleBroadcastManager.getInstance().initScanRule(scanRuleConfig);
	}

	@Override
	public void onScanStarted(boolean success) {

	}

	@Override
	public void onScanning(BleDevice result) {
//		BleLog.e("onScanning " + HexUtil.formatHexString(result.getScanRecord()));
	}

	@Override
	public void onScanFinished(List<BleDevice> scanResultList) {
//		BleLog.e("onScanning onScanFinished " + scanResultList.size());
	}

	@Override
	public void onLeScan(BleDevice bleDevice) {
		int rssi = bleDevice.getRssi();
		if (rssi > -50 && rssi < 0) {
			String data = HexUtil.formatHexString(bleDevice.getScanRecord());

			String headMatch = data.substring(4, 12);
			if (headMatch.startsWith("01704001")) {
				String Battery = data.substring(12, 18);
				String Mac = data.substring(18, 24);
//				BleLog.e("onScanning onLeScan " + "data :" + data);
//				BleLog.e("onScanning onLeScan " + "rssi :" + rssi + "Battery: " + Battery + "Mac: " + Mac);
//				Log.e("run", "Mac: " + Mac);
				if (!macList.contains(Mac)) {//
					macList.add(Mac);
					toStudentClass(Mac);
					
				}
			}

		}
	}

	//获取学生mac后6位并转小写
	private String getMac(String ctrlId) {
		if (ctrlId==null || !ctrlId.contains(":")) {
			return "";
		}
		String str=ctrlId.replace(":", "");
		str=str.substring(6, str.length()).toLowerCase();
		return str;
	}

}