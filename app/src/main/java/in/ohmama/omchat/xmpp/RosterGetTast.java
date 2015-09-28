package in.ohmama.omchat.xmpp;

import android.os.AsyncTask;
import android.os.Message;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import java.io.IOException;
import java.util.Collection;

import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.OmUserInfo;
import in.ohmama.omchat.model.OmUserDao;
import in.ohmama.omchat.util.LogUtil;

/**
 * Created by yanglone on 9/17/15.
 */
public class RosterGetTast extends AsyncTask<Void, Void, Roster> {

    private Message handleMsg;

    public RosterGetTast(Message handleMsg) {
        this.handleMsg = handleMsg;
    }

    /**
     * 将好友列表保存到db
     */
    public void saveRoster(Roster roster) throws XMPPException, IOException {
        if (roster != null) {
            // 获取所有条目，大概只是获取所有好友的意思
            Collection<RosterEntry> entries = roster.getEntries();
            LogUtil.i("获取好友列表长度：" + entries.size());
            OmUserDao userDao = DbUtil.getUserDao();
            for (RosterEntry entry : entries) {
                // 根据用户名获取出席信息
                Presence presence = roster.getPresence(entry.getUser());
                OmUser user = new OmUser();
                user.setNickName(entry.getName());
                user.setUserName(entry.getUser());
                user.setSize(entry.getGroups().size());
                user.setStatus(presence.getStatus());// 状态
                user.setFrom(presence.getFrom());
                // 头像
                userDao.insertOrReplace(user);
            }
        }

    }


    @Override
    protected Roster doInBackground(Void... params) {
        try {
            Roster roster = XmppConnectHelper.getInstance().getConnection().getRoster();
            saveRoster(roster);
            return null;
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Roster roster) {
        handleMsg.sendToTarget();
    }
}
