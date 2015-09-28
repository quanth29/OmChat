package in.ohmama.omchat.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Leon on 5/4/15.
 */
public class SpecialDateUtil {

    public final static String YYYYMMDD = "yyyyMMdd";
    public final static String YYYY_MM_DD = "yy/MM/dd";
    public final static String MMDD = "MMdd";
    public final static String MMDD_E = "MM月dd日 E";
    public final static String HHMM = "hh:mm";
    public final static String THIS_YEAR = Calendar.getInstance().get(Calendar.YEAR) + "";
    public final static int ONE_DAY = 1000 * 60 * 60 * 24;


    // 是否大于一天
    public static boolean isOverOneDay(Date date){
        long chatTime = date.getTime();
        long nowTime = System.currentTimeMillis();
        return (nowTime - chatTime)/ ONE_DAY > 0;
    }


    public static String formatDate(Date date){
        // 大于一天
        if(isOverOneDay(date)){
            return fromDateToString(date,YYYY_MM_DD);
        }else{
            return fromDateToString(date,HHMM);
        }
    }

    /**
     * 返回mmDD格式的今天
     *
     * @return String of mmDD
     */
    public static String getTodayMMDD() {
        return new SimpleDateFormat(MMDD).format(new Date());
    }

    /**
     * 返回mmDD E格式的日期
     *
     * @param mmddString mmdd 格式日期
     * @return Formated String Like “7月18日 星期四”
     */
    public static String weekDayMMDD(String mmddString) {
        String weekDay = null;
        try {
            Date date = new SimpleDateFormat(YYYYMMDD).parse(THIS_YEAR + mmddString);
            weekDay = new SimpleDateFormat(MMDD_E, Locale.CHINA).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return weekDay;
    }

    /**
     * 返回yyyyMMdd格式的日期
     *
     * @param mmddString
     * @return yyyyMMdd格式的日期
     */
    public static String getDateYYYYMMDD(String mmddString) {
        String dateStr = null;
        try {
            Date date = new SimpleDateFormat(YYYYMMDD).parse(THIS_YEAR + mmddString);
            dateStr = fromDateToString(date, YYYYMMDD);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    // 今天 yyyyMMdd
    public static String getToday() {
        return dateToString(new Date());
    }

    // 前一天
    public static String getLastDay(String beforeWhen) {
        try {
            Date lastDay = new SimpleDateFormat(YYYYMMDD).parse(beforeWhen);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastDay);
            calendar.add(Calendar.DATE, -1);
            return dateToString(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return getToday();
    }

    public static String dateToWeekDay(String date) {
        String weekDay = null;
        try {
            Date lastDay = new SimpleDateFormat(YYYYMMDD).parse(date);
            weekDay = new SimpleDateFormat(MMDD_E, Locale.CHINA).format(lastDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return weekDay;
    }

    // from Date to DateStr
    private static String dateToString(Date d) {
        return new SimpleDateFormat(YYYYMMDD).format(d);
    }

    private static String fromDateToString(Date d, String formatStr) {
        return new SimpleDateFormat(formatStr).format(d);
    }
}
