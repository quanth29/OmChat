package in.ohmama.omchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smackx.packet.VCard;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.OmApplication;
import in.ohmama.omchat.R;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.model.service.UserService;
import in.ohmama.omchat.model.type.FriendType;
import in.ohmama.omchat.util.LogUtil;
import in.ohmama.omchat.util.NetLoadThread;
import in.ohmama.omchat.util.ToastUtil;
import in.ohmama.omchat.xmpp.XmppConnectHelper;

public class UserInfoActivity extends BaseActivity {

    private Button btnAddConfirm;
    private TextView tvName;
    private TextView tvGender;
    private TextView tvLocal;
    private TextView tvMore;

    private String userName;
    private boolean isFirend;

    private MessageService messageService;
    private UserService userService;
    private ComingMsgService comingMsgService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        init();
    }

    public void init() {
        btnAddConfirm = (Button) findViewById(R.id.btn_add_confirm);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvGender = (TextView) findViewById(R.id.tv_gender);
        tvLocal = (TextView) findViewById(R.id.tv_local_info);
        tvMore = (TextView) findViewById(R.id.tv_tel_info);

        messageService = DbUtil.getMessageService();
        userService = DbUtil.getUserService();
        comingMsgService = DbUtil.getComingMessageService();

        userName = getIntent().getStringExtra(Constants.KEY_USER_NAME);
        isFirend = userService.isFriend(userName);

        btnAddConfirm.setOnClickListener(this);
        if (isFirend) {
            btnAddConfirm.setText("删除该好友");
        }
        userName = getIntent().getStringExtra(Constants.KEY_USER_NAME);
        getUserInfo();
    }

    public void getUserInfo() {
        new NetLoadThread(this) {

            @Override
            protected Object load() {
                VCard user = XmppConnectHelper.getInstance().getUserInfo(userName);
                return user;

            }

            @Override
            protected void result(Object object) {
                VCard user = (VCard) object;
                tvName.setText(userName);
                tvGender.setText(user.getField("sex"));
                tvLocal.setText(user.getField("adr"));
                tvMore.setText(user.getField("email"));
            }
        };
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_add_confirm:
                if (isFirend) {
                    // 删除
                    removeFriend();
                } else {
                    // 添加
                    addFriend();
                }
                break;
        }
    }

    public void removeFriend() {
        // 删除
        new NetLoadThread(this) {
            @Override
            protected Object load() {
                return XmppConnectHelper.getInstance().removeUser(userName);
            }

            @Override
            protected void result(Object object) {
                boolean isSuccess = (boolean) object;
                if (isSuccess) {
                    ToastUtil.toast("移除成功");
                    isFirend = false;
                    OmApplication.getContext().sendBroadcast(new Intent(Constants.ACTION_FRIEND_CHANGE));
                    btnAddConfirm.setText(getResources().getString(R.string.action_add_friend));
                    messageService.deleteUserMsg(userName);
                    comingMsgService.deleteComingMsg(userName);
                    LogUtil.i("remove friend to none");
                    userService.updateUserType(userName, FriendType.getValue(RosterPacket.ItemType.none));
//                    userService.deleteUser(userName);
//                    OmApplication.getContext().sendBroadcast(new Intent(Constants.ACTION_MSG_REV));
                }
            }
        };
    }

    public void addFriend() {
        new NetLoadThread(this) {

            @Override
            protected Object load() {
                return XmppConnectHelper.getInstance().addUser(userName);
            }

            @Override
            protected void result(Object object) {
                boolean isSuccess = (boolean) object;
                if (isSuccess) {
                    ToastUtil.toast("添加成功，等待通过验证");
                    OmUser addUser = userService.loadUserFromName(userName);
                    if (addUser == null) {
                        addUser = new OmUser();
                        addUser.setUserName(userName);
                        addUser.setTypeId(FriendType.getValue(RosterPacket.ItemType.none));
                        LogUtil.i("add friend still none");
                    } else {
                        LogUtil.i("add friend type id not change",addUser.getTypeId());
                    }
                    userService.saveOrUpdate(addUser);
                    OmApplication.getContext().sendBroadcast(new Intent(Constants.ACTION_FRIEND_CHANGE));
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
