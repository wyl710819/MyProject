package com.routon.smartcampus.face;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.edurelease.R;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.FileRequest;
import com.routon.smartcampus.utils.FileUtil;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.routon.widgets.Toast;
import ang.face.recognizer.DetectionBasedTracker;

public class FaceRecognizeMgr {
	private Context mContext;
	public static File mFaceDir;
	private static final String TAG = FaceRecognizeMgr.class.getName();
	public static final String TMP_MODULE_FILE_NAME = "facerecognite_module.downloading";
	public static final String NORMAL_MODULE_FILE_NAME = "facerecognize_module";
	private String mModuleUrl = null;
	private String mModuleFileMd5 = null;
	private String mDataSize = null;
	private String mLocalVersion = null;
	private String mPlatformVersion = null;
	private ArrayList<String> mImageSavePaths = null;
	public static final String DATA_DIR_NAME = "hdpic";
	public static final String MODULE_DIR_NAME = "model";
	public static final String MODULE_DATA_SUFFIX = "fea";
	public static final String GRAFFITI_PIC = "/graffiti_pic";
	private DetectionBasedTracker mNativeDetector = null;
	// private int mProgress = 0;
	// private ArrayList<String> mImageUrls = null;
	// private String mClassName = null;
	private GetFaceDataThread mGetFaceDataThread = null;

	private FaceRecoginizeCompleteCb mFaceRecoginzeCompleteCb = null;

	public interface FaceRecoginizeCompleteCb {
		void callback(String modelPath, String imagePath);
	};

	public void setFaceRecoginizeCompleteCb(FaceRecoginizeCompleteCb callback) {
		mFaceRecoginzeCompleteCb = callback;
	}

	private static FaceRecognizeMgr instance = null;

	public static FaceRecognizeMgr getInstance(Context context) {
		if (instance == null) {
			instance = new FaceRecognizeMgr(context);
		}
		return instance;
	}
	
    public class NetworkChangeListener extends BroadcastReceiver {  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            // TODO Auto-generated method stub  
            //Toast.makeText(context, intent.getAction(), 1).show();  
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
            //如果无网络连接activeInfo为null
            NetworkInfo activeInfo = manager.getActiveNetworkInfo(); 
            if( mDownloadDialog != null && mDownloadDialog.isShowing() == true ){//下载对话框显示中    	
             	if( mDownloader != null ){
             		Log.d(TAG,"activeInfo:"+activeInfo);
             		 if( activeInfo == null ){
             			mDownloader.pause();
             		 }else{
             			 if( activeInfo.isConnected() == true ){
             				 // 得到下载信息类的个数组成集合  
             		        LoadInfo loadInfo = mDownloader.getDownloaderInfors();  
             				mDownloader.download();
             			 }else{
             				mDownloader.pause();
             			 }
             			
             		 }
             	}
    		}
           
        }   
      
    }  
    
    private NetworkChangeListener mNetworkChangeListener = null;

	public void init(Context context) {		
		mContext = context;
		if (mImageSavePaths != null) {
			mImageSavePaths.clear();
			mImageSavePaths = null;
		}
		
		mNetworkChangeListener = new NetworkChangeListener();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//		filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
//		filter.addAction("android.net.wifi.STATE_CHANGE");
		mContext.registerReceiver(mNetworkChangeListener,filter);
	}

	private boolean showNetWorkFailed(Context context) {
		return InfoReleaseApplication.showNetWorkFailed(context);
	}

	public void deinit() {
		
		if(mContext != null && mNetworkChangeListener != null){
			mContext.unregisterReceiver(mNetworkChangeListener);
		}
		mNetworkChangeListener = null;
		mContext = null;
		if (mImageSavePaths != null) {
			mImageSavePaths.clear();
			mImageSavePaths = null;
		}
		if (mDownloadDialog != null) {
			mDownloadDialog.dismiss();
			mDownloadDialog = null;
		}
		if( mModuleDownloadConfirmDialog != null ){
			mModuleDownloadConfirmDialog.dismiss();
			mModuleDownloadConfirmDialog = null;
		}
	}
	
	public static void clearUserImageDir(){
		String userName = InfoReleaseApplication.authenobjData.userName;
		String userDir = FileUtil.getSDPath()+"/Android/data/com.routon.edurelease/hdpic/"+userName;
		FileUtil.deleteDir(userDir);
		
	}
	
	public static String getImageDir(String gradeName,String className){
		String userName = InfoReleaseApplication.authenobjData.userName;
		return FileUtil.getSDPath()+"/Android/data/com.routon.edurelease/hdpic/"+userName+"/"+gradeName+className;
	}

	private FaceRecognizeMgr(Context context) {
		String sdcardPath = FileUtil.getSDPath();
		if (sdcardPath != null) {
			String appHome = sdcardPath + "/Android/data/com.routon.edurelease";
			File destDir = new File(appHome);
			if (destDir.exists() == false) {// 如果文件不存在，创建文件
				boolean ret = destDir.mkdirs();
				if (ret == false) {
					Log.d(TAG, "创建目录失败");
					mFaceDir = context.getCacheDir();
				} else {
					mFaceDir = destDir;
				}
			} else {
				mFaceDir = destDir;
			}
			File dataDir = new File(mFaceDir, DATA_DIR_NAME);
			if (dataDir.exists() == false) {// 如果文件不存在，创建文件
				dataDir.mkdir();
			}
		}

		mLocalVersion = getLocalModuleVersion();
		if (mLocalVersion != null) {// zip文件存在，model目录不存在,解压zip文件
			File moduleDir = new File(mFaceDir, MODULE_DIR_NAME);
			if (moduleDir.exists() == false) {
				try {
					File newFile = new File(mFaceDir, NORMAL_MODULE_FILE_NAME + "." + mPlatformVersion);
					FileUtil.UnZipFolder(newFile.getAbsolutePath(), mFaceDir.getAbsolutePath());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (mGetFaceDataThread == null) {
			mGetFaceDataThread = new GetFaceDataThread();
			mGetFaceDataThread.start();
		}
	}
	
	//获取记录的下载临时文件的md5，比较是否是最新的文件后进行断点下载
	public String getModuleTmpFileMd5(){
		SharedPreferences sharePrefrences = mContext.getSharedPreferences("FaceData", Context.MODE_PRIVATE); //私有数据
		String fileMd5 = sharePrefrences.getString("TmpFileMd5","");
		return fileMd5;
	}
	
	public long getModuleTmpFileMd5Length(){
		SharedPreferences sharePrefrences = mContext.getSharedPreferences("FaceData", Context.MODE_PRIVATE); //私有数据
		long fileMd5Length = sharePrefrences.getLong("TmpFileMd5Length",0);
		return fileMd5Length;
	}
	
	public void saveModuleTmpFileMd5(String fileMd5,long completeSize){
		SharedPreferences sharePrefrences = mContext.getSharedPreferences("FaceData", Context.MODE_PRIVATE); //私有数据
		SharedPreferences.Editor editor = sharePrefrences.edit();
		editor.putString("TmpFileMd5",fileMd5);
		editor.putLong("TmpFileMd5Length", completeSize);
		editor.commit();
	}

	// 根据数据信息生成图片的本地地址，图片保存在sd目录下，又用于人脸识别时使用
	public static String getImageFilePath(String studentId, String imageLastUpdateTime, String grade, String classname) {
//		Log.d(TAG,"getImageFilePath studentId:"+studentId+",imageLastUpdateTime:"+imageLastUpdateTime+",grade:"+grade+",classname:"+classname);
		if( InfoReleaseApplication.authenobjData == null || InfoReleaseApplication.authenobjData.userName == null ){
			return null;
		}
		String filename = FileUtil.getImageFormatName(studentId, imageLastUpdateTime);
		File classDir = new File(mFaceDir, DATA_DIR_NAME + "/" +InfoReleaseApplication.authenobjData.userName+"/"+ grade + classname);
		// 如果class图片目录不存在，则创建class图片目录
		if (classDir.exists() == false) {
			classDir.mkdir();
		}
		return classDir.getAbsolutePath() + "/" + filename;
	}
	
	public boolean cancelDownloadClassImagesAndGetDatas(){
		synchronized (mGetFaceDataImages) {
			mGetFaceDataImages.clear();
		}
		return true;
	}
	
	public void createImageDirs( ArrayList<String> dirname){
		
	}

	// 开始下载class图片,并生成datas数据
	public boolean startDownloadClassImagesAndGetDatas(ArrayList<String> imageSavePaths, ArrayList<String> imageUrls,
			final Response.Listener<String> listener, final Response.ErrorListener errorListener) {
		if (imageSavePaths == null)
			return false;
		Log.d(TAG, "startDownloadClassImagesAndGetDatas size:" + imageSavePaths.size());	
		if( mImageSavePaths == null ){
			mImageSavePaths = new ArrayList<String>();
		}
		mImageSavePaths.addAll(imageSavePaths);
		for (int i = 0; i < imageSavePaths.size(); i++) {
			final String path = imageSavePaths.get(i);
			File file = new File(path);
			final String imagePath = imageUrls.get(i);
			if (file.exists() == false) {// 图片不存在，开始下载
				if (imagePath.startsWith("/")) {// 本地图片
					File imageFile = new File(imagePath);
					Log.d(TAG, "startDownloadClassImagesAndGetDatas imagePath:" + imagePath + ",path:" + path);
					imageFile.renameTo(file);
				} else {
					Response.Listener<String> myListener = new Response.Listener<String>() {

						@Override
						public void onResponse(String response) {
							// TODO Auto-generated method stub
							// 图片下载完毕后，先处理一遍，获取对应的facedata数据
							Log.d(TAG, "startDownloadClassImagesAndGetDatas onResponse:" + response);
							getFaceData(response);
							if (mContext != null) {
								if (listener != null) {
									listener.onResponse(response);
								}
							}
						}

					};
					
					Response.ErrorListener myErrorListener = new Response.ErrorListener(){

						@Override
						public void onErrorResponse(VolleyError error) {
							// TODO Auto-generated method stub
							Log.d(TAG, "startDownloadClassImagesAndGetDatas onResponse:" + error);
							if( mImageSavePaths != null ){
								mImageSavePaths.remove(path);
							}
							if (mContext != null) {								
								
								if (errorListener != null) {
									errorListener.onErrorResponse(error);
								}
							}
						}
						
					};
					


					FileRequest request = new FileRequest(imagePath, path, myListener, myErrorListener);
					InfoReleaseApplication.requestQueue.add(request);
					Log.d(TAG, "startDownloadClassImagesAndGetDatas download url:" + imagePath);
					
				}
			} else {
				checkGetFaceData(path);
			}
		}
		return true;
	}

	// 图片文件存在，检查face data是否存在，如果不存在，则让线程去生成face data文件
	public void checkGetFaceData(String imagePath) {
		// module data file
		File imageFile = new File(imagePath);
		if (imageFile.exists() == false) {// 图片文件不存在
			return;
		}
		String dataPath = imagePath.substring(0, imagePath.length() - 3) + MODULE_DATA_SUFFIX;
		File dataFile = new File(dataPath);
		
		if (dataFile.exists() == false ) {
			//Log.d(TAG,"checkGetFaceData imagePath:"+imagePath+",dataPath:"+dataPath);
			synchronized (mGetFaceDataImages) {
				//检查数据队伍中是否包含这个数据
				if( mGetFaceDataImages.contains(imagePath) == false ){
					mGetFaceDataImages.add(imagePath);
				}
			}
		}
	}

	public void getFaceData(String imagePath) {
		// module data file
		Log.d(TAG,"getFaceData mLocalVersion:"+mLocalVersion);
		synchronized (mGetFaceDataImages) {
			//检查数据队伍中是否包含这个数据
			if( mGetFaceDataImages.contains(imagePath) == false ){
				mGetFaceDataImages.add(imagePath);
			}
		}
	}

//	// 设置class图片路径，用于在进入人脸识别时检查图片是否下载完毕，数据是否生成
//	public void setClassImages(ArrayList<String> imageSavePaths) {
//		mImageSavePaths = imageSavePaths;
//	}

	public boolean checkImageAndData() {
		if (mContext == null)
			return false;
		if (mImageSavePaths == null) {
			Toast.makeText(mContext, "当前班级学生数据出错，可能导致识别失败，请稍后进入", Toast.LENGTH_SHORT).show();
			return false;
		}
		//Log.d(TAG, "checkImageAndData images num:" + mImageSavePaths.size());
		for (int i = 0; i < mImageSavePaths.size(); i++) {
			String path = mImageSavePaths.get(i);
			File file = new File(path);
			if (file.exists() == false) {
				Log.d(TAG, "checkImageAndData path:" + path);
				Toast.makeText(mContext, "学生照片正在下载中，照片不全可能导致识别失败，请稍后进入", Toast.LENGTH_SHORT).show();
				return false;
			}
		}

//		for (int i = 0; i < mImageSavePaths.size(); i++) {
//			String path = mImageSavePaths.get(i);
//			checkGetFaceData(path);
//		}
		Log.d(TAG, "checkImageAndData mGetFaceDataImages:" + mGetFaceDataImages.size());
		synchronized (mGetFaceDataImages) {
			if (mGetFaceDataImages.size() > 0) {
				Toast.makeText(mContext, "学生照片数据正在生成中，数据不全可能导致识别失败，请稍后进入", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		// for( int i = 0; i < mImageSavePaths.size();i++){
		// String path = mImageSavePaths.get(i);
		// File file = new File(path);
		// if( file.exists() == false ){
		// Toast.makeText(mContext, "当前班级照片正在下载中，照片不全可能导致识别失败，请稍后进入",
		// Toast.LENGTH_SHORT).show();
		// return false;
		// }
		// }
		return true;
	}
	
	//删除无用的照片,避免进入人脸识别后，程序对无用的照片生成fea文件，阻塞主线程
	private void delUnusedPics(){
		 List<String> images = FileUtil.getImagesFromPath(mImageDir);
		 for( int i = 0; i < images.size(); i++ ){
			 String path = images.get(i);
			
			 if( mImageSavePaths.contains(path) == false ){
				 File file = new File(path);
				 file.delete();
				 Log.d(TAG,"delUnusedPics path:"+path);
			 }
		 }
	}

	private static final int MSG_GET_MODULE_FILE_VERSION = 1;
	// 判断是否下载平台模型文件
	private static final int MSG_CHECK_DOWNLOAD_MODULE_FILE = 2;
	
	private static final int MSG_SHOW_DOWNLOAD_MODULE_FILE = 5;
	// 本地模型文件已存在或者已更新
	private static final int MSG_MODEUL_FILE_FINISHED = 3;
	// 更新下载模型文件失败
	private static final int MSG_MODEUL_FILE_FAILED = 4;
	// private static final int MSG_DOWNLOAD_UPDATE_PRGRESS = 7;
	// private static final int MSG_DOWNLOAD_DOWNLOAD_FINISH = 8;
	// 取消下载
	private static final int MSG_CHECK_DOWNLOAD_CANCELED = 9;
	private static final int MSG_HIDE_PROGESSDIALOG = 10;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (mContext == null)
				return;
			switch (msg.what) {
			case MSG_CHECK_DOWNLOAD_MODULE_FILE:// 判断是否下载module文件
				if (mPlatformVersion != null) {
					// 没有本地文件，或者本地文件版本号和平台版本号不一致
					if (mLocalVersion == null || mLocalVersion.equals(mPlatformVersion) == false) {
						boolean ret = makeValidTmpFileToNormal();
						if (ret == true) {
							return;
						}
						// 提示用户是否下载文件
						startModuleDownloadConfirmDialog();
						return;
					} else {// 本地文件存在，人脸识别模型文件已存在
						mHandler.sendEmptyMessageDelayed(MSG_MODEUL_FILE_FINISHED, 0);
					}
				}

				break;
				
			case MSG_SHOW_DOWNLOAD_MODULE_FILE:
				mHandler.removeMessages(MSG_SHOW_DOWNLOAD_MODULE_FILE);
				FaceRecognizeMgr.this.showModuleDownloadConfirmDialog();
				break;

			case MSG_MODEUL_FILE_FINISHED:// 本地文件存在，人脸识别模型文件已存在
				if (mDownloadDialog != null) {
					mDownloadDialog.dismiss();
					mDownloadDialog = null;
					mProgressBar = null;
				}
				Log.d(TAG,"MSG_MODEUL_FILE_FINISHED");
				mLocalVersion = mPlatformVersion;
				mHandler.removeMessages(MSG_MODEUL_FILE_FINISHED);
				mHandler.removeMessages(MSG_CHECK_DOWNLOAD_MODULE_FILE);
				mHandler.removeMessages(MSG_MODEUL_FILE_FAILED);
				mHandler.removeMessages(MSG_CHECK_DOWNLOAD_CANCELED);
				boolean ret = checkImageAndData();
				if (ret == true) {// 照片和数据都齐全,启动人脸识别界面
					if( mImageDir == null ){
						mImageDir = mFaceDir.getAbsolutePath() + "/" + DATA_DIR_NAME+"/"+InfoReleaseApplication.authenobjData.userName;
					}
					delUnusedPics();
					startFaceActivity();
				}
				break;
			case MSG_MODEUL_FILE_FAILED:// 文件下载出错
				if (mDownloadDialog != null) {
					mDownloadDialog.dismiss();
					mDownloadDialog = null;
				}
				Toast.makeText(mContext, "下载文件有问题，请重新下载", Toast.LENGTH_SHORT).show();
				break;
			// case MSG_DOWNLOAD_UPDATE_PRGRESS:
			// if( mDownloadDialog != null ){
			// mProgressBar.setProgress(mProgress);
			// mUpdateTextView.setText("当前进度"+mProgress+"%");
			// }
			// break;
			case MSG_CHECK_DOWNLOAD_CANCELED:
				Log.d(TAG, "MSG_CHECK_DOWNLOAD_CANCELED");
//				File tmpFile = new File(mFaceDir, TMP_MODULE_FILE_NAME);
//				if (tmpFile.exists() == false) {// 下载已经被取消
				Log.d("tag", "STATUS_RUNNING file has been removed");
				if (mDownloadDialog != null) {
					mDownloadDialog.dismiss();
					mDownloadDialog = null;
					Toast.makeText(mContext, "下载已经被取消", Toast.LENGTH_LONG).show();
				}

//				}
				break;
			case MSG_HIDE_PROGESSDIALOG:
				FaceRecognizeMgr.this.hideProgressDialog();
				break;
			}

		}
	};

	// 将有效临时文件修改为normal文件，删除face data数据文件，检查完成后进行model文件下载完成流程处理
	boolean makeValidTmpFileToNormal() {
		if (mContext == null)
			return false;
		File tmpFile = new File(mFaceDir, TMP_MODULE_FILE_NAME);
		if (tmpFile.exists() && mModuleFileMd5 != null) {
			try {// 临时文件存在且和平台md5值相同
				if (FileUtil.fileMD5(tmpFile.getAbsolutePath()).equals(mModuleFileMd5)) {
					// 删除原来的module文件，新文件后命名为facerecognize_module.20170630102030
					File newFile = new File(mFaceDir, NORMAL_MODULE_FILE_NAME + "." + mPlatformVersion);
					boolean ret = tmpFile.renameTo(newFile);
					Log.d(TAG, "ret:" + ret + ",newFile:" + newFile.getAbsolutePath());
					if (ret == true) {
						try {
							FileUtil.UnZipFolder(newFile.getAbsolutePath(), mFaceDir.getAbsolutePath());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						File oldFile = new File(mFaceDir, NORMAL_MODULE_FILE_NAME + "." + mLocalVersion);
						if (oldFile.exists() == true) {
							oldFile.delete();
						}
						deleteAllDataFiles();
						mLocalVersion = mPlatformVersion;
						mHandler.sendEmptyMessage(MSG_MODEUL_FILE_FINISHED);
					}
					return true;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private String mImageDir = null;

	void startFaceActivity() {
		if (mNativeDetector != null) {
			synchronized (mNativeDetector) {
				mNativeDetector.release();
				mNativeDetector = null;
			}
		}
		if (mFaceRecoginzeCompleteCb != null) {
//			File file = new File(mImageSavePaths.get(0));
			mFaceRecoginzeCompleteCb.callback(mFaceDir.getAbsolutePath() + "/" + MODULE_DIR_NAME, 
					mImageDir);
		}
		// Intent intent = new Intent();
		// intent.putExtra(FrActivity.INTENT_MODEL_DIR_DATA,
		// mFaceDir.getAbsolutePath()+"/"+MODULE_DIR_NAME);
		// File file = new File(mImageSavePaths.get(0));
		// intent.putExtra(FrActivity.INTENT_IMAGE_DIR_DATA, file.getParent());
		// intent.setComponent(new
		// ComponentName("com.routon.smartcampus","ang.face.recognizer.FrActivity"));
		// ((Activity) mContext).startActivityForResult(intent,0);
	}

	// 获取本地module版本，本地文件facerecognize_module.20170630102030，读取后缀名即为版本号
	private String getLocalModuleVersion() {
		if (mFaceDir == null) {
			return null;
		}
		File[] files = mFaceDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isFile()) {
				if (f.getName().startsWith(NORMAL_MODULE_FILE_NAME)) {
					return f.getName().substring(NORMAL_MODULE_FILE_NAME.length() + 1);
				}
			}
		}
		return null;
	}
	
	public void setImageDir(String classpath){
		mImageDir = classpath;
	}

	// 开始检查模型文件数据是否齐备，数据完整后启动人脸识别
	public void checkModuleFile() {
		if (mContext == null)
			return;	
		if (mModuleUrl == null) {// 没有获取过版本文件,向平台请求版本文件
			boolean ret = getModuleVersionInfo(true, MSG_CHECK_DOWNLOAD_MODULE_FILE);
			if (ret == false) {// 网络连接失败
				// 检查本地模型文件是否存在
				if( mLocalVersion != null ){
					mHandler.sendEmptyMessageDelayed(MSG_MODEUL_FILE_FINISHED, 0);
				}else{
					Toast.makeText(mContext, "本地模型文件不存在，且获取网络数据失败", Toast.LENGTH_LONG).show();
				}
			}
		} else {// 获取过版本文件，判断是否需要下载
			mHandler.sendEmptyMessageDelayed(MSG_CHECK_DOWNLOAD_MODULE_FILE, 0);
		}

	}
	
	private Dialog mModuleDownloadConfirmDialog = null;
	
	private void initDownload(){
	     // 初始化一个downloader下载器  
        if (mDownloader == null) {  
       	 	String localfile = mFaceDir.getAbsolutePath() + "/" + TMP_MODULE_FILE_NAME;
       	 	//修改为单线程下载
            int threadcount = 1; 
       	 	mDownloader = new Downloader(this.mModuleUrl, localfile, threadcount, mContext, new Downloader.DownloadListener() {
				
				@Override
				public void start() {
					// TODO Auto-generated method stub
					Log.d(TAG,"download start");
				}
				
				@Override
				public void progressUpdate(int increaseLength, int totalSize) {
					// TODO Auto-generated method stub
					Message message = Message.obtain();  
					message.what = 1;  
					message.arg1 = increaseLength;  
					mDownloadHandler.sendMessage(message); 
				}
				
				@Override
				public void complete() {
					// TODO Auto-generated method stub
					Log.d(TAG,"download complete");
	                Toast.makeText(mContext, "下载完成！", 0).show();  
	                 // 下载完成后将map中的数据清空  
	                mDownloader.delete(mModuleUrl);
	                mDownloader.reset();
	                mDownloader = null;   
	 				boolean ret = makeValidTmpFileToNormal();
	 				if (ret == false) {
	 					// 文件下载出错
	 					mHandler.sendEmptyMessage(MSG_MODEUL_FILE_FAILED);
	 					return;
	 				}
				}
				
				@Override
				public void cancel() {
					// TODO Auto-generated method stub
					Log.d(TAG,"download cancel");
				}
			});  
       	 	this.showProgressDialog();
       	 	mDownloader.getAsyncFileSize(new Downloader.AsyncLisenter() {
				
				@Override
				public void getFileReady() {
					// TODO Auto-generated method stub
					mHandler.sendEmptyMessage(MSG_HIDE_PROGESSDIALOG);
					mHandler.sendEmptyMessage(MSG_SHOW_DOWNLOAD_MODULE_FILE);				
				}
				
				@Override
				public void getFileFailed() {
					// TODO Auto-generated method stub
					mHandler.sendEmptyMessage(MSG_HIDE_PROGESSDIALOG);
				}
			});
        } else{
        	mHandler.sendEmptyMessage(MSG_SHOW_DOWNLOAD_MODULE_FILE);		
        } 
	}
	
	private ProgressDialog mProgressDialog = null; 

	// 提示用户下载人脸识别模型数据包
	public void startModuleDownloadConfirmDialog() {
		if (mContext == null)
			return;
		//对话框正在显示
		if( mModuleDownloadConfirmDialog != null && mModuleDownloadConfirmDialog.isShowing() == true ){
			return;
		}
		
		
		//初始化下载信息
		initDownload();
		
		
		
	}
	
	public void showModuleDownloadConfirmDialog(){
		if( mProgressDialog != null ){
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		// 构造对话框
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		if (mLocalVersion != null) {// 本地模型数据存在
			builder.setTitle(R.string.module_update_title);
		} else {
			builder.setTitle(R.string.module_download_title);
		}
		LoadInfo info = mDownloader.getDownloaderInfors();
		int complete = 0;
		if( info != null ){
			complete = info.getComplete();
		}
		if( complete > 0 ){
			builder.setMessage("人脸识别模型数据包大小为" + mDataSize + ",文件较大，建议在wifi环境下载，检测到上次下载文件存在，是否继续下载？");
		}else{
			builder.setMessage("人脸识别模型数据包大小为" + mDataSize + ",文件较大，建议在wifi环境下载，是否下载？");
		}
		Dialog.OnClickListener positiveListener = new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 开始下载
				dialog.dismiss();
				mModuleDownloadConfirmDialog = null;
				showDownloadProgressDialog(mModuleUrl);
			}
		};
		Dialog.OnClickListener negativeListener = new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//稍后下载
				dialog.dismiss();
				mModuleDownloadConfirmDialog = null;
				if (mModuleUrl == null) {
					Toast.makeText(mContext, "人脸识别数据模型包下载后才能进行人脸识别", Toast.LENGTH_SHORT).show();
				}
			}
		};
		// 更新
		if (mLocalVersion == null) {
			builder.setPositiveButton(R.string.download_now, positiveListener);
			builder.setNegativeButton(R.string.download_later, negativeListener);
		} else {
			builder.setPositiveButton(R.string.update_now, positiveListener);
			builder.setNegativeButton(R.string.update_later, negativeListener);
		}
		builder.setCancelable(false);
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
		mModuleDownloadConfirmDialog = noticeDialog;
	}
	
	public boolean getFaceVersionText(final boolean showToast, final int nextStep,String url){
		Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"response:"+response);
//				int code = response.optInt("code");
//				if( code == 0 ){//成功
//					JSONObject obj = response.optJSONObject("obj");
//					if( obj != null ){
//						String txtUrl = obj.optString("url");
//					}
//				}
				JSONObject obj;
				try {
					
					obj = new JSONObject(response);
					String url = obj.optString("url");
					//模型版本号已经取过一次
					if( mModuleUrl != null && mModuleUrl.equals(url) ){
						return;
					}
					mModuleUrl = url;
					mPlatformVersion = obj.optString("version");
					mDataSize = obj.optString("datasize");
					mModuleFileMd5 = obj.optString("md5sum");
					Log.d(TAG,"mModuleUrl:"+mModuleUrl);
					if (mContext == null)
						return;
					mHandler.sendEmptyMessageDelayed(nextStep, 0);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		StringRequest request = new StringRequest(url,
				listener, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {

					}
				});
		InfoReleaseApplication.requestQueue.add(request);
		return true;
	}
	
	private Dialog mWaitDialog = null;
	
	private void hideProgressDialog(){
		Log.d(TAG,"hideProgressDialog");
		if( mWaitDialog != null ){
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}
	}
	
	private void showProgressDialog(){
		mWaitDialog = new Dialog(mContext,R.style.new_circle_progress);    
		mWaitDialog.setContentView(R.layout.dialog_wait);    
		mWaitDialog.show();
	}

	/**
	 * 检查module文件是否有更新版本
	 * 
	 * @return
	 */
	public boolean getModuleVersionInfo(final boolean showToast, final int nextStep) {
		if (mContext == null)
			return false;
		if (this.showNetWorkFailed(mContext) == false) {// 网络连接失败
			return false;
		}
		this.showProgressDialog();
		String url = SmartCampusUrlUtils.getFaceVersionUrl();
		Log.d(TAG, "getModuleVersionInfo:" + url);
		Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				mHandler.sendEmptyMessage(MSG_HIDE_PROGESSDIALOG);
				int code = response.optInt("code");
				if( code == 0 ){//成功
					JSONObject obj = response.optJSONObject("obj");
					if( obj != null ){
						String txtUrl = obj.optString("url");
						getFaceVersionText(showToast,nextStep,txtUrl);
					}
				}

			}
		};
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
				listener, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						mHandler.sendEmptyMessage(MSG_HIDE_PROGESSDIALOG);
					}
				});
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

		return true;
	}

	// when module file changed,delete all data files
	// 删除根据照片生成的fea数据文件，模型文件更新时，数据文件删除，重新生成，否则会不匹配，识别有问题
	public void deleteAllDataFiles() {
		File dataFile = new File(mFaceDir, DATA_DIR_NAME);
		if (dataFile.exists() == true) {
			File[] files = dataFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory() == true) {
					File[] subFiles = files[i].listFiles();
					for (int j = 0; j < subFiles.length; j++) {
						if (subFiles[j].getName().endsWith(MODULE_DATA_SUFFIX)) {
							subFiles[j].delete();
						}
					}
				}
			}
		}
	}

	private ProgressBar mProgressBar = null;
	private TextView mUpdateTextView = null;
	private AlertDialog mDownloadDialog = null;
	private Downloader mDownloader = null;
    
    private void incrementProgressBy(int length){
    	mProgressBar.incrementProgressBy(length); 
    	int progress = (int)(((long)mProgressBar.getProgress())*100/mProgressBar.getMax());
    	mUpdateTextView.setText(progress+"%");
    }
    
    /**  
     * 利用消息处理机制适时更新进度条  
     */  
    private Handler mDownloadHandler = new Handler() {  
        public void handleMessage(Message msg) {  
            if (msg.what == 1) {  
                String url = (String) msg.obj;  
                int length = msg.arg1;  
                if ( mProgressBar != null) {  
                    // 设置进度条按读取的length长度更新  
                	incrementProgressBy(length); 
                	if ( mProgressBar.getProgress() >= mProgressBar.getMax() && mDownloader != null) { 
						Log.d(TAG,"download complete");
		                Toast.makeText(mContext, "下载完成！", 0).show();  
		                 // 下载完成后将map中的数据清空  
		                
		                mDownloader.delete(mModuleUrl);
		                mDownloader.reset();
		                mDownloader = null;   
		 				boolean ret = makeValidTmpFileToNormal();
		 				if (ret == false) {
		 					// 文件下载出错
		 					mHandler.sendEmptyMessage(MSG_MODEUL_FILE_FAILED);
		 				}
					}
                }  
            }
        }  
    };  
	// 显示下载进度对话框
	private void showDownloadProgressDialog(String url) {
//		Log.d(TAG, "showDownloadDialog url:" + url);
		if (mContext == null)
			return;
		//下载进度对话框正在显示
		if( mDownloadDialog != null && mDownloadDialog.isShowing() == true ){
			return;
		}
	
		// 构造软件下载对话框
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.downloading);
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.filedownload_progress, null);
		mProgressBar = (ProgressBar) v.findViewById(R.id.update_progress);
		mUpdateTextView = (TextView) v.findViewById(R.id.update_text);
		builder.setView(v);

		// 取消更新
		builder.setNegativeButton(R.string.soft_update_cancel, new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "showDownloadDialog onClick cancel update");
				//暂停下载
				mDownloader.pause();
				mHandler.sendEmptyMessage(MSG_CHECK_DOWNLOAD_CANCELED);
			}
		});
		builder.setCancelable(false);
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		     
        // 得到下载信息类的个数组成集合  
        LoadInfo loadInfo = mDownloader.getDownloaderInfors();  
        //显示进度条
        mProgressBar.setMax(loadInfo.getFileSize());  
        
        incrementProgressBy(loadInfo.getComplete());
        
        if ( mDownloader.isdownloading() ) //正在下载
            return; 
        
        //开始下载
        mDownloader.download();
	}
	

	public void removeTmpModelFile() {
		// 如果临时文件存在，删除临时文件，重新下载
		File tmpFile = new File(mFaceDir, TMP_MODULE_FILE_NAME);
		if (tmpFile.exists() == true) {
			tmpFile.delete();
		}
	}

	private PriorityQueue<String> mGetFaceDataImages = new PriorityQueue<String>();

	// 根据图片路径生成fea数据，用于人脸识别。此函数运行与线程中，所以部分数据需要同步
	private void getFaceDataOfImage(String imagePath) {
		Log.d(TAG, "getFaceDataOfImage:" + imagePath);
		File moduleDataFile = new File(imagePath.substring(0, imagePath.length() - 3) + MODULE_DATA_SUFFIX);
		if (moduleDataFile.exists() == false) {
			File imageFile = new File(imagePath);
			String parent = imageFile.getParent();

			// mNativeDetector是人脸识别库中实例，比较耗资源，保证只有一个实例运行
			if (mNativeDetector == null) {
				System.loadLibrary("seeta_facedet");
				System.loadLibrary("seeta_fa_lib");
				System.loadLibrary("viplnet");
				System.loadLibrary("opencv_java3");
				System.loadLibrary("detection_based_tracker");
				String dirModel = mFaceDir.getAbsolutePath() + "/" + MODULE_DIR_NAME;
				String dirPicture = imageFile.getParent();
				Log.d(TAG, "dirModel:" + dirModel + ",dirPicture:" + dirPicture);
				mNativeDetector = new DetectionBasedTracker(dirModel, dirPicture, 0);
			} else {
				synchronized (mNativeDetector) {
					if (mNativeDetector.getDirPicture().equals(parent) == false) {
						mNativeDetector.release();
						mNativeDetector = new DetectionBasedTracker(mFaceDir.getAbsolutePath() + "/" + MODULE_DIR_NAME,
								imageFile.getParent(), 0);
					}
				}
			}
			synchronized (mNativeDetector) {
				mNativeDetector.extractFeature(imageFile.getAbsolutePath());
			}
			// }
		}
	}

	// 生成照片fea数据文件的线程
	public class GetFaceDataThread extends Thread {

		// 继承Thread类，并改写其run方法
		private final static String TAG = "GetFaceDataThread";

		public void run() {
			Log.d(TAG, "run");
			while (true) {
				if (mLocalVersion != null) {// 存在本地module文件
					String imagePath = null;
					synchronized (mGetFaceDataImages) {
						if (mGetFaceDataImages.size() > 0) {
							imagePath = mGetFaceDataImages.peek();
						}
					}
					if (imagePath != null) {
						Log.d(TAG, "GetFaceDataThread path:" + imagePath);
						getFaceDataOfImage(imagePath);
						synchronized (mGetFaceDataImages) {
							mGetFaceDataImages.poll();
						}
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// private void showDownloadProgressDialog(Context context,String url,String
	// savePath) {
	// ProgressDialog progressDialog = new ProgressDialog(context);
	// progressDialog.setTitle("提示");
	// progressDialog.setMessage("正在下载...");
	// progressDialog.setIndeterminate(false);
	// progressDialog.setMax(100);
	// progressDialog.setCancelable(false);
	// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	// progressDialog.show();
	// // String downloadUrl = "url";
	// // new DownloadAPK(progressDialog).execute(url,savePath);
	// }

	/**
	 * 下载文件的异步任务
	 */
//	private class DownloadFile extends AsyncTask<String, Integer, String> {
//		// ProgressDialog progressDialog;
//		DownloadListener mListener;
//		File file;
//		long total;
//
//		public DownloadFile(DownloadListener listener) {
//			// this.progressDialog = progressDialog;
//			mListener = listener;
//		}
//		
//		public long getCompleteSize(){
//			return total;
//		}
//
//		@Override
//		protected String doInBackground(String... params) {
//			if (isCancelled())
//				return null;// 判断是否被取消
//			URL url;
//			HttpURLConnection conn = null;
//			BufferedInputStream bis = null;
////			FileOutputStream fos = null;
//			RandomAccessFile randomAccessFile = null; 
//
//			try {
////				Log.d(TAG, "params[0]:" + params[0] + ",params[1]:" + params[1]+",isCancelled():"+isCancelled());
//				url = new URL(params[0]);
//				conn = (HttpURLConnection) url.openConnection();
//				conn.setRequestMethod("GET");
//				conn.setConnectTimeout(5000);
//
//				int fileLength = conn.getContentLength();
//				bis = new BufferedInputStream(conn.getInputStream());
//				
//				File file = new File(params[1]);  
//	            if (!file.exists()) {  
//	                 file.createNewFile();  
//	            } 
//				randomAccessFile = new RandomAccessFile(params[1], "rwd");  
//				randomAccessFile.setLength(fileLength);  
//                randomAccessFile.seek(startPos + compeleteSize); 
//                 
//				file = new File(params[1]);
//				if (!file.exists()) {
//					if (!file.getParentFile().exists()) {
//						file.getParentFile().mkdirs();
//					}
//					file.createNewFile();
//				}
////				fos = new FileOutputStream(file);
//				byte data[] = new byte[4 * 1024];
//				total = 0;
//				int count;
//				while ((count = bis.read(data)) != -1) {
////					Log.d(TAG, "doInBackground isCancelled():"+isCancelled());
//					total += count;
//					if (isCancelled()) {// 判断是否被取消
//						if (randomAccessFile != null) {
//							randomAccessFile.close();
//						}
//						if (bis != null) {
//							bis.close();
//						}
//						if( conn != null ){
//							conn.disconnect(); 
//						}
//						return null;
//					}
//					publishProgress((int) (total * 100 / fileLength));
//					randomAccessFile.write(data, 0, count);  
////					fos.write(data, 0, count);
////					Log.d(TAG, "doInBackground 111 isCancelled():"+isCancelled()+",total:"+total+",count:"+count);
////					fos.flush();
//				}
////				fos.flush();
//
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//				try {
//					if (randomAccessFile != null) {
//						randomAccessFile.close();
//					}
//					if (bis != null) {
//						bis.close();
//					}
//					if( conn != null ){
//						conn.disconnect(); 
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				
//			}
//
//			return null;
//		}
//
//		@Override
//		protected void onProgressUpdate(Integer... progress) {
//			super.onProgressUpdate(progress);
////			Log.d(TAG, "onProgressUpdate progress[0]:" + progress[0]);
//			if (mListener != null) {
//				mListener.progressUpdate(progress[0]);
//			}
//			// progressDialog.setProgress(progress[0]);
//		}
//
//		// 该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//			if (mListener != null) {
//				mListener.start();
//			}
//		}
//
//		@Override
//		protected void onCancelled() {
//			super.onCancelled();
//			if (mListener != null) {
//				mListener.cancel();
//			}
//		}
//
//		@Override
//		protected void onPostExecute(String s) {
//			super.onPostExecute(s);
//			if (mListener != null) {
//				mListener.complete();
//			}
//			// progressDialog.dismiss();
//		}
//	}
	
	public static String getGraffitiPicFilePath(String filename) {
		if( InfoReleaseApplication.authenobjData == null || InfoReleaseApplication.authenobjData.userName == null ){
			return null;
		}
		File picDir = new File(mFaceDir, DATA_DIR_NAME +GRAFFITI_PIC);
		
		if (picDir.exists() == false) {
			picDir.mkdir();
		}
		return picDir.getAbsolutePath() + "/" + filename;
	}
	
}
