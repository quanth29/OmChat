package in.ohmama.omchat.model.service;

import android.content.ContentValues;

import org.jivesoftware.smack.packet.RosterPacket;

import java.util.List;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;
import in.ohmama.omchat.Constants;
import in.ohmama.omchat.model.OmComingMsg;
import in.ohmama.omchat.model.OmComingMsgDao;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.OmUserDao;
import in.ohmama.omchat.model.type.FriendType;
import in.ohmama.omchat.util.LogUtil;

/**
 * Created by Leon on 9/15/15.
 */
public class UserService extends BaseService<OmUser, Long> {

    public UserService(OmUserDao dao) {
        super(dao);
    }

    // 获得所有好友
    public List<OmUser> loadFriend() {
        QueryBuilder<OmUser> qb = getDao().queryBuilder();
        qb.whereOr(OmUserDao.Properties.TypeId.eq(FriendType.getValue(RosterPacket.ItemType.both)),
                OmUserDao.Properties.TypeId.eq(FriendType.getValue(RosterPacket.ItemType.to)));
        return qb.list();
    }

    public boolean isFriend(String userName){
        QueryBuilder<OmUser> qb = getDao().queryBuilder();
        qb.where(OmUserDao.Properties.UserName.eq(userName));
        qb.whereOr(OmUserDao.Properties.TypeId.eq(FriendType.getValue(RosterPacket.ItemType.both)),
                OmUserDao.Properties.TypeId.eq(FriendType.getValue(RosterPacket.ItemType.to)));
        return qb.count() > 0;
    }

    public OmUser loadUserFromName(String userName) {
        QueryBuilder<OmUser> qb = getDao().queryBuilder();
        qb.where(OmUserDao.Properties.UserName.eq(userName));
        return qb.unique();
    }

    public void deleteUser(String name) {
        QueryBuilder<OmUser> qb = getDao().queryBuilder();
        qb.where(OmUserDao.Properties.UserName.eq(name));
        DeleteQuery deleteQuery = qb.buildDelete();
        deleteQuery.executeDeleteWithoutDetachingEntities();
    }

    /**
     * 获得所有提出申请的好友
     * @return
     */
    public List<OmUser> loadFriendReqMsgFromComingMsg() {
        QueryBuilder<OmUser> qb = getDao().queryBuilder();
        qb.join(OmUserDao.Properties.UserName, OmComingMsg.class, OmComingMsgDao.Properties.UserName)
                .where(OmComingMsgDao.Properties.TypeId.eq(1));
        return qb.list();
    }

    /**
     *
     * @param userName
     * @param typeId 0: 消息；1：好友申请
     */
    public void updateUserType(String userName, int typeId) {
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(Constants.CL_TYPE_ID, typeId);
        getDao().getDatabase().update(Constants.TB_USER, dataToInsert, Constants.CL_USER_NAME + "=?", new String[]{userName});
    }
}
