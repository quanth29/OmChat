package in.ohmama.omchat.xmpp;

import android.content.Intent;
import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.OmApplication;
import in.ohmama.omchat.ui.activity.LoginActivity;
import in.ohmama.omchat.util.SharedPreferencesUtil;

/**
 * Created by yanglone on 9/20/15.
 */
public class XmppConnecionListener implements ConnectionListener {

    @Override
    public void connectionClosed() {
        Log.e("smack xmpp", "close");
        XmppConnectHelper.getInstance().closeConnection();
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        if(e.getMessage().contains("conflict")){
            // 删除登陆信息
            SharedPreferencesUtil.removeData(Constants.PREF_KEY_USER_PWD);

            // 清除用户信息
            OmApplication.userSelf = null;
            XmppConnectHelper.getInstance().closeConnection();
            OmApplication.getContext().sendBroadcast(new Intent(Constants.ACTION_LOGIN_CONFLICT));

            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("isRelogin", true);
            intent.setClass(OmApplication.getContext(), LoginActivity.class);
            OmApplication.getContext().startActivity(intent);
        }
    }


    @Override
    public void reconnectingIn(int seconds) {
    }

    @Override
    public void reconnectionSuccessful() {
    }

    @Override
    public void reconnectionFailed(Exception e) {
    }



}
