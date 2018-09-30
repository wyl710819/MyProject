package com.routon.smartcampus.flower;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.GridView;
import android.widget.ListView;

/**
 * @author sj
 * @version 2017年7月5日 上午9:56:50
 */
public class ListViewAnimationFactory {
	
	public static void  setListViewLayoutAnim(ListView listView, Context context, int animId) {
        Animation animation = AnimationUtils.loadAnimation(context, animId);
        LayoutAnimationController lac = new LayoutAnimationController(animation);
        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
        lac.setDelay(1);
        listView.setLayoutAnimation(lac);
    }
	
	public static void  setGridViewLayoutAnim(GridView gridView, Context context, int animId) {
        Animation animation = AnimationUtils.loadAnimation(context, animId);
        LayoutAnimationController lac = new LayoutAnimationController(animation);
        lac.setOrder(LayoutAnimationController.ORDER_RANDOM);
        lac.setDelay(1);
        gridView.setLayoutAnimation(lac);
    }
}
