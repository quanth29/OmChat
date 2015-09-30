package in.ohmama.omchat.xmpp;

import android.util.Log;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.type.FriendType;
import in.ohmama.omchat.util.LogUtil;

/**
 * 网络连接工具类
 */
public class XmppConnectHelper {
    private static final String TAG = "XmppConnectHelper";
    private static XMPPConnection connection = null;
    private static XmppConnectHelper xmppConnection;
    public static List<OmUser> friendList = new ArrayList<>();

    private XmppConnecionListener connectionListener;
    private XmppMessageInterceptor xmppMessageInterceptor;
    private XmppMessageListener messageListener;
    // 会话对象
    private static Chat newchat;

    /**
     * 单例模式
     *
     * @return
     */
    public static XmppConnectHelper getInstance() {
        if (xmppConnection == null) {
            xmppConnection = new XmppConnectHelper();
        }
        return xmppConnection;
    }

    /**
     * 创建连接
     */
    public XMPPConnection getConnection() {
        if (connection == null || !connection.isAuthenticated()) {
            openConnection();
        }
        return connection;
    }

    public boolean isConnect() {
        return null != connection && connection.isAuthenticated();
    }

    /**
     * 打开连接
     */
    public boolean openConnection() {
        try {
            if (null == connection || !connection.isAuthenticated()) {
                XMPPConnection.DEBUG_ENABLED = true;// 开启DEBUG模式
                // 配置连接
                ConnectionConfiguration config = new ConnectionConfiguration(Constants.SERVER_DOMAIN,
                        Constants.SERVER_PORT, Constants.SERVER_NAME);
//				if (Build.VERSION.SDK_INT >= 14) {
//					config.setKeystoreType("AndroidCAStore"); //$NON-NLS-1$
//					config.setTruststorePassword(null);
//					config.setKeystorePath(null);
//				} else {
//					config.setKeystoreType("BKS"); //$NON-NLS-1$
//					String path = System.getProperty("javax.net.ssl.trustStore"); //$NON-NLS-1$
//					if (path == null)
//						path = System.getProperty("java.home") + File.separator //$NON-NLS-1$
//								+ "etc" + File.separator + "security" //$NON-NLS-1$ //$NON-NLS-2$
//								+ File.separator + "cacerts.bks"; //$NON-NLS-1$
//					config.setKeystorePath(path);
//				}
                // config.setSASLAuthenticationEnabled(false);
                config.setReconnectionAllowed(true);
                config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                config.setSASLAuthenticationEnabled(false);
                config.setSendPresence(true); // 状态设为离线，目的为了取离线消息
                connection = new XMPPConnection(config);
                connection.connect();// 连接到服务器
                // 配置各种Provider，如果不配置，则会无法解析数据
                configureConnection(ProviderManager.getInstance());
                // 添加連接監聽
                connectionListener = new XmppConnecionListener();
                connection.addConnectionListener(connectionListener);
                xmppMessageInterceptor = new XmppMessageInterceptor();
                messageListener = new XmppMessageListener();
                // 消息发送
                connection.addPacketInterceptor(xmppMessageInterceptor, new PacketTypeFilter(Message.class));
                // 消息接收
                connection.addPacketListener(messageListener, new PacketTypeFilter(Message.class));
                // 好友请求接收
                connection.addPacketListener(new XmppPresenceListener(), new PacketTypeFilter(Presence.class));
//                connection.addPacketInterceptor(new XmppPresenceInterceptor(), new PacketTypeFilter(Presence.class));
                // connection.addPacketListener(arg0, arg1);
//                ProviderManager.getInstance().addIQProvider("muc", "MZH", new MUCPacketExtensionProvider());
                return true;
            }
        } catch (XMPPException xe) {
            xe.printStackTrace();
            connection = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭连接
     */
    public void closeConnection() {
        if (connection != null) {
            connection.removeConnectionListener(connectionListener);
            ProviderManager.getInstance().removeIQProvider("muc", "MZH");
            try {
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection = null;
                xmppConnection = null;
            }
        }
    }


    /**
     * 获取用户信息
     *
     * @param user
     * @return
     */
    public VCard getUserInfo(String user) {  //null 时查自己
        try {
            VCard vcard = new VCard();
            // 加入这句代码，解决No VCard for
            ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp", new VCardProvider());
            if (user == null) {
                vcard.load(getConnection());
            } else {
                vcard.load(getConnection(), user + "@" + Constants.SERVER_NAME);
            }
            if (vcard != null)
                return vcard;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 登录
     *
     * @param account  登录帐号
     * @param password 登录密码
     * @return
     */
    public boolean login(String account, String password) {
        try {
            if (getConnection() == null) {
                return false;
            }
            boolean fuck = !getConnection().isAuthenticated() && getConnection().isConnected();
            if (!getConnection().isAuthenticated() && getConnection().isConnected()) {
                getConnection().login(account, password);
                // 更改在綫狀態
                Presence presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.available);
                getConnection().sendPacket(presence);
                return true;
            }
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 搜索好友
     *
     * @param key
     * @return
     */
    public List<String> searchUser(String key) {
        List<String> userList = new ArrayList<String>();
        try {
            UserSearchManager search = new UserSearchManager(getConnection());
            Form searchForm = search.getSearchForm("search." + Constants.SERVER_NAME);
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", key);
            ReportedData data = search.getSearchResults(answerForm, "search." + Constants.SERVER_NAME);

            Iterator<ReportedData.Row> it = data.getRows();
            ReportedData.Row row = null;
            while (it.hasNext()) {
                row = it.next();
                userList.add(row.getValues("Username").next().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * 网络获取xmpp好友
     */
    public List<OmUser> loadFriendsFromNet() {
        friendList.clear();
        Roster roster = getConnection().getRoster();
        Collection<RosterEntry> entries = roster.getEntries();

        for (RosterEntry entry : entries) {
            RosterPacket.ItemType type = entry.getType();
            // 根据用户名获取出席信息
            Presence presence = roster.getPresence(entry.getUser());
            OmUser user = new OmUser();
            user.setUserName(getUsername(entry.getUser()));
            user.setNickName(entry.getName());
            user.setSize(entry.getGroups().size());
            user.setStatus(presence.getStatus());// 状态
            user.setFrom(presence.getFrom());
            user.setTypeId(FriendType.getValue(entry.getType()));
            friendList.add(user);
        }
        return friendList;
    }


    /**
     * 发送消息，附带参数
     *
     * @param msg   如果是文件，为filePath
     * @param parms property key
     * @param datas property value
     * @throws Exception
     */
    public void sendMsgWithParms(String msg, String[] parms, Object[] datas) throws Exception {
        if (getConnection() == null) {
            throw new Exception("XmppException");
        }
        org.jivesoftware.smack.packet.Message message = new org.jivesoftware.smack.packet.Message();
        for (int i = 0; i < datas.length; i++) {
            message.setProperty(parms[i], datas[i]);
        }
        message.setBody(msg);
        newchat.sendMessage(message);
    }

    /**
     * 获取用户粗略信息
     *
     * @return
     */
    public OmUser getUserRawInfo(String userName) {
        RosterEntry en = getConnection().getRoster().getEntry(getFullUsername(userName));
        return XmppTool.rosterEntryToOmUser(en);
    }

    public List<OmUser> getAllFriendList() {
        if (friendList == null)
            loadFriendsFromNet();
        return friendList;
    }

    public List<OmUser> getFriendBothList() {
        List<OmUser> friends = new ArrayList<>();
        for (OmUser friend : friendList) {
            if (FriendType.getType(friend.getTypeId()) == RosterPacket.ItemType.both) {
                friends.add(friend);
            }
        }
        return friends;
    }

    public void setRecevier(String chatName) {
        if (getConnection() == null)
            return;

        // 创建会话
        ChatManager cm = getConnection().getChatManager();
        // 发送消息给pc服务器的好友（获取自己的服务器，和好友）
        newchat = cm.createChat(getFullUsername(chatName), null);
    }

    //发送文本消息
    public void sendMsg(String chatName, String msg) throws Exception {
        if (getConnection() == null) {
            throw new Exception("XmppException");
        }

        ChatManager cm = getConnection().getChatManager();
        // 发送消息给pc服务器的好友（获取自己的服务器，和好友）
        Chat newchat = cm.createChat(getFullUsername(chatName), null);
        newchat.sendMessage(msg);
    }

    /**
     * 删除好友
     *
     * @param userName
     * @return
     */
    public boolean removeUser(String userName) {
        if (getConnection() == null)
            return false;
        try {
            RosterEntry entry = null;
            if (userName.contains("@"))
                entry = getConnection().getRoster().getEntry(userName);
            else
                entry = getConnection().getRoster().getEntry(getFullUsername(userName));
            if (entry == null)
                entry = getConnection().getRoster().getEntry(userName);
            getConnection().getRoster().removeEntry(entry);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 添加好友 无分组
     *
     * @param userName 昵称
     * @return
     */
    public boolean addUser(String userName) {
        if (getConnection() == null)
            return false;
        try {
            getConnection().getRoster().createEntry(getFullUsername(userName), getFullUsername(userName), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void configureConnection(ProviderManager pm) {

        // Private Data Storage
        pm.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());

        // Time
        try {
            pm.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
            Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
        }

        // Roster Exchange

        pm.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());

        // Message Events
        pm.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider());

        // Chat State
        pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());

        // XHTML
        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

        // Group Chat Invitations
        pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());

        // Service Discovery # Items
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());

        // Service Discovery # Info
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

        // Data Forms
        pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

        // MUC User
        pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());

        // MUC Admin
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());

        // MUC Owner
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());

        // Delayed Delivery
        pm.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());

        // Version
        try {
            pm.addIQProvider("query", "jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            // Not sure what's happening here.
        }

        // VCard
        pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

        // Offline Message Requests
        pm.addIQProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());

        // Offline Message Indicator
        pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());

        // Last Activity
        pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

        // User Search
        pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

        // SharedGroupsInfo
        pm.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup",
                new SharedGroupsInfo.Provider());

        // JEP-33: Extended Stanza Addressing
        pm.addExtensionProvider("addresses", "http://jabber.org/protocol/address", new MultipleAddressesProvider());

        // FileTransfer
        pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());

        pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());

        // Privacy
        pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        pm.addIQProvider("command", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider());
        pm.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.MalformedActionError());
        pm.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadLocaleError());
        pm.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadPayloadError());
        pm.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadSessionIDError());
        pm.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.SessionExpiredError());
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
