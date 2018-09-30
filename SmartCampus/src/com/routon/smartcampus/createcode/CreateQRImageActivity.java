package com.routon.smartcampus.createcode;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.edurelease.R;
import com.routon.smartcampus.utils.FileUtil;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.utils.QRCodeUtil;
import com.routon.widgets.Toast;

public class CreateQRImageActivity extends CustomTitleActivity{
	private Bitmap mBitmap = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_qrimage);

		initTitleBar(R.string.create_code_title);
		
		final String path = this.getExternalCacheDir()+"/code.jpg";
		
		setTitleNextBtnClickListener(this.getResources().getString(R.string.save),new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				save(path);
			}
		});
		String className = this.getIntent().getStringExtra(MyBundleName.CLASS_NAME);
		int classId = this.getIntent().getIntExtra(MyBundleName.CLASS_ID,0);
		
		String imageContent = "http://edu.wanlogin.com:8086/edu/cmd/app/dl/index_parent.html?;";
		if( InfoReleaseApplication.authenobjData.schools != null && InfoReleaseApplication.authenobjData.schools.length > 0 ){
			imageContent += InfoReleaseApplication.authenobjData.schools[0];
		}
		imageContent += className+";";
		imageContent += String.valueOf(classId);
		String centerTextContent = "";
		if( InfoReleaseApplication.authenobjData.schools != null && InfoReleaseApplication.authenobjData.schools.length > 0 ){
			centerTextContent += InfoReleaseApplication.authenobjData.schools[0]+"\n";
		}
		centerTextContent  = centerTextContent+ className;
		
		ImageView imageview = (ImageView) this.findViewById(R.id.qriv);
		
		DisplayMetrics dm = getResources().getDisplayMetrics();
		Log.d("CreateQRImageActivity","imageContent:"+imageContent+",centerTextContent:"+centerTextContent+",dm.scaledDensity:"+dm.scaledDensity);
		
		mBitmap = QRCodeUtil.createQRTextImage(imageContent,centerTextContent,1200, 
				1200,  45,path);
		imageview.setImageBitmap(mBitmap);
		
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
	}
	
	protected void onDestroy(){
		super.onDestroy();
		if( mBitmap != null ){
			mBitmap.recycle();
			mBitmap = null;
		}
	}
	
	void save(String path){
		if( mBitmap == null ){
			Toast.makeText(this, "图片保存失败", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String imgSaved = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, "title", "description");
		
		if( imgSaved != null ){
			Toast.makeText(this, "图片已保存至相册", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "文件保存失败", Toast.LENGTH_SHORT).show();
		}

	}
}
