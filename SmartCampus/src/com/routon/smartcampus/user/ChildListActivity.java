package com.routon.smartcampus.user;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.net.NetWorkRequest;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.widget.PicSelHelper;
import com.routon.inforelease.widget.PopupList;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;
import com.routon.smartcampus.SmartCampusApplication;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.widgets.Toast;

public class ChildListActivity extends CustomTitleActivity{
	private ArrayList<StudentBean> mDatas = null;
	private final static String TAG = "ChildListActivity";
	private ListView mListview = null;
	private MyAdapter mAdapter = null;
	private PicSelHelper mPicSelHelper = null;
	
	private void initPicSel(){
		mPicSelHelper = new PicSelHelper(this);
		//临时图片保存路径
		File tmpFile = new File(getExternalCacheDir(),"temp.png");
		tmpFile.delete();
		mPicSelHelper.setDestUri(Uri.fromFile(tmpFile));
		
		mPicSelHelper.setCutImageMaxSize(354,472);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_childlist);

		initTitleBar(R.string.menu_child_manage);
		
		initPicSel();
		
		//20180702 不能删除和添加孩子
//		this.setTitleNextImageBtnClickListener(R.drawable.ic_add, new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent();
//				intent.setClass(ChildListActivity.this, ParentRegisterActivity.class);
//				intent.putExtra(MyBundleName.TYPE, ParentRegisterActivity.TYPE_ADD_CHILD);
//				ChildListActivity.this.startActivityForResult(intent, ADD_CHILD_REQUEST_CODE);
//			}
//		});
		mDatas = SmartCampusApplication.mStudentDatas;
		mListview = (ListView) this.findViewById(R.id.listview);
		mAdapter = new MyAdapter(this);
		mListview.setAdapter(mAdapter);
		mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				showPopupList(view,position,null);
			}
		});
//		this.setMoveBackEnable(false);
	}
	
	private int mSelPos = -1;
	
	private PopupList showPopupList(View anchorView, int contextPosition,PopupWindow.OnDismissListener listener) {
		List<String> popupMenuItemList = new ArrayList<String>();	
		//20180702 不能删除和添加孩子
//		popupMenuItemList.add("删除");
		popupMenuItemList.add("更新照片");
		int[] location = new int[2];
		anchorView.getLocationOnScreen(location);
		mSelPos = contextPosition;
		final float x = location[0] + anchorView.getWidth() / 2;
		final float y = location[1] + anchorView.getHeight() / 2;
		PopupList popupList = new PopupList(this);
		popupList.setOnDismissListener(listener);
		popupList.showPopupListWindow(anchorView, contextPosition, x, y, popupMenuItemList,
				new PopupList.PopupListListener() {
					@Override
					public void onPopupListClick(View contextView, int contextPosition, int position) {
						switch (position) {
//						case 0://删除
//							deleteChild(contextPosition);
//							break;
						case 0:// 更新照片
							mPicSelHelper.showAddPicDialog();
//							Intent intent = new Intent();
//							intent.setClass(ChildListActivity.this, ChildModifyActivity.class);
//							intent.putExtra(MyBundleName.STUDENT_BEAN, mDatas.get(contextPosition));
//							ChildListActivity.this.startActivityForResult(intent, UPDATE_PHOTO_REQUEST_CODE);
							break;
						}
					}

					@Override
					public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
						return true;
					}
				});
		return popupList;
	}
	
	private void emptyPhone(final int position){
		StudentBean bean = mDatas.get(position);
		if( bean == null ){
			return;
		}
		String url = SmartCampusUrlUtils.getUpdatePhoneCmdUrl(bean.sid, "");
		Log.d(TAG,"updatePhoto url:"+url);
		this.showProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						Log.d(TAG,"response:"+response);
						hideProgressDialog();
						int code = response.optInt("code",-1);
						if ( code == 0) {	
							mDatas.remove(position);				
							mAdapter.notifyDataSetChanged();
							Toast.makeText(ChildListActivity.this, "删除成功", Toast.LENGTH_LONG).show();	
						
						} else {
							Toast.makeText(ChildListActivity.this, "删除失败", Toast.LENGTH_LONG).show();						
						}									
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {			
						
						InfoReleaseApplication.showNetWorkFailed(ChildListActivity.this);
						Toast.makeText(ChildListActivity.this, "删除失败", Toast.LENGTH_LONG).show(50);
						hideProgressDialog();
					}
				});
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void updatePhone(final StudentBean bean,final String parentPhone){
		String url = SmartCampusUrlUtils.getUpdatePhoneCmdUrl(bean.sid, parentPhone);
		Log.d(TAG,"updatePhoto url:"+url);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						Log.d(TAG,"response:"+response);
						hideProgressDialog();
						int code = response.optInt("code",-1);
						if ( code == 0) {	
							mDatas.add(bean);
							bean.parentPhone = Long.parseLong(parentPhone);
							mAdapter.notifyDataSetChanged();
							Toast.makeText(ChildListActivity.this, "添加成功", Toast.LENGTH_LONG).show();	
						
						} else {
							Toast.makeText(ChildListActivity.this, "添加失败", Toast.LENGTH_LONG).show();						
						}									
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {			
						
						InfoReleaseApplication.showNetWorkFailed(ChildListActivity.this);
						Toast.makeText(ChildListActivity.this, "添加失败", Toast.LENGTH_LONG).show();
						hideProgressDialog();
					}
				});
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private final static int ADD_CHILD_REQUEST_CODE = 0;
	private final static int UPDATE_PHOTO_REQUEST_CODE = 1;
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ADD_CHILD_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				StudentBean addBean = (StudentBean) data.getSerializableExtra(MyBundleName.STUDENT_BEAN);
				if( addBean != null ){
					for( StudentBean bean:this.mDatas ){
						if( bean.sid == addBean.sid ){
							this.reportToast("已经存在孩子列表中");
							addBean = null;
							break;
						}
					}
				}
				if( addBean != null ){ 
					updatePhone(addBean,String.valueOf(mDatas.get(0).parentPhone));
				}
			}
			return;
		}
		if( true == mPicSelHelper.handleActivityResult(requestCode,resultCode,data) ){
			return;
		}
		if( requestCode == PicSelHelper.PHOTO_CUT){
			if (resultCode == Activity.RESULT_OK) {
				if( mPicSelHelper.getImageUri() != null ){
					sendProfileImage(mPicSelHelper.getImageUri().getPath());
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	//头像上传
	private boolean sendProfileImage(final String path) {
		File file = new File(path);
		if( file.exists() == false ){
			this.reportToast("请选择图片");
			return false;
		}
		showProgressDialog();
		String urlString = SmartCampusUrlUtils.getUpdatePhotoUrl();
		Log.i(TAG, "URL:" + urlString);
		Map<String, File> files = new HashMap<String, File>();
		Map<String, String> params = new HashMap<String, String>();		
		params.put("sid", String.valueOf(mDatas.get(mSelPos).sid));
		files.put("photo", file);
		NetWorkRequest.UploadFiles(this,urlString, files, params, new Listener<String>() {
				@Override
				public void onResponse(String response) {
					hideProgressDialog();

					BaseBean bean = BaseBeanParser
							.parseBaseBean(response);
					if (bean == null) {
						reportToast("上传照片失败!");
						hideProgressDialog();
						return;
					}
					Log.d(TAG, response);
					if (bean.code == 0) {
						reportToast("上传照片成功!");					
						StudentBean studentBean = mDatas.get(mSelPos);					
						File dcimFile = ImageUtils.getProfilePhoto(ChildListActivity.this,String.valueOf(studentBean.sid),
								studentBean.imageLastUpdateTime);
						final File portraitFile = ImageUtils.getProfilePhoto(ChildListActivity.this,String.valueOf(studentBean.sid),null);
						ImageUtils.copyFile(path,dcimFile.getAbsolutePath());
						ImageUtils.copyFile(dcimFile.getAbsolutePath(),portraitFile.getAbsolutePath());
						mAdapter.notifyDataSetChanged();						
					} else {
						if ( bean.msg != null && bean.msg.isEmpty() == false ){
							reportToast(bean.msg);
						}else{
							reportToast("上传照片失败!");
						}
					}
				}

			}, new ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					hideProgressDialog();
					if( true == InfoReleaseApplication.showNetWorkFailed(ChildListActivity.this) ){
						reportToast("上传照片失败!");
                	}
				}

			}, null);
			return true;
	}
		
	public void showDeleteDialog(final int position){
		
		new AlertDialog.Builder(this).setMessage("是否删除孩子？")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				emptyPhone(position);
				dialog.dismiss();
			}
		}).setNegativeButton("取消", null).show();
	}
	
	private void deleteChild(int pos){
		if( mDatas.size() == 1 ){
    		ChildListActivity.this.reportToast("至少保留一个孩子数据");
    	}else{
    		showDeleteDialog(pos);
    	}
	}
	
	//在外面先定义，ViewHolder静态类
  	static class ViewHolder
  	{
  	    public TextView mLabel;
  	    public TextView mSecondLabel;
  	    public TextView mThirdLabel;
  	    public ImageView mImageView;
//  	    public TextView mDeleteText;
  	}
	  
  	public class MyAdapter extends BaseAdapter
    {   
	    private LayoutInflater mInflater = null;
	    private Context mContext = null;      	      
	    	
        public MyAdapter(Context context)
        {
            this.mInflater = LayoutInflater.from(context);
            mContext = context;
        }
	      
	    @Override
	    public int getCount() {
      		if( mDatas == null ) return 0;   	
      		return mDatas.size();
	    }
	    
	    @Override
	    public Object getItem(int position) {
	          // Get the data item associated with the specified position in the data set.
	          //获取数据集中与指定索引对应的数据项
	        return position;
	    }
	    
	    @Override
	    public long getItemId(int position) {
	          //Get the row id associated with the specified position in the list.
	          //获取在列表中与指定索引对应的行id
	          return position;
	    }
	                                                      
	      //Get a View that displays the data at the specified position in the data set.
	      //获取一个在数据集中指定索引的视图来显示数据
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ViewHolder holder = null;
//	        Log.d(TAG,"getView position:"+position);
	        //如果缓存convertView为空，则需要创建View
	        if( mDatas.size() == 0 ){
	        	return null;
	        }
	        if(convertView == null)
	        {
	            holder = new ViewHolder();
	            //根据自定义的Item布局加载布局
	            convertView = mInflater.inflate(R.layout.item_child, null);
	            holder.mLabel = (TextView)convertView.findViewById(R.id.tv);
	            holder.mSecondLabel = (TextView)convertView.findViewById(R.id.tv1);
	            holder.mThirdLabel = (TextView)convertView.findViewById(R.id.tv2);
	            
	            holder.mImageView = (ImageView)convertView.findViewById(R.id.profileIv);
	            
//	            holder.mDeleteText = (TextView) convertView.findViewById(R.id.txtv_delete);
	              
	            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
	            convertView.setTag(holder);
	        }else{
	            holder = (ViewHolder)convertView.getTag();
	        }
	        final int pos = position;
//	        holder.mDeleteText.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                	mListview.turnNormal();
//                	deleteChild(pos);
//                }
//            });
	      
//        	Log.d(TAG,"getView position:"+position);
	        StudentBean bean = mDatas.get(position);
          	holder.mLabel.setText(bean.school);
          	holder.mSecondLabel.setText(bean.grade+bean.staffGroup);
   	        holder.mThirdLabel.setText(bean.empName);
   	        holder.mImageView.setImageResource(R.drawable.default_student);
   	        final ImageView imageview = holder.mImageView;
   	     	ImageUtils.downloadAndSaveProfilePhoto(ChildListActivity.this, bean.imgUrl
 				,String.valueOf(bean.sid),bean.imageLastUpdateTime,
 				new ImageUtils.loadCallBack(){

 					@Override
 					public void loadCb(File file,String portrait) {
 						// TODO Auto-generated method stub
 						Uri uri = Uri.fromFile(file);
 						if( uri != null ){
 							Log.d(TAG,"file:"+file.getAbsolutePath());
 							imageview.setImageURI(uri);
 						}
 					}					
 		});
			
	        return convertView;
	    }                                                     
    }
}
