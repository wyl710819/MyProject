package com.routon.inforelease.plan.create.pictureAdd;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.R;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.MaterialParams;
import com.routon.inforelease.plan.create.PicPreviewActivity;
import com.routon.inforelease.plan.create.pictureAdd.AddPicSelAdapter.OnCamerClicked;
import com.routon.inforelease.plan.create.pictureAdd.AddPicSelAdapter.OnImageClicked;
import com.routon.inforelease.plan.create.pictureAdd.ListImageDirPopupWindow.OnImageDirSelected;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStack;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStringRequest;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class PictureAddActivity extends CustomTitleActivity implements OnImageDirSelected {
	private final static String TAG = "PictureAddActivity";
	private ProgressDialog mProgressDialog;

	private int mFileType = MaterialParams.TYPE_AD_PICTURE;

	private final static int SMART_CAMPUS_REMARK_TYPE = 14;
	private final static int SMART_CAMPUS_HOMEWORK_TYPE = 15;
	/**
	 * 存储文件夹中的图片数量
	 */
	private int mPicsSize;
	/**
	 * 图片数量最多的文件夹
	 */
	private File mImgDir;
	/**
	 * //目录下的所有图片
	 */
	private List<String> mImgs;

	private GridView mGirdView;
	private AddPicSelAdapter mAdapter;
	/**
	 * 临时的辅助类，用于防止同一个文件夹的多次扫描
	 */
	private HashSet<String> mDirPaths = new HashSet<String>();

	/**
	 * 扫描拿到所有的图片文件夹
	 */
	private List<ImageFloder> mImageFloders = new ArrayList<ImageFloder>();

	// 扫描到的所有图片
	private ArrayList<String> mAllImgs = new ArrayList<String>();
	private RelativeLayout mBottomLy;

	private TextView mChooseDir;
	private TextView mImageCount;
	int totalCount = 0;

	private int mScreenHeight;

	private ListImageDirPopupWindow mListImageDirPopupWindow;

	public static final String exteranlPath = "/infoRelease_photos/";

	public static String getExternalStorageDirectory() {
		return Environment.getExternalStorageDirectory() + exteranlPath;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mProgressDialog.dismiss();
			// 为View绑定数据
			data2View();
			// 初始化展示文件夹的popupWindw
			initListDirPopupWindw();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mFileType = bundle.getInt("fileType", MaterialParams.TYPE_AD_PICTURE);
		}

		setContentView(R.layout.add_picture_layout);

		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		mScreenHeight = outMetrics.heightPixels;

		this.initTitleBar(R.string.add_pics_title);
		
		if (mFileType==SMART_CAMPUS_REMARK_TYPE || mFileType==SMART_CAMPUS_HOMEWORK_TYPE) {
			
			final int imgCount=bundle.getInt("img_count");
			
			this.setTitleNextBtnClickListener("完成", new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mAdapter.mSelectedImage.size() > 0) {
						if (mFileType==SMART_CAMPUS_HOMEWORK_TYPE) {
							if (mAdapter.mSelectedImage.size()>4-imgCount) {
								com.routon.widgets.Toast.makeText(getBaseContext(), "可添加图片数量为"+(4-imgCount)+"张！", com.routon.widgets.Toast.LENGTH_SHORT).show();
								return;
							}
						}else {
							if (mAdapter.mSelectedImage.size()>11-imgCount) {
								com.routon.widgets.Toast.makeText(getBaseContext(), "可添加图片数量为"+(11-imgCount)+"张！", com.routon.widgets.Toast.LENGTH_SHORT).show();
								return;
							}
						}
						
						ArrayList<String> imgDatas=new ArrayList<String>();
						for (int i = 0; i < mAdapter.mSelectedImage.size(); i++) {
							imgDatas.add(mAdapter.mSelectedImage.get(i));
						}
						
						Intent intent = new Intent();
						intent.putStringArrayListExtra("img_data", imgDatas);
						setResult(RESULT_OK, intent);
					}
					finish();
				}
			});
		}else {
			this.setTitleNextBtnClickListener("上传", new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mAdapter.mSelectedImage.size() > 0) {
						sendImages(mAdapter.mSelectedImage);
						return;
					}

					Toast.makeText(PictureAddActivity.this, R.string.no_pics_error, Toast.LENGTH_SHORT).show();
				}
			});
		}
		
		initView();
		getImages();
		
		// 生成图片大小转换缓存目录
		String out_file_path = getExternalStorageDirectory();
		File dir = new File(out_file_path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	private int MY_PERMISSIONS_REQUEST_CAMERA = 100;
	private boolean hasAuthority(){
		Log.i(TAG, "-----initAuthority--------");
		final String[] perms = {Manifest.permission.CAMERA, 
				Manifest.permission.READ_EXTERNAL_STORAGE, 
				Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
        		ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
        		ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
        	Log.e(TAG, "Build.VERSION.SDK_INT "+Build.VERSION.SDK_INT);
        	Log.e(TAG, "Build.VERSION_CODES.M "+Build.VERSION_CODES.M);
        	
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                new AlertDialog.Builder(this)
//                        .setTitle(R.string.notifyTitle)
//                        .setMessage(R.string.caramNotifyMsg)
//                        .setNegativeButton(R.string.cancel,
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                    	
//                                    }
//                                })
//                        .setPositiveButton(R.string.setting,
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                    	ActivityCompat.requestPermissions(PictureAddActivity.this,
//         			                            perms,
//         			                            MY_PERMISSIONS_REQUEST_CAMERA);                                        
//                                    }
//                                })
//
//                        .setCancelable(false)
//                        .show();
//            } else {
//        		getImages();
//            }
        	return true;
        }else{
        	Toast.makeText(this, "需要开启定摄像头权限和读写本地相册的权限，请确认权限开启后重试！", 2000).show();;
            ActivityCompat.requestPermissions(this,perms,
            		MY_PERMISSIONS_REQUEST_CAMERA);
            return false;
        }
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
            	getImages();;
            } else
            {
                // Permission Denied
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	
	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中 完成图片的扫描，最终获得jpg最多的那个文件夹
	 */
	private void getImages() {
		Log.i(TAG, "---getImages----");
		
        if(!hasAuthority()){
        	return;
        }	
		
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// 显示进度条
		mProgressDialog = ProgressDialog.show(this, null, "正在加图片...");

		new Thread(new Runnable() {
			@Override
			public void run() {

				String firstImage = null;

				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = PictureAddActivity.this.getContentResolver();

				// 只查询jpeg和png的图片
				/*
				 * Cursor mCursor = mContentResolver.query(mImageUri, new
				 * String[] { ImageColumns.DATA,
				 * ImageColumns.BUCKET_DISPLAY_NAME, ImageColumns.SIZE }, null,
				 * null, null);
				 */
				
				Cursor mCursor = mContentResolver.query(mImageUri, null, MediaStore.Images.Media.MIME_TYPE + "=? or "
						+ MediaStore.Images.Media.MIME_TYPE + "=?", new String[] { "image/jpeg", "image/png" },
						MediaStore.Images.Media.DATE_MODIFIED + " DESC");

				mAllImgs.add("camera");

				String filePath = Environment.getExternalStorageDirectory().toString() + File.separator
						+ "infoRelease_photos";
				File photosAll = new File(filePath);
				List<File> photosFileList = getMusicListOnSys(photosAll);
				//
				// Collections.sort(photosFileList, new ImgFileOrder());
				// for (int i = 0; i < photosFileList.size(); i++) {
				// mAllImgs.add(photosFileList.get(i).getPath());
				// }
				//
				// String cameraAllPath =
				// Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
				// List<File> cameraFileList = new ArrayList<File>();
				// File cameraAll = new File(cameraAllPath);
				// cameraFileList=getMusicListOnSys(cameraAll);
				//
				//
				// Collections.sort(cameraFileList, new ImgFileOrder());
				//
				// for (int i = 0; i < cameraFileList.size(); i++) {
				// mAllImgs.add(cameraFileList.get(i).getPath());
				// }

				List<File> allImgFileList = new ArrayList<File>();
				// Log.e("TAG", mCursor.getCount() + "");
				while (mCursor.moveToNext()) {
					// 获取图片的路径
					String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));

					if (!mAllImgs.contains(path)) {
						File imgFile = new File(path);
						allImgFileList.add(imgFile);
						// mAllImgs.add(path);//保存所有扫描到的图片路径
					}
					Log.e("TAG", path);

					// 拿到第一张图片的路径
					if (firstImage == null)
						firstImage = path;
					// 获取该图片的父路径名
					File parentFile = new File(path).getParentFile();
					if (parentFile == null)
						continue;
					String dirPath = parentFile.getAbsolutePath();
					ImageFloder imageFloder = null;
					// 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）
					if (mDirPaths.contains(dirPath)) {
						continue;
					} else {
						mDirPaths.add(dirPath);
						// 初始化imageFloder
						imageFloder = new ImageFloder();
						imageFloder.setDir(dirPath);
						imageFloder.setFirstImagePath(path);
					}

					String[] fileList = parentFile.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String filename) {
							if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg"))
								return true;
							return false;
						}
					});
					int picSize = 0;
					if (fileList != null)
						picSize = fileList.length;

					totalCount += picSize;

					imageFloder.setCount(picSize);
					mImageFloders.add(imageFloder);

					/*
					 * //找出图片最多的文件夹 if (picSize > mPicsSize) { mPicsSize =
					 * picSize; mImgDir = parentFile; }
					 */
				}
				for (int i = 0; i < photosFileList.size(); i++) {
					if (!allImgFileList.contains(photosFileList.get(i))) {
						allImgFileList.add(photosFileList.get(i));
					}

				}

				Collections.sort(allImgFileList, new ImgFileOrder());

				for (int i = 0; i < allImgFileList.size(); i++) {
					mAllImgs.add(allImgFileList.get(i).getPath());
				}

				mCursor.close();

				// 扫描完成，辅助的HashSet也就可以释放内存了
				mDirPaths = null;

				// 添加所有文件的文件夹
				ImageFloder allImageFloder = new ImageFloder();
				allImageFloder.setAllImageFolder(true);
				allImageFloder.setName("所有图片");
				allImageFloder.setCount(mAllImgs.size());
				// 设置第一张图片地址，从第2张开始，因为第一张图位置显示相机
				if (mAllImgs.size() == 1) {

				} else {
					allImageFloder.setFirstImagePath(mAllImgs.get(1));
				}
				mPicsSize = mAllImgs.size();
				mImageFloders.add(0, allImageFloder);
				// 通知Handler扫描图片完成
				mHandler.sendEmptyMessage(0x110);

			}
		}).start();

		initEvent();
	}

	private ArrayList<File> getMusicListOnSys(File file) {
		ArrayList<File> fileList = new ArrayList<File>();
		getFileList(file, fileList);
		return fileList;
	}

	private void getFileList(File path, ArrayList<File> fileList) {
		if (path.isDirectory()) {
			File[] files = path.listFiles();
			if (null == files)
				return;
			for (int i = 0; i < files.length; i++) {
				getFileList(files[i], fileList);
			}
		} else {
			if (checkIsImageFile(path.getPath())) {

				fileList.add(path);
			}

		}
	}

	// 过滤图片文件
	private boolean checkIsImageFile(String fName) {
		boolean isImageFile = false;
		// 获取扩展名
		String FileEnd = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
		if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("jpeg") || FileEnd.equals("bmp")) {
			isImageFile = true;
		} else {
			isImageFile = false;
		}
		return isImageFile;
	}

	/**
	 * 初始化View
	 */
	private void initView() {
		mGirdView = (GridView) findViewById(R.id.id_gridView);
		mChooseDir = (TextView) findViewById(R.id.id_choose_dir);
		mImageCount = (TextView) findViewById(R.id.id_total_count);

		mBottomLy = (RelativeLayout) findViewById(R.id.id_bottom_ly);
	}

	private void sendImages(List<String> images) {
		/*
		 * CloseableHttpClient httpclient = HttpClients.createDefault();
		 * MultipartEntityBuilder multipartEntityBuilder =
		 * MultipartEntityBuilder.create();
		 * 
		 * multipartEntityBuilder.setCharset(Charset.forName(org.apache.http.
		 * protocol.HTTP.UTF_8));
		 * multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		 * 
		 * HttpPost httpPost = new HttpPost(UrlUtils.getResourceUploadUrl());
		 * multipartEntityBuilder.addTextBody("fileType", "41");
		 * 
		 * int length = images.size(); for(int i = 0; i< length; i++){
		 * multipartEntityBuilder.addBinaryBody("file"+(i+1), new
		 * File(images.get(i))); }
		 * 
		 * RequestConfig requestConfig = RequestConfig.custom()
		 * .setConnectionRequestTimeout(15 * 1000) .setConnectTimeout(15 * 1000)
		 * .setSocketTimeout(20 * 1000) .build();
		 * 
		 * // httpPost.setConfig(requestConfig);
		 * httpPost.setEntity(multipartEntityBuilder.build());
		 * 
		 * try { CloseableHttpResponse response = httpclient.execute(httpPost);
		 * 
		 * HttpEntity entity = response.getEntity();
		 * 
		 * if (entity !=null) {
		 * 
		 * System.out.println("Response content: "
		 * +EntityUtils.toString(entity));
		 * 
		 * } } catch (ClientProtocolException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		String urlString = UrlUtils.getResourceUploadUrl();
		Log.i(TAG, "URL:" + urlString);

		filesList = new HashMap<String, File>();

		Map<String, String> params = new HashMap<String, String>();

		params.put("fileType", Integer.toString(mFileType));

		ArrayList<String> imageList = new ArrayList<String>();// 删除重复数据
		for (String s : images) {
			if (Collections.frequency(imageList, s) < 1)
				imageList.add(s);
		}

		length = imageList.size();
		for (int i = 0; i < length; i++) {
			filesList.put("file" + (i + 1), getimageFile(imageList.get(i)));
			Log.i(TAG, "select file:" + imageList.get(i));
		}

		UploadFiles(urlString, filesList, params, mResonseListenerString, mErrorListener, null);
	}

	private File getimageFile(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		Log.v(TAG, "w: " + w + " h: " + h);
		float ww = 1920;
		float hh = 1080;
		int dw = 1920;
		int dh = 1080;
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int resampleSize = 1;// be=1表示不缩放
		if (w > 1920 || h > 1080) {// 如果宽度大的话根据宽度固定大小缩放
			float sw = newOpts.outWidth / ww;
			float sh = newOpts.outHeight / hh;
			if (sw > sh) {
				dh = (int) (newOpts.outHeight / sw);
				resampleSize = (int) sw;
			} else {
				dw = (int) (newOpts.outWidth / sh);
				resampleSize = (int) sh;
			}
		} else {
			return new File(srcPath);
		}
		Log.v(TAG, "resampleSize: " + resampleSize);
		Log.v(TAG, "dw: " + dw + " dh: " + dh);

		String filename = new File(srcPath).getName();
		String out_file_path = getExternalStorageDirectory() + filename;

		newOpts.inSampleSize = resampleSize;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		Log.v(TAG, "w1: " + newOpts.outWidth + " h1: " + newOpts.outHeight);
		w = newOpts.outWidth;
		h = newOpts.outHeight;
		dw = 1920;
		dh = 1080;
		if (w > 1920 || h > 1080) {// 如果宽度大的话根据宽度固定大小缩放
			float sw = newOpts.outWidth / ww;
			float sh = newOpts.outHeight / hh;
			Log.v(TAG, "sw: " + sw + " sh: " + sh);
			if (sw > sh) {
				dw = 1920;
				dh = (int) (newOpts.outHeight / sw);
			} else {
				dw = (int) (newOpts.outWidth / sh);
				dh = 1080;
			}
		}
		Log.v(TAG, "dw: " + dw + " dh: " + dh);
		Bitmap new_bitmap = zoomBitmap(bitmap, dw, dh);
		bitmap.recycle();
		// compressImage(bitmap);//压缩好比例大小后再进行质量压缩
		// Log.i(TAG, "--------------out_put_file:"+out_file_path);
		// 保存入sdCard
		File file2 = new File(out_file_path);
		try {
			FileOutputStream out = new FileOutputStream(file2);
			CompressFormat format;
			if (filename.endsWith("png") || filename.endsWith("PNG")) {
				format = CompressFormat.PNG;
			} else {
				format = CompressFormat.JPEG;
			}
			if (new_bitmap.compress(format, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		new_bitmap.recycle();

		return file2;
		// File myCaptureFile = null;
		// try
		// {
		// Bitmap bitmap = BitmapFactory.decodeFile(srcPath);
		// int bitmapWidth = bitmap.getWidth();
		// int bitmapHeight = bitmap.getHeight();
		//
		// if(bitmapWidth <= 1920 && bitmapHeight <= 1080){
		// return new File(srcPath);
		// }
		//
		// // 缩放图片的尺寸
		// float scaleWidth = (float) 1920 / bitmapWidth;
		// float scaleHeight = (float) 1080 / bitmapHeight;
		// Matrix matrix = new Matrix();
		// matrix.postScale(scaleWidth, scaleHeight);
		// // 产生缩放后的Bitmap对象
		// Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth,
		// bitmapHeight, matrix, false);
		// // save file
		// String filename = new File(srcPath).getName();
		// String out_file_path = getExternalStorageDirectory()+filename;
		// myCaptureFile = new File(out_file_path);
		// FileOutputStream out = new FileOutputStream(myCaptureFile);
		//
		// CompressFormat format;
		// if(filename.endsWith("png")||filename.endsWith("PNG")){
		// format = CompressFormat.PNG;
		// }else{
		// format = CompressFormat.JPEG;
		// }
		//
		// if(resizeBitmap.compress(format, 100, out)){
		// out.flush();
		// out.close();
		// }
		// if(!bitmap.isRecycled()){
		// bitmap.recycle();//记得释放资源，否则会内存溢出
		// }
		// if(!resizeBitmap.isRecycled()){
		// resizeBitmap.recycle();
		// }
		//
		// }catch (FileNotFoundException e)
		// {
		// e.printStackTrace();
		// }
		// catch (IOException ex)
		// {
		// ex.printStackTrace();
		// }
		//
		// return myCaptureFile;
	}

	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return newbmp;
	}

	/*
	 * private Bitmap compressImage(Bitmap image) {
	 * 
	 * ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 * image.compress(Bitmap.CompressFormat.JPEG, 100,
	 * baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中 int options = 100; while (
	 * baos.toByteArray().length / 1024>100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
	 * baos.reset();//重置baos即清空baos options -= 10;//每次都减少10
	 * image.compress(Bitmap.CompressFormat.JPEG, options,
	 * baos);//这里压缩options%，把压缩后的数据存放到baos中
	 * 
	 * } ByteArrayInputStream isBm = new
	 * ByteArrayInputStream(baos.toByteArray())
	 * ;//把压缩后的数据baos存放到ByteArrayInputStream中 Bitmap bitmap =
	 * BitmapFactory.decodeStream(isBm, null,
	 * null);//把ByteArrayInputStream数据生成图片
	 * 
	 * return bitmap; }
	 */

	ProgressDialog progressDialog;

	Listener<String> mResonseListenerString = new Listener<String>() {

		@Override
		public void onResponse(String response) {
			Log.d(TAG, "response=" + response);
			if (progressDialog.isShowing() && progressDialog != null) {
				progressDialog.dismiss();
			}

			try {
				JSONObject jsonObj = new JSONObject(response);
				if (jsonObj.optInt("code") == 0) { // 返回成功
					Toast.makeText(PictureAddActivity.this, "共" + length + "张图片,上传成功!", Toast.LENGTH_LONG).show();
					mAdapter.mSelectedImage.clear();
					mAdapter.notifyDataSetChanged();
					// mGirdView.invalidate();

				} else {// 失败
					Toast.makeText(PictureAddActivity.this, jsonObj.getString("msg"), Toast.LENGTH_LONG).show();

				}
				//
				for (String key : filesList.keySet()) {
					if (filesList.get(key).exists()) {
						filesList.get(key).delete();
					}
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	ErrorListener mErrorListener = new ErrorListener() {

		@Override
		public void onErrorResponse(VolleyError error) {
			error.printStackTrace();
			Toast.makeText(PictureAddActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
			if (progressDialog.isShowing() && progressDialog != null) {
				progressDialog.dismiss();
			}
		}
	};

	public void UploadFiles(final String url, final Map<String, File> files, final Map<String, String> params,
			final Listener<String> responseListener, final ErrorListener errorListener, final Object tag) {
		if (null == url || null == responseListener) {
			return;
		}
		if (progressDialog != null && progressDialog.isShowing() == true) {
			return;
		}
		progressDialog = ProgressDialog.show(this, "", "...Loading...");
		Log.d(TAG, "PictureAddActivity show dialog");
		RequestQueue mSingleQueue = Volley.newRequestQueue(PictureAddActivity.this, new MultiPartStack());
		MultiPartStringRequest multiPartRequest = new MultiPartStringRequest(Request.Method.POST, url,
				responseListener, errorListener) {

			@Override
			public Map<String, File> getFileUploads() {
				return files;
			}

			@Override
			public Map<String, String> getStringUploads() {
				return params;
			}

		};

		multiPartRequest.setRetryPolicy(new RetryPolicy() {

			@Override
			public void retry(VolleyError error) throws VolleyError {
				Toast.makeText(PictureAddActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
			}

			@Override
			public int getCurrentTimeout() {
				// TODO Auto-generated method stub
				return 20 * 1000;
			}

			@Override
			public int getCurrentRetryCount() {
				// TODO Auto-generated method stub
				return 2;
			}
		});
		multiPartRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		mSingleQueue.add(multiPartRequest);
	}

	private void initEvent() {
		/**
		 * 为底部的布局设置点击事件，弹出popupWindow
		 */
		mBottomLy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 如果只有一个文件夹，就是所有图片，点击不响应
				if (mImageFloders.size() == 1) {
					return;
				}
				mListImageDirPopupWindow.setAnimationStyle(R.style.anim_popup_dir);
				mListImageDirPopupWindow.showAsDropDown(mBottomLy, 0, 0);

				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = .3f;
				getWindow().setAttributes(lp);
			}
		});
	}

	private void setOnImageClickedListener() {
		if (mAdapter == null)
			return;

		mAdapter.setOnImageClickedListener(new OnImageClicked() {

			@Override
			public void onImageClicked(String imagPath) {
				Intent previewIntent = new Intent(PictureAddActivity.this, PicPreviewActivity.class);

				previewIntent.putExtra("path", imagPath);
				startActivity(previewIntent);
			}
		});
	}

	@Override
	public void selected(ImageFloder floder) {
		if (floder.isAllImageFolder()) {
			ArrayList<String> allImageList = new ArrayList<String>();
			for (int i = 0; i < mAllImgs.size(); i++) {
				if (!allImageList.contains(mAllImgs.get(i))) {
					allImageList.add(mAllImgs.get(i));
				}
			}
			mAdapter = new AddPicSelAdapter(PictureAddActivity.this, allImageList, R.layout.add_picture_grid_item,
					null, isCaptureImg);
			if (mAdapter.mSelectedImage.size() > 0) {
				mAdapter.mSelectedImage.clear();
			}
			mAdapter.setOnCamerClickedlistener(new OnCamerClicked() {

				@Override
				public void onCamerClicked() {
					getImageFromCamera();
				}
			});
			setOnImageClickedListener();

			mGirdView.setAdapter(mAdapter);

			// mAdapter.notifyDataSetChanged();
			if (floder.getCount() == 1) {

				mImageCount.setText("0张");
			} else {
				mImageCount.setText(floder.getCount() + "张");
			}
			mChooseDir.setText(floder.getName());
			this.isCaptureImg = false;
		} else {

			mImgDir = new File(floder.getDir());
			String[] list = mImgDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg"))
						return true;
					return false;
				}
			});
			if (list == null) {
				return;
			}
			List<File> fileList = new ArrayList<File>();
			for (int i = 0; i < list.length; i++) {
				fileList.add(new File(mImgDir + "/" + list[i]));
			}

			Collections.sort(fileList, new ImgFileOrder());
			mImgs = new ArrayList<String>();
			for (int i = 0; i < fileList.size(); i++) {
				mImgs.add(
						i,
						fileList.get(i)
								.getPath()
								.substring(fileList.get(i).getPath().lastIndexOf("/") + 1,
										fileList.get(i).getPath().length()));
			}

			// mImgs = Arrays.asList(list);

			// Collections.reverse(mImgs);
			/**
			 * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
			 */
			mAdapter = new AddPicSelAdapter(PictureAddActivity.this, mImgs, R.layout.add_picture_grid_item,
					mImgDir.getAbsolutePath(), false);
			setOnImageClickedListener();
			mGirdView.setAdapter(mAdapter);

			// mAdapter.notifyDataSetChanged();
			mImageCount.setText(floder.getCount() + "张");
			mChooseDir.setText(floder.getName());
		}

		if (mListImageDirPopupWindow != null && mListImageDirPopupWindow.isShowing())
			mListImageDirPopupWindow.dismiss();

	}

	private int REQUEST_CODE_CAPTURE_CAMEIA = 1;
	private String capturePath = null;
	private boolean isCaptureImg = false;
	private int length;
	private Map<String, File> filesList;

	protected void getImageFromCamera() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			capturePath = null;
			Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
			String out_file_path = getExternalStorageDirectory();
			File dir = new File(out_file_path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			capturePath = out_file_path + System.currentTimeMillis() + ".jpg";
			Log.i(TAG, "capturePath:  " + capturePath);
			getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(capturePath)));
			getImageByCamera.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(getImageByCamera, REQUEST_CODE_CAPTURE_CAMEIA);
		} else {
			Toast.makeText(this, "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
			Log.i(TAG, "capturePath:" + capturePath);
			if (capturePath != null) {
				File file = new File(capturePath);
				if (file != null && file.exists()) {
					mAllImgs.add(1, capturePath);
					ImageFloder firstFloder = mImageFloders.get(0);
					firstFloder.setCount(firstFloder.getCount() + 1);
					mPicsSize++;
					this.isCaptureImg = true;
					selected(mImageFloders.get(0));
				}
			}
		}
	}

	public static void saveImage(Bitmap photo, String spath) {
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(spath, false));
			photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		return;
	}

	/**
	 * 为View绑定数据
	 */
	private void data2View() {
		selected(mImageFloders.get(0));

	};

	/**
	 * 初始化展示文件夹的popupWindw
	 */
	private void initListDirPopupWindw() {
		mListImageDirPopupWindow = new ListImageDirPopupWindow(LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
				mImageFloders, LayoutInflater.from(/* getApplicationContext() */this).inflate(R.layout.list_dir, null));

		mListImageDirPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 1.0f;
				getWindow().setAttributes(lp);
			}
		});
		// 设置选择文件夹的回调
		mListImageDirPopupWindow.setOnImageDirSelected(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mAdapter.mSelectedImage.size() > 0) {
			mAdapter.mSelectedImage.clear();
		}
	}
}
