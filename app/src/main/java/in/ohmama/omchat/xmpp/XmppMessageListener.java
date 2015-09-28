package in.ohmama.omchat.xmpp;

import android.content.Intent;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.util.Date;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.OmApplication;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.model.type.MsgInOut;
import in.ohmama.omchat.ui.activity.ChattingActivity;
import in.ohmama.omchat.util.FileUtil;
import in.ohmama.omchat.util.NotificationUtil;

/**
 * Created by yanglone on 9/19/15.
 * msg receive listner
 */
public class XmppMessageListener implements PacketListener {

    //    OmMessageDao msgDao;
    ComingMsgService comingMsgService;
    MessageService messageService;

    public XmppMessageListener() {
        this.messageService = DbUtil.getMessageService();
        comingMsgService = DbUtil.getComingMessageService();
    }

    @Override
    public void processPacket(Packet packet) {
        final Message nowMessage = (Message) packet;
        // 会话类型（单聊、群聊等）
        Message.Type type = nowMessage.getType();
        if (type == Message.Type.chat && !nowMessage.getBody().equals("")) {
            // 类型为文本、图片、声音或视频
            int fileType = Constants.MSG_TYPE_TXT;
            String msgBody = null;
            OmMessage msg = new OmMessage();

            //判断是否图片
            if (nowMessage.getProperty(Constants.KEY_PROPERTY_MEDIA) != null) {
                int fileExt = FileUtil.getType(nowMessage.getBody());
                if (fileExt == FileUtil.SOUND) {
                    // 媒体时长
                    int duration = (int) nowMessage.getProperty(Constants.KEY_PROPERTY_TIME_DURATION);
                    msg.setMediaDuration(duration);
                    // 媒体文件路径
                    msgBody = Constants.SOUND_PATH + "/" + nowMessage.getBody();
                    fileType = Constants.MSG_TYPE_SOUND;
                } else if (fileExt == FileUtil.IMG) {
                    msgBody = Constants.IMAGE_PATH + "/" + nowMessage.getBody();
                    fileType = Constants.MSG_TYPE_IMG;
                } else if (fileExt == FileUtil.MOVIE) {
                    msgBody = Constants.VIDEO_PATH + "/" + nowMessage.getBody();
                    fileType = Constants.MSG_TYPE_VIDEO;
                }
                // 将媒体文件保存到本地
                FileUtil.saveFileByBase64(nowMessage.getProperty(Constants.KEY_PROPERTY_MEDIA).toString(), msgBody);
            } else
                msgBody = nowMessage.getBody();

            String userName = XmppTool.getUsername(nowMessage.getFrom());
            msg.setTypeId(fileType);
            msg.setIsRead(false);
            msg.setInOut(MsgInOut.MSG_IN);
            msg.setTextMsg(msgBody);
            msg.setTime(new Date());
            msg.setUserName(userName);
            messageService.save(msg);

            // new msg count
            comingMsgService.saveComingMsg(userName);
            OmApplication.getContext().sendBroadcast(new Intent(Constants.ACTION_MSG_REV));
            // 发送notification
            Intent toChating = new Intent(OmApplication.getContext(), ChattingActivity.class);
            toChating.putExtra(Constants.KEY_USER_NAME, userName);
            NotificationUtil.showNoti("收到新消息", toChating, Constants.NOTIFY_TYPE_MSG);
        }
    }
}
