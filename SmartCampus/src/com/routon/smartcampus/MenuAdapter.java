package com.routon.smartcampus;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.routon.edurelease.R;

public class MenuAdapter extends BaseAdapter{
    private LayoutInflater inflater;//这个一定要懂它的用法及作用
    ArrayList<Integer> mMenus;
    boolean mHideMenu;
    boolean mEditable = false;
    
    public interface BtnClickListener{
    	public void onClick(int pos,boolean hidemenu);
    };
   
    private BtnClickListener mBtnListener = null;
    public void setBtnClickListener(BtnClickListener listener){
    	mBtnListener = listener;
    }

    //构造函数:要理解(这里构造方法的意义非常强大,你也可以传一个数据集合的参数,可以根据需要来传参数)
    public MenuAdapter(Context context,ArrayList<Integer> menus,boolean hidemenu){
          this.inflater = LayoutInflater.from(context);
          mMenus = menus;
          mHideMenu = hidemenu;
    }
    
    public void setEditable(boolean editable){
    	mEditable = editable;
    }
    
    public boolean getEditable(){
    	return mEditable;
    }

   //这里的getCount方法是程序在加载显示到ui上时就要先读取的，这里获得的值决定了listview显示多少行
   @Override
   public int getCount() {
             //在实际应用中，此处的返回值是由从数据库中查询出来的数据的总条数
		return mMenus.size();
   }
  
   //根据ListView所在位置返回View
  @Override
   public Object getItem(int position) {
              // TODO Auto-generated method stub
	  return mMenus.get(position);
   }

    //根据ListView位置得到数据源集合中的Id
   @Override
   public long getItemId(int position) {
             // TODO Auto-generated method stub
      return position;
   }
   
   class ViewHolder{
	   ImageView image;
	   TextView name;
	   ImageView infoicon;
	   ImageView showIcon;
	   ImageView sortIcon;
   }
   
  //重写adapter最重要的就是重写此方法，此方法也是决定listview界面的样式的
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
         //有很多例子中都用到这个holder,理解下??
      ViewHolder holder = null;
      //思考这里为何要判断convertView是否为空  ？？
      if(convertView == null){
         holder = new ViewHolder();

         //把vlist layout转换成View【LayoutInflater的作用】
         convertView = inflater.inflate(R.layout.menu_adapter_item, null);
         //通过上面layout得到的view来获取里面的具体控件
         holder.image = (ImageView) convertView.findViewById(R.id.info_img);
         holder.name = (TextView) convertView.findViewById(R.id.name);
         holder.infoicon = (ImageView) convertView.findViewById(R.id.infoicon);
         holder.showIcon = (ImageView)convertView.findViewById(R.id.menu_show);
         holder.sortIcon = (ImageView)convertView.findViewById(R.id.drag_handle);
         holder.showIcon.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int pos = (Integer) arg0.getTag();
//				int item = (int) getItem(pos);
				mBtnListener.onClick(pos, mHideMenu);
//				if(pos >= mMenus.size()){//add hide menu to show menus
//					mHideMenus.remove(pos-mMenus.size());
//			    	mMenus.add(item);			    	 
//			    }else{//add show menu to show menus
//			    	 mMenus.remove(pos);
//			    	 mHideMenus.add(item);
//			    }
//				notifyDataSetChanged();
			}
		});
         convertView.setTag(holder);
     }else{
       holder = (ViewHolder) convertView.getTag();
     }
     int item = (int) getItem(position);
     holder.image.setImageResource(MenuType.getIcon(item));
     holder.image.setVisibility(View.VISIBLE);
     holder.name.setText(MenuType.getName(item));
     holder.showIcon.setTag(position);
     holder.sortIcon.setTag(position);
     
    
     if( mEditable == false ){
//    	 convertView.setBackgroundColor(parent.getContext().getResources().getColor(R.color.white));
    	 holder.infoicon.setVisibility(View.VISIBLE);
    	 holder.showIcon.setVisibility(View.GONE);
    	 holder.sortIcon.setVisibility(View.GONE);
     }else{
    	 holder.infoicon.setVisibility(View.GONE);
    	 holder.showIcon.setVisibility(View.VISIBLE); 	
    	 if( mHideMenu == true ){
//    		 convertView.setBackgroundColor(parent.getContext().getResources().getColor(R.color.lightgray));
    		 holder.sortIcon.setVisibility(View.GONE);
    		 holder.showIcon.setImageResource(R.drawable.menu_show);
    	 }else{
//    		 convertView.setBackgroundColor(parent.getContext().getResources().getColor(R.color.white));
    		 holder.sortIcon.setVisibility(View.VISIBLE);
    		 holder.showIcon.setImageResource(R.drawable.menu_hide);
    	 }
     }
     return convertView;
  }

}