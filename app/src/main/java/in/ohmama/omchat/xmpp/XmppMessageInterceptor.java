package in.ohmama.omchat.xmpp;

import android.content.Intent;

import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.util.Date;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.OmApplication;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.OmMessageDao;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.model.type.MsgInOut;
import in.ohmama.omchat.util.FileUtil;

// msg sent interceptor
public class XmppMessageInterceptor implements PacketInterceptor {

    MessageService messageService;

    public XmppMessageInterceptor() {
        this.messageService = DbUtil.getMessageService();
    }

    @Override
    public void interceptPacket(Packet packet) {
        Message nowMessage = (Message) packet;
        if (nowMessage.getType() == Message.Type.chat) {
            int type = Constants.MSG_TYPE_TXT;
            OmMessage msg = new OmMessage();
            //name
            String userName = XmppTool.getUsername(nowMessage.getTo());
            msg.setUserName(userName);

            String msgBody = null;
            if (nowMessage.getProperty("imgData") != null) {
                int fileType = FileUtil.getType(nowMessage.getBody());
                if (fileType == FileUtil.SOUND) {
                    int duration = (int) nowMessage.getProperty(Constants.KEY_PROPERTY_TIME_DURATION);
                    msg.setMediaDuration(duration);
                    msgBody = Constants.SOUND_PATH + "/" + nowMessage.getBody();
                    type = Constants.MSG_TYPE_SOUND;
                } else if (fileType == FileUtil.IMG) {
                    msgBody = Constants.IMAGE_PATH + "/" + nowMessage.getBody();
                    type = Constants.MSG_TYPE_IMG;
                } else if (fileType == FileUtil.MOVIE) {
                    msgBody = Constants.VIDEO_PATH + "/" + nowMessage.getBody();
                    type = Constants.MSG_TYPE_VIDEO;
                }
            } else
                msgBody = nowMessage.getBody();

            msg.setTypeId(type);
            msg.setIsRead(false);
            msg.setInOut(MsgInOut.MSG_OUT);
            msg.setTextMsg(msgBody);
            msg.setTime(new Date());
            messageService.save(msg);

            OmApplication.getContext().sendBroadcast(new Intent(Constants.ACTION_MSG_SENT));
        }
    }
}
