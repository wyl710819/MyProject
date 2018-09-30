package com.routon.smartcampus.guideview;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by binIoter
 */
class Common {
  /**
   * 设置Component
   */
  static View componentToView(LayoutInflater inflater, Component c) {
    View view = c.getView(inflater);
    final MaskView.LayoutParams lp = new MaskView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    lp.offsetX = c.getXOffset();
    lp.offsetY = c.getYOffset();
    lp.targetAnchor = c.getAnchor();
    lp.targetParentPosition = c.getFitPosition();
    view.setLayoutParams(lp);
    return view;
  }

  /**
   * Rect在屏幕上去掉状态栏高度的绝对位置
   */
  static Rect getViewAbsRect(View view, int parentX, int parentY) {
//    int[] loc = new int[2];
//    view.getLocationInWindow(loc);
    Rect rect = new Rect();
    Rect visibleRect = new Rect();
    view.getGlobalVisibleRect(visibleRect);
    int width = visibleRect.width();
    int height = visibleRect.height();
    if( width == 0 ){
    	width = view.getMeasuredWidth();
    }
    if( height == 0 ){
    	height = view.getMeasuredHeight();
    }
    rect.set(visibleRect.left, visibleRect.top, visibleRect.left + width, visibleRect.top + height);
    rect.offset(-parentX, -parentY);
    return rect;
  }
}
