package com.routon.smartcampus.attendance;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.android.volley.VolleyError;
import com.routon.edurelease.R;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.util.DataResponse;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.smartcampus.student.ClassSelListViewAdapter;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.PeriscopeLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.routon.widgets.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AttendanceStatisticsFragment extends Fragment implements OnClickListener {
	private String TAG = "AttendanceStatisticsFragment";
	private TextView classTextView;
	private FrameLayout dropdownFl;
	private View dropdownMask;
	private ListView classListView;
	private boolean isClassListShow = true;
	private ProgressDialog progressDialog;
	private ArrayList<String> mClassList;
	private ClassSelListViewAdapter mClassListAdapter;
	private ListView absenceStudentListview;
	private ArrayList<GroupInfo> mClassGroups;
	private AbsenceStudentListViewAdapter mAdapter;
	ArrayList<AttendanceBean> studentdatalist=new ArrayList<AttendanceBean>();
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_attendance_statistics, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initViews(getView());

		initData();
	}

	private void initViews(View view) {
		classTextView = (TextView) view.findViewById(R.id.tv_class);
		LinearLayout classSelView = (LinearLayout) view.findViewById(R.id.tv_class_ll);
		PeriscopeLayout periscopeLayout = (PeriscopeLayout) view.findViewById(R.id.periscope);
		dropdownFl = (FrameLayout) view.findViewById(R.id.dropdown_fl);
		classListView = (ListView) view.findViewById(R.id.dropdown_listview);
		dropdownMask = view.findViewById(R.id.dropdown_mask);
		
		absenceStudentListview = (ListView) view.findViewById(R.id.student_listview);

		dropdownMask.setOnClickListener(this);
		classSelView.setOnClickListener(this);

		classListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				listViewOutAnim();
				classTextView.setText(mClassList.get(position));
				getClassAttendaceListData(mClassGroups.get(position).getId());

			}
		});
		absenceStudentListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getContext(), StudentAttendanceActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(MyBundleName.STUDENT_APP_TYPE,MyBundleName.TYPE_ATTENDANCE);
				bundle.putString(MyBundleName.STUDENT_NAME, studentdatalist.get(position).empName);
				bundle.putInt(MyBundleName.STUDENT_ID, studentdatalist.get(position).sid);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	private void initData() {
		getClassListData();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_class_ll:
			dropdownClick();
			break;
		case R.id.dropdown_mask:
			if (!isClassListShow) {
				listViewOutAnim();
			}

			break;

		default:
			break;
		}
	}

	private void getClassAttendaceListData(Integer groupId) {
		
		ClassStudentData[] mAllStudentDataList=AttendanceApplication.mAllStudentDataList;
		ClassStudentData classStudentData = null;
		if (mAllStudentDataList==null) {
			Toast.makeText(getActivity(), "获取学生数据失败", Toast.LENGTH_SHORT).show();
			return;
		}
		for (int i = 0; i < mAllStudentDataList.length; i++) {
			if (mAllStudentDataList[i].groupId.equals(String.valueOf(groupId))) {
				classStudentData=mAllStudentDataList[i];
			}
		}
		if (classStudentData!=null) {
			studentdatalist=classStudentData.studentdatalist;
			if( studentdatalist != null ){
				Collections.sort(studentdatalist, new Comparator<AttendanceBean>(){  
					@Override
					public int compare(AttendanceBean lhs, AttendanceBean rhs) {
						 if(lhs.absenceCount > rhs.absenceCount){  
			                    return -1;  
			                }  
			                if(lhs.absenceCount == rhs.absenceCount){  
			                    return 0;  
			                }  
			                return 1;  
					}  
		        });   
				int lastAbsenceCount = -1;
				int taxis=0;
				for (int i = 0; i < studentdatalist.size(); i++) {
					if (lastAbsenceCount != studentdatalist.get(i).absenceCount) {
						taxis++;
					}
					studentdatalist.get(i).absenceTaxis=taxis;
					lastAbsenceCount=studentdatalist.get(i).absenceCount;
				}
				mAdapter = new AbsenceStudentListViewAdapter(getContext(),studentdatalist);
				absenceStudentListview.setAdapter(mAdapter);
			}else{
				absenceStudentListview.setAdapter(null);
			}
			
		}
		
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {
			if (!isClassListShow) {
				listViewOutAnim();
			}
		} else {
			// // 当前班级列表为空，重新获取班级列表
			// if (mClassList == null || mClassList.size() == 0) {
			// initData();
			// }
		}
	}

	private void dropdownClick() {
		if (isClassListShow) {
			if (mClassList != null) {
				mClassListAdapter.notifyDataSetChanged();
				listViewInAnim();
			}
		} else {
			listViewOutAnim();
		}
	}

	private void listViewInAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.VISIBLE);
		classListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
		dropdownMask.setVisibility(View.VISIBLE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_in));
		dropdownFl.setVisibility(View.VISIBLE);
		isClassListShow = false;
	}

	private void listViewOutAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.GONE);
		classListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
		dropdownMask.setVisibility(View.GONE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
		dropdownFl.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
		dropdownFl.setVisibility(View.GONE);
		isClassListShow = true;
	}

	private void getClassListData() {

		showProgressDialog();
		GroupListData.getClassListData(getActivity(), new DataResponse.Listener<ArrayList<GroupInfo>>() {

			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				// TODO Auto-generated method stub

				if (classGroups.size() > 0) {
					AttendanceStatisticsFragment.this.mClassGroups=classGroups;
					mClassList = new ArrayList<String>();
					for (int i = 0; i < classGroups.size(); i++) {
						mClassList.add(classGroups.get(i).getName());
					}

					if (mClassList.size() > 0) {
						classTextView.setText(mClassList.get(0));

					}
					mClassListAdapter = new ClassSelListViewAdapter(getContext(), mClassList);
					classListView.setAdapter(mClassListAdapter);

					getClassAttendaceListData(classGroups.get(0).getId());

				} else {
					Toast.makeText(getContext(), "班级列表为空", Toast.LENGTH_SHORT).show();
				}
				hideProgressDialog();
			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				hideProgressDialog();
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideProgressDialog();
			}
		});
	}
	
	private String getStudentImgList(Integer studentId,String gradeName,String className) {
		
		ArrayList<String> imagePathList=new ArrayList<String>();
		String imgPath = FaceRecognizeMgr.getImageDir(gradeName, className);
		File imgFile = new File(imgPath);
		File[] files = imgFile.listFiles();
		if (files!=null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];  
	            if (checkIsImageFile(file.getPath())) {  
	                imagePathList.add(file.getPath());  
	            }
			}
		}else{
			return null;
		}
		
		String imgFilePath = null;
		for (int i = 0; i < imagePathList.size(); i++) {
			String str=imagePathList.get(i);
			int id=Integer.valueOf(str.substring(str.lastIndexOf("/")+1, str.lastIndexOf("_")));
			if (id==studentId) {
				imgFilePath=imagePathList.get(i);
			}
		}
		return imgFilePath;
	}
	
	private boolean checkIsImageFile(String fName) {  
        boolean isImageFile = false;  
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,  
                fName.length()).toLowerCase();  
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")  
                || FileEnd.equals("jpeg")|| FileEnd.equals("bmp") ) {  
            isImageFile = true;  
        } else {  
            isImageFile = false;  
        }  
        return isImageFile;  
    }  

	private void showProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(getActivity(), "", "...loading...");
		}
	}

	private void hideProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
}
