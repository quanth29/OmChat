package in.ohmama.omchat.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.OmApplication;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmUserInfo;
import in.ohmama.omchat.xmpp.RosterGetTast;
import in.ohmama.omchat.xmpp.XmppConnectHelper;
import in.ohmama.omchat.service.OmMessageRevService;
import in.ohmama.omchat.util.LogUtil;
import in.ohmama.omchat.util.SharedPreferencesUtil;

public class OmSplashActivity extends AppCompatActivity {

    private static final int GET_ROSTER_SUCCESS = 0X91;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_ROSTER_SUCCESS:
                    // if connected, to main view
                    Intent mainIntent = new Intent(OmSplashActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        if (XmppConnectHelper.getInstance().isConnect()) {
            startService(new Intent(OmSplashActivity.this, OmMessageRevService.class));
            // if connected, to main view
            Intent mainIntent = new Intent(OmSplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
//            String userUame = (String) SharedPreferencesUtil.getData(OmSplashActivity.this, Constants.PREF_KEY_USER_NAME, "");
//            OmUserInfo user = DbUtil.getUserService().loadUserFromName(userUame);
//            LogUtil.i("is connect userUame", userUame + "," + (user == null));
//            if (user != null) {
//                LogUtil.i("user != null");
//                OmApplication.userSelf = user;
//                Message msg = mHandler.obtainMessage(GET_ROSTER_SUCCESS);
//                new RosterGetTast(msg).execute();
//                startService(new Intent(OmSplashActivity.this, OmMessageRevService.class));
//                return;
//            }
        } else {
            // if not connect, try login use local data
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent mainIntent = new Intent(OmSplashActivity.this, LoginActivity.class);
                    OmSplashActivity.this.startActivity(mainIntent);
                    OmSplashActivity.this.finish();
                }
            }, 500);

        }

//        setContentView(R.layout.activity_splash);
    }

    public void init() {
        OmApplication.BASE_PATH = getFilesDir().getPath() + File.separator;
    }


}
