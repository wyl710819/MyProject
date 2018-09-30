package com.routon.smartcampus.message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.routon.edurelease.R;
import com.routon.inforelease.util.TimeUtils;

public class MessageAdapter extends BaseAdapter{
    private LayoutInflater inflater;//这个一定要懂它的用法及作用
    ArrayList<MessageData> mDatas;
    
    public interface BtnClickListener{
    	public void onClick(int pos,boolean hidemenu);
    };

    //构造函数:要理解(这里构造方法的意义非常强大,你也可以传一个数据集合的参数,可以根据需要来传参数)
    public MessageAdapter(Context context,ArrayList<MessageData> datas){
          this.inflater = LayoutInflater.from(context);
          mDatas = datas;
    }
    
    public void setData(ArrayList<MessageData> datas){
    	 mDatas = datas;
    }

   //这里的getCount方法是程序在加载显示到ui上时就要先读取的，这里获得的值决定了listview显示多少行
   @Override
   public int getCount() {
             //在实际应用中，此处的返回值是由从数据库中查询出来的数据的总条数
	   if( mDatas == null ){
		   return 0;
	   }
	   return mDatas.size();
   }
  
   //根据ListView所在位置返回View
   @Override
   public Object getItem(int position) {
              // TODO Auto-generated method stub
	   if( mDatas == null ){
		   return 0;
	   }
	   return mDatas.get(position);
   }

    //根据ListView位置得到数据源集合中的Id
   @Override
   public long getItemId(int position) {
             // TODO Auto-generated method stub
      return position;
   }
   
   class ViewHolder{
	   ImageView dotImage;
	   TextView titleTv;
	   TextView contentTv;
	   TextView timeTv;
   }
   
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder = null;
      if(convertView == null){
         holder = new ViewHolder();

         //把vlist layout转换成View【LayoutInflater的作用】
         convertView = inflater.inflate(R.layout.item_message, null);
         holder.timeTv = (TextView) convertView.findViewById(R.id.time_tv);
         holder.contentTv = (TextView) convertView.findViewById(R.id.content_tv);
         holder.titleTv = (TextView) convertView.findViewById(R.id.title_tv);
         holder.dotImage = (ImageView) convertView.findViewById(R.id.dot_iv);
         convertView.setTag(holder);
      }else{
    	  holder = (ViewHolder) convertView.getTag();
      }
      MessageData data = mDatas.get(position);
      holder.contentTv.setText(data.content);
      
      holder.timeTv.setText("");
      Calendar time =  null;
      if( data.time != null && data.time.isEmpty() == false ){
    	  time = TimeUtils.getFormatCalendar(data.time, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
      }
      if( time != null ){
    	  SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    	  String formatTime = sdf.format(time.getTime());
    	  holder.timeTv.setText(formatTime);
	  }
     if( data.isNew == 1 ){
    	 holder.dotImage.setVisibility(View.VISIBLE);
     }else{
    	 holder.dotImage.setVisibility(View.INVISIBLE);
     }
      holder.titleTv.setText(data.title);
      return convertView;
  }

}