package com.routon.smartcampus.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.routon.scanner.CaptureActivity;
import com.routon.scanner.CaptureResultListener;
import com.routon.widgets.Toast;

public class ScreenGroupChangeActivity extends CaptureActivity{
	
	public static final String INTENT_TID_DATA = "tid"; 
	public static final String TAG = "ScreenGroupChangeActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setCaptureTitleAndText("智慧屏二维码","请扫描智慧屏上的二维码");
		this.setCaptureResultListener(new CaptureResultListener(){

			@Override
			public void onCapture(String resultString) {
				// TODO Auto-generated method stub
				// btmac=22:22:25:F9:4E:00&model=未初始化 JS DC2 W 4G L R C BK BP2 PT test&version=CI_24T-0100-5959M&termid=000000A00004
				Log.d(TAG, resultString);
				String[] result = resultString.split("=");
				String tid = result[result.length-1];
				if (!TextUtils.isEmpty(tid)) {
					Intent intent = new Intent();
					intent.putExtra(INTENT_TID_DATA, tid);
					Log.d(TAG, "tid:" + tid);
					ScreenGroupChangeActivity.this.setResult(Activity.RESULT_OK, intent);
					ScreenGroupChangeActivity.this.finish();
					return;
				}
				Toast.makeText(ScreenGroupChangeActivity.this, "请扫描正确的智慧屏二维码", Toast.LENGTH_SHORT).show();
//				restartPreviewAfterDelay(3000);//3秒后重新扫描
				ScreenGroupChangeActivity.this.finish();
				return;
			}
			
		});
	
	}
}
