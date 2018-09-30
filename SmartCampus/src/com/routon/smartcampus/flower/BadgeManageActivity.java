package com.routon.smartcampus.flower;

import java.util.ArrayList;
import java.util.List;

import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.smartcampus.flower.RemarkEditPopWin.OnFinishListener;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.DragSortListView;
import com.routon.widgets.Toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

public class BadgeManageActivity extends CustomTitleActivity {
	private ArrayList<BadgeInfo> badgeList = new ArrayList<BadgeInfo>();
	private DragSortListView listView;
	private OftenBadgeAdapter oftenBadgeAdapter;
	private LayoutParams params;
	private RemarkEditPopWin popWin;
	public static final int ADD_CUSTOM_FLOWER_REQUEST_CODE = 1;
	private int mBadgeTitleId=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_badge_manage);

		initView();
		initData();
	}

	private void initView() {
		initTitleBar("常用小红花管理");
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
		setTitleNextBtnClickListener("完成", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!dataIsChange) {
					finish();
				} else {
					uploadFile();
				}
			}
		});

		setTitleBackBtnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!dataIsChange) {
					finish();
				} else {
					showDialog();
				}
			}
		});
		setMoveBackEnable(true);

		listView = (DragSortListView) findViewById(R.id.badge_often_lv);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 编辑原有小红花
				showPopWin(badgeList.get(position), position);
			}
		});
		
		// 监听器在手机拖动停下的时候触发
		DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
			@Override
			public void drop(int from, int to) {// from to 分别表示 被拖动控件原位置 和目标位置
				if (from != to) {
					dataIsChange = true;
					BadgeInfo item = badgeList.remove(from);
					badgeList.add(to, item);
					oftenBadgeAdapter.notifyDataSetChanged();
				}
			}
		};
		listView.setDropListener(onDrop);
		
		View footerView = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.add_custom_flower_item, null, false);
		listView.addFooterView(footerView);
		footerView.setEnabled(true);
		footerView.setOnClickListener(new View.OnClickListener(){// 添加常用小红花
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if( badgeList.size() >= BadgeInfoUtil.MAX_CUSTOM_FLOWERS ){
					Toast.makeText(BadgeManageActivity.this, "常用小红花数目不能超过"+BadgeInfoUtil.MAX_CUSTOM_FLOWERS+"个", Toast.LENGTH_LONG).show();
					return;
				}
				Intent intent = new Intent(BadgeManageActivity.this, BadgeAddSelActivity.class);
				intent.putExtra(MyBundleName.BADGE_INFO_LIST, badgeList);
				startActivityForResult(intent, ADD_CUSTOM_FLOWER_REQUEST_CODE);
			}
			
		});
		
		
		
		/*popView = getLayoutInflater().inflate(R.layout.badge_title_list_layout, null);
		
		badgeTitleLv = (ListView) popView.findViewById(R.id.badge_title_lv);
		
		
		
		
		mPopupWindow = new PopupWindow(popView, dip2px(120), dip2px(120));
		mPopupWindow.setOutsideTouchable(true);*/
	}
	
	private void showDeleteConfirmDialog(final int pos){
		//创建AlertDialog的构造器的对象
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("确认删除这条常见小红花");
        //为构造器设置确定按钮,第一个参数为按钮显示的文本信息，第二个参数为点击后的监听事件，用匿名内部类实现
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
				dataIsChange = true;
				badgeList.remove(pos);
				oftenBadgeAdapter.notifyDataSetChanged();
            }
        });
        //为构造器设置取消按钮,若点击按钮后不需要做任何操作则直接为第二个参数赋值null
        builder.setNegativeButton("取消",null);

        //利用构造器创建AlertDialog的对象,实现实例化
        builder.create().show();
	}
	private boolean dataIsChange;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			//添加常用小红花返回
			if ( requestCode == ADD_CUSTOM_FLOWER_REQUEST_CODE ) {
				dataIsChange = true;
				OftenBadgeBean badgeInfo = (OftenBadgeBean) data.getSerializableExtra("badge_info");
				badgeList.add(0,badgeInfo);
				oftenBadgeAdapter.notifyDataSetChanged();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	private void showDialog() {
		final AlertDialog.Builder normalDialog = new AlertDialog.Builder(BadgeManageActivity.this);

		normalDialog.setMessage("常用小红花有变动，是否进行保存");
		normalDialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				uploadFile();
			}
		});
		normalDialog.setNegativeButton("不保存", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		normalDialog.show();
	}
	
	ProgressDialog progressDialog;
	/*private View popView;
	private PopupWindow mPopupWindow;
	private List<String> badgeTitles= new ArrayList<String>();
	private ListView badgeTitleLv;
	private List<BadgeRemarkBean> badgeRemarkList= new ArrayList<BadgeRemarkBean>();*/
    private void showLoadDialog(){
		if (progressDialog == null) {
			progressDialog = ProgressDialog.show(BadgeManageActivity.this, "", "...loading...");
			progressDialog.show();
		}else {
			progressDialog.show();
		}
	}
	
	private void hideLoadDialog(){		
		if (progressDialog != null) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			
		}
	}

	private void uploadFile() {// 上传新生成的json文件
		showLoadDialog();
		//更新badgeList数据
		BadgeInfoUtil.setCustomFlowers(badgeList);
		BadgeInfoUtil.uploadFile(new BadgeInfoUtil.UploadFileListener() {
			
			@Override
			public void uploadFile(boolean success) {
				// TODO Auto-generated method stub
				hideLoadDialog();
				if (success) {
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});
	}

	private void showPopWin(final BadgeInfo bean, final int position) {

		final OftenBadgeBean beanTag=new OftenBadgeBean();
		beanTag.badgeTitle=bean.badgeTitle;
		beanTag.badgeRemark=bean.badgeRemark;
		beanTag.bonuspoint=bean.bonuspoint;
		
		popWin = new RemarkEditPopWin(BadgeManageActivity.this, bean,badgeList,position, new OnFinishListener() {

			@Override
			public void onFinishClick(OftenBadgeBean b) {

				
				popWin.dismiss();
				
				if (!beanTag.badgeTitle.equals(b.badgeTitle) || !beanTag.badgeRemark.equals(b.badgeRemark) || beanTag.bonuspoint != b.bonuspoint) {
					dataIsChange=true;
				}
				/*if (mBadgeTitleId!=0) {
					b.badgeTitleId=mBadgeTitleId;
				}*/
				
				badgeList.set(position, b);
				oftenBadgeAdapter.notifyDataSetChanged();

			}
		});
		popWin.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		params = getWindow().getAttributes();
		params.alpha = 0.7f;
		getWindow().setAttributes(params);
		popWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				params = getWindow().getAttributes();
				params.alpha = 1f;
				getWindow().setAttributes(params);

			}
		});
		
		/*badgeTitles.clear();
		List<Badge> badges=BadgeInfoUtil.getFlowerList();
		badgeRemarkList.clear();
		if (badges !=null) {
			for (int i = 0; i < badges.size(); i++) {
				if (badges.get(i).id==bean.badgeId) {
					if (badges.get(i).badgeRemarkList!=null) {
						badgeRemarkList= badges.get(i).badgeRemarkList;
						for (int j = 0; j < badges.get(i).badgeRemarkList.size(); j++) {
							badgeTitles.add(badges.get(i).badgeRemarkList.get(j).badgeTitle);
						}
					}
					
				}
			}
		}
		
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, R.layout.badge_title_item, R.id.badge_title_tv,badgeTitles);
		badgeTitleLv.setAdapter(adapter);
		
		popWin.titleEditView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				if (isSoftShowing()) {
//					closeKeyboard(v);
//				}
				
				mPopupWindow.showAsDropDown(v,0,popWin.getHeight());
			}
		});
		
		
		badgeTitleLv.setOnItemClickListener(new OnItemClickListener() {

			

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				popWin.titleEditView.setText(badgeTitles.get(position));
				mBadgeTitleId = badgeRemarkList.get(position).badgeTitleId;
				mPopupWindow.dismiss();
			}
		});*/
	}

	private void initData() {	
		badgeList.clear();
		badgeList.addAll(BadgeInfoUtil.getCustomFlowers());
		
		oftenBadgeAdapter = new OftenBadgeAdapter(BadgeManageActivity.this, badgeList, 0, true);
		oftenBadgeAdapter.setOnDelBtnClickListener(new OftenBadgeAdapter.OnDelBtnClickListener(){

			@Override
			public void onDelBtnClick(int position) {
				// TODO Auto-generated method stub
				showDeleteConfirmDialog(position);
			}
			
		});
		listView.setAdapter(oftenBadgeAdapter);
	}

	@Override
	public void onBackPressed() {

		/*if (mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		}*/
		
		if (!dataIsChange) {
			finish();
		} else {
			showDialog();
		}
	}
	
	/*private boolean isSoftShowing() {
        int screenHeight = getWindow().getDecorView().getHeight();
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
 
        return screenHeight - rect.bottom != 0;
    }
	
	private void closeKeyboard(View view) {
	    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	private int dip2px(float dpValue) {

		final float scale = getResources().getDisplayMetrics().density;

		return (int) (dpValue * scale + 0.5f);

	}*/
}
