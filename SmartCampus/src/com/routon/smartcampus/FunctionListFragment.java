package com.routon.smartcampus;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.routon.common.BaseFragment;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.classinfo.ClassInfoListActivity;
import com.routon.inforelease.json.AuthenobjBean;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.widget.CircleImageView;
import com.routon.edurelease.R;
import com.routon.smartcampus.answerrelease.AnswerMainActivity;
import com.routon.smartcampus.attendance.AttenceActivity;
import com.routon.smartcampus.attendance.StudentAttendanceActivity;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.coursetable.ClassCourseActivity;
import com.routon.smartcampus.family.ColligateOpinionActivity;
import com.routon.smartcampus.flower.NewStudentBadgeActivity;
import com.routon.smartcampus.gradetrack.SubjectExamActivity;
import com.routon.smartcampus.guestbook.GuestbookActivity;
import com.routon.smartcampus.guide.GuideActivity;
import com.routon.smartcampus.guide.GuideHelper;
import com.routon.smartcampus.homework.FamilyHomeworkActivity;
import com.routon.smartcampus.homework.HomeworkActivity;
import com.routon.smartcampus.leave.FamilyLeaveActivity;
import com.routon.smartcampus.leave.LeaveActivity;
import com.routon.smartcampus.leave.StudentLeaveActivity;
import com.routon.smartcampus.notify.FamilyNotifyListActivity;
import com.routon.smartcampus.notify.TeacherNotifyListActivity;
import com.routon.smartcampus.schoolcompare.SchoolCompareActivity;
import com.routon.smartcampus.selectcourse.SelectCourseActivity;
import com.routon.smartcampus.student.StudentListActivity;
import com.routon.smartcampus.studentcard.CardManageActivity;
import com.routon.smartcampus.swtchCtrl.SwtchCtrlMainActivity;
import com.routon.smartcampus.user.UserInfoData;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.DragSortListView;
import com.routon.smartcampus.view.SlidingMenu;

public class FunctionListFragment extends BaseFragment {
	private static final String TAG = FunctionListFragment.class.getSimpleName();
	private int mSelIndex = -1;
	private UserInfoData mUserInfoData = null;
	private CircleImageView mNextButton = null;;
	
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_family_function, container, false);
	}
	
	private SlidingMenu mMenu ; 
	
	public void setSlidingMenu(SlidingMenu menu){
		mMenu = menu;
	}
	
	private void loadProfile(StudentBean student){
		Log.d(TAG,"loadProfile student.imageLastUpdateTime:"+student.imageLastUpdateTime);
		//设置默认图片
		mNextButton.setImageResource(R.drawable.default_student);
		ImageUtils.downloadAndSaveProfilePhoto(getOwnActivity(), student.imgUrl
				,String.valueOf(student.sid),student.imageLastUpdateTime,
				new ImageUtils.loadCallBack(){

					@Override
					public void loadCb(File file,String portrait) {
						// TODO Auto-generated method stub
						if( getContext() == null ){
							return;
						}
						StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
						Log.d(TAG,"loadProfile file:"+file.getAbsolutePath());
						if( bean != null && String.valueOf(bean.sid).equals(portrait) && file.exists() ) {
							mNextButton.setImageURI(Uri.fromFile(file));
						}
					}					
		});
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		if( SmartCampusApplication.mFamilyVersion == true ){
			mSelIndex = SmartCampusApplication.getSelIndex(mUserInfoData);
			StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
			//保存新数据并下载图片
			mUserInfoData.setParentPortrait(String.valueOf(bean.sid));
			initTitleBar(bean.empName+"家长");
			
			mNextButton.setVisibility(View.VISIBLE);
			loadProfile(bean);
			mNextButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mSelIndex++;
						if(  mSelIndex >= SmartCampusApplication.mStudentDatas.size()){
							mSelIndex = 0;
						}
						StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
						//保存新数据并下载图片
						mUserInfoData.setParentPortrait(String.valueOf(bean.sid));
						loadProfile(bean);				
						initTitleBar(bean.empName+"家长");
						initSetttingItems();
					}
				});
			initSetttingItems();
		} 
	}
	
	
	
	ArrayList<Integer> mOrders = new ArrayList<Integer>();
	ArrayList<Integer> mHideMenus = new ArrayList<Integer>();
	ArrayList<Integer> mAllMenus = new ArrayList<Integer>();
	public void getInitOrders(){
		String mainlistorder = new UserInfoData(this.getContext()).getMainListOrder();
		String hidemenus = new UserInfoData(this.getContext()).getMainListHideMenus();
		MenuType.getOrders(mainlistorder,mOrders);
		MenuType.getOrders(hidemenus, mHideMenus);
		for( int i = 0; i < mOrders.size();i++){
			Log.d(TAG,"getOrders mInitOrders:"+mOrders.get(i));
		}
		for( int i = 0; i < mHideMenus.size();i++){
			Log.d(TAG,"getOrders mInitHideMenus:"+mHideMenus.get(i));
		}
	}
	
	private void saveOrders(){
		new UserInfoData(this.getContext()).saveMainListOrder(MenuType.formatMenuListOrder(mOrders));
		new UserInfoData(this.getContext()).saveMainListHideMenus(MenuType.formatMenuListOrder(mHideMenus));
	}
	
	public boolean menusContainsMenu(ArrayList<Integer> menus,int menu){
		for( int i = 0; i < menus.size(); i++ ){
			if( menus.get(i) == menu ){
				return true;
			}
		}
		return false;
	}
	
	public void recorrectOrders(){
		//init all menus
		mAllMenus.clear();
		if( InfoReleaseApplication.authenobjData.headTeacherClasses != null && InfoReleaseApplication.authenobjData.headTeacherClasses.length > 0 ){
			mAllMenus.add(MenuType.MENU_SCHOOL_DYNAMIC);	
		}else{
			String userName = InfoReleaseApplication.authenobjData.userName;
			//特殊处理，以C,S,G用户名可以查看班级动态，发布班牌			
			if( userName != null && (userName.startsWith("C") || userName.startsWith("G")|| userName.startsWith("S"))){
				mAllMenus.add(MenuType.MENU_SCHOOL_DYNAMIC);
			}
		}
		if( InfoReleaseApplication.authenobjData.audit_classinfo_privilege == 1 || InfoReleaseApplication.authenobjData.audit_schoolnotice_privilege == 1 ){
			mAllMenus.add(MenuType.MENU_AUDIT);
		}
		mAllMenus.add(MenuType.MENU_FLOWER);		
		mAllMenus.add(MenuType.MENU_HOMEWORK);	
		mAllMenus.add(MenuType.MENU_COURSE);	
		mAllMenus.add(MenuType.MENU_GRADETRACK);	
		mAllMenus.add(MenuType.MENU_COLLIGATE_OPINION);	
		mAllMenus.add(MenuType.MENU_SCHOOL_COMPARE);
		mAllMenus.add(MenuType.MENU_ANSWER);	
		mAllMenus.add(MenuType.MENU_ATTENDANCE);		
		//是否有换课权限
        if( InfoReleaseApplication.authenobjData.timetable_privilege == 1){
        	mAllMenus.add(MenuType.MENU_LEAVE);
        }else {
        	mAllMenus.add(MenuType.MENU_TEACHER_LEAVE);
		}
        //是否是班主任
        if( InfoReleaseApplication.authenobjData.headTeacherClasses != null && InfoReleaseApplication.authenobjData.headTeacherClasses.length > 0 ){
        	mAllMenus.add(MenuType.MENU_STUDENT_LEAVE);
        }
        mAllMenus.add(MenuType.MENU_NOTIFY);
        
        //老师版校园卡管理用于给学生卡升级，不用判断权限　modified by 20180821
//		if(  InfoReleaseApplication.authenobjData.ctrlId != null &&  InfoReleaseApplication.authenobjData.ctrlId.isEmpty() == false ){
			mAllMenus.add(MenuType.MENU_CARDMANAGER);
//		}	
		mAllMenus.add(MenuType.MENU_SWTCH_CTRL);
		
		for( int i = 0; i < mAllMenus.size();i++){
			int menu = mAllMenus.get(i);
			//排序好的菜单是否包含menu
			boolean ordersContainsMenu = menusContainsMenu(mOrders,menu);
			//隐藏的菜单是否包含menu
			boolean hideMenusContainsMenu = menusContainsMenu(mHideMenus,menu);
			Log.d(TAG,"recorrectOrders menu:"+menu+",ordersContainsMenu:"+ordersContainsMenu+",hideMenusContainsMenu:"+hideMenusContainsMenu
					+",menu:"+menu);
			if( ordersContainsMenu == false && hideMenusContainsMenu == false ){//隐藏菜单中不包含这个menu,显示菜单中也不包含这个menu,则添加到显示菜单中
				mOrders.add(menu);
			}
		}
		
		for( int i = 0; i < mOrders.size();i++){
			int menu = mOrders.get(i);
			boolean allMenusContainsMenu = menusContainsMenu(mAllMenus,menu);
			if( allMenusContainsMenu == false ){//显示菜单中包含的菜单项已经不再要求显示，则去除这一项
				mOrders.remove(i);
				i--;
			}
		}
		
		for( int i = 0; i < mHideMenus.size();i++){
			int menu = mHideMenus.get(i);
			boolean allMenusContainsMenu = menusContainsMenu(mAllMenus,menu);
			if( allMenusContainsMenu == false ){//隐藏菜单中包含的菜单项已经不再要求显示，则去除这一项
				mHideMenus.remove(i);
				i--;
			}
		}
	}
	
	private Button mRightBtn = null;
	public void initSetttingItems(){
		mOrders.clear();
		if(SmartCampusApplication.mFamilyVersion == true ){
			mOrders.add(MenuType.MENU_SCHOOL_DYNAMIC);
			mOrders.add(MenuType.MENU_FLOWER);
			mOrders.add(MenuType.MENU_HOMEWORK);
			mOrders.add(MenuType.MENU_COURSE);
			mOrders.add(MenuType.MENU_GRADETRACK);
			mOrders.add(MenuType.MENU_COLLIGATE_OPINION);
			mOrders.add(MenuType.MENU_ATTENDANCE);
			mOrders.add(MenuType.MENU_GUESTBOOK);
			mOrders.add(MenuType.MENU_STUDENT_ADD_LEAVE);
			mOrders.add(MenuType.MENU_NOTIFY);
//            mOrders.add(MenuType.MENU_SELECT_COURSE);
			mSelIndex = SmartCampusApplication.getSelIndex(mUserInfoData);
			StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
			if( bean.ctrlId != null && bean.ctrlId.isEmpty() == false ){
				mOrders.add(MenuType.MENU_CARDMANAGER);
			}
			mNextButton.setVisibility(View.VISIBLE);
		}else{	
			getInitOrders();
			recorrectOrders();		
			mNextButton.setVisibility(View.GONE);
		}
		initContentView();
	}
	
	@Override  
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);	
		
		mUserInfoData = new UserInfoData(getOwnActivity());
		
		if( SmartCampusApplication.mFamilyVersion == false && InfoReleaseApplication.authenobjData != null ){
			String[] schools = InfoReleaseApplication.authenobjData.schools;
			if( schools != null && schools.length > 0 ){
				if( schools.length == 1 ){
					this.initTitleBar(schools[0]);
				}else{
					this.initTitleBar(schools[0]+"等");
				}
			}
		}

		RelativeLayout titlebarLayout=(RelativeLayout) getView().findViewById(R.id.titlebar);
		titlebarLayout.setBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
		
		mRightBtn = (Button) getView().findViewById(R.id.right_btn);
		if( SmartCampusApplication.mFamilyVersion == false ){//老师版才有主界面菜单编辑功能		
			mRightBtn.setVisibility(View.VISIBLE);
			mRightBtn.setText("编辑");
		}else{
			mRightBtn.setVisibility(View.INVISIBLE);
		}
		
		ImageButton	backBtn = (ImageButton) getView().findViewById(R.id.back_btn);
		backBtn.setImageResource(R.drawable.detail);
		if(backBtn != null){
			backBtn.setVisibility(View.VISIBLE);
			backBtn.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if( mMenu != null ){
						mMenu.toggle();
					}
				}
				
			});
		}
		mNextButton = (CircleImageView ) getView().findViewById(R.id.next_step);
		
		initSetttingItems();		
	}
	
	private int mMenuIndex = 0;
	
	private boolean mEditableState = false;
	
	private void updateListViewHeight(){
		DragSortListView listview = (DragSortListView) this.getView().findViewById(R.id.content_listview);
		ViewGroup.LayoutParams params = listview.getLayoutParams();
		params.height = (int) (this.getContext().getResources().getDimension(R.dimen.setting_item_height)* (mOrders.size())
				    + (listview.getDividerHeight() * (mOrders.size() - 1)));
		listview.setLayoutParams(params);
		DragSortListView hidemenuListView = (DragSortListView) this.getView().findViewById(R.id.hidemenu_listview);
		ViewGroup.LayoutParams params1 = hidemenuListView.getLayoutParams();
		params1.height = (int) (this.getContext().getResources().getDimension(R.dimen.setting_item_height)* (mHideMenus.size())
				    + (listview.getDividerHeight() * (mHideMenus.size() - 1)));
		hidemenuListView.setLayoutParams(params1);
	}
	
	private void initContentView(){
		for( int i = 0; i < mOrders.size();i++){
			Log.d(TAG,"initContentView mOrders:"+mOrders.get(i));
		}
		for( int i = 0; i < mHideMenus.size();i++){
			Log.d(TAG,"initContentView mHideMenus:"+mHideMenus.get(i));
		}
		final DragSortListView listview = (DragSortListView) this.getView().findViewById(R.id.content_listview);
		listview.setDragEnabled(false);
		
		final DragSortListView hidemenuListView = (DragSortListView) this.getView().findViewById(R.id.hidemenu_listview);
		hidemenuListView.setVisibility(View.GONE);
		hidemenuListView.setDragEnabled(false);
		hidemenuListView.setBackgroundColor(this.getContext().getResources().getColor(R.color.lightgray));
		final MenuAdapter adapter = new MenuAdapter(getContext(),mOrders,false);
		listview.setAdapter(adapter);
		final MenuAdapter hideMenuAdapter = new MenuAdapter(getContext(),mHideMenus,true);	
		hidemenuListView.setAdapter(hideMenuAdapter);
		hideMenuAdapter.setEditable(true);
		updateListViewHeight();
		adapter.setBtnClickListener(new MenuAdapter.BtnClickListener() {
			
			@Override
			public void onClick(int pos, boolean hidemenu) {
				// TODO Auto-generated method stub
				int item = mOrders.get(pos);
				mOrders.remove(pos);
				mHideMenus.add(item);
				updateListViewHeight();
				adapter.notifyDataSetChanged();
				hideMenuAdapter.notifyDataSetChanged();
			}
		});
		
		hideMenuAdapter.setBtnClickListener(new MenuAdapter.BtnClickListener() {
			
			@Override
			public void onClick(int pos, boolean hidemenu) {
				// TODO Auto-generated method stub
				int item = mHideMenus.get(pos);
				mHideMenus.remove(pos);
				mOrders.add(item);
				updateListViewHeight();
				adapter.notifyDataSetChanged();
				hideMenuAdapter.notifyDataSetChanged();
			}
		});
		
		// 监听器在手机拖动停下的时候触发
		DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
			@Override
			public void drop(int from, int to) {// from to 分别表示 被拖动控件原位置 和目标位置
				if (from != to) {
					int column = (int) adapter.getItem(from);// 得到listview的适配器
					mOrders.remove(from);
					mOrders.add(to, column);// 在目标位置中插入被拖动的控件。
					adapter.notifyDataSetChanged();
				}
			}
		};
		listview.setDropListener(onDrop);
				
		mRightBtn = (Button) getView().findViewById(R.id.right_btn);
		if( mRightBtn != null ){
			mRightBtn.setClickable(true);
			mRightBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					mEditableState = !mEditableState;
					if( mEditableState == false ){
						listview.setDragEnabled(false);
						hidemenuListView.setVisibility(View.GONE);
						mRightBtn.setText("编辑");
					}else{
						listview.setDragEnabled(true);
						hidemenuListView.setVisibility(View.VISIBLE);
						mRightBtn.setText("完成");
					}
					adapter.setEditable(!(adapter.getEditable()));
					if( adapter.getEditable() == false ){
						saveOrders();
					}else{
					}
					adapter.notifyDataSetChanged();
				}
			});
		}
		
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if( adapter.getEditable() == true ){
					return;
				}
				
				int menuIndex = mOrders.get(position);
				mMenuIndex = menuIndex;
				if( menuIndex == MenuType.MENU_FLOWER){
					startFlowerActivity();
				}else if( menuIndex == MenuType.MENU_COURSE){			
					Intent intent = new Intent();
					if( SmartCampusApplication.mFamilyVersion == true ){
						StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
						intent.putExtra(MyBundleName.STUDENT_BEAN, bean);
						intent.setClass(getOwnActivity(), ClassCourseActivity.class);
					}else{
						intent.putExtra("AppType", "TeacherCourseTable");
						intent.setClass(getOwnActivity(), ClassCourseActivity.class);
					}
					startGuideActivity(intent,null);			
				}else if( menuIndex == MenuType.MENU_ATTENDANCE ){ 
					if( SmartCampusApplication.mFamilyVersion == false ){
						Intent intent = new Intent();
						intent.setClass(getOwnActivity(), AttenceActivity.class);					
						intent.putExtra(MyBundleName.STUDENT_APP_TYPE, MyBundleName.TYPE_STUDENT_ATTENDANCE);
						startGuideActivity(intent,null);	
					}else{
						 Intent intent=new Intent(getContext(),
								 StudentAttendanceActivity.class);
						
						 StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
						 startGuideActivity(intent,null,MyBundleName.STUDENT_BEAN,bean);	
					}
				}else if( menuIndex == MenuType.MENU_AUDIT ){
					startEduAuditActivity();
				}else if( menuIndex == MenuType.MENU_HOMEWORK ){
					if( SmartCampusApplication.mFamilyVersion == false ){
						startHomeWorkActivity();
					}else{
						Intent intent=new Intent(getContext(),FamilyHomeworkActivity.class);
						StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
						startGuideActivity(intent,null,MyBundleName.STUDENT_BEAN,bean);	
					}
				}else if( menuIndex == MenuType.MENU_CARDMANAGER ){//校园卡管理
					if( SmartCampusApplication.mFamilyVersion == true ){
						Intent intent = new Intent();
						intent.setClass(getOwnActivity(), CardManageActivity.class);
						StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
						intent.putExtra(MyBundleName.STUDENT_CARD_ID, bean.sid);
						intent.putExtra(MyBundleName.STUDENT_CARD_NAME, bean.empName);
						intent.putExtra(MyBundleName.STUDENT_CARD_CLASS, bean.grade+bean.staffGroup);
						intent.putExtra(MyBundleName.STUDENT_CARD_IMAGE_URL, bean.imgUrl);
						intent.putExtra(MyBundleName.STUDENT_CARD_LOCAL_IMAGE_PATH, bean.imgSavePath);
						intent.putExtra(MyBundleName.STUDENT_CARD_MAC_ADDRESS, bean.ctrlId);
						startGuideActivity(intent,null);		
					}else{
						Intent intent = new Intent();
						intent.setClass(getOwnActivity(), CardManageActivity.class);
						AuthenobjBean bean = InfoReleaseApplication.authenobjData;
						intent.putExtra(MyBundleName.STUDENT_CARD_NAME, "");
						intent.putExtra(MyBundleName.STUDENT_CARD_CLASS, bean.schools[0]);
//						intent.putExtra(MyBundleName.STUDENT_CARD_IMAGE_URL, bean.portraitUrl);
						//老师版可以给所有卡升级，不需要去匹配mac地址
//						intent.putExtra(MyBundleName.STUDENT_CARD_MAC_ADDRESS, bean.ctrlId);
						startGuideActivity(intent,null);	
					}
				}
				else if( menuIndex == MenuType.MENU_GRADETRACK )
				{
					if( SmartCampusApplication.mFamilyVersion == false ){
						Intent intent = new Intent();
						intent.setClass(getOwnActivity(), SubjectExamActivity.class);
						intent.putExtra(MyBundleName.TYPE, SubjectExamActivity.SCHOOL_TYPE);
						startGuideActivity(intent);		
					}else{
						Intent intent = new Intent(getActivity(), SubjectExamActivity.class);
						StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
						intent.putExtra(MyBundleName.TYPE, SubjectExamActivity.FAMILY_TYPE);
						startGuideActivity(intent,null,MyBundleName.STUDENT_BEAN,bean);	
					}
				}else if( menuIndex == MenuType.MENU_SCHOOL_DYNAMIC ){
					if( SmartCampusApplication.mFamilyVersion == true ){
						Intent intent = new Intent(getActivity(), ClassInfoListActivity.class);
						intent.putExtra("id", "-1");
						StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
						//Log.i(TAG, "---------groupIDs:"+String.valueOf(bean.groupId));
						intent.putExtra("groupIDs", String.valueOf(bean.groupId));
						startGuideActivity(intent,null);	
					}else{
						startEduActivity();
					}
				}else if( menuIndex == MenuType.MENU_COLLIGATE_OPINION  ){				
					if( SmartCampusApplication.mFamilyVersion == false ){
						Intent intent = new Intent();
						intent.setClass(getOwnActivity(), StudentListActivity.class);
						intent.putExtra(MyBundleName.STUDENT_APP_TYPE, MyBundleName.TYPE_STUDENT_OPINION);
						startGuideActivity(intent,null);		
					}else{
						Intent intent = new Intent(getActivity(), ColligateOpinionActivity.class);
						StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
						startGuideActivity(intent,null,MyBundleName.STUDENT_BEAN,bean);	
					}
					
				}else if( menuIndex == MenuType.MENU_SCHOOL_COMPARE ){//校务评比
					Intent intent = new Intent(getActivity(), SchoolCompareActivity.class);
					startGuideActivity(intent);	
				}else if( menuIndex == MenuType.MENU_GUESTBOOK ){//留言薄
					Intent intent = new Intent(getActivity(), GuestbookActivity.class);
					StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);	
					startGuideActivity(intent,null,MyBundleName.STUDENT_BEAN,bean);	
				}else if( menuIndex == MenuType.MENU_ANSWER ){//答题
					Intent intent=new Intent(getActivity(),AnswerMainActivity.class);
					startGuideActivity(intent);	
				}else if( menuIndex == MenuType.MENU_SWTCH_CTRL ){//智能开关
					Intent intent=new Intent(getActivity(),SwtchCtrlMainActivity.class);
					startGuideActivity(intent);	
				}else if( menuIndex == MenuType.MENU_LEAVE ){//请假
					if( SmartCampusApplication.mFamilyVersion == false ){
						Intent intent=new Intent(getActivity(),LeaveActivity.class);
						startGuideActivity(intent);	
					}
				}else if(menuIndex == MenuType.MENU_STUDENT_ADD_LEAVE){//学生请假
					Intent intent = new Intent(getActivity(), FamilyLeaveActivity.class);
					StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);	
					startGuideActivity(intent,null,MyBundleName.STUDENT_BEAN,bean);	
				}else if(menuIndex == MenuType.MENU_TEACHER_LEAVE){
					Intent intent=new Intent(getActivity(),LeaveActivity.class);
					startGuideActivity(intent);	
				}else if(menuIndex == MenuType.MENU_STUDENT_LEAVE){
					Intent intent=new Intent(getActivity(), StudentLeaveActivity.class);
					startGuideActivity(intent);	
				}else if(menuIndex == MenuType.MENU_SELECT_COURSE){
					Intent intent = new Intent(getActivity(),SelectCourseActivity.class);
					StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
					startGuideActivity(intent,null,MyBundleName.STUDENT_BEAN,bean);	
				}else if(menuIndex == MenuType.MENU_NOTIFY){//通知
					if( SmartCampusApplication.mFamilyVersion == false ){
                        Intent intent = new Intent(getActivity(), TeacherNotifyListActivity.class);
                        startGuideActivity(intent);	
					} else {
                        Intent intent = new Intent(getActivity(), FamilyNotifyListActivity.class);
                        startGuideActivity(intent);	
					}
				}
			}
		});
	}		
	
	void startEduAuditActivity(){
		InfoReleaseApplication.isEduPlatform = true;
		Intent intent = new Intent();
		intent.putExtra(CommonBundleName.SHOW_SETTING_FRAGMENT, false);
		intent.putExtra(CommonBundleName.SHOW_EXIT_FRAGMENT, true);
		if( InfoReleaseApplication.authenobjData.audit_classinfo_privilege == 1 ){
			intent.putExtra(CommonBundleName.AuditClassInfoAuthority,true);
		}
		if( InfoReleaseApplication.authenobjData.audit_schoolnotice_privilege == 1 ){
			intent.putExtra(CommonBundleName.AuditSchoolNoticeAuthority,true);
		}
		intent.setComponent(new ComponentName(getOwnActivity().getPackageName(),"com.routon.inforelease.MainActivity"));
		startGuideActivity(intent);	
	}
	
	void startEduActivity(){
		InfoReleaseApplication.isEduPlatform = true;
		Intent intent = new Intent();
		intent.putExtra(CommonBundleName.SHOW_SETTING_FRAGMENT, false);
		intent.putExtra(CommonBundleName.SHOW_EXIT_FRAGMENT, true);
		intent.setComponent(new ComponentName(getOwnActivity().getPackageName(),"com.routon.inforelease.MainActivity"));
		startGuideActivity(intent);	
	}
	
	void startHomeWorkActivity(){
		Intent intent=new Intent(this.getActivity(),HomeworkActivity.class);
		this.startGuideActivity(intent);
	}
	
	void startGuideActivity(Intent intent,Bundle bundle){
		startGuideActivity(intent,bundle,null,null);
	}
	
	void startGuideActivity(Intent intent){
		startGuideActivity(intent,null,null,null);
	}
	
	void startGuideActivity(Intent intent,Bundle bundle,String serialStr,Serializable serial){
		int role = GuideHelper.TEACHER_ROLE;
		if( SmartCampusApplication.mFamilyVersion == true ){
			role = GuideHelper.PARENT_ROLE;
		}
		String[] validImages = GuideHelper.getValidMenuImages(getContext(), GuideHelper.getImagesFromAssetFile(this.getContext()),mMenuIndex,role);
		if(validImages == null || validImages.length == 0 ){//不需要引导页
			if( bundle != null){
				if( bundle != null ){		
					intent.putExtras(bundle);
				}
			}
			if( serialStr != null && serial != null ){
				intent.putExtra(serialStr,serial);
			}
			startActivity(intent);
		}else{//需要引导页
			Intent guideIntent = new Intent();
			guideIntent.setClass(this.getOwnActivity(), GuideActivity.class);
			guideIntent.putExtra(GuideActivity.INTENT_URI_TAG, intent.toURI());
			guideIntent.putExtra(GuideActivity.IMAGES_ARRAY_TAG, validImages);
			guideIntent.putExtra(GuideActivity.INTENT_BUNDLES_TAG, bundle);
			guideIntent.putExtra(GuideActivity.INTENT_SERIAL, serial);
			guideIntent.putExtra(GuideActivity.INTENT_SERIAL_STR, serialStr);
			startActivity(guideIntent);
		}	
	}
	
	void startFlowerActivity(){	
		Intent intent = null;
		Bundle bundle = null;
		if( SmartCampusApplication.mFamilyVersion == false ){
			intent = new Intent();
			intent.setClass(this.getActivity(), StudentListActivity.class);
		}else{
			 intent = new Intent(getContext(),NewStudentBadgeActivity.class);
			 bundle = new Bundle();
			 StudentBean bean = SmartCampusApplication.mStudentDatas.get(mSelIndex);
			 
			 bundle.putString(MyBundleName.STUDENT_IMG_URL, 
					 ImageUtils.getProfilePhoto(getOwnActivity(),String.valueOf(bean.sid),bean.imageLastUpdateTime).getAbsolutePath());
			 bundle.putSerializable(MyBundleName.STUDENT_BEAN, bean);
			 intent.putExtras(bundle);		 
		}
		startGuideActivity(intent,bundle);
	}
	
	
}
