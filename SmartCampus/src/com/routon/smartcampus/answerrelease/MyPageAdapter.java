package com.routon.smartcampus.answerrelease;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class MyPageAdapter extends PagerAdapter{

	private Context context;
	private ArrayList<View>viewContainer=new ArrayList<View>();
	public MyPageAdapter(Context context,ArrayList<View>viewContainer){
		this.context=context;
		this.viewContainer=viewContainer;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		 // 返回整数的最大值
        return viewContainer.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		// TODO Auto-generated method stub
		 return view == object;
	}
	 @Override
     public Object instantiateItem(ViewGroup container, int position) {
         // 将控件添加到容器
         View v = viewContainer.get(position);
         ViewGroup parent = (ViewGroup) v.getParent();
         if (parent != null) {
        	 parent.removeView(v);
         } 
		((ViewPager)container).addView(viewContainer.get(position));
         return viewContainer.get(position);  
     }

     @Override
     public void destroyItem(ViewGroup container, int position, Object object) {
         //super.destroyItem(container, position, object);
//         container.removeView((View) object);
         ((ViewPager) container).removeView(viewContainer.get(position));
     }

}
