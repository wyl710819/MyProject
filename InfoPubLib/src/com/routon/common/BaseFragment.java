package com.routon.common;

import com.routon.inforelease.R;
import com.routon.inforelease.usercontrol.UserListFragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class BaseFragment extends Fragment {
	private boolean mReturnEnable = false;
	
	public void setReturnEnable(boolean returnEnable){
		mReturnEnable = true;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.setTitleBackBtnHide();
		
		if( mReturnEnable == true ){	
			this.setTitleBackBtnClickListener(new View.OnClickListener() {	
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					BaseFragment.this.getActivity().finish();
				}
			});
		}
	}
	
	public Dialog mWaitDialog = null;
	
	public void hideProgressDialog(){
		if( mWaitDialog != null ){
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}
	}
	
	public void showProgressDialog(){
		if( mWaitDialog == null ){
			mWaitDialog = new Dialog(getOwnActivity(),R.style.new_circle_progress);    
			mWaitDialog.setContentView(R.layout.dialog_wait);    
			mWaitDialog.setCancelable(true);
			mWaitDialog.show();
		}
	}
	
	public Activity getOwnActivity(){
		return this.getActivity();
	}
	
	protected void reportToast(int resId) {
		String text = this.getResources().getString(resId);
		reportToast(text);
	}  
      
	
	public void startActivity(Intent intent) {
	    getOwnActivity().startActivity(intent);
	    getOwnActivity().overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
	}
	
	public void startActivityForResult(Intent intent, int requestCode){
		getOwnActivity().startActivityForResult(intent,requestCode);
		getOwnActivity().overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
	}

	
	protected void reportToast(String text) {
		Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	public void initTitleBar(int resid){
		if( this.getView() == null ) return;
		TextView titleTextView = (TextView) getView().findViewById(R.id.title);
		if(titleTextView != null){
			titleTextView.setText(resid);
			titleTextView.setVisibility(View.VISIBLE);
		}
	}
	
	public void initTitleBar(String text){
		if( this.getView() == null ) return;
		TextView titleTextView = (TextView) getView().findViewById(R.id.title);
		if(titleTextView != null){
			titleTextView.setText(text);
			titleTextView.setVisibility(View.VISIBLE);
		}
	}
	
	public void setTitleBackBtnClickListener(OnClickListener listener){
		if( this.getView() == null ) return;
		ImageButton	backBtn = (ImageButton) getView().findViewById(R.id.back_btn);
		if(backBtn != null){
			backBtn.setVisibility(View.VISIBLE);
			backBtn.setOnClickListener(listener);
		}
	}
	
	public void setTitleBackBtnHide(){
		if( this.getView() == null ) return;
		ImageButton	backBtn = (ImageButton) getView().findViewById(R.id.back_btn);
		if(backBtn != null){
			backBtn.setVisibility(View.INVISIBLE);
		}
	}
	
	public void setTitleNextImageBtnHide(){
		if( this.getView() == null ) return;
		ImageButton nextButton = (ImageButton) getView().findViewById(R.id.next_step);
		if(nextButton != null){
			nextButton.setVisibility(View.INVISIBLE);
		}
	}
	
	public void setTitleNextImageBtnClickListener(int resId,OnClickListener listener){
		if( this.getView() == null ) return;
		ImageButton nextButton = (ImageButton) getView().findViewById(R.id.next_step);
		if(nextButton != null){
			nextButton.setImageResource(resId);
			nextButton.setVisibility(View.VISIBLE);
			nextButton.setOnClickListener(listener);
		}
	}
	
	public void setTitleNextBtnClickListener(String title,OnClickListener listener){
		if( this.getView() == null ) return;
		Button nextButton = (Button) getView().findViewById(R.id.next_step_tv);
		if(nextButton != null){
			nextButton.setText(title);
			nextButton.setVisibility(View.VISIBLE);
			nextButton.setOnClickListener(listener);
		}
	}
}
