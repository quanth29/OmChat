package in.ohmama.omchat.model;

import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.model.service.UserService;

/**
 * Created by Leon on 9/17/15.
 */
public class DbUtil {

    private static UserService userService;
    private static MessageService messageService;
    private static ComingMsgService comingMsgService;

    public static OmUserDao getUserDao() {
        return DbCore.getDaoSession().getOmUserDao();
    }

    public static OmMessageDao getMessageDao() {
        return DbCore.getDaoSession().getOmMessageDao();
    }

    public static OmComingMsgDao getComingMsgDao() {
        return DbCore.getDaoSession().getOmComingMsgDao();
    }

    public static UserService getUserService() {
        if (userService == null) {
            userService = new UserService(getUserDao());
        }
        return userService;
    }

    public static MessageService getMessageService() {
        if (messageService == null) {
            messageService = new MessageService(getMessageDao());
        }
        return messageService;
    }

    public static ComingMsgService getComingMessageService() {
        if (comingMsgService == null) {
            comingMsgService = new ComingMsgService(getComingMsgDao());
        }
        return comingMsgService;
    }

}
