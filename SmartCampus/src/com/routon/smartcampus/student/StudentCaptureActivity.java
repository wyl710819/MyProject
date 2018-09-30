package com.routon.smartcampus.student;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.routon.scanner.CaptureActivity;
import com.routon.scanner.CaptureResultListener;
import com.routon.smartcampus.utils.QRCodeUtil;
import com.routon.widgets.Toast;


public class StudentCaptureActivity extends CaptureActivity{
	public static final String  INTENT_SID_DATA     = "sid"; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setCaptureTitleAndText("学生卡二维码","请扫描学生卡上的二维码");
		this.setCaptureResultListener(new CaptureResultListener(){

			@Override
			public void onCapture(String resultString) {
				// TODO Auto-generated method stub
				
				String[] result = QRCodeUtil.decodeQR(resultString, StudentCaptureActivity.this);
				if( result != null && result.length >= 6 ){
					if ( TextUtils.isDigitsOnly(result[5]) == true  ){
						int sid = Integer.parseInt(result[5]);
						if( sid > 0 ){
							Intent intent = new Intent();
							intent.putExtra(INTENT_SID_DATA, sid);
//							Log.d("StudentCaptureActivity","sid:"+sid);
							StudentCaptureActivity.this.setResult(Activity.RESULT_OK, intent);
							StudentCaptureActivity.this.finish();
							return;
						}
					}
					
				}
				Toast.makeText(StudentCaptureActivity.this, "请扫描正确的学生卡二维码", Toast.LENGTH_SHORT).show();
//				restartPreviewAfterDelay(3000);//3秒后重新扫描
				StudentCaptureActivity.this.finish();
				return;
			}
			
		});
	
	}
}
