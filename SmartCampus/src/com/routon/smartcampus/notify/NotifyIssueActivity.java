package com.routon.smartcampus.notify;

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
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.pictureAdd.PictureAddActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.DataResponse;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.notify.NotifyClassAdapter.OnCheckedListener;
import com.routon.smartcampus.notify.NotifyPicAdapter.DeleteClickListener;
import com.routon.smartcampus.utils.ImgUploadUtil;
import com.routon.smartcampus.utils.UploadImgListener;
import com.routon.smartcampus.view.HorizontalListView;
import com.routon.widgets.Toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class NotifyIssueActivity extends CustomTitleActivity {
	private static final String TAG = "NotifyIssueActivity";
	private HorizontalListView addPicGridView;
	private List<String> picLists = new ArrayList<String>();
//	private List<Integer> picIds= new ArrayList<Integer>();
	private NotifyPicAdapter mAdapter;
	private EditText notifyTitleEditView;
	private EditText notifyContentEditView;
	private ProgressDialog progressDialog;
	private Switch switchView;
	private int switchTag = 0;
	private boolean isDeleteRes=false;
	private AlertDialog dialog;
	private List<NotifyClassBean> classBeans= new ArrayList<NotifyClassBean>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notify_issue_layout);

		initView();
		initData();
	}

	private void initView() {
		initTitleBar("发布通知");

		setTitleBackground(this.getResources().getDrawable(R.drawable.leave_title_bg));

		setTitleNextBtnClickListener("发布", new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (notifyTitleEditView.getText().toString().isEmpty()) {
					Toast.makeText(NotifyIssueActivity.this, "通知标题不能为空", Toast.LENGTH_SHORT).show();
					return;
				} else {
					
					if (classBeans!=null && classBeans.size()>0) {
						if (classBeans.size()==1) {//一个班级
							if (picLists.size() > 1) {//是否有图片
								uploadPic(String.valueOf(classBeans.get(0).classId));
							}else {
								issueNotify(String.valueOf(classBeans.get(0).classId),null);
							}
							
						}else {//多个班级需要选择
							showClassSelDialog();
						}
					}else {
						Toast.makeText(NotifyIssueActivity.this, "用户对应班级数据异常或没有对应班级！", Toast.LENGTH_SHORT).show();
					}
					
				}
			}
		});

		setTitleBackBtnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		setMoveBackEnable(false);

		notifyTitleEditView = (EditText) findViewById(R.id.notify_title_edit_view);
		notifyContentEditView = (EditText) findViewById(R.id.notify_content_edit_view);
		switchView = (Switch) findViewById(R.id.switch_view);

		switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if (isChecked) {
					switchTag = 1;
				} else {
					switchTag = 0;
				}

			}
		});

		addPicGridView = (HorizontalListView) findViewById(R.id.add_pic_gridView);
		addPicGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(isDeleteRes){
					isDeleteRes=false;
					return;
				}
				
				if (position == 0) {// 添加图片
					Intent intent = new Intent(NotifyIssueActivity.this, PictureAddActivity.class);
					intent.putExtra("img_count", picLists.size() + 1);
					intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 14);
					NotifyIssueActivity.this.startActivityForResult(intent, 1);
				} else {// 预览图片
					ArrayList<String> arrayList = new ArrayList<String>();
					for (int i = 1; i < picLists.size(); i++) {
						if (picLists.get(i).startsWith("http")) {
							arrayList.add(picLists.get(i));
						} else {
							arrayList.add("file://" + picLists.get(i));
						}
					}
					Intent intent = new Intent(NotifyIssueActivity.this, NotifyPicPreviewActivity.class);
					Bundle bundle = new Bundle();
					bundle.putInt(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_POSITION, position - 1);
					bundle.putStringArrayList(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST,
							arrayList);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
	}



	private void initData() {
		getClassListData();
		picLists.add("");
		mAdapter = new NotifyPicAdapter(NotifyIssueActivity.this, picLists);
		addPicGridView.setAdapter(mAdapter);
		mAdapter.setDeleteClickListener(new DeleteClickListener() {

			@Override
			public void deleteClick(View v, int position) {
				isDeleteRes=true;
				picLists.remove(position);
				mAdapter.notifyDataSetChanged();
			}
		});

		
	}

	private void uploadPic(final String ids) {
		showMyProgressDialog();
		final List<String> picListTags=new ArrayList<String>();
		picListTags.addAll(picLists);
		picListTags.remove(0);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				ImgUploadUtil.uploadNotifyImgs(NotifyIssueActivity.this, picListTags, new UploadImgListener() {


					@Override
					public void uploadImgSuccessListener(List<Integer> imgFileIdList) {
						List<Integer> picIds= new ArrayList<Integer>();
						if (imgFileIdList != null && imgFileIdList.size() > 0) {
							for (int j = 0; j < imgFileIdList.size(); j++) {
								picIds.add(imgFileIdList.get(j));
							}
						}
						
						
						issueNotify(ids,getPicIds(picIds));
					
					
					}

					@Override
					public void uploadImgErrorListener(String errorStr) {
						Toast.makeText(NotifyIssueActivity.this, errorStr, Toast.LENGTH_SHORT).show();
						hideMyProgressDialog();
					}
				});

			}
		}, 200);
		
	}

	// 发布通知
	private void issueNotify(String groupIds, String picIdStr) {
		
		setBeansChecked();
		
		if (groupIds==null || groupIds.isEmpty()) {
			Toast.makeText(NotifyIssueActivity.this, "用户对应班级数据异常或未选中任何班级！", Toast.LENGTH_SHORT).show();
			return;
		}
		
		showMyProgressDialog();
		String endTime=getEndTime();

		Log.e("run", String.valueOf(notifyTitleEditView.getText())+"\n"+String.valueOf(notifyContentEditView.getText())+"\n"+String.valueOf(switchTag)+"\n"+endTime+"\n"+groupIds+"\n"+picIdStr);
		String urlString = "";
		urlString = SmartCampusUrlUtils.getSchoolInformUrl(null, String.valueOf(notifyTitleEditView.getText()),
				String.valueOf(notifyContentEditView.getText()), String.valueOf(switchTag), endTime, groupIds, null);

		// List<NameValuePair> params = new ArrayList<NameValuePair>();
		// params.add(new BasicNameValuePair("startTime", startTime));

		Log.d(TAG, "urlString=" + urlString);

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								Toast.makeText(NotifyIssueActivity.this, "通知发布成功！", Toast.LENGTH_SHORT).show();
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(NotifyIssueActivity.this);
								Toast.makeText(NotifyIssueActivity.this, "登录已失效!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(NotifyIssueActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
										.show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Toast.makeText(NotifyIssueActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideMyProgressDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void setBeansChecked() {
		for (int i = 0; i < classBeans.size(); i++) {
			classBeans.get(i).isChecked=true;
		}
	}

	private String getPicIds(List<Integer> picIds) {
		String picIdStr ="";
		for (int i = 0; i < picIds.size(); i++) {
			picIdStr+=picIds.get(i)+",";
		}
		if (!picIdStr.isEmpty()) {
			picIdStr=picIdStr.substring(0, picIdStr.length()-1);
		}
		return picIdStr;
	}


	//获取当前时间
	private String getEndTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s=format.format(new Date());
		return s;
	}
	
	private void getClassListData() {
		showMyProgressDialog();
		GroupListData.getClassListData(this, new DataResponse.Listener<ArrayList<GroupInfo>>() {

			

			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				hideMyProgressDialog();
				classBeans.clear();
				for (int i = 0; i < classGroups.size(); i++) {
					NotifyClassBean bean=new NotifyClassBean(classGroups.get(i).getName(), classGroups.get(i).getId(), true);
					classBeans.add(bean);
				}

			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				hideMyProgressDialog();

			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				hideMyProgressDialog();
			}
		});
	}

	private void showClassSelDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final View layout = getLayoutInflater().inflate(R.layout.dialog_notify_class_layout, null);
		TextView negativeBut = (TextView) layout.findViewById(R.id.negative_but);
		TextView positiveBut = (TextView) layout.findViewById(R.id.positive_but);
		ListView classLv=(ListView) layout.findViewById(R.id.class_listview);
		
		final NotifyClassAdapter mClassAdapter=new NotifyClassAdapter(NotifyIssueActivity.this, classBeans);
		classLv.setAdapter(mClassAdapter);
		
		/*classLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
			}
		});*/
		
		mClassAdapter.setOnCheckedListener(new OnCheckedListener() {
			
			@Override
			public void onChecked(int i, boolean b) {
				classBeans.get(i).isChecked=b;
				mClassAdapter.notifyDataSetChanged();
			}
		});
		
		negativeBut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		positiveBut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String idStr=getClassIdStr(classBeans);
				if (idStr.isEmpty()) {
					Toast.makeText(NotifyIssueActivity.this, "未选中任何班级!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (picLists.size() > 1) {//是否有图片
					uploadPic(idStr);
				}else {
					issueNotify(idStr,null);
				}
				
				dialog.dismiss();
			}
		});
		
		
		builder.setView(layout);

		dialog = builder.create();
		
		dialog.show();
	}
	


	private String getClassIdStr(List<NotifyClassBean> mDatas) {
		String classIdStr ="";
		for (int i = 0; i < mDatas.size(); i++) {
			if (mDatas.get(i).isChecked) {
				classIdStr+=mDatas.get(i).classId+",";
			}
		}
		if (!classIdStr.isEmpty()) {
			classIdStr=classIdStr.substring(0, classIdStr.length()-1);
		}
		return classIdStr;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				List<String> datas = data.getStringArrayListExtra("img_data");
				if (datas != null) {
					picLists.addAll(datas);
					mAdapter.notifyDataSetChanged();
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(NotifyIssueActivity.this, "", "...loading...");
		}
	}

	private void hideMyProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
}
