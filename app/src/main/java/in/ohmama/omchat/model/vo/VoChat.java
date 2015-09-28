package in.ohmama.omchat.model.vo;

import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.OmUserInfo;

/**
 * Created by Leon on 9/11/15.
 */
public class VoChat {

    private Integer id;
    private OmMessage lastChat;
    private OmUserInfo friendInfo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OmMessage getLastChat() {
        return lastChat;
    }

    public void setLastChat(OmMessage lastChat) {
        this.lastChat = lastChat;
    }

    public OmUserInfo getFriendInfo() {
        return friendInfo;
    }

    public void setFriendInfo(OmUserInfo friendInfo) {
        this.friendInfo = friendInfo;
    }
}
