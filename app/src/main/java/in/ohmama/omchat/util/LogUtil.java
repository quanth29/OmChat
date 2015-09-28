package in.ohmama.omchat.util;

import android.util.Log;

/**
 * Created by Leon on 8/16/15.
 */
public class LogUtil {

    private static String TAG = "OMLOG";

    public static void i(Object... param){
        if(param == null)
            return;
        else{
            if(param.length == 1){
                Log.i(TAG, "+++++++++++" + param[0] + "++++++++++");
            }else{
                if(param[1]==null)
                    Log.i(TAG,"+++++++++++" + param[0] + "++++++++++" );
                else
                    Log.i(TAG,"+++++++++++" + param[0] + "++++++++++" + " --> " + param[1]);
            }
        }
    }

    public static void currentThread(Class clazz){
        i(clazz.getName()+" thread id", Thread.currentThread().getId());
    }

    public static void e(String saySomething){
        Log.e(TAG,saySomething);
    }

    public static void e(String saySomething,Throwable err){
        Log.e(TAG,saySomething,err);
    }
}
