package com.routon.inforelease.plan.create;

import java.io.File;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.routon.ad.element.StringUtils;
import com.routon.ad.pkg.FileGetTask;
import com.routon.ad.pkg.HttpGetTask;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.util.ImageUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PicPreviewActivity extends Activity {
	private String TAG = "PicPreviewActivity";

	private String imagePath;
	private ImageView localImageView;
	private int mScreenHeight;
	private int mScreenWidth;
	private Bitmap bitmap;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.picutre_preview_layout);
		Bundle bundle = getIntent().getExtras();
		imagePath = bundle.getString("path");
		progressDialog = ProgressDialog.show(this, "", "...Loading...");

		
		 
		initView();

		if (imagePath.contains("http")) {
			loadImage(imagePath);
		} else {
			Message msg = Message.obtain(mHandler, 1);
			mHandler.sendMessage(msg);
		}

	}

	private void initView() {
		
		DisplayMetrics outMetrics = new DisplayMetrics();
		 getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		 mScreenHeight = outMetrics.heightPixels;
		 mScreenWidth = outMetrics.widthPixels;
		 titleLayout = (RelativeLayout) findViewById(R.id.titlebar);
		TextView titleText = (TextView) findViewById(R.id.title);
		ImageButton backButton = (ImageButton) findViewById(R.id.back_btn);
		ImageButton nextButton = (ImageButton) findViewById(R.id.next_step);
		titleText.setText(R.string.preview_pics_title);
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		nextButton.setVisibility(View.GONE);
//		NetworkImageView networkImageView = (NetworkImageView) findViewById(R.id.network_image);
//		networkImageView.setVisibility(View.GONE);
		localImageView = (ImageView) findViewById(R.id.local_image);
		localImageView.setScaleType(ScaleType.FIT_CENTER);

	}

	private void loadImage(String url) {
		String name = StringUtils.getFileName(url);
		File file = new File(getCacheDir(), name);
		String path = file.getPath();
		if (file.exists()) {
			bitmap = BitmapFactory.decodeFile(path);

			Message msg = Message.obtain(mHandler, 0, bitmap);
			mHandler.sendMessage(msg);
			return;
		}
		FileGetTask task = new FileGetTask(url, path, url);
		task.setOnHttpGetTaskListener(new HttpGetTask.OnHttpGetTaskListener() {

			@Override
			public void onTaskStarted(HttpGetTask task) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTaskFinished(HttpGetTask task, int code) {

				if (code == HttpGetTask.ERR_NONE) {
					FileGetTask t = (FileGetTask) task;
					String path = t.getFilePath();
					bitmap = BitmapFactory.decodeFile(path);
				}
				Message msg = Message.obtain(mHandler, 0, bitmap);
				mHandler.sendMessage(msg);
			}
		});
		new Thread(task).start();
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				onUpdateImage(bitmap);
				break;

			case 1:
				setImage();
				break;

			}
		}
	};

	private RelativeLayout titleLayout;

	private void setImage() {

		String path = imagePath.replaceFirst("file://", "");

		bitmap = ImageUtils.loadBitmap(path, mScreenWidth, mScreenHeight);
		if (progressDialog != null) {
			progressDialog.dismiss();
		}

		if (bitmap == null) {
			return;
		}

		localImageView.setImageBitmap(bitmap);
	}

	private void onUpdateImage(Bitmap bitmap) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		if (bitmap == null) {
			return;
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if (w > h) {
			RelativeLayout llPic=(RelativeLayout) findViewById(R.id.ll_pic);
			int windowWidth=getWindowManager().getDefaultDisplay().getWidth();
			int windowHeight=getWindowManager().getDefaultDisplay().getHeight();
			FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(windowHeight,windowWidth);
			llPic.setLayoutParams(layoutParams);
			llPic.setPivotX(windowWidth/2);
			llPic.setPivotY(windowWidth/2);
			llPic.setRotation(90);
		} else {
		}
		localImageView.setImageBitmap(bitmap);
	}
	


}
