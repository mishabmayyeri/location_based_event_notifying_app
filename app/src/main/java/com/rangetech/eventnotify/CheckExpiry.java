package com.rangetech.eventnotify;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class CheckExpiry {
    String givenDate;
    String today;
    int position;
    private boolean result=false;
    private String CHECK_DATE="CHECK";

    public CheckExpiry(String givenDate,int position) {
        this.givenDate = givenDate;
        this.position=position;
    }

    public boolean isExpired() throws ParseException {

        today = new SimpleDateFormat("dd-MM-yyy").format(Calendar.getInstance().getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date todayDate= sdf.parse(today);
        Date checkDate=sdf.parse(givenDate);

        if(todayDate.after(checkDate)) {
            Log.i(CHECK_DATE, "After"+"position ->"+position);
            result=true;
        }else if(todayDate.before(checkDate)){
            Log.i(CHECK_DATE, "Before"+"position ->"+position);
            result=false;
        }else if(todayDate.equals(checkDate)){
            result=false;
            Log.i(CHECK_DATE, "Equal"+"position ->"+position);
        }
        return result;
    }
}
