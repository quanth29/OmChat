package in.ohmama.omchat.xmpp;

import org.jivesoftware.smack.RosterEntry;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.type.FriendType;

/**
 * Created by yanglone on 9/22/15.
 */
public class XmppTool {

    public static OmUser rosterEntryToOmUser(RosterEntry re){
        OmUser omUser = new OmUser();
        omUser.setUserName(getUsername(re.getUser()));
        omUser.setNickName(re.getName());
        omUser.setTypeId(FriendType.getValue(re.getType()));
        return omUser;
    }

    public static String getUsername(String fullUsername) {
        return fullUsername.split("@")[0];
    }

    /**
     * 通过username获得jid
     *
     * @param username
     * @return
     */
    public static String getFullUsername(String username) {
        return username + "@" + Constants.SERVER_NAME;
    }

}
