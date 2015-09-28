package in.ohmama.omchat.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Yanglone on 5/10/15.
 */
public class NetUtil {

    private static ConnectivityManager mConnectivityManager;

    // 是否有网络链接
    public static boolean isNetworkConnected(Context context) {
        if(getconnectionMgr(context)!=null){
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    // wifi是否可用
    public static boolean isWifiConnected(Context context) {
        if(getconnectionMgr(context)!=null){
            NetworkInfo mWiFiNetworkInfo =
                    mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    // 移动网络是否可用
    public static boolean isMobileConnected(Context context) {
        if(getconnectionMgr(context)!=null){
            NetworkInfo mMobileNetworkInfo =
                    mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    private static ConnectivityManager getconnectionMgr(Context context){
        if(mConnectivityManager == null)
            mConnectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return mConnectivityManager;
    }

}
