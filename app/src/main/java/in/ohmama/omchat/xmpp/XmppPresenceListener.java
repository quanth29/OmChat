package in.ohmama.omchat.xmpp;

import android.content.Intent;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import java.util.Date;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.OmApplication;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.model.service.UserService;
import in.ohmama.omchat.model.type.FriendType;
import in.ohmama.omchat.model.type.MsgInOut;
import in.ohmama.omchat.ui.activity.ChattingActivity;
import in.ohmama.omchat.ui.activity.MainActivity;
import in.ohmama.omchat.util.LogUtil;
import in.ohmama.omchat.util.NotificationUtil;

/**
 * Created by yanglone on 9/19/15.
 */
public class XmppPresenceListener implements PacketListener {

    //    OmUserDao omUserDao;
    UserService userService;
    ComingMsgService comingMsgService;
    MessageService messageService;


    public XmppPresenceListener() {
//        omUserDao = DbUtil.getUserDao();
        userService = DbUtil.getUserService();
        comingMsgService = DbUtil.getComingMessageService();
        messageService = DbUtil.getMessageService();
    }

    @Override
    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        String fromId = presence.getFrom();//发送方
        String toId = presence.getTo();//接收方
        String userShortName = XmppConnectHelper.getUsername(fromId);
        OmUser fromUser = userService.loadUserFromName(userShortName);
        // 如果还没有此人，添加到db
        if (fromUser == null) {
            fromUser = new OmUser();
            fromUser.setUserName(userShortName);
        }
        //Presence.Type有7中状态
        if (presence.getType().equals(Presence.Type.subscribe)) {// 收到好友申请

            // 如果已是双向好友，不动作
            if (fromUser.getTypeId() != null &&
                    FriendType.getType(fromUser.getTypeId()) == RosterPacket.ItemType.both) {
                comingMsgService.deleteFriendRequest(userShortName);
                LogUtil.i("收到好友申请,不动作", userShortName);
                return;
            } else if (fromUser.getTypeId() != null &&
                    FriendType.getType(fromUser.getTypeId()) == RosterPacket.ItemType.to) {
                LogUtil.i("收到好友确认，添加好友 , 不动作", userShortName);
            } else {
                // 新增消息
                comingMsgService.deleteFriendRequest(userShortName);
                comingMsgService.saveFriendRequest(XmppConnectHelper.getUsername(userShortName));
                LogUtil.i("收到好友申请,新增消息", userShortName);
                // 通知
                OmApplication.getContext().sendBroadcast(new Intent(Constants.ACTION_FRIEND_REQUEST));
                Intent toMain = new Intent(OmApplication.getContext(), MainActivity.class);
                toMain.putExtra(Constants.KEY_TO_FRAGMENT,1);
                NotificationUtil.showNoti("收到新消息",toMain,Constants.TYPE_FRIEND_REQ);
            }
        } else if (presence.getType().equals(Presence.Type.subscribed)) {// 成功添加
            LogUtil.i("friend add success 成功添加 ", toId + ", userShortName " + userShortName);
            // 如果已是双向好友，不动作
            if (FriendType.getValue(RosterPacket.ItemType.both) == fromUser.getTypeId()) {
                LogUtil.i("subscribed fromUser.getTypeId()", fromUser.getTypeId());
                comingMsgService.deleteFriendRequest(userShortName);
                return;
            } else {
                fromUser.setTypeId(FriendType.getValue(RosterPacket.ItemType.both));
                LogUtil.i("subscribed save both", fromUser.getUserName() + "," + fromUser.getTypeId());
                // 保存一条新的消息，提示用户，“可以进行对话了”
                OmMessage msg = new OmMessage();
                msg.setInOut(MsgInOut.MSG_IN);
                msg.setTime(new Date());
                msg.setUserName(userShortName);
                msg.setIsRead(true);
                msg.setTextMsg("填加好友成功，你们可以对话了");
                messageService.save(msg);
                userService.saveOrUpdate(fromUser);
                // 删除消息
                comingMsgService.deleteComingMsg(userShortName);

                OmApplication.getContext().sendBroadcast(new Intent(Constants.ACTION_FRIEND_ADDED));
                // 打开对话框
                Intent toChat = new Intent(OmApplication.getContext(), ChattingActivity.class);
                toChat.putExtra(Constants.KEY_USER_NAME, userShortName);
                NotificationUtil.showNoti("好友添加成功", toChat, Constants.NOTIFY_TYPE_MSG);
            }
        } else if (presence.getType().equals(Presence.Type.unsubscribe)) {
            LogUtil.i("unsubscribe");
            // 对方删除了我
            if (FriendType.getType(fromUser.getTypeId()) != RosterPacket.ItemType.to) {
                fromUser.setTypeId(FriendType.getValue(RosterPacket.ItemType.to));
                userService.saveOrUpdate(fromUser);
            }
            comingMsgService.deleteFriendRequest(userShortName);
        } else if (presence.getType().equals(Presence.Type.unsubscribed)) {
            // 对方不同意加我|对方彻底删除了我
            LogUtil.i("unsubscribed");
            // 通知
            OmApplication.getContext().sendBroadcast(new Intent(Constants.ACTION_ADD_FRIEND_REFUSED));
            fromUser.setTypeId(FriendType.getValue(RosterPacket.ItemType.none));
            // 删除所有短信
            messageService.deleteUserMsg(userShortName);
            comingMsgService.deleteFriendRequest(userShortName);
            userService.saveOrUpdate(fromUser);
//            userService.deleteUser(fromId);
        }

    }
}
