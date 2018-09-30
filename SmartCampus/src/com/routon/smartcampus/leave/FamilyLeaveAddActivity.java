package com.routon.smartcampus.leave;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseFragmentActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.widgets.Toast;

public class FamilyLeaveAddActivity extends BaseFragmentActivity implements OnClickListener{
	private ImageView backImg;
	private Button startTimeBtn;
	private Button endTimeBtn;
	private Button sendRequestBtn;
	private EditText reasonEdt;
	private TextView teacherTv;
	private CustomDatePicker startDatePicker;
	private CustomDatePicker endDatePicker;
	
	private String startTime;
	private String endTime;
	private String headTeacher;
	private StudentBean mStudentBean;
	private String now;
	
	private static final String TAG = "FamilyLeaveAddActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}
	
	public void initView(){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_family_leave_add);
		setMoveBackEnable(true);
		backImg = (ImageView)findViewById(R.id.img_family_leave_add_back);
		startTimeBtn = (Button)findViewById(R.id.btn_family_leave_select_startTime);
		endTimeBtn = (Button)findViewById(R.id.btn_family_leave_select_endTime);
		sendRequestBtn = (Button)findViewById(R.id.btn_family_leave_add_commit);
		reasonEdt = (EditText)findViewById(R.id.edt_family_leave_add_reason);
		teacherTv = (TextView)findViewById(R.id.tv_family_leave_add_teacher);
		backImg.setOnClickListener(this);
		startTimeBtn.setOnClickListener(this);
		endTimeBtn.setOnClickListener(this);
		sendRequestBtn.setOnClickListener(this);
	}
	
	public void initData(){
		initDatePicker();
		mStudentBean = (StudentBean) getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
		headTeacher = getIntent().getStringExtra("headTeacher");
		if(!TextUtils.isEmpty(headTeacher)){
			teacherTv.setText(headTeacher);
		}
	}
	
	private void initDatePicker() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        now = sdf.format(new Date());
        Calendar calendar = Calendar.getInstance();
        String default_start = sdf.format(calendar.getTime());
        startTime = default_start+":00";
        startTimeBtn.setText(default_start);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String default_end = sdf.format(calendar.getTime());
        endTime = default_end+":00";
        endTimeBtn.setText(default_end);
        calendar.add(Calendar.YEAR, 1);
        String end = sdf.format(calendar.getTime());
        startDatePicker = new CustomDatePicker(this, new CustomDatePicker.ResultHandler() {
            @Override
            public void handle(String time) { // 回调接口，获得选中的时间
            	startTime = time+":00";
                startTimeBtn.setText(time);
            }
        }, now, end); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
        startDatePicker.showSpecificTime(true); // 显示时和分
        startDatePicker.setIsLoop(false); // 不允许循环滚动
        endDatePicker = new CustomDatePicker(this, new CustomDatePicker.ResultHandler() {
            @Override
            public void handle(String time) { // 回调接口，获得选中的时间
            	endTime = time+":00";
                endTimeBtn.setText(time);
            }
        }, now, end); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
        endDatePicker.showSpecificTime(true); // 显示时和分
        endDatePicker.setIsLoop(false); // 不允许循环滚动
    }

	public void sendLeaveRequest(){
		String reason = reasonEdt.getText().toString();
		if(TextUtils.isEmpty(reason)){
			Toast.makeText(this, "请假事由不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if(TextUtils.isEmpty(startTime)){
			Toast.makeText(this, "开始时间不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if(TextUtils.isEmpty(endTime)){
			Toast.makeText(this, "结束时间不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate;
		Date endDate;
		try {
			startDate = df.parse(startTime);
			endDate = df.parse(endTime);
			if(startDate.getTime() > endDate.getTime()){
				Toast.makeText(this, "开始时间大于结束时间", Toast.LENGTH_SHORT).show();
				return;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String url = SmartCampusUrlUtils.getFamilyleaveAddUrl(mStudentBean.sid, reason);
		Log.d(TAG, "url="+url);
		List<NameValuePair> params=new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("endTime", endTime));
		params.add(new BasicNameValuePair("startTime", startTime));
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Method.POST, url, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						int code = response.optInt("code");
						String msg = response.optString("msg");
						if (code == 0) {								
							Toast.makeText(FamilyLeaveAddActivity.this, "提交请假成功", Toast.LENGTH_SHORT).show();
							finish();
						} else if (code == -2) {
							InfoReleaseApplication.returnToLogin(FamilyLeaveAddActivity.this);
							Toast.makeText(FamilyLeaveAddActivity.this, "登录已失效!", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(FamilyLeaveAddActivity.this, msg, Toast.LENGTH_LONG).show();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d(TAG, "error="+error);
						Toast.makeText(FamilyLeaveAddActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();						
					}
				});

		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_family_leave_add_back:
			finish();
			break;
		case R.id.btn_family_leave_select_startTime:
			startDatePicker.show(now);
			break;
		case R.id.btn_family_leave_select_endTime:
			endDatePicker.show(now);
			break;
		case R.id.btn_family_leave_add_commit:
			sendLeaveRequest();
			break;
		default:
			break;
		}
	}

}
