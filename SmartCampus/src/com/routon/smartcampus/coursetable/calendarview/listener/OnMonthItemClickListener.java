package com.routon.smartcampus.coursetable.calendarview.listener;

import com.routon.smartcampus.coursetable.calendarview.DateBean;

import android.view.View;

/**
 * 日期点击接口
 */
public interface OnMonthItemClickListener {
    /**
     * @param view
     * @param date
     */
    void onMonthItemClick(View view, DateBean date);
}
