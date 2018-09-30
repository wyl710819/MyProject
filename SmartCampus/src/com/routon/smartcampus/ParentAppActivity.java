package com.routon.smartcampus;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.widgets.Toast;

public class ParentAppActivity extends CustomTitleActivity{
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ImageView imageview = new ImageView(this);
		imageview.setImageResource(R.drawable.parent_app);
		this.setContentView(imageview);
		
		this.initTitleBar(R.string.menu_parentapp);
		
		setTitleNextBtnClickListener(this.getResources().getString(R.string.save),new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				save();
			}
		});
	}
	
	void save(){
		Resources res = this.getResources();
		BitmapDrawable d = (BitmapDrawable) res.getDrawable(R.drawable.parent_app);
		Bitmap img = d.getBitmap();
		if( img == null ){
			Toast.makeText(this, "图片保存失败", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String imgSaved = MediaStore.Images.Media.insertImage(getContentResolver(), img, "title", "description");
		
		if( imgSaved != null ){
			Toast.makeText(this, "图片已保存至相册", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "文件保存失败", Toast.LENGTH_SHORT).show();
		}

	}
}
