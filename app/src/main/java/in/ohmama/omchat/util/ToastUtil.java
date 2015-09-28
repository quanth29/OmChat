package in.ohmama.omchat.util;

import android.widget.Toast;

import in.ohmama.omchat.OmApplication;

/**
 * Created by Leon on 9/15/15.
 */
public class ToastUtil {

    public static void toast(String msg) {
        Toast.makeText(OmApplication.getContext(), msg, Toast.LENGTH_LONG).show();
        LogUtil.i(msg);
    }

    public static void toast(int msgId) {
        String msg = OmApplication.getContext().getResources().getText(msgId).toString();
        Toast.makeText(OmApplication.getContext(), msg, Toast.LENGTH_LONG).show();
        LogUtil.i(msg);
    }


}
