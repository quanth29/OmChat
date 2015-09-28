package in.ohmama.omchat;

import android.content.Intent;

import in.ohmama.omchat.util.Util;

/**
 * Created by Leon on 9/12/15.
 */
public class Constants {

    /* KEY */
    public final static String KEY_CONTACT_ID = "KEY_CONTACT_ID";
    public static final String KEY_USER_NAME = "KEY_USER_NAME";
    public static final String KEY_MSG_DATA = "KEY_MSG_DATA";
    public static final String KEY_TO_FRAGMENT = "KEY_TO_FRAGMENT";
    // mxpp
    public static final String KEY_PROPERTY_MEDIA = "imgData";
    public static final String KEY_PROPERTY_TIME_DURATION = "timeDuration";


    /* TYPE */
    public static final int NOTIFY_TYPE_MSG = 0x01;
    public static final int NOTIFY_TYPE_FRIEND = 0x11;
    // MSG TYPE TXT or OTHER
    public static final int MSG_TYPE_TXT = 0x10;
    public static final int MSG_TYPE_SOUND = 0x20;
    public static final int MSG_TYPE_IMG = 0x40;
    public static final int MSG_TYPE_VIDEO = 0x80;
    // MSG IN or OUT
    public static final int MSG_IN = 0x01;
    public static final int MSG_OUT = 0x02;
    public static final int MSG_TYPE_COUNT = 6;
    // MSG TYPE SHOW
    public static String FILE_TYPE_AUDIO = "[语音]";

    /* FILE */
    public final static String EXTRA_PATH = Util.getInstance().getExtPath() + "/omchat";
    public final static String IMAGE_PATH = EXTRA_PATH + "/images";
    public final static String SOUND_PATH = EXTRA_PATH + "/sounds";
    public final static String VIDEO_PATH = EXTRA_PATH + "/videos";
    public final static String AMR_SURFFIX = ".amr";

    /* PREF */
    public static final String APP_PREF_NAME = "OM_CHAT";
    public static final String PREF_KEY_USER_NAME = "PREF_KEY_USER_NAME";
    public static final String PREF_KEY_USER_PWD = "PREF_KEY_USER_PWD";
    public static final String ACTION_FRIEND_CHANGE = "ACTION_FRIEND_CHANGE";
    public static final String ACTION_FRIEND_REQUEST = "ACTION_FRIEND_REQUEST";
    public static final String ACTION_FRIEND_ADDED = "ACTION_FRIEND_ADDED";
    public static final String ACTION_ADD_FRIEND_REFUSED = "ACTION_ADD_FRIEND_REFUSED";

    /* SERVER */
    public static String SERVER_DOMAIN = "192.168.1.107";
    public static int SERVER_PORT = 5222;
    public static String SERVER_NAME = "localhost";

    /* DB */
    public static final String DB_NAME = "OM_CHAT";
    public static final String TB_USER = "OM_USER";
    public final static String CL_USER_NAME = "user_name";
    public static final String CL_TYPE_ID = "type_id";

    /* ACTION */
    public final static String ACTION_MSG_REV = "ACTION_MSG_REV";
    public final static String ACTION_MSG_SENT = "ACTION_MSG_SENT";
    public final static String ACTION_LOGIN_CONFLICT = "ACTION_LOGIN_CONFLICT";

    /* BEAN */
    public final static int TYPE_MSG = 0;
    public final static int TYPE_FRIEND_REQ = 1;



}
