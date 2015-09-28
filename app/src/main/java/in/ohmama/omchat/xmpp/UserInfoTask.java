package in.ohmama.omchat.xmpp;

import android.os.AsyncTask;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import java.io.IOException;

import in.ohmama.omchat.OmApplication;
import in.ohmama.omchat.util.FileUtil;

/**
 * Created by yanglone on 9/17/15.
 */
public class UserInfoTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
        String userName = params[0];
        VCard vCard = new VCard();
//            vCard.load(XmppConnectHelper.getConnection(),userName);
        //
        String avatorPath = OmApplication.AVATOR_BASE_PATH + userName;
        FileUtil.saveFileByBase64(vCard.getAvatar(), avatorPath);
        return null;
    }

}
