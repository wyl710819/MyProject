package com.routon.smartcampus.flower;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.pictureAdd.PictureAddActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.edurelease.R;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.flower.RemarkEditPopWin.OnFinishListener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.ImgUploadUtil;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.utils.UploadImgListener;
import com.routon.smartcampus.view.SoftKeyBoardListener;
import com.routon.smartcampus.view.SoftKeyBoardListener.OnSoftKeyBoardChangeListener;
import com.squareup.picasso.Picasso;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.routon.widgets.Toast;

//小红花评语界面
public class BadgeRemarkActivity extends CustomTitleActivity implements OnItemClickListener {

	private String TAG = "BadgeRemarkActivity";
	private ImageView studentImg;
	private TextView badgeRemarkTv;
	private SlidingItemListView badgeRemarkLv;
	private TextView studentName;
	private LayoutParams params;
	private BadgeEditPopWin takePhotoPopWin;
	private BadgeRemarkListViewAdapter badgeRemarkListViewAdapter;

	private List<BadgeInfo> badgeRemarkList = new ArrayList<BadgeInfo>();
	private ArrayList<String> imgDatas = new ArrayList<String>();
	private ArrayList<String> remarkImages = new ArrayList<String>();
	private ArrayList<String> remarkImageList = new ArrayList<String>();
	private List<Integer> pictureMaterialIds;

	private Badge badge;

	private int studentId = 0;
	private String remarkStr = "";
	private String remarkTitle = "";
	private String editStr = null;
	private String editTitleStr = null;
	private int issuedId = 0;
	private String titleNextBtnText;
	private String issuedTime;
	private int awardType = 0;

	private ArrayList<String> saveRemarkImages;
	private int savedBonusPoint;
	private String saveRemarkStr;
	private ArrayList<StudentBean> allStudents;
	
	private ArrayList<BadgeInfo> mBadgeList;
	
	private int remarkCount = 0;
	private int teacherUserId = -1;
	private List<BadgeInfo> mSaveList = new ArrayList<>();
	private List<BadgeInfo> tempRemarkList;
	private List<String> badgeTitles= new ArrayList<String>();
	private View popView;
//	private int mBadgeTitleId=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_badge_remark);

		initView();
		initData();

	}

	private GridView stGridView;

	private void initView() {

		initTitleBar("小红花评语");
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));

		studentImg = (ImageView) findViewById(R.id.badge_remark_student_img);
		studentName = (TextView) findViewById(R.id.badge_remark_user_name);
		badgeRemarkTv = (TextView) findViewById(R.id.badge_describe_tv);
		badgeRemarkLv = (SlidingItemListView) findViewById(R.id.badge_remark_lv);
		stGridView = (GridView) findViewById(R.id.students_list_grid_view);
		stGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

		badgeRemarkLv.setOnItemClickListener(this);

		SoftKeyBoardListener.setListener(this, new OnSoftKeyBoardChangeListener() {

			@Override
			public void keyBoardShow(int height) {
				if (!isAddOften) {
					FrameLayout.LayoutParams popupParams = new FrameLayout.LayoutParams(
							FrameLayout.LayoutParams.WRAP_CONTENT, takePhotoPopWin.view.getHeight() + 280);
					takePhotoPopWin.view.setLayoutParams(popupParams);
				}

			}

			@Override
			public void keyBoardHide(int height) {
				if (!isAddOften) {
					FrameLayout.LayoutParams popupParams = new FrameLayout.LayoutParams(
							FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
					takePhotoPopWin.view.setLayoutParams(popupParams);
				}

			}
		});

	}

	private void initData() {
		studentBadge = (StudentBadge) getIntent().getSerializableExtra(MyBundleName.STUDENT_BADGE_INFO);
		studentBean = (StudentBean) getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
		allStudents = (ArrayList<StudentBean>) getIntent().getSerializableExtra(MyBundleName.MULTI_STUDENT_BEANS);
		//获取常用小红包，从常用小红花编辑界面过来，可能数据还没有更新到最新
		mBadgeList = (ArrayList<BadgeInfo>) getIntent().getSerializableExtra(MyBundleName.BADGE_INFO_LIST);
		if (studentBean == null && allStudents != null && allStudents.size() > 1) {
			studentBean = allStudents.get(allStudents.size() - 1);
		}

		badge = (Badge) getIntent().getSerializableExtra(MyBundleName.BADGE_INFO);

		if (studentBadge != null) {
			titleNextBtnText = "添加";
			if (studentBadge != null) {
				badge = studentBadge.badge;
				issuedId = studentBadge.id;
				issuedTime = studentBadge.createTime;
			}
		} else {
			if (allStudents == null && studentBean == null) {
				titleNextBtnText = "完成";
				isAddOften = true;
			} else {
				titleNextBtnText = "颁发";
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
			}

		}
		savedBonusPoint = badge.bonuspoint;

		setTitleNextBtnClickListener(titleNextBtnText, new OnClickListener() {

			private boolean isSel = false;

			@Override
			public void onClick(View v) {

				if (isAddOften) {// 添加常用小红花
					OftenBadgeBean badgeInfo = new OftenBadgeBean();
					for (BadgeInfo bean : badgeRemarkList) {
						if (bean.isSelect) {
							isSel = true;
							badgeInfo.badgeId = bean.badgeId;
							badgeInfo.badgeTitle = bean.badgeTitle;
							badgeInfo.badgeRemark = bean.badgeRemark;
							badgeInfo.bonuspoint = bean.bonuspoint;
							badgeInfo.id = bean.id;
							badgeInfo.imgUrl = bean.imgUrl;
							badgeInfo.prop = bean.prop;
							badgeInfo.badgeTitleId = bean.badgeTitleId;
						}
					}

					if (!isSel) {
						Toast.makeText(BadgeRemarkActivity.this, "未选中小红花评语", Toast.LENGTH_SHORT).show();
						return;
					}

					Intent intent = new Intent();
					intent.putExtra("badge_info", badgeInfo);
					setResult(RESULT_OK, intent);
					finish();

					return;
				}

				if (studentBadge != null) {
					for (BadgeInfo bean : badgeRemarkList) {
						if (bean.isSelect) {
							isSel = true;
						}
					}

					if (!isSel) {
						Toast.makeText(BadgeRemarkActivity.this, "您的评语为空", Toast.LENGTH_SHORT).show();
						return;
					}
				}

				if (isSelectType) {
					if (saveRemarkImages != null) {
						saveRemarkImages.clear();
					}
					saveRemarkStr = null;
					isSelectType = false;
				}

				awardType = 0;
				if (saveRemarkImages != null && saveRemarkImages.size() > 2) {
					remarkStr = saveRemarkStr;

					showLoadDialog();
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							// sendImages(saveRemarkImages);
							ImgUploadUtil.uploadImg(BadgeRemarkActivity.this, saveRemarkImages,
									new UploadImgListener() {

										@Override
										public void uploadImgSuccessListener(List<Integer> imgFileIdList) {
											// TODO Auto-generated method stub
											pictureMaterialIds = new ArrayList<Integer>();
											if (imgFileIdList != null && imgFileIdList.size() > 0) {
												for (int j = 0; j < imgFileIdList.size(); j++) {
													pictureMaterialIds.add(imgFileIdList.get(j));
												}
											}
											awardBadge(savedBonusPoint);
										}

										@Override
										public void uploadImgErrorListener(String errorStr) {
											Toast.makeText(BadgeRemarkActivity.this, errorStr, Toast.LENGTH_SHORT)
													.show();
											hideLoadDialog();
										}
									});

						}
					}, 200);

				} else {
					remarkStr = saveRemarkStr;
					awardBadge(savedBonusPoint);
				}

			}

		});

		getMyRemark();

		if (issuedId != 0) {
			if (studentBadge.status == 3) {
				badgeRemarkTv.setText(getTimeStr(issuedTime) + "\n代理人颁发");
			} else {
				badgeRemarkTv.setText(getTimeStr(issuedTime));
			}

			studentName.setText(studentBadge.student.empName);
			String url = studentBadge.student.imgUrl;
			if (url != null && url.isEmpty() == false) {
				Picasso.with(this).load(url).placeholder(R.drawable.default_student).error(R.drawable.default_student)
						.into(studentImg);
			} else {
				studentImg.setImageResource(R.drawable.default_student);
			}
		} else {

			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd ");
			String date = sDateFormat.format(new java.util.Date());
			badgeRemarkTv.setText(getTimeStr(date));

			if (studentBean != null) {
				studentId = studentBean.sid;
				studentName.setText(studentBean.empName);
				String url = studentBean.imgUrl;
				if (url != null && url.isEmpty() == false) {
					Picasso.with(this).load(url).placeholder(R.drawable.default_student)
							.error(R.drawable.default_student).into(studentImg);
				} else {
					studentImg.setImageResource(R.drawable.default_student);
				}
			}
		}

		if (badge != null) {
			for (int i = 0; i < badge.badgeRemarkList.size(); i++) {
				BadgeRemarkBean badgeRemarkBean = new BadgeRemarkBean();
				if (badge.badgeRemarkList.get(i).bonuspoint == 0) {
					badgeRemarkBean.bonuspoint = badge.bonuspoint;
				} else {
					badgeRemarkBean.bonuspoint = badge.badgeRemarkList.get(i).bonuspoint;
				}

				badgeRemarkBean.badgeTitle = badge.badgeRemarkList.get(i).badgeTitle;
				badgeRemarkBean.badgeRemark = badge.badgeRemarkList.get(i).badgeRemark;
				badgeRemarkBean.imgUrl = badge.badgeRemarkList.get(i).imgUrl;
				badgeRemarkBean.badgeTitleId = badge.badgeRemarkList.get(i).badgeTitleId;
				badgeRemarkList.add(badgeRemarkBean);
			}

		}

		badgeRemarkListViewAdapter = new BadgeRemarkListViewAdapter(this, badgeRemarkList,
				badgeRemarkLv.getRightViewWidth());
		badgeRemarkListViewAdapter.setDelListener(new BadgeRemarkListViewAdapter.DelListener() {
			
			@Override
			public void del(int position) {
				// TODO Auto-generated method stub
				BadgeInfo info = badgeRemarkList.remove(position);
				badgeRemarkListViewAdapter.notifyDataSetChanged();
				BadgeInfoUtil.getDefineEvas().remove(info);
				uploadFile();
			}
		});
		badgeRemarkLv.setAdapter(badgeRemarkListViewAdapter);

		/*if (badgeRemarkList.size() == 1) {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					showPopWin(saveRemarkImages, saveRemarkStr, savedBonusPoint);
					remarkTag = -1;
				}
			}, 500);

		}*/
		
		
		
      /*  popView = getLayoutInflater().inflate(R.layout.badge_title_list_layout, null);
        badgeTitleLv = (ListView) popView.findViewById(R.id.badge_title_lv);
		if (isAddOften) {
			mPopupWindow = new PopupWindow(popView, dip2px(120), dip2px(120));
			mPopupWindow.setOutsideTouchable(true);
		}*/
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		mBadgeTitleId=0;
		/*if (position == badgeRemarkList.size() - 1) {
			remarkTag=-1;
			showPopWin(null, null, savedBonusPoint);
			return;
		}*/

		editStr = null;
		editTitleStr = null;

		if (badgeRemarkList.get(position).isSelect) {
			badgeRemarkList.get(position).isSelect = false;
			isSelectType = true;
			remarkTag=-1;
		} else {
			for (BadgeInfo bean : badgeRemarkList) {
				bean.isSelect = false;
			}
			badgeRemarkList.get(position).isSelect = true;
			if (remarkTag == position) {
				isSelectType = false;
			} else {
				if (saveRemarkImages != null) {
					saveRemarkImages.clear();
				}
				saveRemarkStr = null;
			}
			editStr = badgeRemarkList.get(position).badgeRemark;
			editTitleStr = badgeRemarkList.get(position).badgeTitle;
			remarkTag = position;
			savedBonusPoint = badgeRemarkList.get(position).bonuspoint;
			showPopWin(saveRemarkImages, saveRemarkStr, savedBonusPoint);

		}

		badgeRemarkListViewAdapter.notifyDataSetChanged();

	}

	/*
	 * 备注编辑窗口
	 */
	private void showPopWin(ArrayList<String> savemages, String saveRemarkString, int bonusPoint) {
		if (badge == null) {
			Toast.makeText(this, "当前小红花信息不明确！", 2000).show();
		}
		if (takePhotoPopWin != null && takePhotoPopWin.isShowing()) {
			takePhotoPopWin.dismiss();
		}
		final BadgeInfo bean = new BadgeInfo();
		bean.badgeRemark = editStr;
		bean.badgeTitle = editTitleStr;
		bean.bonuspoint = bonusPoint;
		bean.imgUrl = badge.imgUrl;
		
		if (remarkTag>-1) {
			bean.badgeTitleId=badgeRemarkList.get(remarkTag).badgeTitleId;
		}
		

		if (isAddOften) {// 添加小红花编辑框
			popWin = new RemarkEditPopWin(BadgeRemarkActivity.this, bean, new OnFinishListener() {

				@Override
				public void onFinishClick(OftenBadgeBean b) {
					ArrayList<BadgeInfo> badgeInfos = mBadgeList;			
					for (int i = 0; i < badgeInfos.size(); i++) {
						if (badgeInfos.get(i).badgeTitle.equals(b.badgeTitle) ) {
							Toast.makeText(BadgeRemarkActivity.this, "常用小红花标题不能重复！", Toast.LENGTH_SHORT).show();
							 return;
						}
					}
					
					/*if (mBadgeTitleId>0) {
						b.badgeTitleId=mBadgeTitleId;
					}*/
					b.badgeId = badge.id;
					b.type = BadgeInfo.TYPE_CUSTOMFLOWER;
					Intent intent = new Intent();			
					intent.putExtra("badge_info", b);
					setResult(RESULT_OK, intent);
					popWin.dismiss();
					finish();

				}
			});

			popWin.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			params = getWindow().getAttributes();
			params.alpha = 0.7f;
			getWindow().setAttributes(params);
			popWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					params = getWindow().getAttributes();
					params.alpha = 1f;
					getWindow().setAttributes(params);
					

				}
			});
			

			
			/*badgeTitles.clear();
			for (int i = 0; i < badgeRemarkList.size(); i++) {
				badgeTitles.add(badgeRemarkList.get(i).badgeTitle);
			}
			
			ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, R.layout.badge_title_item, R.id.badge_title_tv,badgeTitles);
			badgeTitleLv.setAdapter(adapter);
			
			
			popWin.titleEditView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (isSoftShowing()) {
						closeKeyboard(v);
					}
					
					mPopupWindow.showAsDropDown(v,0,popWin.getHeight());
				}
			});
			
			badgeTitleLv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					popWin.titleEditView.setText(badgeTitles.get(position));
					mBadgeTitleId = badgeRemarkList.get(position).badgeTitleId;
					mPopupWindow.dismiss();
				}
			});*/
			

		} else {//颁发编辑框
			takePhotoPopWin = new BadgeEditPopWin(BadgeRemarkActivity.this, bean, popOnClickListener, titleNextBtnText,
					savemages, saveRemarkString);

			takePhotoPopWin.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			params = getWindow().getAttributes();
			params.alpha = 0.7f;
			getWindow().setAttributes(params);

			takePhotoPopWin.setOnDismissListener(new PopupWindow.OnDismissListener() {

				@Override
				public void onDismiss() {
					params = getWindow().getAttributes();
					params.alpha = 1f;
					getWindow().setAttributes(params);

					saveRemarkImages = takePhotoPopWin.getRemarkImages();
					saveRemarkStr = takePhotoPopWin.getRemarkData().badgeRemark;
					savedBonusPoint = takePhotoPopWin.getRemarkData().bonuspoint;
				}
			});
		}

	}
	private PopOnClickListener popOnClickListener = new PopOnClickListener() {
		@Override
		public void itemClick(int position) {// 图片预览
			Intent intent = new Intent(BadgeRemarkActivity.this, RemarkImagePreviewActivity.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_ADD_PIC, true);
			bundle.putInt(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_POSITION, position);
			bundle.putStringArrayList(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST,
					takePhotoPopWin.imgList);
			intent.putExtras(bundle);
			BadgeRemarkActivity.this.startActivityForResult(intent, 2);
		}

		@Override
		public void lastItemtemClick() {// 添加图片
			Intent intent = new Intent(BadgeRemarkActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size()+1);
			intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 14);
			BadgeRemarkActivity.this.startActivityForResult(intent, 1);
		}

		@Override
		public void addImgClick(int position) {// 添加图片图标
			Intent intent = new Intent(BadgeRemarkActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size()+1);
			intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 14);
			BadgeRemarkActivity.this.startActivityForResult(intent, 1);
		}

		@Override
		public void awardClick() {// 颁发

			awardType = 1;
			if (takePhotoPopWin.getRemarkImages() != null && takePhotoPopWin.getRemarkImages().size() > 1) {
				if (takePhotoPopWin.getRemarkData().badgeRemark != null
						&& !takePhotoPopWin.getRemarkData().badgeRemark.replace(" ", "").replace("\n", "").equals("")) {
					remarkStr = takePhotoPopWin.getRemarkData().badgeRemark;
				}
				if (takePhotoPopWin.getRemarkData().badgeTitle!=null) {
					remarkTitle = takePhotoPopWin.getRemarkData().badgeTitle;
				}

				remarkImages = takePhotoPopWin.getRemarkImages();
				remarkImages.remove(remarkImages.size()-1);
				for (int i = 0; i < remarkImages.size() ; i++) {
					remarkImageList.add(remarkImages.get(i));
				}

				showLoadDialog();
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						// sendImages(remarkImages);
						ImgUploadUtil.uploadPic(BadgeRemarkActivity.this, remarkImages, new UploadImgListener() {

							@Override
							public void uploadImgSuccessListener(List<Integer> imgFileIdList) {
								// TODO Auto-generated method stub
								pictureMaterialIds = new ArrayList<Integer>();
								if (imgFileIdList != null && imgFileIdList.size() > 0) {
									for (int j = 0; j < imgFileIdList.size(); j++) {
										pictureMaterialIds.add(imgFileIdList.get(j));
									}
								}
								awardBadge(takePhotoPopWin.getRemarkData().bonuspoint);
							}

							@Override
							public void uploadImgErrorListener(String errorStr) {
								// TODO Auto-generated method stub
								Toast.makeText(BadgeRemarkActivity.this, errorStr, Toast.LENGTH_SHORT).show();
								hideLoadDialog();
							}
						});

					}
				}, 200);

			} else {
				if (takePhotoPopWin.getRemarkData().badgeTitle!=null) {
					remarkTitle = takePhotoPopWin.getRemarkData().badgeTitle;
				}else {
					if (issuedId != 0) {
						Toast.makeText(BadgeRemarkActivity.this, "您的评语标题为空", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
				if (takePhotoPopWin.getRemarkData().badgeRemark != null
						&& !takePhotoPopWin.getRemarkData().badgeRemark.replace(" ", "").replace("\n", "").equals("")) {
					remarkStr = takePhotoPopWin.getRemarkData().badgeRemark;
					awardBadge(takePhotoPopWin.getRemarkData().bonuspoint);
				} else {
					if (issuedId != 0) {
						Toast.makeText(BadgeRemarkActivity.this, "您的评语为空", Toast.LENGTH_SHORT).show();
					} else {
						awardBadge(takePhotoPopWin.getRemarkData().bonuspoint);
					}
				}

			}

		}

		@Override
		public void saveRemark(View v) {// 保存
			if (takePhotoPopWin.getRemarkData().badgeTitle == null
					|| takePhotoPopWin.getRemarkData().badgeTitle.trim().equals("")) {
				Toast.makeText(BadgeRemarkActivity.this, "评语标题不能为空", Toast.LENGTH_SHORT).show();
				return;
			}
			if (takePhotoPopWin.getRemarkData().badgeRemark == null
					|| takePhotoPopWin.getRemarkData().badgeRemark.trim().equals("")) {
				Toast.makeText(BadgeRemarkActivity.this, "评语内容不能为空", Toast.LENGTH_SHORT).show();
				return;
			}else {
				if (badgeRemarkList.get(remarkTag).badgeRemark.equals(takePhotoPopWin.getRemarkData().badgeRemark)) {
					Toast.makeText(BadgeRemarkActivity.this, "评语内容重复，无法保存", Toast.LENGTH_SHORT).show();
					return;
				}
				
			}

			BadgeRemarkBean bean = new BadgeRemarkBean();
			bean.badgeRemark = takePhotoPopWin.getRemarkData().badgeRemark;
			bean.badgeId = badge.id;
			bean.badgeTitle = takePhotoPopWin.getRemarkData().badgeTitle;
			bean.bonuspoint = takePhotoPopWin.getRemarkData().bonuspoint;
			bean.type = BadgeInfo.TYPE_DEFINEEVAS;
			bean.imgUrl = badge.imgUrl;
			bean.badgeTitleId = takePhotoPopWin.getRemarkData().badgeTitleId;
			
			ArrayList<BadgeInfo> deineEvas = BadgeInfoUtil.getDefineEvas();
			boolean exitsSameRemark = false;
			for (BadgeInfo badgeinfo:deineEvas) { 
				if(bean.badgeRemark.equals(badgeinfo.badgeRemark)){
					exitsSameRemark = true;
				}
			}
			if( exitsSameRemark == false ){
				BadgeInfoUtil.addDefineEva(bean);
				remarkCount++;
				badgeRemarkLv.setDataSize(remarkCount);
				tempRemarkList = new ArrayList<BadgeInfo>();
				tempRemarkList.addAll(badgeRemarkList);
				badgeRemarkList.clear();
				mSaveList.clear();
	
				mSaveList.add(0, bean);
				tempRemarkList.addAll(0, mSaveList);
				badgeRemarkList.addAll(tempRemarkList);
	
				if (badgeRemarkListViewAdapter != null) {
					badgeRemarkListViewAdapter.notifyDataSetChanged();
				}
				uploadFile();
				showDialog("评语已保存，你可选择设置为常用小红花",bean);
			}else{
				showDialog("评语已存在，你可选择设置为常用小红花",bean);
			}
		}
	};

	@Override
	protected void onDestroy() {	
		super.onDestroy();

	}

	private void uploadFile() {// 上传新生成的json文件
		showLoadDialog();
		BadgeInfoUtil.uploadFile(new BadgeInfoUtil.UploadFileListener() {
			
			@Override
			public void uploadFile(boolean success) {
				// TODO Auto-generated method stub
				hideLoadDialog();
			}
		});
	}
	
	ProgressDialog progressDialog;
    private void showLoadDialog(){
		if (progressDialog == null) {
			progressDialog = ProgressDialog.show(BadgeRemarkActivity.this, "", "...loading...");
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

	private void showDialog(String message,final BadgeInfo bean) {
		final AlertDialog.Builder normalDialog = new AlertDialog.Builder(BadgeRemarkActivity.this);
		normalDialog.setMessage(message);
		normalDialog.setPositiveButton("立即设置", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (takePhotoPopWin.isShowing()) {
					takePhotoPopWin.dismiss();
				}
				ArrayList<BadgeInfo> flowers = BadgeInfoUtil.getCustomFlowers();
				if( flowers.size() >= BadgeInfoUtil.MAX_CUSTOM_FLOWERS ) {
					Toast.makeText(BadgeRemarkActivity.this, "常用小红花数目不能超过"+BadgeInfoUtil.MAX_CUSTOM_FLOWERS+"个", Toast.LENGTH_LONG).show();
					return;
				}else {			
					for (int i = 0; i < flowers.size(); i++) {
						if (flowers.get(i).badgeTitle.equals(bean.badgeTitle) ) {
							Toast.makeText(BadgeRemarkActivity.this, "常用小红花标题不能重复！", Toast.LENGTH_SHORT).show();
							 return;
						}
					}
					OftenBadgeBean badgeInfo = new OftenBadgeBean();
					
					badgeInfo.badgeRemark = bean.badgeRemark;
					badgeInfo.badgeTitle = bean.badgeTitle;
					badgeInfo.bonuspoint = bean.bonuspoint;
					badgeInfo.imgUrl = bean.imgUrl;
					badgeInfo.badgeId = bean.badgeId;
					badgeInfo.type = 0;
					badgeInfo.id = bean.id;
					badgeInfo.badgeTitleId = bean.badgeTitleId;
					BadgeInfoUtil.addCustomFlower(badgeInfo);
					uploadFile();				
				}	

			}
		});
		normalDialog.setNegativeButton("暂不设置", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (takePhotoPopWin.isShowing()) {
					takePhotoPopWin.dismiss();
				}
			}
		});
		normalDialog.show();
	}

	/**
	 * 初始化时获取保存的评语
	 */
	private void getMyRemark() {
		ArrayList<BadgeInfo> defineEvas = BadgeInfoUtil.getDefineEvas();
		for (int i = 0; i < defineEvas.size(); i++) {
			BadgeInfo badgeInfo = defineEvas.get(i);
			if ( badgeInfo.badgeId == badge.id) {
				badgeRemarkList.add(badgeInfo);
			}
		}
		remarkCount = badgeRemarkList.size();
		badgeRemarkLv.setDataSize(remarkCount);

	}

	// 删除保存的评语
//	public void deleteSaveRemarkArrays(int pos) {
//
//		for (int i = 0; i < allBadgeList.size(); i++) {
//			if (allBadgeList.get(i).type == 1) {
//				allBadgeList.remove(i);
//				 i--;
//			}
//		}
//
//		customBadgeList.remove(pos);
//		allBadgeList.addAll(0, customBadgeList);
//
//		uploadFile();
//		badgeRemarkLv.setDataSize(--remarkCount);
//	}

	public void saveRemarkArrays(int teacherUserId, String remarkArrays) {
		SharedPreferences.Editor editor = getSharedPreferences("Sa", MODE_PRIVATE).edit();
		editor.putString(String.valueOf(teacherUserId) + badge.id, remarkArrays);
		editor.commit();
	}

	public String getRemarkArrays() {
		SharedPreferences prf = getSharedPreferences("Sa", MODE_PRIVATE);
		String remarkArrays = prf.getString(String.valueOf(teacherUserId) + badge.id, null);
		return remarkArrays;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				imgDatas = data.getStringArrayListExtra("img_data");
				takePhotoPopWin.addImgList(imgDatas);
			} else if (requestCode == 2) {
				imgDatas = data.getStringArrayListExtra("img_data");
				takePhotoPopWin.updateImgList(imgDatas);
			}
		}

	}

	/*
	 * 颁发小红花
	 */
	private int integral = 0;

	private void awardBadge(final int newBonusPoints) {
		boolean isSelRemark = false;
		BadgeInfo bean = null;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		integral = 0;
		for (int i = 0; i < badgeRemarkList.size(); i++) {
			if (badgeRemarkList.get(i).isSelect) {
				isSelRemark = true;
				bean = badgeRemarkList.get(i);
				remarkTitle = bean.badgeTitle;
			}
		}
		if (!isSelRemark) {
			Toast.makeText(BadgeRemarkActivity.this, "未选中任何小红花评语，无法颁发！", Toast.LENGTH_SHORT).show();
			return ;
		}
		
		String materialIdSrc = "";

		if (pictureMaterialIds != null && pictureMaterialIds.size() > 0) {

			// materialIdSrc = "&fileIds=";
			for (int i = 0; i < pictureMaterialIds.size(); i++) {
				if (i == pictureMaterialIds.size() - 1) {
					materialIdSrc += pictureMaterialIds.get(i);
				} else {
					materialIdSrc += pictureMaterialIds.get(i) + ",";
				}

			}
		}

		String urlString;
		if (issuedId == 0) {// 颁发徽章
			urlString = SmartCampusUrlUtils.getBadugeIssueURl();
			// urlString += "?studentIds=" + studentId;
			String studentIds = "";
			if (allStudents != null) {
				for (int i = 0; i < allStudents.size(); i++) {
					if (i != 0) {
						studentIds += ",";
					}
					studentIds += String.valueOf(allStudents.get(i).sid);
				}
				params.add(new BasicNameValuePair("studentIds", studentIds));
			} else {
				params.add(new BasicNameValuePair("studentIds", String.valueOf(studentId)));
			}
			if (isSelRemark && bean != null) {
				// urlString += "&badgeId=" + badge.id + "&count=" + 1 +
				// "&title=" + bean.badgeTitle + "&remark="
				// + remarkStr + "&bonuspoint="
				// + bean.badgeBonuspoint+ materialIdSrc;
				// urlString +=materialIdSrc;
				params.add(new BasicNameValuePair("badgeId", String.valueOf(badge.id)));
				params.add(new BasicNameValuePair("count", String.valueOf(1)));
				params.add(new BasicNameValuePair("title", bean.badgeTitle));
				params.add(new BasicNameValuePair("remark", remarkStr));
				params.add(new BasicNameValuePair("bonuspoint", "" + newBonusPoints));// String.valueOf(bean.badgeBonuspoint)));
				params.add(new BasicNameValuePair("fileIds", materialIdSrc));
				if (bean.badgeTitleId>0) {
					params.add(new BasicNameValuePair("resId", String.valueOf(bean.badgeTitleId)));
				}
				integral = newBonusPoints;// bean.badgeBonuspoint;
			} else {
				// urlString += "&badgeId=" + badge.id + "&count=" + 1 +
				// materialIdSrc+"&bonuspoint="+badge.bonuspoint;
				params.add(new BasicNameValuePair("badgeId", String.valueOf(badge.id)));
				params.add(new BasicNameValuePair("count", String.valueOf(1)));
				params.add(new BasicNameValuePair("bonuspoint", "" + newBonusPoints));// String.valueOf(badge.bonuspoint)));
				params.add(new BasicNameValuePair("fileIds", materialIdSrc));
				params.add(new BasicNameValuePair("title", remarkTitle));
				if (bean!=null && bean.badgeTitleId>0) {
					params.add(new BasicNameValuePair("resId", String.valueOf(bean.badgeTitleId)));
				}
				if (remarkStr != null && !remarkStr.equals("")) {
					// urlString += "&remark=" + remarkStr;
					params.add(new BasicNameValuePair("remark", remarkStr));
				}
				integral = newBonusPoints;// badge.bonuspoint;

			}
		} else {// 添加备注
			urlString = SmartCampusUrlUtils.getBadugeIssueAuditURl();
			urlString += "?issueId=" + issuedId;
			if (isSelRemark && bean != null) {
				// urlString += "&title=" + bean.badgeTitle + "&remark=" +
				// remarkStr + "&bonuspoint="
				// + bean.badgeBonuspoint + materialIdSrc;

				urlString += materialIdSrc;
				params.add(new BasicNameValuePair("title", bean.badgeTitle));
				params.add(new BasicNameValuePair("remark", remarkStr));
				params.add(new BasicNameValuePair("bonuspoint", "" + newBonusPoints));// String.valueOf(bean.badgeBonuspoint)));
				params.add(new BasicNameValuePair("fileIds", materialIdSrc));
				if (bean.badgeTitleId>0) {
					params.add(new BasicNameValuePair("resId", String.valueOf(bean.badgeTitleId)));
				}
			} else {
				if (awardType == 0) {
					hideLoadDialog();
					Toast.makeText(BadgeRemarkActivity.this, "未选中任何评语信息，无法添加！", Toast.LENGTH_SHORT).show();
					return;
				} else {
					// urlString += "&remark=" + remarkStr + materialIdSrc+
					// "&bonuspoint="+badge.bonuspoint;
					
					params.add(new BasicNameValuePair("title", remarkTitle));
					params.add(new BasicNameValuePair("remark", remarkStr));
					params.add(new BasicNameValuePair("bonuspoint", "" + newBonusPoints));// String.valueOf(badge.bonuspoint)));
					params.add(new BasicNameValuePair("fileIds", materialIdSrc));
					if (bean!=null && bean.badgeTitleId>0) {
						params.add(new BasicNameValuePair("resId", String.valueOf(bean.badgeTitleId)));
					}
				}

			}
		}

		showLoadDialog();

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideLoadDialog();
						try {
							if (response.getInt("code") == 0) {

								if (takePhotoPopWin != null && takePhotoPopWin.isShowing()) {
									takePhotoPopWin.dismiss();
								}

								Intent intent = new Intent();
								if (issuedId != 0) {
									Toast.makeText(BadgeRemarkActivity.this, "评语添加成功", Toast.LENGTH_SHORT).show();
									intent.putExtra(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_TEXT,
											remarkStr);
									if (awardType == 0) {
										remarkImageList.clear();
										for (int i = 0; i < saveRemarkImages.size() - 1; i++) {
											remarkImageList.add(saveRemarkImages.get(i));
										}
									}

									intent.putExtra(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_IMAGES,
											remarkImageList);
									intent.putExtra(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_TITLE,
											remarkTitle);
									intent.putExtra(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_SCORE,
											newBonusPoints);
								} else {

									Toast.makeText(BadgeRemarkActivity.this, "颁发成功", Toast.LENGTH_SHORT).show();
									intent.putExtra(MyBundleName.STUDENT_BADGE_SELECT_INTEGRAL, integral);
									intent.putExtra(MyBundleName.STUDENT_BADGE_SELECT_COUNT, 1);
									intent.putExtra(MyBundleName.STUDENT_BADGE_SELECT_URLS, badge.imgUrl);
									intent.putExtra(MyBundleName.STUDENT_BADGE_SELECT_NAME, badge.name);
									intent.putExtra(MyBundleName.STUDENT_BADGE_SELECT_SCORE, newBonusPoints);
								}
								setResult(RESULT_OK, intent);
								BadgeRemarkActivity.this.finish();

							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(BadgeRemarkActivity.this);
								finish();
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(BadgeRemarkActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
										.show();

							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(BadgeRemarkActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	private StudentBean studentBean;
	private StudentBadge studentBadge;
	private int remarkTag = -1;
	private boolean isSelectType = false;
	private RemarkEditPopWin popWin;
	//是否添加常用小红花
	private boolean isAddOften;
//	private PopupWindow mPopupWindow;
//	private ListView badgeTitleLv;
	

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
	
	/*private boolean isSoftShowing() {
        int screenHeight = getWindow().getDecorView().getHeight();
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
 
        return screenHeight - rect.bottom != 0;
    }
	
	private void closeKeyboard(View view) {
	    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	private int dip2px(float dpValue) {

		final float scale = getResources().getDisplayMetrics().density;

		return (int) (dpValue * scale + 0.5f);

	}*/
	

}
