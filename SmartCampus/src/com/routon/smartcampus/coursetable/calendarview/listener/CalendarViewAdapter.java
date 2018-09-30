package com.routon.smartcampus.coursetable.calendarview.listener;

import com.routon.smartcampus.coursetable.calendarview.DateBean;

import android.view.View;
import android.widget.TextView;



public interface CalendarViewAdapter {
    /**
     * 返回阳历、阴历两个TextView
     *
     * @param view
     * @param date
     * @return
     */
    TextView[] convertView(View view, DateBean date);
}
