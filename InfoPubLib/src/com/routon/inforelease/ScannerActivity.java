package com.routon.inforelease;

import android.content.Intent;
import android.os.Bundle;

import com.routon.scanner.CaptureActivity;
import com.routon.scanner.CaptureCompleteListener;

public class ScannerActivity extends CaptureActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setCaptureCompleteListerner(new CaptureCompleteListener() {
			
			@Override
			public void onCapture(String terminalType, String btMac, String termId) {
				// TODO Auto-generated method stub
				Intent data = new Intent();
				data.putExtra("blueMac", btMac);
				data.putExtra("termId", termId);
				setResult(RESULT_OK, data);
			}
		});
	}
}
