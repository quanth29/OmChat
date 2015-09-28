package in.ohmama.omchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import org.jivesoftware.smack.XMPPException;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmMessageDao;
import in.ohmama.omchat.model.OmUserDao;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.model.service.UserService;
import in.ohmama.omchat.util.LogUtil;
import in.ohmama.omchat.util.SharedPreferencesUtil;
import in.ohmama.omchat.xmpp.XmppConnectHelper;

public class OmMessageRevService extends Service {

    private OmMessageDao messageDao;
    private MessageService messageService;
    private OmUserDao userDao;
    private UserService userService;

    private Handler mHandler = new Handler();

    public OmMessageRevService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!XmppConnectHelper.getInstance().isConnect()) {
            String usr = (String) SharedPreferencesUtil.getData(this, Constants.PREF_KEY_USER_NAME, "");
            String pwd = (String) SharedPreferencesUtil.getData(this, Constants.PREF_KEY_USER_PWD, "");
            try {
                XmppConnectHelper.getInstance().getConnection().login(usr, pwd);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Called by Settings dialog when a connection is establised with the XMPP
     * server
     */
//    public void setConnection() throws XMPPException {
//        PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
//        XmppConnectHelper.getConnection().addPacketListener(new PacketListener() {
//            @Override
//            public void processPacket(Packet packet) {
//                final Message message = (Message) packet;
//                if (message.getBody() != null) {
//                    // Add the incoming message to db
//                    mHandler.post(new Runnable() {
//                        public void run() {
//                            String fromName = StringUtils.parseBareAddress(message
//                                    .getFrom());
//                            Log.i("OmMessageRevService", "Text Recieved " + message.getBody()
//                                    + " from " + fromName);
//                            OmMessage msg = new OmMessage();
//                            OmUser fromUser = userService.loadUserFromName(fromName);
//                            if(fromUser!=null){
//                                msg.setFromUserId(fromUser.getId());
//                                msg.setTextMsg(message.getBody());
//                                msg.setToUserId(OmApplication.userSelf.getId());
//                                msg.setIsRead(false);
//                                LogUtil.i("my id is ",OmApplication.userSelf.getId());
//                                messageService.save(msg);
//
//                                // send broadcast
//                                Intent mIntent = new Intent();
//                                mIntent.setAction(Constants.ACTION_MSG_REV);
//                                mIntent.putExtra(Constants.KEY_MSG_DATA, fromUser.getId());
//                                sendBroadcast(mIntent);
//
//                            }else{
//                                ToastUtil.toast("from user is null");
//                                throw new RuntimeException();
//                            }
//
//                        }
//                    });
//                }
//            }
//        }, filter);
//    }

//    public long getMessageTime(Message message) {
//        DelayInformation delay = message.getExtension(DelayInformation.XEP_0091_UTC_FORMAT.);
//        if (delay == null) return 0;
//        if (delay.getStamp() == null) return 0;
//
//        return delay.getStamp().getTime();
//    }
}
