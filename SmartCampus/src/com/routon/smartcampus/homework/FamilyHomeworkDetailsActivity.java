package com.routon.smartcampus.homework;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.edurelease.R;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class FamilyHomeworkDetailsActivity extends BaseActivity {

	private static String TAG = "FamilyHomeworkDetailsActivity";
	private TextView homeworkDescriptionTv;
	private TextView homeworkRemarkTv;
	private ListView homeworkImgLv;

	private ProgressDialog progressDialog;
	private FamilyHomeworkBean bean;
	private String dateStr;
	private TextView titleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_homework_details_layout);

		Intent intent = getIntent();
		bean = (FamilyHomeworkBean) intent.getSerializableExtra(MyBundleName.HOMEWORK_BEAN);
		dateStr = intent.getStringExtra(MyBundleName.HOMEWORK_DATE);

		initView();
		initData();
	}

	private void initView() {
		ImageView backBtn = (ImageView) findViewById(R.id.back_btn);
		titleView = (TextView) findViewById(R.id.title_view);

		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				FamilyHomeworkDetailsActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};

		backBtn.setOnClickListener(mBackListener);

		homeworkDescriptionTv = (TextView) findViewById(R.id.homework_description_tv);
		homeworkRemarkTv = (TextView) findViewById(R.id.homework_remark_tv);
		homeworkImgLv = (ListView) findViewById(R.id.homework_img_lv);

		homeworkImgLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Intent intent = new
				// Intent(FamilyHomeworkDetailsActivity.this,
				// RemarkImagePreviewActivity.class);
				// Bundle bundle = new Bundle();
				// bundle.putStringArrayList(MyBundleName.BADGE_REMARK_PIC_LIST,
				// bean.homeworkImgUrls);
				// bundle.putInt(MyBundleName.BADGE_REMARK_PIC_POSITION,
				// position);
				// intent.putExtras(bundle);
				// startActivity(intent);
			}
		});

	}

	private void initData() {
		titleView.setText(dateStr + " " + bean.courseName + "作业 " + bean.teacherName + "老师");
		homeworkDescriptionTv.setText(bean.description);
		if (bean.remark != null && !bean.remark.trim().equals("") && !bean.remark.trim().equals("null")) {
			homeworkRemarkTv.setText("附注：" + bean.remark);
		}

		FamilyHomeworkImgListViewAdapter adapter = new FamilyHomeworkImgListViewAdapter(
				FamilyHomeworkDetailsActivity.this, bean.homeworkImgUrls);
		homeworkImgLv.setAdapter(adapter);
	}

	// 家长检查作业
	/*private void checkHomework(String studentId, String homeworkId, String isCheck) {

		String urlString = SmartCampusUrlUtils.getFamilyCheckHomeworkUrl(studentId, homeworkId, isCheck);

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

							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(FamilyHomeworkDetailsActivity.this);
								finish();
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(FamilyHomeworkDetailsActivity.this, response.getString("msg"),
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
						Toast.makeText(FamilyHomeworkDetailsActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	*/
	private void showLoadDialog(){
		if( progressDialog == null || !progressDialog.isShowing()){
			progressDialog = ProgressDialog.show(FamilyHomeworkDetailsActivity.this, "", "...loading...");
		}
	}
	
	private void hideLoadDialog(){
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

}
