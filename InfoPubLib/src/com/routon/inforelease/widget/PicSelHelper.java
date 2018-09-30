package com.routon.inforelease.widget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.routon.inforelease.util.ImageUtils;
import com.routon.widgets.Toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class PicSelHelper {
	private static final String TAG = "PicSelHelper";
	
	public static final int PHOTO_CARMERA = 1001;

	public static final int PHOTO_PICK = 1002;

	public static final int PHOTO_CUT = 1003;
	
	private Activity mActivity = null;
	
	public PicSelHelper(Activity activity){
		mActivity = activity;
		mImageUri = createImageUri();
	}
	
	private int mCutImageW = 1920;
	private int mCutImageH = 1080;
	
	public void setCutImageMaxSize(int width,int height){
		mCutImageW = width;
		mCutImageH = height;
	}
	
	public boolean handleActivityResult(int requestCode,int resultCode, Intent data){
		switch (requestCode) {
		case PHOTO_CARMERA:
			if (resultCode == Activity.RESULT_OK) {
//				Log.d(TAG, "start camera crop imageUri:" + uri);
				cropImageUri(mImageUri,mImageUri);
			}
			return true;
		case PHOTO_PICK:
			if (resultCode == Activity.RESULT_OK) {
//				Log.d(TAG, "start camera crop imageUri:" + uri);
				cropImageUri(data.getData(),mImageUri);
			}
			return true;
		case PHOTO_CUT:
			if( mImageUri != null ){
				Bitmap bitmap = ImageUtils.loadBitmap(mImageUri.getPath(), mCutImageW, mCutImageH);
				if( bitmap == null ){
					return false;
				}	
				Log.d(TAG,"handleActivityResult bitmap:"+bitmap.getWidth()+","+bitmap.getHeight());
				ImageUtils.saveBitmap(bitmap, mImageUri);		
			}
			return false;
		default:
			return false;
		}
	}
	
	// 调用系统相册
	private void startPick() {
		Log.d(TAG, "startPick");

		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
		mActivity.startActivityForResult(intent, PHOTO_PICK);
	}
	 
	public void setDestUri(Uri uri){
		if( mImageUri == null ){
			return;
		}
		mImageUri = uri;
	}
	
	private boolean mCircleCrop = false;
	public void setCircleCrop(boolean circelCrop){
		mCircleCrop = circelCrop;
	}
	
	private void cropImageUri(Uri uri,Uri imageUri) {
		Log.d(TAG, "cropImageUri uri:"+uri+",imageUri:"+imageUri);
		Intent intent = new Intent("com.android.camera.action.CROP");

		intent.setDataAndType(uri, "image/*");

		intent.putExtra("crop", "true");
		if( mCircleCrop == true ){
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
		}else{
//			intent.putExtra("aspectX", 3);
//			intent.putExtra("aspectY", 4);
		}

		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

		intent.putExtra("return-data", false);

		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

		intent.putExtra("noFaceDetection", true); // no face detection

		mActivity.startActivityForResult(intent, PHOTO_CUT);

	}
	
	/**
	 * 获取图片的路径
	 */
	private Uri createImageUri() {
		File outputImage = new File(Environment.getExternalStorageDirectory(),"tempImage.jpg");
		if (outputImage.exists()) {
			outputImage.delete();
		}
		try {
			outputImage.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Uri.fromFile(outputImage);
	};
	
	public void startCamera(Uri imageUri) {

		if (ImageUtils.isHasSdcard()) {
			Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// 指定调用相机拍照后照片的存储路径
			takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			mActivity.startActivityForResult(takePhotoIntent, PHOTO_CARMERA);
		} else {
			Toast.makeText(mActivity, "未检测到sd卡",
					Toast.LENGTH_LONG).show();
		}
	}
	
	public Uri getImageUri(){
		return mImageUri;
	}
	
	private Uri mImageUri = null;
		
	public void showAddPicDialog() {
		String[] takePhotoitems = new String[] { "选择本地图片", "拍照" };
		new AlertDialog.Builder(mActivity)
				.setTitle("添加图片")
				.setItems(takePhotoitems,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								switch (which) {
								case 0:
									// 调用系统相册
									startPick();
									break;

								case 1:
									// 拍照
									startCamera(mImageUri);
									break;
								}
							}
						})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}
}
