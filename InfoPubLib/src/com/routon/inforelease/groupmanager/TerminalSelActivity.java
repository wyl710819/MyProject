package com.routon.inforelease.groupmanager;


import java.util.ArrayList;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.TerminalListHelper;
import com.routon.inforelease.TerminalListHelper.onTerminalListItemClickListener;
import com.routon.inforelease.json.TerminalListdatasBean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.routon.widgets.Toast;


public class TerminalSelActivity extends CustomTitleActivity {
	
//	private static final String TAG = "TerminalSelActivity";
	private ArrayList<TerminalListdatasBean> mSelList;
	
	private PullToRefreshListView lv_terminal;
	private TerminalListHelper mTerminalHelper;
	private ArrayList<Integer> mDefaultTerminalIds = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_groupterminal);
		
		this.initTitleBar(R.string.add_terminal);
		
		this.setTitleNextImageBtnClickListener(R.drawable.ok,new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//未指派终端
				if( mSelList == null || mSelList.size() == 0 ){
					Toast.makeText(TerminalSelActivity.this, R.string.not_sel_terminal, Toast.LENGTH_SHORT).show();
					return;
				}
				
				for( int i = 0; i < mSelList.size(); i++ ){
					mDefaultTerminalIds.add(mSelList.get(i).archiveid);
				}
				
				Intent intent = new Intent();
				intent.putExtra(TerminalListHelper.TERMINAL_IDS, mDefaultTerminalIds);
				intent.putParcelableArrayListExtra(TerminalListHelper.TERMINALS, mSelList);
				TerminalSelActivity.this.setResult(RESULT_OK, intent);
				
				TerminalSelActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		});
		
		mSelList = new ArrayList<TerminalListdatasBean>();
		
		//下拉刷新，上拉加载
		lv_terminal = (PullToRefreshListView)findViewById(R.id.lv_terminal);
		final TerminalAdapter adapter = new TerminalAdapter(this);
		lv_terminal.setAdapter(adapter);	
		
		mTerminalHelper = new TerminalListHelper(lv_terminal,adapter,this);
		mTerminalHelper.setOnTerminalListItemClickListener(new TerminalListHelper.onTerminalListItemClickListener() {
			
			@Override
			public void onTerminalListItemClick(int position, TerminalListdatasBean data) {
				// TODO Auto-generated method stub
				if( mSelList.contains(data)){
					mSelList.remove(data);
				}else{
					mSelList.add(data);
				}
				adapter.notifyDataSetChanged();
			}
		});
		if( mDefaultTerminalIds == null ){
			mDefaultTerminalIds = new ArrayList<Integer>();
		}
		mDefaultTerminalIds.addAll(getIntent().getIntegerArrayListExtra(TerminalListHelper.TERMINAL_IDS));
		mTerminalHelper.setUnSelTerminalIds(mDefaultTerminalIds);
	    
        boolean netWorkState = InfoReleaseApplication.showNetWorkFailed(this);
	    if( netWorkState == true ){
	    	mTerminalHelper.reload();
	    }
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if( mTerminalHelper != null ){
			mTerminalHelper.cancelAllHandlerMsg();
		}
	}
	
	//在外面先定义，ViewHolder静态类
  	static class ViewHolder
  	{
  	    public TextView tv_id;
  	    public TextView tv_area;
  	    public TextView tv_team;
  	    public ImageView iv_sel;
  	}
	
	public class TerminalAdapter extends BaseAdapter
    {   
	    private LayoutInflater mInflater = null;
	    private Context mContext = null;    	      
	    	
        public TerminalAdapter(Context context)
        {
            this.mInflater = LayoutInflater.from(context);
            mContext = context;
        }
	      
	    @Override
	    public int getCount() {
	       //How many items are in the data set represented by this Adapter.
	       //在此适配器中所代表的数据集中的条目数
	    	if( mTerminalHelper == null ) return 0;
      		return mTerminalHelper.getDataSize();
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
	        if( mTerminalHelper == null || mTerminalHelper.getDataSize() == 0 ){
	        	return null;
	        }
	        if(convertView == null)
	        {
	            holder = new ViewHolder();
	            //根据自定义的Item布局加载布局
	            convertView = mInflater.inflate(R.layout.item_terminal_sel, null);
	            holder.tv_id = (TextView)convertView.findViewById(R.id.tv_terminal_id);
	            holder.tv_team = (TextView)convertView.findViewById(R.id.tv_terminal_team);
	            holder.tv_area = (TextView)convertView.findViewById(R.id.tv_terminal_area);
	            holder.iv_sel = (ImageView)convertView.findViewById(R.id.item_select);
	            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
	            convertView.setTag(holder);            
	        }else{
	            holder = (ViewHolder)convertView.getTag();
	        }

//        	Log.d(TAG,"getView position:"+position);
          	TerminalListdatasBean itemData = mTerminalHelper.getTerminalData(position);
          	holder.tv_id.setText(mContext.getResources().getString(R.string.terminal_id)+":"
          			+itemData.terminalid);
            holder.tv_team.setText(mContext.getResources().getString(R.string.group_name)+":"
   	        		+itemData.bsgroup);
   	        holder.tv_area.setText(mContext.getResources().getString(R.string.install_space)+":"
   	        		+itemData.installplace);  
   	        
   	        if( isSelectListContains(itemData) ){
   	        	holder.iv_sel.setImageResource(R.drawable.checkbox_pressed);
   	        }else{
   	        	holder.iv_sel.setImageResource(R.drawable.checkbox_normal);
   	        }

	        return convertView;
	    }                                                     
    }
	
	private boolean isSelectListContains(TerminalListdatasBean itemData){
		int selectSize = mSelList.size();
		for(int i= 0; i<selectSize; i++){
			TerminalListdatasBean selectItem = mSelList.get(i);
			if(selectItem.terminalid.equals(itemData.terminalid)){
				return true;
			}
		}
		return false;
	}
}
