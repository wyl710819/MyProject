package com.routon.smartcampus.student;

import android.view.View;

public interface IPinnedHeader {

	public static final int PINNED_HEADER_GONE = 0;
    public static final int PINNED_HEADER_VISIBLE = 1;
    public static final int PINNED_HEADER_PUSHED_UP = 2;

    int getPinnedHeaderState(int position);

    void configurePinnedHeader(View header, int position);
}
