package com.routon.inforelease.groupmanager;


import java.util.ArrayList;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.TerminalListHelper;
import com.routon.inforelease.json.TerminalListdatasBean;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class GroupTerminalActivity extends CustomTitleActivity {
	
//	private static final String TAG = "GroupTerminalActivity";
	public static final String GROUPID_TAG = "groupid";
	
	private PullToRefreshListView lv_terminal;
	private TerminalListHelper mTerminalHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_groupterminal);
		
		this.initTitleBar(R.string.terminal_list_title);
		
		this.setTitleNextImageBtnClickListener(R.drawable.ic_add, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra(TerminalListHelper.TERMINAL_IDS, mTerminalHelper.getTerminalArchiveids());
				intent.setClass(GroupTerminalActivity.this, TerminalSelActivity.class);
				startActivityForResult(intent,0);
			}
		});
		
		//下拉刷新，上拉加载
		lv_terminal = (PullToRefreshListView)findViewById(R.id.lv_terminal);
		TerminalAdapter adapter = new TerminalAdapter(this);
		lv_terminal.setAdapter(adapter);
		
		mTerminalHelper = new TerminalListHelper(lv_terminal,adapter,this);
		mTerminalHelper.setGroups(this.getIntent().getStringExtra(GROUPID_TAG));
		
		mTerminalHelper.setOnTerminalListNetWorkReply(new TerminalListHelper.onTerminalListNetWorkReply() {
			
			@Override
			public void addTerminal(boolean success) {
				// TODO Auto-generated method stub
				if( success == true ){
					mTerminalHelper.addData(mAddTerminals);				
				}
			}
		});
	    
        boolean netWorkState = InfoReleaseApplication.showNetWorkFailed(this);
	    if( netWorkState == true ){
	    	mTerminalHelper.reload();
	    }
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if( mTerminalHelper != null ){
			mTerminalHelper.cancelAllHandlerMsg();
		}
	}
	
	ArrayList<TerminalListdatasBean> mAddTerminals = null;
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
		   case RESULT_OK:
			   ArrayList<Integer> terminalIds = data.getIntegerArrayListExtra(TerminalListHelper.TERMINAL_IDS);
			   mAddTerminals = data.getParcelableArrayListExtra(TerminalListHelper.TERMINALS);
//			   Log.d(TAG,"onActivityResult terminalIds:"+terminalIds+",mAddTerminals:"+mAddTerminals);
			   if( terminalIds != null && terminalIds.size() > 0 ){
				   String terminalIdsStr = "";
				   for( int i = 0; i < terminalIds.size(); i++ ){
					   terminalIdsStr += terminalIds.get(i) + ",";
				   }
				   mTerminalHelper.addTerminal(terminalIdsStr);
			   }
			   break;
		   default:
			   break;
		    }
		}
	
	//在外面先定义，ViewHolder静态类
  	static class ViewHolder
  	{
  	    public TextView tv_id;
  	    public TextView tv_area;
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
	            convertView = mInflater.inflate(R.layout.item_group_terminal, null);
	            holder.tv_id = (TextView)convertView.findViewById(R.id.tv_terminal_id);
	            holder.tv_area = (TextView)convertView.findViewById(R.id.tv_terminal_area);
	              
	            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
	            convertView.setTag(holder);
	        }else{
	            holder = (ViewHolder)convertView.getTag();
	        }

          	TerminalListdatasBean itemData = mTerminalHelper.getTerminalData(position);
          	holder.tv_id.setText(mContext.getResources().getString(R.string.terminal_id)+":"
          			+itemData.terminalid);
   	        holder.tv_area.setText(mContext.getResources().getString(R.string.install_space)+":"
   	        		+itemData.installplace);  	    

	        return convertView;
	    }                                                     
    }
}
