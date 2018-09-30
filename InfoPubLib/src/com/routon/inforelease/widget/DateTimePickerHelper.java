package com.routon.inforelease.widget;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.routon.inforelease.R;

public class DateTimePickerHelper {
  public interface OnClickListener {
        /**
         * This method will be invoked when a button in the dialog is clicked.
         * 
         * @param dialog The dialog that received the click.
         * @param which The button that was clicked (e.g.
         *            {@link DialogInterface#BUTTON1}) or the position
         *            of the item clicked.
         */
        /* TODO: Change to use BUTTON_POSITIVE after API council */
        public void onClick(Calendar time);
    }
	
	private static int isDateAfter(DatePicker datepicker,TimePicker timepicker){
		//当前时间
		Calendar curTime = Calendar.getInstance();
		Calendar newTime = Calendar.getInstance();
		newTime.set(datepicker.getYear(), datepicker.getMonth(), datepicker.getDayOfMonth(), 
				timepicker.getCurrentHour(), timepicker.getCurrentMinute());
		return newTime.compareTo(curTime);
	}
	
	public static void showDateTimePicker(Context context,Calendar showTime,long minDate,long maxDate,final OnClickListener positiveListener){
		View view = View.inflate(context, R.layout.date_time_picker, null);  
        final DatePicker datePicker = (DatePicker)view.findViewById(R.id.new_act_date_picker);  
        final TimePicker timePicker = (TimePicker)view.findViewById(R.id.new_act_time_picker);  
        
        // Init DatePicker  
        datePicker.setCalendarViewShown(false);
        if( minDate > 0 ){
        	datePicker.setMinDate(minDate);
        }
        if( maxDate > 0 ){
        	datePicker.setMaxDate(maxDate);
        }
        datePicker.init(showTime.get(Calendar.YEAR), showTime.get(Calendar.MONTH), 
       		 showTime.get(Calendar.DAY_OF_MONTH), null);
          
        // Init TimePicker   
        timePicker.setIs24HourView(true); 
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			
			@Override
			public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub
				//不能小于当前时间
				if( isDateAfter(datePicker,timePicker) < 0 ){
					Calendar curTime = Calendar.getInstance();
					timePicker.setCurrentHour(curTime.get(Calendar.HOUR_OF_DAY));
					timePicker.setCurrentMinute(curTime.get(Calendar.MINUTE));
				}
				
			}
		 });
        timePicker.setCurrentHour(showTime.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(showTime.get(Calendar.MINUTE));
          
        // Build DateTimeDialog  
        AlertDialog.Builder builder = new AlertDialog.Builder(context);  
       
        builder.setView(view);  
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				 Calendar confirmTime = Calendar.getInstance();
 				 confirmTime.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
 						timePicker.getCurrentHour(),timePicker.getCurrentMinute()); 
 				 if( positiveListener != null ){
 					positiveListener.onClick(confirmTime);
 				 }
			}
		});  
        builder.setNegativeButton(android.R.string.cancel, null); 
        builder.show();  
	}
}
