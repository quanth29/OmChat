package in.ohmama.omchat.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.OmApplication;
import in.ohmama.omchat.R;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.ui.activity.MainActivity;

/**
 * Created by yanglone on 9/23/15.
 */
public class NotificationUtil {

    public static void clearNoti(int typeId) {
//        myNoti.number = 0;
        NotificationManager manger = (NotificationManager) OmApplication.getContext()
                .getSystemService(Service.NOTIFICATION_SERVICE);
        manger.cancel(typeId);
        manger.cancelAll();
    }

    public static void showNoti(String notiMsg, Intent toView, int typeId) {
        //android推送
//        if (notiMsg.contains(Constants.IMAGE_PATH))
//            myNoti.tickerText = "[图片]";
//        else if (notiMsg.contains(Constants.SOUND_PATH))
//            myNoti.tickerText = "[语音]";
//        else if(notiMsg.contains("[/g0"))
//            myNoti.tickerText = "[动画表情]";
//        else if(notiMsg.contains("[/f0"))  //适配表情
//            myNoti.tickerText = ExpressionUtil.getText(OmApplication.getInstance(), StringUtil.Unicode2GBK(notiMsg));
//        else if(notiMsg.contains("[/a0"))
//            myNoti.tickerText = "[位置]";
//        else {
//            myNoti.tickerText = notiMsg;
//        }

        Intent intent = new Intent();   //要跳去的界面
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(OmApplication.getContext(), MainActivity.class);

        NotificationManager mNotificationManager =
                (NotificationManager) OmApplication.getContext().getSystemService(Service.NOTIFICATION_SERVICE);
        PendingIntent appIntent = PendingIntent.getActivity(OmApplication.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ComingMsgService comingMsgService = DbUtil.getComingMessageService();
        int msgCount = comingMsgService.loadAllUnreadMsg() + comingMsgService.loadAllFriendReq();

//        if (OmApplication.sharedPreferences.getBoolean("isShake", true)) {
//            myNoti.defaults = Notification.DEFAULT_VIBRATE; // 震动
//        }
//        if (OmApplication.sharedPreferences.getBoolean("isSound", true)) {
//            myNoti.defaults = Notification.DEFAULT_SOUND; // 响铃
//        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(OmApplication.getContext())
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("收到" + msgCount + "条新信息")
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentText(notiMsg);
//        Intent resultIntent = new Intent(OmApplication.getContext(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(OmApplication.getContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(toView);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(typeId, mBuilder.build());
    }

}
