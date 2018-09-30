package com.routon.smartcampus.createcode;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.edurelease.R;
import com.routon.smartcampus.utils.MyBundleName;

public class ClassListActivity extends CustomTitleActivity{
	
	private int[] mClassIds = null;
	private String[] mClassNames = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_class_list);

		initTitleBar(R.string.class_list_title);
		
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
		
		mClassIds = this.getIntent().getIntArrayExtra(MyBundleName.CLASS_IDS);
		mClassNames = this.getIntent().getStringArrayExtra(MyBundleName.CLASS_NAMES);
		
		ListView listview = (ListView) this.findViewById(R.id.class_lv);
		listview.setAdapter(new ClassAdapter(this));
		listview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				int classId = mClassIds[position];
				Intent intent = new Intent();
				intent.setClass(ClassListActivity.this, CreateQRImageActivity.class);
				intent.putExtra(MyBundleName.CLASS_NAME, mClassNames[position]);
				intent.putExtra(MyBundleName.CLASS_ID, mClassIds[position]);
				
				startActivity(intent);
			}
			
		});
	}
	
	public class ClassAdapter extends BaseAdapter
    {   
	    private LayoutInflater mInflater = null;
	    private Context mContext = null;    	      
	    	
        public ClassAdapter(Context context)
        {
            this.mInflater = LayoutInflater.from(context);
            mContext = context;
        }
	      
	    @Override
	    public int getCount() {
	       //How many items are in the data set represented by this Adapter.
	       //在此适配器中所代表的数据集中的条目数
	    	if( mClassNames == null ) return 0;
      		return mClassNames.length;
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
	        TextView textview = null;
	        if(convertView == null)
	        {
	            //根据自定义的Item布局加载布局
	            convertView = mInflater.inflate(R.layout.item_class, null);
	            textview = (TextView)convertView.findViewById(R.id.name);
	              
	            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
	            convertView.setTag(textview);
	        }else{
	        	textview = (TextView) convertView.getTag();
	        }
	        textview.setText(mClassNames[position]);
	        return convertView;
	    }                                                     
    }

}
