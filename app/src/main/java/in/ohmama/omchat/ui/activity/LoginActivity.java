package in.ohmama.omchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.List;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.OmApplication;
import in.ohmama.omchat.R;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.OmUserDao;
import in.ohmama.omchat.model.OmUserInfo;
import in.ohmama.omchat.util.LogUtil;
import in.ohmama.omchat.util.NetLoadThread;
import in.ohmama.omchat.util.SharedPreferencesUtil;
import in.ohmama.omchat.util.StringUtil;
import in.ohmama.omchat.util.ToastUtil;
import in.ohmama.omchat.xmpp.XmppConnectHelper;

public class LoginActivity extends AppCompatActivity {

    private static final int CONECTION_SUCCESS = 10;
    private static final int CONECTION_FAILD = 20;

    private EditText etUserName;
    private EditText etPassWord;
    private Button btnLogin;
    private ProgressBar pbLogin;

    private String username;
    private String password;


    //    SharedPreferences sp = null;
//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            // 取消进度框
//            pbLogin.setVisibility(View.GONE);
//
//            switch (msg.what) {
//                case CONECTION_SUCCESS:
//                    // 启动主界面
//                    Intent intent = new Intent(LoginActivity.this,
//                            MainActivity.class);
//                    startActivity(intent);
//                    // 开启服务
//                    OmMessageRevService revService = new OmMessageRevService();
//                    startService(new Intent(LoginActivity.this, OmMessageRevService.class));
//
//                    finish();
//                    break;
//                case CONECTION_FAILD:
//                    pbLogin.setVisibility(View.GONE);
//                    ToastUtil.toast("登陆失败");
//                    break;
//            }
//        }
//    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        checkLoginHistory();
        // 进度框显示
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginCheck()) {
                    loginAccount(username, password);
//                    if (NetUtil.isNetworkConnected(LoginActivity.this)) {
//                        new Thread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                try {
//                                    BuilderString(username.toString());
//                                    password = etPassWord.getText().toString();
//                                    LogUtil.i("用户信息: " + username + ":" + password);
//                                    // 连接服务器
//                                    XmppConnectHelper.getConnection().login(username, password);
//                                    LogUtil.i("登陆成功");
//                                    loginSuccess(username, password);
//                                } catch (XMPPException e) {
//                                    e.printStackTrace();
//                                    handler.sendEmptyMessage(CONECTION_FAILD);
//                                    LogUtil.i("登陆失败");
//                                } catch (IOException e) {
//                                    handler.sendEmptyMessage(CONECTION_FAILD);
//                                    LogUtil.i("写入图片失败");
//                                    e.printStackTrace();
//                                }
//                            }
//                        }).start();
//                    } else {
//                        ToastUtil.toast(getResources().getString(R.string.error_no_internet));
//                    }
                }

            }
        });
        //用户详情
        OmApplication.userSelf = new OmUserInfo(XmppConnectHelper.getInstance().getUserInfo(null));
    }

    public void init() {
        etPassWord = (EditText) this.findViewById(R.id.et_passWord);
        etUserName = (EditText) this.findViewById(R.id.et_userName);
        btnLogin = (Button) this.findViewById(R.id.btn_login);
        pbLogin = (ProgressBar) this.findViewById(R.id.progress_login);
    }

    public boolean loginCheck() {
        username = etUserName.getText().toString();
        if (StringUtil.isEmpty(username)) {
            etUserName.setError("请输入用户名");
            return false;
        }
        password = etPassWord.getText().toString();
        if (StringUtil.isEmpty(password)) {
            etPassWord.setError("请输入密码");
            return false;
        }
        return true;
    }

    public void checkLoginHistory() {
//        sp = getSharedPreferences(Constants.APP_PREF_NAME, Context.MODE_PRIVATE);
        String u = (String) SharedPreferencesUtil.getData(this, Constants.PREF_KEY_USER_NAME, "");
        String p = (String) SharedPreferencesUtil.getData(this, Constants.PREF_KEY_USER_PWD, "");
//        String u = sp.getString(Constants.PREF_KEY_USER_NAME, "");
//        String p = sp.getString(Constants.PREF_KEY_USER_PWD, "");
        if (!StringUtil.isEmpty(u) && !StringUtil.isEmpty(p)) {
            etPassWord.setText(p);
            etUserName.setText(u);
        }
    }

    private void loginAccount(final String userName, final String password) {
        new NetLoadThread(this) {

            @Override
            protected Object load() {
                boolean isSuccess = XmppConnectHelper.getInstance().login(userName, password);

                // load friend and save to db
                List<OmUser> userList = null;
                if (isSuccess) {
                    userList = XmppConnectHelper.getInstance().loadFriendsFromNet();
                    if (userList != null) {
                        OmUserDao userDao = DbUtil.getUserDao();
                        userDao.deleteAll();
                        for (OmUser omUser : userList) {
                            userDao.insertOrReplace(omUser);
                        }
                    } else {
                        isSuccess = false;
                    }
                }
                return isSuccess;
            }

            @Override
            protected void result(Object o) {
                boolean userList = (boolean) o;
                if (userList) {
                    SharedPreferencesUtil.saveData(LoginActivity.this, Constants.PREF_KEY_USER_NAME, userName);
                    SharedPreferencesUtil.saveData(LoginActivity.this, Constants.PREF_KEY_USER_PWD, password);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    ToastUtil.toast("登陆失败");
                }
            }

        };
    }

    /**
     * 转换用户名中的@符号
     *
     * @param user
     */
//    public void BuilderString(String user) {
//        if (user.contains("@")) {
//            String[] userArray = user.split("@");
//            username = userArray[0].toString() + "\\40"
//                    + userArray[1].toString();
//        }
//    }

//    public void loginSuccess(String userName, String pwd) throws XMPPException, IOException {
//        OmUserInfo omUser = DbUtil.getUserService().loadUserFromName(userName);
//        // db 没有，新建一个
//        if (omUser == null) {
//            omUser = new OmUserInfo();
//            omUser.setUserName(userName);
////            omUser.setNickName(vCard.getNickName());
//            String avatorPath = OmApplication.AVATOR_BASE_PATH + omUser.getUserName();
//            omUser.setAvatorPath(avatorPath);
//            // 保存头像到文件
//            DbUtil.getUserDao().insertOrReplace(omUser);
//            OmApplication.userSelf = DbUtil.getUserService().loadUserFromName(userName);
//        } else {
//            OmApplication.userSelf = omUser;
//        }
//
//        // 发送消息通知ui更新
//        Message msg = handler.obtainMessage(CONECTION_SUCCESS);
//        // 获取好友
//        new RosterGetTast(msg).execute();
//
//        SharedPreferencesUtil.saveData(this, Constants.PREF_KEY_USER_NAME, userName);
//        SharedPreferencesUtil.saveData(this, Constants.PREF_KEY_USER_PWD, pwd);
//    }
}
