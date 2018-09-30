package com.routon.inforelease.plan.create;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.routon.ad.element.StringUtils;
import com.routon.ad.pkg.FileGetTask;
import com.routon.ad.pkg.HttpGetTask;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.classinfo.ClassInfoEditActivity;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.MaterialParams;
import com.routon.inforelease.plan.create.PictureListAdapter.OnImageClickedListener;
import com.routon.inforelease.plan.create.pictureAdd.AddPicSelAdapter;
import com.routon.inforelease.plan.create.pictureAdd.ImageFloder;
import com.routon.inforelease.plan.create.pictureAdd.ListImageDirPopupWindow;
import com.routon.inforelease.plan.create.pictureAdd.AddPicSelAdapter.OnCamerClicked;
import com.routon.inforelease.plan.create.pictureAdd.AddPicSelAdapter.OnImageClicked;
import com.routon.inforelease.plan.create.pictureAdd.ListImageDirPopupWindow.OnImageDirSelected;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStack;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStringRequest;

import android.app.Activity;

public class OfflinePicSelectActivity extends CustomTitleActivity implements OnImageDirSelected {

	private static final String TAG = "OfflinePicSelectActivity";

	private int currentAllPage = -1;
	private int currentMyPage = -1;

	private RadioGroup switchGroup;
	private ArrayList<MaterialItem> allMaterialDatas = new ArrayList<MaterialItem>();
	private ArrayList<MaterialItem> myMaterialDatas = new ArrayList<MaterialItem>();

	private int defaultPageSize = 20;
	private int picFileType = MaterialParams.TYPE_AD_PICTURE;
	private int classInfoType = 1;
	private int ON_TEXT_SELECT_ACTIVITY_FINISH = 0;
	private int ON_PICTURE_ADD_FINISH = 1;
	private int ON_GROUP_SELECT_ACTIVITY_FINISH = 2;
	private int materialType;// 0:mine 1:all

	private PullToRefreshGridView picsGridView;
	private Button minePicsBtn;
	private Button otherPicsBtn;
	private PictureListAdapter picturesAdapter = null;
	private String mStartBy;

	private ProgressDialog mProgressDialog;

	private int mFileType = MaterialParams.TYPE_AD_PICTURE;

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

	int totalCount = 0;

	private int mScreenHeight;

	private ListImageDirPopupWindow mListImageDirPopupWindow;

	public static final String exteranlPath = "/infoRelease_photos/";

	private boolean isLocalPicture = true;
	private ProgressDialog progressDialog;
	private boolean isCaptureImg=false;
	private PlanListrowsBean mNewPlanBean;
	private ClassInfoListdatasBean mNewClassInfoBean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.plan_pictures_fragment);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			editType = bundle.getString("editType");
			mStartBy = bundle.getString("start_by");
			picFileType = bundle.getInt("fileType", MaterialParams.TYPE_AD_PICTURE);
			classInfoType = bundle.getInt("classInfoType", 1);
			startType = bundle.getString("start_type");
		}

		initTitleBar(R.string.pictures_select_title);

		setTitleNextBtnClickListener("保存",new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isLocalPicture) {// 本地素材

					if (mAdapter.mSelectedImage.size() > 0) {

						List<String> images = mAdapter.mSelectedImage;
						ArrayList<String> paramStr = new ArrayList<String>();

						ArrayList<String> imageList = new ArrayList<String>();
						for (String url : images) {
							imageList.add("file://" + url);
							paramStr.add(url);
						}

						if ( mStartBy!= null && mStartBy.equals("class_info_add")) {//班牌添加图片
							if (picFileType == MaterialParams.TYPE_CLASS_TEMPLATE) {

								return;
							}
							if (imageList.size() == 0) {
								reportToast("请至少选择一张图片");
								return;
							}
							Intent data = new Intent(OfflinePicSelectActivity.this, ClassInfoEditActivity.class);
							data.putStringArrayListExtra("select_pic_param", paramStr);
							data.putStringArrayListExtra("select_pic_url", imageList);
							data.putExtra("classInfoType", classInfoType);
							data.putExtra("editType", editType);
							startActivity(data);
							finish();
						}else if ( mStartBy!= null && mStartBy.equals("edit")) {//节目添加图片
							Intent data = new Intent();
							data.putStringArrayListExtra("select_pic_param", paramStr);
							data.putStringArrayListExtra("select_pic_url", imageList);
							setResult(Activity.RESULT_OK, data);
							finish();						
						}else{
							Intent intent=new Intent(OfflinePicSelectActivity.this,TextSelectActivity.class);
							intent.putExtra("start_by", "plan_list");
							intent.putStringArrayListExtra("image_list", imageList);
							intent.putExtra("start_type", startType);
							startActivity(intent);
							finish();
							
						}

					} else {
						Toast.makeText(OfflinePicSelectActivity.this, R.string.no_pics_error, Toast.LENGTH_SHORT)
								.show();
					}

				} else {// 在线素材

					int len = getSelectMaterialList().size();
					if (len == 0) {
						Toast.makeText(OfflinePicSelectActivity.this, "没有选中任何素材!", Toast.LENGTH_SHORT).show();
						return;
					}

					ArrayList<String> paramStr = new ArrayList<String>();
					ArrayList<String> urls = new ArrayList<String>();
					int length = getSelectMaterialList().size();
					for (int i = 0; i < length; i++) {
						MaterialItem item = getSelectMaterialList().get(i);

						paramStr.add(Integer.toString(item.getId()));
						urls.add(item.getContent());
					}
					if (mStartBy.equals("class_info_add")) {
						if (picFileType == MaterialParams.TYPE_CLASS_TEMPLATE) {

							return;
						}
						if (urls.size() == 0) {
							reportToast("请至少选择一张图片");
							return;
						}
						Intent data = new Intent(OfflinePicSelectActivity.this, ClassInfoEditActivity.class);
						data.putStringArrayListExtra("select_pic_param", paramStr);
						data.putStringArrayListExtra("select_pic_url", urls);
						data.putExtra("classInfoType", classInfoType);
						data.putExtra("editType", editType);
						startActivity(data);
						finish();

					} else if (mStartBy.equals("edit")) {//在线图片要制作成离线图片素材，将在线图片缓存到本地
						loadProgressDialog = new ProgressDialog(OfflinePicSelectActivity.this);
						mCacheImageNum = 0;
						mOnlineUrls = urls;
						Log.d(TAG,"mOnlineUrls:"+mOnlineUrls.size());
						for( String url:urls){
							loadImage(url);
						}					
						
//						Intent data = new Intent();
//						data.putStringArrayListExtra("select_pic_param", paramStr);
//						data.putStringArrayListExtra("select_pic_url", urls);
//						setResult(Activity.RESULT_OK, data);
//						finish();						
					}else {

						Intent intent=new Intent(OfflinePicSelectActivity.this,TextSelectActivity.class);
						intent.putExtra("start_by", "plan_list");
						intent.putStringArrayListExtra("image_list", urls);
						intent.putExtra("start_type", startType);
						startActivity(intent);
						finish();

					}

				}

			}
		});

		currentAllPage = -1;
		currentMyPage = -1;
		initView();

		if (picFileType == MaterialParams.TYPE_CLASS_PICTURE) {
			switchGroup.setEnabled(false);
		}
	}
	
	private Handler mImageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				cacheImage(msg.obj.toString());
				break;
			}
		}
	};
	
	//缓存完一个图片
	private void cacheImage(String path){
		mCacheImageNum++;
		if( mCachePaths == null ){
			mCachePaths = new ArrayList<String>();
		}
		mCachePaths.add(path);
		
		Log.d(TAG,"cacheImage path:"+path+",mCacheImageNum:"+mCacheImageNum
				+",mOnlineUrls.size:"+mOnlineUrls.size());
		//图片全部缓存完毕
		if( mOnlineUrls != null && mOnlineUrls.size() == mCacheImageNum ){
			loadProgressDialog.dismiss();
			Intent data = new Intent();
			data.putStringArrayListExtra("select_pic_url", mCachePaths);
			setResult(Activity.RESULT_OK, data);
			finish();
		}
	}
	
	private int mCacheImageNum = 0;
	private ArrayList<String> mOnlineUrls = null; 
	private ArrayList<String> mCachePaths = null; 
	
	//缓存图片到本地
	private void loadImage(String url) {
		String name = StringUtils.getFileName(url);
		File file = new File(getCacheDir(), name);
		String path = file.getPath();
		Log.d(TAG,"loadImage url:"+url);
		if (file.exists()) {//存在对应本地图片,删除图片，以防出现图片出错的情况
			file.delete();
		}
		FileGetTask task = new FileGetTask(url, path, url);
		task.setOnHttpGetTaskListener(new HttpGetTask.OnHttpGetTaskListener() {

			@Override
			public void onTaskStarted(HttpGetTask task) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTaskFinished(HttpGetTask task, int code) {
				String path = null;
				if (code == HttpGetTask.ERR_NONE) {//下载完成本地图片
					FileGetTask t = (FileGetTask) task;
					path = t.getFilePath();				
				}	
				Message msg = Message.obtain(mImageHandler, 0, path);
				mImageHandler.sendMessage(msg);
			}
		});
		new Thread(task).start();
	}

	private void initView() {
		picturesAdapter = new PictureListAdapter(this, allMaterialDatas);
		picsGridView = (PullToRefreshGridView) findViewById(R.id.pics_grid_view);

		setPushRefreshListener();
		setImageClickListener();

		minePicsBtn = (RadioButton) findViewById(R.id.mine_pics);
		otherPicsBtn = (RadioButton) findViewById(R.id.other_pics);
		mGirdView = (GridView) findViewById(R.id.id_gridView);
		switchGroup = (RadioGroup) findViewById(R.id.switch_group);
		switchGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if( checkedId == R.id.mine_pics ){
					isLocalPicture = true;
					picsGridView.setVisibility(View.GONE);
					mGirdView.setVisibility(View.VISIBLE);

					materialType = 0;
					if (currentMyPage == -1) {
						currentMyPage = 1;

						getMineMaterials();
					} else {
						picturesAdapter.setDatas(myMaterialDatas);
						picsGridView.setAdapter(picturesAdapter);
					}

					switchGroup.setBackgroundResource(R.drawable.switch_l);
					minePicsBtn.setTextColor(Color.WHITE);
					otherPicsBtn.setTextColor(Color.rgb(44, 145, 225));
				}else if( checkedId == R.id.other_pics ){
					Log.d(TAG, "checkedId:  R.id.other_pics");
					isLocalPicture = false;
					mGirdView.setVisibility(View.GONE);
					picsGridView.setVisibility(View.VISIBLE);

					materialType = 1;
					if (currentAllPage == -1) {
						currentAllPage = 1;
						allMaterialDatas.clear();
						getPictureMaterials(1, currentAllPage,true);
					} else {

						picturesAdapter.setDatas(allMaterialDatas);
						picsGridView.setAdapter(picturesAdapter);
					}
					switchGroup.setBackgroundResource(R.drawable.switch_r);
					otherPicsBtn.setTextColor(Color.WHITE);
					minePicsBtn.setTextColor(Color.rgb(44, 145, 225));
				}
			}
		});

		switchGroup.check(R.id.mine_pics);
	}

	public static String getExternalStorageDirectory() {
		return Environment.getExternalStorageDirectory() + exteranlPath;
	}

	public ArrayList<MaterialItem> getSelectMaterialList() {

		return picturesAdapter.selectMaterails;
	}

	

	private void setPushRefreshListener() {
		picsGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
				Log.i(TAG, "------onPullDownToRefresh-------");

			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {

				if (materialType == 0) {
					getPictureMaterials(0, ++currentMyPage,false);
				}

				if (materialType == 1) {
					getPictureMaterials(1, ++currentAllPage,false);
				}
			}
		});
	}

	private void setImageClickListener() {
		picturesAdapter.setListener(new OnImageClickedListener() {

			@Override
			public void onImageClicked(int position) {

				MaterialItem item = null;
				if (materialType == 1) {
					item = allMaterialDatas.get(position);
				}

				if (item != null) {
					String name = item.getContent();
					Intent previewIntent = new Intent(OfflinePicSelectActivity.this, PicPreviewActivity.class);
					previewIntent.putExtra("path", name);
					startActivity(previewIntent);
				}
			}
		});
	}

	public void getPictureMaterials(int type, int page, boolean isShowDialog) {
		materialType = type;
		String lastResId = null;
		switch (type) {
		case 1:
			if (allMaterialDatas.size() != 0) {
				MaterialItem item = allMaterialDatas.get(allMaterialDatas.size() - 1);
				lastResId = Integer.toString(item.getId());
			}
			getMaterialLists(page, defaultPageSize, picFileType, "my", lastResId,isShowDialog);
			break;
		}

	}
	private ProgressDialog loadProgressDialog;
	private void getMaterialLists(final int page, int pageSize, int FileType, String flag, String flagId,boolean isShowDialog) {
		if (pageSize == -1)
			pageSize = 10;

		String urlString = UrlUtils.getResourceListUrl(page, pageSize, FileType, flag, flagId, null, null, null);
		Log.d(TAG, "URL:" + urlString);

		if (isShowDialog) {
			loadProgressDialog = ProgressDialog.show(this, "", "...Loading...");
		}else{
			loadProgressDialog=new ProgressDialog(this);
		}

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if ( loadProgressDialog != null && loadProgressDialog.isShowing() ) {
							loadProgressDialog.dismiss();
						}

						try {
							int code = response.optInt("code"); 
							if ( code == 0) { // 返回成功
								MaterialRequestResult requestResult = new MaterialRequestResult();
								requestResult.info.code = code;
								requestResult.info.msg = response.optString("msg");
								requestResult.info.fullListSize = response.optInt("fullListSize");
								requestResult.info.page = response.optInt("page");
								requestResult.info.pageSize = response.optInt("pageSize");

								JSONArray jsonArray = response.getJSONArray("datas");
								if (jsonArray.length() == 0) {

									picsGridView.onRefreshComplete();

									if (page == 1) {
										updateGridView(materialType);
									} else {
										if (materialType == 0) {
											currentMyPage--;
										} else {
											currentAllPage--;
										}
										Toast.makeText(OfflinePicSelectActivity.this, "没有了!", Toast.LENGTH_LONG).show();
									}
									return;
								}
								for (int i = 0; i < jsonArray.length(); i++) {
									JSONObject obj = jsonArray.getJSONObject(i);
									MaterialItem item = new MaterialItem();
									item.setId(obj.optInt("resid"));
									item.setType(obj.optInt("filetypeid"));
									item.setContent(obj.optString("content"));
									item.setCreatetime(obj.optString("createtime"));
									if (materialType == 0) {
										myMaterialDatas.add(item);
									} else {
										allMaterialDatas.add(item);
									}

									Log.d(TAG, "resid:" + item.getId() + "  filetype:" + item.getType() + "  content:"
											+ item.getContent() + " createtime:" + item.getCreatetime());
								}

								updateGridView(materialType);

							} else if (code == -2) {
								InfoReleaseApplication.returnToLogin(OfflinePicSelectActivity.this);
							} else {// 失败
								Log.e(TAG, response.optString("msg"));
								Toast.makeText(OfflinePicSelectActivity.this, response.optString("msg"),
										Toast.LENGTH_LONG).show();

							}

							picsGridView.onRefreshComplete();
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(OfflinePicSelectActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						if ( loadProgressDialog != null && loadProgressDialog.isShowing() ) {
							loadProgressDialog.dismiss();
						}

						picsGridView.onRefreshComplete();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void updateGridView(final int type) {
		switch (type) {
		case 0:// mine

			mAdapter.notifyDataSetChanged();
			break;
		case 1:// other
			picturesAdapter.setDatas(allMaterialDatas);
			picturesAdapter.notifyDataSetChanged();
			break;
		}
	}

	private void getMineMaterials() {
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
				ContentResolver mContentResolver = OfflinePicSelectActivity.this.getContentResolver();

				Cursor mCursor = mContentResolver.query(mImageUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED+ " DESC");

				mAllImgs.add("camera");
				
				String filePath = Environment.getExternalStorageDirectory().toString() + File.separator  
		                + "infoRelease_photos"; 
				File fileAll = new File(filePath);  
		        File[] files = fileAll.listFiles(); 
		        if (files!=null&&files.length>0) {
		        	
		        	for (int start = 0, end = files.length - 1; start < end; start++, end--) {//倒序
		                File temp = files[end];
		                files[end] = files[start];
		                files[start] = temp;
		            }
		        	
		        	for (int i = 0; i < files.length; i++) {  
			            mAllImgs.add(files[i].getPath());  
			        }
				}
				
				// Log.e("TAG", mCursor.getCount() + "");
				while (mCursor.moveToNext()) {
					// 获取图片的路径
					String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));

					if (!mAllImgs.contains(path)) {
						mAllImgs.add(path);//保存所有扫描到的图片路径
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
//				String filePath = Environment.getExternalStorageDirectory().toString() + File.separator  
//		                + "infoRelease_photos"; 
//				File fileAll = new File(filePath);  
//		        File[] files = fileAll.listFiles(); 
//		        if (files!=null&&files.length>0) {
//		        	for (int i = 0; i < files.length; i++) {  
//			            mAllImgs.add(files[i].getPath());  
//			        }
//				}
		        
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

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mProgressDialog.dismiss();
			// 为View绑定数据
			data2View();
			
		}
	};

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

	public void UploadFiles(final String url, final Map<String, File> files, final Map<String, String> params,
			final Listener<String> responseListener, final ErrorListener errorListener, final Object tag) {
		if (null == url || null == responseListener) {
			return;
		}

		progressDialog = ProgressDialog.show(this, "", "...Loading...");
		RequestQueue mSingleQueue = Volley.newRequestQueue(OfflinePicSelectActivity.this, new MultiPartStack());
		MultiPartStringRequest multiPartRequest = new MultiPartStringRequest(Request.Method.POST, url, responseListener,
				errorListener) {

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
				Toast.makeText(OfflinePicSelectActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
			}

			@Override
			public int getCurrentTimeout() {
				return 20 * 1000;
			}

			@Override
			public int getCurrentRetryCount() {
				return 2;
			}
		});
		multiPartRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		mSingleQueue.add(multiPartRequest);
	}

	private void setOnImageClickedListener() {
		if (mAdapter == null)
			return;

		mAdapter.setOnImageClickedListener(new OnImageClicked() {

			@Override
			public void onImageClicked(String imagPath) {
				Intent previewIntent = new Intent(OfflinePicSelectActivity.this, PicPreviewActivity.class);

				previewIntent.putExtra("path", imagPath);
				startActivity(previewIntent);
			}
		});
	}

	@Override
	public void selected(ImageFloder floder) {
		if (floder.isAllImageFolder()) {
			ArrayList<String> allImageList=new ArrayList<String>();
			for (int i = 0; i < mAllImgs.size(); i++) {
				if (!allImageList.contains(mAllImgs.get(i))) {
					allImageList.add(mAllImgs.get(i));
				}
			}
			mAdapter = new AddPicSelAdapter(OfflinePicSelectActivity.this, allImageList, R.layout.add_picture_grid_item,
					null,isCaptureImg);
			
			mAdapter.setOnCamerClickedlistener(new OnCamerClicked() {

				@Override
				public void onCamerClicked() {
					getImageFromCamera();
				}
			});
			setOnImageClickedListener();

			mGirdView.setAdapter(mAdapter);
			this.isCaptureImg=false;
		} else {

			mImgDir = new File(floder.getDir());
			mImgs = Arrays.asList(mImgDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg"))
						return true;
					return false;
				}
			}));
			/**
			 * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
			 */
			mAdapter = new AddPicSelAdapter(OfflinePicSelectActivity.this, mImgs, R.layout.add_picture_grid_item,
					mImgDir.getAbsolutePath(),false);
			setOnImageClickedListener();
			mGirdView.setAdapter(mAdapter);

		}

		if (mListImageDirPopupWindow != null && mListImageDirPopupWindow.isShowing())
			mListImageDirPopupWindow.dismiss();

	}

	private int REQUEST_CODE_CAPTURE_CAMEIA = 1;
	private String capturePath = null;

	private String editType;

	private String startType;

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
					this.isCaptureImg=true;
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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mAdapter.mSelectedImage.size() > 0) {
			mAdapter.mSelectedImage.clear();
		}
	}
	

}
